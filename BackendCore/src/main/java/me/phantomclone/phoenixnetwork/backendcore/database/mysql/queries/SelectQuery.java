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
public class SelectQuery {

    private String table;
    private List<String> columns = new ArrayList<String>();
    private List<String> wheres = new ArrayList<String>();
    private String orderBy;
    private boolean orderByAscending = false;
    private int limitOffset = 0;
    private int limitRowCount = 0;

    public SelectQuery(String table) {
        this.table = table;
    }

    public SelectQuery column(String column) {
        columns.add(column);
        return this;
    }

    public SelectQuery where(String expression) {
        wheres.add(expression);
        return this;
    }

    public SelectQuery and(String expression) {
        where(expression);
        return this;
    }

    public SelectQuery orderBy(String column, boolean ascending) {
        this.orderBy = column;
        this.orderByAscending = ascending;
        return this;
    }

    public SelectQuery limit(int offset, int rowCount) {
        this.limitOffset = offset;
        this.limitRowCount = rowCount;
        return this;
    }

    public SelectQuery limit(int rowCount) {
        this.limitOffset = 0;
        this.limitRowCount = rowCount;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ")
                .append(QueryUtils.separate(columns, ","))
                .append(" FROM ")
                .append(table);

        if (wheres.size() > 0) {
            builder.append(" WHERE ")
                    .append(QueryUtils.separate(wheres, " AND "));
        }

        if (orderBy != null) {
            builder.append(" ORDER BY ")
                    .append(orderBy)
                    .append(orderByAscending ? " ASC" : " DESC");
        }

        if (limitRowCount > 0) {
            builder.append(" LIMIT ")
                    .append(limitOffset)
                    .append(",").append(limitRowCount);
        }

        return builder.toString();
    }

}