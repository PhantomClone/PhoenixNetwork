/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.backendcore.database.redis;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface JedisFactory {

    boolean createJedis(String name, String host, int port, String password, boolean connectByStart);

}
