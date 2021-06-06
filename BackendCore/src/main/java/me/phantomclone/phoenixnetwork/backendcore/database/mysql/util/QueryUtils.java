/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql.util;

import java.util.Collection;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class QueryUtils {

    public static String separate(Collection<String> collection, String separator) {
        StringBuilder builder = new StringBuilder();
        String sep = "";
        for (String item : collection) {
            builder.append(sep)
                    .append(item);
            sep = separator;
        }
        return builder.toString();
    }

}