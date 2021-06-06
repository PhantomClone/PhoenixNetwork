/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.wrapper;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

import java.util.UUID;

/**
 * <p>A interface that represents help for {@link ByteBuf}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface WrapperFactory {

    /**
     * It create the wrapper and registered him afterwards.
     * It will ask in {@link me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKeyRegistry} for the {@link me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKey} for this {@link UUID}.
     *
     * @param cloudLib The non-null {@link CloudLib} which allows access.
     * @param uuid The non-null {@link UUID} which identify the wrapper.
     * @param name The non-null name which give the wrapper a name.
     * @param address The non-null address to hand over it the wrapper.
     */
    void create(@NonNull CloudLib cloudLib, @NonNull UUID uuid, @NonNull String name, @NonNull String address);

}