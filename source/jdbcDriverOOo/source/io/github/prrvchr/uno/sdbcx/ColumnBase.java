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

import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class ColumnBase
    extends Descriptor
    implements XDataDescriptorFactory
{

    protected String m_DefaultValue = "";
    protected String m_Description = "";
    protected boolean m_IsAutoIncrement;
    private boolean m_IsCurrency;
    protected int m_IsNullable;
    private boolean m_IsRowVersion;
    private int m_Precision;
    private int m_Scale;
    protected int m_Type;
    protected String m_TypeName = "";

    protected final TableSuper m_table;

    // The constructor method:
    public ColumnBase(String service,
                      String[] services,
                      TableSuper table,
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
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.DEFAULTVALUE.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_DefaultValue;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.DESCRIPTION.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Description;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.ISAUTOINCREMENT.getName(),
                       new PropertyWrapper(Type.BOOLEAN, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsAutoIncrement;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.ISCURRENCY.getName(),
                       new PropertyWrapper(Type.BOOLEAN, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsCurrency;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.ISNULLABLE.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsNullable;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.ISROWVERSION.getName(),
                       new PropertyWrapper(Type.BOOLEAN, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsRowVersion;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.PRECISION.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Precision;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.SCALE.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Scale;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.TYPE.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Type;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.TYPENAME.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_TypeName;
                                               }
                                           },
                                           null));

        super.registerProperties(properties);
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
