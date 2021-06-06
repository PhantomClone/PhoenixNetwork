/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.redis;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface JedisFactory {

    void createJedis(String host, int port, String password);

}
