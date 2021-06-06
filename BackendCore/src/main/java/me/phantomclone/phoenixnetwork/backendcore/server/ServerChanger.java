package me.phantomclone.phoenixnetwork.backendcore.server;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerChanger<P> {

    void unregister();

    void sendPlayerToServer(P player, String host, String port);
    void sendPlayerToServer(P player, String serverName);

}
