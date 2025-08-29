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

import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;


public final class IndexDescriptor
    extends Descriptor
    implements XColumnsSupplier {

    private static final String SERVICE = IndexDescriptor.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.IndexDescriptor"};

    private IndexColumnDescriptorContainer mColumns;

    private String mCatalog = "";
    private boolean mIsUnique;
    private boolean mIsClustered;

    // The constructor method:
    public IndexDescriptor(boolean sensitive) {
        super(SERVICE, SERVICES, sensitive);
        mColumns = new IndexColumnDescriptorContainer(this, isCaseSensitive());
        registerProperties();
        System.out.println("sdbcx.descriptors.IndexDescriptor()");
    }

    private void registerProperties() {
        Map<PropertyID, PropertyWrapper> properties = new HashMap<PropertyID, PropertyWrapper>();

        properties.put(PropertyID.CATALOG,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mCatalog;
                },
                value -> {
                    mCatalog = (String) value;
                }));

        properties.put(PropertyID.ISCLUSTERED,
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mIsClustered;
                },
                value -> {
                    mIsClustered = (boolean) value;
                }));

        properties.put(PropertyID.ISUNIQUE,
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mIsUnique;
                },
                value -> {
                    mIsUnique = (boolean) value;
                }));

        super.registerProperties(properties);
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public IndexColumnDescriptorContainer getColumns() {
        System.out.println("sdbcx.descriptors.IndexDescriptor.getColumns()");
        return mColumns;
    }

}
