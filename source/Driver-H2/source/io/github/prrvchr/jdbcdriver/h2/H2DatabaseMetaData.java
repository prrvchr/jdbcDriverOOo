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
package io.github.prrvchr.jdbcdriver.h2;

import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class H2DatabaseMetaData
    extends DatabaseMetaDataBase
{

    private final Map<String, String> m_tableType = Map.ofEntries(Map.entry("BASE TABLE", "TABLE"));
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
    public H2DatabaseMetaData(final XComponentContext ctx,
                              final DriverProvider provider,
                              final XConnection connection,
                              final java.sql.DatabaseMetaData metadata,
                              final PropertyValue[] info,
                              final String url)
    {
        super(ctx, provider, connection, metadata, info, url);
        System.out.println("h2.DatabaseMetaData() 1");
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


    protected final short _mapDatabaseDataType(final short type)
    {
        return (short)_mapDatabaseDataType((int) type);
    }
    protected final int _mapDatabaseDataType(final int type)
    {
        if (m_dataType.containsKey(type))
        {
            System.out.println("h2.DatabaseMetaData._mapDatabaseDataType() Type: " + type);
            return m_dataType.get(type);
        }
        return type;
    }

    protected final String _mapDatabaseTableTypes(final String type)
    {
        if (m_tableType.containsKey(type))
            return m_tableType.get(type);
        return type;
    }


}