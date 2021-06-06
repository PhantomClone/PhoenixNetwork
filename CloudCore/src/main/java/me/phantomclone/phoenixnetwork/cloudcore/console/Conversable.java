/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

/**
 * <p>A interface that represents an lambda which allows to send messages.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Conversable {

    /**
     * It invoke when someone want to send a message back.
     * @param message The message which someone send back.
     */
    void sendMessage(String message);

}
