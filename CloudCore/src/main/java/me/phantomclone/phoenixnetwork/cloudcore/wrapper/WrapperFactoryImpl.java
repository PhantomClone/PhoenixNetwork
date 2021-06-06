/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.wrapper;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKey;

import java.util.Objects;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class WrapperFactoryImpl implements WrapperFactory {

    public static WrapperFactoryImpl create() {
        return new WrapperFactoryImpl();
    }

    private WrapperFactoryImpl() {}

    @Override
    public void create(@NonNull CloudLib cloudLib, @NonNull UUID uuid, @NonNull String name, @NonNull String address) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(name);
        Objects.requireNonNull(address);
        Wrapper wrapper = new Wrapper() {

            private Channel channel;

            @Override
            public UUID getUUID() {
                return uuid;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getAddress() {
                return address;
            }

            @Override
            public void setChannel(Channel channel) {
                this.channel = channel;
            }

            @Override
            public Channel getChannel() {
                return channel;
            }

            @Override
            public AuthKey getAuthKey() {
                return cloudLib.getAuthKeyRegistry().getKey(getUUID());
            }
        };
        cloudLib.getWrapperRegistry().registerWrapper(wrapper);
    }

}