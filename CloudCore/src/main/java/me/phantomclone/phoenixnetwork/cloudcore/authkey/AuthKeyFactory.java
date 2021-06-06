/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.authkey;

import lombok.NonNull;

import java.util.UUID;

/**
 * <p>A interface that represents a factory with can create a new {@link AuthKey}.</p>
 *
 * <p>It creates only a new {@link AuthKey} if it do not have already one created with a specific {@link UUID}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface AuthKeyFactory {

    /**
     * Create a new {@link AuthKey} when an {@link AuthKey} does not exist with this given {@link UUID}.
     *
     * @param uuid Requires a non-null {@link UUID} to create a new {@link AuthKey}.
     * @return new {@link AuthKey} or null if a {@link AuthKey} with this given {@link UUID} already exists.
     */
    AuthKey createAuthKey(@NonNull UUID uuid);

}