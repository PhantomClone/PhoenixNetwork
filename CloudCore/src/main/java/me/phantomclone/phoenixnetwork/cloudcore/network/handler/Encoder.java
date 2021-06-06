/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.handler;

import io.netty.handler.codec.MessageToByteEncoder;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public abstract class Encoder extends MessageToByteEncoder<Object> {

    private final CloudLib cloudLib;

    public Encoder(CloudLib cloudLib) {
        this.cloudLib = cloudLib;
    }

    public CloudLib getCloudLib() {
        return cloudLib;
    }

}
