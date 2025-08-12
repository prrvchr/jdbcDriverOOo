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

import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbcx.ColumnDescriptorSuper;
import io.github.prrvchr.uno.sdbcx.TableMain;


public final class ColumnDescriptor
    extends ColumnDescriptorSuper {
    private static final String SERVICE = ColumnDescriptor.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.ColumnDescriptor"};

    @SuppressWarnings("unused")
    private int mAlign = 0;
    @SuppressWarnings("unused")
    private String mControlDefault = null;
    @SuppressWarnings("unused")
    private XPropertySet mControlModel = null;
    @SuppressWarnings("unused")
    private Integer mFormatKey;
    @SuppressWarnings("unused")
    private String mHelpText = null;
    private boolean mHidden = false;
    private Integer mPosition;
    @SuppressWarnings("unused")
    private Integer mWidth;

    // The constructor method:
    public ColumnDescriptor(TableMain table,
                            boolean sensitive) {
        super(SERVICE, SERVICES, table, sensitive);
        registerProperties();
        System.out.println("sdb.ColumnDescriptor()");
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short maybevoid = PropertyAttribute.MAYBEVOID;

        /*properties.put(PropertyIds.ALIGN.getName(),
            new PropertyWrapper(Type.LONG, maybevoid,
               () -> {
                    return m_Align;
               },
               value -> {
                    m_Align = (int) value;
               }));

        properties.put(PropertyIds.CONTROLDEFAULT.getName(),
           new PropertyWrapper(Type.STRING, maybevoid,
               () -> {
                    return m_ControlDefault;
               },
               value -> {
                    m_ControlDefault = (String) value;
               }));

        properties.put(PropertyIds.CONTROLMODEL.getName(),
           new PropertyWrapper(new Type(XPropertySet.class), maybevoid,
               () -> {
                    return m_ControlModel;
               },
               value -> {
                    m_ControlModel = (XPropertySet) value;
               }));

        properties.put(PropertyIds.FORMATKEY.getName(),
            new PropertyWrapper(Type.LONG, maybevoid,
               () -> {
                    return m_FormatKey;
               },
               value -> {
                    m_FormatKey = (Integer) value;
               }));

        properties.put(PropertyIds.HELPTEXT.getName(),
           new PropertyWrapper(Type.STRING, maybevoid,
               () -> {
                    return m_HelpText;
               },
               value -> {
                    m_HelpText = (String) value;
               }));*/

        properties.put(PropertyIds.HIDDEN.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mHidden;
                },
                value -> {
                    mHidden = (boolean) value;
                }));

        properties.put(PropertyIds.POSITION.getName(),
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return mPosition;
                },
                value -> {
                    mPosition = (Integer) value;
                }));

        /*properties.put(PropertyIds.WIDTH.getName(),
            new PropertyWrapper(Type.LONG, maybevoid,
                () -> {
                    return m_Width;
                },
                value -> {
                    m_Width = (Integer) value;
                }));*/

        super.registerProperties(properties);
    }

}
