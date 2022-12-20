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

import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public abstract class ColumnDescriptorBase
    extends Descriptor
{

    protected int m_Type;
    protected String m_TypeName = "";
    protected int m_Precision;
    protected int m_Scale;
    protected int m_IsNullable;
    protected boolean m_IsAutoIncrement;
    protected boolean m_IsRowVersion;
    protected String m_Description = "";
    protected String m_DefaultValue = "";
    protected boolean m_IsCurrency;


    // The constructor method:
    public ColumnDescriptorBase(String service,
                                String[] services,
                                boolean sensitive)
    {
        super(service, services, sensitive);
        System.out.println("sdbcx.descriptors.ColumnDescriptor()");
        registerProperties();
    }

    private void registerProperties() {
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.LONG,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_Type;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_Type = (int) value;
                    }
                });
        registerProperty(PropertyIds.TYPENAME.name, PropertyIds.TYPENAME.id, Type.STRING,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_TypeName;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_TypeName = (String) value;
                    }
                });
        registerProperty(PropertyIds.PRECISION.name, PropertyIds.PRECISION.id, Type.LONG,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_Precision;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_Precision = (Integer) value;
                    }
                });
        registerProperty(PropertyIds.SCALE.name, PropertyIds.SCALE.id, Type.LONG,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_Scale;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_Scale = (Integer) value;
                    }
                });
        registerProperty(PropertyIds.ISNULLABLE.name, PropertyIds.ISNULLABLE.id, Type.LONG,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_IsNullable;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_IsNullable = (Integer) value;
                    }
                });
        registerProperty(PropertyIds.ISAUTOINCREMENT.name, PropertyIds.ISAUTOINCREMENT.id, Type.BOOLEAN,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_IsAutoIncrement;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_IsAutoIncrement = (Boolean) value;
                    }
                });
        registerProperty(PropertyIds.ISROWVERSION.name, PropertyIds.ISROWVERSION.id, Type.BOOLEAN,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_IsRowVersion;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_IsRowVersion = (Boolean) value;
                    }
                });
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_Description;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_Description = (String) value;
                    }
                });
        registerProperty(PropertyIds.DEFAULTVALUE.name, PropertyIds.DEFAULTVALUE.id, Type.STRING,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_DefaultValue;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_DefaultValue = (String) value;
                    }
                });
        registerProperty(PropertyIds.ISCURRENCY.name, PropertyIds.ISCURRENCY.id, Type.BOOLEAN,
                new PropertyGetter() {
                    @Override
                    public Object getValue() {
                        return m_IsCurrency;
                        
                    }
                },
                new PropertySetter() {
                    @Override
                    public void setValue(Object value) {
                        m_IsCurrency = (Boolean) value;
                    }
                });
    }


}
