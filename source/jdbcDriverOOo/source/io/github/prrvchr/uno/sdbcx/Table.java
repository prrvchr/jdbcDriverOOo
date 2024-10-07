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

import java.util.HashMap;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;

import io.github.prrvchr.jdbcdriver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class Table
    extends TableSuper
{

    private static final String m_service = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Table"};

    // The constructor method:
    public Table(Connection connection,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks)
    {
        super(m_service, m_services, connection, sensitive, catalog, schema, name, type, remarks);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }

    protected Connection getConnection()
    {
        return (Connection) m_connection;
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        TableDescriptor descriptor = new TableDescriptor(true);
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

    protected ColumnContainer getColumnContainer(List<ColumnDescription> descriptions)
            throws ElementExistException
    {
        return new ColumnContainer(this, isCaseSensitive(), descriptions);
    }

}
