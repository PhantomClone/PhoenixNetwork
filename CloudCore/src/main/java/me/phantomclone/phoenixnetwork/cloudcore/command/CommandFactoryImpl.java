/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CommandFactoryImpl implements CommandFactory {

    public static CommandFactoryImpl create() {
        return new CommandFactoryImpl();
    }

    private CommandFactoryImpl() {}

    @Override
    public Command<Conversable> createCommand(String commandName) {
        return new CommandImpl<>().addAliases(commandName);
    }
}
