/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * <p>A interface that represents a ConsoleReader.</p>
 *
 * <p>It reads incoming lines and invoke it in {@link Console#handle(String[])}.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ConsoleReader {

    /**
     * It start the command line reading.
     *
     * It can only start if {@link ConsoleReader#isAlive()} it {@code false}.
     *
     * It set {@link ConsoleReader#isAlive()} to {@code true} if it have started.
     *
     * @param cloudLib Non-null {@link CloudLib} which will be set in this class to gain access.
     */
    void start(@NonNull CloudLib cloudLib);

    /**
     * It stops the command line reading and set {@link ConsoleReader#isAlive()} to {@code false}.
     */
    void stop();

    /**
     * It returns if the command line reading is still active.
     * @return It returns {@code true} when the command line reading is still active, else it returns {@code false}.
     */
    boolean isAlive();

}
