/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import java.util.List;
import java.util.Set;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;


public final class Connection
    extends ConnectionSuper {

    private static final String SERVICE = Connection.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.Connection",
                                              "com.sun.star.sdbcx.DatabaseDefinition"};

    // The constructor method:
    protected Connection(XComponentContext ctx,
                         Provider provider,
                         String url,
                         PropertyValue[] info,
                         Set<String> properties) {
        super(ctx, SERVICE, SERVICES, provider, url, info, properties);
        System.out.println("sdbcx.Connection() *************************");
    }

    protected Provider getProvider() {
        return super.getProvider();
    }

    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    protected XStatement getStatement() {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement getPreparedStatement(String sql)
        throws SQLException {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement getCallableStatement(String sql)
        throws SQLException {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected TableContainer getTableContainer(List<String> names)
        throws ElementExistException {
        return new TableContainer(this, getProvider().isCaseSensitive(), names);
    }

    @Override
    protected ViewContainer getViewContainer(List<String> names)
        throws ElementExistException {
        return new ViewContainer(this, getProvider().isCaseSensitive(), names);
    }

}