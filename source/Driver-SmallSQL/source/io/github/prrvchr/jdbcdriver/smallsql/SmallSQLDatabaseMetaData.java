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

import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.CustomColumn;
import io.github.prrvchr.uno.sdbc.CustomResultSet;
import io.github.prrvchr.uno.sdbc.CustomResultSetMetaData;
import io.github.prrvchr.uno.sdbc.CustomRowSet;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class SmallSQLDatabaseMetaData
    extends DatabaseMetaDataBase
{

    // The constructor method:
    public SmallSQLDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        super(connection);
        System.out.println("smallsql.SmallSQLDatabaseProvider() 1");
    }


    @Override
    public final XResultSet getTypeInfo()
        throws SQLException
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
    public final XResultSet getTableTypes()
        throws SQLException
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
    public final XResultSet getTables(final Object catalog,
                                      final String schema,
                                      final String table,
                                      final String[] types)
        throws SQLException
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
    protected final CustomRowSet[] _getTablesRowSet(final java.sql.ResultSet result)
        throws java.sql.SQLException
    {
        CustomRowSet[] row = new CustomRowSet[5];
        row[0] = new CustomRowSet(_getCatalogName(result.getString(1)), result.wasNull());
        row[1] = new CustomRowSet(result.getString(2), result.wasNull());
        row[2] = new CustomRowSet(result.getString(3), result.wasNull());
        row[3] = new CustomRowSet(_mapDatabaseTableTypes(result.getString(4)), result.wasNull());
        row[4] = new CustomRowSet(result.getString(5), result.wasNull());
        return row;
    }

    @Override
    public final XResultSet getColumns(final Object catalog,
                                       final  String schema,
                                       final String table,
                                       final String column)
        throws SQLException
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
    public final XResultSet getCatalogs()
        throws SQLException
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
    public final boolean supportsAlterTableWithAddColumn()
        throws SQLException
    {
        System.out.println("smallsql.SmallSQLDatabaseMetaData.supportsAlterTableWithAddColumn()");
        return false;
    }

    @Override
    public final boolean supportsAlterTableWithDropColumn()
        throws SQLException
    {
        System.out.println("smallsql.SmallSQLDatabaseMetaData.supportsAlterTableWithDropColumn()");
        return false;
    }

    @Override
    protected final String _mapDatabaseTableTypes(final String type)
    {
        return type;
    }
    @Override
    protected final String _mapDatabaseTableType(final String schema,
                                                 final String type) {
        return type;
    }

    // XDatabaseMetaData.getCatalogs:
    protected final XResultSet _getCatalogs()
        throws java.sql.SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        System.out.println("smallsql.SmallSQLDatabaseMetaDataBase._getCatalogs()");
        rows.add(_getCatalogsRowSet());
        return new CustomResultSet(_getCatalogsMetadata(), rows);
    }

    protected final CustomRowSet[] _getCatalogsRowSet()
        throws java.sql.SQLException
    {
        CustomRowSet[] row = new CustomRowSet[1];
        String catalog = _getCatalogName(m_Metadata.getConnection().getCatalog());
        System.out.println("smallsql.SmallSQLDatabaseMetaDataBase._getCatalogsRowSet() 1 : " + catalog);
        row[0] = new CustomRowSet(catalog, false);
        return row;
    }

    protected final XResultSetMetaData _getCatalogsMetadata()
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

    private final String _getCatalogName(final String path)
    {
        return Paths.get(path).getFileName().toString();
    }


}