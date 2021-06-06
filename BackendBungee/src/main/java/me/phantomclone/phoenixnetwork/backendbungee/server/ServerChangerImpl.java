package me.phantomclone.phoenixnetwork.backendbungee.server;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.phantomclone.phoenixnetwork.backendbungee.BackendPlugin;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerChangerImpl implements ServerChanger<ProxiedPlayer>, Listener {

    private final BackendPlugin plugin;

    public static ServerChanger<ProxiedPlayer> create(BackendPlugin plugin) {
        return new ServerChangerImpl(plugin);
    }

    private ServerChangerImpl(BackendPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().registerChannel("BackendChannel");
        this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
    }

    @Override
    public void unregister() {
        plugin.getProxy().unregisterChannel("BackendChannel");
        plugin.getProxy().getPluginManager().unregisterListener(this);
    }

    @Override
    public void sendPlayerToServer(ProxiedPlayer player, String host, String port) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(host + ":" + port);
        player.getServer().sendData("BackendChannel", out.toByteArray());
    }

    @Override
    public void sendPlayerToServer(ProxiedPlayer player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(serverName);
        player.getServer().sendData("BackendChannel", out.toByteArray());

    }

    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase("BackendChannel")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String uuid = in.readUTF();
            ProxiedPlayer player = this.plugin.getProxy().getPlayer(UUID.fromString(uuid));
            if (player == null) {
                System.out.println("Big error in PluginMessageEvent... uuid do not match with a player!");
                return;
            }
            String server = in.readUTF();
            if (server.equalsIgnoreCase("BackToLobby")) {
                Server randomLobbyServer = getRandomLobbyServer();
                if (randomLobbyServer == null) {
                    player.disconnect(new TextComponent("§cEs wurde keine Lobby gefunden!"));
                } else {
                    this.plugin.getServerRegistry().sendPlayerToServer(player, server, b -> {});
                }
            } else if (server.contains(":")) {
                this.plugin.getServerRegistry().sendPlayerToServer(player, server.replace(":", "_"), b -> {});
            } else {
                //TODO REMOVE WHEN CLOUD SERVER REGISTRY IS IMPLEMENTED
                ServerInfo serverInfo = plugin.getProxy().getServerInfo(server);
                player.connect(serverInfo);
                //this.plugin.getServerRegistry().sendPlayerToServer(player, server, b -> {});
            }
        }
    }

    private Server getRandomLobbyServer() {
        List<Server> list = Lists.newArrayList();
        for (ServerInfo info : this.plugin.getProxy().getServers().values()) {
            Server server = this.plugin.getServerRegistry().getServerByServerName(info.getName());
            if (server != null && server.getServerState().equals(ServerState.STARTED)) {
                list.add(server);
            }
        }
        if (list.size() > 0) {
            return list.get(new Random().nextInt(list.size()));
        }
        return null;
    }
}
