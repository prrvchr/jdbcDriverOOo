/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import com.sun.star.awt.FontDescriptor;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.sdbcx.TableDescriptorBase;


public final class TableDescriptor
    extends TableDescriptorBase
{

    private static final String m_service = TableDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.TableDescriptor",
                                                "com.sun.star.sdb.DataSettings",
                                                "com.sun.star.sdbcx.TableDescriptor",
                                                "com.sun.star.sdbcx.Descriptor"};
    private String m_Filter = "";
    private boolean m_ApplyFilter;
    private String m_Order = "";
    private FontDescriptor m_FontDescriptor = null;
    private int m_RowHeight;
    private int m_TextColor;
    private String m_HavingClause = "";
    private String m_GroupBy = "";

    // The constructor method:
    public TableDescriptor(boolean sensitive)
    {
        super(m_service, m_services, sensitive);
        registerProperties();
        System.out.println("sdb.TableDescriptor()");
    }


    private void registerProperties() {
        registerProperty(PropertyIds.FILTER.name, PropertyIds.FILTER.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Filter;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Filter = (String) value;
                }
            });
        registerProperty(PropertyIds.APPLYFILTER.name, PropertyIds.APPLYFILTER.id, Type.BOOLEAN,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_ApplyFilter;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_ApplyFilter = (boolean) value;
                }
            });
        registerProperty(PropertyIds.ORDER.name, PropertyIds.ORDER.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Order;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Order = (String) value;
                }
            });
        registerProperty(PropertyIds.FONTDESCRIPTOR.name, PropertyIds.FONTDESCRIPTOR.id, new Type(FontDescriptor.class),
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_FontDescriptor;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_FontDescriptor = (FontDescriptor) value;
                }
            });
        registerProperty(PropertyIds.ROWHEIGHT.name, PropertyIds.ROWHEIGHT.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_RowHeight;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_RowHeight = (int) value;
                }
            });
        registerProperty(PropertyIds.TEXTCOLOR.name, PropertyIds.TEXTCOLOR.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_TextColor;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_TextColor = (int) value;
                }
            });
        registerProperty(PropertyIds.HAVINGCLAUSE.name, PropertyIds.HAVINGCLAUSE.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_HavingClause;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_HavingClause = (String) value;
                }
            });
        registerProperty(PropertyIds.GROUPBY.name, PropertyIds.GROUPBY.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_GroupBy;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_GroupBy = (String) value;
                }
            });
    }


/*    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableContainer.createDataDescriptor()
    public TableDescriptor(Connection connection)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        registerProperties();
        System.out.println("sdb.TableDescriptor()");
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.Table.createDataDescriptor()
    public TableDescriptor(Connection connection,
                           Table table)
    {
        super(m_name, m_services, connection, table);
        m_Filter = table.m_Filter;
        m_ApplyFilter = table.m_ApplyFilter;
        m_Order = table.m_Order;
        //m_FontDescriptor = table.m_FontDescriptor;
        m_RowHeight = table.m_RowHeight;
        m_TextColor = table.m_TextColor;
        m_HavingClause = table.m_HavingClause;
        m_GroupBy = table.m_GroupBy;
        registerProperties();
        System.out.println("sdb.TableDescriptor()");
    }*/

}
