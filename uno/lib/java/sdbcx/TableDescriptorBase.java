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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Type;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public abstract class TableDescriptorBase
    extends Descriptor
    implements XColumnsSupplier,
               XKeysSupplier
{

    private ColumnDescriptorContainer m_columns;
    private KeyDescriptorContainer m_keys;
    private String m_CatalogName = "";
    private String m_SchemaName = "";
    private String m_Description = "";

    // The constructor method:
    public TableDescriptorBase(String service,
                               String[] services,
                               boolean sensitive)
    {
        super(service, services, sensitive);
        m_columns = new ColumnDescriptorContainer(this, sensitive);
        m_keys = new KeyDescriptorContainer(this, sensitive);
        registerProperties();
        System.out.println("sdbcx.descriptors.TableDescriptor()");
    }


/*    // The constructor method:
    public TableDescriptorBase(String service,
                               String[] services,
                               Connection connection)
    {
        super(service, services, connection);
        m_xColumns = new ColumnContainer(connection);
        m_xKeys = new KeyContainer(connection);
        registerProperties();
        System.out.println("sdbcx.TableDescriptor() ***************************************************");
    }

    public TableDescriptorBase(String service,
                               String[] services,
                               Connection connection,
                               TableBase table)
    {
        super(service, services, connection);
        m_Name = table.m_Name;
        m_CatalogName = table.m_CatalogName;
        m_SchemaName = table.m_SchemaName;
        m_Description = table.m_Description;
        m_xColumns = new ColumnContainer(connection, table.getColumns());
        m_xKeys = new KeyContainer(connection, table.getKeys(), table);
        registerProperties();
        System.out.println("sdbcx.TableDescriptor() ***************************************************");
    }*/

    private void registerProperties() {
        registerProperty(PropertyIds.CATALOGNAME.name, PropertyIds.CATALOGNAME.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CatalogName;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_CatalogName = (String) value;
                }
            });
        registerProperty(PropertyIds.SCHEMANAME.name, PropertyIds.SCHEMANAME.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_SchemaName;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_SchemaName = (String) value;
                }
            });
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Description;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Description = (String) value;
                }
            });
    }



    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdbcx.descriptors.TableDescriptorBase.getColumns()");
        return m_columns;
    }


    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        System.out.println("sdbcx.descriptors.TableDescriptorBase.getKeys()");
        return m_keys;
    }


}
