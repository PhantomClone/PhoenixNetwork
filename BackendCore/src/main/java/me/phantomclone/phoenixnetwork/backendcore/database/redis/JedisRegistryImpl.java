/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.backendcore.database.redis;

import me.phantomclone.phoenixnetwork.backendcore.config.Config;
import me.phantomclone.phoenixnetwork.backendcore.config.ConfigImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class JedisRegistryImpl implements JedisRegistry {

    private final HashMap<String, JedisPool> jedis = new HashMap<>();

    public static JedisRegistryImpl create() {return new JedisRegistryImpl();}

    private JedisRegistryImpl() {}

    @Override
    public void load() {
        File folder = new File("./plugins/Backend/database/redis/");
        folder.mkdirs();
        if (folder.list() != null) {
            Config config = ConfigImpl.create();
            Arrays.stream(folder.listFiles()).filter(file -> file.getName().endsWith(".json")).forEach(file -> {
                config.read(file);
                if (config.get("connectByStart") != null && Boolean.valueOf(config.get("connectByStart").toString())) {
                    String pw = config.get("password").toString();
                    if (pw != null && !pw.isEmpty()) {
                        this.jedis.put(file.getName().replace(".json", "").toLowerCase(), new JedisPool(new JedisPoolConfig(), config.get("host").toString(), (int) (double) config.get("port"), 5000, pw));
                    } else {
                        this.jedis.put(file.getName().replace(".json", "").toLowerCase(), new JedisPool(new JedisPoolConfig(), config.get("host").toString(), (int) (double) config.get("port"), 5000));
                    }
                }
            });
        }
    }

    @Override
    public void unload() {
        this.jedis.values().forEach(jedisPool -> {
            Jedis jedis = jedisPool.getResource();
            if (jedis.isConnected()) jedis.quit();
        });
        this.jedis.clear();
    }

    @Override
    public JedisPool getJedisPool(String name) {
        if (this.jedis.containsKey(name.toLowerCase())) {
            return this.jedis.get(name.toLowerCase());
        }
        File file = new File("./plugins/Backend/database/redis", name.toLowerCase() + ".json");
        if (!file.exists())
            return null;
        Config config = ConfigImpl.create();

        String pw = config.get("password").toString();
        if (pw != null && !pw.isEmpty()) {
            this.jedis.put(name.toLowerCase(), new JedisPool(new JedisPoolConfig(), config.get("host").toString(), (int) (double) config.get("port"), 5000, pw));
        } else {
            this.jedis.put(name.toLowerCase(), new JedisPool(new JedisPoolConfig(), config.get("host").toString(), (int) (double) config.get("port"), 5000));
        }
        return this.jedis.get(name.toLowerCase());
    }
}