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

import java.io.File;
import java.io.IOException;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class JedisFactoryImpl implements JedisFactory {

    public static JedisFactoryImpl create() {return new JedisFactoryImpl();}

    private JedisFactoryImpl() {}

    @Override
    public boolean createJedis(String name, String host, int port, String password, boolean connectByStart) {
        File file = new File("./plugins/Backend/database/redis", name.toLowerCase() + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
                Config config = ConfigImpl.create();
                config.set("host", host);
                config.set("port", port);
                config.set("password", password);
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