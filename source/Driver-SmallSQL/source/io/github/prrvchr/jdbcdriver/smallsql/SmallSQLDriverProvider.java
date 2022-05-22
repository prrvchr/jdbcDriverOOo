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
package io.github.prrvchr.jdbcdriver.smallsql;

import java.sql.DriverManager;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdb.ResultSet;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public final class SmallSQLDriverProvider
    implements DriverProvider
{

    private static final String m_subProtocol = "smallsql";
    private static final boolean m_warnings = false;
    private List<String> m_properties = List.of("user", "password");

    // The constructor method:
    public SmallSQLDriverProvider()
    {
        System.out.println("smallsql.SmallSQLDriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol(m_subProtocol));
    }

    @Override
    public final boolean supportWarningsSupplier() {
        return m_warnings;
    }

    @Override
    public java.sql.Connection getConnection(final String url,
                                             final PropertyValue[] info,
                                             final String level)
        throws java.sql.SQLException
    {
        return DriverManager.getConnection(url, getConnectionProperties(m_properties, info));
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final XComponentContext context,
                                                          final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new SmallSQLDatabaseMetaData(context, connection);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final ConnectionBase connection,
                                            final java.sql.ResultSet resultset)
        throws java.sql.SQLException
    {
        return new ResultSet(context, connection, resultset);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final ConnectionBase connection,
                                            final StatementMain statement,
                                            final java.sql.ResultSet resultset)
        throws java.sql.SQLException
    {
        return new ResultSet(context, connection, statement, resultset);
    }


}