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
package io.github.prrvchr.uno.sdbc;

import java.util.HashMap;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.GeneratedKeys;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.provider.Resources;


public final class PreparedStatement
    extends PreparedStatementBase {

    private static final String SERVICE = PreparedStatement.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.PreparedStatement"};

    // The constructor method:
    public PreparedStatement(Connection connection,
                             String sql)
        throws java.sql.SQLException {
        super(SERVICE, SERVICES, connection, sql);
        registerProperties(new HashMap<PropertyID, PropertyWrapper>());
        System.out.println("sdbc.PreparedStatement() 1: '" + sql + "'");
    }

    @Override
    protected Connection getConnectionInternal() {
        return (Connection) mConnection;
    }

    @Override
    public XResultSet getResultSet()
        throws SQLException {
        java.sql.ResultSet rs = getJdbcResultSet();
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
        ResultSet result = new ResultSet(getConnectionInternal(), rs, this);
        String services = String.join(", ", result.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID,
                           services, result.getLogger().getObjectId());
        return result;
    }

    protected java.sql.ResultSet getGeneratedValues(ConfigSQL config, java.sql.Statement statement)
        throws SQLException {
        return GeneratedKeys.getGeneratedResult(config, statement, mQuery);
    }

}
