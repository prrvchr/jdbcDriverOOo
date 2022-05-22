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
package io.github.prrvchr.jdbcdriver;

import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.logging.UnoLoggerPool;
import io.github.prrvchr.jdbcdriver.logging.XDBLoggerPool;


public final class DBLoggerPool
    extends WeakBase
    implements XServiceInfo,
               XDBLoggerPool
{

    private static final String m_name = DBLoggerPool.class.getName();
    private static final String[] m_services = {"io.github.prrvchr.jdbcdriver.logging.DBLoggerPool"};
    @SuppressWarnings("unused")
    private final XComponentContext m_xContext;

    // The constructor method:
    public DBLoggerPool(XComponentContext context)
    {
        super();
        m_xContext = context;
        System.out.println("logging.DBLoggerPool() 1: ");
    }

    @Override
    public String[] getLoggerNames() {
        return UnoLoggerPool.getInstance().getLoggerNames();
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_name);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // UNO Service Registration:
    public static XSingleComponentFactory __getComponentFactory(String name)
    {
        XSingleComponentFactory factory = null;
        if (name.equals(m_name))
        {
            factory = Factory.createComponentFactory(DBLoggerPool.class, m_services);
        }
        return factory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey key)
    {
        return Factory.writeRegistryServiceInfo(m_name, m_services, key);
    }


}