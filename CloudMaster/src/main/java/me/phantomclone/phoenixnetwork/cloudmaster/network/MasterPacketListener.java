/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.network;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener.PacketListener;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.*;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateType;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudmaster.CloudMaster;
import me.phantomclone.phoenixnetwork.cloudmaster.server.MasterServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MasterPacketListener implements PacketListener, Utils {

    private final CloudMaster cloudMaster;

    public MasterPacketListener(CloudMaster cloudMaster) {
        this.cloudMaster = cloudMaster;
    }

    @Override
    public void onReceive(@NonNull CloudLib cloudLib, @NonNull Channel channel, @NonNull Object packet) {
        if (packet instanceof StopPacket) {
            handleStopPacket((StopPacket) packet);
        } else if (packet instanceof UpdateServerStatePacket) {
            handleUpdateServerStatePacket((UpdateServerStatePacket) packet, cloudLib);
        } else if (packet instanceof RegisterServerPacket) {
            handleRegisterServerPacket((RegisterServerPacket) packet, channel, cloudLib);
        } else if (packet instanceof UnregisterServerPacket) {
            handleUnregisterServerPacket((UnregisterServerPacket) packet, cloudLib);
        } else if (packet instanceof LobbyAddressesPacket) {
            handleLobbyAddressesPacket(channel, cloudLib);
        }
    }

    private void handleStopPacket(StopPacket packet) {
        log(packet.getReason());
        this.cloudMaster.stop();
    }

    private void handleUpdateServerStatePacket(UpdateServerStatePacket packet, CloudLib cloudLib) {
        try {
            Server server = cloudLib.getServerRegistry().getServer(packet.getServerName());
            if (server != null) {
                server.handleUpdateStateServerPacket(packet.getAction(), packet.isSuccess());
                this.cloudMaster.serverUpdate(server, packet.getAction());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegisterServerPacket(RegisterServerPacket packet, Channel channel, CloudLib cloudLib) {
        try {
            Server server = cloudLib.getServerRegistry().getServer(packet.getTemplateName() + "_" + packet.getPort());
            Template template = cloudLib.getTemplateRegistry().getTemplate(packet.getTemplateName());
            if (template == null) {
                log("Something went wrong.. Template not found " + packet.getTemplateName());
                return;
            }
            if (server == null) {
                cloudLib.getServerRegistry().registerServer(new MasterServer(template, packet.getPort(), cloudLib.getWrapperRegistry().getWrapper(template.getWrapperName()), packet.getServerState(), cloudLib));
                log("Registered Server (" + template.getName() + "_" + packet.getPort() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUnregisterServerPacket(UnregisterServerPacket packet, CloudLib cloudLib) {
        Server server = cloudLib.getServerRegistry().getServer(packet.getServerName());
        if (server == null) {
            log("Server not found lol - " + packet.getServerName());
            return;
        }
        cloudLib.getServerRegistry().unregisterServer(server);
        log("Unregistered Server (" + server.getName() + ")");
    }

    private void handleLobbyAddressesPacket(Channel channel, CloudLib cloudLib) {
        List<String> list = new ArrayList<>();
        cloudLib.getTemplateRegistry().getLoadedTemplates().stream().filter(template -> template.getTemplateType().equals(TemplateType.LOBBY)).forEach(template -> {
            String host = cloudLib.getWrapperRegistry().getHostFromWrapperName(template.getWrapperName());
            if (host != null) {
                for (int port = template.getStartPort(); port <= template.getEndPort(); port++)
                    list.add(template.getName() + ":" + host + ":" + port);
            }
        });
        cloudLib.getNettyServerRegistry().sendPacket(new LobbyAddressesPacket(list), channel);
    }

}
