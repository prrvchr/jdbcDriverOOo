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
package io.github.prrvchr.uno.sdbcx;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.DriverProvider;
import io.github.prrvchr.uno.helper.Resources;
import io.github.prrvchr.uno.sdb.Table;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbc.ResourceBasedEventLogger;


public class Connection
    extends ConnectionSuper
{

    private static final String m_service = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Connection"};
    private static final boolean m_crawler = false;

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      ResourceBasedEventLogger logger,
                      boolean enhanced)
    {
        super(ctx, m_service, m_services, provider, logger, enhanced, m_crawler);
    }

    protected XStatement _getStatement()
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID, statement.getObjectId());
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql)
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID, statement.getObjectId());
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID, statement.getObjectId());
        return statement;
    }

    public Table getTable(boolean sensitive,
                          String catalog,
                          String schema,
                          String name,
                          String type,
                          String remarks)
    {
        return new Table(this, sensitive, catalog, schema, name, type, remarks);
    }

}