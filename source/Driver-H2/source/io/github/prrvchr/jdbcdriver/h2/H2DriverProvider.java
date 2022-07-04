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

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class H2DriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    private static final String m_subProtocol = "h2";
    private static final String m_logger = ";TRACE_LEVEL_FILE=4";

    // The constructor method:
    public H2DriverProvider()
    {
        System.out.println("h2.H2DriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol(m_subProtocol));
    }

    @Override
    public int getDataType(int type) {
        if (H2DatabaseMetaData.m_dataType.containsKey(type)) {
            return H2DatabaseMetaData.m_dataType.get(type);
        }
        return type;
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
        return "SELECT USER FROM INFORMATION_SCHEMA.USERS";
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
        String property = "Installed/" + getProtocol(m_subProtocol) + ":*/Properties/DriverLoggerLevel/Value";
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