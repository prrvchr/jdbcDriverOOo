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


public abstract class ColumnMain
    extends Descriptor {

    protected String mDefaultValue = "";
    protected String mDescription = "";
    protected boolean mIsAutoIncrement;
    protected int mIsNullable;
    protected int mType;
    protected String mTypeName = "";
    protected final String mCatalog;
    protected final String mSchema;
    protected final String mTable;
    private boolean mIsCurrency;
    private boolean mIsRowVersion;
    private int mPrecision;
    private int mScale;

    // The constructor method:
    public ColumnMain(final String service,
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
        super(service, services, sensitive, name);
        mCatalog = catalog;
        mSchema = schema;
        mTable = table;
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
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.DEFAULTVALUE.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mDefaultValue;
                },
                null));

        properties.put(PropertyIds.DESCRIPTION.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mDescription;
                },
                null));

        properties.put(PropertyIds.ISAUTOINCREMENT.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsAutoIncrement;
                },
                null));

        properties.put(PropertyIds.ISCURRENCY.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsCurrency;
                },
                null));

        properties.put(PropertyIds.ISNULLABLE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mIsNullable;
                },
                null));

        properties.put(PropertyIds.ISROWVERSION.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsRowVersion;
                },
                null));

        properties.put(PropertyIds.PRECISION.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mPrecision;
                },
                null));

        properties.put(PropertyIds.SCALE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mScale;
                },
                null));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mType;
                },
                null));

        properties.put(PropertyIds.TYPENAME.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mTypeName;
                },
                null));

        super.registerProperties(properties);
    }

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing() {
        super.postDisposing();
    }

}
