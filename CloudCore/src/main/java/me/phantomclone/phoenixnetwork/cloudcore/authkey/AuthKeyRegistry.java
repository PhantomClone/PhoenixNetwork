/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.authkey;

import java.util.UUID;

/**
 * <p>A interface that represents a registry with give a existing {@link AuthKey}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface AuthKeyRegistry {

    /**
     * It returns the {@link AuthKey}, which is assigned to the {@link UUID}
     * @param uuid Requires a non-null {@link UUID} which is used in {@link AuthKeyFactory#createAuthKey(UUID)} to return a existing {@link AuthKey}.
     * @return {@link AuthKey} when the {@link UUID} used in {@link AuthKeyFactory#createAuthKey(UUID)} else it will return null.
     */
    AuthKey getKey(UUID uuid);

}