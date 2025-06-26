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
package io.github.prrvchr.uno.sdbcx;

import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.RowSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class RowSetSuper
    extends RowSetBase {

    private boolean mCanUpdateInsertedRows = true;

    // The constructor method:
    protected RowSetSuper(String service,
                          String[] services,
                          ConnectionSuper connection,
                          CachedRowSet rowset,
                          StatementMain statement)
        throws SQLException {
        super(service, services, connection, rowset, statement);
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.CANUPDATEINSERTEDROWS.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    System.out.println("RowSetSuper.CanUpdateInsertedRows() 1: " + mCanUpdateInsertedRows);
                    return mCanUpdateInsertedRows;
                },
                null));

        super.registerProperties(properties);
    }

    protected CachedRowSet getRowSet() {
        return (CachedRowSet) mResult;
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }
}
