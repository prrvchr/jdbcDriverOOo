/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
║                                                                                    ║
║   Permission is hereby granted, free of charge, to any person obtaining            ║
║   a copy of this software and associated documentation files (the "Software"),     ║
║   to deal in the Software without restriction, including without limitation        ║
║   the rights to use, copy, modify, merge, publish, distribute, sublicense,         ║
║   and/or sell copies of the Software, and to permit persons to whom the Software   ║
║   is furnished to do so, subject to the following conditions:                      ║
║                                                                                    ║
║   The above copyright notice and this permission notice shall be included in       ║
║   all copies or substantial portions of the Software.                              ║
║                                                                                    ║
║   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,                  ║
║   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES                  ║
║   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.        ║
║   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY             ║
║   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,             ║
║   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE       ║
║   OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
*/
package io.github.prrvchr.driver.rowset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;

public class RowTable {

    private RowCatalog mCatalog;
    private Map<String, RowColumn> mColumns = new LinkedHashMap<>();
    private List<String> mKeys = new ArrayList<>();
    private boolean mIsPrimary = false;
    private NamedComponents mComponent;
    private String mWhere;


    // The constructor method:
    public RowTable (Connection connection,
                     RowCatalog catalog,
                     ResultSetMetaData metadata,
                     int index)
        throws SQLException {
        this(connection, catalog, DBTools.getNamedComponents(metadata, index), false);
    }

    public RowTable (Connection connection,
                     RowCatalog catalog,
                     NamedComponents component)
        throws SQLException {
        this(connection, catalog, component, false);
    }

    public RowTable (Connection connection,
                     RowCatalog catalog,
                     NamedComponents component,
                     boolean ordinal)
        throws SQLException {
        mCatalog = catalog;
        mComponent = component;
        try (ResultSet result = connection.getMetaData().getColumns(component.getCatalog(),
                                            component.getSchema(), component.getTable(), "%")) {
            while (result.next()) {
                addColumn(new RowColumn(this, result, ordinal));
            }
        }
    }

    public RowCatalog getCatalog() {
        return mCatalog;
    }

    public boolean isValid() {
        return mComponent.getTable() != null && !mComponent.getTable().isBlank();
    }

    public void addColumn(RowColumn column) {
        mColumns.put(column.getName(), column);
    }

    public void setIndexColumn(String name, int index) {
        if (mColumns.containsKey(name)) {
            RowColumn column = mColumns.get(name);
            column.setIndex(index);
        }
    }

    public boolean hasRowIdentifier() {
        return !mKeys.isEmpty();
    }

    public boolean isIdentifierPrimaryKey() {
        return mIsPrimary;
    }

    public void addRowIdentifier(String column, int index) {
        mKeys.add(index, column);
    }

    public void setIdentifierAsPrimaryKey() {
        mIsPrimary = true;
    }

    public void setDefaultRowIdentifier()
        throws SQLException {
        // XXX: We don't know the primary key of this table!!!!
        // XXX: First auto-increment column become default row identifier
        RowColumn column = getAutoIncrementColumn();
        if (column == null) {
            // XXX: Ok we need to find a unique row column...
            column = getPseudoIdentifierColumn();
        }
        if (column != null) {
            mKeys.add(column.getName());
        }
    }

    private RowColumn getPseudoIdentifierColumn()
        throws SQLException {
        RowColumn identifier = null;
        String query = mCatalog.getUniqueQuery();
        for (RowColumn column : mColumns.values()) {
            System.out.println("RowTable.getPseudoIdentifierColumn() 1 Column: " + column.getName());
            // We are looking only for certain column type.
            if (RowHelper.isValidKeyType(column.getType())) {
                String command = String.format(query, column.getIdentifier(), getComposedName(true));
                try (ResultSet result = mCatalog.getStatement().executeQuery(command)) {
                    if (!result.next()) {
                        identifier = column;
                        System.out.println("RowTable.getPseudoIdentifierColumn() 2 Column: " + column.getName());
                        break;
                    }
                }
            }
        }
        return identifier;
    }

    public List<String> getRowIdentifier() {
        return mKeys;
    }

    public boolean hasColumn(String column) {
        boolean hascolumn = false;
        if (mColumns.containsKey(column)) {
            hascolumn = mColumns.get(column).getIndex() > 0;
        }
        return hascolumn;
    }

    public RowColumn getColumn(String column) {
        return mColumns.get(column);
    }

    public Map<String, RowColumn> getColumnNames() {
        return mColumns;
    }

    public RowColumn getRowIdentifierColumn() {
        RowColumn key = null;
        if (!mKeys.isEmpty()) {
            key = mColumns.get(mKeys.get(0));
        } else {
            key = getAutoIncrementColumn();
        }
        return key;
    }

    public RowColumn getAutoIncrementColumn() {
        RowColumn column = null;
        for (RowColumn c : mColumns.values()) {
            if (c.isAutoIncrement() && c.getIndex() > 0) {
                column = c;
            }
        }
        return column;
    }

    public String getCatalogName() {
        return mComponent.getCatalogName();
    }

    public String getSchemaName() {
        return mComponent.getSchemaName();
    }

    public String getName() {
        return mComponent.getTableName();
    }

    public String getComposedName(boolean quoted)
        throws SQLException {
        return DBTools.buildName(mCatalog.getProvider(), mComponent, mCatalog.getNamedSupport(), quoted);
    }

    public boolean isSameTable(ResultSetMetaData metadata,
                               int index)
        throws SQLException {
        return getCatalogName().equals(metadata.getCatalogName(index)) &&
               getSchemaName().equals(metadata.getSchemaName(index)) &&
               getName().equals(metadata.getTableName(index));
    }

    public boolean isSameTable(NamedComponents component)
        throws SQLException {
        return component != null &&
               getCatalogName().equals(component.getCatalogName()) &&
               getSchemaName().equals(component.getSchemaName()) &&
               getName().equals(component.getTableName());
    }

    public String getWhereCmd()
        throws SQLException {
        if (mWhere == null) {
            List<String> columns = new ArrayList<>();
            for (String key : mKeys) {
                columns.add(mColumns.get(key).getPredicate());
            }
            mWhere = String.join(mCatalog.getAnd(), columns);
        }
        return mWhere;
    }

    public String getSeparator() {
        return mCatalog.getSeparator();
    }

    public String getParameter() {
        return mCatalog.getParameter();
    }

    public String getMark() {
        return mCatalog.getMark();
    }

    public Collection<RowColumn> getColumns() {
        return mColumns.values();
    }

}
