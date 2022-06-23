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

import io.github.prrvchr.uno.sdb.Connection;


public class IndexColumnContainer
    extends ContainerSuper<IndexColumn>
{

    private static final String m_name = IndexColumnContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexDescriptor()
    public IndexColumnContainer(Connection connection)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.IndexColumnContainer() 1");
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Index()
    public IndexColumnContainer(Connection connection,
                                TableBase<?> table,
                                boolean ascending,
                                String name,
                                int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.IndexColumnContainer() 1");
        m_Elements.add(new IndexColumn(m_Connection, table, ascending, name, position));
        m_Names.add(name);
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Index(ConnectionBase, XPropertySet, String)
    public IndexColumnContainer(Connection connection,
                                XNameAccess columns)
        throws UnknownPropertyException, WrappedTargetException, java.sql.SQLException, NoSuchElementException
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.IndexColumnContainer() 1");
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
        System.out.println("sdbcx.IndexColumnContainer() 1");
        int position = 1;
        while (iter.hasMoreElements()) {
            XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
            String name = (String) descriptor.getPropertyValue("Name");
            IndexColumn column = new IndexColumn(m_Connection, descriptor, name, position++);
            m_Elements.add(column);
            m_Names.add(name);
        }
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(ColumnBase column)
    {
        System.out.println("sdbcx.IndexColumnContainer._getDropQuery() ********************************");
        return null;
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.IndexColumnContainer.createDataDescriptor() ***************************");
        return new ColumnDescriptor(m_Connection);
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.IndexColumnContainer.appendByDescriptor() 1");
        try {
            String name = (String) descriptor.getPropertyValue("Name");
            IndexColumn column = new IndexColumn(m_Connection, descriptor, name, m_Elements.size() + 1);
            m_Elements.add(column);
            m_Names.add(name);
            _insertElement(column);
            System.out.println("sdbcx.IndexColumnContainer.appendByDescriptor() 2 : " + name);
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    protected String _getDropQuery(IndexColumn element)
        throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }


}
