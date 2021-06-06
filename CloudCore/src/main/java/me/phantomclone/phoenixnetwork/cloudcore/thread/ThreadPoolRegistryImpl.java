/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.thread;

import lombok.NonNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ThreadPoolRegistryImpl implements ThreadPoolRegistry {

    private final ExecutorService service = Executors.newCachedThreadPool();

    public static ThreadPoolRegistryImpl create() {
        return new ThreadPoolRegistryImpl();
    }

    private ThreadPoolRegistryImpl() {}

    @Override
    public Future<?> submit(@NonNull Runnable runnable) {
        Objects.requireNonNull(runnable);
        return this.service.submit(runnable);
    }

    @Override
    public void shutdownPool() {
        this.service.shutdown();
    }

}