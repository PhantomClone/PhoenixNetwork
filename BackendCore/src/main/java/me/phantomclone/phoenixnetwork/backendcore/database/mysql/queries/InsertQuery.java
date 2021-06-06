/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mysql.queries;

import me.phantomclone.phoenixnetwork.backendcore.database.mysql.util.QueryUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author PhantomClone
 * @since 1.0-SNAPSHOT
 */
public class InsertQuery {

    private String table;
    private LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> duplicateValues = new LinkedHashMap<String, String>();
    private boolean onDuplicateKey = false;

    public InsertQuery(String table) {
        this.table = table;
    }

    public InsertQuery value(String column, String value) {
        values.put(column, value);
        return this;
    }

    public InsertQuery value(String column) {
        value(column, "?");
        return this;
    }

    public InsertQuery onDuplicateKeyUpdate() {
        onDuplicateKey = true;
        return this;
    }

    public InsertQuery set(String column, String value) {
        duplicateValues.put(column, value);
        return this;
    }

    public InsertQuery set(String column) {
        set(column, "VALUES(" + column + ")");
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ")
                .append(table)
                .append(" (")
                .append(QueryUtils.separate(values.keySet(), ","))
                .append(")")
                .append(" VALUES (")
                .append(QueryUtils.separate(values.values(), ","))
                .append(")");

        if (onDuplicateKey) {
            builder.append(" ON DUPLICATE KEY UPDATE ");
            String separator = "";
            for (Map.Entry<String, String> entry : duplicateValues.entrySet()) {
                String column = entry.getKey();
                String value = entry.getValue();
                builder.append(separator)
                        .append(column)
                        .append("=")
                        .append(value);
                separator = ",";
            }
        }

        return builder.toString();
    }

}