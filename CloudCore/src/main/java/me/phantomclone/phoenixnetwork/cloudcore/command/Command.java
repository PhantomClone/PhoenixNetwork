/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

import java.util.List;
import java.util.function.Consumer;

/**
 * <p>Represents a Command that can send Messages as String during a methode in {@link Conversable#sendMessage(String)}.</p>
 *
 * <p>You get an instance of this class in {@link CommandFactory#createCommand(String)}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Command<T extends Conversable> {

    /**
     * Add an alias for this {@link Command} which will return this {@link Command} in {@link CommandRegistry#getCommandByAlias(String)} if the aliases are the same.
     * @param aliases Non-null aliases which will be added in this {@link Command}.
     * @return It returns itself to allows a chain.
     */
    Command<T> addAliases(@NonNull String... aliases);

    /**
     * Add a help {@link Consumer} which will be called at first in {@link Command#sendHelp(Conversable)}.
     * @param consumer A parameter with can be null. I will be set to first help {@link Consumer}.
     * @return It returns itself to allows a chain.
     */
    Command<T> setFirstHelp(Consumer<T> consumer);
    /**
     * Add a help {@link Consumer} which will be called at least in {@link Command#sendHelp(Conversable)}.
     * @param consumer A parameter with can be null. I will be set to least help {@link Consumer}.
     * @return It returns itself to allows a chain.
     */
    Command<T> setLastHelp(Consumer<T> consumer);

    /**
     * It set the boolean sendHelpMessage.
     * If it is set to {@code true}, then it will call the methode {@link Command#sendHelp(Conversable)} when in {@link Command#executeCommand(Conversable, String[])} the string array is empty.
     * If it is set to {@code false}, then it wont call the methode.
     * @param b A parameter which will be saved in {@link Command}.
     * @return It returns itself to allows a chain.
     */
    Command<T> setSendHelpMessage(boolean b);
    /**
     * It set the {@link Consumer} which will be called when the methode {@link Command#executeCommand(Conversable, String[])} is called when string array is empty.
     * @param consumer A parameter which will be saved in {@link Command}.
     * @return It returns itself to allows a chain.
     */
    Command<T> setNonArgs(Consumer<T> consumer);

    /**
     * It add a new {@link SubCommand} in {@link Command} and return it.
     * @return It returns a new {@link SubCommand}.
     */
    SubCommand<T> addSubCommand();

    /**
     * It execute the Command and loop throw all added {@link SubCommand} and call the methode {@link SubCommand#handle(Object, String[])}.
     * @param t This parameter is non-null and extends from {@link Conversable} to allow to send Messages.
     * @param args A non-null Array handover in {@link SubCommand#handle(Object, String[])}.
     */
    void executeCommand(@NonNull T t, @NonNull String[] args);

    /**
     * It return all aliases of this {@link Command}.
     * @return It return all Aliases which are added in {@link Command#addAliases(String...)}.
     */
    List<String> getAliases();

    /**
     * This methode will call first {@link Consumer#accept(Object)} with is set in {@link Command#setFirstHelp(Consumer)} if it is not null.
     * Then it will call {@link Consumer#accept(Object)} in all {@link SubCommand} which is set in {@link SubCommand#setHelp(Consumer)} if it is not null.
     * At least it will call {@link Consumer#accept(Object)} with is set in {@link Command#setLastHelp(Consumer)} if it is not null.
     * @param t This parameter is non-null extends from {@link Conversable} to allow to send Messages.
     */
    void sendHelp(@NonNull T t);

}
