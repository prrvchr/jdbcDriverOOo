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
package io.github.prrvchr.jdbcdriver.hsqldb;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.jdbcdriver.CustomColumn;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class HsqlDBDatabaseMetaData
    extends DatabaseMetaDataBase
{

    // The constructor method:
    public HsqlDBDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        super(connection);
    }

    /* //@Override
    public boolean supportsCatalogsInDataManipulation()
        throws SQLException
    {
        // FIXME: With HsqlDB, if we want to be able to display the different
        // FIXME: schemas correctly in Base, we need to disable the catalog in Data Manipulation.
        // FIXME: This setting allows to no longer use ;default_schema=true in the connection URL
        return true;
    }*/


    @Override
    public final XResultSet getTypeInfo()
        throws SQLException
    {
        try
        {
            System.out.println("hsqldb.DatabaseMetaData.getTypeInfo()");
            return _getTypeInfo();
        }
        catch (java.lang.Exception e) {
            System.out.println("hsqldb.DatabaseMetaData ********************************* ERROR: " + e);
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
            System.out.println("hsqldb.DatabaseMetaData.getTableTypes()");
            return _getTableTypes();
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("hsqldb.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("hsqldb.DatabaseMetaData ********************************* ERROR: " + e);
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
            System.out.println("hsqldb.DatabaseMetaData.getTables() Catalog: " + _getPattern(catalog) + " - Schema: " + _getPattern(schema) + " - Table: " + table + " - Types: " + _getPattern(types));
            return _getTables(_getPattern(catalog), _getPattern(schema), table, _getPattern(types));
        }
        catch (java.lang.Exception e) {
            System.out.println("hsqldb.DatabaseMetaData.getTables() ********************************* ERROR: " + e);
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
        row[0] = new CustomColumn(result.getString(1), result.wasNull());
        row[1] = new CustomColumn(result.getString(2), result.wasNull());
        row[2] = new CustomColumn(result.getString(3), result.wasNull());
        row[3] = new CustomColumn(_mapDatabaseTableTypes(result.getString(4)), result.wasNull());
        row[4] = new CustomColumn(result.getString(5), result.wasNull());
        //System.out.println("hsqldb.DatabaseMetaData._getTablesRowSet() Catalog: " + result.getString(1) + " Schema: " + result.getString(2) + " Table: " + result.getString(3));
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
            System.out.println("hsqldb.DatabaseMetaData.getColumns()");
            return _getColumns(_getPattern(catalog), _getPattern(schema), table, column);
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("hsqldb.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("hsqldb.DatabaseMetaData ********************************* ERROR: " + e);
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
        return type;
    }

    @Override
    protected final String _mapDatabaseTableTypes(final String catalog,
                                                  final String schema,
                                                  final String type)
    {
        return type;
    }
}