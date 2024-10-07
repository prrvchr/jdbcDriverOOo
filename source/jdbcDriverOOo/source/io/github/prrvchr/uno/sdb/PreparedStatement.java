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

import java.util.HashMap;

import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.resultset.ResultSetHelper;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.PreparedStatementSuper;


public final class PreparedStatement
    extends PreparedStatementSuper
    implements XColumnsSupplier
{

    private static final String m_service = PreparedStatement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.PreparedStatement",
                                                "com.sun.star.sdbc.PreparedStatement",
                                                "com.sun.star.sdbcx.PreparedStatement"};

    // The constructor method:
    public PreparedStatement(Connection connection,
                             String sql)
    {
        super(m_service, m_services, connection, sql);
        registerProperties(new HashMap<String, PropertyWrapper>());
        System.out.println("sdb.PreparedStatement() 1: '" + sql + "'");
    }

    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdb.PreparedStatement.getColumns() ************************************");
        /*try {
            System.out.println("sdb.PreparedStatement.getColumns() ************************************");
            java.sql.ResultSetMetaData metadata = _getPreparedStatement().getMetaData();
            return new ColumnContainer((Connection) m_Connection, metadata);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdb.PreparedStatement.getColumns() 2 **********************************");
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
            Connection connection = getConnectionInternal();
            java.sql.ResultSet result = getJdbcResultSet();
            if (m_UseBookmarks) {
                RowCatalog catalog = null;
                if (ResultSetHelper.isUpdatable(connection.getProvider(), result, catalog, m_Sql)) {
                    RowSet resultset = new RowSet(connection.getProvider(), connection, result, this, catalog, m_Sql.getTable());
                    String services = String.join(", ", resultset.getSupportedServiceNames());
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, services, resultset.getLogger().getObjectId());
                    return resultset;
                }
                else {
                    ResultSet resultset =  new ResultSet(connection, result, this);
                    String services = String.join(", ", resultset.getSupportedServiceNames());
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, services, resultset.getLogger().getObjectId());
                    return resultset;
                }
            }
            else {
                ResultSet resultset =  new ResultSet(connection, result, this);
                String services = String.join(", ", resultset.getSupportedServiceNames());
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, services, resultset.getLogger().getObjectId());
                return resultset;
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected Connection getConnectionInternal() {
        return (Connection) m_Connection;
    }


}
