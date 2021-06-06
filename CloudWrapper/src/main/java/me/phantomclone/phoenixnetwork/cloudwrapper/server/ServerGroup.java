/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.watcher.ServerGroupWatchdog;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerGroup {

    void loadServers(CloudLib cloudLib);

    void unload(CloudLib cloudLib);

    void startServer(CloudLib cloudLib, Consumer<Boolean> callback);

    void startServerGroup(CloudLib cloudLib, Consumer<Boolean> callback);

    void stopServerGroup(CloudLib cloudLib, Consumer<Boolean> callback);

    void handleServerStop(Server server, CloudLib cloudLib);

    void handleFlawedServer(Server server, CloudLib cloudLib);

    Template getTemplate();

    ServerGroupWatchdog getServerGroupWatcher();

    void blockServer(Server server);

}
