/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerRegistryImpl implements ServerRegistry {

    private final List<Server> servers;

    private final HashMap<String, List<Consumer<Server>>> registerCallBacks = new HashMap<>();
    private final HashMap<String, List<Consumer<Server>>> unregisterCallBacks = new HashMap<>();
    private final List<Consumer<Server>> registerServerCallBacks = new ArrayList<>();
    private final List<Consumer<Server>> unregisterServerCallBacks = new ArrayList<>();

    public static ServerRegistryImpl create() {
        return new ServerRegistryImpl();
    }

    private ServerRegistryImpl() {
        this.servers = new ArrayList<>();
    }

    @Override
    public void registerServer(@NonNull Server server) {
        Objects.requireNonNull(server);
        this.servers.add(server);
        this.registerServerCallBacks.forEach(c -> c.accept(server));
        if (this.registerCallBacks.containsKey(server.getName())) {
            this.registerCallBacks.get(server.getName()).forEach(c -> c.accept(server));
            this.registerCallBacks.get(server.getName()).clear();
        }
    }

    @Override
    public void unregisterServer(@NonNull Server server) {
        Objects.requireNonNull(server);
        this.servers.remove(server);
        this.unregisterServerCallBacks.forEach(c -> c.accept(server));
        if (this.unregisterCallBacks.containsKey(server.getName())) {
            this.unregisterCallBacks.get(server.getName()).forEach(c -> c.accept(server));
            this.unregisterCallBacks.get(server.getName()).clear();
        }
    }

    @Override
    public List<Server> getServers(@NonNull ServerType type) {
        Objects.requireNonNull(type);
        return this.servers.stream().filter(server -> server.getServerType().equals(type)).collect(Collectors.toList());
    }

    @Override
    public List<Server> getServers(@NonNull Wrapper wrapper) {
        Objects.requireNonNull(wrapper);
        return this.servers.stream().filter(server -> server.getTemplate().getWrapperName().equalsIgnoreCase(wrapper.getName())).collect(Collectors.toList());
    }

    @Override
    public Server getServer(@NonNull String name) {
        Objects.requireNonNull(name);
        return this.servers.stream().filter(server -> server.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public List<Server> getServers() {
        return new ArrayList<>(this.servers);
    }

    @Override
    public void callWhenRegistered(@NonNull String serverName, @NonNull Consumer<Server> callback) {
        Objects.requireNonNull(serverName);
        Objects.requireNonNull(callback);
        if (!this.registerCallBacks.containsKey(serverName)) this.registerCallBacks.put(serverName, new ArrayList<>());
        this.registerCallBacks.get(serverName).add(callback);
    }

    @Override
    public void callWhenUnregistered(@NonNull String serverName, @NonNull Consumer<Server> callback) {
        Objects.requireNonNull(serverName);
        Objects.requireNonNull(callback);
        if (!this.unregisterCallBacks.containsKey(serverName)) this.unregisterCallBacks.put(serverName, new ArrayList<>());
        this.unregisterCallBacks.get(serverName).add(callback);
    }

    @Override
    public void callWhenRegistered(@NonNull Consumer<Server> callBack) {
        Objects.requireNonNull(callBack);
        this.registerServerCallBacks.add(callBack);
    }

    @Override
    public void callWhenUnregistered(@NonNull Consumer<Server> callBack) {
        Objects.requireNonNull(callBack);
        this.unregisterServerCallBacks.add(callBack);
    }

    @Override
    public boolean isPortFree(int port) {
        return this.servers.stream().noneMatch(server -> server.getPort() == port);
    }
}