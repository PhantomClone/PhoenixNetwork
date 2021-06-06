/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.wrapper;

import io.netty.channel.Channel;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * <p>A interface that represents a registry for {@link Wrapper}s.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface WrapperRegistry {

    /**
     * Registered the non-null {@link Wrapper} if the {@link Wrapper} is not registered yet.
     * It will also save the data of this wrapper as file.
     * @param wrapper The non-null {@link Wrapper} which will be registered.
     */
    void registerWrapper(@NonNull Wrapper wrapper);

    /**
     * Return a specific {@link Wrapper} with the same name if it is registered else it returns null.
     * @param name Search throw all registered {@link Wrapper} to find the {@link Wrapper} with non-null name.
     * @return Returns a specific {@link Wrapper} with the same name if it is registered else it returns null.
     */
    Wrapper getWrapper(@NonNull String name);

    /**
     * Return a specific {@link Wrapper} with the same name if it is registered else it returns null.
     * @param uuid Search throw all registered {@link Wrapper} to find the {@link Wrapper} with non-null {@link UUID}.
     * @return Returns a specific {@link Wrapper} with the same name if it is registered else it returns null.
     */
    Wrapper getWrapper(@NonNull UUID uuid);

    /**
     * Return a specific {@link Wrapper} with the same name if it is registered else it returns null.
     * @param channel Search throw all registered {@link Wrapper} to find the {@link Wrapper} with non-null {@link Channel}.
     * @return Returns a specific {@link Wrapper} with the same name if it is registered else it returns null.
     */
    Wrapper getWrapper(@NonNull Channel channel);

    /**
     * It search for a file with the wrapper name and read it out and returns the address when it is found, else it returns null.
     * @param wrapperName The non-null name which will be needed to search for the wrapper data file.
     * @return It search for a file with the wrapper name and read it out and returns the address when it is found, else it returns null.
     */
    String getHostFromWrapperName(@NonNull String wrapperName);

    /**
     * Returns all online {@link Wrapper}s.
     * @return Returns all online {@link Wrapper}s in a {@link List}.
     */
    List<Wrapper> getOnlineWrappers();

    /**
     * Unregister the {@link Wrapper} with the same {@link Channel}.
     * @param channel Used to get the {@link Wrapper} and removes it when found.
     */
    void remove(@NonNull Channel channel);
}