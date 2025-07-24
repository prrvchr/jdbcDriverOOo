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
package io.github.prrvchr.uno.sdb;

import java.util.HashMap;

import javax.sql.rowset.CachedRowSet;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbcx.StatementSuper;


public final class Statement
    extends StatementSuper {
    
    private static final String SERVICE = Statement.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.Statement",
                                              "com.sun.star.sdbcx.Statement"};

    // The constructor method:
    public Statement(Connection connection) {
        super(SERVICE, SERVICES, connection);
        registerProperties(new HashMap<String, PropertyWrapper>());
        System.out.println("sdb.Statement() 1");
    }


    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    @Override
    public XResultSet getResultSet()
        throws SQLException {
        XResultSet resultset = null;
        java.sql.ResultSet rs = getJdbcResultSet();
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
        if (mUseBookmarks && getConnectionInternal().getProvider().getConfigSQL().useCachedRowSet(rs, mQuery)) {
            CachedRowSet crs = ResultSetHelper.getCachedRowSet(rs);
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
    }

    @Override
    protected Connection getConnectionInternal() {
        return (Connection) mConnection;
    }


}
