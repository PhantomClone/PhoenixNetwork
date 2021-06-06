/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.storage;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface StorageRegistry<T> {

    void init();
    void stop();

    void registerStorable(Class<?> clazz, BiConsumer<T, Map<String, Object>> defaultData);

    void unregisterStorable(Class<?> clazz);

    <B> B getStoreObject(UUID uuid, Class<B> clazz);

    void store(UUID uuid);

}
