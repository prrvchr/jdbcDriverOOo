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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;


public abstract class TableContainerMain<T extends TableMain>
    extends ContainerSuper<T> {
    protected final ConnectionSuper mConnection;
    private final ConnectionLog mLogger;

    // The constructor method:
    protected TableContainerMain(String service,
                                 String[] services,
                                 ConnectionSuper connection,
                                 boolean sensitive,
                                 String[] names,
                                 LoggerObjectType logtype)
        throws ElementExistException {
        super(service, services, connection, sensitive, names);
        mConnection = connection;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), logtype);
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    protected ConnectionSuper getConnection() {
        return mConnection;
    }

    // FIXME: This is the Java implementation of com.sun.star.sdbcx.XContainer interface for the
    // FIXME: com.sun.star.sdbcx.XRename interface available for the com.sun.star.sdbcx.XTable and XView
    protected void rename(String oldname, String newname)
        throws SQLException {
        if (hasByName(oldname)) {
            replaceElement(oldname, newname, false);
        }
    }

    @Override
    protected String getElementName(XPropertySet descriptor) {
        ComposeRule rule = ComposeRule.InTableDefinitions;
        return DBTools.composeTableName(mConnection.getProvider(), descriptor, rule, false);
    }

    @Override
    protected T appendElement(XPropertySet descriptor)
        throws SQLException {
        T element = null;
        String name = getElementName(descriptor);
        if (createDataBaseElement(descriptor, name)) {
            element = createElement(name);
        }
        return element;
    }

    @Override
    protected void refreshInternal() {
        mConnection.refresh();
    }

    abstract boolean createDataBaseElement(XPropertySet descriptor, String name) throws SQLException;

}
