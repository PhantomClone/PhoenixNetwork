package me.phantomclone.phoenixnetwork.backendspigot.storage;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import me.phantomclone.phoenixnetwork.backendcore.storage.Storable;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageType;
import me.phantomclone.phoenixnetwork.backendspigot.BackendPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class StorageRegistryImpl implements StorageRegistry<Player> {

    private final BackendPlugin plugin;
    private final Gson gson = new Gson();

    private LoginAndOutListener listener;

    private Jedis jedis;

    //UUID, CLASSNAME, INSTANCE OF THE CLASS
    private Map<String, Map<String, Object>> objectMap;

    private Map<Class<?>, BiConsumer<Player, Map<String, Object>>> registerMap;

    private Map<String, Long> lastStore;

    public static StorageRegistryImpl create(BackendPlugin plugin) {
        return new StorageRegistryImpl(plugin);
    }

    private StorageRegistryImpl(BackendPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        this.jedis = this.plugin.getBackend().getDatabaseLib().getJedisRegistry().getJedisPool("Basics").getResource();

        this.objectMap = Maps.newHashMap();
        this.registerMap = Maps.newHashMap();
        this.lastStore = Maps.newHashMap();

        this.listener = new LoginAndOutListener();

        this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this.listener);
    }

    @Override
    public void registerStorable(Class<?> clazz, BiConsumer<Player, Map<String, Object>> defaultData) {
        try {
            clazz.getAnnotation(Storable.class);
            this.registerMap.put(clazz, defaultData);
        } catch (NullPointerException e) {
            System.out.println("Class" + clazz.getName() + " is missing Storable!");
        }
    }

    @Override
    public void unregisterStorable(Class<?> clazz) {
        this.registerMap.remove(clazz);
    }

    @Override
    public <B> B getStoreObject(UUID uuid, Class<B> clazz) {
        return (B) this.objectMap.get(uuid.toString()).get(clazz.getSimpleName());
    }

    @Override
    public void store(UUID uuid) {
        this.lastStore.put(uuid.toString(), System.currentTimeMillis());
        this.objectMap.get(uuid.toString()).forEach((className, object) -> {
            this.plugin.getServer().getPluginManager().callEvent(new DataStoreInRedisEvent(uuid, className, object));
            this.jedis.hset(uuid.toString(), className, gson.toJson(object));
        });
    }

    private Object fillObject(Class<?> clazz, Map<String, Object> fields) {
        try {
            Object object = clazz.newInstance();
            fields.forEach((s, o) -> {
                try {
                    Field field = clazz.getDeclaredField(s);
                    field.setAccessible(true);
                    field.set(object, o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class LoginAndOutListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onJoin(PlayerJoinEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            objectMap.put(uuid.toString(), Maps.newHashMap());
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                //CLASSNAME, JSONOBJECT
                Map<String, String> map = jedis.hgetAll(event.getPlayer().getUniqueId().toString());
                registerMap.forEach((clazz, defaultCon) -> {
                    if (map.containsKey(clazz.getSimpleName())) {
                        System.out.println(map.get(clazz.getSimpleName()));
                        Object object = gson.fromJson(map.get(clazz.getSimpleName()), clazz);
                        plugin.getServer().getPluginManager().callEvent(new DataLoadOutRedisEvent(uuid, clazz.getSimpleName(), object));
                        objectMap.get(uuid.toString()).put(clazz.getSimpleName(), object);
                    } else {
                        try {
                            Storable storable = clazz.getAnnotation(Storable.class);
                            if (storable.storageType().equals(StorageType.PROXYMINECRAFT) || storable.storageType().equals(StorageType.MINECRAFT)) {
                                Map<String, Object> fillMap = Maps.newHashMap();
                                defaultCon.accept(event.getPlayer(), fillMap);
                                Object o = fillObject(clazz, fillMap);
                                objectMap.get(uuid.toString()).put(clazz.getSimpleName(), o);
                            }
                        } catch (NullPointerException e) {
                            System.out.println("Class" + clazz.getName() + " is missing Storable!");
                        }
                    }
                });
            }, 20);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (!lastStore.containsKey(uuid.toString()) || (System.currentTimeMillis() - lastStore.get(uuid.toString())) > 1000) {
                store(uuid);
            }
            objectMap.remove(event.getPlayer().getUniqueId().toString());
        }

    }
}
