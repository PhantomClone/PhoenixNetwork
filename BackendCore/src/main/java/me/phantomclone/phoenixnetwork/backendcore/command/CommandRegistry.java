/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.command;

import java.util.List;

/**
 * @author PhantomClone
 */
public interface CommandRegistry<T> {

    boolean registerCommand(Command<T> command);
    boolean unregisterCommand(Command<T> command);

    boolean registerInServer(Command<T> command);
    boolean unregisterInServer(Command<T> command);

    List<Command<T>> getRegisteredCommands();
    List<Command<T>> getSensitiveCommands();
    Command<T> getCommandByCommandName(String command);

}
