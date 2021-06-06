/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.redis;

import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;

import java.io.File;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class JedisFactoryImpl implements JedisFactory {

    public static JedisFactoryImpl create() { return new JedisFactoryImpl(); }

    private JedisFactoryImpl() {}

    @Override
    public void createJedis(String host, int port, String password) {
        File file = new File("./redis/config.cfg");
        if (file.exists())
            file.delete();
        ConfigImpl config = ConfigImpl.create();
        config.set("host", host);
        config.set("port", "" + port);
        config.set("password", password);
        config.save(file);
    }
}
