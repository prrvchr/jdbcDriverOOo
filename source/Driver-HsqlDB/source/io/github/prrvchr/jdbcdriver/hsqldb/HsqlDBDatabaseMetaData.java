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
package io.github.prrvchr.jdbcdriver.hsqldb;

import java.util.Map;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.CustomRowSet;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class HsqlDBDatabaseMetaData
    extends DatabaseMetaDataBase
{

    private final Map<Integer, Integer> m_dataType = Map.ofEntries(Map.entry(-16, -1),
                                                                   Map.entry(-15, 1),
                                                                   Map.entry(-9, 12),
                                                                   Map.entry(-8, 4),
                                                                   Map.entry(70, 1111),
                                                                   Map.entry(2009, 1111),
                                                                   Map.entry(2011, 2005),
                                                                   Map.entry(2012, 2006),
                                                                   Map.entry(2013, 12),
                                                                   Map.entry(2014, 12));

    // The constructor method:
    public HsqlDBDatabaseMetaData(final XComponentContext ctx,
                                  final ConnectionBase connection)
        throws java.sql.SQLException
    {
        super(ctx, connection);
        System.out.println("hsqldb.DatabaseMetaData() 1");
    }

    //@Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        // FIXME: If we want to be able to display the different schemas correctly
        // FIXME: in Base, we need to disable the catalog in Data Manipulation.
        // FIXME: This setting allows to no longer use ;default_schema=true in the connection URL
        return false;
    }


    @Override
    public final XResultSet getTypeInfo()
        throws SQLException
    {
        try
        {
            System.out.println("hsqldb.DatabaseMetaData.getTypeInfo()");
            return _getTypeInfo();
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
    protected final CustomRowSet[] _getTablesRowSet(final java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[5];
            row[0] = new CustomRowSet(result.getString(1));
            row[1] = new CustomRowSet(result.getString(2));
            row[2] = new CustomRowSet(result.getString(3));
            row[3] = new CustomRowSet(_mapDatabaseTableTypes(result.getString(4)));
            String description = result.getString(5);
            if (description != null) {
                row[4] = new CustomRowSet(description);
            }
            else {
                row[4] = new CustomRowSet("");
                row[4].setNull();
            }
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


    protected final short _mapDatabaseDataType(final short type)
    {
        return (short)_mapDatabaseDataType((int) type);
    }
    protected final int _mapDatabaseDataType(final int type)
    {
        if (m_dataType.containsKey(type))
        {
            System.out.println("hsqldb.DatabaseMetaData._mapDatabaseDataType() Type: " + type);
            return m_dataType.get(type);
        }
        return type;
    }

    @Override
    protected final String _mapDatabaseTableTypes(final String type)
    {
        return type;
    }
    @Override
    protected final String _mapDatabaseTableType(final String schema,
                                                 final String type)
    {
        return type;
    }
}