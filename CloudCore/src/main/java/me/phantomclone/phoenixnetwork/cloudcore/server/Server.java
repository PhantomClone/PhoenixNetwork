/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.network.packet.packets.ServerActionPacket;
import me.phantomclone.phoenixnetwork.cloudcore.server.template.Template;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * <p>A interface that represents a server.</p>
 *
 * <p>The JavaDocs describe only the interface and not the implantation of a MasterServer, ProxyServer or MinecraftServer!</p>
 *
 * <p>It have some default methode to copy and delete files.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Server {

    /**
     * It returns the {@link ServerType} of this server.
     * @return It returns the {@link ServerType} of this server.
     */
    ServerType getServerType();

    /**
     * It returns the {@link Template} of this server.
     * @return It returns the {@link Template} of this server.
     */
    Template getTemplate();

    /**
     * It returns the {@link ServerState} of this server.
     * @return It returns the {@link ServerState} of this server.
     */
    ServerState getServerState();

    /**
     * It returns the port of this server.
     * @return It returns the port of this server.
     */
    int getPort();

    /**
     * It returns the name of this server.
     * Normally the name is: {@link Template#getName()}  "_" + {@link Server#getPort()}.
     * @return It returns the name of this server.
     */
    String getName();

    /**
     * Call this methode to prepare this server. It can only be prepared if the server is deleted.
     * It also can only prepare one per time. So do not call this methode again before you do not get an callback!
     * @param cloudLib The non-null {@link CloudLib} which is need to update the state whether to the master or to redis.
     * @param callback The non-null {@link Consumer} will be accepted when the server is prepared with true or false when it fails.
     */
    void preparation(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback);

    /**
     * Call this methode to start this server. It can only be prepared if the server is deleted.
     * It also can only start one per time. So do not call this methode again before you do not get an callback!
     * @param cloudLib The non-null {@link CloudLib} which is need to update the state whether to the master or to redis.
     * @param callback The non-null {@link Consumer} will be accepted when the server is started with true or false when it fails.
     */
    void start(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback);

    /**
     * Call this methode to stop this server. It can only be stopped if the server is deleted.
     * It also can only stop one per time. So do not call this methode again before you do not get an callback!
     * @param cloudLib The non-null {@link CloudLib} which is need to update the state whether to the master or to redis.
     * @param callback The non-null {@link Consumer} will be accepted when the server is stopped with true or false when it fails.
     */
    void stop(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback);

    /**
     * Call this methode to delete this server. It can only be deleted if the server is deleted.
     * It also can only delete one per time. So do not call this methode again before you do not get an callback!
     * @param cloudLib The non-null {@link CloudLib} which is need to update the state whether to the master or to redis.
     * @param callback The non-null {@link Consumer} will be accepted when the server is deleted with true or false when it fails.
     */
    void delete(@NonNull CloudLib cloudLib, @NonNull Consumer<Boolean> callback);

    /**
     * Call this methode when you want to send a command to this server.
     * @param line This non-null {@link String} will be send to the server.
     * @param callback The non-null {@link Consumer} will be accepted when the server got the command with true or false when the server do not get the command.
     */
    void sendCommand(@NonNull String line, @NonNull Consumer<Boolean> callback);

    /**
     * Do not call this methode by your self! This methode is needed to synchrony MasterServer and GameServer!
     * @param serverState The non-null {@link ServerState} which the server will be set to if the success is true else not.
     * @param success Says if the {@link ServerState} change was successfully.
     */
    void handleUpdateStateServerPacket(@NonNull ServerState serverState, boolean success);

    /**
     * Do not call this methode by your self! This methode is needed to synchrony MasterServer and GameServer!
     * @param packet The incoming non-null packet from Master which a server action.
     * @param cloudLib The non-null {@link CloudLib} to send an result backwards to master.
     */
    void handleServerActionPacket(@NonNull ServerActionPacket packet, @NonNull CloudLib cloudLib);

    /**
     * Kill forcefully the running server process. Only Wrapper supported!
     */
    void killProcess();

    /**
     * Returns the running server folder path. Only Wrapper supported!
     * @return Returns the running server folder path. Only Wrapper supported!
     */
    String getFolderPath();

    default void copy(@NonNull File sourceLocation, @NonNull File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    default void copyDirectory(@NonNull File source, @NonNull File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String f : source.list()) {
            copy(new File(source, f), new File(target, f));
        }
    }

    default void copyFile(@NonNull File source, @NonNull File target) throws IOException {
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)
        ) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    default void deleteDir(@NonNull File file) {
        if (!file.exists()) return;
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

}