/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mongodb;

import me.phantomclone.phoenixnetwork.backendcore.config.Config;
import me.phantomclone.phoenixnetwork.backendcore.config.ConfigImpl;

import java.io.File;
import java.io.IOException;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MongoDBFactoryImpl implements MongoDBFactory {

    public static MongoDBFactoryImpl create() {return new MongoDBFactoryImpl();}

    private MongoDBFactoryImpl() {}

    @Override
    public boolean createMongoDB(String name, String host, String port, String username, String password, String database, boolean connectByStart) {
        File file = new File("./plugins/Backend/database/mongodb", name.toLowerCase() + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
                Config config = ConfigImpl.create();
                config.set("host", host);
                config.set("port", port);
                config.set("username", username);
                config.set("password", password);
                config.set("database", database);
                config.set("connectByStart", connectByStart);
                config.save(file);
                config.unload();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
}
