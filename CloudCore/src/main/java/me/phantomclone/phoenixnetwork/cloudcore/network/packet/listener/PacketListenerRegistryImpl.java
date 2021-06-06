/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

import java.util.ArrayList;
import java.util.List;

public class PacketListenerRegistryImpl implements PacketListenerRegistry {

    public static PacketListenerRegistryImpl create(){
        return new PacketListenerRegistryImpl();
    }

    private PacketListenerRegistryImpl(){}

    private final List<PacketListener> packetListeners = new ArrayList<>();

    @Override
    public void register(@NonNull PacketListener packetListener) {
        if (this.packetListeners.contains(packetListener)) return;
        this.packetListeners.add(packetListener);
    }

    @Override
    public void unregister(@NonNull PacketListener packetListener) {
        if (!this.packetListeners.contains(packetListener)) return;
        this.packetListeners.remove(packetListener);
    }

    @Override
    public void callPacket(@NonNull CloudLib cloudLib, @NonNull Channel channel, @NonNull Object packet) {
        this.packetListeners.forEach(l -> l.onReceive(cloudLib, channel, packet));
    }

}