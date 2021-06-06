/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.authkey;

import java.io.IOException;
import java.util.UUID;

/**
 * <p>A interface that represents an immutable universally unique {@link AuthKey} which bound to a UUID.</p>
 *
 * <p>This interface have to get the UUID and to get the Key as String to compare it with other {@link AuthKey} if it the same.</p>
 *
 * <p>You get an instance of this class in {@link AuthKeyRegistry#getKey(UUID)} or {@link AuthKeyFactory#createAuthKey(UUID)}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface AuthKey {

    /**
     * It returns the UUID of the {@link AuthKey}.
     * @return the UUID of the {@link AuthKey}.
     */
    UUID getUUID();
    /**
     * It returns a specific unique {@link String} which belongs to this {@link AuthKey}.
     * @throws IOException Throws an IOException when it fails to read the unique {@link String}.
     * @return Returns a specific unique String which belongs to this getUUID methode.
     */
    String getKeyString() throws IOException;

}