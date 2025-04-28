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

import com.sun.star.uno.Type;

import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbc.StatementBase;


public abstract class StatementSuper
    extends StatementBase {

    protected boolean mUseBookmarks = false;

    // The constructor method:
    public StatementSuper(String service,
                          String[] services,
                          ConnectionSuper connection) {
        super(service, services, connection);
        System.out.println("sdbcx.Statement() 1");
    }

    @Override
    public java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException {
        return super.getJdbcResultSet();
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.USEBOOKMARKS.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    System.out.println("sdbcx.Statement._getUseBookmarks():" + mUseBookmarks);
                    return mUseBookmarks;
                },
                value -> {
                    System.out.println("sdbcx.Statement._setUseBookmarks():" + (boolean) value);
                    mUseBookmarks = (boolean) value;
                }));

        super.registerProperties(properties);
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

}
