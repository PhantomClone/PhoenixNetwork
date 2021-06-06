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
public class RegisterServerPacket {

    @PacketValue
    String templateName;

    @PacketValue
    String port;

    @PacketValue
    String serverState;

    public RegisterServerPacket() {}

    public RegisterServerPacket(String templateName, int port) {
        this.templateName = templateName;
        this.port = port + "";
        this.serverState = "DELETED";
    }

    public String getTemplateName() {
        return templateName;
    }

    public int getPort() {
        return Integer.parseInt(port);
    }

    public ServerState getServerState() {
        return ServerState.valueOf(this.serverState);
    }

    public RegisterServerPacket setServerState(ServerState serverState) {
        this.serverState = serverState.toString();
        return this;
    }
}
