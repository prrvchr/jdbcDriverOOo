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

import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Type;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public abstract class TableDescriptorSuper
    extends TableMain
    implements XColumnsSupplier,
               XKeysSupplier {

    protected ColumnDescriptorContainerSuper<?> mColumns;
    private KeyDescriptorContainer mKeys;
    private String mDescription = "";
    private String mType = "TABLE";

    // The constructor method:
    public TableDescriptorSuper(String service,
                                String[] services,
                                boolean sensitive) {
        super(service, services, sensitive);
        mKeys = new KeyDescriptorContainer(this, sensitive);
        System.out.println("sdbcx.TableDescriptorSuper()");
    }

    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.DESCRIPTION.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mDescription;
                },
                value -> {
                    mDescription = (String) value;
                }));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mType;
                },
                value -> {
                    mType = (String) value;
                }));

        super.registerProperties(properties);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns() {
        System.out.println("sdbcx.descriptors.TableDescriptorBase.getColumns()");
        return mColumns;
    }


    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        System.out.println("sdbcx.descriptors.TableDescriptorBase.getKeys()");
        return mKeys;
    }

}
