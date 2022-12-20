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
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public abstract class ColumnSuper
    extends ColumnMain
    implements XDataDescriptorFactory
{

    private final ConnectionBase m_Connection;
    private final String m_CatalogName;
    private final String m_SchemaName;
    private final String m_TableName;

    // The constructor method:
    public ColumnSuper(String service,
                      String[] services,
                      ConnectionBase connection,
                      boolean sensitive,
                      String catalog,
                      String schema,
                      String table)
    {
        super(service, services, sensitive);
        m_Connection = connection;
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        registerProperties();
    }
    public ColumnSuper(String service,
                      String[] services,
                      ConnectionBase connection,
                      boolean sensitive,
                      String catalog,
                      String schema,
                      String table,
                      String name,
                      final String typename,
                      final String defaultvalue,
                      final String description,
                      final int nullable,
                      final int precision,
                      final int scale,
                      final int type,
                      final boolean autoincrement,
                      final boolean rowversion,
                      final boolean currency)
    {
        super(service, services, sensitive, name, typename, defaultvalue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        m_Connection = connection;
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        registerProperties();
    }

    // FIXME: Although these properties are not in the UNO API, they are claimed by
    // FIXME: LibreOffice/Base and necessary to obtain tables whose contents can be edited in Base
    private void registerProperties() {
        short attribute = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CATALOGNAME.name, PropertyIds.CATALOGNAME.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CatalogName;
                }
            }, null);
        registerProperty(PropertyIds.SCHEMANAME.name, PropertyIds.SCHEMANAME.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_SchemaName;
                }
            }, null);
        registerProperty(PropertyIds.TABLENAME.name, PropertyIds.TABLENAME.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_TableName;
                }
            }, null);
        registerProperty(PropertyIds.AUTOINCREMENTCREATION.name, PropertyIds.AUTOINCREMENTCREATION.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Connection.getProvider().getAutoIncrementCreation();
                }
            }, null);
    }

    // XDataDescriptorFactory
    
    @Override
    public XPropertySet createDataDescriptor()
    {
        ColumnDescriptor descriptor = new ColumnDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }


}
