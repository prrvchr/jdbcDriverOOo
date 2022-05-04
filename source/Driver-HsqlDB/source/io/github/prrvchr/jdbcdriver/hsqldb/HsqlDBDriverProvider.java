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

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;

import org.slf4j.bridge.SLF4JBridgeHandler;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSet;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public final class HsqlDBDriverProvider
    implements DriverProvider
{

    private static final String m_subProtocol = "hsqldb";
    private static final boolean m_warnings = true;
    private List<String> m_properties = List.of("user", "password");
    private final Map<String, String> m_sqllogger = Map.ofEntries(Map.entry("0", "1"),
                                                                  Map.entry("1", "1"),
                                                                  Map.entry("2", "1"),
                                                                  Map.entry("3", "2"),
                                                                  Map.entry("4", "2"),
                                                                  Map.entry("5", "3"),
                                                                  Map.entry("6", "3"),
                                                                  Map.entry("7", "3"));

    // The constructor method:
    public HsqlDBDriverProvider()
    {
        System.out.println("hsqldb.HsqlDBDriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol(m_subProtocol));
    }

    @Override
    public final boolean supportWarningsSupplier() {
        return m_warnings;
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
    public java.sql.Connection getConnection(final String level,
                                             final String url,
                                             final PropertyValue[] info)
        throws java.sql.SQLException
    {
        String location = url;
        if (!level.equals("-1")) {
            location += ";hsqldb.sqllog=" + m_sqllogger.get(level);
        }
        return DriverManager.getConnection(location, getConnectionProperties(m_properties, info));
    }

    @Override
    public void setSystemProperties(String level)
        throws SQLException
    {
        System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 1");
        if (!level.equals("-1")) {
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 2");
            System.setProperty("hsqldb.reconfig_logging", "false");
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            Logger.getLogger("").setLevel(Level.FINEST);
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 3");
        }
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final XComponentContext context,
                                                          final ConnectionBase connection,
                                                          final java.sql.DatabaseMetaData metadata,
                                                          final PropertyValue[] info,
                                                          final String url)
    {
        return new HsqlDBDatabaseMetaData(context, this, connection, metadata, info, url);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final java.sql.ResultSet resultset,
                                            final PropertyValue[] info)
    {
        return new ResultSet(context, this, resultset, info);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final StatementMain statement,
                                            final java.sql.ResultSet resultset,
                                            final PropertyValue[] info)
    {
        return new ResultSet(context, this, statement, resultset, info);
    }


}