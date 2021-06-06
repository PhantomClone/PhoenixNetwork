/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql;

import me.phantomclone.phoenixnetwork.backendcore.config.Config;
import me.phantomclone.phoenixnetwork.backendcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.backendcore.thread.ThreadPoolRegistry;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MySQLRegistryImpl implements MySQLRegistry {

    private final Map<String, MySQL> mySQLMap = new HashMap<>();
    private final ThreadPoolRegistry threadPoolRegistry;

    public static MySQLRegistryImpl create(ThreadPoolRegistry threadPoolRegistry) {return new MySQLRegistryImpl(threadPoolRegistry);}

    private MySQLRegistryImpl(ThreadPoolRegistry threadPoolRegistry) {
        this.threadPoolRegistry = threadPoolRegistry;
    }

    @Override
    public void load() {
        File folder = new File("./plugins/Backend/database/mysql/");
        folder.mkdirs();
        if (folder.list() != null) {
            Config config = ConfigImpl.create();
            Arrays.stream(folder.listFiles()).filter(file -> file.getName().endsWith(".json")).forEach(file -> {
                config.read(file);
                if (config.get("connectByStart") != null && Boolean.valueOf(config.get("connectByStart").toString())) {
                    MySQL mySQL = new MySQL(this.threadPoolRegistry);
                    mySQL.connect(config.get("host").toString(), config.get("port").toString(), config.get("username").toString(), config.get("password").toString(), config.get("database").toString());
                    this.mySQLMap.put(file.getName().replace(".json", "").toLowerCase(), mySQL);
                }
            });
        }
    }

    @Override
    public void unload() {
        this.mySQLMap.values().forEach(MySQL::disconnect);
        this.mySQLMap.clear();
    }

    @Override
    public MySQL getMySQL(String name) {
        if (this.mySQLMap.containsKey(name.toLowerCase())) {
            return this.mySQLMap.get(name.toLowerCase());
        }
        File file = new File("./plugins/Backend/database/mysql", name.toLowerCase() + ".json");
        if (!file.exists())
            return null;
        Config config = ConfigImpl.create();
        config.read(file);
        MySQL mySQL = new MySQL(this.threadPoolRegistry);
        mySQL.connect(config.get("host").toString(), config.get("port").toString(), config.get("username").toString(), config.get("password").toString(), config.get("database").toString());
        this.mySQLMap.put(file.getName().replace(".json", "").toLowerCase(), mySQL);
        return mySQL;
    }
}
