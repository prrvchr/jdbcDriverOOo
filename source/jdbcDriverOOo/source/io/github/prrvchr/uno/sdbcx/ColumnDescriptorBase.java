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

import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public abstract class ColumnDescriptorBase
    extends Descriptor {

    protected String mDefaultValue = "";
    protected String mDescription = "";
    protected boolean mIsAutoIncrement;
    protected boolean mIsCurrency;
    protected int mIsNullable;
    protected boolean mIsRowVersion;
    protected int mPrecision;
    protected int mScale;
    protected int mType;
    protected String mTypeName = "";

    // The constructor method:
    public ColumnDescriptorBase(String service,
                                String[] services,
                                boolean sensitive) {
        super(service, services, sensitive);
        System.out.println("sdbcx.ColumnDescriptorBase()");
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.DEFAULTVALUE.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mDefaultValue;
                },
                value -> {
                    mDefaultValue = (String) value;
                }));

        properties.put(PropertyIds.DESCRIPTION.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mDescription;
                },
                value -> {
                    mDescription = (String) value;
                }));

        properties.put(PropertyIds.ISAUTOINCREMENT.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mIsAutoIncrement;
                },
                value -> {
                    mIsAutoIncrement = (boolean) value;
                }));

        properties.put(PropertyIds.ISCURRENCY.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mIsCurrency;
                },
                value -> {
                    mIsCurrency = (boolean) value;
                }));

        properties.put(PropertyIds.ISNULLABLE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mIsNullable;
                },
                value -> {
                    mIsNullable = (int) value;
                }));

        registerProperties2(properties);
    }

    private void registerProperties2(Map<String, PropertyWrapper> properties) {


        properties.put(PropertyIds.ISROWVERSION.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mIsRowVersion;
                },
                value -> {
                    mIsRowVersion = (boolean) value;
                }));

        properties.put(PropertyIds.PRECISION.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mPrecision;
                },
                value -> {
                    mPrecision = (int) value;
                }));

        properties.put(PropertyIds.SCALE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mScale;
                },
                value -> {
                    mScale = (int) value;
                }));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mType;
                },
                value -> {
                    mType = (int) value;
                }));

        properties.put(PropertyIds.TYPENAME.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mTypeName;
                },
                value -> {
                    mTypeName = (String) value;
                }));

        super.registerProperties(properties);
    }

}
