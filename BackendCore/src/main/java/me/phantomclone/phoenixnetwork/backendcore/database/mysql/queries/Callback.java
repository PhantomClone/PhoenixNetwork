/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql.queries;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public interface Callback<V extends Object, T extends Throwable> {

    void call(V result, T thrown);

}