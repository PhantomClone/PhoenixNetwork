/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.console;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.command.Command;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DefaultConsole implements Console, Utils {

    private CloudLib cloudLib;

    @Override
    public String getName() {
        return "DefaultConsole";
    }

    @Override
    public void hello(@NonNull CloudLib cloudLib) {
        Objects.requireNonNull(cloudLib);
        this.cloudLib = cloudLib;
    }

    @Override
    public void handle(@NonNull String[] args) {
        Objects.requireNonNull(args);
        Command<Conversable> command = this.cloudLib.getCommandRegistry().getCommandByAlias(args[0]);
        if (command == null) {
            sendMessage("Unknown Command");
            sendHelp();
            return;
        }
        command.executeCommand(this, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public void sendMessage(@NonNull String message) {
        Objects.requireNonNull(message);
        log(message);
    }

    @Override
    public void sendHelp() {
        sendMessage("-*--Commands--*-");
        this.cloudLib.getCommandRegistry().getAllCommands().forEach(command -> command.sendHelp(this));
        sendMessage("-*--Commands--*-");
    }
}
