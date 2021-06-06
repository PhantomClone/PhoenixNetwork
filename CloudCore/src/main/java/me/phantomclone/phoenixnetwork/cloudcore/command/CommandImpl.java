/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CommandImpl<T extends Conversable> implements Command<T> {

    private final List<String> aliases = new ArrayList<>();

    private Consumer<T> firstHelp, lastHelp, nonArgs;
    private boolean sendHelpMessage = true;

    private final List<SubCommand<T>> subCommands = new ArrayList<>();

    @Override
    public Command<T> addAliases(String... alias) {
        for (String s : alias)
            this.aliases.add(s.toLowerCase());
        return this;
    }

    @Override
    public Command<T> setFirstHelp(Consumer<T> consumer) {
        this.firstHelp = consumer;
        return this;
    }

    @Override
    public Command<T> setLastHelp(Consumer<T> consumer) {
        this.lastHelp = consumer;
        return this;
    }

    @Override
    public Command<T> setSendHelpMessage(boolean b) {
        this.sendHelpMessage = b;
        return this;
    }

    @Override
    public Command<T> setNonArgs(Consumer<T> consumer) {
        this.nonArgs = consumer;
        return this;
    }

    @Override
    public SubCommand<T> addSubCommand() {
        SubCommand<T> subCommand = new SubCommandImpl<>();
        this.subCommands.add(subCommand);
        return subCommand;
    }

    @Override
    public void executeCommand(@NonNull T t, @NonNull String[] args) {
        Objects.requireNonNull(t);
        Objects.requireNonNull(args);
        if (args.length == 0) {
            if (this.nonArgs != null) this.nonArgs.accept(t);
            if (this.sendHelpMessage) {
                if (this.firstHelp != null) this.firstHelp.accept(t);
                this.subCommands.stream().filter(subCommand -> subCommand.getHelp() != null).forEach(subCommand -> subCommand.getHelp().accept(t));
                if (this.lastHelp != null) this.lastHelp.accept(t);
            }
            return;
        }
        if (this.subCommands.stream().noneMatch(subCommand -> subCommand.handle(t, args))) {
            sendHelp(t);
        }
    }

    @Override
    public void sendHelp(@NonNull T t) {
        Objects.requireNonNull(t);
        if (this.firstHelp != null) this.firstHelp.accept(t);
        this.subCommands.stream().filter(subCommand -> subCommand.getHelp() != null).forEach(subCommand -> subCommand.getHelp().accept(t));
        if (this.lastHelp != null) this.lastHelp.accept(t);
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(this.aliases);
    }

    private static class SubCommandImpl<T> implements SubCommand<T> {

        private Consumer<T> help;
        private int length = -1;

        private Map<Integer, Predicate<String>> filterMap = new HashMap<>();
        private Map<Integer, BiConsumer<T, String>> failed = new HashMap<>();

        private BiConsumer<T, String[]> execute;

        @Override
        public SubCommand<T> setHelp(Consumer<T> consumer) {
            this.help = consumer;
            return this;
        }

        @Override
        public SubCommand<T> setArgsLength(int length) {
            this.length = length;
            return this;
        }

        @Override
        public SubCommand<T> addFilter(int argsCount, @NonNull Predicate<String> predicate) {
            Objects.requireNonNull(predicate);
            this.filterMap.put(argsCount, predicate);
            return this;
        }

        @Override
        public SubCommand<T> addFilter(int argsCount, @NonNull Predicate<String> predicate, @NonNull BiConsumer<T, String> failed) {
            Objects.requireNonNull(predicate);
            Objects.requireNonNull(failed);
            this.failed.put(argsCount, failed);
            return addFilter(argsCount, predicate);
        }

        @Override
        public SubCommand<T> execute(@NonNull BiConsumer<T, String[]> execute) {
            Objects.requireNonNull(execute);
            this.execute = execute;
            return this;
        }

        @Override
        public boolean handle(@NonNull T t, String[] args) {
            Objects.requireNonNull(t);
            Objects.requireNonNull(args);
            if (this.length != -1 && args.length != this.length) return false;
            if (!this.filterMap.entrySet().stream().filter(set -> !failed.containsKey(set.getKey())).allMatch(set -> set.getValue().test(args[set.getKey()]))) return false;
            AtomicBoolean failed = new AtomicBoolean(false);
            this.failed.forEach((i, help) -> {
                if (!filterMap.get(i).test(args[i])) {
                    help.accept(t, args[i]);
                    failed.set(true);
                }
            });
            if (!failed.get() && this.execute != null) execute.accept(t, args);
            return true;
        }

        @Override
        public Consumer<T> getHelp() {
            return this.help;
        }
    }
}
