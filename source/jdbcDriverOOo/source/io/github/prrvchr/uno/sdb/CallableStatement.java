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

import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.CallableStatementSuper;


public final class CallableStatement
    extends CallableStatementSuper<Connection>
    implements XColumnsSupplier
{

    private static final String m_service = CallableStatement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.CallableStatement",
                                                "com.sun.star.sdbc.CallableStatement",
                                                "com.sun.star.sdbcx.CallableStatement",
                                                "com.sun.star.sdb.PreparedStatement",
                                                "com.sun.star.sdbc.PreparedStatement",
                                                "com.sun.star.sdbcx.PreparedStatement"};

    // The constructor method:
    public CallableStatement(Connection connection,
                             String sql)
    {
        super(m_service, m_services, connection, sql);
        System.out.println("sdb.CallableStatement() 1");
    }

    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        /*try {
            System.out.println("sdb.CallableStatement.getColumns() ************************************");
            java.sql.ResultSetMetaData metadata = _getPreparedStatement().getMetaData();
            return new ColumnContainer((Connection) m_Connection, metadata);
            return null;
        }
        catch (java.sql.SQLException e) {
            // pass
        }*/
        return null;
    }

    @Override
    public XResultSet getResultSet()
        throws SQLException
    {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            if (m_UseBookmarks) {
                RowSet<CallableStatement> rowset = new RowSet<CallableStatement>(m_Connection.getProvider(), m_Connection, getJdbcResultSet(), this, m_Sql);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, rowset.getLogger().getObjectId());
                return rowset;
            }
            else {
                ResultSet<CallableStatement> resultset =  new ResultSet<CallableStatement>(getConnectionInternal(), getJdbcResultSet(), this);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, resultset.getLogger().getObjectId());
                return resultset;
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
