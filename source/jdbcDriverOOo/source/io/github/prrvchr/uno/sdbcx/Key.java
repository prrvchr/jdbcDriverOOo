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

import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public class Key
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory
{

    private static final String m_service = Key.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Key"};

    protected KeyColumnContainer m_columns = null;

    private final TableBase m_table;
    public int m_Type;
    protected String m_ReferencedTable;
    protected int m_UpdateRule;
    protected int m_DeleteRule;

    // The constructor method:
    public Key(boolean sensitive)
    {
        super(m_service, m_services, sensitive, "");
        m_table = null;
        System.out.println("sdbcx.Key() 1");
    }
    // The constructor method:
    public Key(TableBase table,
               boolean sensitive,
               String name,
               String reference,
               int type,
               int update,
               int delete,
               List<String> columns)
        throws ElementExistException
    {
        super(m_service, m_services, sensitive, name);
        System.out.println("sdbcx.Key() 1");
        m_table = table;
        m_Type = type;
        m_ReferencedTable = reference;
        m_UpdateRule = update;
        m_DeleteRule = delete;
        m_columns = new KeyColumnContainer(this, this, columns);
        registerProperties();
    }

    
    
/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer(XPropertySet)
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer.appendByDescriptor(ConnectionBase, XIndexAccess, TableBase<?>)
    public Key(Connection connection,
               TableBase table,
               XPropertySet descriptor,
               String name)
        throws SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        super(m_name, m_services, connection, name);
        System.out.println("sdbcx.Key() 1");
        m_table = table;
        m_Type = (int) descriptor.getPropertyValue("Type");
        m_ReferencedTable = (String) descriptor.getPropertyValue("ReferencedTable");
        m_UpdateRule = (int) descriptor.getPropertyValue("UpdateRule");
        m_DeleteRule = (int) descriptor.getPropertyValue("DeleteRule");
        XColumnsSupplier columns = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        try {
            m_columns = new KeyColumnContainer(m_Connection, columns.getColumns());
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Key() 1 ERROR *******************");
        }
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer(XPropertySet)
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer.appendByDescriptor(ConnectionBase, XIndexAccess, TableBase<?>)
    public Key(Connection connection,
               TableBase table,
               String name,
               String column,
               int position)
        throws SQLException
    {
        super(m_name, m_services, connection, name);
        System.out.println("sdbcx.Key() 2");
        m_table = table;
        m_Type = KeyType.PRIMARY;
        m_ReferencedTable = "";
        m_UpdateRule = KeyRule.CASCADE;
        m_DeleteRule = KeyRule.CASCADE;
        try {
            m_columns = new KeyColumnContainer(connection, table, column, position);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Key() 2 ERROR *******************\n" + UnoHelper.getStackTrace(e));
        }
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer(XPropertySet)
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer.appendByDescriptor(ConnectionBase, XIndexAccess, TableBase<?>)
    public Key(Connection connection,
               TableBase table,
               PrimaryKey key)
        throws SQLException
    {
        super(m_name, m_services, connection, key.getName());
        System.out.println("sdbcx.Key() 2");
        m_table = table;
        m_Type = KeyType.PRIMARY;
        m_ReferencedTable = "";
        m_UpdateRule = KeyRule.CASCADE;
        m_DeleteRule = KeyRule.CASCADE;
        try {
            m_columns = new KeyColumnContainer(connection, key);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Key() 2 ERROR *******************\n" + UnoHelper.getStackTrace(e));
        }
        registerProperties();
    }
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyContainer.appendByDescriptor(ConnectionBase, XIndexAccess, TableBase<?>)
    public Key(Connection connection,
               TableBase table,
               ForeignKey key)
        throws SQLException
    {
        super(m_name, m_services, connection, key.getName());
        System.out.println("sdbcx.Key() 2");
        m_table = table;
        m_Type = KeyType.FOREIGN;
        m_ReferencedTable = "";
        m_UpdateRule = key.getUpdateRule().id();
        m_DeleteRule = key.getDeleteRule().id();;
        try {
            m_columns = new KeyColumnContainer(connection, key);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Key() 2 ERROR *******************\n" + UnoHelper.getStackTrace(e));
        }
        registerProperties();
    }

    public Key(Connection connection,
               TableBase table,
               String name,
               String referencedtable,
               int type,
               int update,
               int delete,
               List<String> columns)
        throws SQLException
    {
        super(m_name, m_services, connection, name);
        System.out.println("sdbcx.Key() 2");
        m_table = table;
        m_Type = type;
        m_ReferencedTable = referencedtable;
        m_UpdateRule = update;
        m_DeleteRule = delete;
        try {
            m_columns = new KeyColumnContainer(connection, columns);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Key() 2 ERROR *******************\n" + UnoHelper.getStackTrace(e));
        }
        registerProperties();
    }*/


    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
        registerProperty(PropertyIds.REFERENCEDTABLE.name, PropertyIds.REFERENCEDTABLE.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_ReferencedTable;
                }
            }, null);
        registerProperty(PropertyIds.UPDATERULE.name, PropertyIds.UPDATERULE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_UpdateRule;
                }
            }, null);
        registerProperty(PropertyIds.DELETERULE.name, PropertyIds.DELETERULE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_DeleteRule;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns()
    {
        return m_columns;
    }


/*    protected void _addColumn(KeyColumn column)
    {
        int index = 0;
        for (KeyColumn element : m_columns.m_Elements) {
            if (column.m_position < element.m_position) {
                m_columns.m_Elements.add(index, column);
                m_columns.m_Names.add(index, column.m_Name);
                break;
            }
            if (column.m_position > element.m_position) {
                m_columns.m_Elements.add(index + 1, column);
                m_columns.m_Names.add(index + 1, column.m_Name);
                break;
            }
            index++;
        }
    }*/


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        return new KeyDescriptor(isCaseSensitive());
    }

    public TableBase getTable()
    {
        return m_table;
    }

    @Override
    public String toString()
    {
        return String.format("%s: Name: %s, Type: %s, ReferencedTable: %s, UpdateRule: %s, DeleteRule: %s ",
                             this.getClass().getName(),getName(), m_Type, m_ReferencedTable, m_UpdateRule, m_DeleteRule);
    }
}
