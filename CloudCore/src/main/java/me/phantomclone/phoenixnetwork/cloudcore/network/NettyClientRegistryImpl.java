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
public class NettyClientRegistryImpl implements NettyClientRegistry, Utils {

    private final NettyClient nettyClient;
    private Channel channel;

    private final List<Object> packetsToSend = new ArrayList<>();

    public static NettyClientRegistryImpl create() {
        return new NettyClientRegistryImpl();
    }

    private NettyClientRegistryImpl() {
        this.nettyClient = packet -> {
            Objects.requireNonNull(packet);
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.writeAndFlush(packet);
            } else {
                this.packetsToSend.add(packet);
            }
        };
    }

    @Override
    public Runnable connectToServer(@NonNull CloudLib cloudLib, @NonNull String host, int port) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(host);
        return () -> this.nettyClient.startClient(cloudLib, host, port, channel -> {
            this.channel = channel;
            this.packetsToSend.forEach(nettyClient::sendPacket);
            this.packetsToSend.clear();
        });
    }

    @Override
    public void stop() {
        if (this.channel != null && this.channel.isOpen()) {
            this.channel.close();
        }
    }

    @Override
    public NettyClient getNettyClient() {
        return nettyClient;
    }
}