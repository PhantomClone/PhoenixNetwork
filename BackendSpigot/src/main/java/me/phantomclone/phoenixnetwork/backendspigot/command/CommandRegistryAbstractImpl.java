/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendspigot.command;

import me.phantomclone.phoenixnetwork.backendcore.command.Command;
import me.phantomclone.phoenixnetwork.backendcore.command.CommandRegistryAbstract;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PhantomClone
 */
public class CommandRegistryAbstractImpl implements CommandRegistryAbstract<CommandSender> {

    private final List<Command<CommandSender>> commands = new ArrayList<>();

    private final Plugin plugin;

    private final CommandExecutor commandExecutor;

    public static CommandRegistryAbstract<CommandSender> create(Plugin plugin) {
        return new CommandRegistryAbstractImpl(plugin);
    }

    private CommandRegistryAbstractImpl(Plugin plugin) {
        this.plugin = plugin;
        this.commandExecutor = (commandSender, command, label, args) -> {
            commands.stream().filter(c -> c.getCommandAliases().stream().anyMatch(s -> s.equalsIgnoreCase(label))).forEach(cc -> cc.executeCommand(commandSender, args));
            return true;
        };
    }

    @Override
    public boolean registerInServer(Command<CommandSender> command) {
        PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command.getCommandAliases().get(0));
        if (pluginCommand == null) return false;
        pluginCommand.setExecutor(this.commandExecutor);
        return true;
    }

    @Override
    public boolean unregisterInServer(Command<CommandSender> command) {
        PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command.getCommandAliases().get(0));
        if (pluginCommand == null)
            return false;
        pluginCommand.setExecutor(null);
        return true;
    }

    @Override
    public List<Command<CommandSender>> getSensitiveCommands() {
        return commands;
    }
}
