/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.command;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.console.Conversable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CommandRegistryImpl implements CommandRegistry{

    private final List<Command<Conversable>> commands = new ArrayList<>();

    public static CommandRegistryImpl create(){
        return new CommandRegistryImpl();
    }

    private CommandRegistryImpl(){}

    @Override
    public boolean registerCommand(@NonNull Command<Conversable> command) {
        Objects.requireNonNull(command);
        if (command.getAliases().stream().noneMatch(alias -> getCommandByAlias(alias) != null)) {
            this.commands.add(command);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregisterCommand(@NonNull Command<Conversable> command) {
        Objects.requireNonNull(command);
        this.commands.remove(command);
        return false;
    }

    @Override
    public Command<Conversable> getCommandByAlias(String alias) {
        String finalAlias = alias.toLowerCase();
        return this.commands.stream().filter(command -> command.getAliases().contains(finalAlias)).findFirst().orElse(null);
    }

    @Override
    public List<String> getAllCommandAliases() {
        List<String> list = new ArrayList<>();
        this.commands.forEach(command -> list.addAll(command.getAliases()));
        return list;
    }

    @Override
    public List<Command<Conversable>> getAllCommands() {
        return new ArrayList<>(this.commands);
    }
}
