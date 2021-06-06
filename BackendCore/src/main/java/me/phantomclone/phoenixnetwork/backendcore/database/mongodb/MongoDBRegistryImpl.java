/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import me.phantomclone.phoenixnetwork.backendcore.config.Config;
import me.phantomclone.phoenixnetwork.backendcore.config.ConfigImpl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MongoDBRegistryImpl implements MongoDBRegistry {

    private final Map<String, Client> clients = new HashMap<>();

    public static MongoDBRegistryImpl create() {return new MongoDBRegistryImpl();}

    private MongoDBRegistryImpl() {}

    @Override
    public void load() {
        File folder = new File("./plugins/Backend/database/mongodb/");
        folder.mkdirs();
        if (folder.list() != null) {
            Config config = ConfigImpl.create();
            Arrays.stream(folder.listFiles()).filter(file -> file.getName().endsWith(".json")).forEach(file -> {
                config.read(file);
                if (config.get("connectByStart") != null && Boolean.valueOf(config.get("connectByStart").toString())) {
                    this.clients.put(file.getName().replace(".json", "").toLowerCase(), new Client(MongoClients.create(new ConnectionString("mongodb://" + config.get("host") + ":" + config.get("port") + "/" + config.get("database") + "?authSource=" + config.get("password") + " --username " + config.get("username"))), config.get("database").toString()));
                }
            });
        }
    }

    @Override
    public void unload() {
        this.clients.values().forEach(client -> client.getMongoClient().close());
        this.clients.clear();
    }

    @Override
    public Client getMongoClient(String name) {
        if (this.clients.containsKey(name.toLowerCase())) {
            return this.clients.get(name.toLowerCase());
        }
        File file = new File("./plugins/Backend/database/mongodb", name.toLowerCase() + ".json");
        if (!file.exists())
            return null;
        Config config = ConfigImpl.create();
        config.read(file);
        MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://" + config.get("host") + ":" + config.get("port") + "/" + config.get("database") + "?authSource=" + config.get("password") + " --username " + config.get("username")));
        Client client = new Client(mongoClient, config.get("database").toString());
        this.clients.put(file.getName().replace(".json", "").toLowerCase(), client);
        return client;
    }
}
