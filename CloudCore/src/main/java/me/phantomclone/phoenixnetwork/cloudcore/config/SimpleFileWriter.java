/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.config;

import com.google.gson.Gson;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * <p>A interface that represents 2 Methods which are helpful to write some text as {@link String} in a {@link File}</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface SimpleFileWriter {

    /**
     * A default methode which create the {@link File} if not {@link File#exists()} and write or append the text as {@link String} to the {@link File}.
     * @param file The non-null {@link File} which will the content be wrote in.
     * @param text The non-mull text as {@link String} which will be written or appended in the {@link File}.
     * @param append If this param is {@code true} the text as {@link String} will be append else it will clear the {@link File} and only write the text in it.
     * @throws Exception Throws an Exception when it fails to create the {@link File} with {@link File#createNewFile()} or to write/append it in with a {@link BufferedWriter} and {@link FileWriter}.
     */
    default void writeToFile(@NonNull File file, @NonNull String text, boolean append) throws Exception {
        file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, append));
        if (append) {
            bufferedWriter.append(text);
        } else {
            bufferedWriter.write(text);
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * A default methode which create the {@link File} if not {@link File#exists()} and write or append the content as {@link java.util.Map} to the {@link File}.
     * @param file The non-null {@link File} which will the content be wrote in.
     * @param config The content of the non-null {@link Config} will be written or appended in the {@link File}.
     * @param append If this param is {@code true} the content of the non-null {@link Config} will be append else it will clear the {@link File} and only write the content of the {@link Config} only in it.
     * @throws Exception Throws an Exception when it fails to create the {@link File} with {@link File#createNewFile()} or to write/append it in with a {@link BufferedWriter} and {@link FileWriter}.
     */
    default void writeToFile(@NonNull File file, @NonNull Config config, boolean append) throws Exception {
        file.getParentFile().mkdirs();
        HashMap<String, Object> configMap = new HashMap<>();
        config.getKeys().forEach(key -> configMap.put(key, config.get(key)));

        Gson gson = new Gson();
        writeToFile(file, gson.toJson(configMap), append);

    }

}