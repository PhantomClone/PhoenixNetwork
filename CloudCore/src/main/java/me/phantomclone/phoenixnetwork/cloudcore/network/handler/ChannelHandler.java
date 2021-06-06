/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public abstract class ChannelHandler extends SimpleChannelInboundHandler<Object> implements Utils {

    private final CloudLib cloudLib;

    public ChannelHandler(CloudLib cloudLib) {
        this.cloudLib = cloudLib;
    }

    public CloudLib getCloudLib() {
        return cloudLib;
    }
}