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
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertySet;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.property.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.ServiceInfo;


public abstract class Descriptor
    extends PropertySet
    implements XServiceInfo {

    protected final String mService;
    protected final boolean mReadonly;
    protected String mName;
    protected ColumnMain mColumn;
    private final String[] mServices;
    private final boolean mSensitive;

    // The constructor method:
    public Descriptor(String service,
                      String[] services,
                      boolean sensitive) {
        this(service, services, sensitive, false);
        mName = "";
    }
    public Descriptor(String service,
                      String[] services,
                      ColumnMain column) {
        this(service, services, column.isCaseSensitive(), true);
        mColumn = column;
    }

    public Descriptor(String service,
                      String[] services,
                      boolean sensitive,
                      String name) {
        this(service, services, sensitive, true);
        mName = name;
    }

    private Descriptor(String service,
                       String[] services,
                       boolean sensitive,
                       boolean readonly) {
        mService = service;
        mServices = services;
        mSensitive = sensitive;
        mReadonly = readonly;
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short attribute;
        PropertySetter setter;

        if (mReadonly) {
            attribute = PropertyAttribute.READONLY;
            setter = null;
        } else {
            attribute = 0;
            setter = value -> {
                mName = (String) value;
            };
        }

        properties.put(PropertyID.NAME,
            new PropertyWrapper(Type.STRING, attribute,
                () -> {
                    return getName();
                },
                setter));

        super.registerProperties(properties);
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mService);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServices);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServices, service);
    }

    // Method for internal use (no UNO method)
    protected String getName() {
        String value;
        if (mColumn != null) {
            value = mColumn.mName;
        } else {
            value = mName;
        }
        return value;
    }

    protected void setName(String name) {
        mName = name;
    }
    

    protected boolean isCaseSensitive() {
        return mSensitive;
    }

}
