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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.ColumnDescriptor;
import io.github.prrvchr.uno.sdbcx.ColumnSuper;


public final class Column
    extends ColumnSuper {
    private static final String SERVICE = Column.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.Column",
                                              "com.sun.star.sdbcx.Column",
                                              "com.sun.star.sdb.ColumnSettings"};
    private static final int FORMAT_KEY = 256;
    private static final int WIDTH = 100;

    private Integer mAlign = 0;
    private String mControlDefault = "";
    private XPropertySet mControlModel = null;
    private Integer mFormatKey = FORMAT_KEY;
    private String mHelpText = "";
    private boolean mHidden = false;
    private Integer mPosition;
    private Integer mRelativePosition;
    private Integer mWidth = WIDTH;

    // The constructor method:
    public Column(final Table table,
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
        super(SERVICE, SERVICES, table.getConnection(), table, sensitive,
              name, typeName, defaultValue, description, nullable,
              precision, scale, type, autoincrement, rowversion, currency);
        registerProperties();
    }

    private void registerProperties() {
        Map<PropertyID, PropertyWrapper> properties = new HashMap<PropertyID, PropertyWrapper>();
        short maybevoid = PropertyAttribute.MAYBEVOID;

        properties.put(PropertyID.ALIGN,
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mAlign;
                },
                value -> {
                    mAlign = (Integer) value;
                }));

        properties.put(PropertyID.CONTROLDEFAULT,
            new PropertyWrapper(Type.STRING, maybevoid,
                () -> {
                    return mControlDefault;
                },
                value -> {
                    mControlDefault = (String) value;
                }));

        properties.put(PropertyID.CONTROLMODEL,
            new PropertyWrapper(Type.ANY, maybevoid,
                () -> {
                    return mControlModel;
                },
                value -> {
                    mControlModel = (XPropertySet) value;
                }));

        properties.put(PropertyID.FORMATKEY,
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mFormatKey;
                },
                value -> {
                    mFormatKey = (Integer) value;
                }));

        properties.put(PropertyID.HELPTEXT,
            new PropertyWrapper(Type.STRING, maybevoid,
                () -> {
                    return mHelpText;
                },
                value -> {
                    mHelpText = (String) value;
                }));

        properties.put(PropertyID.HIDDEN,
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mHidden;
                },
                value -> {
                    mHidden = (boolean) value;
                }));

        properties.put(PropertyID.POSITION,
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mPosition;
                },
                value -> {
                    mPosition = (Integer) value;
                }));

        properties.put(PropertyID.RELATIVEPOSITION,
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mRelativePosition;
                },
                value -> {
                    mRelativePosition = (Integer) value;
                }));

        properties.put(PropertyID.WIDTH,
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mWidth;
                },
                value -> {
                    mWidth = (Integer) value;
                }));

        super.registerProperties(properties);
    }


    // XDataDescriptorFactory
    
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdb.Column.createDataDescriptor() 1");

        ColumnDescriptor descriptor = new ColumnDescriptor(mTable, isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }


}
