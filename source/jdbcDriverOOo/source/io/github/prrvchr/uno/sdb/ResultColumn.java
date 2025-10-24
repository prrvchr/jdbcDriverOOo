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
package io.github.prrvchr.uno.sdb;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.sdbcx.ColumnBase;


public final class ResultColumn
    extends ColumnBase {
    private static final String SERVICE = ResultColumn.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.ResultColumn"};
    private static final int FORMAT_KEY = 256;
    private static final int WIDTH = 100;

    // XXX: com.sun.star.sdb.ResultColumn properties
    private boolean mIsSearchable = false;
    private boolean mIsSigned = false;
    private boolean mIsCaseSensitive = true;
    private boolean mIsReadOnly = false;
    private boolean mIsWritable = false;
    private boolean mIsDefinitelyWritable = true;
    private int mDisplaySize = 0;
    private String mLabel;
    private String mServiceName;

    // XXX: com.sun.star.sdb.ColumnSettings properties
    private Integer mAlign = 0;
    private Integer mFormatKey = FORMAT_KEY;
    private String mControlDefault = "";
    private XPropertySet mControlModel = null;
    private String mHelpText = "";
    private boolean mHidden = false;
    private Integer mPosition;
    private Integer mRelativePosition;
    private Integer mWidth = WIDTH;

    // The constructor method:
    protected ResultColumn(final ResultSetMetaData metadata,
                           final int index,
                           final String catalog,
                           final String schema,
                           final String table,
                           final boolean sensitive,
                           final String name,
                           final String typeName,
                           final String defaultValue,
                           final String description,
                           final int nullable,
                           final int precision,
                           final int scale,
                           final int type,
                           final boolean autoincrement,
                           final boolean rowversion,
                           final boolean currency) {
        super(SERVICE, SERVICES, catalog, schema, table, sensitive, name, typeName, defaultValue,
              description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        try {
            mIsSearchable = metadata.isSearchable(index);
            mIsSigned = metadata.isSigned(index);
            mIsCaseSensitive = metadata.isCaseSensitive(index);
            mIsReadOnly = metadata.isReadOnly(index);
            mIsWritable = metadata.isWritable(index);
            mIsDefinitelyWritable = metadata.isDefinitelyWritable(index);
            mDisplaySize = metadata.getColumnDisplaySize(index);
            mLabel = metadata.getColumnLabel(index);
            mServiceName = metadata.getColumnClassName(index);
        } catch (SQLException e) { }

        registerProperties();
    }

    private void registerProperties() {
        Map<PropertyID, PropertyWrapper> properties = new HashMap<PropertyID, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        // XXX: com.sun.star.sdb.ResultColumn properties
        properties.put(PropertyID.ISSEARCHABLE,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsSearchable;
                },
                null));

        properties.put(PropertyID.ISSIGNED,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsSigned;
                },
                null));

        properties.put(PropertyID.ISCASESENSITIVE,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsCaseSensitive;
                },
                null));

        properties.put(PropertyID.ISREADONLY,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsReadOnly;
                },
                null));

        properties.put(PropertyID.ISWRITABLE,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsWritable;
                },
                null));

        properties.put(PropertyID.ISDEFINITELYWRITABLE,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsDefinitelyWritable;
                },
                null));

        properties.put(PropertyID.DISPLAYSIZE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mDisplaySize;
                },
                null));

        properties.put(PropertyID.LABEL,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mLabel;
                },
                null));

        properties.put(PropertyID.SERVICENAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mServiceName;
                },
                null));


       // XXX: com.sun.star.sdb.ColumnSettings properties
        properties.put(PropertyID.ALIGN,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mAlign;
                },
                null));

        properties.put(PropertyID.CONTROLDEFAULT,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mControlDefault;
                },
                null));

        properties.put(PropertyID.CONTROLMODEL,
            new PropertyWrapper(Type.ANY, readonly,
                () -> {
                    return mControlModel;
                },
                null));

        properties.put(PropertyID.FORMATKEY,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mFormatKey;
                },
                null));

        properties.put(PropertyID.HELPTEXT,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mHelpText;
                },
                null));

        properties.put(PropertyID.HIDDEN,
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mHidden;
                },
                null));

        properties.put(PropertyID.POSITION,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mPosition;
                },
                null));

        properties.put(PropertyID.WIDTH,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mWidth;
                },
                null));

        // XXX: Does this property really needed?
        properties.put(PropertyID.RELATIVEPOSITION,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mRelativePosition;
                },
                null));

        super.registerProperties(properties);
    }

}
