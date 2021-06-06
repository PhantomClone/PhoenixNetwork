/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.config;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface SimpleFileWriter {

    default void writeToFile(File file, String text, boolean append) throws Exception {
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

    default void writeToFile(File file, Config config, boolean append) throws Exception {
        file.getParentFile().mkdirs();
        HashMap<String, Object> configMap = new HashMap<>();
        config.getKeys().forEach(key -> configMap.put(key, config.get(key)));

        Gson gson = new Gson();
        writeToFile(file, gson.toJson(configMap), append);

    }

}