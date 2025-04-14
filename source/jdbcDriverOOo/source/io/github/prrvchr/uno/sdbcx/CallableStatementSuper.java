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

import java.util.Map;

import com.sun.star.uno.Type;

import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.CallableStatementBase;


public abstract class CallableStatementSuper
    extends CallableStatementBase {

    protected boolean mUseBookmarks  = false;


    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.CallableStatement()
    // XXX: - io.github.prrvchr.uno.sdbcx.CallableStatement()
    public CallableStatementSuper(String service,
                                  String[] services,
                                  ConnectionSuper connection,
                                  String sql) {
        super(service, services, connection, sql);
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.USEBOOKMARKS.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mUseBookmarks;
                },
                value -> {
                    System.out.println("sdbc.CallableStatementSuper._setUseBookmarks():" + (boolean) value);
                    mUseBookmarks = (boolean) value;
                }));

        super.registerProperties(properties);
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException {
        return super.getJdbcResultSet();
    }

    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

}
