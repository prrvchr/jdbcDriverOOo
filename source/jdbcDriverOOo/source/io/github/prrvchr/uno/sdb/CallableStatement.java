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
package io.github.prrvchr.uno.sdb;

import java.sql.SQLException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.CallableStatementSuper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbcx.ColumnsSupplier;


public class CallableStatement
extends CallableStatementSuper
implements XColumnsSupplier
{
    private static final String m_name = CallableStatement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.CallableStatement",
                                                "com.sun.star.sdbc.CallableStatement",
                                                "com.sun.star.sdbcx.CallableStatement"};

    // The constructor method:
    public CallableStatement(XComponentContext context,
                             DriverProvider provider,
                             ConnectionBase xConnection,
                             java.sql.Connection connection,
                             String sql,
                             PropertyValue[] info)
    throws SQLException
    {
        super(context, m_name, m_services, provider, xConnection, connection, sql, info);
        System.out.println("sdb.CallableStatement() 1");
    }


    protected XResultSet _getResultSet(XComponentContext ctx,
                                       java.sql.ResultSet resultset)
    throws java.sql.SQLException
    {
        return new ResultSet(ctx, m_provider, this, resultset, m_info);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        try
        {
            java.sql.ResultSetMetaData metadata = _getStatement().getMetaData();
            return ColumnsSupplier.getColumns(metadata);
        }
        catch (java.sql.SQLException e)
        {
            // pass
        }
        return null;
    }


}