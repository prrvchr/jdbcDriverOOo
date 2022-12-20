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

import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.sdbc.PreparedStatementSuper;


public final class PreparedStatement
    extends PreparedStatementSuper
    implements XColumnsSupplier
{

    private static final String m_service = PreparedStatement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.PreparedStatement",
                                          "com.sun.star.sdbc.PreparedStatement"};

    // The constructor method:
    public PreparedStatement(Connection connection,
                             String sql)
    {
        super(m_service, m_services, connection, sql);
        System.out.println("sdb.PreparedStatement() 1: '" + sql + "'");
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        /*try {
            System.out.println("sdb.PreparedStatement.getColumns() ************************************");
            java.sql.ResultSetMetaData metadata = _getPreparedStatement().getMetaData();
            return new ColumnContainer((Connection) m_Connection, metadata);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdb.PreparedStatement.getColumns() 2 **********************************");
            // pass
        }*/
        return null;
    }


    protected XResultSet _getResultSet(java.sql.ResultSet result)
        throws SQLException
    {
        System.out.println("sdb.PreparedStatement._getResultSet(): " + m_UseBookmarks);
        XResultSet resultset = null;
        if (result != null) {
            resultset =  m_Connection.getProvider().getResultSet(m_Connection, result, this, m_UseBookmarks);
        }
        return resultset;
    }

}
