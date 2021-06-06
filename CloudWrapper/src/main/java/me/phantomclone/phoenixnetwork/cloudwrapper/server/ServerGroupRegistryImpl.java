/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.server;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerGroupRegistryImpl implements ServerGroupRegistry, Utils {

    private final List<ServerGroup> serverGroups = new ArrayList<>();

    public static ServerGroupRegistryImpl create() { return new ServerGroupRegistryImpl(); }

    private ServerGroupRegistryImpl() {}

    @Override
    public void load(CloudLib cloudLib) {
        cloudLib.getTemplateRegistry().getLoadedTemplates().forEach(template -> load(template, cloudLib));
    }

    @Override
    public ServerGroup load(Template template, CloudLib cloudLib) {
        ServerGroup sg = getServerGroup(template.getName());
        if (sg != null) {
            return sg;
        }
        if (!(new File(cloudLib.getTemplateRegistry().getTemplateFolder() + template.getName() + "/").exists())) return null;
        ServerGroup serverGroup = new ServerGroupImpl(template, cloudLib);
        serverGroup.loadServers(cloudLib);
        this.serverGroups.add(serverGroup);
        return serverGroup;
    }

    @Override
    public ServerGroup load(String templateName, CloudLib cloudLib) {
        Template template = cloudLib.getTemplateRegistry().getTemplate(templateName);
        if (template == null) {
            log("Template - " + templateName + ", wurde noch nicht geladen! (ServerGroupRegistry)");
            return null;
        }
        return load(template, cloudLib);
    }

    @Override
    public void startAllServerGroups(CloudLib cloudLib) {
        this.serverGroups.forEach(serverGroup -> serverGroup.startServerGroup(cloudLib, b -> {}));
    }

    @Override
    public void startServerGroup(ServerGroup serverGroup, CloudLib cloudLib, Consumer<Boolean> b) {
        serverGroup.startServerGroup(cloudLib, b);
    }

    @Override
    public void stopAndUnload(CloudLib cloudLib, String templateName, Consumer<Boolean> done) {
        ServerGroup serverGroup = getServerGroup(templateName);
        if (serverGroup == null) {
            done.accept(false);
            return;
        }
        serverGroup.stopServerGroup(cloudLib, b -> {
            serverGroup.unload(cloudLib);
            this.serverGroups.remove(serverGroup);
            done.accept(true);
        });
    }

    @Override
    public void stopAndUnload(CloudLib cloudLib, Consumer<Boolean> done) {
        AtomicInteger i = new AtomicInteger(serverGroups.size());
        if (i.get() == 0) {
            done.accept(true);
            return;
        }
        this.serverGroups.forEach(serverGroup -> serverGroup.stopServerGroup(cloudLib, b -> {
            serverGroup.unload(cloudLib);
            if (i.decrementAndGet() == 1) {
                done.accept(true);
            }
        }));
        this.serverGroups.clear();
    }

    @Override
    public ServerGroup getServerGroup(String templateName) {
        return serverGroups.stream().filter(serverGroup -> serverGroup.getTemplate().getName().equalsIgnoreCase(templateName)).findFirst().orElse(null);
    }
}
