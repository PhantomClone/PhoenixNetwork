/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database;

import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBRegistry;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLRegistry;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisRegistry;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface DatabaseLib {

    MySQLFactory getMySQLFactory();
    MySQLRegistry getMySQLRegistry();

    JedisFactory getJedisFactory();
    JedisRegistry getJedisRegistry();

    MongoDBFactory getMongoDBFactory();
    MongoDBRegistry getMongoDBRegistry();

}
