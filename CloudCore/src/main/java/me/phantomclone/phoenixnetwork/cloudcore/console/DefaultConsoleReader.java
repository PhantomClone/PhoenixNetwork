/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;

import java.util.Objects;
import java.util.Scanner;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DefaultConsoleReader implements ConsoleReader {

    private boolean isAlive = false;

    @Override
    public void start(@NonNull CloudLib cloudLib) {
        Objects.requireNonNull(cloudLib);
        if (isAlive()) return;
        this.isAlive = true;
        cloudLib.getThreadPoolRegistry().submit(() -> {
            cloudLib.getConsoleRegistry().getCurrentConsole().hello(cloudLib);
            while (isAlive()) {
                try {
                    Scanner in = new Scanner(System.in);
                    String line = in.nextLine();
                    if (!line.trim().isEmpty())
                        cloudLib.getConsoleRegistry().getCurrentConsole().handle(line.split(" "));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stop() {
        this.isAlive = false;
    }

    @Override
    public boolean isAlive() {
        return this.isAlive;
    }
}
