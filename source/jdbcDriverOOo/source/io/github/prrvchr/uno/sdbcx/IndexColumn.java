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
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;


public class IndexColumn
    extends ColumnMain
{
    private static final String m_service = IndexColumn.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.IndexColumn"};

    private boolean m_IsAscending = false;


    // The constructor method:
    public IndexColumn(final boolean sensitive,
                       final String catalog,
                       final String schema,
                       final String table,
                       final String name,
                       final String typename,
                       final String defaultvalue,
                       final String description,
                       final int nullable,
                       final int precision,
                       final int scale,
                       final int type,
                       final boolean autoincrement,
                       final boolean rowversion,
                       final boolean currency,
                       final boolean ascending) {
        super(m_service, m_services, sensitive, name, typename, defaultvalue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        m_IsAscending = ascending;
        registerProperties();
    }


    private void registerProperties() {
        short attribute = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.ISASCENDING.name, PropertyIds.ISASCENDING.id, Type.BOOLEAN, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsAscending;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        IndexColumnDescriptor descriptor = new IndexColumnDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }


}
