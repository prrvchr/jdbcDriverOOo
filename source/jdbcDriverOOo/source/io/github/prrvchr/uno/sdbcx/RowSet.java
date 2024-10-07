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

import java.util.HashMap;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.StatementMain;


public final class RowSet
    extends RowSetSuper
{
    private static final String m_service = ResultSet.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.ResultSet",
                                                "com.sun.star.sdbcx.ResultSet"};

    // The constructor method:
    public RowSet(DriverProvider provider,
                  Connection connection,
                  java.sql.ResultSet result,
                  StatementMain statement,
                  RowCatalog catalog,
                  String table)
        throws SQLException
    {
        super(m_service, m_services, provider, connection, result, statement, catalog, table);
        registerProperties(new HashMap<String, PropertyWrapper>());
        System.out.println("sdbcx.RowSet() 1");
    }

    protected Connection getConnection() {
        return (Connection) m_Connection;
    }

}
