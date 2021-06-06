/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendspigot.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ClickableInventory {

    void registerListener();
    void update();
    void destroy();
    void openInventory(Player player);
    void removeItem(int slot);
    void setItem(int slot, ClickableItem item);
    void setItem(int slot, ItemStack itemstack);
    void setItem(int slot, ItemStack itemstack, Consumer<Player> clickConsumer);
    boolean isDestroy();
    void setDestroyOnClose(boolean destroyOnClose);
    void setCloseConsumer(Consumer<Player> closeConsumer);
    ItemStack getFillItem();
    void setFillItem(ItemStack fillItem);

}
