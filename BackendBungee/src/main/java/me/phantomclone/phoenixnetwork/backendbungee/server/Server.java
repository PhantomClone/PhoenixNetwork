/*
 *
 * @author PhantomClone
 *
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 *
 */

package me.phantomclone.phoenixnetwork.backendbungee.server;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Server {

    String getName();
    String getHost();
    int getPort();
    String getTemplateName();

    ServerState getServerState();
    void setServerState(ServerState serverState);

}
