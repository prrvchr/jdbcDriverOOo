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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public abstract class ColumnDescriptorBase
    extends Descriptor
{

    protected String m_DefaultValue = "";
    protected String m_Description = "";
    protected boolean m_IsAutoIncrement;
    protected boolean m_IsCurrency;
    protected int m_IsNullable;
    protected boolean m_IsRowVersion;
    protected int m_Precision;
    protected int m_Scale;
    protected int m_Type;
    protected String m_TypeName = "";

    // The constructor method:
    public ColumnDescriptorBase(String service,
                                String[] services,
                                boolean sensitive)
    {
        super(service, services, sensitive);
        System.out.println("sdbcx.ColumnDescriptorBase()");
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.DEFAULTVALUE.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_DefaultValue;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_DefaultValue = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.DESCRIPTION.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Description;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Description = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.ISAUTOINCREMENT.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsAutoIncrement;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_IsAutoIncrement = (boolean) value;
                                               }
                                           }));

        properties.put(PropertyIds.ISCURRENCY.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsCurrency;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_IsCurrency = (boolean) value;
                                               }
                                           }));

        properties.put(PropertyIds.ISNULLABLE.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsNullable;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_IsNullable = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.ISROWVERSION.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_IsRowVersion;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_IsRowVersion = (boolean) value;
                                               }
                                           }));

        properties.put(PropertyIds.PRECISION.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Precision;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Precision = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.SCALE.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Scale;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Scale = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.TYPE.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Type;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Type = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.TYPENAME.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_TypeName;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_TypeName = (String) value;
                                               }
                                           }));

        super.registerProperties(properties);
    }

}
