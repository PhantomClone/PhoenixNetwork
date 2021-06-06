/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudwrapper.bootstrap;

import me.phantomclone.phoenixnetwork.cloudwrapper.CloudWrapper;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class Bootstrap {

    public static void main(String[] args) {
        CloudWrapper.create().start();
    }

}
