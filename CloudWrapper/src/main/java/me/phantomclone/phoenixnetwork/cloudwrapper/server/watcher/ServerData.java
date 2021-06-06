/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher;

import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;

import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerData {

    boolean handlePrepare();
    boolean handleStart();
    boolean handleStop();
    boolean handleDelete();

    void changeServerState(ServerState serverState);

    int getPreparedTimes();
    int getStartedTimes();
    int getStoppedTimes();
    int getDeleteTimes();

    Map<Long, ServerState> getServerStateChanges();

}
