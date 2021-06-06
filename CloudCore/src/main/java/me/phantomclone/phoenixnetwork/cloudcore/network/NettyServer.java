/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudDecoder;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudEncoder;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudServerChannelHandler;

/**
 * <p>A interface that represents a netty server which clients be connected to and send packets to them.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface NettyServer {

    boolean EPOLL = Epoll.isAvailable();

    /**
     * Send the packet as {@link Object} to netty server.
     *
     * If the wrapperChannel is not connected it do not send the packet.
     *
     * @param packet The non-null packet what will be send.
     * @param wrapperChannel The non-null {@link Channel} which will get send the packet.
     */
    void sendPacket(Object packet, Channel wrapperChannel);

    /**
     * A default methode will start a netty server depend on {@link Epoll#isAvailable()}.
     *
     * When it initChannel it register all necessary handlers in the pipeline.
     *
     * If it ends, it shutdownGracefully. when an try catch finally .
     *
     * @param cloudLib The non-null {@link CloudLib} will be handover for the handler in the pipeline if needed.
     * @param port The client will be connected to the non-null port.
     */
    default void startServer(CloudLib cloudLib, int port) {
        EventLoopGroup boss = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        EventLoopGroup worker = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).
                    channel(EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("splitter", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast(new CloudDecoder(cloudLib))
                                    .addLast("prepender", new LengthFieldPrepender(4))
                                    .addLast(new CloudEncoder(cloudLib))
                                    .addLast(new CloudServerChannelHandler(cloudLib));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

}