/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet;

import com.google.gson.Gson;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PacketFactoryImpl implements PacketFactory, Utils {

    private final PacketRegistry registry;

    private PacketFactoryImpl(final PacketRegistry registry) {
        this.registry = registry;
    }

    public static PacketFactoryImpl create(final PacketRegistry registry) {
        return new PacketFactoryImpl(registry);
    }

    @Override
    public Object create(@NonNull Integer packetId, @NonNull String raw) {
        Objects.requireNonNull(packetId);
        Objects.requireNonNull(raw);
        return create(Objects.requireNonNull(this.registry.getPacketClass(packetId)), raw);
    }

    @Override
    public Object create(@NonNull Class<?> packetClass, @NonNull String raw) {
        Objects.requireNonNull(packetClass);
        Objects.requireNonNull(raw);
        if (packetClass.getAnnotation(Packet.class) == null) return null;
        Gson gson = new Gson();
        Map<String, Object> values = gson.fromJson(raw, HashMap.class);
        Object packet = this.registry.getPacket(packetClass);
        if (packet == null) return null;
        values.forEach((key, value) -> {
            Field field;
            try {
                field = packet.getClass().getDeclaredField(key);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(packet, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return packet;
    }

}