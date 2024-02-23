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
package io.github.prrvchr.jdbcdriver.h2;

import java.sql.Types;
import java.util.Map;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdb.Group;
import io.github.prrvchr.uno.sdb.User;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class H2DriverProvider
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

    private static final String m_logger = ";TRACE_LEVEL_FILE=4";

    // The constructor method:
    public H2DriverProvider()
    {
        super("h2");
        System.out.println("h2.H2DriverProvider() 1");
    }

    @Override
    public String[] getTableTypes()
    {
        System.out.println("h2.H2DriverProvider.getTableTypes() 1");
        //return new String[]{"BASE TABLE", "VIEW"};
        return null;
    }


    @Override
    public int getDataType(int type) {
        if (m_datatype.containsKey(type)) {
            return m_datatype.get(type);
        }
        return type;
    }


    @Override
    public boolean isCaseSensitive(String clazz)
    {
        if (clazz == User.class.getName() || clazz == Group.class.getName()) {
            return false;
        }
        return true;
    }

    public String getTableType(String type)
    {
        if (type.equals("BASE TABLE")) {
            type = "TABLE";
        }
        return type;
    }

    @Override
    public String getUserQuery()
    {
        return "SELECT USER_NAME FROM INFORMATION_SCHEMA.USERS";
    }

    @Override
    public String getGroupQuery()
    {
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ROLES";
    }

    @Override
    public String getDropTableQuery()
    {
        return "DROP TABLE %s IF EXISTS;";
    }

    @Override
    public String getDropViewQuery(String view)
    {
        return String.format("DROP VIEW %s IF EXISTS;", view);
    }

    @Override
    public String getLoggingLevel(XHierarchicalNameAccess driver)
    {
        String level = "-1";
        String property = "Installed/" + getSubProtocol() + ":*/Properties/DriverLoggerLevel/Value";
        try {
            level = (String) driver.getByHierarchicalName(property);
        } catch (NoSuchElementException e) { }
        return level;
    }

    @Override
    public String getConnectionUrl(final String location,
                                   final String level)
    {
        String url = location;
        if (!level.equals("-1")) {
            url += m_logger;
        }
        return url;
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new H2DatabaseMetaData(connection);
    }

}