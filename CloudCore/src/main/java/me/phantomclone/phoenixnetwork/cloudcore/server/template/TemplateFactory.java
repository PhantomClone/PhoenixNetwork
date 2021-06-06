/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.server.template;

import lombok.NonNull;
import me.phantomclone.phoenixnetwork.cloudcore.CloudLib;
import me.phantomclone.phoenixnetwork.cloudcore.server.ServerType;

/**
 * <p>A interface that represents a factory which can creates templates.</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface TemplateFactory {

    /**
     * Try to create a new Template. If it succeed, it will send the template to the wrapper and return true. Else it returns false.
     * @param cloudLib A non-null {@link CloudLib} to get the wrapper and send him the packet.
     * @param wrapperName A non-null wrapperName to get the wrapper and send him the packet.
     * @param name A non-null name of the template.
     * @param templateType A non-null {@link TemplateType} of the template.
     * @param serverType A non-null {@link ServerType} of the template.
     * @param startPort The start port for the template.
     * @param endPort The end port for the template.
     * @param minMemory The min memory for the servers of the template.
     * @param maxMemory The max memory for the servers of the template.
     * @param minOnlineServers The min online server of the template.
     * @return Returns if it succeed to create and send the template to the wrapper, true else it returns false.
     */
    boolean createTemplate(@NonNull CloudLib cloudLib, @NonNull String wrapperName, @NonNull String name, @NonNull TemplateType templateType, @NonNull ServerType serverType, int startPort, int endPort, int minMemory, int maxMemory, int minOnlineServers);

}