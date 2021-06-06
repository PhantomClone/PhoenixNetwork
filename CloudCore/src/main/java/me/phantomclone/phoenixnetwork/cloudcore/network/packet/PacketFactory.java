/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet;

import lombok.NonNull;

/**
 * <p>A interface that represents a factory which creates packet.</p>
 *
 * <p>To creates a packet, it needs raw data and the packet it or packet class.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface PacketFactory {

    /**
     * Creates a packet as {@link Object} which the class of the packet and raw data.
     *
     * It tries to get an instance of the packet class from {@link PacketRegistry#getPacket(Class)}. If it fail, it returns null.
     *
     * @param packetClass The non-null packet class. It tries with it to get an instance of it witch {@link PacketRegistry#getPacket(Class)}.
     * @param raw The non-null raw data will be converted with {@link com.google.gson.Gson} to fill the packet up.
     * @return Returns a the filled packet with the raw data as {@link Object}. If it do get an instance of the packet class from {@link PacketRegistry#getPacket(Class)} it returns null.
     */
    Object create(@NonNull Class<?> packetClass, @NonNull String raw);

    /**
     * It get the class of the packet with {@link PacketRegistry#getPacketClass(Integer)} and return the result of {@link PacketFactory#create(Class, String)}.
     * @param packetId Try to get the class of the non-null packetId which {@link PacketRegistry#getPacketClass(Integer)}.
     * @param raw It will handover the non-null raw data to {@link PacketFactory#create(Class, String)}.
     * @return It returns the result of {@link PacketFactory#create(Class, String)}.
     */
    Object create(@NonNull Integer packetId, @NonNull String raw);

}