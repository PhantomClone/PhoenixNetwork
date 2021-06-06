/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore;

import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKeyFactory;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKeyFactoryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKeyRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.authkey.AuthKeyRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.command.*;
import me.phantomclone.phoenixnetwork.cloudcore.console.ConsoleRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.console.ConsoleRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.master.MasterRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.master.MasterRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.network.NettyClientRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.network.NettyClientRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.network.NettyServerRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.network.NettyServerRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.*;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener.PacketListenerRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener.PacketListenerRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateFactory;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateFactoryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.thread.ThreadPoolRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.thread.ThreadPoolRegistryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.WrapperFactory;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.WrapperFactoryImpl;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.WrapperRegistry;
import me.phantomclone.phoenixnetwork.cloudcore.wrapper.WrapperRegistryImpl;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class CloudLib {

    private final PacketRegistry packetRegistry;
    private final PacketFactory packetFactory;
    private final PacketListenerRegistry packetListenerRegistry;

    private final ThreadPoolRegistry threadPoolRegistry;

    private final NettyServerRegistry nettyServerRegistry;
    private final NettyClientRegistry nettyClientRegistry;

    private final AuthKeyRegistry authKeyRegistry;
    private final AuthKeyFactory authKeyFactory;

    private final ServerRegistry serverRegistry;
    private final TemplateRegistry templateRegistry;
    private final TemplateFactory templateFactory;

    private final CommandRegistry commandRegistry;
    private final CommandFactory commandFactory;

    private final ConsoleRegistry consoleRegistry;

    private final MasterRegistry masterRegistry;

    private final WrapperRegistry wrapperRegistry;
    private final WrapperFactory wrapperFactory;

    /**
     * Create and returns a new CloudLib.
     * @return Returns a new CloudLib.
     */
    public static CloudLib createCloudLib() {
        return new CloudLib();
    }

    private CloudLib() {
        this.packetRegistry = PacketRegistryImpl.create();
        this.packetFactory = PacketFactoryImpl.create(this.packetRegistry);
        this.packetListenerRegistry = PacketListenerRegistryImpl.create();

        this.threadPoolRegistry = ThreadPoolRegistryImpl.create();

        this.nettyServerRegistry = NettyServerRegistryImpl.create();
        this.nettyClientRegistry = NettyClientRegistryImpl.create();

        this.authKeyRegistry = AuthKeyRegistryImpl.create();
        this.authKeyFactory = AuthKeyFactoryImpl.create();

        this.serverRegistry = ServerRegistryImpl.create();
        this.templateRegistry = TemplateRegistryImpl.create();
        this.templateFactory = TemplateFactoryImpl.create();

        this.commandRegistry = CommandRegistryImpl.create();
        this.commandFactory = CommandFactoryImpl.create();

        this.consoleRegistry = ConsoleRegistryImpl.create();

        this.masterRegistry = MasterRegistryImpl.create();

        this.wrapperRegistry = WrapperRegistryImpl.create();
        this.wrapperFactory = WrapperFactoryImpl.create();

        getTemplateRegistry().loadTemplates();

        registerPackets();
    }

    /**
     * Find and register all packets in the package 'net.ghastgames.ghastcloud.cloudlib.network.packet.packets'.
     * Then it sorts the packets with {@link PacketRegistry#shuffle()}.
     */
    private void registerPackets() {
        String packetListPackage = "net.ghastgames.ghastcloud.cloudlib.network.packet.packets";
        Reflections reflections = new Reflections(packetListPackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Packet.class);
        classes.forEach(aClass -> getPacketRegistry().registerPacket(aClass));
        getPacketRegistry().shuffle();
    }

    /**
     * It stops the hole {@link CloudLib}.
     */
    public void stop() {
        getConsoleRegistry().getConsoleReader().stop();
        getThreadPoolRegistry().shutdownPool();
    }

    /**
     * Returns the {@link PacketRegistry}.
     * @return Returns the {@link PacketRegistry}.
     */
    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    /**
     * Returns the {@link PacketFactory}.
     * @return Returns the {@link PacketFactory}.
     */
    public PacketFactory getPacketFactory() {
        return packetFactory;
    }

    /**
     * Returns the {@link PacketListenerRegistry}.
     * @return Returns the {@link PacketListenerRegistry}.
     */
    public PacketListenerRegistry getPacketListenerRegistry() {
        return packetListenerRegistry;
    }

    /**
     * Returns the {@link ThreadPoolRegistry}.
     * @return Returns the {@link ThreadPoolRegistry}.
     */
    public ThreadPoolRegistry getThreadPoolRegistry() {
        return threadPoolRegistry;
    }

    /**
     * Returns the {@link NettyServerRegistry}.
     * @return Returns the {@link NettyServerRegistry}.
     */
    public NettyServerRegistry getNettyServerRegistry() {
        return nettyServerRegistry;
    }

    /**
     * Returns the {@link NettyClientRegistry}.
     * @return Returns the {@link NettyClientRegistry}.
     */
    public NettyClientRegistry getNettyClientRegistry() {
        return nettyClientRegistry;
    }

    /**
     * Returns the {@link AuthKeyRegistry}.
     * @return Returns the {@link AuthKeyRegistry}.
     */
    public AuthKeyRegistry getAuthKeyRegistry() {
        return authKeyRegistry;
    }

    /**
     * Returns the {@link AuthKeyFactory}.
     * @return Returns the {@link AuthKeyFactory}.
     */
    public AuthKeyFactory getAuthKeyFactory() {
        return authKeyFactory;
    }

    /**
     * Returns the {@link ServerRegistry}.
     * @return Returns the {@link ServerRegistry}.
     */
    public ServerRegistry getServerRegistry() {
        return serverRegistry;
    }

    /**
     * Returns the {@link TemplateRegistry}.
     * @return Returns the {@link TemplateRegistry}.
     */
    public TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    /**
     * Returns the {@link TemplateFactory}.
     * @return Returns the {@link TemplateFactory}.
     */
    public TemplateFactory getTemplateFactory() {
        return templateFactory;
    }

    /**
     * Returns the {@link CommandRegistry}.
     * @return Returns the {@link CommandRegistry}.
     */
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    /**
     * Returns the {@link CommandFactory}.
     * @return Returns the {@link CommandFactory}.
     */
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    /**
     * Returns the {@link ConsoleRegistry}.
     * @return Returns the {@link ConsoleRegistry}.
     */
    public ConsoleRegistry getConsoleRegistry() {
        return consoleRegistry;
    }

    /**
     * Returns the {@link MasterRegistry}.
     * @return Returns the {@link MasterRegistry}.
     */
    public MasterRegistry getMasterRegistry() {
        return masterRegistry;
    }

    /**
     * Returns the {@link WrapperRegistry}.
     * @return Returns the {@link WrapperRegistry}.
     */
    public WrapperRegistry getWrapperRegistry() {
        return wrapperRegistry;
    }

    /**
     * Returns the {@link WrapperFactory}.
     * @return Returns the {@link WrapperFactory}.
     */
    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }
}
