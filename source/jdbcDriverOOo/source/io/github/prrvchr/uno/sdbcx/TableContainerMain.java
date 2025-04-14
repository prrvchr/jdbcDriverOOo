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

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.StandardSQLState;


public abstract class TableContainerMain<T extends TableMain>
    extends Container<T> {
    protected final ConnectionSuper mConnection;
    private final ConnectionLog mLogger;

    // The constructor method:
    protected TableContainerMain(String service,
                                 String[] services,
                                 ConnectionSuper connection,
                                 boolean sensitive,
                                 List<String> names,
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
    protected String getElementName(XPropertySet descriptor)
        throws SQLException {
        try {
            return DBTools.composeTableName(mConnection.getProvider(), descriptor,
                                            ComposeRule.InTableDefinitions, false);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
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
