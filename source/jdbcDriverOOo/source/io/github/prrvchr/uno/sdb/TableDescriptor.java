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

import com.sun.star.awt.FontDescriptor;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.sdbcx.TableDescriptorSuper;


public final class TableDescriptor
    extends TableDescriptorSuper {
    private static final String SERVICE = TableDescriptor.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.TableDescriptor",
                                              "com.sun.star.sdb.DataSettings",
                                              "com.sun.star.sdbcx.TableDescriptor",
                                              "com.sun.star.sdbcx.Descriptor"};
    private static final int ROW_HEIGHT = 15;

    protected boolean mApplyFilter = false;
    protected String mFilter = "";
    protected FontDescriptor mFontDescriptor = null;
    protected String mGroupBy = "";
    protected String mHavingClause = "";
    protected String mOrder = "";
    protected int mRowHeight = ROW_HEIGHT;
    protected int mTextColor = 0;
    private int mPrivileges = 0;

    // The constructor method:
    public TableDescriptor(boolean sensitive) {
        super(SERVICE, SERVICES, sensitive);
        mColumns = new ColumnDescriptorContainer(this, sensitive);
        registerProperties();
        System.out.println("sdb.TableDescriptor()");
    }

    private void registerProperties() {

        Map<PropertyID, PropertyWrapper> properties = new HashMap<PropertyID, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyID.APPLYFILTER,
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mApplyFilter;
                },
                value -> {
                    mApplyFilter = (boolean) value;
                }));

        properties.put(PropertyID.FILTER,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mFilter;
                },
                value -> {
                    mFilter = (String) value;
                }));

        properties.put(PropertyID.FONTDESCRIPTOR,
            new PropertyWrapper(new Type(FontDescriptor.class),
                () -> {
                    return mFontDescriptor;
                },
                value -> {
                    mFontDescriptor = (FontDescriptor) value;
                }));

        properties.put(PropertyID.GROUPBY,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mGroupBy;
                },
                value -> {
                    mGroupBy = (String) value;
                }));

        properties.put(PropertyID.HAVINGCLAUSE,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mHavingClause;
                },
                value -> {
                    mHavingClause = (String) value;
                }));

        registerProperties(properties, readonly);
    }

    private void registerProperties(Map<PropertyID, PropertyWrapper> properties, short readonly) {

        properties.put(PropertyID.ORDER,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mOrder;
                },
                value -> {
                    mOrder = (String) value;
                }));

        properties.put(PropertyID.PRIVILEGES,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    System.out.println("sdb.Table.getPrivileges() 1 Privileges: " + mPrivileges);
                    return mPrivileges;
                },
                value -> {
                    mPrivileges = (int) value;
                }));

        properties.put(PropertyID.ROWHEIGHT,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mRowHeight;
                },
                value -> {
                    mRowHeight = (int) value;
                }));

        properties.put(PropertyID.TEXTCOLOR,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mTextColor;
                },
                value -> {
                    mTextColor = (int) value;
                }));

        super.registerProperties(properties);
    }

    public String getName() {
        return super.getName();
    }

}
