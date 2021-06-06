/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.utils;

import lombok.NonNull;

import java.util.Date;
import java.util.Objects;

/**
 * <p>A interface that represents utils for console.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Utils {

    String ANSI_RESET = "\u001B[0m";
    String ANSI_BLACK = "\u001B[30m";
    String ANSI_RED = "\u001B[31m";
    String ANSI_GREEN = "\u001B[32m";
    String ANSI_YELLOW = "\u001B[33m";
    String ANSI_BLUE = "\u001B[34m";
    String ANSI_PURPLE = "\u001B[35m";
    String ANSI_CYAN = "\u001B[36m";
    String ANSI_WHITE = "\u001B[37m";

    /**
     * Default methode to println a text with prefix in the console out.
     * @param text The text which get printed which a prefix out.
     */
    default void log(@NonNull String text){
        Objects.requireNonNull(text);
        System.out.println(ANSI_PURPLE +new Date().toLocaleString() +ANSI_RESET+" > " + text);
    }

    /**
     * A default methode which clears the console.
     */
    default void clearConsole(){
        final String os = System.getProperty("os.name");

        try {
            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}