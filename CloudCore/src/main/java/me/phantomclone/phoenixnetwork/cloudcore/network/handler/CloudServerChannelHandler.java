/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKey;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.AuthenticationPacket;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.AuthenticationSuccessPacket;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.StopPacket;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudServerChannelHandler extends ChannelHandler implements Utils {

    private final List<Channel> check = new LinkedList<>();

    public CloudServerChannelHandler(CloudLib cloudLib) {
        super(cloudLib);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.check.add(ctx.channel());
        getCloudLib().getThreadPoolRegistry().submit(() ->{
           try {
               Thread.sleep(5000);
               if (this.check.contains(ctx.channel()))
                   ctx.close();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception { }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        getCloudLib().getNettyServerRegistry().remove(ctx.channel());
        Wrapper wrapper = getCloudLib().getWrapperRegistry().getWrapper(ctx.channel());
        getCloudLib().getWrapperRegistry().remove(ctx.channel());
        getCloudLib().getServerRegistry().getServers(wrapper).forEach(server -> getCloudLib().getServerRegistry().unregisterServer(server));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object packet) throws Exception {
        if (this.check.contains(ctx.channel())) {
            if (packet instanceof AuthenticationPacket) {
                AuthenticationPacket authPacket = (AuthenticationPacket) packet;
                if (authPacket.getAuthKey().equalsIgnoreCase("need")) {
                    getCloudLib().getAuthKeyFactory().createAuthKey(UUID.fromString(authPacket.getUuid()));
                    ctx.channel().writeAndFlush(new StopPacket("Copy your AuthKey in the Folder './authkeys/' Id: " + authPacket.getUuid()));
                    this.check.remove(ctx.channel());
                    ctx.close();
                    return;
                }
                AuthKey masterKey = getCloudLib().getAuthKeyRegistry().getKey(UUID.fromString(authPacket.getUuid()));
                if (masterKey == null || !masterKey.getKeyString().equals(authPacket.getAuthKey())) {
                    this.check.remove(ctx.channel());
                    ctx.close();
                    return;
                }
                this.check.remove(ctx.channel());
                getCloudLib().getWrapperFactory().create(getCloudLib(), UUID.fromString(authPacket.getUuid()), authPacket.getName(), ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0]);
                try {
                    Wrapper wrapper = getCloudLib().getWrapperRegistry().getWrapper(authPacket.getName());
                    wrapper.setChannel(ctx.channel());
                    getCloudLib().getNettyServerRegistry().sendPacket(new AuthenticationSuccessPacket(), wrapper.getChannel());
                } catch (Exception e) { e.printStackTrace(); }
            }
            return;
        }
        getCloudLib().getPacketListenerRegistry().callPacket(getCloudLib(), ctx.channel(), packet);
    }
}