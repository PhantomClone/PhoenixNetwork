/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendbungee;

import com.google.common.collect.Lists;
import me.phantomclone.phoenixnetwork.backendbungee.server.ServerRegistry;
import me.phantomclone.phoenixnetwork.backendbungee.server.ServerRegistryImpl;
import me.phantomclone.phoenixnetwork.backendbungee.storage.BasicData;
import me.phantomclone.phoenixnetwork.backendbungee.storage.DataLoadOutDBEvent;
import me.phantomclone.phoenixnetwork.backendbungee.storage.DataStoreInDBEvent;
import me.phantomclone.phoenixnetwork.backendbungee.storage.StorageRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.Backend;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class BackendPlugin extends Plugin implements Listener {

    private Backend<ProxiedPlayer> backend;
    private ServerRegistry serverRegistry;

    @Override
    public void onEnable() {
        this.backend = ProxyBackend.create(this);
        this.backend.getDatabaseLib().getJedisRegistry().load();
        this.backend.getStorageRegistry().init();

        this.serverRegistry = ServerRegistryImpl.create(this);
        this.serverRegistry.start();

        getProxy().getPluginManager().registerListener(this, this);

        this.backend.getStorageRegistry().registerStorable(BasicData.class,
                new BiConsumer<ProxiedPlayer, Map<String, Object>>() {
                    @Override
                    public void accept(ProxiedPlayer player, Map<String, Object> map) {
                        map.put("name", player.getName());
                        map.put("firstLogin", System.currentTimeMillis());
                        map.put("lastLogin", System.currentTimeMillis());
                        map.put("playtime", 0L);
                        map.put("nameHistory", Lists.newArrayList(player.getName()));
                        map.put("language", 0);
                    }
                });
        getProxy().getPluginManager().registerCommand(this, new Command("removeBlockedUUID") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                //Just to save
                ((StorageRegistryImpl)backend.getStorageRegistry()).removeBlockedUUID(args[0]);
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

    public Backend<ProxiedPlayer> getBackend() {
        return backend;
    }

    public ServerRegistry getServerRegistry() {
        return serverRegistry;
    }
}
