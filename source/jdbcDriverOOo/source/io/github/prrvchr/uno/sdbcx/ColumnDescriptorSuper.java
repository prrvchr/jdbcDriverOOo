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

public abstract class ColumnDescriptorSuper
    extends ColumnDescriptorBase {

    protected String mAutoIncrementCreation = "";
    private TableMain mTable;

    // The constructor method:
    public ColumnDescriptorSuper(final String service,
                                 final String[] services,
                                 final TableMain table,
                                 final boolean sensitive) {
        super(service, services, sensitive);
        mTable = table;
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        // FIXME: Although these properties are not in the UNO API, they are claimed by
        // FIXME: LibreOffice/Base and necessary to obtain tables whose contents can be edited in Base
        properties.put(PropertyID.CATALOGNAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mTable.getCatalogName();
                },
                null));

        properties.put(PropertyID.SCHEMANAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mTable.getSchemaName();
                },
                null));

        properties.put(PropertyID.TABLENAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mTable.getName();
                },
                null));

        properties.put(PropertyID.AUTOINCREMENTCREATION,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mAutoIncrementCreation;
                },
                value -> {
                    mAutoIncrementCreation = (String) value;
                }));

        super.registerProperties(properties);
    }

}
