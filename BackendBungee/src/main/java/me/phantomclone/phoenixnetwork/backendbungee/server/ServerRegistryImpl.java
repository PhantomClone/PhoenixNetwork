/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.backendbungee.server;

import me.phantomclone.phoenixnetwork.backendbungee.BackendPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerRegistryImpl implements ServerRegistry {

    private final BackendPlugin plugin;
    private final JedisPubSub jedisPubSub;

    private final List<Server> servers;

    public static ServerRegistryImpl create(BackendPlugin plugin) { return new ServerRegistryImpl(plugin); }

    private ServerRegistryImpl(BackendPlugin plugin) {
        this.plugin = plugin;
        this.servers = new ArrayList<>();

        this.jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("messagetoserver")) {
                    ProxiedPlayer player = plugin.getProxy().getPlayer(UUID.fromString(message.split(":")[0]));
                    if (player != null) player.sendMessage(new TextComponent("§6Cloud: §7" + message.replace(message.split(":")[0], "")));
                } else if (channel.equalsIgnoreCase("RegisterServer")) {
                    plugin.getProxy().getPlayers().forEach(player -> player.sendMessage(new TextComponent(message)));
                    if (!message.contains(":")) {
                        unregisterServer(message);
                        return;
                    }
                    String[] args = message.split(":");
                    registerServer(args[0], args[1], Integer.parseInt(args[2]), ServerState.valueOf(args[3]));
                } else if (channel.equalsIgnoreCase("UpdateServer")) {
                    String[] args = message.split(":");
                    Server server = getServerByServerName(args[0]);
                    if (server != null)
                        server.setServerState(ServerState.valueOf(args[1].toUpperCase()));
                }
            }
        };
    }

    @Override
    public void start() {
        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
            try (Jedis jedis = this.plugin.getBackend().getDatabaseLib().getJedisRegistry().getJedisPool("Basics").getResource()) {
                jedis.subscribe(this.jedisPubSub, "RegisterServer", "UpdateServer", "messagetoserver");
            } catch (Exception e) { e.printStackTrace(); }
        });

    }

    @Override
    public void stop() {
        this.jedisPubSub.unsubscribe();
    }

    @Override
    public void registerServer(String serverName, String host, int port, ServerState sState) {
        if (getServerByServerName(serverName) == null) this.servers.add(new Server() {

            private ServerState serverState = sState;

            @Override
            public String getName() {
                return serverName;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public String getTemplateName() {
                return serverName.contains("_") ? serverName.split("_")[0] : serverName;
            }

            @Override
            public ServerState getServerState() {
                return serverState;
            }

            @Override
            public void setServerState(ServerState serverState) {
                this.serverState = serverState;
            }
        });
    }

    @Override
    public void unregisterServer(String serverName) {
        Server server = getServerByServerName(serverName);
        if (server != null) servers.remove(server);
    }

    @Override
    public List<Server> getAllServers() {
        return new ArrayList<>(servers);
    }

    @Override
    public List<Server> getServersByTemplate(String templateName) {
        return this.servers.stream().filter(server -> server.getTemplateName().equalsIgnoreCase(templateName)).collect(Collectors.toList());
    }

    @Override
    public Server getServerByServerName(String serverName) {
        return this.servers.stream().filter(server -> server.getName().equalsIgnoreCase(serverName)).findFirst().orElse(null);
    }

    @Override
    public boolean sendCommandToCloudMaster(String uuid, String line) {
        try (Jedis jedis = plugin.getBackend().getDatabaseLib().getJedisRegistry().getJedisPool("Basics").getResource()) {
            if (jedis == null || !jedis.isConnected()) return false;
            jedis.publish("commandtomaster", uuid + ":" + line);
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void sendPlayerToServer(ProxiedPlayer player, String serverName, Consumer<Boolean> consumer) {
        Server server = getServerByServerName(serverName);
        if (server != null) {
            sendPlayerToServer(player, server, consumer);
        } else {
            consumer.accept(false);
        }
    }

    @Override
    public void sendPlayerToServer(ProxiedPlayer player, Server server, Consumer<Boolean> consumer) {
        ServerInfo serverInfo = this.plugin.getProxy().getServerInfo(server.getName());
        if (serverInfo == null) serverInfo = this.plugin.getProxy().constructServerInfo(server.getName(), new InetSocketAddress(server.getHost(), server.getPort()), "GhastServer", false);
        player.connect(serverInfo, (b, t) -> consumer.accept(b));
    }
}
