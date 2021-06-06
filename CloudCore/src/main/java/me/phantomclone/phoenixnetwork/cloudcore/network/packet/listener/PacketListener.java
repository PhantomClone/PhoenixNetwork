/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * <p>A interface that represents a listener which get all incoming packets.</p>
 *
 * <p>It have be registered in {@link PacketListenerRegistry#register(PacketListener)} to work.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface PacketListener {

    /**
     * A methode which will be call from {@link PacketListenerRegistry} when a packet comes in.
     * @param cloudLib The non-null {@link CloudLib} which gains access to it.
     * @param channel The non-null {@link Channel} where the packet comes it.
     * @param packet The non-null packet as {@link Object}. It have to be casted to the actually packet.
     */
    void onReceive(@NonNull CloudLib cloudLib, @NonNull Channel channel, @NonNull Object packet);

}