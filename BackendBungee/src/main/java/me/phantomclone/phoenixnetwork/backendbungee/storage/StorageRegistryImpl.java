/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendbungee.storage;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import me.phantomclone.phoenixnetwork.backendbungee.BackendPlugin;
import me.phantomclone.phoenixnetwork.backendcore.config.Config;
import me.phantomclone.phoenixnetwork.backendcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.backendcore.database.mongodb.Client;
import me.phantomclone.phoenixnetwork.backendcore.storage.Storable;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class StorageRegistryImpl implements StorageRegistry<ProxiedPlayer> {

    private final BackendPlugin plugin;
    private final Gson gson = new Gson();

    private Listener eventListener;

    private MongoCollection<Document> collection;
    private Jedis jedis;

    //UUID, CLASSNAME, INSTANCE OF THE CLASS
    private Map<String, Map<String, Object>> objectMap;

    private Map<Class<?>, BiConsumer<ProxiedPlayer, Map<String, Object>>> registerMap;

    private List<String> blockedUUIDs;

    public static StorageRegistryImpl create(BackendPlugin plugin) {
        return new StorageRegistryImpl(plugin);
    }

    private StorageRegistryImpl(BackendPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        Client client = plugin.getBackend().getDatabaseLib().getMongoDBRegistry().getMongoClient("Basics");
        this.collection = client.getMongoDatabase().getCollection("PlayerData");
        JedisPool pool = plugin.getBackend().getDatabaseLib().getJedisRegistry().getJedisPool("Basics");
        this.jedis = pool.getResource();

        this.objectMap = new HashMap<>();
        this.registerMap = new HashMap<>();

        this.blockedUUIDs = new ArrayList<>();

        this.eventListener = new LoginAndOutListener();

        this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this.eventListener);
    }

    @Override
    public void stop() {
        this.plugin.getProxy().getPluginManager().unregisterListener(this.eventListener);
    }

    @Override
    public void registerStorable(Class<?> clazz, BiConsumer<ProxiedPlayer, Map<String, Object>> defaultData) {
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
    public <T> T getStoreObject(UUID uuid, Class<T> clazz) {
        return (T) this.objectMap.get(uuid.toString()).get(clazz.getSimpleName());
    }

    @Override
    public void store(UUID uuid) {
        Map<String, String> redisMap = jedis.hgetAll(uuid.toString());
        if (redisMap == null || !objectMap.containsKey(uuid.toString())) {
            //Data did not loaded correctly. Kicked Player and do not allow him to join again.
            return;
        }
        objectMap.get(uuid.toString()).forEach((clazzName, object) -> {
            plugin.getProxy().getPluginManager().callEvent(new DataStoreInDBEvent(uuid, clazzName, object));
            redisMap.put(clazzName, gson.toJson(object));
        });
        Document document = new Document();
        redisMap.forEach(document::append);

        Publisher<UpdateResult> publisher = collection.replaceOne(Filters.eq("_id", uuid.toString()), document);
        publisher.subscribe(new Subscriber<UpdateResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(UpdateResult updateResult) {
            }

            @Override
            public void onError(Throwable throwable) {
                Config config = ConfigImpl.create();
                config.set("Error", throwable.getMessage());
                config.set("uuid", uuid.toString());
                config.set("data", gson.toJson(redisMap));
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                config.set("Stacktrace", stringWriter.toString());
                File saveFile = new File("./plugins/Backend/SaveDataFail/", uuid.toString() + "--" + (new Random().nextInt(100)));
                saveFile.getParentFile().mkdirs();
                try {
                    config.save(saveFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onComplete() {
                blockedUUIDs.remove(uuid.toString());
            }
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

    public void removeBlockedUUID(String uuid) {
        this.blockedUUIDs.remove(uuid);
    }

    //I hate bungeecord...
    public class LoginAndOutListener implements Listener {

        private LinkedHashMap<String, String> serverQuit = new LinkedHashMap<>();

        //UUID, PacketHandler
        private Map<String, Object> packedHandlerCache = new HashMap<>();

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onEarlyJoin(LoginEvent event) {
            if (event.getConnection().getUniqueId() == null) {
                event.setCancelled(true);
                return;
            }
            if (blockedUUIDs.contains(event.getConnection().getUniqueId().toString())) {
                event.setCancelled(true);
            } else {
                blockedUUIDs.add(event.getConnection().getUniqueId().toString());
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onJoin(PostLoginEvent event) {
            try {
                UUID uuid = event.getPlayer().getUniqueId();

                FindPublisher<Document> publisher = collection.find(Filters.eq("_id", uuid.toString()));
                publisher.subscribe(new Subscriber<Document>() {
                    private Document document;
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Document document) {
                        this.document = document;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        event.getPlayer().disconnect(new TextComponent("Something went wrong with your data" +
                                "\nPlease contact an administrator."));
                        Config config = ConfigImpl.create();
                        config.set("Message", throwable.getMessage());
                        StringWriter stringWriter = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(stringWriter);
                        throwable.printStackTrace(printWriter);
                        config.set("Stacktrace", stringWriter.toString());
                        try {
                            config.save(new File("./Backend/LoadDataFail-1/", uuid.toString() + "--" + (new Random().nextInt(100))));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {
                        objectMap.put(uuid.toString(), Maps.newHashMap());

                        if (document == null) {
                            Publisher<InsertOneResult> publisher = collection.insertOne(new Document("_id", uuid.toString()));
                            publisher.subscribe(new Subscriber<InsertOneResult>() {
                                @Override
                                public void onSubscribe(Subscription s) {
                                    s.request(1);
                                }

                                @Override
                                public void onNext(InsertOneResult insertOneResult) { }

                                @Override
                                public void onError(Throwable throwable) {
                                    event.getPlayer().disconnect(new TextComponent("Something went wrong with your data" +
                                            "\nPlease contact an administrator."));
                                    Config config = ConfigImpl.create();
                                    config.set("Message", throwable.getMessage());
                                    StringWriter stringWriter = new StringWriter();
                                    PrintWriter printWriter = new PrintWriter(stringWriter);
                                    throwable.printStackTrace(printWriter);
                                    config.set("Stacktrace", stringWriter.toString());
                                    try {
                                        config.save(new File("./Backend/LoadDataFail-2/", uuid.toString() + "--" + (new Random().nextInt(100))));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onComplete() {}
                            });
                        }

                        Map<String, String> inJedis = Maps.newHashMap();
                        if (document != null) {
                            //GET ALL DATA FROM MONGODB
                            document.forEach((clazzName, json) -> {
                                AtomicBoolean found = new AtomicBoolean(false);
                                registerMap.keySet().forEach(clazz -> {
                                    if (clazz.getSimpleName().equalsIgnoreCase(clazzName)) {
                                        try {
                                            Storable storable = clazz.getAnnotation(Storable.class);
                                            Object object = gson.fromJson(json.toString(), clazz);
                                            switch (storable.storageType()) {
                                                case PROXY:
                                                    plugin.getProxy().getPluginManager().callEvent(new DataLoadOutDBEvent(uuid, clazz.getSimpleName(), object));
                                                    objectMap.get(uuid.toString()).put(clazz.getSimpleName(), object);
                                                    break;
                                                case MINECRAFT:
                                                    inJedis.put(clazzName, json.toString());
                                                    break;
                                                case PROXYMINECRAFT:
                                                    plugin.getProxy().getPluginManager().callEvent(new DataLoadOutDBEvent(uuid, clazz.getSimpleName(), object));
                                                    objectMap.get(uuid.toString()).put(clazz.getSimpleName(), object);
                                                    inJedis.put(clazzName, json.toString());
                                                    break;
                                            }
                                            found.set(true);
                                        } catch (NullPointerException e) {
                                            System.out.println("Class" + clazz.getName() + " is missing Storable!");
                                        }
                                    }
                                });
                                if (!found.get()) {
                                    inJedis.put(clazzName, json.toString());
                                }
                            });
                            //GET ALL DATA FROM REGISTRY WHEN NOT IN DOCUMENT
                            registerMap.entrySet().stream().filter(set -> !document.containsKey(set.getKey().getSimpleName())).forEach(set -> {
                                try {
                                    Storable storable = set.getKey().getAnnotation(Storable.class);
                                    Map<String, Object> map = Maps.newHashMap();
                                    set.getValue().accept(event.getPlayer(), map);
                                    Object o = fillObject(set.getKey(), map);
                                    if (o != null) {
                                        switch (storable.storageType()) {
                                            case PROXY:
                                                objectMap.get(uuid.toString()).put(set.getKey().getSimpleName(), o);
                                                break;
                                            case MINECRAFT:
                                                inJedis.put(set.getKey().getSimpleName(), gson.toJson(o));
                                                break;
                                            case PROXYMINECRAFT:
                                                objectMap.get(uuid.toString()).put(set.getKey().getSimpleName(), o);
                                                inJedis.put(set.getKey().getSimpleName(), gson.toJson(o));
                                                break;
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    System.out.println("Class" + set.getKey().getName() + " is missing Storable!");
                                }
                            });
                        } else {
                            registerMap.forEach((clazz, biConsumer) -> {
                                try {
                                    Storable storable = clazz.getAnnotation(Storable.class);
                                    Map<String, Object> map = Maps.newHashMap();
                                    biConsumer.accept(event.getPlayer(), map);
                                    Object o = fillObject(clazz, map);
                                    if (o != null) {
                                        switch (storable.storageType()) {
                                            case PROXY:
                                                objectMap.get(uuid.toString()).put(clazz.getSimpleName(), o);
                                                break;
                                            case MINECRAFT:
                                                inJedis.put(clazz.getSimpleName(), gson.toJson(o));
                                                break;
                                            case PROXYMINECRAFT:
                                                objectMap.get(uuid.toString()).put(clazz.getSimpleName(), o);
                                                inJedis.put(clazz.getSimpleName(), gson.toJson(o));
                                                break;
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    System.out.println("Class" + clazz.getName() + " is missing Storable!");
                                }
                            });
                        }
                        inJedis.forEach((clazzName, json) -> jedis.hset(uuid.toString(), clazzName, json));
                    }
                });
            }catch (Exception e) { e.printStackTrace(); }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onQuit(PlayerDisconnectEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();

            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                    store(uuid);
                    objectMap.remove(uuid.toString());
                    jedis.del(uuid.toString());
                });
            }, 1, TimeUnit.SECONDS);
        }
    }
}
