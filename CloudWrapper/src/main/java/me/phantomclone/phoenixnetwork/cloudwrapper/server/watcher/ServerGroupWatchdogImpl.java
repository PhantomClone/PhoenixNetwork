/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerState;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.ServerGroup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerGroupWatchdogImpl implements ServerGroupWatchdog, Utils {

    private final CloudLib cloudLib;
    private final ServerGroup serverGroup;

    private HashMap<Server, ServerData> serverData;

    public static ServerGroupWatchdogImpl create(CloudLib cloudLib, ServerGroup serverGroup) {
        return new ServerGroupWatchdogImpl(cloudLib, serverGroup);
    }

    private ServerGroupWatchdogImpl(CloudLib cloudLib, ServerGroup serverGroup) {
        this.cloudLib = cloudLib;
        this.serverGroup = serverGroup;
        this.serverData = new HashMap<>();
    }

    @Override
    public void registerServer(Server server) {
        this.serverData.put(server, new ServerDataImpl());
    }

    @Override
    public void unregisterServer(Server server) {
        this.serverData.remove(server);
    }

    @Override
    public boolean preparation(Server server) {
        return handle(this.serverData.get(server).handlePrepare(), server, "Tried to prepare up to 5 times under 5s!");
    }

    @Override
    public boolean start(Server server) {
        return handle(this.serverData.get(server).handleStart(), server, "Tried to start up to 5 times under 5s!");
    }

    @Override
    public boolean stop(Server server) {
        return handle(this.serverData.get(server).handleStop(), server, "Tried to stop up to 5 times under 5s!");
    }

    @Override
    public boolean delete(Server server) {
        return handle(this.serverData.get(server).handleDelete(), server, "Tried to delete up to 5 times under 5s!");
    }

    @Override
    public void changeServerState(Server server, ServerState serverState) {
        if (this.serverData.containsKey(server))
            this.serverData.get(server).changeServerState(serverState);
    }

    private boolean handle(boolean fine, Server server, String error) {
        if (fine) return true;
        log("Critical Server found <" + server.getName() + ">");
        log("|--" + error);
        if (new File(server.getFolderPath()).exists()) {
            log("Try to save Server...");
            this.serverGroup.blockServer(server);
            try {
                String name = server.getName() + "-" + UUID.randomUUID().toString();
                FileUtils.copyDirectory(new File(server.getFolderPath()), new File("./flawedServer/" +  name + "/server/"));
                ConfigImpl config = ConfigImpl.create();
                config.set("Error", error);
                ServerData data = this.serverData.get(server);
                config.set("Last Server State", server.getServerState());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-HH:mm:ss", Locale.GERMANY);
                data.getServerStateChanges().forEach((time, state) -> config.set(simpleDateFormat.format(new Date(time)), state));
                config.set("Total times prepare", data.getPreparedTimes());
                config.set("Total times start", data.getStartedTimes());
                config.set("Total times stop", data.getStoppedTimes());
                config.set("Total times delete", data.getDeleteTimes());
                config.save(new File("./flawedServer/" + name + "/info.json"));
                log("Server Saved (Id: " + name);
                server.killProcess();
                server.deleteDir(new File(server.getFolderPath()));
            } catch (IOException e) {
                log("Server could not be saved!");
                server.killProcess();
                server.deleteDir(new File(server.getFolderPath()));
            }
        }
        this.serverGroup.handleFlawedServer(server, this.cloudLib);
        return false;
    }
}
