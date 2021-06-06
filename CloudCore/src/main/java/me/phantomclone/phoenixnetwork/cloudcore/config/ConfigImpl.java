/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ConfigImpl implements Config, SimpleFileWriter{

    private HashMap<String, Object> configData = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigImpl create() {
        return new ConfigImpl();
    }

    private ConfigImpl() {}

    @Override
    public void read(@NonNull File file) {
        Objects.requireNonNull(file);
        try {
            String content = FileUtils.readFileToString(file, Charset.defaultCharset());
            this.configData = this.gson.fromJson(content, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        this.configData.put(key, value);
    }

    @Override
    public Object get(@NonNull String key) {
        Objects.requireNonNull(key);
        return this.configData.get(key);
    }


    @Override
    public void delete(@NonNull String key) {
        Objects.requireNonNull(key);
        this.configData.remove(key);
    }

    @Override
    public void save(@NonNull File file) {
        Objects.requireNonNull(file);
        String content = this.gson.toJson(this.configData);
        try {
            writeToFile(file, content, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {
        this.configData.clear();
    }

    @Override
    public List<String> getKeys() {
        return new ArrayList<>(this.configData.keySet());
    }


}