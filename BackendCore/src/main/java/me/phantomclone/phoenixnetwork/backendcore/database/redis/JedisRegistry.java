/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.backendcore.database.redis;

import redis.clients.jedis.JedisPool;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface JedisRegistry {

    void load();
    void unload();

    JedisPool getJedisPool(String name);

}
