/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets;

import me.phantomclone.phoenixnetwork.cloudcore.network.packet.Packet;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Packet
public class UpdateServerStatePacket {

    @PacketValue
    String serverName;

    @PacketValue
    String serverState;

    @PacketValue
    boolean success;

    public UpdateServerStatePacket(String serverName, ServerState serverState, boolean success) {
        this.serverName = serverName;
        this.serverState = serverState.name();
        this.success = success;
    }

    public UpdateServerStatePacket() {}

    public String getServerName() {
        return serverName;
    }

    public ServerState getAction() {
        return ServerState.valueOf(serverState);
    }

    public boolean isSuccess() {
        return success;
    }

}
