/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ConsoleRegistryImpl implements ConsoleRegistry {

    private Console console;
    private ConsoleReader consoleReader;
    private ConsoleReader consoleReaders;

    public static ConsoleRegistryImpl create() {
        return new ConsoleRegistryImpl();
    }

    private ConsoleRegistryImpl() {}

    @Override
    public Console getCurrentConsole() {
        return this.console;
    }

    @Override
    public void setReader(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    @Override
    public void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public ConsoleReader getConsoleReader() {
        return this.consoleReader;
    }
}
