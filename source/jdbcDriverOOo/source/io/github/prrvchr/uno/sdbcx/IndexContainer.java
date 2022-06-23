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
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class IndexContainer
    extends ContainerSuper<Index>
{

    private static final String m_name = IndexContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    public IndexContainer(Connection connection,
                          TableBase<?> table)
    {
        super(m_name, m_services, connection);
        try {
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getIndexInfo(null, table.m_SchemaName, table.m_Name, false, true);
            while (result.next()) {
                boolean unique = !result.getBoolean(4);
                String catalog = result.getString(5);
                String name = result.getString(6);
                boolean primary = table.m_keys.hasNamedElement(name);
                int cluster = result.getShort(7);
                boolean clustered = result.wasNull() ? false : cluster == java.sql.DatabaseMetaData.tableIndexClustered;
                int position = result.getShort(8);
                String column = result.getString(9);
                String ascending = result.getString(10);
                boolean isascending = result.wasNull() ? false : ascending == "A";
                if (m_Names.contains(name)) {
                    m_Elements.get(m_Names.indexOf(name))._addColumn(new IndexColumn(m_Connection, table, isascending, column, position));
                }
                else {
                    Index index = new Index(m_Connection, table, name, catalog, unique, primary, clustered, isascending, column, position);
                    m_Elements.add(index);
                    m_Names.add(name);
                }
                System.out.println("sdbcx.IndexContainer.refresh() Column: " + column + " - Name: " + name);
            }
            result.close();
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            System.out.println("sdbcx.IndexContainer.refresh() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbcx.IndexContainer.refresh() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        System.out.println("sdbcx.IndexContainer(): " + getCount());
    }
    public IndexContainer(Connection connection,
                          XNameAccess indexes)
    {
        super(m_name, m_services, connection);
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, indexes)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                Index index = new Index(m_Connection, descriptor, name);
                m_Elements.add(index);
                m_Names.add(name);
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(Index index)
    {
        System.out.println("sdbcx.IndexContainer._getDropQuery() 1 ***************************");
        return null;
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.IndexContainer.appendByDescriptor() 1 ***************************");
        try {
            String name = (String) descriptor.getPropertyValue("Name");
            if (hasByName(name)) {
                throw new ElementExistException();
            }
            Index index = new Index(m_Connection, descriptor, name);
            m_Elements.add(index);
            m_Names.add(name);
            _insertElement(index);
            System.out.println("sdbcx.IndexContainer.appendByDescriptor() 2");
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            System.out.println("sdbcx.IndexContainer.appendByDescriptor() ERROR\n" + UnoHelper.getStackTrace(e));
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return new IndexDescriptor(m_Connection);
    }


}
