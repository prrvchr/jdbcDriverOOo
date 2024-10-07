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
package io.github.prrvchr.uno.sdbc;

import java.util.HashMap;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public final class ResultSet
    extends ResultSetBase
{
    private static final String m_service = ResultSet.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.ResultSet"};


    // The constructor method:
    public ResultSet(Connection connection,
                     java.sql.ResultSet resultset)
        throws SQLException
    {
        this(connection, resultset, null);
    }

    public ResultSet(Connection connection,
                     java.sql.ResultSet resultset,
                     StatementMain statement)
        throws SQLException
    {
        super(m_service, m_services, connection, resultset, statement);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }

    public ResultSet(ConnectionBase connection,
                     java.sql.ResultSet resultset)
        throws SQLException
    {
        super(m_service, m_services, connection, resultset, null);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }

    @Override
    protected Connection getConnection() {
        return (Connection) m_Connection;
    }

    @Override
    protected ConnectionLog getLogger() {
        return m_logger;
    }

}
