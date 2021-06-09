package me.phantomclone.phoenixnetwork.backendspigot;

import me.phantomclone.phoenixnetwork.backendcore.Backend;
import me.phantomclone.phoenixnetwork.backendcore.command.CommandRegistry;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;
import me.phantomclone.phoenixnetwork.backendspigot.command.CommandRegistryAbstractImpl;
import me.phantomclone.phoenixnetwork.backendspigot.server.ServerChangerImpl;
import me.phantomclone.phoenixnetwork.backendspigot.storage.StorageRegistryImpl;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class SpigotBackend extends Backend<Player, CommandSender> {

    private final StorageRegistry<Player> storageRegistry;
    private final ServerChanger<Player> serverChanger;
    private final CommandRegistry<CommandSender> commandRegistry;

    public static SpigotBackend create(BackendPlugin plugin) {
        return new SpigotBackend(plugin);
    }

    private SpigotBackend(BackendPlugin plugin) {
        this.storageRegistry = StorageRegistryImpl.create(plugin);
        this.serverChanger = ServerChangerImpl.create(plugin);
        this.commandRegistry = CommandRegistryAbstractImpl.create(plugin);
    }

    @Override
    public StorageRegistry<Player> getStorageRegistry() {
        return storageRegistry;
    }

    @Override
    public ServerChanger<Player> getServerChanger() {
        return serverChanger;
    }

    @Override
    public CommandRegistry<CommandSender> getCommandRegistry() {
        return commandRegistry;
    }
}
