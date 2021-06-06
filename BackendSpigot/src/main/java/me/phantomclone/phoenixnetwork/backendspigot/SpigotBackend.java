package me.phantomclone.phoenixnetwork.backendspigot;

import me.phantomclone.phoenixnetwork.backendcore.Backend;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;
import me.phantomclone.phoenixnetwork.backendspigot.server.ServerChangerImpl;
import me.phantomclone.phoenixnetwork.backendspigot.storage.StorageRegistryImpl;
import org.bukkit.entity.Player;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class SpigotBackend extends Backend<Player> {

    private final StorageRegistry<Player> storageRegistry;
    private final ServerChanger<Player> serverChanger;

    public static SpigotBackend create(BackendPlugin plugin) {
        return new SpigotBackend(plugin);
    }

    private SpigotBackend(BackendPlugin plugin) {
        this.storageRegistry = StorageRegistryImpl.create(plugin);
        this.serverChanger = ServerChangerImpl.create(plugin);
    }

    @Override
    public StorageRegistry<Player> getStorageRegistry() {
        return storageRegistry;
    }

    @Override
    public ServerChanger<Player> getServerChanger() {
        return serverChanger;
    }
}
