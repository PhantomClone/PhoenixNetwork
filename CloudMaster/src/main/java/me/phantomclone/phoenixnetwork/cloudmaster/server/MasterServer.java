/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.server;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.ServerActionPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MasterServer implements Server {

    private final Template template;
    private int port;

    private ServerState serverState;
    private List<Consumer<Boolean>> startedBack, stoppedBack, preparedBack, deletedBack;

    private final Wrapper wrapper;
    private final CloudLib cloudLib;

    public MasterServer(Template template, int port, Wrapper wrapper, ServerState serverState, CloudLib cloudLib) {
        this.template = template;
        this.port = port;

        this.wrapper = wrapper;
        this.cloudLib = cloudLib;

        this.serverState = serverState;

        this.startedBack = new ArrayList<>();
        this.stoppedBack = new ArrayList<>();
        this.preparedBack = new ArrayList<>();
        this.deletedBack = new ArrayList<>();
    }

    public MasterServer(Template template, Wrapper wrapper, CloudLib cloudLib) {
        this.template = template;

        this.wrapper = wrapper;
        this.cloudLib = cloudLib;
    }

    @Override
    public ServerType getServerType() {
        return template.getServerType();
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public ServerState getServerState() {
        return serverState;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getName() {
        return this.template.getName() + "_" + this.port;
    }

    @Override
    public void preparation(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (getServerState().equals(ServerState.STARTED) || getServerState().equals(ServerState.STOPPED) || getServerState().equals(ServerState.PREPARED)) {
            callback.accept(false);
            return;
        }
        this.preparedBack.add(callback);
        cloudLib.getNettyServerRegistry().getNettyServer().sendPacket(new ServerActionPacket().setServerAction(ServerActionPacket.ServerAction.PREPARE).setServerName(getName()), this.wrapper.getChannel());
    }

    @Override
    public void start(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (getServerState().equals(ServerState.DELETED) || getServerState().equals(ServerState.STARTED)) {
            callback.accept(false);
            return;
        }
        this.startedBack.add(callback);
        cloudLib.getNettyServerRegistry().getNettyServer().sendPacket(new ServerActionPacket().setServerAction(ServerActionPacket.ServerAction.START).setServerName(getName()), this.wrapper.getChannel());
    }

    @Override
    public void stop(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (!getServerState().equals(ServerState.STARTED)) {
            callback.accept(false);
            return;
        }
        this.stoppedBack.add(callback);
        cloudLib.getNettyServerRegistry().getNettyServer().sendPacket( new ServerActionPacket().setServerAction(ServerActionPacket.ServerAction.STOP).setServerName(getName()), this.wrapper.getChannel());
    }

    @Override
    public void delete(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (getServerState().equals(ServerState.DELETED) || getServerState().equals(ServerState.STARTED)) {
            callback.accept(false);
            return;
        }
        this.deletedBack.add(callback);
        cloudLib.getNettyServerRegistry().getNettyServer().sendPacket(new ServerActionPacket().setServerAction(ServerActionPacket.ServerAction.DELETE).setServerName(getName()), this.wrapper.getChannel());
    }

    @Override
    public void sendCommand(@NonNull String line, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(callback);
        if (getServerState().equals(ServerState.STARTED)) {
            this.cloudLib.getNettyServerRegistry().getNettyServer().sendPacket(new ServerActionPacket().setServerAction(ServerActionPacket.ServerAction.COMMAND).setServerName(getName()).setCommandLine(line), this.wrapper.getChannel());
            callback.accept(true);
        } else callback.accept(false);
    }

    @Override
    public void handleUpdateStateServerPacket(@NonNull ServerState serverState, boolean success) {
        Objects.requireNonNull(serverState);
        switch (serverState) {
            case STARTED:
                this.startedBack.forEach(c -> c.accept(success));
                this.startedBack.clear();
                this.serverState = ServerState.STARTED;
                break;
            case STOPPED:
                this.stoppedBack.forEach(c -> c.accept(success));
                this.stoppedBack.clear();
                this.serverState = ServerState.STOPPED;
                break;
            case DELETED:
                this.deletedBack.forEach(c -> c.accept(success));
                this.deletedBack.clear();
                this.serverState = ServerState.DELETED;
                break;
            case PREPARED:
                this.preparedBack.forEach(c -> c.accept(success));
                this.preparedBack.clear();
                this.serverState = ServerState.PREPARED;
                break;
        }
    }

    @Override
    public void handleServerActionPacket(@NonNull ServerActionPacket packet, @NonNull CloudLib cloudLib) {
        throw new UnsupportedOperationException("This Methode is only Supported in Wrapper!");
    }

    @Override
    public void killProcess() {
        throw new UnsupportedOperationException("This Methode is only Supported in Wrapper!");
    }

    @Override
    public String getFolderPath() {
        throw new UnsupportedOperationException("This Methode is only Supported in Wrapper!");
    }
}
