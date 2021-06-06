/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets;

import me.phantomclone.phoenixnetwork.cloudcore.network.packet.Packet;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Packet
public class StopPacket {

    @PacketValue
    String reason;

    public StopPacket() {}

    public StopPacket(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
