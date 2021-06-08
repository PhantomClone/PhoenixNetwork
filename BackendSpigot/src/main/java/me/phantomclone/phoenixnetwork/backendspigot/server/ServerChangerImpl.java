package me.phantomclone.phoenixnetwork.backendspigot.server;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import me.phantomclone.phoenixnetwork.backendspigot.BackendPlugin;
import me.phantomclone.phoenixnetwork.backendspigot.storage.StorageRegistryImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerChangerImpl implements ServerChanger<Player>, PluginMessageListener, Listener {

    private final BackendPlugin plugin;

    public static ServerChangerImpl create(BackendPlugin plugin) {
        return new ServerChangerImpl(plugin);
    }

    private ServerChangerImpl(BackendPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, "BackendChannel", this);
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BackendChannel");
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void unregister() {
        this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, "BackendChannel", this);
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, "BackendChannel");
        HandlerList.unregisterAll(this);
    }

    @Override
    public void sendPlayerToServer(Player player, String host, String port) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ((StorageRegistryImpl) this.plugin.getBackend().getStorageRegistry()).store(player.getUniqueId());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            try {
                out.writeUTF("SendPlayerToServer");
                out.writeUTF(player.getUniqueId().toString());
                out.writeUTF(host + "_" + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.sendPluginMessage(this.plugin, "BackendChannel", stream.toByteArray());
        });
    }

    @Override
    public void sendPlayerToServer(Player player, String serverName) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ((StorageRegistryImpl)this.plugin.getBackend().getStorageRegistry()).store(player.getUniqueId());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            try {
                out.writeUTF("SendPlayerToServer");
                out.writeUTF(player.getUniqueId().toString());
                out.writeUTF(serverName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.sendPluginMessage(this.plugin, "BackendChannel", stream.toByteArray());
        });
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equalsIgnoreCase("BackendChannel")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String first = in.readUTF();
            if (first.contains(":")) {
                sendPlayerToServer(player, first.split(":")[0], first.split(":")[1]);
            } else {
                sendPlayerToServer(player, first);
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        event.setCancelled(true);
        if (event.getReason().contains("SendToServer"))
            sendPlayerToServer(event.getPlayer(), event.getReason().replace("SendToServer", ""));
        else
            sendPlayerToServer(event.getPlayer(), "BackToLobby");
    }
}
