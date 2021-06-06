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
public class UnregisterServerPacket {

    @PacketValue
    String serverName;

    public UnregisterServerPacket() {}

    public UnregisterServerPacket(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }
}
