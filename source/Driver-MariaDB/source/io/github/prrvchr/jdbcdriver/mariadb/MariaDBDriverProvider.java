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

import java.sql.Types;
import java.util.Map;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class MariaDBDriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    protected static final Map<Integer, Integer> m_datatype = Map.ofEntries(Map.entry(Types.LONGNVARCHAR, Types.LONGVARCHAR),
                                                                            Map.entry(Types.NCHAR, Types.CHAR),
                                                                            Map.entry(Types.NVARCHAR, Types.VARCHAR),
                                                                            Map.entry(Types.ROWID, Types.JAVA_OBJECT),
                                                                            Map.entry(Types.DATALINK, Types.VARCHAR),
                                                                            Map.entry(Types.SQLXML, Types.VARCHAR),
                                                                            Map.entry(Types.NCLOB, Types.CLOB),
                                                                            Map.entry(Types.REF_CURSOR, Types.REF),
                                                                            Map.entry(Types.TIME_WITH_TIMEZONE, Types.VARCHAR),
                                                                            Map.entry(Types.TIMESTAMP_WITH_TIMEZONE, Types.VARCHAR));

    // The constructor method:
    public MariaDBDriverProvider()
    {
        super("mariadb");
    }

    @Override
    public int getDataType(int type) {
        if (m_datatype.containsKey(type)) {
            return m_datatype.get(type);
        }
        return type;
    }

    @Override
    public String[] getViewTypes(final boolean showsystem)
    {
        System.out.println("mariadb.MariaDBDriverProvider.getViewTypes() 1");
        if (showsystem) {
            return new String[]{"VIEW", "SYSTEM VIEW"};
        }
        else {
            return new String[]{"VIEW"};
        }
    }

    @Override
    public String getUserQuery()
    {
        return "SELECT user FROM mysql.user WHERE is_role='N';";
    }

    @Override
    public String getGroupQuery()
    {
        return "SELECT user FROM mysql.user WHERE is_role='Y';";
    }

    @Override
    public String getGroupUsersQuery()
    {
        return "SELECT user FROM mysql.roles_mapping WHERE role=?;";
    }

    @Override
    public String getUserGroupsQuery()
    {
        //TODO: We use recursion to find privileges inherited from roles,
        //TODO: we need to filter recursive entries (even role and user)
        return "SELECT role FROM mysql.roles_mapping WHERE user=? AND user!=role;";
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new MariaDBDatabaseMetaData(connection);
    }

}