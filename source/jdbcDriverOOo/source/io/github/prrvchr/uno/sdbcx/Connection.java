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

import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class Connection
    extends ConnectionSuper
{

    private static final String m_service = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Connection"};

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      String url,
                      PropertyValue[] info,
                      ResourceBasedEventLogger logger,
                      boolean enhanced,
                      boolean showsystem,
                      boolean usebookmark)
    {
        super(ctx, m_service, m_services, provider, url, info, logger, enhanced, showsystem, usebookmark);
        System.out.println("sdbcx.Connection() *************************");
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
        throws SQLException
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID, statement.getObjectId());
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
        throws SQLException
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID, statement.getObjectId());
        return statement;
    }

    protected TableContainer _getTableContainer(List<String> names)
        throws ElementExistException
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLECONTAINER);
        TableContainer tables = new TableContainer(this, getProvider().isCaseSensitive(null), names);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLECONTAINER_ID, tables.getObjectId());
        return tables;
    }

}