/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendspigot.api.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 */
public interface ClickableInventoryFactory {

    default ClickableInventory createClickableInventory(Inventory inventory, Plugin plugin) {
      return new ClickableInventory() {

          private final HashMap<Integer, ClickableItem> items = Maps.newHashMap();
          private ItemStack fillItem;

          private Consumer<Player> closeConsumer;
          private boolean destroy;
          private boolean destroyOnClose;

          private final List<String> openUuids = Lists.newArrayList();

          private final Listener listener = new Listener() {

              @EventHandler
              public void click(InventoryClickEvent event) {
                  if (event.getWhoClicked() instanceof Player && !openUuids.contains(event.getWhoClicked().getUniqueId().toString())) {
                      return;
                  }
                  event.setCancelled(true);

                  if (items.containsKey(event.getRawSlot()) && items.get(event.getSlot()).getClickConsumer() != null) {
                      items.get(event.getSlot()).getClickConsumer().accept((Player) event.getWhoClicked());
                  }
              }

              @EventHandler
              public void open(InventoryOpenEvent event) {
                  if (event.getInventory().equals(inventory)) {
                      openUuids.add(event.getPlayer().getUniqueId().toString());
                  }
              }

              @EventHandler
              public void close(InventoryCloseEvent event) {
                  Player player = (Player) event.getPlayer();
                  if (!event.getInventory().equals(inventory) || destroy || !openUuids.remove(player.getUniqueId().toString()))
                      return;
                  if (closeConsumer != null)
                      closeConsumer.accept(player);
                  if (destroyOnClose)
                      destroy();
              }
          };

          @Override
          public void registerListener() {
              plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
          }

          @Override
          public void update() {
              for (int i = 0; i < inventory.getContents().length; i++) {
                  inventory.setItem(i, this.items.containsKey(i) ? this.items.get(i).getItemStack() : this.fillItem);
              }
          }

          @Override
          public void destroy() {
              this.destroy = true;
              this.openUuids.clear();
              this.items.clear();
              HandlerList.unregisterAll(this.listener);
          }

          @Override
          public void openInventory(Player player) {
              player.openInventory(inventory);
          }

          @Override
          public void removeItem(int slot) {
              this.items.remove(slot);
          }

          @Override
          public void setItem(int slot, ClickableItem item) {
              this.items.put(slot, item);
          }

          @Override
          public void setItem(int slot, ItemStack itemstack) {
              setItem(slot, new ClickableItemImpl(itemstack, null));
          }

          @Override
          public void setItem(int slot, ItemStack itemstack, Consumer<Player> clickConsumer) {
              setItem(slot, new ClickableItemImpl(itemstack, clickConsumer));
          }

          @Override
          public boolean isDestroy() {
              return this.destroy;
          }

          @Override
          public void setDestroyOnClose(boolean destroyOnClose) {
              this.destroyOnClose = destroyOnClose;
          }

          @Override
          public void setCloseConsumer(Consumer<Player> closeConsumer) {
              this.closeConsumer = closeConsumer;
          }

          @Override
          public ItemStack getFillItem() {
              return this.fillItem;
          }

          @Override
          public void setFillItem(ItemStack fillItem) {
              this.fillItem = fillItem;
          }

          class ClickableItemImpl implements ClickableItem {

              private final ItemStack itemStack;
              private Consumer<Player> clickConsumer;

              ClickableItemImpl(ItemStack itemStack, Consumer<Player> clickConsumer) {
                  this.itemStack = itemStack;
                  this.clickConsumer = clickConsumer;
              }

              @Override
              public ItemStack getItemStack() {
                  return itemStack;
              }

              @Override
              public Consumer<Player> getClickConsumer() {
                  return clickConsumer;
              }

              @Override
              public void setClickConsumer(Consumer<Player> clickConsumer) {
                  this.clickConsumer = clickConsumer;
              }
          }
      };
    }


}
