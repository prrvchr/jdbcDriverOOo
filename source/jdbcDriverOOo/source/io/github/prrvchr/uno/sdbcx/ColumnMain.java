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


public abstract class ColumnMain
    extends Descriptor {

    private String mDefaultValue = "";
    private String mDescription = "";
    private boolean mIsAutoIncrement;
    private int mIsNullable;
    private int mType;
    private String mTypeName = "";

    private boolean mIsCurrency;
    private boolean mIsRowVersion;
    private int mPrecision;
    private int mScale;


    // The constructor method:
    public ColumnMain(final String service,
                      final String[] services,
                      final ColumnMain column) {
        super(service, services, column);
        mColumn = column;
    }

    // The constructor method:
    public ColumnMain(final String service,
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
        super(service, services, sensitive, name);
        mTypeName = typename;
        mDescription = description;
        mDefaultValue = defaultvalue;
        mIsNullable = nullable;
        mPrecision = precision;
        mScale = scale;
        mType = type;
        mIsAutoIncrement = autoincrement;
        mIsRowVersion = rowversion;
        mIsCurrency = currency;
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyID.DEFAULTVALUE,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return getDefaultValue();
                },
                null));

        properties.put(PropertyID.DESCRIPTION,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return getDescription();
                },
                null));

        properties.put(PropertyID.ISAUTOINCREMENT,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return getIsAutoIncrement();
                },
                null));

        properties.put(PropertyID.ISCURRENCY,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return getIsCurrency();
                },
                null));

        properties.put(PropertyID.ISNULLABLE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getIsNullable();
                },
                null));

        properties.put(PropertyID.ISROWVERSION,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return getIsRowVersion();
                },
                null));

        properties.put(PropertyID.PRECISION,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getPrecision();
                },
                null));

        properties.put(PropertyID.SCALE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getScale();
                },
                null));

        properties.put(PropertyID.TYPE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getType();
                },
                null));

        properties.put(PropertyID.TYPENAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return getTypeName();
                },
                null));

        super.registerProperties(properties);
    }

    private String getDefaultValue() {
        String value;
        if (mColumn != null) {
            value = mColumn.getDefaultValueInternal();
        } else {
            value = mDefaultValue;
        }
        return value;
    }


    // Private methods
    private String getDescription() {
        String value;
        if (mColumn != null) {
            value = mColumn.getDescriptionInternal();
        } else {
            value = mDescription;
        }
        return value;
    }

    private boolean getIsAutoIncrement() {
        Boolean value;
        if (mColumn != null) {
            value = mColumn.getIsAutoIncrementInternal();
        } else {
            value = mIsAutoIncrement;
        }
        return value;
    }

    private boolean getIsCurrency() {
        Boolean value;
        if (mColumn != null) {
            value = mColumn.getIsCurrencyInternal();
        } else {
            value = mIsCurrency;
        }
        return value;
    }

    private int getIsNullable() {
        int value;
        if (mColumn != null) {
            value = mColumn.getIsNullableInternal();
        } else {
            value = mIsNullable;
        }
        return value;
    }

    private boolean getIsRowVersion() {
        Boolean value;
        if (mColumn != null) {
            value = mColumn.getIsRowVersionInternal();
        } else {
            value = mIsRowVersion;
        }
        return value;
    }

    private int getPrecision() {
        int value;
        if (mColumn != null) {
            value = mColumn.getPrecisionInternal();
        } else {
            value = mPrecision;
        }
        return value;
    }

    private int getScale() {
        int value;
        if (mColumn != null) {
            value = mColumn.getScaleInternal();
        } else {
            value = mScale;
        }
        return value;
    }

    private int getType() {
        int value;
        if (mColumn != null) {
            value = mColumn.getTypeInternal();
        } else {
            value = mType;
        }
        return value;
    }

    private String getTypeName() {
        String value;
        if (mColumn != null) {
            value = mColumn.getTypeNameInternal();
        } else {
            value = mTypeName;
        }
        return value;
    }


    // Protected methods
    protected String getDefaultValueInternal() {
        return mDefaultValue;
    }

    protected String getDescriptionInternal() {
        return mDescription;
    }

    protected boolean getIsAutoIncrementInternal() {
        return mIsAutoIncrement;
    }

    protected boolean getIsCurrencyInternal() {
        return mIsCurrency;
    }

    protected int getIsNullableInternal() {
        return mIsNullable;
    }

    protected boolean getIsRowVersionInternal() {
        return mIsRowVersion;
    }

    protected int getPrecisionInternal() {
        return mPrecision;
    }

    protected int getScaleInternal() {
        return mScale;
    }

    protected int getTypeInternal() {
        return mType;
    }

    protected String getTypeNameInternal() {
        return mTypeName;
    }

    protected void setDefaultValueInternal(String value) {
        mDefaultValue = value;
    }


    protected void setDescriptionInternal(String value) {
        mDescription = value;
    }

    protected void setIsAutoIncrementInternal(boolean value) {
        mIsAutoIncrement = value;
    }

    protected void setIsCurrencyInternal(boolean value) {
        mIsCurrency = value;
    }

    protected void setIsNullableInternal(int value) {
        mIsNullable = value;
    }

    protected void setIsRowVersionInternal(boolean value) {
        mIsRowVersion = value;
    }

    protected void setPrecisionInternal(int value) {
        mPrecision = value;
    }

    protected void setScaleInternal(int value) {
        mScale = value;
    }

    protected void setTypeInternal(int value) {
        mType = value;
    }

    protected void setTypeNameInternal(String value) {
        mTypeName = value;
    }

}
