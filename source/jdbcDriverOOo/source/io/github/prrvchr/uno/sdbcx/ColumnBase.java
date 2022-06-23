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
package io.github.prrvchr.uno.sdbcx;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbc.PropertyIds;


public abstract class ColumnBase
    extends Item
{

    protected final int m_position;
    private final int m_Type;
    private final String m_TypeName;
    private final int m_Precision;
    private final int m_Scale;
    private final int m_IsNullable;
    private final boolean m_IsAutoIncrement;
    private final boolean m_IsCurrency;
    private final boolean m_IsRowVersion;
    private final String m_Description;
    private final String m_DefaultValue;

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexColumn()
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyColumn()
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexColumn()
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyColumn()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      java.sql.ResultSet result,
                      String name,
                      int position)
        throws java.sql.SQLException
    {
        super(service, services, connection, name);
        m_Type = UnoHelper.mapSQLDataType(result.getInt(5));
        m_TypeName = UnoHelper.mapSQLDataTypeName(result.getString(6), m_Type);
        m_Precision = result.getInt(9);
        m_Scale = result.getInt(10);
        m_IsNullable = result.getInt(11);
        m_IsAutoIncrement = ("YES".equals(result.getString(23))) ? true : false;
        m_IsCurrency = false;
        m_IsRowVersion = false;
        m_Description = "";
        m_DefaultValue = "";
        m_position = position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexColumn()
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyColumn()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      TableBase<?> table,
                      String name,
                      int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        this(service, services, connection, (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, table.getColumns().getByName(name)), name, position);
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnBase()
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexColumn()
    // XXX: - io.github.prrvchr.uno.sdbcx.KeyColumn()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      XPropertySet descriptor,
                      String name,
                      int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(service, services, connection, name);
        m_Type = (int) descriptor.getPropertyValue("Type");
        m_TypeName = (String) descriptor.getPropertyValue("TypeName");
        m_Precision = (int) descriptor.getPropertyValue("Precision");
        m_Scale = (int) descriptor.getPropertyValue("Scale");
        m_IsNullable = (int) descriptor.getPropertyValue("IsNullable");
        m_IsAutoIncrement = (boolean) descriptor.getPropertyValue("IsAutoIncrement");
        m_IsCurrency = false;
        XPropertySetInfo info = descriptor.getPropertySetInfo();
        m_IsRowVersion = info.hasPropertyByName("IsRowVersion") ? (boolean) descriptor.getPropertyValue("IsRowVersion") : false;
        m_Description = info.hasPropertyByName("Description") ? (String) descriptor.getPropertyValue("Description") : "";
        m_DefaultValue = info.hasPropertyByName("DefaultValue") ? (String) descriptor.getPropertyValue("DefaultValue") : "";
        m_position = position;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      java.sql.ResultSet result,
                      String name)
        throws java.sql.SQLException
    {
        super(service, services, connection, name);
        m_Type = UnoHelper.mapSQLDataType(result.getInt(5));
        m_TypeName = UnoHelper.mapSQLDataTypeName(result.getString(6), m_Type);
        m_Precision = result.getInt(7);
        int scale = result.getInt(9);
        m_Scale = result.wasNull() ? 0 : scale;
        m_IsNullable = result.getInt(11);
        String description = result.getString(12);
        m_Description = result.wasNull() ? "" : description;
        String value = result.getString(13);
        m_DefaultValue = result.wasNull() ? "" : value;;
        m_position = result.getInt(17);
        // TODO: SmallSQL does not have this column
        //String auto = result.getMetaData().getColumnCount() < 23 ? "NO" : result.getString(23);
        String auto = result.getString(23);
        m_IsAutoIncrement = (!result.wasNull() && auto.equals("YES")) ? true : false;
        m_IsCurrency = false;
        m_IsRowVersion = false;
        registerProperties();
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      java.sql.ResultSetMetaData metadata,
                      String name,
                      int position)
    throws java.sql.SQLException
    {
        super(service, services, connection, name);
        m_Type = UnoHelper.mapSQLDataType(metadata.getColumnType(position));
        m_TypeName = UnoHelper.mapSQLDataTypeName(metadata.getColumnTypeName(position), m_Type);
        m_Precision = metadata.getPrecision(position);
        m_Scale = metadata.getScale(position);
        m_IsNullable = metadata.isNullable(position);
        m_IsAutoIncrement = metadata.isAutoIncrement(position);
        m_IsCurrency = metadata.isCurrency(position);
        m_IsRowVersion = false;
        m_Description = "";
        m_DefaultValue = "";
        m_position = position;
        registerProperties();
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
     public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      schemacrawler.schema.Column column,
                      String name)
    {
        super(service, services, connection, name);
        m_Type = UnoHelper.mapSQLDataType(column.getColumnDataType().getJavaSqlType().getVendorTypeNumber());
        m_TypeName = UnoHelper.mapSQLDataTypeName(column.getColumnDataType().getName(), m_Type);
        m_Precision = (int) column.getColumnDataType().getPrecision();
        m_Scale = column.getColumnDataType().getMaximumScale();
        m_IsNullable = (column.isNullable()) ? ColumnValue.NULLABLE : ColumnValue.NO_NULLS;
        m_IsAutoIncrement = column.isAutoIncremented();
        m_IsCurrency = false;
        m_IsRowVersion = false;
        m_Description = column.getRemarks();
        m_DefaultValue = column.getDefaultValue();
        m_position = column.getOrdinalPosition();;
        registerProperties();
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnSuper()
    public ColumnBase(String service,
                      String[] services,
                      Connection connection,
                      schemacrawler.schema.ResultsColumn column,
                      String name)
    {
        super(service, services, connection, name);
        m_Type = UnoHelper.mapSQLDataType(column.getColumnDataType().getJavaSqlType().getVendorTypeNumber());
        m_TypeName = UnoHelper.mapSQLDataTypeName(column.getColumnDataType().getName(), m_Type);
        m_Precision = (int) column.getColumnDataType().getPrecision();
        m_Scale = column.getColumnDataType().getMaximumScale();
        m_IsNullable = (column.isNullable()) ? ColumnValue.NULLABLE : ColumnValue.NO_NULLS;
        m_IsAutoIncrement = column.isAutoIncrement();
        m_IsCurrency = false;
        m_IsRowVersion = false;
        m_Description = column.getRemarks();
        m_DefaultValue = ((schemacrawler.schema.Column) column).getDefaultValue();
        m_position = column.getOrdinalPosition();
        registerProperties();
    }


    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.TYPE.name, PropertyIds.TYPE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
        registerProperty(PropertyIds.TYPENAME.name, PropertyIds.TYPENAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_TypeName;
                }
            }, null);
        registerProperty(PropertyIds.PRECISION.name, PropertyIds.PRECISION.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Precision;
                }
            }, null);
        registerProperty(PropertyIds.SCALE.name, PropertyIds.SCALE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Scale;
                }
            }, null);
        registerProperty(PropertyIds.ISNULLABLE.name, PropertyIds.ISNULLABLE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsNullable;
                }
            }, null);
        registerProperty(PropertyIds.ISAUTOINCREMENT.name, PropertyIds.ISAUTOINCREMENT.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsAutoIncrement;
                }
            }, null);
        registerProperty(PropertyIds.ISCURRENCY.name, PropertyIds.ISCURRENCY.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsCurrency;
                }
            }, null);
        registerProperty(PropertyIds.ISROWVERSION.name, PropertyIds.ISROWVERSION.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsRowVersion;
                }
            }, null);
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Description;
                }
            }, null);
        registerProperty(PropertyIds.DEFAULTVALUE.name, PropertyIds.DEFAULTVALUE.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_DefaultValue;
                }
            }, null);
    }


    public String _getComment()
    {
        return null;
    }


}
