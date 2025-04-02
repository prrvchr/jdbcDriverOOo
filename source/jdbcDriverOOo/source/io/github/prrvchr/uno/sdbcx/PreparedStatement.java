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

import java.util.HashMap;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.resultset.ResultSetHelper;
import io.github.prrvchr.driver.rowset.RowCatalog;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class PreparedStatement
    extends PreparedStatementSuper {
    private static final String SERVICE = PreparedStatement.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.PreparedStatement",
                                              "com.sun.star.sdbcx.PreparedStatement"};

    // The constructor method:
    public PreparedStatement(Connection connection,
                             String sql) {
        super(SERVICE, SERVICES, connection, sql);
        registerProperties(new HashMap<String, PropertyWrapper>());
        System.out.println("sdbcx.PreparedStatement() 1: '" + sql + "'");
    }


    @Override
    public XResultSet getResultSet()
        throws SQLException {
        XResultSet resultset = null;
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            Connection connection = getConnectionInternal();
            java.sql.ResultSet result = getJdbcResultSet();
            if (mUseBookmarks) {
                RowCatalog catalog = null;
                if (ResultSetHelper.isUpdatable(connection.getProvider(), result, catalog, mSql)) {
                    RowSet rowset = new RowSet(connection.getProvider(), connection, result,
                                               this, catalog, mSql.getTable());
                    String services = String.join(", ", rowset.getSupportedServiceNames());
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                                       services, rowset.getLogger().getObjectId());
                    resultset = rowset;
                } else {
                    ResultSet rowset =  new ResultSet(connection, result, this);
                    String services = String.join(", ", rowset.getSupportedServiceNames());
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                                       services, rowset.getLogger().getObjectId());
                    resultset = rowset;
                }
            } else {
                ResultSet rowset =  new ResultSet(connection, result, this);
                String services = String.join(", ", rowset.getSupportedServiceNames());
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                                   services, rowset.getLogger().getObjectId());
                resultset = rowset;
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return resultset;
    }

    @Override
    protected Connection getConnectionInternal() {
        return (Connection) mConnection;
    }


}
