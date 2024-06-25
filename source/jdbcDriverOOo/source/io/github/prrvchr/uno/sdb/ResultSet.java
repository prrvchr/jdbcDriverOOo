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
package io.github.prrvchr.uno.sdb;

import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.uno.sdbcx.ResultSetSuper;
import io.github.prrvchr.uno.sdbc.StatementMain;


public final class ResultSet
    extends ResultSetSuper
    implements XColumnsSupplier
{
    private static final String m_service = ResultSet.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.ResultSet",
                                                "com.sun.star.sdbc.ResultSet", 
                                                "com.sun.star.sdbcx.ResultSet"};


    // The constructor method:
    public ResultSet(Connection connection,
                     java.sql.ResultSet resultset,
                     StatementMain statement)
    throws SQLException
    {
        super(m_service, m_services, connection, resultset, statement, false, false);
        System.out.println("sdb.ResultSet() 1");
    }

    @Override
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdb.ResultSet.getColumns() 1 *********************************************");
        /*XNameAccess columns = null;
        try {
            columns = new ColumnContainer((Connection) m_Connection, m_Result);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdb.ResultSet.getColumns() ERROR *****************************************");
            //throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("sdb.ResultSet.getColumns() 2 *********************************************");
        return columns;*/
        return null;
    }

    @Override
    protected Connection getConnection() {
        return (Connection) m_Connection;
    }


}
