/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.master;

/**
 * <p>A interface that represents a storage for the {@link Master}.</p>
 *
 * <p>At first time it runs {@link MasterRegistry#getMaster()}, it will read out the data out of config.json.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface MasterRegistry {

    /**
     * When it get call the first time, it will try to read out the master data form config.yml.
     * They could be happened, that the master data is not set yet, this will throw a {@link NullPointerException}!
     *
     * After reading it out the first time, it will be cached in this class.
     * @return Returns the {@link Master} which filled data. It returns null when the config.yml do not exist!
     * @throws NullPointerException If the data is not set yet, it throws an {@link NullPointerException}!
     */
    Master getMaster() throws NullPointerException;

}