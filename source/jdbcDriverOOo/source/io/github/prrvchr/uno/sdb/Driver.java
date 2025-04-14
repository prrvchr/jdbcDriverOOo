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
package io.github.prrvchr.uno.sdb;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.driver.provider.ApiLevel;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbcx.DriverSuper;


public final class Driver
     extends DriverSuper {

    private static final String m_implementationName = "io.github.prrvchr.jdbcdriver.sdb.Driver";
    private static final String[] m_services = {"com.sun.star.sdbc.Driver", "com.sun.star.sdbcx.Driver"};
    @SuppressWarnings("unused")
    private static final String[] m_serviceNames = {m_implementationName};

    // The constructor method:
    public Driver(XComponentContext ctx)
        throws Exception {
        super(ctx, "io.github.prrvchr.jdbcDriverOOo.Driver", m_services);
        System.out.println("sdb.Driver() 1");
    }
 
    @Override
    protected ConnectionBase getConnection(XComponentContext ctx,
                                           DriverProvider provider,
                                           String url,
                                           PropertyValue[] info,
                                           ApiLevel level) {
        ConnectionBase connection = null;
        if (level == ApiLevel.COM_SUN_STAR_SDB && provider.getAPILevels().contains(level)) {
            System.out.println("sdb.Driver.getConnection() 1 level: " + level.name());
            connection = new Connection(ctx, provider, url, info);
        } else {
            System.out.println("sdb.Driver.getConnection() 2 level: " + level.name());
            connection = super.getConnection(ctx, provider, url, info, level);
        }
        return connection;
    }

}
