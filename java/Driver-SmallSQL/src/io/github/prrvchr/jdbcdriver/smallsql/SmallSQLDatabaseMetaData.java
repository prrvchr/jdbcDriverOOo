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
package io.github.prrvchr.jdbcdriver.smallsql;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.CustomColumn;
import io.github.prrvchr.uno.sdbc.CustomResultSet;
import io.github.prrvchr.uno.sdbc.CustomResultSetMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.CustomRowSet;


public class SmallSQLDatabaseMetaData
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
    public SmallSQLDatabaseMetaData(XComponentContext ctx,
                                    DriverProvider provider,
                                    XConnection connection,
                                    java.sql.DatabaseMetaData metadata,
                                    PropertyValue[] info,
                                    String url)
    {
        super(ctx, provider, connection, metadata, info, url);
        System.out.println("smallsql.SmallSQLDatabaseProvider() 1");
    }


    @Override
    public XResultSet getTypeInfo() throws SQLException
    {
        try
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider.getTypeInfo()");
            return _getTypeInfo();
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getTableTypes() throws SQLException
    {
        try
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider.getTableTypes()");
            return _getTableTypes();
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getTables(Object catalog, String schema, String table, String[] types) throws SQLException
    {
        try
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider.getTables() Catalog: " + _getPattern(catalog) + " - Schema: " + _getPattern(schema) + " - Table: " + table + " - Types: " + _getPattern(types));
            return _getTables(_getPattern(catalog), _getPattern(schema), table, _getPattern(types));
        }
        catch (java.lang.Exception e) {
            System.out.println("smallsql.SmallSQLDatabaseProvider.getTables() ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    protected CustomRowSet[] _getTablesRowSet(java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[5];
            row[0] = new CustomRowSet(_getCatalogName(result.getString(1)));
            row[1] = new CustomRowSet(result.getString(2));
            row[2] = new CustomRowSet(result.getString(3));
            row[3] = new CustomRowSet(_mapDatabaseTableTypes(result.getString(4)));
            row[4] = new CustomRowSet(result.getString(5));
            return row;
        }

    @Override
    public XResultSet getColumns(Object catalog, String schema, String table, String column) throws SQLException
    {
        try
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider.getColumns()");
            return _getColumns(_getPattern(catalog), _getPattern(schema), table, column);
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("smallsql.SmallSQLDatabaseProvider ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getCatalogs() throws SQLException
    {
        try
        {
            return _getCatalogs();
        }
        catch (java.lang.Exception e) {
            System.out.println("smallsql.SmallSQLDatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        System.out.println("smallsql.SmallSQLDatabaseMetaData.supportsAlterTableWithAddColumn()");
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        System.out.println("smallsql.SmallSQLDatabaseMetaData.supportsAlterTableWithDropColumn()");
        return false;
    }


    protected short _mapDatabaseDataType(short type)
    {
        return (short)_mapDatabaseDataType((int) type);
    }
    protected int _mapDatabaseDataType(int type)
    {
        if (m_dataType.containsKey(type))
        {
            System.out.println("smallsql.SmallSQLDatabaseProvider._mapDatabaseDataType() Type: " + type);
            type = m_dataType.get(type);
        }
        return type;
    }

    protected String _mapDatabaseTableTypes(String type)
    {
        return type;
    }

    // XDatabaseMetaData.getCatalogs:
    protected XResultSet _getCatalogs()
        throws java.sql.SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        System.out.println("smallsql.SmallSQLDatabaseMetaDataBase._getCatalogs()");
        rows.add(_getCatalogsRowSet());
        return new CustomResultSet(_getCatalogsMetadata(), rows);
    }

    protected CustomRowSet[] _getCatalogsRowSet()
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[1];
            String catalog = _getCatalogName();
            System.out.println("smallsql.SmallSQLDatabaseMetaDataBase._getCatalogsRowSet() 1 : " + catalog);
            row[0] = new CustomRowSet(catalog);
            return row;
        }

    protected XResultSetMetaData _getCatalogsMetadata()
    {
        CustomColumn[] columns = new CustomColumn[1];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TABLE_CAT");
        columns[0].setNullable(ColumnValue.NO_NULLS);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        return new CustomResultSetMetaData(columns);
    }

    private String _getCatalogName()
        throws java.sql.SQLException
    {
        return _getCatalogName(m_Metadata.getConnection().getCatalog());
    }

    private String _getCatalogName(String path)
    {
        return Paths.get(path).getFileName().toString();
    }


}