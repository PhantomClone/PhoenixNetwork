/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets;

import me.phantomclone.phoenixnetwork.cloudcore.network.packet.Packet;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;

import java.util.Arrays;
import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Packet
public class LobbyAddressesPacket {

    @PacketValue
    List<String> lobbyAddresses;

    public LobbyAddressesPacket(String... lobbyAddresses) {
        this.lobbyAddresses = Arrays.asList(lobbyAddresses);
    }

    public LobbyAddressesPacket(List<String> lobbyAddresses) {
        this.lobbyAddresses = lobbyAddresses;
    }

    public LobbyAddressesPacket() {}

    public List<String> getLobbyAddresses() {
        return lobbyAddresses;
    }
}
