/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Config {

    void read(File file);

    void set(String key, Object value);

    Object get(String key);

    void delete(String key);

    void save(File file) throws IOException;

    void unload();

    List<String> getKeys();

}