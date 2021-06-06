/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet;

import lombok.NonNull;

/**
 * <p>A interface that represents a registry for packets.</p>
 *
 * <p>All packets have to be registered.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface PacketRegistry {

    /**
     * Register the packet class in a {@link java.util.List}.
     * @param packetClass Add the non-null packetClass in {@link java.util.List}.
     */
    void registerPacket(@NonNull Class<?> packetClass);

    /**
     * Unregister the packet class in a {@link java.util.List}.
     * @param packetClass Remove the non-null packetClass out of {@link java.util.List}.
     */
    void unregisterPacket(@NonNull Class<?> packetClass);

    /**
     * Tries to create a new instance of this class.
     * @param clazz Tries to create a new instance if this class.
     * @return It returns the new instance of this class or null when it fails.
     */
    Object getPacket(@NonNull Class<?> clazz);

    /**
     * Returns a class which is registered.
     *
     * It return the class with {@link java.util.List#get(int)}
     *
     * @param packetId The non-null packet id which is used in {@link java.util.List#get(int)} to get a class.
     * @return Returns the class out of {@link java.util.List} which packetId as index.
     */
    Class<?> getPacketClass(@NonNull Integer packetId);

    /**
     * Returns the place as index of the packetClass in the {@link java.util.List} if it registered, else it will return negative one.
     * @param packetClass The non-null packet class which will be use to get the index place on the packet class in the {@link java.util.List}.
     * @return It returns the index of the packetClass in the {@link java.util.List}. If it is not found it return negative one.
     */
    Integer getPacketId(@NonNull Class<?> packetClass);

    /**
     * It sort the packets in {@link java.util.List}, that are all list in different programs are the same.
     */
    void shuffle();
}