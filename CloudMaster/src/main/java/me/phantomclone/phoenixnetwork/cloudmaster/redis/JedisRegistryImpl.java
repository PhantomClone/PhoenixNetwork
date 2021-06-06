/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.redis;

import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class JedisRegistryImpl implements JedisRegistry {

    private JedisPool jedisPool = null;

    public static JedisRegistryImpl create() { return new JedisRegistryImpl(); }

    private JedisRegistryImpl() {}

    @Override
    public JedisPool getJedisPool() {
        if (jedisPool == null) {
            File file = new File("./redis/config.cfg");
            if (!file.exists())
                return null;
            ConfigImpl config = ConfigImpl.create();
            config.read(file);
            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            String pw = config.get("password").toString();

            if (pw != null && !pw.isEmpty())
                this.jedisPool = new JedisPool(poolConfig, config.get("host").toString(), Integer.parseInt(config.get("port").toString()), 5000, pw);
            else
                this.jedisPool = new JedisPool(poolConfig, config.get("host").toString(), Integer.parseInt(config.get("port").toString()), 5000);

        }
        return this.jedisPool;
    }
}