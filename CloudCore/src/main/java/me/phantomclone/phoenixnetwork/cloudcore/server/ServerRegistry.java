/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;

import java.util.List;
import java.util.function.Consumer;

/**
 * <p>A interface that represents a registry for server.</p>
 *
 * <p>It register and unregister server and invoke callbacks.</p>
 *
 * <p>It can give sorfted servers back.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerRegistry {

    /**
     * Register non-null {@link Server} and accept the register callback. It also accept a callback when it there is a specific callback set for the server.
     * @param server The non-null {@link Server} which will be registered and accepted.
     */
    void registerServer(@NonNull Server server);

    /**
     * Unregister non-null {@link Server} and accept the unregister callback. It also accept a callback when it there is a specific callback set for the server.
     * @param server The non-null {@link Server} which will be registered and accepted.
     */
    void unregisterServer(@NonNull Server server);

    /**
     * Returns all registered server which the same {@link ServerType} as given.
     * @param type Compare this non-null {@link ServerType} with all other.
     * @return Returns a list with all registered server with the same {@link ServerType}.
     */
    List<Server> getServers(@NonNull ServerType type);

    /**
     * Returns all registered server which the same {@link Wrapper} as given.
     * @param wrapper Compare this non-null {@link Wrapper} with all other.
     * @return Returns a list with all registered server with the same {@link Wrapper}.
     */
    List<Server> getServers(@NonNull Wrapper wrapper);

    /**
     * Returns all registered servers.
     * @return Returns all registered servers.
     */
    List<Server> getServers();

    /**
     * Return a specific server with the same name if it is registered else it returns null.
     * @param serverName Search throw all registered server to find the server with this name.
     * @return Returns a specific server with the same name if it is registered else it returns null.
     */
    Server getServer(@NonNull String serverName);

    /**
     * Set a Callback for a specific {@link Server} when it get registered and accept the {@link Server} when the {@link Server} will be registered.
     * @param serverName The specific non-null server name which will be accepted when the server will be registered.
     * @param callback The non-null callback which will be accepted when the server with the same name will be registered.
     */
    void callWhenRegistered(@NonNull String serverName, @NonNull Consumer<Server> callback);

    /**
     * Set a {@link Consumer} for a specific {@link Server} when it get unregistered and accept the {@link Server} when the {@link Server} will be unregistered.
     * @param serverName The specific non-null server name which will be accepted when the server will be unregistered.
     * @param callback The non-null {@link Consumer} which will be accepted when the server with the same name will be unregistered.
     */
    void callWhenUnregistered(@NonNull String serverName, @NonNull Consumer<Server> callback);

    /**
     * Set a non-null {@link Consumer} which will be accepted when any server try to unregistered it self.
     * @param callBack The non-null {@link Consumer} which will be accepted when any server try to unregistered it self.
     */
    void callWhenUnregistered(@NonNull Consumer<Server> callBack);
    /**
     * Set a non-null {@link Consumer} which will be accepted when any server try to registered it self.
     * @param callBack The non-null {@link Consumer} which will be accepted when any server try to registered it self.
     */
    void callWhenRegistered(@NonNull Consumer<Server> callBack);

    /**
     * Returns true when the port is unused in the registered servers, else it returns false.
     * @param port The port which will be compared to all other registered server ports.
     * @return Returns true when the port is unused in the registered servers, else it returns false.
     */
    boolean isPortFree(int port);

}