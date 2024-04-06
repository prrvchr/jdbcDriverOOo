/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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

import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public final class Index
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory
{

    private static final String m_service = Index.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Index"};
    
    protected IndexColumnContainer m_columns = null;
    protected final TableSuper<?> m_table;
    
    protected String m_Catalog;
    protected boolean m_IsUnique;
    protected boolean m_IsPrimaryKeyIndex;
    protected boolean m_IsClustered;

    // The constructor method:
    public Index(TableSuper<?> table,
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
        System.out.println("sdbcx.Index() 1");
        m_table = table;
        m_Catalog = catalog;
        m_IsUnique = unique;
        m_IsPrimaryKeyIndex = primarykey;
        m_IsClustered = clustered;
        m_columns = new IndexColumnContainer(this, columns);
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

    protected IndexColumnContainer getColumnsInternal() {
        return m_columns;
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdbcx.Table.createDataDescriptor() ***************************************************");
        IndexDescriptor descriptor = new IndexDescriptor(isCaseSensitive());
        UnoHelper.copyProperties(this, descriptor);
        try {
            DBTools.cloneDescriptorColumns(this, descriptor);
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


    public TableSuper<?> getTable()
    {
        return m_table;
    }


}
