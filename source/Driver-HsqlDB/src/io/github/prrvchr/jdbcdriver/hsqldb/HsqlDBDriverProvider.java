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
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.logging.LogLevel;
import com.sun.star.logging.XLogger;
import com.sun.star.uno.XComponentContext;

import org.slf4j.bridge.SLF4JBridgeHandler;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.logging.UnoLoggerPool;
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
    public java.sql.Connection getConnection(String url,
                                             PropertyValue[] info)
        throws SQLException
    {
        return DriverManager.getConnection(url, getConnectionProperties(m_properties, info));
    }

    @Override
    public void setSystemProperties()
    {
        final XLogger logger = UnoLoggerPool.getInstance().getNamedLogger("hsqldb.db");
        if (logger.getLevel() != LogLevel.OFF) {
            //System.setProperty("hsqldb.reconfig_logging", "false");
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            Logger.getLogger("").setLevel(Level.FINEST);
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