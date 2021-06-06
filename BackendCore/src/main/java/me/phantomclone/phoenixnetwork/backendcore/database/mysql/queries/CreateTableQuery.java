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
public class CreateTableQuery {

    private String table;
    private boolean ifNotExists = false;
    private List<String> columns = new ArrayList<String>();
    private String primaryKey;

    public CreateTableQuery(String table) {
        this.table = table;
    }

    public CreateTableQuery ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    public CreateTableQuery column(String column, String settings) {
        columns.add(column + " " + settings);
        return this;
    }

    public CreateTableQuery primaryKey(String column) {
        this.primaryKey = column;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");

        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }

        builder.append(table)
                .append(" (")
                .append(QueryUtils.separate(columns, ","));

        if (primaryKey != null) {
            builder.append(",PRIMARY KEY(");
            builder.append(primaryKey);
            builder.append(")");
        }

        builder.append(")");

        return builder.toString();
    }

}