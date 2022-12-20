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
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
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
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.DataBaseTableHelper;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.DataBaseTableHelper.ColumnDescription;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public abstract class TableBase
    extends Descriptor
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    private ColumnContainer m_columns = null;
    private KeyContainer m_keys = null;
    private IndexContainer m_indexes = null;
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    protected String m_Description = "";
    protected String m_Type = "";

    // The constructor method:
    public TableBase(String service,
                     String[] services,
                     boolean sensitive,
                     String name)
    {
        super(service, services, sensitive, name);
        System.out.println("sdbcx.TableBase() 1");
        registerProperties();
        System.out.println("sdbcx.TableBase() 1");
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
        registerProperty(PropertyIds.TABLETYPE.name, PropertyIds.TABLETYPE.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
    }

    
    @Override
    protected void postDisposing() {
        super.postDisposing();
        if (m_keys != null) {
            m_keys.dispose();
        }
        if (m_columns != null) {
            m_columns.dispose();
        }
        if (m_indexes != null) {
            m_indexes.dispose();
        }
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        try {
            System.out.println("sdbcx.TableBase.getColumns() 1");
            if (m_columns == null) {
                m_columns = _refreshColumns();
            }
            System.out.println("sdbcx.TableBase.getColumns() 2");
            return m_columns;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.TableBase.getColumns() 2" + UnoHelper.getStackTrace(e));
        }
        return null;
    }

    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        System.out.println("sdbcx.TableBase.getKeys() 1");
        checkDisposed();
        if (m_keys == null) {
            m_keys = _refreshKeys();
        }
        System.out.println("sdbcx.TableBase.getKeys() 2");
        return m_keys;
    }


    // com.sun.star.sdbcx.XIndexesSupplier
    @Override
    public XNameAccess getIndexes() {
        System.out.println("sdbcx.TableBase.getIndexes() 1");
        checkDisposed();
        if (m_indexes == null) {
            m_indexes = _refreshIndexes();
        }
        System.out.println("sdbcx.TableBase.getIndexes() 2");
        return m_indexes;
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

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


    protected ColumnContainer _refreshColumns()
    {
        try {
            List<ColumnDescription> columns = DataBaseTableHelper.readColumns(getConnection(), this);
            return new ColumnContainer(this, this, isCaseSensitive(), columns);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (java.sql.SQLException e) {
            return null;
        }
    }

    private IndexContainer _refreshIndexes()
    {
        try {
            List<String> indexes = DataBaseTableHelper.readIndexes(getConnection(), this);
            return new IndexContainer(this, this, isCaseSensitive(), indexes);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (SQLException e) {
            return null;
        }
    }

    private KeyContainer _refreshKeys() {
        try {
            Map<String, Key> keys = DataBaseTableHelper.readKeys(getConnection(), isCaseSensitive(), this);
            return new KeyContainer(this, this, isCaseSensitive(), keys);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (SQLException e) {
            return null;
        }
    }

    public String getCatalogName()
    {
        return m_CatalogName.isBlank() ? null : m_CatalogName;
    }
    public String getSchemaName()
    {
        return m_SchemaName.isBlank() ? null : m_SchemaName;
    }

    public String getTypeCreatePattern()
    {
        return "";
    }

    public abstract ConnectionSuper getConnection();

    /*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Table()
    // XXX: - io.github.prrvchr.uno.sdb.Table()
    public TableBase(String service,
                     String[] services,
                     Connection connection,
                     XPropertySet descriptor)
    throws SQLException
    {
        super(service, services, connection, descriptor);
        try {
            m_CatalogName = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.CATALOGNAME.name));
            m_SchemaName = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.SCHEMANAME.name));
            String value;
            String[] types = m_Connection.getProvider().getTableTypes();
            System.out.println("TableBase() 1 Type: " + Arrays.toString(types));
            //String[] types = null;
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getTables(m_CatalogName, m_SchemaName, m_Name, types);
            if (result.next()) {
                System.out.println("TableBase() 2");
                value = result.getString(4);
                m_Type = result.wasNull() ? "" : m_Connection.getProvider().getTableType(value);
                value = result.getString(5);
                m_Description = result.wasNull() ? "" : value;
                System.out.println("TableBase() 3");
            }
            result.close();
            m_columns = new ColumnContainer(connection, this);
            System.out.println("sdbcx.TableBase.TableBase() Columns: " + m_columns.getCount());
            m_keys = new KeyContainer(connection, this);
            System.out.println("sdbcx.TableBase.TableBase() Keys: " + m_keys.getCount());
            m_indexes = new IndexContainer(connection, this);
            System.out.println("sdbcx.TableBase.TableBase() Indexes: " + m_indexes.getCount());
            registerProperties();
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type + "\nDecription: " + m_Description);
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.Table()
    // XXX: - io.github.prrvchr.uno.sdb.Table()
    public TableBase(String service,
                     String[] services,
                     Connection connection,
                     String catalog,
                     String schema,
                     String name,
                     String type,
                     String description)
        throws SQLException
    {
        super(service, services, connection, name);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Type = type;
        m_Description = description != null ? description : "";
        System.out.println("sdbcx.TableBase.TableBase() Columns:");
        m_columns = new ColumnContainer(connection, this);
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
                     schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(service, services, connection, table.getName());
        m_CatalogName = table.getSchema().getCatalogName();
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_columns = new ColumnContainer(connection, table);
        m_keys = new KeyContainer(connection, table);
        //m_indexes = new IndexContainer(connection, table, this);
        registerProperties();
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }*/


}
