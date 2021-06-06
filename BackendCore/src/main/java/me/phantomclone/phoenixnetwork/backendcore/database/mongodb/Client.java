/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mongodb;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class Client {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    public Client(MongoClient mongoClient, String database) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = this.mongoClient.getDatabase(database);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
