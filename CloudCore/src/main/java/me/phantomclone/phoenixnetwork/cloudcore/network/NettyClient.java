/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudClientChannelHandler;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudDecoder;
import me.phantomclone.phoenixnetwork.cloudcore.network.handler.CloudEncoder;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>A interface that represents a netty connections client which can connect to the server and send packets.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface NettyClient {

    boolean EPOLL = Epoll.isAvailable();

    /**
     * Send the packet as {@link Object} to netty server.
     *
     * If the client is not connected jet, it will cache the packets and send them if the client is connected.
     *
     * @param packet The non-null packet what will be send.
     */
    void sendPacket(@NonNull Object packet);

    /**
     * A default methode will start a netty client depend on {@link Epoll#isAvailable()}.
     *
     * When it initChannel it register all necessary handlers in the pipeline.
     *
     * If it ends, it shutdownGracefully. when an try catch finally .
     *
     * @param cloudLib The non-null {@link CloudLib} will be handover for the handler in the pipeline if needed.
     * @param host The client will be connected to the non-null host.
     * @param port The client will be connected to the non-null port.
     * @param connect The non-null {@link Consumer} will be called if the client is connected to the server.
     */
    default void startClient(@NonNull CloudLib cloudLib, @NonNull String host, @NonNull int port, @NonNull Consumer<Channel> connect) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(host);
        Objects.requireNonNull(port);
        Objects.requireNonNull(connect);
        EventLoopGroup workerGroup = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(EPOLL ? EpollSocketChannel.class : NioSocketChannel.class).
                    option(ChannelOption.SO_KEEPALIVE, true).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("splitter", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast(new CloudDecoder(cloudLib))
                                    .addLast("prepender", new LengthFieldPrepender(4))
                                    .addLast(new CloudEncoder(cloudLib))
                                    .addLast(new CloudClientChannelHandler(cloudLib, connect));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
