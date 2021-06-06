/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class NettyServerRegistryImpl implements NettyServerRegistry, Utils {

    private final List<Channel> channels;
    private final NettyServer nettyServer;

    public static NettyServerRegistryImpl create() {
        return new NettyServerRegistryImpl();
    }

    public NettyServerRegistryImpl() {
        this.channels = new ArrayList<>();
        this.nettyServer = (packet, wrapperChannel) -> {
            Objects.requireNonNull(packet);
            Objects.requireNonNull(wrapperChannel);
            if (!wrapperChannel.isActive()) { log("Tried to send Packet (" + packet.getClass() + ") to an not registered Channel"); return;}
            wrapperChannel.writeAndFlush(packet);
        };
    }

    @Override
    public Runnable startServer(@NonNull CloudLib cloudLib, int port) {
        Objects.requireNonNull(cloudLib);
        return () -> this.nettyServer.startServer(cloudLib, port);
    }

    @Override
    public void stop() {
        this.channels.stream().filter(Channel::isOpen).forEach(Channel::close);
    }

    @Override
    public void sendPacket(@NonNull Object packet, @NonNull Channel wrapper) {
        Objects.requireNonNull(packet);
        Objects.requireNonNull(wrapper);
        this.nettyServer.sendPacket(packet, wrapper);
    }

    @Override
    public NettyServer getNettyServer() {
        return this.nettyServer;
    }


    @Override
    public void remove(@NonNull Channel channel) {
        Objects.requireNonNull(channel);
        if (!this.channels.contains(channel)) return;
        this.channels.remove(channel);
    }
}