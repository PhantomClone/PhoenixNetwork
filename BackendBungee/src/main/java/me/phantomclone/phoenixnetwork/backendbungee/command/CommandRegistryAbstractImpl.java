/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendbungee.command;

import me.phantomclone.phoenixnetwork.backendcore.command.Command;
import me.phantomclone.phoenixnetwork.backendcore.command.CommandRegistryAbstract;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author PhantomClone
 */
public class CommandRegistryAbstractImpl implements CommandRegistryAbstract<CommandSender> {

    private final List<Command<CommandSender>> commands = new ArrayList<>();

    private final Plugin plugin;

    public static CommandRegistryAbstract<CommandSender> create(Plugin plugin) {
        return new CommandRegistryAbstractImpl(plugin);
    }

    private CommandRegistryAbstractImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerInServer(Command<CommandSender> command) {

        String[] aliases = new String[command.getCommandAliases().size() - 1];
        for (int i = 1; i < command.getCommandAliases().size(); i++) {
            aliases[i - 1] = command.getCommandAliases().get(i);
        }

        this.plugin.getProxy().getPluginManager().registerCommand(this.plugin,
            new net.md_5.bungee.api.plugin.Command(command.getCommandAliases().get(0), null, aliases) {
                @Override
                public void execute(CommandSender sender, String[] args) {
                    command.executeCommand(sender, args);
                }
            });
        return true;
    }

    @Override
    public boolean unregisterInServer(Command<CommandSender> command) {
        Map.Entry<String, net.md_5.bungee.api.plugin.Command> stringCommandEntry = this.plugin.getProxy().getPluginManager().getCommands().stream().filter(set -> set.getValue().getName().equalsIgnoreCase(command.getCommandAliases().get(0))).findFirst().orElse(null);
        if (stringCommandEntry == null || stringCommandEntry.getValue() == null) return false;
        this.plugin.getProxy().getPluginManager().unregisterCommand(stringCommandEntry.getValue());
        return true;
    }


    @Override
    public List<Command<CommandSender>> getSensitiveCommands() {
        return this.commands;
    }
}
