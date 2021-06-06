/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.command.Command;
import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;
import me.phantomclone.phoenixnetwork.cloudcore.console.DefaultConsole;
import me.phantomclone.phoenixnetwork.cloudcore.console.DefaultConsoleReader;
import me.phantomclone.phoenixnetwork.cloudcore.master.Master;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.AuthenticationPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudwrapper.network.WrapperPacketListener;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.ServerGroupRegistry;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.ServerGroupRegistryImpl;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudWrapper implements Utils {

    private final CloudLib cloudLib;
    private final ServerGroupRegistry serverGroupRegistry;

    private String wrapperName;
    private UUID wrapperUUID;

    public static CloudWrapper create() {
        return new CloudWrapper();
    }

    public static CloudWrapper getInstance() {
        return instance;
    }

    private static CloudWrapper instance;

    public static List<String> lobbyAddresses;

    private CloudWrapper() {
        instance = this;
        this.cloudLib = CloudLib.createCloudLib();
        this.serverGroupRegistry = ServerGroupRegistryImpl.create();
    }

    public void start() {
        log("Starting Wrapper...");

        new File("./authkeys").mkdirs();
        File configFile = new File("./config.json");
        if (!configFile.exists()) {
            if (getCloudLib().getCommandRegistry().getCommandByAlias("register") == null) {
                Command<Conversable> command = getCloudLib().getCommandFactory().createCommand("register");
                command.addSubCommand().setArgsLength(3).addFilter(2, s -> {
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }).setHelp(c -> c.sendMessage("register <WrapperName> <MasterHost> <MasterPort>")).execute((c, args) -> {
                    Integer port = Integer.parseInt(args[2]);
                    ConfigImpl config = ConfigImpl.create();
                    config.set("uuid", UUID.randomUUID());
                    config.set("name", args[0]);
                    config.set("hostname", args[1]);
                    config.set("port", port);
                    config.save(new File("./config.json"));
                    log("Data saved. Stopping Wrapper...");
                    stop();
                });
                getCloudLib().getCommandRegistry().registerCommand(command);
            }
            log("config.yml not found!");
            log("use 'register <WrapperName> <MasterHost> <MasterPort>' to get ready");
            startConsoleReading();
            return;
        }
        ConfigImpl config = ConfigImpl.create();
        config.read(configFile);
        this.wrapperName = config.get("name").toString();
        this.wrapperUUID = UUID.fromString(config.get("uuid").toString());
        config.unload();

        Master master = getCloudLib().getMasterRegistry().getMaster();
        startNetwork(master.getHostname(), master.getPort());

        addDefaultCommands();
        log("Waiting for Authentication...");
        getCloudLib().getNettyClientRegistry().getNettyClient().sendPacket(new AuthenticationPacket(getCloudLib().getAuthKeyRegistry().getKey(this.wrapperUUID), this.wrapperUUID.toString(), this.wrapperName));

        startConsoleReading();
    }

    public void stop() {
        getServerGroupRegistry().stopAndUnload(getCloudLib(), b -> {
            getCloudLib().getNettyClientRegistry().stop();
            getCloudLib().stop();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getCloudLib().getServerRegistry().getServers().forEach(Server::killProcess);
            System.exit(0);
        });
    }

    private void startConsoleReading() {
        getCloudLib().getConsoleRegistry().setReader(new DefaultConsoleReader());
        getCloudLib().getConsoleRegistry().setConsole(new DefaultConsole());
        getCloudLib().getConsoleRegistry().getCurrentConsole().hello(getCloudLib());
        getCloudLib().getConsoleRegistry().getConsoleReader().start(getCloudLib());
    }

    private void startNetwork(String hostname, int port) {
        getCloudLib().getPacketListenerRegistry().register(new WrapperPacketListener(this));
        getCloudLib().getThreadPoolRegistry().submit(getCloudLib().getNettyClientRegistry().connectToServer(getCloudLib(), hostname, port));
    }


    public void startSeverGroups() {
        log("Starting.. ServerGroups");
        getServerGroupRegistry().load(getCloudLib());
        getServerGroupRegistry().startAllServerGroups(getCloudLib());
    }

    public void addDefaultCommands() {
        Command<Conversable> stopCommand = getCloudLib().getCommandFactory().createCommand("stop");
        stopCommand.addAliases("shutdown").setNonArgs(c -> {
            c.sendMessage("Stopping Wrapper...");
            stop();
        }).setFirstHelp(c -> c.sendMessage("stop"));

        getCloudLib().getCommandRegistry().registerCommand(stopCommand);
    }

    public CloudLib getCloudLib() {
        return cloudLib;
    }

    public ServerGroupRegistry getServerGroupRegistry() {
        return serverGroupRegistry;
    }
}
