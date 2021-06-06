/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.thread;

import lombok.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * <p>A interface that represents registry for a {@link ExecutorService}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ThreadPoolRegistry {

    /**
     * Submit a non-null runnable which will be invoked.
     * @param runnable The non-null runnable which will be invoked.
     * @return Returns the {@link Future} which give the {@link ExecutorService} back.
     */
    Future<?> submit(@NonNull Runnable runnable);

    /**
     * Shutdown the {@link ExecutorService}.
     */
    void shutdownPool();

}