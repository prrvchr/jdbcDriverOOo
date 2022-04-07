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
package io.github.prrvchr.jdbcdriver;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.sdbc.ConnectionBase;
import io.github.prrvchr.jdbcdriver.sdbc.DatabaseMetaData;
import io.github.prrvchr.jdbcdriver.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.jdbcdriver.sdbc.ResultSet;
import io.github.prrvchr.jdbcdriver.sdbc.ResultSetBase;
import io.github.prrvchr.jdbcdriver.sdbc.StatementMain;

public final class DefaultDriverProvider
    implements DriverProvider
{

    // The constructor method:
    public DefaultDriverProvider()
    {
        System.out.println("jdbcdriver.DefaultDatabaseProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return true;
    }

    @Override
    public final boolean supportWarningsSupplier()
    {
        return true;
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final XComponentContext context,
                                                          final ConnectionBase connection,
                                                          final java.sql.DatabaseMetaData metadata,
                                                          final PropertyValue[] info,
                                                          final String url)
    {
        return new DatabaseMetaData(context, this, connection, metadata, info, url);
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
        return new ResultSet(context, this, statement, resultset);
    }


}