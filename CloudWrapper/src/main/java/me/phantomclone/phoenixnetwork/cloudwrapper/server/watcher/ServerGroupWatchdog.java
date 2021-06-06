/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher;

import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerGroupWatchdog {

    void registerServer(Server server);
    void unregisterServer(Server server);

    boolean preparation(Server server);
    boolean start(Server server);
    boolean stop(Server server);
    boolean delete(Server server);

    void changeServerState(Server server, ServerState serverState);

}
