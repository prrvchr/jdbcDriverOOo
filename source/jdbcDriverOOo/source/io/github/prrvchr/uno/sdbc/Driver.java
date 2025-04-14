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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.driver.provider.ApiLevel;
import io.github.prrvchr.driver.provider.DriverProvider;


public final class Driver
    extends DriverBase {

    private static final String m_implementationName = "io.github.prrvchr.jdbcdriver.sdbc.Driver";
    private static final String[] m_services = {"com.sun.star.sdbc.Driver"};
    @SuppressWarnings("unused")
    private static final String[] m_serviceNames = {m_implementationName};

    // The constructor method:
    public Driver(XComponentContext ctx)
        throws Exception {
        super(ctx, "io.github.prrvchr.jdbcDriverOOo.Driver", m_services, false);
        System.out.println("sdbc.Driver() 1");
    }

    @Override
    protected ConnectionBase getConnection(XComponentContext ctx,
                                           DriverProvider provider,
                                           String url,
                                           PropertyValue[] info,
                                           ApiLevel level) {
        return getDefaultConnection(ctx, provider, url, info);
    }

}
