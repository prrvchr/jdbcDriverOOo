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

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;

public class ColumnDescriptor
    extends ColumnDescriptorBase
{

    private static final String m_service = ColumnDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.ColumnDescriptor"};
    protected String m_AutoIncrementCreation = "";

    // The constructor method:
    public ColumnDescriptor(boolean sensitive)
    {
        super(m_service, m_services, sensitive);
        registerProperties();
        System.out.println("sdbcx.descriptors.ColumnDescriptor()");
    }

    private void registerProperties() {
        registerProperty(PropertyIds.AUTOINCREMENTCREATION.name, PropertyIds.AUTOINCREMENTCREATION.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() {
                    return m_AutoIncrementCreation;
                    
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) {
                    m_AutoIncrementCreation = (String) value;
                }
            });
    }


}