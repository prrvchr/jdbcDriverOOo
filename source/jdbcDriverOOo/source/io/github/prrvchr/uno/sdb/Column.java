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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.sdbcx.ColumnSuper;


public class Column
    extends ColumnSuper
{
    private static final String m_service = Column.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Column",
                                                "com.sun.star.sdbcx.Column",
                                                "com.sun.star.sdb.ColumnSettings"};
    private int m_FormatKey = 256;
    private int m_Align = 0;
    private int m_Width = 100;
    private int m_Position;
    private boolean m_Hidden = false;

    // The constructor method:
    public Column(final boolean sensitive,
                  final String catalog,
                  final String schema,
                  final String table)
    {
        super(m_service, m_services, sensitive, catalog, schema, table);
        registerProperties();
    }
    public Column(final boolean sensitive,
                  final String catalog,
                  final String schema,
                  final String table,
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
        super(m_service, m_services, sensitive, catalog, schema, table, name, typeName, defaultValue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
        registerProperties();
    }


    private void registerProperties() {
        registerProperty(PropertyIds.FORMATKEY.name, PropertyIds.FORMATKEY.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_FormatKey;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_FormatKey = (int) value;
                }
            });
        registerProperty(PropertyIds.ALIGN.name, PropertyIds.ALIGN.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Align;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Align = (int) value;
                }
            });
        registerProperty(PropertyIds.WIDTH.name, PropertyIds.WIDTH.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Width;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Width = (int) value;
                }
            });
        registerProperty(PropertyIds.POSITION.name, PropertyIds.POSITION.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Position;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Position = (int) value;
                }
            });
        registerProperty(PropertyIds.HIDDEN.name, PropertyIds.HIDDEN.id, Type.BOOLEAN,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Hidden;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Hidden = (boolean) value;
                }
            });
    }


/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer.appendByDescriptor()
    public Column(Connection connection,
                  XPropertySet descriptor,
                  int position)
        throws SQLException
    {
        super(m_name, m_services, connection, descriptor, position);
        registerProperties();
    }
    public Column(Connection connection,
                  XPropertySet descriptor,
                  String name,
                  int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(m_name, m_services, connection, descriptor, name, position);
        m_Position = m_position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  java.sql.ResultSet result,
                  String name)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, result, name);
        m_Position = m_position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  schemacrawler.schema.Column column,
                  String name)
    {
        super(m_name, m_services, connection, column, name);
        m_Position = m_position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  schemacrawler.schema.ResultsColumn column,
                  String name)
    {
        super(m_name, m_services, connection, column, name);
        m_Position = m_position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  ResultSetMetaData metadata,
                  String name,
                  int position)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, metadata, name, position);
        m_Position = m_position;
        registerProperties();
    }*/



}
