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

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class ColumnBase<T extends TableSuper<?>>
    extends Descriptor
    implements XDataDescriptorFactory
{

    protected final T m_table;
    protected int m_Type;
    private int m_Precision;
    private int m_Scale;
    protected int m_IsNullable;
    private boolean m_IsCurrency;
    protected boolean m_IsAutoIncrement;
    private boolean m_IsRowVersion;
    protected String m_TypeName = "";
    protected String m_Description = "";
    protected String m_DefaultValue = "";

    // The constructor method:
    public ColumnBase(String service,
                      String[] services,
                      T table,
                      boolean sensitive,
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
        super(service, services, sensitive, name);
        m_table = table;
        m_TypeName = typename;
        m_Description = description;
        m_DefaultValue = defaultvalue;
        m_IsNullable = nullable;
        m_Precision = precision;
        m_Scale = scale;
        m_Type = type;
        m_IsAutoIncrement = autoincrement;
        m_IsRowVersion = rowversion;
        m_IsCurrency = currency;
        registerProperties();
    }

    private void registerProperties() {
        short attribute = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.LONG, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
        registerProperty(PropertyIds.TYPENAME.name, PropertyIds.TYPENAME.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_TypeName;
                }
            }, null);
        registerProperty(PropertyIds.PRECISION.name, PropertyIds.PRECISION.id, Type.LONG, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Precision;
                }
            }, null);
        registerProperty(PropertyIds.SCALE.name, PropertyIds.SCALE.id, Type.LONG, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Scale;
                }
            }, null);
        registerProperty(PropertyIds.ISNULLABLE.name, PropertyIds.ISNULLABLE.id, Type.LONG, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsNullable;
                }
            }, null);
        registerProperty(PropertyIds.ISAUTOINCREMENT.name, PropertyIds.ISAUTOINCREMENT.id, Type.BOOLEAN, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsAutoIncrement;
                }
            }, null);
        registerProperty(PropertyIds.ISROWVERSION.name, PropertyIds.ISROWVERSION.id, Type.BOOLEAN, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsRowVersion;
                }
            }, null);
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Description;
                }
            }, null);
        registerProperty(PropertyIds.DEFAULTVALUE.name, PropertyIds.DEFAULTVALUE.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_DefaultValue;
                }
            }, null);
        registerProperty(PropertyIds.ISCURRENCY.name, PropertyIds.ISCURRENCY.id, Type.BOOLEAN, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() {
                    return m_IsCurrency;
                    
                }
            }, null);
    }

    // XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing()
    {
        super.postDisposing();
    }


}
