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

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.GeneratedKeys;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.sdbc.PreparedStatementBase;


public abstract class PreparedStatementSuper
    extends PreparedStatementBase {

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.PreparedStatement()
    // XXX: - io.github.prrvchr.uno.sdbcx.PreparedStatement()
    public PreparedStatementSuper(String service,
                                  String[] services,
                                  ConnectionSuper connection,
                                  String sql)
        throws java.sql.SQLException {
        super(service, services, connection, sql);
        System.out.println("sdbc.PreparedStatementSuper() 1: '" + sql + "'");
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws SQLException {
        return super.getJdbcResultSet();
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    @Override
    protected ConnectionSuper getConnectionInternal() {
        return (ConnectionSuper) mConnection;
    }

    @Override
    protected java.sql.ResultSet getGeneratedValues(ConfigSQL config, java.sql.Statement statement)
        throws SQLException {
        return GeneratedKeys.getGeneratedResult(config, getConnectionInternal(), statement, mQuery);
    }

}
