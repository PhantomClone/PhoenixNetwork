/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.command;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 */
public interface Command<T> {

    Command<T> firstHelpConsumer(Consumer<T> consumer);
    Command<T> lastHelpConsumer(Consumer<T> consumer);

    Command<T> noArgsConsumer(Consumer<T> consumer);
    <B extends T> Command<T> noArgsConsumer(Consumer<B> consumer, Class<B> senderClass);
    <B extends T> Command<T> noArgsConsumer(Consumer<B> consumer, Class<B> senderClass, Consumer<T> notSenderClass);

    SubCommand<T> addSubCommand();

    void executeCommand(T t, String[] args);

    void sendHelp(T t);

    List<String> getCommandAliases();
    Command<T> addAliases(String... aliases);
}
