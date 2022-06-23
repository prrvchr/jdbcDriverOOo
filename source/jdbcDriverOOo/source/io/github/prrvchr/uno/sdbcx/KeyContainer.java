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

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.sdb.Connection;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.PrimaryKey;


public class KeyContainer
    extends ContainerBase<Key>
{

    private static final String m_name = KeyContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public KeyContainer(Connection connection)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.KeyContainer()");
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public KeyContainer(Connection connection,
                        schemacrawler.schema.Table table)
    {
        super(m_name, m_services, connection);
        try {
            if (table.hasPrimaryKey()) {
                for (PrimaryKey key : table.getAlternateKeys()) {
                    m_Elements.add(new Key(m_Connection, key));
                }
            }
            if (table.hasForeignKeys()) {
                for (ForeignKey key : table.getForeignKeys()) {
                    m_Elements.add(new Key(m_Connection, key));
                }
            }
            System.out.println("sdbcx.KeyContainer.refresh() Number of Key: " + getCount());
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public KeyContainer(Connection connection,
                        TableBase<?> table)
    {
        super(m_name, m_services, connection);
        try {
            Map<String, Key> keys = new LinkedHashMap<String, Key>();
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getPrimaryKeys(null, table.m_SchemaName, table.m_Name);
            while (result.next()) {
                String column = result.getString(4);
                int position = result.getShort(5);
                String name = result.getString(6);
                if (keys.containsKey(name)) {
                    System.out.println("sdbcx.KeyContainer.refresh() Add Keycolumn to Key: " + column + " - Name: " + name);
                    keys.get(name)._addColumn(new KeyColumn(m_Connection, table, column, position));
                }
                else {
                    System.out.println("sdbcx.KeyContainer.refresh() Create New Key: " + column + " - Name: " + name);
                    Key key = new Key(m_Connection, table, name, column, position);
                    keys.put(name, key);
                    m_Elements.add(key);
                }
            }
            System.out.println("sdbcx.KeyContainer.refresh() Number of Key: " + getCount());
            result.close();
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public KeyContainer(Connection connection,
                        XIndexAccess keys,
                        TableBase<?> table)
    {
        super(m_name, m_services, connection);
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, keys)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                Key key = new Key(m_Connection, descriptor, name);
                m_Elements.add(key);
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

        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }



    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(Key key)
    {
        System.out.println("sdbcx.KeyContainer._getDropQuery() 1 ***************************");
        return null;
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.KeyContainer.appendByDescriptor() 1 ***************************");
        try {
            Key key = new Key(m_Connection, descriptor, (String) descriptor.getPropertyValue("Name"));
            m_Elements.add(key);
            _insertElement(key);
            System.out.println("sdbcx.KeyContainer.appendByDescriptor() 2");
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return new KeyDescriptor(m_Connection);
    }


}
