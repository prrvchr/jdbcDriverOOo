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

import java.util.HashMap;

import javax.sql.rowset.CachedRowSet;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;


public final class CallableStatement
    extends CallableStatementSuper {
    private static final String SERVICE = CallableStatement.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.CallableStatement",
                                              "com.sun.star.sdbcx.CallableStatement"};

    // The constructor method:
    public CallableStatement(Connection connection,
                             String sql)
        throws java.sql.SQLException {
        super(SERVICE, SERVICES, connection, sql);
        registerProperties(new HashMap<PropertyID, PropertyWrapper>());
        System.out.println("sdbcx.CallableStatement() 1");
    }

    @Override
    public XResultSet getResultSet()
        throws SQLException {
        try {
            XResultSet resultset = null;
            java.sql.ResultSet rs = getJdbcResultSet();
            Provider provider = getConnectionInternal().getProvider();
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            if (mUseBookmarks && provider.getConfigSQL().useCachedRowSet(rs, mQuery)) {
                CachedRowSet crs = ResultSetHelper.getCachedRowSet(provider, rs, mQuery);
                if (!crs.isReadOnly()) {
                    RowSet rowset = new RowSet(getConnectionInternal(), crs, this);
                    String services = String.join(", ", rowset.getSupportedServiceNames());
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                                       services, rowset.getLogger().getObjectId());
                    resultset = rowset;
                } else {
                    rs = getJdbcResultSet();
                }
            }
            if (resultset == null) {
                ResultSet result =  new ResultSet(getConnectionInternal(), rs, this);
                String services = String.join(", ", result.getSupportedServiceNames());
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                                   services, result.getLogger().getObjectId());
                resultset = result;
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    protected Connection getConnectionInternal() {
        return (Connection) mConnection;
    }


}
