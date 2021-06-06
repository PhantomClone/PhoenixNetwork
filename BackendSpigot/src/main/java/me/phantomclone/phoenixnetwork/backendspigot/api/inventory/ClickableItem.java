/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendspigot.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ClickableItem {

    ItemStack getItemStack();

    Consumer<Player> getClickConsumer();
    void setClickConsumer(Consumer<Player> clickConsumer);

}
