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

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;


public abstract class ColumnBase
    extends ColumnMain {

    protected String mCatalogName;
    protected String mSchemaName;
    protected String mTableName;
    protected TableSuper mTable;

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
        this(service, services, sensitive, name, typename, defaultvalue, description,
             nullable, precision, scale, type, autoincrement, rowversion, currency);
        mCatalogName = catalog;
        mSchemaName = schema;
        mTableName = table;
    }

    public ColumnBase(final String service,
                      final String[] services,
                      final TableSuper table,
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
        this(service, services, sensitive, name, typename, defaultvalue, description,
             nullable, precision, scale, type, autoincrement, rowversion, currency);
        mTable = table;
    }

    private ColumnBase(final String service,
                       final String[] services,
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
        super(service, services, sensitive, name, typename, defaultvalue, description,
              nullable, precision, scale, type, autoincrement, rowversion, currency);
    }


    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        // FIXME: Although these properties are not in the UNO API, they are claimed by
        // FIXME: LibreOffice/Base and necessary to obtain tables whose contents can be edited in Base
        properties.put(PropertyID.CATALOGNAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    String catalog;
                    if (mTable != null) {
                        catalog = mTable.getCatalogName();
                    } else {
                        catalog = mCatalogName;
                    }
                    return catalog;
                },
                null));

        properties.put(PropertyID.SCHEMANAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    String schema;
                    if (mTable != null) {
                        schema = mTable.getSchemaName();
                    } else {
                        schema = mSchemaName;
                    }
                    return schema;
                },
                null));

        properties.put(PropertyID.TABLENAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    String table;
                    if (mTable != null) {
                        table = mTable.getName();
                    } else {
                        table = mTableName;
                    }
                    return table;
                },
                null));

        super.registerProperties(properties);
    }

    protected TableSuper getTableInternal() {
        return mTable;
    }

}
