/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.master;

/**
 * <p>A interface that represents a storage for the connection data (Hostname:port)</p>
 *
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Master {

    /**
     * Returns the hostname of the master.
     * For example 'localhost'
     * @return Returns the hostname of the master.
     */
    String getHostname();

    /**
     * Returns the port of the master.
     * For example '5566'
     * @return Returns the hostname of the master.
     */
    int getPort();

}