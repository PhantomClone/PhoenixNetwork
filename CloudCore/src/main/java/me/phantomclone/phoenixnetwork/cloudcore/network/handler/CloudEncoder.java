/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;
import me.phantomclone.phoenixnetwork.cloudcore.utils.ByteBufUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudEncoder extends Encoder implements ByteBufUtils {

    public CloudEncoder(CloudLib cloudLib) {
        super(cloudLib);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object packet, ByteBuf byteBuf) throws Exception {
        try {
            int packetId = getCloudLib().getPacketRegistry().getPacketId(packet.getClass());
            if (packetId != -1) {
                byteBuf.writeInt(packetId);
                writeString(serialize(packet), byteBuf);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    String serialize(Object object) {
        Gson gson = new Gson();
        HashMap<String, Object> values = new HashMap<>();
        Arrays.asList(object.getClass().getDeclaredFields()).forEach(field -> {
            PacketValue value = field.getAnnotation(PacketValue.class);
            if (value != null) {
                field.setAccessible(true);
                try {
                    values.put(field.getName(), field.get(object));
                } catch (IllegalAccessException e) {
                }
            }
        });
        return gson.toJson(values);
    }
}
