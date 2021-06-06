/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database;

import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBFactoryImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBRegistry;
import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.MongoDBRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLFactoryImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLRegistry;
import me.phantomclone.phoenixnetwork.backendcore.database.mysql.MySQLRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisFactory;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisFactoryImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisRegistry;
import me.phantomclone.phoenixnetwork.backendcore.database.redis.JedisRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.thread.ThreadPoolRegistry;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DatabaseLibImpl implements DatabaseLib {

    private final JedisFactory jedisFactory;
    private final JedisRegistry jedisRegistry;

    private final MongoDBFactory mongoDBFactory;
    private final MongoDBRegistry mongoDBRegistry;

    private final MySQLFactory mySQLFactory;
    private final MySQLRegistry mySQLRegistry;

    public static DatabaseLibImpl create(ThreadPoolRegistry threadPoolRegistry) {
        return new DatabaseLibImpl(threadPoolRegistry);
    }

    private DatabaseLibImpl(ThreadPoolRegistry threadPoolRegistry) {
        jedisFactory = JedisFactoryImpl.create();
        jedisRegistry = JedisRegistryImpl.create();

        mongoDBFactory = MongoDBFactoryImpl.create();
        mongoDBRegistry = MongoDBRegistryImpl.create();

        mySQLFactory = MySQLFactoryImpl.create();
        mySQLRegistry = MySQLRegistryImpl.create(threadPoolRegistry);
    }

    @Override
    public MySQLFactory getMySQLFactory() {
        return this.mySQLFactory;
    }

    @Override
    public MySQLRegistry getMySQLRegistry() {
        return this.mySQLRegistry;
    }

    @Override
    public JedisFactory getJedisFactory() {
        return this.jedisFactory;
    }

    @Override
    public JedisRegistry getJedisRegistry() {
        return this.jedisRegistry;
    }

    @Override
    public MongoDBFactory getMongoDBFactory() {
        return this.mongoDBFactory;
    }

    @Override
    public MongoDBRegistry getMongoDBRegistry() {
        return this.mongoDBRegistry;
    }
}
