/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import java.util.HashMap;

import javax.sql.rowset.CachedRowSet;

import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.RowSetSuper;


public final class RowSet
    extends RowSetSuper
    implements XColumnsSupplier {

    private static final String SERVICE = RowSet.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.ResultSet",
                                              "com.sun.star.sdbc.ResultSet", 
                                              "com.sun.star.sdbcx.ResultSet"};

    private XNameAccess mColumns;

    // The constructor method:
    protected RowSet(Connection connection,
                     CachedRowSet rowset,
                     StatementMain statement)
        throws SQLException {
        super(SERVICE, SERVICES, connection, rowset, statement);
        registerProperties(new HashMap<String, PropertyWrapper>());
        System.out.println("sdb.RowSet() 1");
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns() {
        System.out.println("sdb.RowSet.getColumns() 1 *********************************************");
        try {
            if (mColumns == null) {
                mColumns = new ResultColumnContainer(this, mResult.getStatement().getConnection().getMetaData(),
                                                     mResult.getMetaData(), true);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("sdb.RowSet.getColumns() ERROR *****************************************");
        }
        System.out.println("sdb.RowSet.getColumns() 2 *********************************************");
        return mColumns;
    }

    @Override
    protected Connection getConnection() {
        return (Connection) mConnection;
    }

}
