/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.config;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>A interface that represents an config which can save Object with a key in a {@link Map} in this class.</p>
 *
 * <p>It save the {@link Map} in this class with {@link com.google.gson.Gson}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Config {

    /**
     * Read the file, which need to have json content in.
     * @param file Reads out the given non-null file.
     */
    void read(@NonNull File file);

    /**
     * Put a {@link Object} and the Key as {@link String} in the {@link Map} in this class.
     * @param key The non-null key which put with the {@link Object} in the {@link Map} in this class.
     * @param value The non-null value which put with the key as {@link String} in the {@link Map} in this class.
     */
    void set(@NonNull String key, @NonNull Object value);

    /**
     * Returns the Object compares to this given non-null key as {@link String} if it exists.
     * @param key The non-mull key as {@link String} is the key to get the Object out of the {@link Map} in this class.
     * @return Returns whether the {@link Object} in the {@link Map} in this class with the key as {@link String} or when it has not an entry with this key, it returns null.
     */
    Object get(@NonNull String key);

    /**
     * It removes the key as {@link String} and the value in the {@link Map} in this class if it exist.
     * @param key The non-null key as {@link String} is the key for the {@link Map} in this class.
     */
    void delete(@NonNull String key);

    /**
     * Save the content of the {@link Map} in this class with {@link com.google.gson.Gson} in the given non-null {@link File}.
     * @param file In the non-null {@link File} it will be saved the content of the {@link Map} in this class.
     * @throws IOException When it fails to save the content of the {@link Map} in this class in the given {@link File}.
     */
    void save(@NonNull File file) throws IOException;

    /**
     * It clears the {@link Map} in this class with {@link Map#clear()}
     */
    void unload();

    /**
     * It returns all keys as {@link String} of the {@link Map} in this class as {@link List}.
     * @return It returns all keys as {@link String} of the {@link Map} in this class as {@link List}.
     */
    List<String> getKeys();

}