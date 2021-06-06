/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface ServerGroupRegistry {

    void load(CloudLib cloudLib);
    ServerGroup load(Template template, CloudLib cloudLib);
    ServerGroup load (String templateName, CloudLib cloudLib);

    void startAllServerGroups(CloudLib cloudLib);
    void startServerGroup(ServerGroup serverGroup, CloudLib cloudLib, Consumer<Boolean> b);

    void stopAndUnload(CloudLib cloudLib, String templateName, Consumer<Boolean> done);
    void stopAndUnload(CloudLib cloudLib, Consumer<Boolean> done);

    ServerGroup getServerGroup(String templateName);

}
