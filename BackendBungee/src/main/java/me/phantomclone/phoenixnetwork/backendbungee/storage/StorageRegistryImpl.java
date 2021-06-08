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
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
            try (Jedis subRedis = pool.getResource()) {
                jedis.configSet("notify-keyspace-events", "Ex");
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
                        } else if (channel.equalsIgnoreCase("LoadOfflinePlayer")) {
                            UUID uuid = UUID.fromString(message);
                            loadDataOutOfMongoDB(uuid, null, document -> {
                                document.forEach((className, json) -> jedis.hset("offline." + uuid.toString(), className, json.toString()));
                                document.forEach((className, json) -> jedis.hset("toSave." + uuid.toString(), className, json.toString()));
                                jedis.publish("LoadedOfflinePlayer", uuid.toString() + "/Loaded");
                            }, throwable -> jedis.publish("LoadedOfflinePlayer", uuid.toString() + "/Failed"));
                        } else if (channel.equalsIgnoreCase("__keyevent@0__:expired")) {
                            Map<String, String> redisMap = jedis.hgetAll("toSave." + message);
                            jedis.del("toSave." + message);
                            Document document = new Document();
                            redisMap.forEach(document::append);

                            Publisher<UpdateResult> publisher = collection.replaceOne(Filters.eq("_id", message), document);
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
                                    config.set("uuid", message);
                                    config.set("data", gson.toJson(redisMap));
                                    StringWriter stringWriter = new StringWriter();
                                    PrintWriter printWriter = new PrintWriter(stringWriter);
                                    throwable.printStackTrace(printWriter);
                                    config.set("Stacktrace", stringWriter.toString());
                                    File saveFile = new File("./plugins/Backend/SaveDataFail/", message + "--" + (new Random().nextInt(100)));
                                    saveFile.getParentFile().mkdirs();
                                    try {
                                        config.save(saveFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onComplete() { }
                            });
                        }
                    }
                }, "BackendUpdateData", "LoadOfflinePlayer", "__keyevent@0__:expired");
            }
        });
    }

    @Override
    public void stop() {
        this.plugin.getProxy().getPluginManager().unregisterListener(this.eventListener);
    }

    @Override
    public void registerStorable(Class<?> clazz, BiConsumer<ProxiedPlayer, Map<String, Object>> defaultData) {
        this.registerMap.put(clazz, defaultData);
    }

    @Override
    public void unregisterStorable(Class<?> clazz) {
        this.registerMap.remove(clazz);
    }

    @Override
    public <T> T getStoreObject(UUID uuid, Class<T> clazz) {
        return (T) this.objectMap.get(uuid.toString()).get(clazz.getSimpleName());
    }

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

    @Override
    public <B> void getOfflineObject(Class<B> clazz, UUID uuid, Consumer<B> consumer) {
        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
           if (this.jedis.exists(uuid.toString())) {
               String json = this.jedis.hget(uuid.toString(), clazz.getSimpleName());
               consumer.accept(this.gson.fromJson(json, clazz));
           } else if (this.jedis.exists("offline." + uuid.toString())) {
               //It should not happened, that offline has not the json loaded
               String json = this.jedis.hget("offline." + uuid.toString(), clazz.getSimpleName());
               jedis.expire("offline." + uuid.toString(), 60 * 5);
               consumer.accept(this.gson.fromJson(json, clazz));
           } else {
               loadDataOutOfMongoDB(uuid, null, document -> {
                   if (document == null) {
                       consumer.accept(null);
                       return;
                   }
                   document.forEach((clazzName, json) -> {
                       jedis.hset("offline." + uuid.toString(), clazzName, json.toString());
                       jedis.hset("toSave." + uuid.toString(), clazzName, json.toString());
                   });
                   jedis.expire("offline." + uuid.toString(), 60 * 5);
                   consumer.accept(!document.containsKey(clazz.getSimpleName()) ? null : gson.fromJson(document.get(clazz.getSimpleName()).toString(), clazz));
               }, throwable -> consumer.accept(null));
           }
        });
    }

    @Override
    public void storeInRedis(UUID uuid, Object object) {
        plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
            if (this.jedis.exists(uuid.toString())) {
                this.jedis.hset(uuid.toString(), object.getClass().getSimpleName(), this.gson.toJson(object));
                this.jedis.publish("BackendUpdateData",  uuid.toString() + "/" + object.getClass().getSimpleName());
            } else {
                this.jedis.hset("offline" + uuid.toString(), object.getClass().getSimpleName(), this.gson.toJson(object));
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

    //If player != it fills the missing DocumentData with registiered Map data.
    //If player != null he will be added in db
    private void loadDataOutOfMongoDB(UUID uuid, ProxiedPlayer player, Consumer<Document> documentConsumer, Consumer<Throwable> error) {
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
            public void onError(Throwable t) {
                error.accept(t);
            }

            @Override
            public void onComplete() {
                if (document == null && player != null) {
                    Publisher<InsertOneResult> publisher = collection.insertOne(document = new Document("_id", uuid.toString()));
                    publisher.subscribe(new Subscriber<InsertOneResult>() {
                                            @Override
                                            public void onSubscribe(Subscription s) {
                                                s.request(1);
                                            }

                                            @Override
                                            public void onNext(InsertOneResult insertOneResult) {
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                player.disconnect(new TextComponent("Something went wrong with your data" +
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
                                            public void onComplete() {
                                            }
                                        });
                }
                if (player != null)
                    registerMap.entrySet().stream().filter(set -> !document.containsKey(set.getKey().getSimpleName())).forEach(set -> {
                        Map<String, Object> map = Maps.newHashMap();
                        set.getValue().accept(player, map);
                        Object object = fillObject(set.getKey(), map);
                        document.put(set.getKey().getSimpleName(), gson.toJson(object));
                    });
                documentConsumer.accept(document);
            }
        });
    }

    //I hate bungeecord...
    public class LoginAndOutListener implements Listener {

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
            UUID uuid = event.getPlayer().getUniqueId();

            if (jedis.exists("offline." + uuid.toString())) {
                Map<String, String> map = jedis.hgetAll("offline." + uuid.toString());
                jedis.del("offline." + uuid.toString());
                jedis.del("toSave." + uuid.toString());
                objectMap.put(uuid.toString(), Maps.newHashMap());
                registerMap.keySet().forEach(clazz -> {
                    Object json = map.get(clazz.getSimpleName());
                    if (json != null) {
                        Object object = gson.fromJson(json.toString(), clazz);
                        objectMap.get(uuid.toString()).put(clazz.getSimpleName(), object);
                    }
                });
                map.forEach((clazzName, json) -> jedis.hset(uuid.toString(), clazzName, json));
                return;
            }

            loadDataOutOfMongoDB(uuid, event.getPlayer(), document -> {
                objectMap.put(uuid.toString(), Maps.newHashMap());
                registerMap.keySet().forEach(clazz -> {
                    Object json = document.get(clazz.getSimpleName());
                    if (json != null) {
                           Object object = gson.fromJson(json.toString(), clazz);
                           objectMap.get(uuid.toString()).put(clazz.getSimpleName(), object);
                    }
                });
                document.forEach((clazzName, json) -> jedis.hset(uuid.toString(), clazzName, json.toString()));
                }, throwable -> {
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
                });
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
