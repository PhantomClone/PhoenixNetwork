/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore;

import me.phantomclone.phoenixnetwork.backendcore.database.DatabaseLib;
import me.phantomclone.phoenixnetwork.backendcore.database.DatabaseLibImpl;
import me.phantomclone.phoenixnetwork.backendcore.language.LanguageRegistry;
import me.phantomclone.phoenixnetwork.backendcore.language.LanguageRegistryImpl;
import me.phantomclone.phoenixnetwork.backendcore.server.ServerChanger;
import me.phantomclone.phoenixnetwork.backendcore.storage.StorageRegistry;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public abstract class Backend<T> {

    private final DatabaseLib databaseLib;
    private final LanguageRegistry languageRegistry;

    public Backend() {
        this.databaseLib = DatabaseLibImpl.create();
        this.languageRegistry = LanguageRegistryImpl.create();
    }

    public abstract StorageRegistry<T> getStorageRegistry();
    public abstract ServerChanger<T> getServerChanger();

    public DatabaseLib getDatabaseLib() {
        return this.databaseLib;
    }

    public LanguageRegistry getLanguageRegistry() {
        return this.languageRegistry;
    }

}
