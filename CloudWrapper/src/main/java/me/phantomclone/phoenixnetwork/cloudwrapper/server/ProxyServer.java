/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.ServerActionPacket;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.UpdateServerStatePacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateType;
import me.phantomclone.phoenixnetwork.cloudwrapper.CloudWrapper;
import me.phantomclone.phoenixnetwork.cloudwrapper.utils.FileEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ProxyServer implements Server, FileEditor {

    private final Template template;
    private final int port;
    private final ServerGroup serverGroup;

    private ServerState serverState;

    private Process process;

    private final String folderPath;

    private Consumer<Boolean> stopCallBack;

    private boolean preparing, starting, stopping, deleting;

    public ProxyServer(Template template, int port, ServerGroup serverGroup) {
        this.template = template;
        this.port = port;
        this.serverGroup = serverGroup;
        this.folderPath = "./runningServers/" + getTemplate().getName() + "/" + getName() + "/";

        File file = new File(folderPath, "server.jar");
        this.serverState = file.exists() ? ServerState.PREPARED : ServerState.DELETED;

        this.preparing = false;
        this.starting = false;
        this.stopping = false;
        this.deleting = false;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.PROXY;
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
        return template.getName() + "_" + getPort();
    }

    @Override
    public void preparation(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (this.preparing || getServerState().equals(ServerState.STARTED) || getServerState().equals(ServerState.STOPPED) ||getServerState().equals(ServerState.PREPARED)) {
            callback.accept(false);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.PREPARED, false));
            return;
        }
        if (!this.serverGroup.getServerGroupWatcher().preparation(this)) return;
        this.preparing = true;

        File folder = new File(this.folderPath);
        if ((folder.exists() && getTemplate().getTemplateType().equals(TemplateType.DYNAMIC)) || (folder.list() != null && Arrays.stream(folder.list()).noneMatch(s -> s.equalsIgnoreCase("server.jar")))) {
            deleteDir(folder);
        }
        if (!folder.exists()) {
            try {
                folder.mkdirs();
                copy(new File(cloudLib.getTemplateRegistry().getTemplateFolder() + getTemplate().getName() + "/"), folder);
            } catch (IOException e) {
                e.printStackTrace();
                callback.accept(false);
                cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.PREPARED, false));
                return;
            }
        }
        setProperties(CloudWrapper.lobbyAddresses);
        this.preparing = false;
        this.serverState = ServerState.PREPARED;
        this.serverGroup.getServerGroupWatcher().changeServerState(this, ServerState.PREPARED);
        cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.PREPARED, true));
        callback.accept(true);
    }

    @Override
    public void start(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (this.starting || getServerState().equals(ServerState.DELETED) || getServerState().equals(ServerState.STARTED)) {
            callback.accept(false);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STOPPED, false));
            return;
        }
        if (!this.serverGroup.getServerGroupWatcher().start(this)) return;
        this.starting = true;

        cloudLib.getThreadPoolRegistry().submit(() -> {
            ProcessBuilder pb = new ProcessBuilder("Java", "-Djline.terminal=jline.UnsupportedTerminal", /*"-Xmx" + getTemplate().getMaxMemory() + "M", */"-jar", "server.jar");
            pb.directory(new File(this.folderPath));
            try {
                try {
                    this.process = pb.start();
                } catch (Exception e) {
                    callback.accept(false);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Listening on /")) {
                        this.starting = false;
                        this.serverState = ServerState.STARTED;
                        this.serverGroup.getServerGroupWatcher().changeServerState(this, ServerState.STARTED);
                        cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STARTED, true));
                        callback.accept(true);
                        break;
                    }
                }
                reader.close();
                this.process.waitFor();
            } catch (Exception e) {
                cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STARTED, false));
                e.printStackTrace();
            }
            this.process.destroy();
            this.serverState = ServerState.STOPPED;
            this.starting = false;
            this.stopping = false;
            this.serverGroup.getServerGroupWatcher().changeServerState(this, ServerState.STOPPED);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STOPPED, true));
            serverGroup.handleServerStop(this, cloudLib);
            if (this.stopCallBack != null) this.stopCallBack.accept(true);
            this.stopCallBack = null;
        });
    }

    @Override
    public void stop(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (this.stopping || !getServerState().equals(ServerState.STARTED)) {
            callback.accept(false);
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STOPPED, false));
            return;
        }
        if (!this.serverGroup.getServerGroupWatcher().stop(this)) return;
        this.stopping = true;
        this.stopCallBack = callback;
        sendCommand("end", b -> {});
    }

    @Override
    public void delete(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(cloudLib);
        Objects.requireNonNull(callback);
        if (this.deleting || getServerState().equals(ServerState.DELETED) || getServerState().equals(ServerState.STARTED)) {
            cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.STOPPED, false));
            callback.accept(false);
            return;
        }
        if (!this.serverGroup.getServerGroupWatcher().delete(this)) return;
        this.deleting = true;
        if (this.template.getTemplateType().equals(TemplateType.DYNAMIC)) {
            deleteDir(new File(this.folderPath));
        }
        this.serverState = ServerState.DELETED;
        this.deleting = false;
        this.serverGroup.getServerGroupWatcher().changeServerState(this, ServerState.DELETED);
        cloudLib.getNettyClientRegistry().getNettyClient().sendPacket(new UpdateServerStatePacket(getName(), ServerState.DELETED, true));
        callback.accept(true);
    }

    @Override
    public void sendCommand(@NonNull String line, @NonNull Consumer<Boolean> callback) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(callback);
        if (this.process != null && this.process.isAlive()) {
            try {
                this.process.getOutputStream().write((line + "\n").getBytes(StandardCharsets.UTF_8));
                this.process.getOutputStream().flush();
                callback.accept(true);
            } catch (IOException e) {
                e.printStackTrace();
                callback.accept(false);
            }
        }
    }

    @Override
    public void handleUpdateStateServerPacket(@NonNull ServerState serverState, boolean success) {
        Objects.requireNonNull(serverState);
        throw new UnsupportedOperationException("Wrapper have do this by it self!");
    }

    @Override
    public void handleServerActionPacket(@NonNull ServerActionPacket packet, @NonNull CloudLib cloudLib) {
        Objects.requireNonNull(packet);
        Objects.requireNonNull(cloudLib);
        try {
            switch (packet.getServerAction()) {
                case PREPARE:
                    preparation(cloudLib, b -> {
                    });
                    break;
                case START:
                    start(cloudLib, b -> {
                    });
                    break;
                case STOP:
                    stop(cloudLib, b -> {
                    });
                    break;
                case COMMAND:
                    sendCommand(packet.getCommandLine(), b -> {
                    });
                    break;
                case DELETE:
                    delete(cloudLib, b -> {
                    });
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void killProcess() {
        if (this.process.isAlive())
            this.process.destroy();
    }

    @Override
    public String getFolderPath() {
        return this.folderPath;
    }

    private void setProperties(List<String> lobbies) {
        String hosts = "";
        String servers = "";
        if (lobbies != null) {
            for (String s : lobbies) {
                String[] args = s.split(":");
                //1 TEMPLATE 2 HOST 3 PORT
                hosts = hosts + "\n  -  " + args[0] + "_" + args[2];
                servers = servers + "\n  " + args[0] + "_" + args[2] + ":" + "\n    motd: '&1Just another Server by GhastCloud'\n    address: " + args[1] + ":" + args[2] + "\n    restricted: false";
            }
        }
        File file = new File("./runningServers/" + getTemplate().getName() + "/" + getName() + "/config.yml");
        try {
            replaceLineWhereFind(file, "  priorities:", "  priorities:" + hosts);
            replaceWordWhereFind(file, "{PORT}", getPort() + "");
            replaceLineWhereFind(file, "servers:", "servers:\n" +  servers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
