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

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class Column
    extends Item
{
    private static final String m_name = Column.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Column"};
    private final int m_Type;
    @SuppressWarnings("unused")
    private final String m_TypeName;
    @SuppressWarnings("unused")
    private final int m_Precision;
    @SuppressWarnings("unused")
    private final int m_Scale;
    @SuppressWarnings("unused")
    private final int m_IsNullable;
    @SuppressWarnings("unused")
    private final boolean m_IsAutoIncrement;
    @SuppressWarnings("unused")
    private final boolean m_IsCurrency;
    @SuppressWarnings("unused")
    private final boolean m_IsRowVersion;
    @SuppressWarnings("unused")
    private final String m_Description;
    @SuppressWarnings("unused")
    private final String m_DefaultValue;
    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Type", UnoHelper.getProperty("Type", "long", readonly));
        map.put("m_TypeName", UnoHelper.getProperty("TypeName", "string", readonly));
        map.put("m_Precision", UnoHelper.getProperty("Precision", "long", readonly));
        map.put("m_Scale", UnoHelper.getProperty("Scale", "long", readonly));
        map.put("m_IsNullable", UnoHelper.getProperty("IsNullable", "long", readonly));
        map.put("m_IsAutoIncrement", UnoHelper.getProperty("IsAutoIncrement", "boolean", readonly));
        map.put("m_IsCurrency", UnoHelper.getProperty("IsCurrency", "boolean", readonly));
        map.put("m_IsRowVersion", UnoHelper.getProperty("IsRowVersion", "boolean", readonly));
        map.put("m_Description", UnoHelper.getProperty("Description", "string", readonly));
        map.put("m_DefaultValue", UnoHelper.getProperty("DefaultValue", "string", readonly));
        return map;
    }

    // The constructor method:
    public Column(ConnectionBase connection,
                  XPropertySet descriptor,
                  String name)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(m_name, m_services, connection, _getPropertySet(), name);
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
    }
    public Column(ConnectionBase connection,
                  java.sql.ResultSet result,
                  String catalog,
                  String schema,
                  String table,
                  String name)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, _getPropertySet(), name);
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
    }
    public Column(ConnectionBase connection,
                  java.sql.ResultSetMetaData metadata,
                  int index,
                  String name)
    throws java.sql.SQLException
    {
        super(m_name, m_services, connection, _getPropertySet(), name);
        m_Type = UnoHelper.mapSQLDataType(metadata.getColumnType(index));
        m_TypeName = UnoHelper.mapSQLDataTypeName(metadata.getColumnTypeName(index), m_Type);
        m_Precision = metadata.getPrecision(index);
        m_Scale = metadata.getScale(index);
        m_IsNullable = metadata.isNullable(index);
        m_IsAutoIncrement = metadata.isAutoIncrement(index);
        m_IsCurrency = metadata.isCurrency(index);
        m_IsRowVersion = false;
        m_Description = "";
        m_DefaultValue = "";
    }

    public Column(ConnectionBase connection,
                  schemacrawler.schema.Column column,
                  String catalog,
                  String schema,
                  String table,
                  String name)
    {
        super(m_name, m_services, connection, _getPropertySet(), name);
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
    }
    public Column(ConnectionBase connection,
                  schemacrawler.schema.ResultsColumn column,
                  String name)
    {
        super(m_name, m_services, connection, _getPropertySet(), name);
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
    }


}
