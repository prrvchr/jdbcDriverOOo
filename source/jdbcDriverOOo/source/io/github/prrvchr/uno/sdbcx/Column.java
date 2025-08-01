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

import com.sun.star.beans.XPropertySet;

import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;

public final class Column
    extends ColumnSuper {
    private static final String SERVICE = Column.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Column"};

    // The constructor method:
    public Column(final ConnectionSuper connection,
                  final String catalog,
                  final String schema,
                  final String table,
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
                  final boolean currency) {
        super(SERVICE, SERVICES, connection, catalog, schema, table, sensitive, name, typename, defaultvalue,
              description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }

    // XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        ColumnDescriptor descriptor = new ColumnDescriptor(mCatalog, mSchema, mTable, isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }


}
