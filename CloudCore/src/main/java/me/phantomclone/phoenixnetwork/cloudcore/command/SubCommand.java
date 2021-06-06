/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * <p>Represents a SubCommand that can send Messages as String during a methode in {@link Conversable#sendMessage(String)}.</p>
 *
 * <p>You get an instance of this class with {@link Command#addSubCommand()}.</p>
 *
 * <p>Each {@link SubCommand} belongs to one {@link Command}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface SubCommand<T> {

    /**
     * Add a help {@link Consumer} which will be called in {@link Command#sendHelp(Conversable)}.
     * @param consumer A parameter with can be null. I will be set to least help {@link Consumer}.
     * @return It returns itself to allows a chain.
     */
    SubCommand<T> setHelp(Consumer<T> consumer);

    /**
     * Set the arguments length which will filter incoming lines in {@link SubCommand#handle(Object, String[])}.
     * If length of the string array are the same as in {@link SubCommand#handle(Object, String[])} it pass.
     * If the length negative 1 it do not use it to filter in {@link SubCommand#handle(Object, String[])}.
     * @param length It set the length in {@link SubCommand}. By default the value is negative 1.
     * @return It returns itself to allows a chain.
     */
    SubCommand<T> setArgsLength(int length);

    /**
     * Set a incoming String predication with the incoming String value in {@link SubCommand#handle(Object, String[])} with argsCount.
     * If it {@code true} then it will pass else not.
     * @param argsCount An integer which use to get a {@link String} out of {@link String[]} in {@link SubCommand#handle(Object, String[])}.
     * @param predicate A non-null predication which compares with the value of {@link SubCommand#handle(Object, String[])} with the argsCount.
     * @return It returns itself to allows a chain.
     */
    SubCommand<T> addFilter(int argsCount, @NonNull Predicate<String> predicate);
    /**
     * Set a incoming String predication with the incoming String value in {@link SubCommand#handle(Object, String[])} with argsCount.
     * If it {@code true} then it will {@link BiConsumer#accept(Object, Object)} the parameter 'failed'.
     * @param argsCount An integer which use to get a {@link String} out of {@link String[]} in {@link SubCommand#handle(Object, String[])}.
     * @param predicate A non-null predication which compares with the value of {@link SubCommand#handle(Object, String[])} with the argsCount.
     * @param failed A non-null {@link BiConsumer} which {@link BiConsumer#accept(Object, Object)} when the prediction fails.
     * @return It returns itself to allows a chain.
     */
    SubCommand<T> addFilter(int argsCount, @NonNull Predicate<String> predicate, @NonNull BiConsumer<T, String> failed);

    /**
     * Set a BiConsumer which {@link BiConsumer#accept(Object, Object)} when {@link SubCommand#handle(Object, String[])} pass throw all filters.
     * @param execute Set a non-null BiConsumer.
     * @return It returns itself to allows a chain.
     */
    SubCommand<T> execute(@NonNull BiConsumer<T, String[]> execute);

    /**
     * It will filter the in coming command in {@link String[]} and filter it with the added filter.
     * @param t This parameter is non-null and extends from {@link Conversable} to allow to send Messages.
     * @param args A non-null Array handover in {@link SubCommand#handle(Object, String[])}.
     * @return Returns true when it pass true all normal filters.
     */
    boolean handle(@NonNull T t, @NonNull String[] args);

    /**
     * It return the help Consumer of this {@link SubCommand}.
     * @return It return the help Consumer which are set in {@link SubCommand#setHelp(Consumer)}.
     */
    Consumer<T> getHelp();

}
