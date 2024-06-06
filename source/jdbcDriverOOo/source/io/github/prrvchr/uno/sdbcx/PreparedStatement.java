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

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.jdbcdriver.Resources;


public final class PreparedStatement
    extends PreparedStatementSuper<Connection>
{
    private static final String m_service = PreparedStatement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.PreparedStatement",
                                                "com.sun.star.sdbcx.PreparedStatement"};

    // The constructor method:
    public PreparedStatement(Connection connection,
                             String sql)
    {
        super(m_service, m_services, connection, sql);
        System.out.println("sdbcx.PreparedStatement() 1: '" + sql + "'");
    }

    @Override
    public XResultSet getResultSet()
        throws SQLException
    {
        java.sql.ResultSet result = getJdbcResultSet();
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
        if (m_UseBookmarks) {
            RowSet<PreparedStatement> rowset = new RowSet<PreparedStatement>(m_Connection.getProvider(), m_Connection, result, this, m_Sql);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, rowset.getLogger().getObjectId());
            return rowset;
        }
        else {
            ResultSet<PreparedStatement> resultset =  new ResultSet<PreparedStatement>(m_Connection, result, this);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, resultset.getLogger().getObjectId());
            return resultset;
        }
    }


}
