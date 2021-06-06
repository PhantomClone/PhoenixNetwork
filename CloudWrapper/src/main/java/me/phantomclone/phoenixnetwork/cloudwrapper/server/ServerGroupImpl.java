/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.RegisterServerPacket;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.UnregisterServerPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudwrapper.CloudWrapper;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher.ServerGroupWatchdog;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher.ServerGroupWatchdogImpl;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerGroupImpl implements ServerGroup, Utils {

    private final Template template;

    private final ServerGroupWatchdog serverGroupWatchdog;

    private final HashMap<Server, Boolean> loadedServers;

    private final List<Consumer<Boolean>> stopCallBacks;

    private final List<String> blockedServerPorts;

    private int flawedTimes = 0;

    private boolean stopped = false;

    public ServerGroupImpl(Template template, CloudLib cloudLib) {
        this.template = template;
        this.serverGroupWatchdog = ServerGroupWatchdogImpl.create(cloudLib, this);
        this.loadedServers = new LinkedHashMap<>();
        this.stopCallBacks = new ArrayList<>();
        this.blockedServerPorts = new ArrayList<>();
    }

    @Override
    public void loadServers(CloudLib cloudLib) {
        File folder = new File("./runningServers/" + this.template.getName() + "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (folder.listFiles() == null) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (file.getName().contains(this.template.getName() + "_")) {
                if (file.listFiles() != null && Arrays.stream(file.listFiles()).anyMatch(f -> f.getName().equalsIgnoreCase("server.jar"))) {
                    int port = Integer.parseInt(file.getName().replace(this.template.getName() + "_", ""));
                    Server server = this.template.getServerType().equals(ServerType.MINECRAFT) ? new MinecraftServer(template, port, this) : new ProxyServer(template, port, this);
                    this.loadedServers.put(server, false);
                    this.serverGroupWatchdog.registerServer(server);
                    cloudLib.getServerRegistry().registerServer(server);
                    cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new RegisterServerPacket(server.getTemplate().getName(), port).setServerState(server.getServerState()));
                }
            }
        }
    }

    @Override
    public void unload(CloudLib cloudLib) {
        this.loadedServers.keySet().forEach(server -> {
            getServerGroupWatcher().unregisterServer(server);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UnregisterServerPacket(server.getName()));
            cloudLib.getServerRegistry().unregisterServer(server);
        });
        this.loadedServers.clear();
    }

    @Override
    public void startServer(CloudLib cloudLib, Consumer<Boolean> callback) {
        for (Map.Entry<Server, Boolean> set : this.loadedServers.entrySet()) {
            if (!set.getValue() && !this.blockedServerPorts.contains(set.getKey().getPort() + "")) {
                if (set.getKey().getServerState().equals(ServerState.DELETED)) {
                    set.getKey().preparation(cloudLib, b -> {
                        if (b) cloudLib.getThreadPoolRegistry().submit(() -> set.getKey().start(cloudLib, callback));
                        else log("Servergroup failed preparing a Server - lol");
                        this.loadedServers.replace(set.getKey(), true);
                    });
                } else if (set.getKey().getServerState().equals(ServerState.PREPARED) || set.getKey().getServerState().equals(ServerState.STOPPED)) {
                    cloudLib.getThreadPoolRegistry().submit(() -> set.getKey().start(cloudLib, callback));
                    this.loadedServers.replace(set.getKey(), true);
                }
                return;
            }
        }
        int port = getFreePort(cloudLib);
        if (port == -1) {
            callback.accept(false);
            return;
        }
        Server server = this.template.getServerType().equals(ServerType.MINECRAFT) ? new MinecraftServer(this.template, port, this) : new ProxyServer(this.template, port, this);
        this.loadedServers.put(server, true);
        this.serverGroupWatchdog.registerServer(server);
        cloudLib.getServerRegistry().registerServer(server);
        cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new RegisterServerPacket(server.getTemplate().getName(), port).setServerState(server.getServerState()));
        if (server.getServerState().equals(ServerState.DELETED)) {
            server.preparation(cloudLib, b -> {
                if (b) cloudLib.getThreadPoolRegistry().submit(() -> server.start(cloudLib, callback));
            });
        } else {
            cloudLib.getThreadPoolRegistry().submit(() -> server.start(cloudLib, callback));
        }
    }

    @Override
    public void startServerGroup(CloudLib cloudLib, Consumer<Boolean> callback) {
        int i = (int) this.loadedServers.entrySet().stream().filter(Map.Entry::getValue).count();
        AtomicInteger atomicInteger = new AtomicInteger(getTemplate().getMinServer() - i);
        if (atomicInteger.get() <= 0) {
            callback.accept(true);
            return;
        }
        for (; i < this.template.getMinServer(); i++) {
            startServer(cloudLib, b -> {
                if (atomicInteger.decrementAndGet() <= 0) {
                    callback.accept(true);
                }
            });
        }
        if ((int) this.loadedServers.entrySet().stream().filter(Map.Entry::getValue).count() < template.getMinServer()) {
            startServer(cloudLib, b -> startServerGroup(cloudLib, callback));
        } else {
            callback.accept(true);
        }
    }

    @Override
    public void stopServerGroup(CloudLib cloudLib, Consumer<Boolean> callback) {
        stopped = true;
        this.stopCallBacks.add(callback);
        new ArrayList<>(this.loadedServers.keySet()).forEach(server -> {
            if (server.getServerState().equals(ServerState.STARTED)) {
                server.stop(cloudLib, b -> server.delete(cloudLib, callback));
            } else if (server.getServerState().equals(ServerState.PREPARED) || server.getServerState().equals(ServerState.STOPPED)) {
                server.delete(cloudLib, callback);
            }
            this.serverGroupWatchdog.unregisterServer(server);
            this.loadedServers.remove(server);
            cloudLib.getServerRegistry().unregisterServer(server);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UnregisterServerPacket(server.getName()));
        });
        this.loadedServers.clear();
    }

    @Override
    public void handleServerStop(Server server, CloudLib cloudLib) {
        this.loadedServers.replace(server, false);
        if (this.stopped) {
            if (this.loadedServers.entrySet().stream().noneMatch(Map.Entry::getValue)) {
                this.stopCallBacks.forEach(c -> c.accept(true));
                this.stopCallBacks.clear();
            }
            return;
        }
        server.delete(cloudLib, b -> cloudLib.getThreadPoolRegistry().submit(() -> startServerGroup(cloudLib, a -> {})));
    }

    @Override
    public void handleFlawedServer(Server server, CloudLib cloudLib) {
        this.loadedServers.remove(server);
        cloudLib.getServerRegistry().unregisterServer(server);
        cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UnregisterServerPacket(server.getName()));

        this.blockedServerPorts.remove(server.getPort() + "");

        if (++this.flawedTimes > 2) {
            log("Template <" + this.template.getName() + "> has over 3 faulty servers!");
            log("ServerGroup will shutdown and unload it self. It do not start again by it self!");
            CloudWrapper.getInstance().getServerGroupRegistry().stopAndUnload(cloudLib, server.getTemplate().getName(), b -> log("ServerGroup<" + getTemplate().getName() + ">" + (b ? "unloaded!" : "Could not be unloaded! Huge ERROR!")));
        }
    }

    @Override
    public void blockServer(Server server) {
        if (this.blockedServerPorts.contains(server.getPort() + ""))
            this.blockedServerPorts.remove(server.getPort() + "");
        else
            this.blockedServerPorts.add(server.getPort() + "");
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public ServerGroupWatchdog getServerGroupWatcher() {
        return serverGroupWatchdog;
    }

    private int getFreePort(CloudLib cloudLib) {
        for (int i = this.template.getStartPort(); i < this.template.getEndPort() + 1; i++) {
            if (!this.blockedServerPorts.contains(i) && cloudLib.getServerRegistry().isPortFree(i)) return i;
        }
        return -1;
    }
}
