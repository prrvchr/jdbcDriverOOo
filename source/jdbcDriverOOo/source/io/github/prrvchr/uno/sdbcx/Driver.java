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
package io.github.prrvchr.uno.sdbcx;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.logging.LogLevel;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbcx.XDataDefinitionSupplier;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.ApiLevel;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DriverBase;


public final class Driver
    extends DriverBase
    implements XDataDefinitionSupplier
{
    private static final String m_service = Driver.class.getName();
    private static final String[] m_registry = {"io.github.prrvchr.jdbcdriver.sdbcx.Driver"};
    private static final String[] m_services = {"io.github.prrvchr.jdbcdriver.sdbcx.Driver",
                                                "com.sun.star.sdbcx.Driver",
                                                "com.sun.star.sdbc.Driver"};

    // The constructor method:
    public Driver(XComponentContext ctx)
        throws Exception
    {
        super(ctx, m_service, m_services, true);
        System.out.println("sdbcx.Driver() 1");
    }

    protected ConnectionBase getConnection(XComponentContext ctx,
                                            DriverProvider provider,
                                            String url,
                                            PropertyValue[] info,
                                            ApiLevel level)
    {
        ConnectionBase connection = null;
        switch(level) {
        case COM_SUN_STAR_SDB:
            if (provider.supportService(level)) {
                connection = new io.github.prrvchr.uno.sdb.Connection(ctx, provider, url, info);
                break;
            }
            level = ApiLevel.COM_SUN_STAR_SDBCX;
        case COM_SUN_STAR_SDBCX:
            if (provider.supportService(level)) {
                connection = new Connection(ctx, provider, url, info);
                break;
            }
        case COM_SUN_STAR_SDBC:
            connection = new io.github.prrvchr.uno.sdbc.Connection(ctx, provider, url, info);
            break;
        }
        return connection;
    }


    // UNO Service Registration:
    public static XSingleComponentFactory __getComponentFactory(String name)
    {
        XSingleComponentFactory factory = null;
        if (name.equals(m_service))
        {
            factory = Factory.createComponentFactory(Driver.class, m_registry);
        }
        return factory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey key)
    {
        return Factory.writeRegistryServiceInfo(m_service, m_registry, key);
    }


    // com.sun.star.lang.XDataDefinitionSupplier:
    @Override
    public XTablesSupplier getDataDefinitionByConnection(XConnection connection)
    throws SQLException
    {
        XTablesSupplier tables = (XTablesSupplier) UnoRuntime.queryInterface(XTablesSupplier.class, connection);
        System.out.println("Driver.getDataDefinitionByConnection()");
        return tables;
    }

    @Override
    public XTablesSupplier getDataDefinitionByURL(String url, PropertyValue[] info)
    throws SQLException
    {
        if (!acceptsURL(url)) {
            String message = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_URI_SYNTAX_ERROR, url);
            m_logger.logp(LogLevel.SEVERE, message);
            throw new SQLException(message, this, StandardSQLState.SQL_UNABLE_TO_CONNECT.text(), 0, null);
        }
        XTablesSupplier tables = getDataDefinitionByConnection(connect(url, info));
        System.out.println("Driver.getDataDefinitionByURL(");
        return tables;
    }


}
