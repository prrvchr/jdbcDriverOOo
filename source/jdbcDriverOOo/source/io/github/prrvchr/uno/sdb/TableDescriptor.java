/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.sdbcx.TableDescriptorSuper;


public final class TableDescriptor
    extends TableDescriptorSuper
{
    private static final String m_service = TableDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.TableDescriptor",
                                                "com.sun.star.sdb.DataSettings",
                                                "com.sun.star.sdbcx.TableDescriptor",
                                                "com.sun.star.sdbcx.Descriptor"};

    protected boolean m_ApplyFilter = false;
    protected String m_Filter = "";
    protected FontDescriptor m_FontDescriptor = null;
    protected String m_GroupBy = "";
    protected String m_HavingClause = "";
    protected String m_Order = "";
    private int m_Privileges = 0;
    protected int m_RowHeight = 15;
    protected int m_TextColor = 0;

    // The constructor method:
    public TableDescriptor(boolean sensitive)
    {
        super(m_service, m_services, sensitive);
        m_columns = new ColumnDescriptorContainer(this, sensitive);
        registerProperties();
        System.out.println("sdb.TableDescriptor()");
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short bound = PropertyAttribute.BOUND;
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.APPLYFILTER.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_ApplyFilter;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_ApplyFilter = (boolean) value;
                                               }
                                           }));

        properties.put(PropertyIds.FILTER.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Filter;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Filter = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.FONTDESCRIPTOR.getName(),
                       new PropertyWrapper(new Type(FontDescriptor.class),
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_FontDescriptor;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_FontDescriptor = (FontDescriptor) value;
                                               }
                                           }));

        properties.put(PropertyIds.GROUPBY.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_GroupBy;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_GroupBy = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.HAVINGCLAUSE.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_HavingClause;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_HavingClause = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.ORDER.getName(),
                       new PropertyWrapper(Type.STRING, bound,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Order;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Order = (String) value;
                                               }
                                           }));


        properties.put(PropertyIds.PRIVILEGES.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   System.out.println("sdb.Table.getPrivileges() 1 Privileges: " + m_Privileges);
                                                   return m_Privileges;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Privileges = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.ROWHEIGHT.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_RowHeight;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_RowHeight = (int) value;
                                               }
                                           }));

        properties.put(PropertyIds.TEXTCOLOR.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_TextColor;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_TextColor = (int) value;
                                               }
                                           }));

        super.registerProperties(properties);
    }

    protected String getName() {
        return super.getName();
    }

}
