/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.command.Command;
import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;
import me.phantomclone.phoenixnetwork.cloudcore.console.DefaultConsole;
import me.phantomclone.phoenixnetwork.cloudcore.console.DefaultConsoleReader;
import me.phantomclone.phoenixnetwork.cloudcore.master.Master;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.StopPacket;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.TemplateActionPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateType;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;
import me.phantomclone.phoenixnetwork.cloudmaster.network.MasterPacketListener;
import me.phantomclone.phoenixnetwork.cloudmaster.redis.JedisFactory;
import me.phantomclone.phoenixnetwork.cloudmaster.redis.JedisFactoryImpl;
import me.phantomclone.phoenixnetwork.cloudmaster.redis.JedisRegistry;
import me.phantomclone.phoenixnetwork.cloudmaster.redis.JedisRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudmaster.server.ServerFactory;
import me.phantomclone.phoenixnetwork.cloudmaster.server.ServerFactoryImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.util.Arrays;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudMaster implements Utils {

    private final CloudLib cloudLib;

    private final JedisFactory jedisFactory;
    private final JedisRegistry jedisRegistry;

    private JedisPubSub jedisPubSub;

    private final ServerFactory serverFactory;

    private boolean isConnectedWithRedis;

    public static CloudMaster create() { return new CloudMaster(); }

    private CloudMaster() {
        this.cloudLib = CloudLib.createCloudLib();
        this.serverFactory = ServerFactoryImpl.create();

        this.jedisFactory = JedisFactoryImpl.create();
        this.jedisRegistry = JedisRegistryImpl.create();
    }

    public void start() {
        log("Starting Master...");
        Master master = null;
        try {
            master = getCloudLib().getMasterRegistry().getMaster();
        } catch (NullPointerException e) { }
        if (master == null) {
            if (getCloudLib().getCommandRegistry().getCommandByAlias("setport") == null) {
                Command<Conversable> command = getCloudLib().getCommandFactory().createCommand("setport");
                command.addSubCommand().setArgsLength(1).addFilter(0, s -> {
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }).setHelp(c -> c.sendMessage("setport <port>")).execute((c, args) -> {
                    Integer port = Integer.parseInt(args[0]);
                    ConfigImpl config = ConfigImpl.create();
                    config.set("hostname", "localhost");
                    config.set("port", port);
                    config.save(new File("./config.json"));
                    log("Data saved. Stopping Master...");
                    stop();
                });
                getCloudLib().getCommandRegistry().registerCommand(command);
            }
            log("config.yml not found!");
            log("use 'setport <port>' to get ready");
            startConsoleReading();
            return;
        }
        addDefaultCommands();

        startJedis();

        startNetwork(master.getPort());
        startConsoleReading();
        log("Master started!");
    }

    public void stop() {
        getCloudLib().getWrapperRegistry().getOnlineWrappers().forEach(wrapper -> getCloudLib().getNettyServerRegistry().sendPacket(new StopPacket("Cloud Master stopping..."), wrapper.getChannel()));
        getCloudLib().getNettyServerRegistry().stop();
        getCloudLib().stop();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (this.jedisPubSub != null) {
                this.jedisPubSub.unsubscribe();
            }
        } catch (Exception e) { }
        if (getJedisRegistry().getJedisPool() != null && !getJedisRegistry().getJedisPool().isClosed()) {
            getJedisRegistry().getJedisPool().close();
        }
        log("Master stopped!");
        System.exit(0);
    }

    private void startConsoleReading() {
        getCloudLib().getConsoleRegistry().setReader(new DefaultConsoleReader());
        getCloudLib().getConsoleRegistry().setConsole(new DefaultConsole());
        getCloudLib().getConsoleRegistry().getCurrentConsole().hello(getCloudLib());
        getCloudLib().getConsoleRegistry().getConsoleReader().start(getCloudLib());
    }

    private void startNetwork(int port) {
        getCloudLib().getPacketListenerRegistry().register(new MasterPacketListener(this));
        getCloudLib().getThreadPoolRegistry().submit(getCloudLib().getNettyServerRegistry().startServer(getCloudLib(), port));
    }

    private void startJedis() {
        getCloudLib().getThreadPoolRegistry().submit(() -> {
            try (Jedis jedis = getJedisRegistry().getJedisPool().getResource()) {
                this.isConnectedWithRedis = true;
            } catch (Exception e) {
                this.isConnectedWithRedis = false;
                log("Redis could not connect!");
                return;
            }
            getCloudLib().getServerRegistry().callWhenRegistered(server -> {
                try (Jedis jedis = getJedisRegistry().getJedisPool().getResource()) {
                    String serverString = getCloudLib().getWrapperRegistry().getWrapper(server.getTemplate().getWrapperName()).getAddress() + ":" + server.getPort() + ":" + server.getServerState().toString();
                    jedis.publish("RegisterServer", server.getName() + ":" + serverString);
                    jedis.hset("servers", server.getName(), serverString);
                }
            });
            getCloudLib().getServerRegistry().callWhenUnregistered(server -> {
                try (Jedis jedis = getJedisRegistry().getJedisPool().getResource()) {
                    jedis.hdel("servers", server.getName());
                    jedis.publish("RegisterServer", server.getName());
                }
            });

            Jedis jedis = getJedisRegistry().getJedisPool().getResource();
            jedis.subscribe(this.jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    try {
                        String uuid = message.split(":")[0];
                        String[] args = message.split(":")[1].split(" ");
                        Conversable conversable = msg -> {
                            try (Jedis messageBackJedis = getJedisRegistry().getJedisPool().getResource()) {
                                messageBackJedis.publish("messagetoserver", uuid + ":" + msg);
                            }
                        };
                        System.out.println(message);
                        getCloudLib().getCommandRegistry().getCommandByAlias(args[0]).executeCommand(conversable, Arrays.copyOfRange(args, 1, args.length));
                    } catch (Exception e) {
                        log("CommandToMaster Error. <" + message + ">");
                    }
                }
            }, "commandtomaster");
        });
    }

    public void addDefaultCommands() {
        Command<Conversable> stopCommand = getCloudLib().getCommandFactory().createCommand("stop");
        stopCommand.addAliases("shutdown").setNonArgs(c -> {
            c.sendMessage("Stopping Master...");
            stop();
        }).setFirstHelp(c -> c.sendMessage("|-------Stop------")).setLastHelp(c -> c.sendMessage("|stop"));

        Command<Conversable> templateCommand = getCloudLib().getCommandFactory().createCommand("template");
        templateCommand.setFirstHelp(c -> c.sendMessage("|----------Template----------"));
        templateCommand.addSubCommand().setArgsLength(10).setHelp(c -> {
            c.sendMessage("|template create <TemplateName> <WrapperName> <TemplateType> <ServerType> <StartPort> <EndPort> <MinMemory> <MaxMemory> <MinOnlineServers>");
            c.sendMessage("|  | TemplateType can be LOBBY, STATIC or DYNAMIC");
            c.sendMessage("|  | ServerType can be PROXY or MINECRAFT");
        }).addFilter(0, s -> s.equalsIgnoreCase("create"))
            .addFilter(2, s -> getCloudLib().getWrapperRegistry().getWrapper(s) != null, (c, msg) -> c.sendMessage("Wrapper <" + msg + "> is not Online"))
            .addFilter(3, s -> s.equalsIgnoreCase("LOBBY") || s.equalsIgnoreCase("STATIC") || s.equalsIgnoreCase("DYNAMIC"), (c, msg) -> {
                c.sendMessage("Invalid TemplateType! <" + msg + ">");
                c.sendMessage("| TemplateType can be LOBBY, STATIC or DYNAMIC");
            })
            .addFilter(4, s -> s.equalsIgnoreCase("PROXY") || s.equalsIgnoreCase("MINECRAFT"), (c, msg) -> {
                c.sendMessage("Invalid ServerType! <" + msg + ">");
                c.sendMessage("| ServerType can be PROXY or MINECRAFT");
            })
            .addFilter(5, s -> { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }, (c, msg) -> c.sendMessage("StartPort <" + msg + "> have to be a Integer"))
            .addFilter(6, s -> { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }, (c, msg) -> c.sendMessage("EndPort <" + msg + "> have to be a Integer"))
            .addFilter(7, s -> { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }, (c, msg) -> c.sendMessage("MinMemory <" + msg + "> have to be a Integer"))
            .addFilter(8, s -> { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }, (c, msg) -> c.sendMessage("MaxMemory <" + msg + "> have to be a Integer"))
            .addFilter(9, s -> { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }, (c, msg) -> c.sendMessage("MinOnlineServers <" + msg + "> have to be a Integer"))
                .execute((c, args) -> {
                    File file = new File(getCloudLib().getTemplateRegistry().getTemplateFolder(), args[1]);
                    if (!file.exists()) {
                        c.sendMessage("You have to create first a filled Template Folder! " + getCloudLib().getTemplateRegistry().getTemplateFolder() + "/" + args[1] + "/");
                        return;
                    }
                    if (file.list() == null || Arrays.stream(file.list()).noneMatch(s -> s.equalsIgnoreCase("server.jar"))) {
                        c.sendMessage("You have to put server.jar in the Template Folder!");
                        return;
                    }
                    ServerType serverType = ServerType.valueOf(args[4].toUpperCase());
                    if (serverType.equals(ServerType.PROXY) && Arrays.stream(file.list()).anyMatch(s -> s.equalsIgnoreCase("config.yml"))) {
                        c.sendMessage("You have to put the config.yml in the Template Folder!");
                        return;
                    }
                    c.sendMessage("Template creating and sending to Wrapper...");
                    getCloudLib().getThreadPoolRegistry().submit(() -> c.sendMessage(getCloudLib().getTemplateFactory().createTemplate(getCloudLib(), args[2], args[1], TemplateType.valueOf(args[3].toUpperCase()), serverType, Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9])) ? "Template created!" : "Template could not be created!"));
                });
        templateCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|template update <TemplateName>"))
            .addFilter(0, s -> s.equalsIgnoreCase("update")).addFilter(1, s -> getCloudLib().getTemplateRegistry().getTemplate(s) != null)
                .execute((c, args) -> {
                    Template template = getCloudLib().getTemplateRegistry().getTemplate(args[1]);
                    Wrapper wrapper = getCloudLib().getWrapperRegistry().getWrapper(template.getWrapperName());
                    if (wrapper == null) {
                        c.sendMessage("Wrapper is not Online!");
                        return;
                    }
                    c.sendMessage("Packing and Sending...");
                    getCloudLib().getThreadPoolRegistry().submit(() -> {
                        TemplateActionPacket packet = new TemplateActionPacket(TemplateActionPacket.Type.UPDATE, template);
                        packet.loadTemplateFolderInPacket(getCloudLib());
                        getCloudLib().getNettyServerRegistry().sendPacket(packet, wrapper.getChannel());
                        c.sendMessage("Done");
                    });
                });
        templateCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|template delete <TemplateName>"))
            .addFilter(0, s -> s.equalsIgnoreCase("delete")).addFilter(1, s -> getCloudLib().getTemplateRegistry().getTemplate(s) != null)
                .execute((c, args) -> {
                    Template template = getCloudLib().getTemplateRegistry().getTemplate(args[1]);
                    Wrapper wrapper = getCloudLib().getWrapperRegistry().getWrapper(template.getWrapperName());
                    if (wrapper == null) {
                        c.sendMessage("Wrapper is not Online!");
                        return;
                    }
                    c.sendMessage("Deleting Template...");
                    getCloudLib().getNettyServerRegistry().sendPacket(new TemplateActionPacket(TemplateActionPacket.Type.REMOVE, template), wrapper.getChannel());
                    getCloudLib().getTemplateRegistry().unloadTemplate(template.getName());
                    template.delete();
                    c.sendMessage("Done");
                });

        Command<Conversable> serverCommand = getCloudLib().getCommandFactory().createCommand("server");
        serverCommand.setFirstHelp(c -> c.sendMessage("|-------Server-------"));
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server register <TemplateName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("register")).addFilter(1, s -> getCloudLib().getTemplateRegistry().getTemplate(s) != null)
                .execute((c, args) -> {
                    Template template = getCloudLib().getTemplateRegistry().getTemplate(args[1]);
                    c.sendMessage("Try to register new server...");
                    getServerFactory().createServer(template, getCloudLib(), server -> {
                        if (server == null) c.sendMessage("A new server could not be registered!"); else c.sendMessage("Server register! (" + server.getName() + ")");
                    });
                });
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server prepare <ServerName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("prepare")).addFilter(1, s -> getCloudLib().getServerRegistry().getServer(s) != null)
                .execute((c, args) -> {
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Try to prepare server...");
                    server.preparation(getCloudLib(), b -> { if (!b) c.sendMessage("Server can not be prepared!"); else c.sendMessage("Server is prepared"); });
                });
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server start <ServerName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("start")).addFilter(1, s -> getCloudLib().getServerRegistry().getServer(s) != null)
                .execute((c, args) -> {
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Try to start server...");
                    server.start(getCloudLib(), b -> { if (!b) c.sendMessage("Server can not be started!"); else c.sendMessage("Server is started"); });
                });
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server stop <ServerName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("stop")).addFilter(1, s -> getCloudLib().getServerRegistry().getServer(s) != null)
                .execute((c, args) -> {
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Try to stop server...");
                    server.stop(getCloudLib(), b -> { if (!b) c.sendMessage("Server can not be stopped!"); else c.sendMessage("Server is stopped"); });
                });
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server delete <ServerName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("delete")).addFilter(1, s -> getCloudLib().getServerRegistry().getServer(s) != null)
                .execute((c, args) -> {
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Try to delete server...");
                    server.stop(getCloudLib(), b -> { if (!b) c.sendMessage("Server can not be deleted!"); else c.sendMessage("Server is deleted"); });
                });
        serverCommand.addSubCommand().setArgsLength(2).setHelp(c -> c.sendMessage("|server info <ServerName>"))
                .addFilter(0, s -> s.equalsIgnoreCase("info")).addFilter(1, s -> getCloudLib().getServerRegistry().getServer(s) != null)
                .execute((c, args) -> {
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Name: " + server.getName());
                    c.sendMessage("Template: " + server.getTemplate().getName());
                    c.sendMessage("Port: " + server.getPort());
                    c.sendMessage("Wrapper: " + server.getTemplate().getWrapperName());
                    c.sendMessage("Server State: " + server.getServerState().name());
                    c.sendMessage("Server Type: " + server.getServerType().name());
                });
        serverCommand.addSubCommand().setArgsLength(1).setHelp(c -> c.sendMessage("|server list"))
                .addFilter(0, s -> s.equalsIgnoreCase("list"))
                .execute((c, args) -> {
                    c.sendMessage("Registered Servers:");
                    getCloudLib().getServerRegistry().getServers().forEach(server -> c.sendMessage(server.getName() + " - " + server.getServerState().name()));
                });
        serverCommand.addSubCommand().setHelp(c -> c.sendMessage("|server command <ServerName> <Command Line>"))
                .addFilter(0, s -> s.equalsIgnoreCase("command"))
                .execute((c, args) -> {
                    if (args.length < 3) return;
                    Server server = getCloudLib().getServerRegistry().getServer(args[1]);
                    c.sendMessage("Try to start server...");
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < args.length; i++)
                        builder.append(args[i]).append(" ");
                    server.sendCommand(builder.toString(), b -> { if (!b) c.sendMessage("Server can not execute the command!"); else c.sendMessage("Server execute command"); });
                });
        Command<Conversable> jedisCommand = getCloudLib().getCommandFactory().createCommand("jedis");
        jedisCommand.setFirstHelp(c -> c.sendMessage("|-------Jedis-------"));
        jedisCommand.addSubCommand().setHelp(c -> c.sendMessage("| jedis register <Host> <Port> <Password>")).setArgsLength(4)
                .addFilter(0, s -> s.equalsIgnoreCase("register"))
                .addFilter(2, s -> {try {Integer.parseInt(s); return true; } catch (Exception e) {return false;}})
        .execute((c, args) -> {
            try {
                getJedisFactory().createJedis(args[1], Integer.parseInt(args[2]), args[3].equalsIgnoreCase("null") ? "" : args[3]);
                c.sendMessage("Redis data saved!");
                c.sendMessage("Restart to start Jedis");
                startJedis();
            } catch (Exception e) {
                c.sendMessage("Failed to Save Jedis data!");
            }
        });

        getCloudLib().getCommandRegistry().registerCommand(jedisCommand);
        getCloudLib().getCommandRegistry().registerCommand(serverCommand);
        getCloudLib().getCommandRegistry().registerCommand(stopCommand);
        getCloudLib().getCommandRegistry().registerCommand(templateCommand);
    }

    public void serverUpdate(Server server, ServerState state) {
        //getCloudLib().getThreadPoolRegistry().submit(() -> {
        if (!this.isConnectedWithRedis) return;
        if (getJedisRegistry().getJedisPool() == null || getJedisRegistry().getJedisPool().isClosed()) return;
        try (Jedis jedis = getJedisRegistry().getJedisPool().getResource()) {
            jedis.publish("UpdateServer", server.getName() + ":" + state.toString());
            String serverString = getCloudLib().getWrapperRegistry().getWrapper(server.getTemplate().getWrapperName()).getAddress() + ":" + server.getPort() + ":" + server.getServerState().toString();
            jedis.hset("servers", server.getName(), serverString);
        }
        //});
    }

    public CloudLib getCloudLib() {
        return cloudLib;
    }

    public ServerFactory getServerFactory() {
        return serverFactory;
    }

    public JedisRegistry getJedisRegistry() {
        return jedisRegistry;
    }

    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

}
