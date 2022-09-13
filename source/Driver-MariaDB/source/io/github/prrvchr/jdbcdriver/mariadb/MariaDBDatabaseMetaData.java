/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.CustomRowSet;
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
        System.out.println("mariadb.DatabaseMetaData() 1");
    }

    //@Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        boolean value = false;
        try {
            if (m_Connection.isEnhanced()) {
                value = m_Metadata.supportsSchemasInDataManipulation();
            }
        }
        catch (java.sql.SQLException e) {
            System.out.println("mariadb.DatabaseMetaData.supportsSchemasInDataManipulation() ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("mariadb.DatabaseMetaData.supportsSchemasInDataManipulation() 2: " + value);
        return value;
    }


    //@Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        boolean value = false;
        try {
            if (m_Connection.isEnhanced()) {
                value = m_Metadata.supportsCatalogsInDataManipulation();
            }
        }
        catch (java.sql.SQLException e) {
            System.out.println("mariadb.DatabaseMetaData.supportsCatalogsInDataManipulation() ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("mariadb.DatabaseMetaData.supportsCatalogsInDataManipulation() 2: " + value);
        return value;
    }

    @Override
    public final XResultSet getTypeInfo()
        throws SQLException
    {
        try
        {
            System.out.println("h2.DatabaseMetaData.getTypeInfo()");
            return _getTypeInfo();
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
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
            System.out.println("h2.DatabaseMetaData.getTableTypes()");
            return _getTableTypes();
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
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
            System.out.println("h2.DatabaseMetaData.getTables() Catalog: " + _getPattern(catalog) + " - Schema: " + _getPattern(schema) + " - Table: " + table + " - Types: " + _getPattern(types));
            return _getTables(_getPattern(catalog), _getPattern(schema), table, _getPattern(types));
        }
        catch (java.lang.Exception e) {
            System.out.println("h2.DatabaseMetaData.getTables() ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    protected final CustomRowSet[] _getTablesRowSet(final java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[5];
            row[0] = new CustomRowSet(result.getString(1), result.wasNull());
            String schema = result.getString(2);
            row[1] = new CustomRowSet(schema, result.wasNull());
            row[2] = new CustomRowSet(result.getString(3), result.wasNull());
            row[3] = new CustomRowSet(_mapDatabaseTableType(schema, result.getString(4)), result.wasNull());
            row[4] = new CustomRowSet(null, true);
            //System.out.println("h2.DatabaseMetaData._getTablesRowSet() Catalog: " + result.getString(1) + " Schema: " + result.getString(2) + " Table: " + result.getString(3));
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
            System.out.println("h2.DatabaseMetaData.getColumns()");
            return _getColumns(_getPattern(catalog), _getPattern(schema), table, column);
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("h2.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    protected final String _mapDatabaseTableTypes(final String type)
    {
        if (m_tableType.containsKey(type))
        {
            System.out.println("h2.DatabaseMetaData._mapDatabaseTableTypes() Type: " + type);
            return m_tableType.get(type);
        }
        return type;
    }
    @Override
    protected final String _mapDatabaseTableType(final String schema, String type)
    {
        if ("BASE TABLE".equals(type)) {
            type = "INFORMATION_SCHEMA".equals(schema) ? "SYSTEM TABLE" : "TABLE";
        }
        else if ("VIEW".equals(type)) {
            type = "INFORMATION_SCHEMA".equals(schema) ? "SYSTEM TABLE" : "VIEW";
        }
        return type;
    }


}