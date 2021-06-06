/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.network;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.listener.PacketListener;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.*;
import me.phantomclone.phoenixnetwork.cloudcore.server.Server;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.TemplateType;
import me.phantomclone.phoenixnetwork.cloudcore.utils.Utils;
import me.phantomclone.phoenixnetwork.cloudwrapper.CloudWrapper;
import me.phantomclone.phoenixnetwork.cloudwrapper.server.ServerGroup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class WrapperPacketListener implements PacketListener, Utils {

    private final CloudWrapper cloudWrapper;

    public WrapperPacketListener(CloudWrapper cloudWrapper) {
        this.cloudWrapper = cloudWrapper;
    }

    @Override
    public void onReceive(@NonNull CloudLib cloudLib, @NonNull Channel channel, @NonNull Object packet) {
        try {
            if (packet instanceof StopPacket) {
                handleStopPacket((StopPacket) packet);
            } else if (packet instanceof ServerActionPacket) {
                handleServerActionPacket((ServerActionPacket) packet, cloudLib);
            } else if (packet instanceof RegisterServerPacket) {
                handleRegisterServerPacket((RegisterServerPacket) packet, cloudLib);
            } else if (packet instanceof LobbyAddressesPacket) {
                handleLobbyAddressesPacket((LobbyAddressesPacket) packet);
            } else if (packet instanceof TemplateActionPacket) {
                handleTemplateActionPacket((TemplateActionPacket) packet, cloudLib);
            } else {
                log(packet.getClass().getSimpleName() + " received from Master - lol");
            }
        } catch (Exception e) {
            e.printStackTrace();//FOR TESTING
        }
    }

    private void handleStopPacket(StopPacket packet) {
        log("Wrapper Stopped by Master...");
        log("Reason: " + packet.getReason());
        this.cloudWrapper.stop();
    }

    private void handleServerActionPacket(ServerActionPacket packet, CloudLib cloudLib) {
        Server server = cloudLib.getServerRegistry().getServer(packet.getServerName());
        if (server == null) {
            log("Master send a ServerActionPacket with a not existen Servername - " + packet.getServerName());
            return;
        }
        server.handleServerActionPacket(packet, cloudLib);
    }
    private void handleRegisterServerPacket(RegisterServerPacket packet, CloudLib cloudLib) {
        this.cloudWrapper.getServerGroupRegistry().getServerGroup(packet.getTemplateName()).startServer(cloudLib, b -> {/*MAYBE SAY IT WHEN U CANT START*/});
    }

    private void handleLobbyAddressesPacket(LobbyAddressesPacket packet) {
        CloudWrapper.lobbyAddresses = new ArrayList<>(packet.getLobbyAddresses());
        this.cloudWrapper.startSeverGroups();
    }

    private void handleTemplateActionPacket(TemplateActionPacket packet, CloudLib cloudLib) {
        switch (packet.getType()) {
            case CREATE:
                if (!packet.getTemplate().exist() && cloudLib.getTemplateRegistry().getTemplate(packet.getTemplate().getName()) == null) {
                    packet.getTemplate().save();
                    if (packet.hasTemplateFolder()) {
                        try {
                            packet.readAndSaveTemplateFolderFromPacket(cloudLib);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cloudLib.getTemplateRegistry().loadTemplate(packet.getTemplate().getName());
                    ServerGroup serverGroup = this.cloudWrapper.getServerGroupRegistry().load(packet.getTemplate().getName(), cloudLib);
                    if (serverGroup != null) this.cloudWrapper.getServerGroupRegistry().startServerGroup(serverGroup, cloudLib, b -> {});
                }
                break;
            case UPDATE:
                cloudLib.getThreadPoolRegistry().submit(() -> this.cloudWrapper.getServerGroupRegistry().stopAndUnload(cloudLib, packet.getTemplate().getName(), b -> {
                    packet.getTemplate().delete();
                    cloudLib.getTemplateRegistry().unloadTemplate(packet.getTemplate().getName());
                    packet.getTemplate().save();
                    if (packet.getTemplate().getTemplateType().equals(TemplateType.LOBBY)) {
                        try {
                            FileUtils.deleteDirectory(new File("./runningServers/", packet.getTemplate().getName()));
                        } catch (IOException e) {}
                    }
                    if (packet.hasTemplateFolder()) {
                        try {
                            packet.readAndSaveTemplateFolderFromPacket(cloudLib);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cloudLib.getThreadPoolRegistry().submit(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        cloudLib.getTemplateRegistry().loadTemplate(packet.getTemplate().getName());
                        ServerGroup serverGroup = this.cloudWrapper.getServerGroupRegistry().load(packet.getTemplate().getName(), cloudLib);
                        if (serverGroup != null) this.cloudWrapper.getServerGroupRegistry().startServerGroup(serverGroup, cloudLib, a -> {});
                    });
                }));
                break;
            case REMOVE:
                cloudLib.getThreadPoolRegistry().submit(() -> this.cloudWrapper.getServerGroupRegistry().stopAndUnload(cloudLib, packet.getTemplate().getName(), b -> {
                    packet.getTemplate().delete();
                    cloudLib.getTemplateRegistry().unloadTemplate(packet.getTemplate().getName());
                }));
                break;
        }
    }
}
