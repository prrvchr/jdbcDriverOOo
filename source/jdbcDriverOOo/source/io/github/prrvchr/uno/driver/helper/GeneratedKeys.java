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
package io.github.prrvchr.uno.driver.helper;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.resultset.ResultSetWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class GeneratedKeys {


    // XXX: Method called from sdbc.CallableStatement.getGeneratedValues()
    // XXX: Method called from sdbc.PreparedStatement.getGeneratedValues()
    // XXX: Method called from sdbc.Statement.getGeneratedValues()
    public static final java.sql.ResultSet getGeneratedResult(Provider provider,
                                                              Statement statement,
                                                              QueryHelper query)
        throws SQLException {
        java.sql.ResultSet result = null;
        try {
            if (provider.getConfigSQL().isAutoRetrievingEnabled()) {
                DatabaseMetaData metadata = provider.getConnection().getMetaData();
                String[] columns = getTableColumns(metadata, query);
                if (columns != null) {
                    result = statement.getGeneratedKeys();
                    if (!isResultSetValid(result.getMetaData(), columns)) {
                        List<String> keys = getTableKeys(metadata, query);
                        Map<String, Map<Object, Integer>> predicates = getKeyPredicates(provider, result, keys);
                        result = getGeneratedResult(provider, predicates, query.getTableIdentifier());
                    }
                }
            }
            if (result == null) {
                result = getDefaultGeneratedResult(provider, query);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e);
        }
        return result;
    }

    // XXX: Method called from sdbcx.CallableStatementSuper.getGeneratedValues()
    // XXX: Method called from sdbcx.PreparedStatementSuper.getGeneratedValues()
    // XXX: Method called from sdbcx.StatementSuper.getGeneratedValues()
    public static final java.sql.ResultSet getGeneratedResult(Provider provider,
                                                              ConnectionSuper connection,
                                                              Statement statement,
                                                              QueryHelper query)
        throws SQLException {
        java.sql.ResultSet result = null;
        try {
            if (provider.getConfigSQL().isAutoRetrievingEnabled()) {
                String[] columns = getTableColumns(connection, query);
                if (columns != null) {
                    result = statement.getGeneratedKeys();
                    if (!isResultSetValid(result.getMetaData(), columns)) {
                        List<String> keys = getTableKeys(connection, query);
                        Map<String, Map<Object, Integer>> predicates = getKeyPredicates(provider, result, keys);
                        result = getGeneratedResult(provider, predicates, query.getTableIdentifier());
                    }
                }
            }
            if (result == null) {
                result = getDefaultGeneratedResult(provider, query);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e);
        }
        return result;
    }

    private static final String[] getTableColumns(DatabaseMetaData metadata,
                                                  QueryHelper query)
        throws SQLException {
        List<String> columns = new ArrayList<>();
        if (query.hasTable()) {
            final int COLUMN_NAME = 4;
            try (ResultSet result = metadata.getColumns(query.getCatalog(),
                                                        query.getSchema(),
                                                        query.getTableIdentifier(),
                                                        null)) {
                while (result.next()) {
                    String column = result.getString(COLUMN_NAME);
                    if (!result.wasNull()) {
                        columns.add(column);
                    }
                }
            } catch (java.sql.SQLException e) {
                throw new SQLException(e);
            }
        }
        return columns.toArray(new String[0]);
    }

    private static final String[] getTableColumns(ConnectionSuper connection,
                                                  QueryHelper query)
        throws SQLException {
        String[] columns = null;
        try {
            XNameAccess tables = connection.getTables();
            if (query.hasTable() && tables.hasByName(query.getTableName())) {
                TableSuper table = (TableSuper) tables.getByName(query.getTableName());
                columns = table.getColumns().getElementNames();
            }
        } catch (NoSuchElementException | WrappedTargetException e) {
            throw new SQLException(e);
        }
        return columns;
    }

    private static boolean isResultSetValid(ResultSetMetaData metadata,
                                            String[] columns)
        throws java.sql.SQLException {
        boolean valid = false;
        int i;
        int count = metadata.getColumnCount();
        for (String column : columns) {
            valid = false;
            for (i = 1; i <= count; i++) {
                if (column.equals(metadata.getColumnName(i))) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                break;
            }
        }
        return valid;
    }

    private static final List<String> getTableKeys(DatabaseMetaData metadata,
                                                   QueryHelper query)
        throws SQLException {
        List<String> keys = new ArrayList<>();
        if (query.hasTable()) {
            final int COLUMN_NAME = 4;
            try (ResultSet result = metadata.getPrimaryKeys(query.getCatalog(),
                                                            query.getSchema(),
                                                            query.getTableIdentifier())) {
                while (result.next()) {
                    String column = result.getString(COLUMN_NAME);
                    if (!result.wasNull()) {
                        keys.add(column);
                    }
                }
            } catch (java.sql.SQLException e) {
                throw new SQLException(e);
            }
        }
        return keys;
    }

    private static final List<String> getTableKeys(ConnectionSuper connection,
                                                   QueryHelper query)
        throws SQLException {
        List<String> keys = new ArrayList<>();
        if (query.hasTable()) {
            try {
                XNameAccess tables = connection.getTables();
                if (tables.hasByName(query.getTableName())) {
                    TableSuper table = (TableSuper) tables.getByName(query.getTableName());
                    XIndexAccess columns = table.getKeys();
                    for (int i = 0; i < columns.getCount(); i++) {
                        Key key = (Key) columns.getByIndex(i);
                        for (String column : key.getColumns().getElementNames()) {
                            keys.add(column);
                        }
                    }
                }
            } catch (NoSuchElementException | WrappedTargetException | IndexOutOfBoundsException e) {
                throw new SQLException(e);
            }
        }
        return keys;
    }

    private static ResultSet getGeneratedResult(Provider provider,
                                                Map<String, Map<Object, Integer>> predicates,
                                                String table)
        throws SQLException {
        try {
            ResultSet resultset = null;
            if (!predicates.isEmpty()) {
                StringJoiner whereClause = new StringJoiner(") OR (", "(", ")");
                Map<Object, Integer> params = new HashMap<>();
                for (Entry<String, Map<Object, Integer>> entry : predicates.entrySet()) {
                    whereClause.add(entry.getKey());
                    params.putAll(entry.getValue());
                }
                // XXX: If we want to follow the UNO API we must return all the columns of the table
                String query = provider.getConfigSQL().getGeneratedKeyQuery(table, whereClause.toString());
                PreparedStatement prepared = provider.getConnection().prepareStatement(query);
                setStatementParams(prepared, params);
                // XXX: The statement will be wrapped in order to be closed correctly when closing the ResultSet.
                resultset = new ResultSetWrapper(prepared);
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private static Map<String, Map<Object, Integer>> getKeyPredicates(Provider provider,
                                                                      ResultSet result,
                                                                      List<String> keys)
        throws java.sql.SQLException {
        Map<String, Map<Object, Integer>> predicates = new HashMap<>();
        if (!keys.isEmpty()) {
            ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            while (result.next()) {
                int index = 0;
                StringJoiner predicate = new StringJoiner(" AND ");
                Map<Object, Integer> params = new HashMap<>();
                for (int i = 1; i <= count; i++) {
                    String name = metadata.getColumnName(i);
                    if (!keys.contains(name) && i < keys.size()) {
                        name = keys.get(index++);
                    }
                    predicate.add(provider.enquoteIdentifier(name) + " = ?");
                    int type = metadata.getColumnType(i);
                    params.put(getResultSetValue(result, i, type), type);
                }
                predicates.put(predicate.toString(), params);
            }
        }
        result.close();
        return predicates;
    }

    private static Object getResultSetValue(ResultSet result,
                                           int index,
                                           int type)
        throws java.sql.SQLException {
        Object value = null;
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                value = result.getString(index);
                break;
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                value = result.getNString(index);
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                value = result.getObject(index, Boolean.class);
                break;
            case Types.TINYINT:
                value = result.getObject(index, Byte.class);
                break;
            case Types.SMALLINT:
                value = result.getObject(index, Short.class);
                break;
            case Types.INTEGER:
                value = result.getObject(index, Integer.class);
                break;
            case Types.BIGINT:
                value = result.getObject(index, Long.class);
                break;
            case Types.FLOAT:
                value = result.getObject(index, Double.class);
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                value = result.getBytes(index);
                break;
            case Types.CLOB:
                value = result.getObject(index, Clob.class);
                break;
            case Types.NCLOB:
                value = result.getObject(index, NClob.class);
                break;
            case Types.BLOB:
                value = result.getObject(index, Blob.class);
                break;
            case Types.ARRAY:
                value = result.getObject(index, Array.class);
                break;
            case Types.DATE:
                value = result.getObject(index, Date.class);
                break;
            case Types.TIME:
                value = result.getObject(index, Time.class);
                break;
            case Types.TIMESTAMP:
                value = result.getObject(index, Timestamp.class);
                break;
            case Types.TIME_WITH_TIMEZONE:
                value = result.getObject(index, OffsetTime.class);
                break;
            case Types.TIMESTAMP_WITH_TIMEZONE:
                value = result.getObject(index, OffsetDateTime.class);
                break;
            default:
                value = result.getObject(index);
        }
        return value;
    }

    private static void setStatementParams(PreparedStatement statement,
                                      Map<Object, Integer> params)
        throws java.sql.SQLException {
        int index = 1;
        for (Entry<Object, Integer> entry : params.entrySet()) {
            Object value = entry.getKey();
            int type = entry.getValue();
            if (value != null) {
                statement.setObject(index, value, type);
            } else {
                statement.setNull(index, type);
            }
            index ++;
        }
    }

    private static ResultSet getDefaultGeneratedResult(Provider provider, QueryHelper query)
        throws java.sql.SQLException {
        String sql = provider.getConfigSQL().getEmptyResultSetQuery(query.getTableIdentifier());
        return provider.getStatement().executeQuery(sql);
    }

}
