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
package io.github.prrvchr.jdbcdriver.mariadb;

import java.util.Map;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.CustomColumn;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class MariaDBDatabaseMetaData
    extends DatabaseMetaDataBase
{

    private final Map<String, String> m_tableType = Map.ofEntries(Map.entry("BASE TABLE", "TABLE"));

    // The constructor method:
    public MariaDBDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        super(connection);
    }

    @Override
    public final XResultSet getTypeInfo()
        throws SQLException
    {
        try
        {
            System.out.println("mariadb.DatabaseMetaData.getTypeInfo()");
            return _getTypeInfo();
        }
        catch (java.lang.Exception e) {
            System.out.println("mariadb.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public final XResultSet getTableTypes()
        throws SQLException
    {
        try
        {
            System.out.println("mariadb.DatabaseMetaData.getTableTypes()");
            return _getTableTypes();
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public final XResultSet getTables(final Object catalog,
                                      final String schema,
                                      final String table,
                                      final String[] types)
        throws SQLException
    {
        try
        {
            System.out.println("mariadb.DatabaseMetaData.getTables() Catalog: " + _getPattern(catalog) + " - Schema: " + _getPattern(schema) + " - Table: " + table + " - Types: " + _getPattern(types));
            return _getTables(_getPattern(catalog), _getPattern(schema), table, _getPattern(types));
        }
        catch (java.lang.Exception e) {
            System.out.println("mariadb.DatabaseMetaData.getTables() ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    protected final CustomColumn[] _getTablesRow(final java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomColumn[] row = new CustomColumn[5];
            String catalog = result.getString(1);
            row[0] = new CustomColumn(catalog, result.wasNull());
            String schema = result.getString(2);
            row[1] = new CustomColumn(schema, result.wasNull());
            row[2] = new CustomColumn(result.getString(3), result.wasNull());
            row[3] = new CustomColumn(_mapDatabaseTableTypes(catalog, schema, result.getString(4)), result.wasNull());
            row[4] = new CustomColumn(null, true);
            System.out.println("mariadb.DatabaseMetaData._getTablesRowSet() Catalog: " + catalog + " Schema: " + schema + " Table: " + result.getString(3));
            return row;
        }

    @Override
    public final XResultSet getColumns(final Object catalog,
                                       final String schema,
                                       final String table,
                                       final String column)
        throws SQLException
    {
        try
        {
            System.out.println("mariadb.DatabaseMetaData.getColumns()");
            return _getColumns(_getPattern(catalog), _getPattern(schema), table, column);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    protected final String _mapDatabaseTableTypes(final String type)
    {
        if (m_tableType.containsKey(type))
        {
            System.out.println("mariadb.DatabaseMetaData._mapDatabaseTableTypes() Type: " + type);
            return m_tableType.get(type);
        }
        return type;
    }
    @Override
    protected final String _mapDatabaseTableTypes(final String catalog,
                                                  final String schema,
                                                  String type)
    {
        System.out.println("mariadb.DatabaseMetaData._mapDatabaseTableTypes() Catalog: " + catalog + " - Schema: " + schema + " - Type: " + type);
        if ("BASE TABLE".equals(type)) {
            type = "INFORMATION_SCHEMA".equals(catalog) ||
                   "mysql".equals(catalog) ||
                   "performance_schema".equals(catalog) ||
                   "sys".equals(catalog) ? "SYSTEM TABLE" : "TABLE";
        }
        else if ("VIEW".equals(type)) {
            type = "INFORMATION_SCHEMA".equals(catalog) ||
                   "mysql".equals(catalog) ||
                   "performance_schema".equals(catalog) ||
                   "sys".equals(catalog)? "SYSTEM TABLE" : "VIEW";
        }
        return type;
    }


}