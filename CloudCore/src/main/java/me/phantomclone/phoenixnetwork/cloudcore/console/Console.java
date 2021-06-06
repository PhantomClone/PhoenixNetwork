/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

/**
 * <p>A interface that represents an console interface which have a Name, start methode {@link Console#sendHelp()}, handle incoming command line {@link Console#handle(String[])}.</p>
 * <p>Also it have a methode to get help, to what can u do with this console.</p>
 *
 * <p>It extends out of {@link Conversable} to enable to {@link Conversable#sendMessage(String)} to communicate.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Console extends Conversable {

    /**
     * Returns the name of this console.
     * @return Return the name of this console.
     */
    String getName();

    /**
     * The start up methode. It will be called, if this console get active.
     * @param cloudLib It set the non-null {@link CloudLib} to handle some incoming commands where it is needed.
     */
    void hello(@NonNull CloudLib cloudLib);

    /**
     * The main methode in the console class. It will handle incoming command lines.
     *
     * For example it could handle System.in and transfer it into an execution of a command.
     *
     * @param args The incoming non-null command line.
     */
    void handle(@NonNull String[] args);

    /**
     * A methode which communicate with {@link Conversable} and tells what u can do with this Console.
     */
    void sendHelp();

}
