/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql.queries;

import me.phantomclone.phoenixnetwork.backendcore.database.mysql.util.QueryUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class UpdateQuery {

    private String table;
    private LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
    private List<String> wheres = new ArrayList<String>();

    public UpdateQuery(String table) {
        this.table = table;
    }

    public UpdateQuery set(String column, String value) {
        values.put(column, value);
        return this;
    }

    public UpdateQuery set(String column) {
        set(column, "?");
        return this;
    }

    public UpdateQuery where(String expression) {
        wheres.add(expression);
        return this;
    }

    public UpdateQuery and(String expression) {
        where(expression);
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(table)
                .append(" SET ");

        String seperator = "";
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String column = entry.getKey();
            String value = entry.getValue();
            builder.append(seperator)
                    .append(column)
                    .append("=")
                    .append(value);
            seperator = ",";
        }

        if (wheres.size() > 0) {
            builder.append(" WHERE ")
                    .append(QueryUtils.separate(wheres, " AND "));
        }

        return builder.toString();
    }

}