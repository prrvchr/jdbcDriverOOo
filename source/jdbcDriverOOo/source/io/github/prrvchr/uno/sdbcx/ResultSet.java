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

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdbc.StatementMain;


public final class ResultSet<S extends StatementMain<?, ?>>
    extends ResultSetSuper<ConnectionSuper, S>
{
    private static final String m_service = ResultSet.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.ResultSet",
                                                "com.sun.star.sdbcx.ResultSet"};

    // The constructor method:
    public ResultSet(ConnectionSuper connection,
                     java.sql.ResultSet resultset)
        throws SQLException
    {
        this(connection, resultset, null);
    }

    public ResultSet(ConnectionSuper connection,
                     java.sql.ResultSet resultset,
                     S statement)
        throws SQLException
    {
        super(m_service, m_services, connection, resultset, statement, false, false);
        System.out.println("sdbcx.ResultSet() 1");
    }

}
