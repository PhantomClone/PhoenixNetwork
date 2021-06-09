/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.command;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author PhantomClone
 */
public class CommandImpl<T> implements Command<T> {

    private final List<String> aliases = new ArrayList<>();

    private Consumer<T> firstHelp, lastHelp;

    private Consumer<T> noArgs;
    private Class<? extends T> senderClass;
    private Consumer<T> notSenderClass;

    private final List<SubCommand<T>> subCommands = new ArrayList<>();

    public CommandImpl(String command) {
        this.aliases.add(command);
    }

    @Override
    public Command<T> firstHelpConsumer(Consumer<T> consumer) {
        this.firstHelp = consumer;
        return this;
    }

    @Override
    public Command<T> lastHelpConsumer(Consumer<T> consumer) {
        this.lastHelp = consumer;
        return this;
    }

    @Override
    public Command<T> noArgsConsumer(Consumer<T> consumer) {
        this.noArgs = consumer;
        return this;
    }

    @Override
    public <B extends T> Command<T> noArgsConsumer(Consumer<B> consumer, Class<B> senderClass) {
        return noArgsConsumer(consumer, senderClass, null);
    }

    @Override
    public <B extends T> Command<T> noArgsConsumer(Consumer<B> consumer, Class<B> senderClass, Consumer<T> notSenderClass) {
        this.noArgs = (Consumer<T>) consumer;
        this.senderClass = senderClass;
        this.notSenderClass = notSenderClass;
        return this;
    }

    @Override
    public SubCommand<T> addSubCommand() {
        SubCommand<T> subCommand = new SubCommandImpl();
        this.subCommands.add(subCommand);
        return subCommand;
    }

    @Override
    public void executeCommand(T t, String[] args) {
        if (args.length == 0) {
            if (this.noArgs == null) {
                sendHelp(t);
                return;
            }
            if (this.senderClass != null && !this.senderClass.isInstance(t) && this.notSenderClass != null) {
                this.notSenderClass.accept(t);
                return;
            }
            this.noArgs.accept(t);
            return;
        }
        if (this.subCommands.stream().noneMatch(subCommand -> subCommand.handle(t, args))) {
            sendHelp(t);
        }
    }

    @Override
    public void sendHelp(T t) {
        if (this.firstHelp != null) this.firstHelp.accept(t);
        this.subCommands.stream().filter(subCommand -> subCommand.getHelp() != null).forEach(subCommand -> subCommand.getHelp().accept(t));
        if (this.lastHelp != null) this.lastHelp.accept(t);
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>(this.aliases);
    }

    @Override
    public Command<T> addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    private class SubCommandImpl implements SubCommand<T> {

        private Consumer<T> help;
        private int length = -1;

        private final Map<Integer, Predicate<String>> filterMap = new HashMap<>();
        private final Map<Integer, BiConsumer<T, String>> failed = new HashMap<>();

        private BiConsumer<T, String[]> execute;
        private Class<? extends T> senderClass;
        private Consumer<T> notSenderClass;

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
        public SubCommand<T> addFilter(int argsCount, Predicate<String> predicate) {
            this.filterMap.put(argsCount, predicate);
            return this;
        }

        @Override
        public SubCommand<T> addFilter(int argsCount, Predicate<String> predicate, BiConsumer<T, String> failed) {
            this.failed.put(argsCount, failed);
            return addFilter(argsCount, predicate);
        }

        @Override
        public void execute(BiConsumer<T, String[]> execute) {
            this.execute = execute;
        }

        @Override
        public <B extends T> void execute(BiConsumer<B, String[]> execute, Class<B> senderClass) {
            execute(execute, senderClass, null);
        }

        @Override
        public <B extends T> void execute(BiConsumer<B, String[]> execute, Class<B> senderClass, Consumer<T> notSenderClass) {
            this.execute = (BiConsumer<T, String[]>) execute;
            this.senderClass = senderClass;
            this.notSenderClass = notSenderClass;
        }

        @Override
        public boolean handle(T t, String[] args) {
            if (this.length != -1 && args.length != this.length) return false;
            if (!this.filterMap.entrySet().stream().filter(set -> !failed.containsKey(set.getKey())).allMatch(set -> set.getValue().test(args[set.getKey()]))) return false;
            if (this.senderClass != null && !this.senderClass.isInstance(t)) {
                if (this.notSenderClass != null)
                    this.notSenderClass.accept(t);
                return true;
            }
            AtomicBoolean failed = new AtomicBoolean(false);
            this.failed.forEach((i, help) -> {
                if (i >= args.length || !filterMap.get(i).test(args[i])) {
                    help.accept(t, i >= args.length ? "NULL" : args[i]);
                    failed.set(true);
                }
            });
            if (!failed.get() && this.execute != null)
                execute.accept(t, args);
            return true;
        }

        @Override
        public Consumer<T> getHelp() {
            return this.help;
        }
    }

}
