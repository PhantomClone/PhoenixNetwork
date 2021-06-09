/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.command;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PhantomClone
 */
public interface CommandRegistryAbstract<T> extends CommandRegistry<T> {

    @Override
    default boolean registerCommand(Command<T> command) {
        if (command.getCommandAliases().stream().anyMatch(alias -> getCommandByCommandName(alias) != null)) return false;
        if (!registerInServer(command)) {
            return false;
        }
        return getSensitiveCommands().add(command);
    }

    @Override
    default boolean unregisterCommand(Command<T> command) {
        return getSensitiveCommands().remove(command);
    }

    @Override
    default List<Command<T>> getRegisteredCommands() {
        return new ArrayList<>(this.getSensitiveCommands());
    }

    @Override
    default Command<T> getCommandByCommandName(String command) {
        return this.getRegisteredCommands().stream().filter(c -> c.getCommandAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(command))).findFirst().orElse(null);
    }
}
