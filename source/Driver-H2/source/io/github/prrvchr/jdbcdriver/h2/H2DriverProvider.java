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

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbcx.Group;
import io.github.prrvchr.uno.sdbcx.User;


public final class H2DriverProvider
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

    private static final String m_logger = ";TRACE_LEVEL_FILE=4";

    // The constructor method:
    public H2DriverProvider()
    {
        super("h2");
        System.out.println("h2.H2DriverProvider() 1");
    }

    @Override
    public ConnectionBase getConnection(XComponentContext ctx,
                                        ResourceBasedEventLogger logger,
                                        boolean enhanced)
    {
        return new Connection(ctx, this, logger, enhanced);
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


    @Override
    public String getAlterViewQuery()
    {
        return "CREATE OR REPLACE VIEW %s AS %s";
    }

    public String[] getTableTypes()
    {
        String[] types = {"BASE TABLE", "VIEW", "ALIAS", "SYNONYM"};
        return types;
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
    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table)
    {
        String query = "DROP TABLE \"%s\".\"%s\";";
        return String.format(query, schema, table);
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