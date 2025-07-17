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

import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public abstract class ColumnBase
    extends ColumnMain {

    // The constructor method:
    public ColumnBase(final String service,
                      final String[] services,
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
        super(service, services, catalog, schema, table, sensitive, name, typename, defaultvalue,
              description, nullable, precision, scale, type, autoincrement, rowversion, currency);
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        // FIXME: Although these properties are not in the UNO API, they are claimed by
        // FIXME: LibreOffice/Base and necessary to obtain tables whose contents can be edited in Base
        properties.put(PropertyIds.CATALOGNAME.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mCatalog;
                },
                null));

        properties.put(PropertyIds.SCHEMANAME.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mSchema;
                },
                null));

        properties.put(PropertyIds.TABLENAME.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mTable;
                },
                null));

        super.registerProperties(properties);
    }

}
