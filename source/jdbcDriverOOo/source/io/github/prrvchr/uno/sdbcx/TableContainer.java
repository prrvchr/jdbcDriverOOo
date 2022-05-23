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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.sdb.Table;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import schemacrawler.schema.Catalog;


public class TableContainer
    extends ContainerSuper<TableBase>
{

    // The constructor method:
    public TableContainer(ConnectionBase connection)
    {
        super(connection);
        refresh();
        System.out.println("sdbcx.TableContainer()");
    }
    public TableContainer(ConnectionBase connection,
                          Catalog catalog)
        throws java.sql.SQLException
    {
        super(connection);
        m_Names.clear();
        m_Elements.clear();
        for (final schemacrawler.schema.Table table : catalog.getTables())
        {
            String schema = table.getSchema().getName();
            String name = table.getName();
            m_Elements.add(new Table(connection, table));
            // FIXME: We must construct a unique name!!!
            m_Names.add(String.format("%s.%s", schema, name));
        }
    }

    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(TableBase table)
    {
        return m_Connection.getProvider().getDropTableQuery(m_Connection, table.m_CatalogName, table.m_SchemaName, table.m_Name);
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
                m_Elements.add(new Table(m_Connection, catalog, schema, name, type, description));
                // FIXME: We must construct a unique name!!!
                m_Names.add(String.format("%s.%s", schema, name));
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdbcx.TableContainer.createDataDescriptor() 1 ***************************");
        XPropertySet descriptor = new TableDescriptor(m_Connection);
        System.out.println("sdbcx.TableContainer.createDataDescriptor() 2");
        return descriptor;
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.TableContainer.appendByDescriptor() 1");
        try {
            String catalog = (String) descriptor.getPropertyValue("CatalogName");
            String schema = (String) descriptor.getPropertyValue("SchemaName");
            String table = (String) descriptor.getPropertyValue("Name");
            String name = String.format("%s.%s", schema, table);
            if (hasByName(name)) {
                 throw new ElementExistException();
            }
            System.out.println("sdbcx.TableContainer.appendByDescriptor() 2: " + name);
            XColumnsSupplier columns = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            XKeysSupplier keys = (XKeysSupplier) UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
            String elements = m_Connection.getProvider().getTableElementsQuery(m_Connection, columns.getColumns(), keys.getKeys());
            String query = m_Connection.getProvider().getCreateTableQuery(m_Connection, catalog, schema, table, elements);
            System.out.println("sdbcx.TableContainer.appendByDescriptor() 3");
            if (query != null) {
                System.out.println("sdbcx.TableContainer.appendByDescriptor() 4 : " + query);
                executeQuery(query);
            }
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
