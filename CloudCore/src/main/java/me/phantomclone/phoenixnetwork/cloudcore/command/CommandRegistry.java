/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

import java.util.List;

/**
 * <p>A interface that represents a registry which caches {@link Command}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface CommandRegistry {

    /**
     * It add the {@link Command} in {@link List} in this class if non {@link Command#getAliases()} compares to a other one of the registered commands.
     * @param command This non-null command will be register if it pass the mention comparison.
     * @return An boolean if it successfully registered or not.
     */
    boolean registerCommand(@NonNull Command<Conversable> command);

    /**
     * It removes the {@link Command} in {@link List} in this class if it is registered.
     * @param command This non-null command will be unregister if it is registered.
     * @return An boolean if the remove was successfully or not.
     */
    boolean unregisterCommand(@NonNull Command<Conversable> command);

    /**
     * It search for a {@link Command} which is registered and contains that alias in the {@link Command#getAliases()}.
     * If it find it, it will return this {@link Command} if not it returns null.
     * @param alias The alias which will be use to search for the command.
     * @return It returns the found {@link Command} or when not it returns fauls
     */
    Command<Conversable> getCommandByAlias(String alias);

    /**
     * Give all aliases from all registered Command back
     * @return It return all aliases from all registered Command ({@link Command#getAliases()})
     */
    List<String> getAllCommandAliases();

    /**
     * Give all registered {@link Command}.
     * @return Returns all registered {@link Command} in a {@link List} as Copy.
     */
    List<Command<Conversable>> getAllCommands();

}
