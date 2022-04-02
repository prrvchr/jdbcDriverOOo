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

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSet;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public final class SmallSQLDriverProvider
    implements DriverProvider
{

    private static String m_protocol = "sdbc:smallsql";
    private static boolean m_warnings = false;

    // The constructor method:
    public SmallSQLDriverProvider()
    {
        System.out.println("smallsql.SmallSQLDriverProvider() 1");
    }

    @Override
    public boolean acceptsURL(String url)
    {
        return url.startsWith(m_protocol);
    }

    @Override
    public boolean supportWarningsSupplier() {
        return m_warnings;
    }

    @Override
    public DatabaseMetaDataBase getDatabaseMetaData(XComponentContext context,
                                                    ConnectionBase connection,
                                                    java.sql.DatabaseMetaData metadata,
                                                    PropertyValue[] info,
                                                    String url)
    {
        return new SmallSQLDatabaseMetaData(context, this, connection, metadata, info, url);
    }

    @Override
    public ResultSetBase getResultSet(XComponentContext context,
                                      java.sql.ResultSet resultset)
    {
        return new ResultSet(context, this, resultset);
    }

    @Override
    public ResultSetBase getResultSet(XComponentContext context,
                                      StatementMain statement,
                                      java.sql.ResultSet resultset)
    {
        return new SmallSQLResultSet(context, this, statement, resultset);
    }


}