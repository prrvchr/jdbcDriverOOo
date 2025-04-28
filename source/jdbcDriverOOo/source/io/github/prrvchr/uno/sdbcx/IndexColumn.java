/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.Type;

import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class IndexColumn
    extends ColumnBase {
    private static final String SERVICE = IndexColumn.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.IndexColumn"};

    private boolean mIsAscending = false;


    // The constructor method:
    public IndexColumn(final TableSuper table,
                       final boolean sensitive,
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
        super(SERVICE, SERVICES, table, sensitive, name, typename, defaultvalue,
              description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        mIsAscending = ascending;
        registerProperties();
    }


    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.ISASCENDING.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsAscending;
                },
                null));

        super.registerProperties(properties);
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
