/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendbungee;

import me.phantomclone.phoenixnetwork.backendbungee.command.CommandRegistryAbstractImpl;
import me.phantomclone.phoenixnetwork.backendbungee.server.ServerChangerImpl;
import me.phantomclone.phoenixnetwork.backendbungee.storage.StorageRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.Backend;
import me.phantomclone.phoenixnetwork.backendcore.command.CommandRegistry;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ProxyBackend extends Backend<ProxiedPlayer, CommandSender> {

    private final StorageRegistry<ProxiedPlayer> storageRegistry;
    private final ServerChanger<ProxiedPlayer> serverChanger;
    private final CommandRegistry<CommandSender> commandRegistry;

    public static ProxyBackend create(BackendPlugin plugin) {
        return new ProxyBackend(plugin);
    }

    private ProxyBackend(BackendPlugin plugin) {
        this.storageRegistry = StorageRegistryImpl.create(plugin);
        this.serverChanger = ServerChangerImpl.create(plugin);
        this.commandRegistry = CommandRegistryAbstractImpl.create(plugin);
    }

    @Override
    public StorageRegistry<ProxiedPlayer> getStorageRegistry() {
        return storageRegistry;
    }

    @Override
    public ServerChanger<ProxiedPlayer> getServerChanger() {
        return serverChanger;
    }

    @Override
    public CommandRegistry<CommandSender> getCommandRegistry() {
        return commandRegistry;
    }
}
