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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.Resources;


public final class Connection
    extends ConnectionBase {

    private static final String SERVICE = Connection.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.Connection"};

    // The constructor method:
    protected Connection(XComponentContext ctx,
                         DriverProvider provider,
                         String url,
                         PropertyValue[] info) {
        super(ctx, SERVICE, SERVICES, provider, url, info);
        System.out.println("sdbc.Connection() *************************");
    }

    protected DriverProvider getProvider() {
        return super.getProvider();
    }
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    protected XStatement _getStatement() {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql) {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql) {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

}