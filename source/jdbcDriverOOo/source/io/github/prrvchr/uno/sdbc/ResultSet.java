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

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;


public final class ResultSet
    extends ResultSetBase {
    private static final String SERVICE = ResultSet.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbc.ResultSet"};


    // The constructor method:
    public ResultSet(Connection connection,
                     java.sql.ResultSet result)
        throws SQLException {
        this(connection, result, null, "");
    }

    public ResultSet(ConnectionBase connection,
                     java.sql.ResultSet result)
        throws SQLException {
        this(connection, result, null, "");
    }

    public ResultSet(Connection connection,
                     java.sql.ResultSet result,
                     StatementMain statement)
        throws SQLException {
        this(connection, result, statement, "");
    }

    public ResultSet(ConnectionBase connection,
                     java.sql.ResultSet result,
                     StatementMain statement,
                     String method)
        throws SQLException {
        super(SERVICE, SERVICES, connection, result, statement, false, method);
        registerProperties(new HashMap<PropertyID, PropertyWrapper>());
    }

    @Override
    protected Connection getConnection() {
        return (Connection) mConnection;
    }

    @Override
    protected ConnectionLog getLogger() {
        return mLogger;
    }

    // com.sun.star.sdbc.XCloseable
    @Override
    public synchronized void close() throws SQLException {
        mConnection.closeResultSet(getMethod());
        super.close();
    }

}
