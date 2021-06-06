/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.redis;

import redis.clients.jedis.JedisPool;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface JedisRegistry {

    JedisPool getJedisPool();

}
