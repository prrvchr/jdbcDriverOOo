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
package io.github.prrvchr.jdbcdriver.sqlite;

import java.util.Map;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class SQLiteDriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    protected static final Map<Integer, Integer> m_datatype = Map.ofEntries(Map.entry(-16, -1),
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
    public SQLiteDriverProvider()
    {
        super("sqlite");
        System.out.println("sqlite.SQLiteDriverProvider() 1");
    }

    @Override
    public int getDataType(int type) {
        if (m_datatype.containsKey(type)) {
            return m_datatype.get(type);
        }
        return type;
    }

    @Override
    public String getViewQuery()
    {
        return "SELECT sql, 'NONE' FROM sqlite_master WHERE type='view' AND name=?";
    }

    @Override
    public String getViewCommand(String sql)
    {
        int position = sql.indexOf(" AS ");
        if (position >= 0) {
            sql = sql.substring(position+ 4, sql.length());
        }
        return sql;
    }

    @Override
    public String[] getAlterViewQueries(String view,
                                        String command)
    {
        String drop = String.format("DROP VIEW %s", view);
        String create = String.format("CREATE VIEW %s AS %s", view, command);
        String[] queries = {drop, create};
        return queries;
    }

    public String[] getTableTypes()
    {
        String[] types = {"GLOBAL TEMPORARY", "SYSTEM TABLE", "TABLE", "VIEW"};
        return types;
    }

    public String getTableType(String type)
    {
        return type;
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new SQLiteDatabaseMetaData(connection);
    }

    @Override
    public boolean supportCreateTableKeyParts()
    {
        return false;
    }


}