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

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterTable;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XIndexesSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbc.PropertyIds;


public abstract class TableBase<T extends ColumnSuper>
    extends Item
    implements XColumnsSupplier,
                XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    private ColumnContainer<T> m_columns = null;
    protected KeyContainer m_keys = null;
    protected IndexContainer m_indexes = null;
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    protected String m_Description = "";
    protected String m_Type = "";

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Table()
    // XXX: - io.github.prrvchr.uno.sdb.Table()
    public TableBase(String service,
                     String[] services,
                     Connection connection,
                     Class<T> column,
                     XPropertySet descriptor,
                     String name)
    throws java.sql.SQLException
    {
        super(service, services, connection, name);
        try {
            m_CatalogName = (String) descriptor.getPropertyValue("CatalogName");
            m_SchemaName = (String) descriptor.getPropertyValue("SchemaName");
            m_Type = (String) descriptor.getPropertyValue("Type");
            m_Description = (String) descriptor.getPropertyValue("Description");
            XColumnsSupplier columns = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            m_columns = new ColumnContainer<T>(connection, column, columns.getColumns());
            XKeysSupplier keys = (XKeysSupplier) UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
            m_keys = new KeyContainer(connection, keys.getKeys(), this);
            XIndexesSupplier indexes = (XIndexesSupplier) UnoRuntime.queryInterface(XIndexesSupplier.class, descriptor);
            m_indexes = new IndexContainer(connection, indexes.getIndexes());
            registerProperties();
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type + "\nDecription: " + m_Description);
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Table()
    // XXX: - io.github.prrvchr.uno.sdb.Table()
    public TableBase(String service,
                     String[] services,
                     Connection connection,
                     Class<T> column,
                     String catalog,
                     String schema,
                     String name,
                     String type,
                     String description)
        throws java.sql.SQLException
    {
        super(service, services, connection, name);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Type = type;
        m_Description = description != null ? description : "";
        System.out.println("sdbcx.TableBase.TableBase() Columns:");
        m_columns = new ColumnContainer<T>(connection, column, this);
        System.out.println("sdbcx.TableBase.TableBase() Keys:");
        m_keys = new KeyContainer(connection, this);
        System.out.println("sdbcx.TableBase.TableBase() Indexes:");
        m_indexes = new IndexContainer(connection, this);
        registerProperties();
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type + "\nDecription: " + m_Description);
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Table()
    // XXX: - io.github.prrvchr.uno.sdb.Table()
    public TableBase(String service,
                     String[] services,
                     Connection connection,
                     Class<T> column,
                     schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(service, services, connection, table.getName());
        m_CatalogName = table.getSchema().getCatalogName();
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_columns = new ColumnContainer<T>(connection, column, table);
        m_keys = new KeyContainer(connection, table);
        //m_indexes = new IndexContainer(connection, table, this);
        registerProperties();
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }


    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CATALOGNAME.name, PropertyIds.CATALOGNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CatalogName;
                }
            }, null);
        registerProperty(PropertyIds.SCHEMANAME.name, PropertyIds.SCHEMANAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_SchemaName;
                }
            }, null);
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Description;
                }
            }, null);
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        return m_columns;
    }


    // com.sun.star.sdbcx.XAlterTable:
    @Override
    public void alterColumnByIndex(int index, XPropertySet properties)
        throws SQLException, IndexOutOfBoundsException
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.alterColumnByIndex()");
    }

    @Override
    public void alterColumnByName(String name, XPropertySet properties)
        throws SQLException, NoSuchElementException
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.alterColumnByName()");
    }


    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.getKeys() ***************************************************: " + m_keys.getCount());
        return m_keys;
    }


    // com.sun.star.sdbcx.XIndexesSupplier
    @Override
    public XNameAccess getIndexes() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.getIndexes() ***************************************************");
        return m_indexes;
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


    public XPropertySet createDataDescriptor(Connection connection) {return null;};


}
