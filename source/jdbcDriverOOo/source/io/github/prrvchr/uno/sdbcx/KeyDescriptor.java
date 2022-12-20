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
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public class KeyDescriptor
    extends Descriptor
    implements XColumnsSupplier
{

    private static final String m_service = KeyDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.KeyDescriptor"};

    private KeyColumnDescriptorContainer m_columns = null;

    private int m_Type;
    private String m_ReferencedTable = "";
    private int m_UpdateRule;
    private int m_DeleteRule;


    // The constructor method:
    public KeyDescriptor(boolean sensitive)
    {
        super(m_service, m_services, sensitive);
        m_columns = new KeyColumnDescriptorContainer(this, isCaseSensitive());
        registerProperties();
        System.out.println("sdbcx.descriptors.KeyDescriptor()");
    }

    private void registerProperties() {
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Type = (int) value;
                }
            });
        registerProperty(PropertyIds.REFERENCEDTABLE.name, PropertyIds.REFERENCEDTABLE.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_ReferencedTable;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_ReferencedTable = (String) value;
                }
            });
        registerProperty(PropertyIds.UPDATERULE.name, PropertyIds.UPDATERULE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_UpdateRule;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_UpdateRule = (int) value;
                }
            });
        registerProperty(PropertyIds.DELETERULE.name, PropertyIds.DELETERULE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_DeleteRule;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_DeleteRule = (int) value;
                }
            });
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdbcx.descriptors.KeyDescriptor.getColumns()");
        return m_columns;
    }


}
