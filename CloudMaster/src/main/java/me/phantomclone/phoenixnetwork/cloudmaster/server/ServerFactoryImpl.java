/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.server;

import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.RegisterServerPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.Wrapper;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class ServerFactoryImpl implements ServerFactory, Utils {

    public static ServerFactoryImpl create() { return new ServerFactoryImpl(); }

    private ServerFactoryImpl() {}

    @Override
    public void createServer(Template template, CloudLib cloudLib, Consumer<Server> callback) {
        int port = getFreePort(template.getStartPort(), template.getEndPort(), cloudLib);
        if (port == -1) {
            callback.accept(null);
            return;
        }
        Wrapper wrapper = cloudLib.getWrapperRegistry().getWrapper(template.getWrapperName());
        if (wrapper == null) {
            log("Wrapper is not Registered! (" + template.getWrapperName() + ")");
            callback.accept(null);
            return;
        }
        cloudLib.getNettyServerRegistry().getNettyServer().sendPacket(new RegisterServerPacket(template.getName(), port), wrapper.getChannel());
        cloudLib.getServerRegistry().callWhenRegistered(template.getName() + "_" + port, callback);
    }

    private int getFreePort(int portStart, int portEnd, CloudLib cloudLib) {
        for (int i = portStart; i < portEnd + 1; i++) {
            if (cloudLib.getServerRegistry().isPortFree(i)) return i;
        }
        return -1;
    }

}
