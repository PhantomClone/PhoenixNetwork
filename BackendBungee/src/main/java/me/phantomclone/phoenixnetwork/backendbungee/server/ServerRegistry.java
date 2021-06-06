package me.phantomclone.phoenixnetwork.backendbungee.server;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerRegistry {

    void start();
    void stop();

    void registerServer(String serverName, String host, int port, ServerState serverState);
    void unregisterServer(String serverName);

    List<Server> getAllServers();
    List<Server> getServersByTemplate(String templateName);
    Server getServerByServerName(String serverName);

    boolean sendCommandToCloudMaster(String uuid, String line);
    void sendPlayerToServer(ProxiedPlayer player, String serverName, Consumer<Boolean> consumer);
    void sendPlayerToServer(ProxiedPlayer player, Server server, Consumer<Boolean> consumer);

}
