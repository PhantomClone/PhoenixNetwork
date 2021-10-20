package me.phantomclone.phoenixnetwork.backendspigot;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.phantomclone.phoenixnetwork.backendcore.Backend;
import me.phantomclone.phoenixnetwork.backendspigot.api.inventory.ClickableInventory;
import me.phantomclone.phoenixnetwork.backendspigot.api.inventory.ClickableInventoryFactory;
import me.phantomclone.phoenixnetwork.backendspigot.storage.BasicData;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class BackendPlugin extends JavaPlugin implements ClickableInventoryFactory {

    private Backend<Player, CommandSender> backend;

    @Override
    public void onEnable() {
        this.backend = SpigotBackend.create(this);
        PluginCommand spigotTest1Command = getCommand("spigotTest1");
        if (spigotTest1Command != null)
            spigotTest1Command.setExecutor((commandSender, command, s, strings) -> {
                BasicData basicData = this.backend.getStorageRegistry().getStoreObject(((Player) commandSender).getUniqueId(), BasicData.class);
                commandSender.sendMessage(new Gson().toJson(basicData));
                return true;
            });
        PluginCommand spigotTest2Command = getCommand("spigotTest2");
        if (spigotTest2Command != null)
            spigotTest2Command.setExecutor((commandSender, command, s, strings) -> {
                long start = System.currentTimeMillis();
                this.backend.getStorageRegistry().getOfflineObject(BasicData.class, UUID.fromString("791addad-5ff2-49bd-ac04-d94f58ae3e0e"), basicData -> {
                    commandSender.sendMessage("Delay: " + (System.currentTimeMillis() - start) + "ms");
                    commandSender.sendMessage(new Gson().toJson(basicData));
                });
                return true;
            });

        this.backend.getDatabaseLib().getJedisRegistry().load();
        this.backend.getStorageRegistry().init();
        this.backend.getStorageRegistry().registerStorable(BasicData.class, (player, map) -> {
            map.put("name", player.getName());
            map.put("firstLogin", System.currentTimeMillis());
            map.put("lastLogin", System.currentTimeMillis());
            map.put("playtime", 0L);
            map.put("nameHistory", Lists.newArrayList(player.getName()));
            map.put("language", 0);
        });
    }

    public Backend<Player, CommandSender> getBackend() {
        return backend;
    }

}
