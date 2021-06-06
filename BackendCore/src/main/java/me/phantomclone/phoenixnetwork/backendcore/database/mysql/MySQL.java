/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql;

import me.phantomclone.phoenixnetwork.backendcore.thread.ThreadPoolRegistry;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MySQL {

    private ConnectionManager connectionManager;
    private ThreadPoolRegistry threadPoolRegistry;

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MySQL(ThreadPoolRegistry threadPoolRegistry) {
        this.threadPoolRegistry = threadPoolRegistry;
    }

    public boolean connect(String host, String port, String username, String password, String database) {
        this.connectionManager = new ConnectionManager(host, port, username, password, database);
        return this.connectionManager.open();
    }

    public void disconnect() {
        if (this.connectionManager != null)
            this.connectionManager.close();
    }

    public ThreadPoolRegistry getThreadPoolRegistry() {
        return threadPoolRegistry;
    }
}