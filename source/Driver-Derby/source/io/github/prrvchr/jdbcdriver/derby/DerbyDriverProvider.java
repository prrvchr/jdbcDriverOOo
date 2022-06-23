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
package io.github.prrvchr.jdbcdriver.derby;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class DerbyDriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    private static final String m_subProtocol = "derby";
    private final Map<String, String> m_sqllogger = Map.ofEntries(Map.entry("0", "50000"),
                                                                  Map.entry("1", "40000"),
                                                                  Map.entry("2", "40000"),
                                                                  Map.entry("3", "30000"),
                                                                  Map.entry("4", "30000"),
                                                                  Map.entry("5", "20000"),
                                                                  Map.entry("6", "20000"),
                                                                  Map.entry("7", "0"));

    // The constructor method:
    public DerbyDriverProvider()
    {
        System.out.println("derby.DerbyDriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol(m_subProtocol));
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
    public java.sql.Connection getConnection(final String url,
                                             final PropertyValue[] info,
                                             final String level)
        throws java.sql.SQLException
    {
        return DriverManager.getConnection(url, getConnectionProperties(m_properties, info));
    }

    @Override
    public void setSystemProperties(String level)
    {
        if (!level.equals("-1")) {
            final String value = "io.github.prrvchr.jdbcdriver.derby.DerbyLoggerBridge.bridge";
            System.setProperty("derby.stream.error.method", value);
            System.setProperty("derby.stream.error.logSeverityLevel", m_sqllogger.get(level));
        }
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws SQLException
    {
        return new DerbyDatabaseMetaData(connection);
    }


}