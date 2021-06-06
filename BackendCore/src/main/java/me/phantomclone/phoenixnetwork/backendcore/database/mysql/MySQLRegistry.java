/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface MySQLRegistry {

    void load();
    void unload();

    MySQL getMySQL(String name);

}
