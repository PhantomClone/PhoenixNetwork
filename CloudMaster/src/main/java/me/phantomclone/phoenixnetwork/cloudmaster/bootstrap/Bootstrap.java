/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudmaster.bootstrap;

import me.phantomclone.phoenixnetwork.cloudmaster.CloudMaster;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class Bootstrap {

    public static void main(String[] args) {
        CloudMaster.create().start();
    }

}
