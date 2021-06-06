/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

/**
 *  <p>A interface that represents a factory which can create a new {@link Command}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface CommandFactory {

    /**
     * It creates a new {@link Command} and set it an alias.
     * @param alias Automatically add this alias in {@link Command#addAliases(String...)}.
     * @return It returns a new {@link Command}.
     */
    Command<Conversable> createCommand(String alias);

}
