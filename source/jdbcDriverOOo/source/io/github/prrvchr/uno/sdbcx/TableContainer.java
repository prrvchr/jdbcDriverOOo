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
import com.sun.star.logging.LogLevel;

import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.Resources;


public final class TableContainer
    extends TableContainerSuper<Table> {
    private static final String SERVICE = TableContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Tables",
                                              "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public TableContainer(Connection connection,
                          String[] names,
                          boolean sensitive)
        throws ElementExistException {
        super(SERVICE, SERVICES, connection, sensitive, names);
    }

    @Override
    protected XPropertySet createDescriptor() {
        System.out.println("sdbcx.TableContainer._createDescriptor()");
        return new TableDescriptor(isCaseSensitive());
    }

    protected Table getTable(NamedComponent component,
                             String type,
                             String remarks) {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLE);
        Table table = new Table(getConnection(), isCaseSensitive(), component.getCatalogName(),
                                component.getSchemaName(), component.getTableName(), type, remarks);
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLE_ID, table.getLogger().getObjectId());
        return table;
    }

    protected Connection getConnection() {
        return (Connection) mConnection;
    }

}
