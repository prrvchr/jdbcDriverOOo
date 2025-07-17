/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.driver.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.AbstractMap.SimpleImmutableEntry;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.spi.SyncFactory;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.provider.Provider;


public class ResultSetHelper {

    static final String ROWSET_FACTORY = "io.github.prrvchr.java.rowset.RowSetFactoryImpl";
    static final String ROWSET_PROVIDER = "io.github.prrvchr.java.rowset.providers.OptimisticProvider";

    private static RowSetFactory sFactory = null;

    public static CachedRowSet getCachedRowSet(ResultSet result) throws SQLException {
        try {
            CachedRowSet rowset = getCachedRowSet();
            rowset.populate(result);
            // XXX: Does this CachedRowSet is writable or readonly? This can be found
            // XXX: by running acceptChange() and reading the isReadOnly() property.
            rowset.acceptChanges(result.getStatement().getConnection());
            result.close();
            return rowset;
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public static ResultSet getCustomDataResultSet(ResultSet result,
                                                   RowSetData... dataset)
        throws java.sql.SQLException {
        CachedRowSet rowset = null;
        int count = 0;
        if (dataset.length > 0) {
            for (RowSetData data : dataset) {
                // XXX: We must take into account that the data can be null and that
                // XXX: in this case it is the unchanged ResultSet that is returned.
                if (data == null) {
                    continue;
                }
                if (rowset == null) {
                    rowset = getCachedRowSet();
                    rowset.populate(result);
                    result.close();
                    count = rowset.getMetaData().getColumnCount();
                }
                if (rowset.size() > 0) {
                    setCustomRowSet(rowset, data, count);
                }
            }
            if (rowset != null && rowset.size() > 0) {
                rowset.setReadOnly(true);
                result = rowset;
            }
        }
        return result;
    }

    public static ResultSet getTablePrivilegesResultset(ResultSet result,
                                                        String[] columns,
                                                        RowSetData data)
        throws java.sql.SQLException {
        CachedRowSet rowset = getCachedRowSet();
        rowset.setMetaData(getTablePrivilegesMetadata(columns));
        while (result.next()) {
            rowset.moveToInsertRow();
            for (int i = 1; i <= columns.length; i++) {
                String value = result.getString(i);
                if (result.wasNull()) {
                    rowset.updateNull(i);
                } else {
                    rowset.updateString(i, value);
                }
            }
            rowset.insertRow();
            rowset.moveToCurrentRow();
        }
        result.close();
        rowset.beforeFirst();
        if (data != null) {
            setCustomRowSet(rowset, data, columns.length);
        }
        rowset.setReadOnly(true);
        return rowset;
    }

    public static ResultSet getDefaultTablePrivilegesResultset(Provider provider,
                                                               String catalog,
                                                               String schema,
                                                               String table,
                                                               String user)
        throws java.sql.SQLException {
        String[] columns = provider.getConfigSQL().getTablePrivilegesColumns();
        CachedRowSet rowset = getCachedRowSet();
        rowset.setMetaData(getTablePrivilegesMetadata(columns));
        int i = 1;
        for (String privilege : provider.getConfigSQL().getDefaultTablePrivileges()) {
            rowset.moveToInsertRow();
            rowset.updateString(i++, catalog);
            rowset.updateString(i++, schema);
            rowset.updateString(i++, table);
            rowset.updateNull(i++);
            rowset.updateString(i++, user);
            rowset.updateString(i, privilege);
            rowset.insertRow();
            rowset.moveToCurrentRow();
            i = 1;
        }
        rowset.beforeFirst();
        rowset.setReadOnly(true);
        return rowset;
    }

    private static RowSetMetaDataImpl getTablePrivilegesMetadata(String[] columns)
        throws java.sql.SQLException {
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        int i = 1;
        md.setColumnCount(columns.length);
        for (String column : columns) {
            md.setAutoIncrement(i, false);
            md.setCaseSensitive(i, true);
            md.setSearchable(i, false);
            md.setCurrency(i, false);
            md.setNullable(i, ResultSetMetaData.columnNullableUnknown);
            md.setCatalogName(i, "");
            md.setSchemaName(i, "");
            md.setTableName(i, "");
            md.setColumnName(i, column);
            md.setColumnLabel(i, column);
            md.setScale(i, 0);
            md.setPrecision(i, 0);
            md.setColumnType(i, java.sql.Types.VARCHAR);
            md.setColumnTypeName(i, "VARCHAR");
            i++;
        }
        return md;
    }

    private static CachedRowSet getCachedRowSet() throws java.sql.SQLException {
        CachedRowSet rowset = getRowSetFactory().createCachedRowSet();
        rowset.setSyncProvider(ROWSET_PROVIDER);
        return rowset;
    }

    private static RowSetFactory getRowSetFactory()
        throws java.sql.SQLException {
        if (sFactory == null) {
            SyncFactory.registerProvider(ROWSET_PROVIDER);
            sFactory = RowSetProvider.newFactory(ROWSET_FACTORY, null);
        }
        return sFactory;
    }

    private static void setCustomRowSet(CachedRowSet rowset, RowSetData data, int count)
        throws java.sql.SQLException {
        while (rowset.next()) {
            boolean updated = false;
            for (int index = 1; index <= count; index++) {
                String key = rowset.getString(index);
                if (rowset.wasNull() || !data.hasEntry(index, key)) {
                    continue;
                }
                key = RowSetData.getDataKey(index, key);
                if (data.isDeletableRow(key)) {
                    rowset.deleteRow();
                    break;
                }
                updateRowSetData(rowset, data, key);
                updated = true;
            }
            if (updated) {
                rowset.setOriginalRow();
            }
        }
        rowset.beforeFirst();
    }

    private static void updateRowSetData(CachedRowSet rowset,
                                         RowSetData data,
                                         String key)
        throws java.sql.SQLException {
        try {
            for (SimpleImmutableEntry<Integer, String> entry : data.getValue(key)) {
                int index = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    rowset.updateNull(index);
                } else {
                    switch (rowset.getMetaData().getColumnType(index)) {
                        case Types.BIGINT:
                            rowset.updateLong(index, Long.valueOf(value));
                            break;
                        case Types.INTEGER:
                        case Types.SMALLINT:
                            rowset.updateInt(index, Integer.valueOf(value));
                            break;
                        case Types.TINYINT:
                            rowset.updateShort(index, Short.valueOf(value));
                            break;
                        case Types.BIT:
                        case Types.BOOLEAN:
                            rowset.updateBoolean(index, Boolean.valueOf(value));
                            break;
                        case Types.LONGVARCHAR:
                        case Types.VARCHAR:
                        case Types.CHAR:
                            rowset.updateString(index, value);
                            break;
                        case Types.DOUBLE:
                            rowset.updateDouble(index, Double.valueOf(value));
                            break;
                        case Types.FLOAT:
                            rowset.updateFloat(index, Float.valueOf(value));
                            break;
                        default:
                            rowset.updateObject(index, value);
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new java.sql.SQLException(e);
        }
    }

}
