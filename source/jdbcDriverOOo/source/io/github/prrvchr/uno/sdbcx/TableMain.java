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

import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;


public abstract class TableMain
    extends Descriptor {

    protected String mCatalogName = "";
    protected String mSchemaName = "";

    // The constructor method:
    protected TableMain(String service,
                        String[] services,
                        String catalog,
                        String schema,
                        String name,
                        boolean sensitive) {
        super(service, services, sensitive, name);
        mCatalogName = catalog;
        mSchemaName = schema;
    }
    protected TableMain(String service,
                        String[] services,
                        boolean sensitive) {
        super(service, services, sensitive);
        mCatalogName = "";
        mSchemaName = "";
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short attribute;
        PropertySetter catalogSetter, schemaSetter;

        if (mReadonly) {
            attribute = PropertyAttribute.READONLY;
            catalogSetter = null;
            schemaSetter = null;
        } else {
            attribute = 0;
            catalogSetter = value -> {
                mCatalogName = (String) value;
            };
            schemaSetter = value -> {
                mSchemaName = (String) value;
            };
        }

        properties.put(PropertyIds.CATALOGNAME.getName(),
            new PropertyWrapper(Type.STRING, attribute,
                () -> {
                    return mCatalogName;
                },
                catalogSetter));

        properties.put(PropertyIds.SCHEMANAME.getName(),
            new PropertyWrapper(Type.STRING, attribute,
                () -> {
                    return mSchemaName;
                },
                schemaSetter));

        super.registerProperties(properties);
    }

    // Method for internal use (no UNO method)
    protected String getCatalogName() {
        return mCatalogName;
    }

    protected String getSchemaName() {
        return mSchemaName;
    }

    protected NamedComponent getNamedComponents() {
        return new NamedComponent(mCatalogName, mSchemaName, getName());
    }

}
