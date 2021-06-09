/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.command;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author PhantomClone
 */
public interface SubCommand<T> {


    SubCommand<T> setHelp(Consumer<T> consumer);
    SubCommand<T> setArgsLength(int length);
    SubCommand<T> addFilter(int argsCount, Predicate<String> predicate);
    SubCommand<T> addFilter(int argsCount, Predicate<String> predicate, BiConsumer<T, String> failed);

    void execute(BiConsumer<T, String[]> execute);
    <B extends T> void execute(BiConsumer<B, String[]> execute, Class<B> senderClass);
    <B extends T> void execute(BiConsumer<B, String[]> execute, Class<B> senderClass, Consumer<T> notSenderClass);

    boolean handle(T t, String[] args);

    Consumer<T> getHelp();

}
