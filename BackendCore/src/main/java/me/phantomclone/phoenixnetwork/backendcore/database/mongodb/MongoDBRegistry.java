/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mongodb;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface MongoDBRegistry {

    void load();
    void unload();

    Client getMongoClient(String name);

}
