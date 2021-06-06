/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql.queries;

import me.phantomclone.phoenixnetwork.backendcore.database.mysql.util.QueryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class DeleteQuery {

    private String table;
    private List<String> wheres = new ArrayList<String>();

    public DeleteQuery(String table) {
        this.table = table;
    }

    public DeleteQuery where(String expression) {
        wheres.add(expression);
        return this;
    }

    public DeleteQuery and(String expression) {
        where(expression);
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ")
                .append(table);

        if (wheres.size() > 0) {
            builder.append(" WHERE ")
                    .append(QueryUtils.separate(wheres, " AND "));
        }

        return builder.toString();
    }

}