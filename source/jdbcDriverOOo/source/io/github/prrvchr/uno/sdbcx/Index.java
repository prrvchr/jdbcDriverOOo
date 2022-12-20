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
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;


public class Index
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory
{

    private static final String m_service = Index.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Index"};

    protected IndexColumnContainer m_columns = null;
    protected final TableBase m_table;

    protected String m_Catalog;
    protected boolean m_IsUnique;
    protected boolean m_IsPrimaryKeyIndex;
    protected boolean m_IsClustered;

    // The constructor method:
    public Index(TableBase table,
                 boolean sensitive,
                 String name,
                 String catalog,
                 boolean unique,
                 boolean primarykey,
                 boolean clustered,
                 List<String> columns)
        throws ElementExistException
    {
        super(m_service, m_services, sensitive, name);
        m_table = table;
        m_Catalog = catalog;
        m_IsUnique = unique;
        m_IsPrimaryKeyIndex = primarykey;
        m_IsClustered = clustered;
        m_columns = new IndexColumnContainer(this, this, columns);
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CATALOG.name, PropertyIds.CATALOG.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Catalog;
                }
            }, null);
        registerProperty(PropertyIds.ISUNIQUE.name, PropertyIds.ISUNIQUE.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsUnique;
                }
            }, null);
        registerProperty(PropertyIds.ISPRIMARYKEYINDEX.name, PropertyIds.ISPRIMARYKEYINDEX.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsPrimaryKeyIndex;
                }
            }, null);
        registerProperty(PropertyIds.ISCLUSTERED.name, PropertyIds.ISCLUSTERED.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsClustered;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdbcx.Table.createDataDescriptor() ***************************************************");
        IndexDescriptor descriptor = new IndexDescriptor(isCaseSensitive());
        UnoHelper.copyProperties(this, descriptor);
        try {
            DataBaseTools.cloneDescriptorColumns(this, descriptor);
        }
        catch (SQLException e) {
        }
        return descriptor;
    }


    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns()
    {
        return m_columns;
    }


    public TableBase getTable()
    {
        return m_table;
    }


/*    // The constructor method:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexContainer.appendByDescriptor()
    public Index(Connection connection,
                 String name,
                 String catalog,
                 boolean unique,
                 boolean primarykey,
                 boolean clustered,
                 List<String> columns)
    {
        super(m_name, m_services, connection, name);
        m_Catalog = catalog;
        m_IsUnique = unique;
        m_IsPrimaryKeyIndex = primarykey;
        m_IsClustered = clustered;
    }
    public Index(Connection connection,
                 XPropertySet descriptor)
        throws SQLException
    {
        super(m_name, m_services, connection, descriptor);
        System.out.println("sdbcx.Index() 1");
        try {
            m_Catalog = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.CATALOG.name));
            m_IsUnique = AnyConverter.toBoolean(descriptor.getPropertyValue(PropertyIds.ISUNIQUE.name));
            //m_IsPrimaryKeyIndex = AnyConverter.toBoolean(descriptor.getPropertyValue(PropertyIds.ISPRIMARYKEYINDEX.name));
            m_IsClustered = AnyConverter.toBoolean(descriptor.getPropertyValue(PropertyIds.ISCLUSTERED.name));
            XColumnsSupplier columns = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            m_columns = new IndexColumnContainer(m_Connection, columns.getColumns());
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Index() 1 ERROR *******************");
        }
        registerProperties();
    }
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexContainer(ConnectionBase, XNameAccess, TableBase<?>)
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexContainer.appendByDescriptor()
    public Index(Connection connection,
                 XPropertySet descriptor,
                 String name)
        throws SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        super(m_name, m_services, connection, name);
        System.out.println("sdbcx.Index() 1");
        m_Catalog = (String) descriptor.getPropertyValue("Catalog");
        m_IsUnique = (boolean) descriptor.getPropertyValue("IsUnique");
        m_IsPrimaryKeyIndex = (boolean) descriptor.getPropertyValue("IsPrimaryKeyIndex");
        m_IsClustered = (boolean) descriptor.getPropertyValue("IsClustered");
        XColumnsSupplier columns = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        try {
            m_columns = new IndexColumnContainer(m_Connection, columns.getColumns());
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Index() 1 ERROR *******************");
        }
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexContainer(ConnectionBase, XNameAccess, TableBase<?>)
    public Index(Connection connection,
                 TableBase table,
                 String name,
                 String catalog,
                 boolean unique,
                 boolean primary,
                 boolean clustered,
                 boolean ascending,
                 String column,
                 int position)
        throws SQLException
    {
        super(m_name, m_services, connection, name);
        System.out.println("sdbcx.Index() 2");
        m_Catalog = catalog;
        m_IsUnique = unique;
        m_IsPrimaryKeyIndex = primary;
        m_IsClustered = clustered;
        try {
            m_columns = new IndexColumnContainer(connection, table, ascending, column, position);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Index() 2 ERROR *******************\n" + UnoHelper.getStackTrace(e));
        }
        registerProperties();
    }*/


}
