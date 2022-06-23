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
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;


public class KeyColumnContainer
    extends ContainerSuper<KeyColumn>
{

    private static final String m_name = KeyColumnContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyDescriptor()
    public KeyColumnContainer(Connection connection)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.KeyColumnContainer() 1");
        System.out.println("sdbcx.KeyColumnContainer() Count: " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyDescriptor()
    public KeyColumnContainer(Connection connection,
                              TableConstraint key)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.KeyColumnContainer() 1");
        for (TableConstraintColumn column : key.getConstrainedColumns()) {
            m_Elements.add(new KeyColumn(m_Connection, column));
            m_Names.add(column.getFullName());
        }
        System.out.println("sdbcx.KeyColumnContainer() Count: " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Key()
    public KeyColumnContainer(Connection connection,
                              XNameAccess columns)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.KeyColumnContainer() 1");
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
        int position = 1;
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                String name;
                    name = (String) descriptor.getPropertyValue("Name");
                KeyColumn column = new KeyColumn(m_Connection, descriptor, name, position++);
                m_Elements.add(column);
                m_Names.add(name);
            }
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.KeyColumnContainer() Count: " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Key()
    public KeyColumnContainer(Connection connection,
                              TableBase<?> table,
                              String name,
                              int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.KeyColumnContainer() 1");
        m_Elements.add(new KeyColumn(m_Connection, table, name, position));
        m_Names.add(name);
        System.out.println("sdbcx.KeyColumnContainer() Count: " + getCount());
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(ColumnBase column)
    {
        return m_Connection.getProvider().getDropColumnQuery(m_Connection, column);
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.ColumnContainer.createDataDescriptor() ***************************");
        return new KeyColumnDescriptor(m_Connection);
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 1");
        try {
            String name = (String) descriptor.getPropertyValue("Name");
            KeyColumn column = new KeyColumn(m_Connection, descriptor, name, m_Elements.size() + 1);
            m_Elements.add(column);
            m_Names.add(name);
            _insertElement(column);
            System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 2 : " + name);
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    protected String _getDropQuery(KeyColumn element)
        throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }


}
