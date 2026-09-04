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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.spi.SyncFactory;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.helper.UnoHelper;


public class ResultSetHelper {

    static final String ROWSET_FACTORY = "io.github.prrvchr.java.rowset.RowSetFactoryImpl";
    static final String ROWSET_PROVIDER = "io.github.prrvchr.java.rowset.providers.OptimisticProvider";

    private static RowSetFactory Factory = null;

    public static CachedRowSet getCachedRowSet(ResultSet result) throws SQLException {
        try {
            CachedRowSet rowset = getCachedRowSet();
            rowset.populate(result);
            result.close();
            return rowset;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e);
        }
    }

    public static CachedRowSet getCachedRowSet(Provider provider, ResultSet result, QueryHelper query)
        throws SQLException {
        try {
            CachedRowSet rowset = getCachedRowSet();
            // XXX: Some drivers do not provide the table or schema name in the ResultSet metadata.
            // XXX: For these drivers, you must provide before populating
            // XXX: the full table name on which the select query is based.
            if (provider.getConfigSQL().needCompletedMetaData()) {
                rowset.setTableName(query.getTableName());
            }
            rowset.populate(result);
            // XXX: Assigning the key descriptions lets us know if this result is editable
            setCachedRowSetKeyDesc(result.getStatement().getConnection(), rowset);
            result.close();
            return rowset;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e);
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

    public static ResultSet getDefaultTablePrivilegesResultset(String[] privileges,
                                                               String[] columns,
                                                               String catalog,
                                                               String schema,
                                                               String table,
                                                               String user)
        throws java.sql.SQLException {
        CachedRowSet rowset = getCachedRowSet();
        rowset.setMetaData(getTablePrivilegesMetadata(columns));
        int i = 1;
        for (String privilege : privileges) {
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
        if (Factory == null) {
            SyncFactory.registerProvider(ROWSET_PROVIDER);
            Factory = RowSetProvider.newFactory(ROWSET_FACTORY, null);
        }
        return Factory;
    }

    private static void setCustomRowSet(CachedRowSet rowset, RowSetData data, int count)
        throws java.sql.SQLException {
        while (rowset.next()) {
            boolean updated = false;
            for (int index = 1; index <= count; index++) {
                String key = rowset.getString(index);
                //System.out.println("ResultSetHelper.setCustomRowSet() 1 key: " + key);
                if (rowset.wasNull() || !data.hasEntry(index, key)) {
                    continue;
                }
                key = RowSetData.getDataKey(index, key);
                if (data.isDeletableRow(key)) {
                    //System.out.println("ResultSetHelper.setCustomRowSet() 2 key: " + key);
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
                        case Types.BIGINT -> rowset.updateLong(index, Long.valueOf(value));

                        case Types.INTEGER,
                             Types.SMALLINT -> rowset.updateInt(index, Integer.valueOf(value));

                        case Types.TINYINT -> rowset.updateShort(index, Short.valueOf(value));

                        case Types.BIT,
                             Types.BOOLEAN -> rowset.updateBoolean(index, Boolean.valueOf(value));

                        case Types.LONGVARCHAR,
                             Types.VARCHAR,
                             Types.CHAR -> rowset.updateString(index, value);

                        case Types.DOUBLE -> rowset.updateDouble(index, Double.valueOf(value));

                        case Types.FLOAT -> rowset.updateFloat(index, Float.valueOf(value));

                        default -> rowset.updateObject(index, value);
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new java.sql.SQLException(e);
        }
    }

    private static void setCachedRowSetKeyDesc(Connection connection, CachedRowSet crs)
        throws java.sql.SQLException {

        boolean isReadOnly = true;
        List<Integer> keys = new ArrayList<>();
        ResultSetMetaData md = crs.getMetaData();
        if (!crs.isReadOnly() && md.getColumnCount() > 0) {
            isReadOnly = setCachedRowSetKeyDesc(connection, md, keys);
        }
        crs.setReadOnly(isReadOnly);
        if (!isReadOnly && !keys.isEmpty()) {
            crs.setKeyColumns(keys.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    private static boolean setCachedRowSetKeyDesc(Connection connection, ResultSetMetaData md, List<Integer> keys)
        throws java.sql.SQLException {
        boolean isReadOnly = false;
        List<Table> tables = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            // XXX: The columns will be retrieved in the order of their table appearance.
            Table table = new Table(md, i);
            if (!table.isTableFullyNamed()) {
                if (md.isReadOnly(i)) {
                    continue;
                }
                isReadOnly = true;
                break;
            }
            if (!tables.contains(table)) {
                List<String> primaryKeys = getPrimaryKeys(connection.getMetaData(), table);
                List<Integer> pkIndexes = getCachedRowSetKeyDesc(md, primaryKeys, table);
                if (!primaryKeys.isEmpty() && pkIndexes.isEmpty()) {
                    isReadOnly = true;
                    break;
                }
                keys.addAll(pkIndexes);
                tables.add(table);
            }
        }
        return isReadOnly;
    }

    private static List<Integer> getCachedRowSetKeyDesc(ResultSetMetaData md, List<String> primaryKeys, Table table)
        throws java.sql.SQLException {
        List<Integer> keys = new ArrayList<>();
        if (primaryKeys.isEmpty()) {
            // XXX: A natural compound key will be constructed from the table columns
            keys = getKeyDescFromTableColumns(md, table);
        } else {
            // XXX: We have primary keys and we need to use all of them
            keys = getKeyDescFromPrimaryKeys(md, primaryKeys, table);
        }
        return keys;
    }

    private record Table(Optional<String> catalog, Optional<String> schema, String table) {

        private Table(ResultSetMetaData md, int index)
                throws java.sql.SQLException {
            this(Optional.ofNullable(md.getCatalogName(index)),
                 Optional.ofNullable(md.getSchemaName(index)),
                 md.getTableName(index));
        }

        private boolean isTableFullyNamed() {
            return table != null;
        }
    }

    private static final List<String> getPrimaryKeys(DatabaseMetaData dbmd, Table table)
        throws java.sql.SQLException {
        final int COLUMN_NAME = 4;
        List<String> keys = new ArrayList<>();

        try (ResultSet rs = dbmd.getPrimaryKeys(table.catalog.orElse(null), table.schema.orElse(null), table.table)) {
            while (rs.next()) {
                String column = rs.getString(COLUMN_NAME);
                if (!rs.wasNull()) {
                    keys.add(column);
                }
            }
        }
        return keys;
    }

    private static List<Integer> getKeyDescFromTableColumns(ResultSetMetaData md,
                                                            Table table)
        throws java.sql.SQLException {
        List<Integer> keys = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            switch (md.getColumnType(i)) {
                case java.sql.Types.CLOB:
                case java.sql.Types.STRUCT:
                case java.sql.Types.SQLXML:
                case java.sql.Types.BLOB:
                case java.sql.Types.ARRAY:
                case java.sql.Types.OTHER:
                    break;
                default:
                    if (table.equals(new Table(md, i))) {
                        keys.add(i);
                    }
            }
        }
        return keys;
    }

    private static List<Integer> getKeyDescFromPrimaryKeys(ResultSetMetaData md,
                                                           List<String> primaryKeys,
                                                           Table table)
        throws java.sql.SQLException {
        List<Integer> keys = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (primaryKeys.contains(md.getColumnName(i)) && table.equals(new Table(md, i))) {
                keys.add(-i);
            }
        }
        if (keys.size() == primaryKeys.size()) {
            return keys;
        }
        return List.of();
    }

}
