package me.phantomclone.phoenixnetwork.backendspigot.storage;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
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
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    private Map<String, Consumer<Boolean>> loadOfflineDataCallBack;

    public static StorageRegistryImpl create(BackendPlugin plugin) {
        return new StorageRegistryImpl(plugin);
    }

    private StorageRegistryImpl(BackendPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        JedisPool pool = this.plugin.getBackend().getDatabaseLib().getJedisRegistry().getJedisPool("Basics");
        this.jedis = pool.getResource();

        this.objectMap = Maps.newHashMap();
        this.registerMap = Maps.newHashMap();
        this.loadOfflineDataCallBack = Maps.newHashMap();

        this.listener = new LoginAndOutListener();

        this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (Jedis subRedis = pool.getResource()) {
                subRedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equalsIgnoreCase("BackendUpdateData")) {
                            UUID uuid = UUID.fromString(message.split("/")[0]);
                            String className = message.replace(uuid.toString() + "/", "");
                            Map<String, Object> map = objectMap.get(uuid.toString());
                            if (map != null) {
                                Object object = map.get(className);
                                if (object != null) {
                                    String json = jedis.hget(uuid.toString(), className);
                                    Object toParseObject = gson.fromJson(json, object.getClass());
                                    for (Field field : object.getClass().getDeclaredFields()) {
                                        try {
                                            field.setAccessible(true);
                                            Object toSetObject = field.get(toParseObject);
                                            if (toSetObject != null)
                                                field.set(object, toSetObject);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } else if (channel.equalsIgnoreCase("LoadedOfflinePlayer")) {
                            UUID uuid = UUID.fromString(message.split("/")[0]);
                            Consumer<Boolean> consumer = loadOfflineDataCallBack.get(uuid.toString());
                            if (consumer == null) return;
                            String result = message.replace(uuid.toString() + "/", "");
                            if (result.equalsIgnoreCase("Failed")) {
                                consumer.accept(false);
                            } else {
                                consumer.accept(true);
                            }
                        }
                    }
                }, "BackendUpdateData", "LoadedOfflinePlayer");
            }
        });
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this.listener);
    }

    @Override
    public void registerStorable(Class<?> clazz, BiConsumer<Player, Map<String, Object>> defaultData) {
        this.registerMap.put(clazz, defaultData);
    }

    @Override
    public void unregisterStorable(Class<?> clazz) {
        this.registerMap.remove(clazz);
    }

    @Override
    public <B> B getStoreObject(UUID uuid, Class<B> clazz) {
        return (B) this.objectMap.get(uuid.toString()).get(clazz.getSimpleName());
    }

    public void store(UUID uuid) {
        this.objectMap.get(uuid.toString()).forEach((className, object) -> {
            this.plugin.getServer().getPluginManager().callEvent(new DataStoreInRedisEvent(uuid, className, object));
            this.jedis.hset(uuid.toString(), className, gson.toJson(object));
        });
    }

    @Override
    public <B> void getOfflineObject(Class<B> clazz, UUID uuid, Consumer<B> consumer) {
        if (this.jedis.hexists(uuid.toString(), clazz.getSimpleName())) {
            String json = this.jedis.hget(uuid.toString(), clazz.getSimpleName());
            consumer.accept(this.gson.fromJson(json, clazz));
        } else if (this.jedis.hexists("offline." + uuid.toString(), clazz.getSimpleName())){
            String json = this.jedis.hget("offline." + uuid.toString(), clazz.getSimpleName());
            consumer.accept(this.gson.fromJson(json, clazz));
        } else {
            this.loadOfflineDataCallBack.put(uuid.toString(), b -> {
                if (b) {
                    getOfflineObject(clazz, uuid, consumer);
                } else {
                    consumer.accept(null);
                }
            });
            this.jedis.publish("LoadOfflinePlayer", uuid.toString());
        }
    }

    @Override
    public void storeInRedis(UUID uuid, Object object) {
        this.plugin.getServer().getPluginManager().callEvent(new DataStoreInRedisEvent(uuid, object.getClass().getSimpleName(), object));
        this.jedis.hset(uuid.toString(), object.getClass().getSimpleName(), this.gson.toJson(object));
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
                            Map<String, Object> fillMap = Maps.newHashMap();
                            defaultCon.accept(event.getPlayer(), fillMap);
                            Object o = fillObject(clazz, fillMap);
                            objectMap.get(uuid.toString()).put(clazz.getSimpleName(), o);
                        } catch (NullPointerException e) {
                            System.out.println("Class" + clazz.getName() + " is missing Storable!");
                        }
                    }
                });
            }, 20);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();store(uuid);
            objectMap.remove(event.getPlayer().getUniqueId().toString());
        }

    }
}
