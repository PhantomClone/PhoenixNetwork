/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.cloudcore.master;

import me.phantomclone.phoenixnetwork.cloudcore.config.ConfigImpl;

import java.io.File;
import java.util.Objects;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class MasterRegistryImpl implements MasterRegistry {

    private Master master;
    private final ConfigImpl config = ConfigImpl.create();
    private final File masterConfigFile = new File("./config.json");

    public static MasterRegistryImpl create() {
        return new MasterRegistryImpl();
    }

    private MasterRegistryImpl() {}

    @Override
    public Master getMaster() {
        if (!this.masterConfigFile.exists()) {
            return null;
        }
        if (this.master != null) return this.master;
        this.config.read(this.masterConfigFile);
        String hostname = (String) Objects.requireNonNull(this.config.get("hostname"));
        int port = new Double((Double) Objects.requireNonNull(this.config.get("port"))).intValue();

        this.master = new Master() {
            @Override
            public String getHostname() {
                return hostname;
            }

            @Override
            public int getPort() {
                return port;
            }
        };
        this.config.unload();
        return this.master;
    }
}
