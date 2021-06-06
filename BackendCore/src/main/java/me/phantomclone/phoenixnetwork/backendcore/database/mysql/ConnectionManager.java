/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ConnectionManager {

    private HikariDataSource dataSource;
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String database;
    private final int connectionTimeout;

    public ConnectionManager(String host, String port, String username, String password, String database, int connectionTimeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.connectionTimeout = connectionTimeout;
    }

    public ConnectionManager(String host, String port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.connectionTimeout = 5000;
    }

    public Connection getConnection() {
        if (isClosed())
            throw new IllegalStateException("Connection is not open.");

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean open() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", host, port, database));
            config.setConnectionTimeout(connectionTimeout);
            config.setMaximumPoolSize(10);
            this.dataSource = new HikariDataSource(config);
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public void close() {
        if (!isClosed())
            this.dataSource.close();
    }

    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }

}