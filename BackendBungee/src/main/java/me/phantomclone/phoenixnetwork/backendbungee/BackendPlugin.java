/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendbungee;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.phantomclone.phoenixnetwork.backendbungee.server.ServerRegistry;
import me.phantomclone.phoenixnetwork.backendbungee.server.ServerRegistryImpl;
import me.phantomclone.phoenixnetwork.backendbungee.storage.BasicData;
import me.phantomclone.phoenixnetwork.backendbungee.storage.DataLoadOutDBEvent;
import me.phantomclone.phoenixnetwork.backendbungee.storage.DataStoreInDBEvent;
import me.phantomclone.phoenixnetwork.backendbungee.storage.StorageRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.Backend;
import me.phantomclone.phoenixnetwork.backendcore.command.CommandImpl;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class BackendPlugin extends Plugin implements Listener {

    private Backend<ProxiedPlayer, CommandSender> backend;
    private ServerRegistry serverRegistry;

    @Override
    public void onEnable() {
        this.backend = ProxyBackend.create(this);
        this.backend.getDatabaseLib().getJedisRegistry().load();
        this.backend.getStorageRegistry().init();

        this.serverRegistry = ServerRegistryImpl.create(this);
        this.serverRegistry.start();

        getProxy().getPluginManager().registerListener(this, this);

        this.backend.getStorageRegistry().registerStorable(BasicData.class, (player, map) -> {
                    map.put("name", player.getName());
                    map.put("firstLogin", System.currentTimeMillis());
                    map.put("lastLogin", System.currentTimeMillis());
                    map.put("playtime", 0L);
                    map.put("nameHistory", Lists.newArrayList(player.getName()));
                    map.put("language", 0);
                });
        getProxy().getPluginManager().registerCommand(this, new Command("removeBlockedUUID") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                //Just to save
                ((StorageRegistryImpl)backend.getStorageRegistry()).removeBlockedUUID(args[0]);
            }
        });
        getProxy().getPluginManager().registerCommand(this, new Command("Test2") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                    long start = System.currentTimeMillis();
                    backend.getStorageRegistry().getOfflineObject(BasicData.class, UUID.fromString("791addad-5ff2-49bd-ac04-d94f58ae3e0e"), basicData -> {
                        sender.sendMessage(new TextComponent("Delay: " + (System.currentTimeMillis() - start) + "ms"));
                        sender.sendMessage(new TextComponent(new Gson().toJson(basicData)));
                    });
            }
        });

        Map.Entry<String, Command> commandEntry = getProxy().getPluginManager().getCommands().stream().filter(set -> set.getKey().equalsIgnoreCase("server")).findFirst().orElse(null);
        if (commandEntry != null) {
            getProxy().getPluginManager().unregisterCommand(commandEntry.getValue());
        }
        getProxy().getPluginManager().registerCommand(this, new Command("server") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                switch (args.length) {
                    //TODO IMPLEMENT CLOUD SERVER REGISTRY ...
                    case 1:
                        ServerInfo info = getProxy().getServerInfo(args[0]);
                        if (info == null) {
                            sender.sendMessage(new TextComponent("Server not found (" + args[0] + ")"));
                            return;
                        }
                        getBackend().getServerChanger().sendPlayerToServer((ProxiedPlayer) sender, info.getName());
                        break;
                    case 0:
                        TextComponent textComponent = new TextComponent("Servers: ");
                        getProxy().getServers().forEach((name, s) -> textComponent.addExtra("\n" + name));
                        sender.sendMessage(textComponent);
                        break;
                }
            }
        });
        commandTest();
    }

    private void commandTest() {

        me.phantomclone.phoenixnetwork.backendcore.command.Command<CommandSender> command = new CommandImpl<>("Test");
        command.firstHelpConsumer(sender -> sender.sendMessage(new TextComponent("First Help!")))
                .lastHelpConsumer(sender -> sender.sendMessage(new TextComponent("Last Help!")))
                .noArgsConsumer(sender -> sender.sendMessage(new TextComponent("U run no Args")))
                .addAliases("max")
                .addSubCommand().setHelp(sender -> sender.sendMessage(new TextComponent("SubHelp"))).addFilter(0, s -> s.equalsIgnoreCase("test")).addFilter(1, s -> Pattern.compile("-?[0-9]+").matcher(s).matches(), (sender, s) -> sender.sendMessage(new TextComponent(s + " ist keine Zahl!")))
                .execute((player, args) -> player.sendMessage(new TextComponent("Sub command run!" + player.getDisplayName())), ProxiedPlayer.class, sender -> sender.sendMessage(new TextComponent("Du bist kein Spieler!")));
        getBackend().getCommandRegistry().registerCommand(command);
    }

    @Override
    public void onDisable() {
        this.serverRegistry.stop();
        this.backend.getServerChanger().unregister();
        this.backend.getStorageRegistry().stop();
    }

    @EventHandler
    public void onStore(DataLoadOutDBEvent event) {
        if (event.getObject() instanceof BasicData) {
            BasicData data = (BasicData) event.getObject();
            data.setLastLogin(System.currentTimeMillis());
            ProxiedPlayer player = getProxy().getPlayer(event.getUuid());
            if (!data.getName().equalsIgnoreCase(player.getName())) {
                data.getNameHistory().add(player.getName());
                data.setName(player.getName());
            }
        }
    }

    @EventHandler
    public void onStore(DataStoreInDBEvent event) {
        if (event.getObject() instanceof BasicData) {
            BasicData data = (BasicData) event.getObject();
            long playtime = System.currentTimeMillis() - data.getLastLogin() + data.getPlaytime();
            data.setPlaytime(playtime);
        }
    }

    public Backend<ProxiedPlayer, CommandSender> getBackend() {
        return backend;
    }

    public ServerRegistry getServerRegistry() {
        return serverRegistry;
    }
}
