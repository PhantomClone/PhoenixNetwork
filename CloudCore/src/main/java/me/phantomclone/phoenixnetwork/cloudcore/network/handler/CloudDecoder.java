/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.utils.ByteBufUtils;

import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudDecoder extends Decoder implements ByteBufUtils {

    public CloudDecoder(CloudLib cloudLib) {
        super(cloudLib);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
            int packetId = byteBuf.readInt();
            Class<?> packetClass = getCloudLib().getPacketRegistry().getPacketClass(packetId);
            if (packetClass != null) {
                Object packet = getCloudLib().getPacketFactory().create(packetId, readString(byteBuf));
                list.add(packet);
            } else {
                byteBuf.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
