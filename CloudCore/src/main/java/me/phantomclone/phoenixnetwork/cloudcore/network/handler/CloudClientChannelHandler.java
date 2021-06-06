/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.*;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudClientChannelHandler extends ChannelHandler {

    private Consumer<Channel> connect;

    public CloudClientChannelHandler(CloudLib cloudLib, Consumer<Channel> connect) {
        super(cloudLib);
        this.connect = connect;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause.getMessage().contains("Connection reset")) {
            getCloudLib().getPacketListenerRegistry().callPacket(getCloudLib(), ctx.channel(), new StopPacket("Master shutdown... (#1)"));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        getCloudLib().getPacketListenerRegistry().callPacket(getCloudLib(), ctx.channel(), new StopPacket("Master Shutdown..."));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.connect.accept(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object packet) throws Exception {
        if (packet instanceof AuthenticationSuccessPacket) {
            log("Authentication successfully!");
            ctx.channel().writeAndFlush(new LobbyAddressesPacket(new ArrayList<>()));
            return;
        }
        getCloudLib().getPacketListenerRegistry().callPacket(getCloudLib(), ctx.channel(), packet);
    }

}