/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * <p>A interface that represents a registry to cache all {@link PacketListener}</p>
 *
 * <p>It all {@link PacketListener#onReceive(CloudLib, Channel, Object)} when a packet comes in over {@link PacketListenerRegistry#callPacket(CloudLib, Channel, Object)}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface PacketListenerRegistry {

    /**
     * It register the {@link PacketListener} in a {@link java.util.List}.
     * @param packetListener The {@link PacketListener} which get registered.
     */
    void register(@NonNull PacketListener packetListener);

    /**
     * It unregister the {@link PacketListener} out of a {@link java.util.List}.
     * @param packetListener The {@link PacketListener} which get unregistered if it registered.
     */
    void unregister(@NonNull PacketListener packetListener);

    /**
     * This methode get call by a channel handler which handles incoming objects.
     *
     * It invokes {@link PacketListener#onReceive(CloudLib, Channel, Object)} from all registered {@link PacketListener}s.
     *
     * @param cloudLib The non-null {@link CloudLib} which gains access to it.
     * @param channel The non-null {@link Channel} where the packet comes it.
     * @param packet The non-null packet as {@link Object}.
     */
    void callPacket(@NonNull CloudLib cloudLib, @NonNull Channel channel, @NonNull Object packet);

}