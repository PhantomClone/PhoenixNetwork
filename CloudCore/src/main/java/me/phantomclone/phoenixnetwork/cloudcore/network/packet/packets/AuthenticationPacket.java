/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets;

import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKey;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.Packet;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.PacketValue;

import java.io.IOException;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
@Packet
public class AuthenticationPacket {
    @PacketValue
    private String authKey = "";
    @PacketValue
    private String uuid;
    @PacketValue
    private String name;

    public AuthenticationPacket() {}

    public AuthenticationPacket(AuthKey authKeyObj, String uuid, String name) {
        if (authKeyObj == null) {
            authKey = "need";
        } else {
            try {
                authKey = authKeyObj.getKeyString();
            } catch (IOException e) {
                authKey = "need";
            }
        }
        this.uuid = uuid;
        this.name = name;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
