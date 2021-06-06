/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.wrapper;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class WrapperRegistryImpl implements WrapperRegistry, Utils {

    private List<Wrapper> onlineWrappers = new ArrayList<>();

    public static WrapperRegistryImpl create() {
        return new WrapperRegistryImpl();
    }

    private WrapperRegistryImpl() {}

    @Override
    public void registerWrapper(@NonNull Wrapper wrapper) {
        Objects.requireNonNull(wrapper);
        if (getWrapper(wrapper.getUUID()) == null) {
            File file = new File("wrappers",wrapper.getName() + ".json");
            file.getParentFile().mkdirs();
            ConfigImpl config = ConfigImpl.create();
            config.set("uuid", wrapper.getUUID().toString());
            config.set("address", wrapper.getAddress());
            config.save(file);
        }
        this.onlineWrappers.add(wrapper);
        log("Registered Wrapper " + wrapper.getName() + " [Online:" + this.onlineWrappers.size() + "]");
    }

    @Override
    public Wrapper getWrapper(@NonNull String name) {
        Objects.requireNonNull(name);
        return this.onlineWrappers.stream().filter(wrapper -> wrapper.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public Wrapper getWrapper(@NonNull UUID uuid) {
        Objects.requireNonNull(uuid);
        return this.onlineWrappers.stream().filter(wrapper -> wrapper.getUUID().toString().equalsIgnoreCase(uuid.toString())).findFirst().orElse(null);
    }

    @Override
    public Wrapper getWrapper(@NonNull Channel channel) {
        Objects.requireNonNull(channel);
        return this.onlineWrappers.stream().filter(wrapper -> wrapper.getChannel().equals(channel)).findFirst().orElse(null);
    }

    @Override
    public String getHostFromWrapperName(@NonNull String wrapperName) {
        Objects.requireNonNull(wrapperName);
        ConfigImpl config = ConfigImpl.create();
        File file = new File("wrappers", wrapperName + ".json");
        if (!file.exists()) return null;
        config.read(file);
        return config.get("address").toString();
    }

    @Override
    public List<Wrapper> getOnlineWrappers() {
        return new ArrayList<>(this.onlineWrappers);
    }

    @Override
    public void remove(@NonNull Channel channel) {
        Objects.requireNonNull(channel);
        Wrapper wrapper = getWrapper(channel);
        this.onlineWrappers.remove(wrapper);
        log("Wrapper " + wrapper.getName() + " disconnected [Online:" + this.onlineWrappers.size() + "]");
    }
}
