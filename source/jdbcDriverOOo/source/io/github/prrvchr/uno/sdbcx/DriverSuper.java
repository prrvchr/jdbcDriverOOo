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
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbcx.XDataDefinitionSupplier;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.driver.provider.ApiLevel;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DriverBase;


public abstract class DriverSuper
    extends DriverBase
    implements XDataDefinitionSupplier {

    // The constructor method:
    public DriverSuper(XComponentContext ctx, String service, String[] services)
        throws Exception {
        super(ctx, service, services, true);
        System.out.println("sdbcx.DriverSuper() 1");
    }

    @Override
    protected ConnectionBase getConnection(XComponentContext ctx,
                                           DriverProvider provider,
                                           String url,
                                           PropertyValue[] info,
                                           ApiLevel level) {
        System.out.println("sdbcx.DriverSuper.getConnection() 1 level: " + level.name());
        ConnectionBase connection = null;
        if (level == ApiLevel.COM_SUN_STAR_SDBCX && provider.getAPILevels().contains(level)) {
            connection = new Connection(ctx, provider, url, info);
        } else {
            connection = getDefaultConnection(ctx, provider, url, info);
        }
        return connection;
    }

    // com.sun.star.lang.XDataDefinitionSupplier:
    @Override
    public XTablesSupplier getDataDefinitionByConnection(XConnection connection)
        throws SQLException {
        XTablesSupplier tables = null;
        XServiceInfo service = UnoRuntime.queryInterface(XServiceInfo.class, connection);
        if (service.supportsService("com.sun.star.sdbcx.DatabaseDefinition")) {
            tables = UnoRuntime.queryInterface(XTablesSupplier.class, connection);
        }
        return tables;
    }

    @Override
    public XTablesSupplier getDataDefinitionByURL(String url, PropertyValue[] info)
        throws SQLException {
        if (!acceptsURL(url)) {
            String message = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_URI_SYNTAX_ERROR,
                                                                                       url);
            mLogger.logp(LogLevel.SEVERE, message);
            throw new SQLException(message, this, StandardSQLState.SQL_UNABLE_TO_CONNECT.text(), 0, null);
        }
        XTablesSupplier tables = getDataDefinitionByConnection(connect(url, info));
        return tables;
    }

}
