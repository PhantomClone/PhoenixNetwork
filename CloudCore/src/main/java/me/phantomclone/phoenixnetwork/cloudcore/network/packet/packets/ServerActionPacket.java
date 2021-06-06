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
public class ServerActionPacket {

    @PacketValue
    String serverAction;

    @PacketValue
    String templateName;
    @PacketValue
    String serverName;
    @PacketValue
    String commandLine = "";

    public ServerActionPacket setServerAction(ServerAction serverAction) {
        this.serverAction = serverAction.name();
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ServerActionPacket setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public ServerActionPacket setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public ServerAction getServerAction() {
        return ServerAction.valueOf(serverAction);
    }

    public String getCommandLine() {
        return commandLine;
    }

    public ServerActionPacket setCommandLine(String commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public enum ServerAction {
        START, STOP, PREPARE, DELETE, COMMAND
    }

}