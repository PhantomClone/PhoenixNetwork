/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.wrapper;

import io.netty.channel.Channel;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKey;

import java.util.UUID;

/**
 * <p>A interface that represents a storage some data.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Wrapper {

    /**
     * The {@link UUID} which belong to a specific wrapper.
     * @return the specific {@link UUID} of this server.
     */
    UUID getUUID();

    /**
     * The name which belong to a specific wrapper.
     * @return the specific name of this server.
     */
    String getName();

    /**
     * The {@link AuthKey} which belong to a specific wrapper.
     * @return the specific {@link AuthKey} of this server.
     */
    AuthKey getAuthKey();

    /**
     * The address which belong to a specific wrapper.
     * For example localhost.
     * @return the specific address of this server.
     */
    String getAddress();

    /**
     * It set the {@link Channel} which belongs to this wrapper connection.
     * @param channel The {@link Channel} which belongs to this wrapper.
     */
    void setChannel(Channel channel);

    /**
     * It returns the {@link Channel} of this wrapper. It could be null, if the wrapper is not connected!
     * @return It returns the {@link Channel} of this wrapper. It could be null, if the wrapper is not connected!
     */
    Channel getChannel();

}