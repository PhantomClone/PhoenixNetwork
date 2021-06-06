/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

import java.util.function.Consumer;

/**
 * <p>A interface that represents a registry which contains the {@link NettyServer}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface NettyServerRegistry {

    /**
     * Returns a {@link Runnable} which when it invokes it start the netty server with the port.
     * @param cloudLib A non-null {@link CloudLib} which will be use to invoke {@link NettyClient#startClient(CloudLib, String, int, Consumer)}.
     * @param port A {@link Integer} which will be use to invoke {@link NettyClient#startClient(CloudLib, String, int, Consumer)}.
     * @return Returns a {@link Runnable} which when it invokes it connect to the netty server with the non-null host and port.
     */
    Runnable startServer(@NonNull CloudLib cloudLib, int port);

    /**
     * Send the packet as {@link Object} to netty server.
     *
     * If the client is not connected jet, it will cache the packets and send them if the client is connected.
     *
     * @param packet The packet what will be send.
     * @param wrapperChannel The Chann
     */
    void sendPacket(@NonNull Object packet, @NonNull Channel wrapperChannel);

    /**
     * Returns the {@link NettyServer} which will be create when an instance of this class will be created.
     * @return Returns the {@link NettyServer} which will be create when an instance of this class will be created.
     */
    NettyServer getNettyServer();

    /**
     * Stops the NettyServer if it is connected.
     */
    void stop();

    /**
     * It remove a connected channel.
     * @param channel A non-null channel which will be used to remove it out of this class.
     */
    void remove(@NonNull Channel channel);
}