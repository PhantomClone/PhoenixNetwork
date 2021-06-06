/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher;

import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerDataImpl implements ServerData {

    private List<Long> prepareTimes, startTimes, stopTimes, deleteTimes;

    private HashMap<Long, ServerState> serverStates;

    public ServerDataImpl() {
        this.prepareTimes = new ArrayList<>();
        this.startTimes = new ArrayList<>();
        this.stopTimes = new ArrayList<>();
        this.deleteTimes = new ArrayList<>();
        this.serverStates = new HashMap<>();
    }

    @Override
    public boolean handlePrepare() {
        long millis = System.currentTimeMillis();
        this.prepareTimes.add(millis);
        return this.prepareTimes.stream().filter(l -> l - millis < 5000L).count() < 5L;
    }

    @Override
    public boolean handleStart() {
        long millis = System.currentTimeMillis();
        this.startTimes.add(millis);
        return this.startTimes.stream().filter(l -> l - millis < 5000L).count() < 5L;
    }

    @Override
    public boolean handleStop() {
        long millis = System.currentTimeMillis();
        this.stopTimes.add(millis);
        return this.stopTimes.stream().filter(l -> l - millis < 5000L).count() < 5L;
    }

    @Override
    public boolean handleDelete() {
        long millis = System.currentTimeMillis();
        this.deleteTimes.add(millis);
        return this.deleteTimes.stream().filter(l -> l - millis < 5000L).count() < 5L;
    }

    @Override
    public void changeServerState(ServerState serverState) {
        //Should not be replaced.. pretty unlikely that the key currentTimeMillis already exists -> could be buggy
        this.serverStates.put(System.currentTimeMillis(), serverState);
    }

    @Override
    public int getPreparedTimes() {
        return this.prepareTimes.size();
    }

    @Override
    public int getStartedTimes() {
        return this.startTimes.size();
    }

    @Override
    public int getStoppedTimes() {
        return this.stopTimes.size();
    }

    @Override
    public int getDeleteTimes() {
        return this.deleteTimes.size();
    }

    @Override
    public Map<Long, ServerState> getServerStateChanges() {
        return this.serverStates;
    }
}