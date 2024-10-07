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

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.sdbcx.ColumnDescriptor;
import io.github.prrvchr.uno.sdbcx.ColumnSuper;


public final class Column
    extends ColumnSuper
{
    private static final String m_service = Column.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Column",
                                                "com.sun.star.sdbcx.Column",
                                                "com.sun.star.sdb.ColumnSettings"};
    private Integer m_Align = 0;
    private String m_ControlDefault = "";
    private XPropertySet m_ControlModel = null;
    private Integer m_FormatKey = 256;
    private String m_HelpText = "";
    private boolean m_Hidden = false;
    private Integer m_Position;
    private Integer m_RelativePosition;
    private Integer m_Width = 100;

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
                  final boolean currency)
    {
        super(m_service, m_services, table, sensitive, name, typeName, defaultValue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        registerProperties();
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short maybevoid = PropertyAttribute.MAYBEVOID;

        properties.put(PropertyIds.ALIGN.getName(),
                       new PropertyWrapper(Type.LONG, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Align;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Align = (Integer) value;
                                               }
                                           }));

        properties.put(PropertyIds.CONTROLDEFAULT.getName(),
                       new PropertyWrapper(Type.STRING, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_ControlDefault;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_ControlDefault = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.CONTROLMODEL.getName(),
                       new PropertyWrapper(Type.ANY, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_ControlModel;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_ControlModel = (XPropertySet) value;
                                               }
                                           }));

        properties.put(PropertyIds.FORMATKEY.getName(),
                       new PropertyWrapper(Type.LONG, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_FormatKey;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_FormatKey = (Integer) value;
                                               }
                                           }));

        properties.put(PropertyIds.HELPTEXT.getName(),
                       new PropertyWrapper(Type.STRING, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_HelpText;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_HelpText = (String) value;
                                               }
                                           }));

        properties.put(PropertyIds.HIDDEN.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Hidden;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Hidden = (boolean) value;
                                               }
                                           }));

        properties.put(PropertyIds.POSITION.getName(),
                       new PropertyWrapper(Type.LONG, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Position;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Position = (Integer) value;
                                               }
                                           }));

        properties.put(PropertyIds.RELATIVEPOSITION.getName(),
                       new PropertyWrapper(Type.LONG, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_RelativePosition;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_RelativePosition = (Integer) value;
                                               }
                                           }));

        properties.put(PropertyIds.WIDTH.getName(),
                       new PropertyWrapper(Type.LONG, maybevoid,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Width;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_Width = (Integer) value;
                                               }
                                           }));

        super.registerProperties(properties);
    }


    // XDataDescriptorFactory
    
    @Override
    public XPropertySet createDataDescriptor()
    {
        Table table = (Table) m_table;
        ColumnDescriptor descriptor = new ColumnDescriptor(table.getCatalogName(), table.getSchemaName(), table.getName(), isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }


}
