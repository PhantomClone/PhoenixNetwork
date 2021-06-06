/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * <p>A interface that represents a registry for the console.</p>
 *
 * <p>It can only hole one active {@link Console} and one active {@link ConsoleReader}.</p>
 *
 * <p>It just a class to register them. It do not start or stop them!</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ConsoleRegistry {

    /**
     * Returns the current console.
     * @return Returns the current console. It can return null when non console is registered.
     */
    Console getCurrentConsole();

    /**
     * Set the non-null {@link ConsoleReader}.
     * It will not stop the old {@link ConsoleReader#stop()} and it will not start the new one!
     * @param consoleReader A nullable {@link ConsoleReader} which will be set in this class.
     */
    void setReader(ConsoleReader consoleReader);

    /**
     * It just set the {@link ConsoleRegistry#getCurrentConsole()} to the new one.
     *
     * It do not stop the old {@link Console} either it say {@link Console#hello(CloudLib)} to the new one!
     *
     * @param console A nullable {@link Console} which will be set to the current console.
     */
    void setConsole(Console console);

    /**
     * It returns the {@link ConsoleReader} which is registered with {@link ConsoleRegistry#setReader(ConsoleReader)}.
     * @return It return the {@link ConsoleRegistry#setReader(ConsoleReader)}, it can return null, when non is {@link ConsoleReader} is set.
     */
    ConsoleReader getConsoleReader();

}
