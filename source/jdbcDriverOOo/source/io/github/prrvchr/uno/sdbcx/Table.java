/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public final class Table
    extends TableBase
{

    private static final String m_service = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Table"};
    protected final ConnectionSuper m_connection;

    // The constructor method:
    public Table(ConnectionSuper connection,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks)
    {
        super(m_service, m_services, sensitive, name);
        m_connection = connection;
        m_CatalogName = catalog;
        m_SchemaName= schema;
        m_Type = type;
        m_Description = remarks;
    }


    public ConnectionSuper getConnection()
    {
        return m_connection;
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdbcx.Table.createDataDescriptor() ***************************************************");
        TableDescriptor descriptor = new TableDescriptor(true);
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableContainer()
    public Table(Connection connection,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String description)
        throws SQLException
    {
        super(m_name, m_services, connection, catalog, schema, name, type, description);
        System.out.println("sdbcx.Table.Table() : 1" );
    }
    public Table(Connection connection,
                 schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, table);
        System.out.println("sdbcx.Table.Table() : 1" );
    }
    public Table(Connection connection,
                 XPropertySet descriptor)
        throws SQLException
    {
        super(m_name, m_services, connection, descriptor);
        System.out.println("sdbcx.Table.Table() : 1" );
    }*/



}
