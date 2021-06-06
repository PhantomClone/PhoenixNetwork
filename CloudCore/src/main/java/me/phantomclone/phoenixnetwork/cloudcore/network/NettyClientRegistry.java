/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

import java.util.function.Consumer;

/**
 * <p>A interface that represents a registry which contains the {@link NettyClient}.</p>
 *
 * <p>It can return a {@link Runnable} which contains the connectToServer lambda.</p>
 *
 * <p>Also it can stop the connection.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface NettyClientRegistry {

    /**
     * Returns a {@link Runnable} which when it invokes it connect to the netty server with the non-null host and port.
     * @param cloudLib A non-null {@link CloudLib} which will be use to invoke {@link NettyClient#startClient(CloudLib, String, int, Consumer)}.
     * @param host A non-null {@link String} which will be use to invoke {@link NettyClient#startClient(CloudLib, String, int, Consumer)}.
     * @param port A {@link Integer} which will be use to invoke {@link NettyClient#startClient(CloudLib, String, int, Consumer)}.
     * @return Returns a {@link Runnable} which when it invokes it connect to the netty server with the non-null host and port.
     */
    Runnable connectToServer(@NonNull CloudLib cloudLib, @NonNull String host, int port);

    /**
     * Returns the {@link NettyClient} which will be create when an instance of this class will be created.
     * @return Returns the {@link NettyClient} which will be create when an instance of this class will be created.
     */
    NettyClient getNettyClient();

    /**
     * Stops the NettyClient if it is connected.
     */
    void stop();

}