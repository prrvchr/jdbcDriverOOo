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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.ConnectionService;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;


public final class Driver
    extends DriverBase
{
    private static final String m_service = Driver.class.getName();
    private static final String[] m_registry = {"io.github.prrvchr.jdbcdriver.sdbc.Driver"};
    private static final String[] m_services = {"io.github.prrvchr.jdbcdriver.sdbc.Driver",
                                                "com.sun.star.sdbc.Driver"};

    // The constructor method:
    public Driver(XComponentContext ctx)
    {
        super(ctx, m_service, m_services, false);
        System.out.println("sdbc.Driver() 1");
    }

    protected ConnectionBase _getConnection(XComponentContext ctx,
                                            DriverProvider provider,
                                            ResourceBasedEventLogger logger,
                                            boolean enhanced,
                                            boolean showsystem,
                                            boolean usebookmark,
                                            ConnectionService service)
    {
        ConnectionBase connection = null;
        switch(service) {
        case CSS_SDBC_CONNECTION:
            connection = new Connection(ctx, provider, logger, enhanced, showsystem, usebookmark);
            break;
        case CSS_SDBCX_CONNECTION:
            connection = new io.github.prrvchr.uno.sdbcx.Connection(ctx, provider, logger, enhanced, showsystem, usebookmark);
            break;
        case CSS_SDB_CONNECTION:
            connection = new io.github.prrvchr.uno.sdb.Connection(ctx, provider, logger, enhanced, showsystem, usebookmark);
            break;
        default:
            connection = new Connection(ctx, provider, logger, enhanced, showsystem, usebookmark);
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


}
