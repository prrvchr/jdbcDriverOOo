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

import java.sql.SQLException;

import com.sun.star.beans.XPropertySet;

import io.github.prrvchr.uno.sdb.Table;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import schemacrawler.schema.Catalog;


public class TableContainer
    extends Container<TableBase>
{

    // The constructor method:
    public TableContainer(ConnectionBase connection)
    {
        super(connection);
        refresh();
    }
    public TableContainer(ConnectionBase connection,
                          Catalog catalog)
        throws SQLException
    {
        super(connection);
        m_Names.clear();
        m_Elements.clear();
        for (final schemacrawler.schema.Table table : catalog.getTables())
        {
            String schema = table.getSchema().getName();
            String name = table.getName();
            m_Elements.add(new Table(connection, table));
            m_Names.add(String.format("%s.%s", schema, name));
        }
    }
    // com.sun.star.util.XRefreshable
    @Override
    public void refresh()
    {
        m_Names.clear();
        m_Elements.clear();
        try {
            String[] types = m_Connection.getProvider().getTableTypes();
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getTables(null, null, "%", types);
            while (result.next())
            {
                String catalog = result.getString(1);
                String schema = result.getString(2);
                String name = result.getString(3);
                String type = m_Connection.getProvider().getTableType(result.getString(4));
                String description = result.getString(5);
                Table table = new Table(m_Connection, catalog, schema, name, type, description);
                m_Elements.add(table);
                String tname = String.format("%s.%s", schema, name);
                System.out.println("sdbcx.TableContainer.refresh() : " + tname);
                m_Names.add(tname);
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.TableContainer.createDataDescriptor() ***************************");
        return null;
    }


}
