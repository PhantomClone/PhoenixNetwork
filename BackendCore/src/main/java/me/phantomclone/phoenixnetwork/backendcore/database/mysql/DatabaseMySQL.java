/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DatabaseMySQL {

    private ConnectionManager connectionManager;

    public boolean connect(String host, String port, String username, String password, String database) {
        this.connectionManager = new ConnectionManager(host, port, username, password, database);
        return connectionManager.open();
    }

    public void disconnect() {
        if (connectionManager != null)
            connectionManager.close();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
