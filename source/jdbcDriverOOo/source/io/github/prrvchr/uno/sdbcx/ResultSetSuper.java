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

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class ResultSetSuper
    extends ResultSetBase {

    private boolean mCanUpdateInsertedRows = false;

    // The constructor method:
    public ResultSetSuper(String service,
                          String[] services,
                          ConnectionSuper connection,
                          java.sql.ResultSet resultset,
                          StatementMain statement,
                          boolean bookmark,
                          boolean updatable)
        throws SQLException {
        super(service, services, connection, resultset, statement, bookmark);
        mCanUpdateInsertedRows = updatable;
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyID.CANUPDATEINSERTEDROWS,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    System.out.println("ResultSetSuper.CanUpdateInsertedRows() 1: " + mCanUpdateInsertedRows);
                    return mCanUpdateInsertedRows;
                },
                null));

        super.registerProperties(properties);
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }
}
