/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class PacketRegistryImpl implements PacketRegistry {

    private List<Class<?>> packets = new ArrayList<>();

    private PacketRegistryImpl() {
    }

    public static PacketRegistryImpl create() {
        return new PacketRegistryImpl();
    }

    @Override
    public void registerPacket(@NonNull Class<?> packetClass) {
        Objects.requireNonNull(packetClass);
        if (packetClass.getAnnotation(Packet.class) != null && !this.packets.contains(packetClass)) {
            this.packets.add(packetClass);
        }
    }

    @Override
    public void unregisterPacket(@NonNull Class<?> packetClass) {
        Objects.requireNonNull(packetClass);
        this.packets.remove(packetClass);
    }

    @Override
    public Object getPacket(@NonNull Class<?> packetClass) {
        Objects.requireNonNull(packetClass);
        Object packet = null;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    @Override
    public Class<?> getPacketClass(@NonNull Integer packetId) {
        Objects.requireNonNull(packetId);
        return this.packets.get(packetId);
    }

    @Override
    public Integer getPacketId(@NonNull Class<?> packetClass) {
        Objects.requireNonNull(packetClass);
        return this.packets.contains(packetClass) ? this.packets.indexOf(packetClass) : -1;
    }

    @Override
    public void shuffle() {
        this.packets = this.packets.stream().sorted((o1, o2) -> {
            int i1 = 0;
            for (byte aByte : o1.getSimpleName().getBytes()) {
                i1 += aByte;
            }
            int i2 = 0;
            for (byte aByte : o2.getSimpleName().getBytes()) {
                i2 += aByte;
            }
            return i2 - i1;
        }).collect(Collectors.toList());
    }
}