/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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

import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Type;

import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public final class KeyDescriptor
    extends Descriptor
    implements XColumnsSupplier {

    private static final String SERVICE = KeyDescriptor.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.KeyDescriptor"};

    private KeyColumnDescriptorContainer mColumns = null;

    private int mType;
    private String mReferencedTable = "";
    private int mUpdateRule;
    private int mDeleteRule;


    // The constructor method:
    public KeyDescriptor(boolean sensitive) {
        super(SERVICE, SERVICES, sensitive);
        mColumns = new KeyColumnDescriptorContainer(this, isCaseSensitive());
        registerProperties();
        System.out.println("sdbcx.descriptors.KeyDescriptor()");
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();

        properties.put(PropertyIds.DELETERULE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mDeleteRule;
                },
                value -> {
                    mDeleteRule = (int) value;
                }));

        properties.put(PropertyIds.REFERENCEDTABLE.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mReferencedTable;
                },
                value -> {
                    mReferencedTable = (String) value;
                }));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mType;
                },
                value -> {
                    mType = (int) value;
                }));

        properties.put(PropertyIds.UPDATERULE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mUpdateRule;
                },
                value -> {
                    mUpdateRule = (int) value;
                }));

        super.registerProperties(properties);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns() {
        System.out.println("sdbcx.descriptors.KeyDescriptor.getColumns()");
        return mColumns;
    }


}
