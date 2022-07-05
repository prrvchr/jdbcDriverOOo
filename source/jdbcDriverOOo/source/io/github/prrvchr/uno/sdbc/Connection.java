/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import com.sun.star.beans.PropertyValue;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbcx.CallableStatement;
import io.github.prrvchr.uno.sdbcx.PreparedStatement;
import io.github.prrvchr.uno.sdbcx.Statement;


public class Connection
    extends ConnectionBase
{

    private static final String m_service = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Connection"};
    private static final boolean m_crawler = false;

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      String url,
                      PropertyValue[] info,
                      boolean enhanced)
    {
        super(ctx, m_service, m_services, provider, url, info, enhanced, m_crawler);
        System.out.println("sdbc.Connection() 1");
    }


    protected XStatement _getStatement()
    {
        System.out.println("sdbc.Connection._getStatement() 1");
        Statement statement = new Statement(this);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql)
    {
        System.out.println("sdbc.Connection._getPreparedStatement() 1: '" + sql + "'");
        PreparedStatement statement = new PreparedStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
    {
        System.out.println("sdbc.Connection._getCallableStatement() 1: '" + sql + "'");
        CallableStatement statement = new CallableStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }


}