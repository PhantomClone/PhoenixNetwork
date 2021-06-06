/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server.template;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.TemplateActionPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;

import java.io.Serializable;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class TemplateFactoryImpl implements TemplateFactory {

    public static TemplateFactoryImpl create() { return new TemplateFactoryImpl(); }

    private TemplateFactoryImpl() {}

    @Override
    public boolean createTemplate(@NonNull CloudLib cloudLib, @NonNull String wrapperName, @NonNull String name, @NonNull TemplateType templateType, @NonNull ServerType serverType, int startPort, int endPort, int minMemory, int maxMemory, int minOnlineServers) {
        Template template = new TemplateImpl(name, wrapperName, templateType.name(), serverType.name(), startPort + "", endPort + "", minMemory + "", maxMemory + "", minOnlineServers + "");
        if (template.exist()) return false;

        template.save();
        cloudLib.getTemplateRegistry().loadTemplate(name);
        TemplateActionPacket packet = new TemplateActionPacket(TemplateActionPacket.Type.CREATE, template);
        packet.loadTemplateFolderInPacket(cloudLib);
        cloudLib.getNettyServerRegistry().sendPacket(packet, cloudLib.getWrapperRegistry().getWrapper(wrapperName).getChannel());
        return true;
    }

    private static class TemplateImpl implements Template, Serializable {

        private final String name, wrapperName, templateType, serverType, startPort, endPort, minServer, minMemory, maxMemory;

        private TemplateImpl(@NonNull String name, @NonNull String wrapperName, @NonNull String templateType, @NonNull String serverType, @NonNull String startPort, @NonNull String endPort, @NonNull String minMemory, @NonNull String maxMemory, @NonNull String minServer) {
            this.name = name;
            this.wrapperName = wrapperName;
            this.templateType = templateType;
            this.serverType = serverType;
            this.startPort = startPort;
            this.endPort = endPort;
            this.minMemory = minMemory;
            this.maxMemory = maxMemory;
            this.minServer = minServer;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getWrapperName() {
            return wrapperName;
        }

        @Override
        public TemplateType getTemplateType() {
            return TemplateType.valueOf(templateType);
        }

        @Override
        public ServerType getServerType() {
            return ServerType.valueOf(serverType);
        }

        @Override
        public int getStartPort() {
            return Integer.parseInt(startPort);
        }

        @Override
        public int getEndPort() {
            return Integer.parseInt(endPort);
        }

        @Override
        public int getMinServer() {
            return Integer.parseInt(minServer);
        }

        @Override
        public int getMinMemory() {
            return Integer.parseInt(minMemory);
        }

        @Override
        public int getMaxMemory() {
            return Integer.parseInt(maxMemory);
        }
    }
}