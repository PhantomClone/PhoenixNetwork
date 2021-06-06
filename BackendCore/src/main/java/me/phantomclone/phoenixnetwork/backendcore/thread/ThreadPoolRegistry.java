/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.thread;

import java.util.concurrent.Future;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ThreadPoolRegistry {

    Future<?> submit(Runnable runnable);

    void shutdownPool();

}
