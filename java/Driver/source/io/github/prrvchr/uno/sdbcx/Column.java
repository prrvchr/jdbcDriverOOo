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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbcx.XDataDescriptorFactory;

import io.github.prrvchr.uno.helper.UnoHelper;


public class Column
extends ContainerElement
implements XDataDescriptorFactory
{
	private static final String m_name = Column.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbcx.Column"};
	public final int m_Type;
	public final String m_TypeName;
	public final long m_Precision;
	public final long m_Scale;
	public final long m_IsNullable;
	public final boolean m_IsAutoIncrement;
	public boolean m_IsCurrency = false;
	public final boolean m_IsRowVersion = false;
	public String m_Description = "";
	public String m_DefaultValue = "";


	// The constructor method:
	public Column(java.sql.ResultSet result)
	throws java.sql.SQLException
	{
		super(m_name, m_services, result.getString(4), _getPropertySet());
		m_Type = result.getInt(5);
		m_TypeName = result.getString(6);
		m_Precision = result.getInt(9);
		m_Scale = result.getInt(10);
		m_IsNullable = result.getInt(11);
		m_IsAutoIncrement = ("YES".equals(result.getString(23))) ? true : false;
		String isgenerated = result.getString(24);
		System.out.println("Column.Column() Name: " + getName() + " - TypeName: " + getTypeName() + " - Type: " + getType());
		System.out.println("Column.Column() Name: " + getName() + " - Precision: " + m_Precision + " - Scale: " + m_Scale + " - IsNullable: " + m_IsNullable + " - IsAutoIncrement: " + m_IsAutoIncrement + " - IsGenerated: " + isgenerated);
	}

	public Column(java.sql.ResultSetMetaData metadata,
				  int index)
	throws java.sql.SQLException
	{
		super(m_name, m_services, metadata.getColumnName(index), _getPropertySet());
		m_Type = metadata.getColumnType(index);
		m_TypeName = metadata.getColumnTypeName(index);
		m_Precision = metadata.getPrecision(index);
		m_Scale = metadata.getScale(index);
		m_IsNullable = metadata.isNullable(index);
		m_IsAutoIncrement = metadata.isAutoIncrement(index);
		m_IsCurrency = metadata.isCurrency(index);
	}

	public Column(schemacrawler.schema.Column column)
	{
		super(m_name, m_services, column.getName(), _getPropertySet());
		m_Type = column.getColumnDataType().getJavaSqlType().getVendorTypeNumber();
		m_TypeName = column.getColumnDataType().getJavaSqlType().getName();
		m_Precision = column.getColumnDataType().getPrecision();
		m_Scale = column.getColumnDataType().getMaximumScale();
		m_IsNullable = (column.isNullable()) ? ColumnValue.NULLABLE : ColumnValue.NO_NULLS;
		m_IsAutoIncrement = column.isAutoIncremented();
		m_Description = column.getRemarks();
		m_DefaultValue = column.getDefaultValue();
	}


	private static Map<String, Property> _getPropertySet()
	{
		short readonly = PropertyAttribute.READONLY;
		Map<String, Property> map = new HashMap<String, Property>();
		map.put("Name", UnoHelper.getProperty("Name", "string", readonly));
		map.put("Type", UnoHelper.getProperty("Type", "long", readonly));
		map.put("TypeName", UnoHelper.getProperty("TypeName", "string", readonly));
		map.put("Precision", UnoHelper.getProperty("Precision", "long", readonly));
		map.put("Scale", UnoHelper.getProperty("Scale", "long", readonly));
		map.put("IsNullable", UnoHelper.getProperty("IsNullable", "long", readonly));
		map.put("IsAutoIncrement", UnoHelper.getProperty("IsAutoIncrement", "boolean", readonly));
		map.put("IsCurrency", UnoHelper.getProperty("IsCurrency", "boolean", readonly));
		map.put("IsRowVersion", UnoHelper.getProperty("IsRowVersion", "boolean", readonly));
		map.put("Description", UnoHelper.getProperty("Description", "string", readonly));
		map.put("DefaultValue", UnoHelper.getProperty("DefaultValue", "string", readonly));
		return map;
	}


	public long getType()
	throws java.sql.SQLException
	{
		return UnoHelper.getConstantValue(DataType.class, getTypeName());
	}

	public String getTypeName()
	throws java.sql.SQLException
	{
		System.out.println("Column.getTypeName() Name: " + getName() + " - TypeName: '" + m_TypeName + "' - Type: '" + m_Type + "'");
		return UnoHelper.mapSQLDataType(m_Type, m_TypeName);
	}

	public long getPrecision()
	throws java.sql.SQLException
	{
		return m_Precision;
	}

	public long getScale()
	throws java.sql.SQLException
	{
		return m_Scale;
	}

	public long getIsNullable()
	throws java.sql.SQLException
	{
		return m_IsNullable;
	}

	public boolean getIsAutoIncrement()
	throws java.sql.SQLException
	{
		return m_IsAutoIncrement;
	}

	public boolean getIsCurrency()
	throws java.sql.SQLException
	{
		return m_IsCurrency;
	}

	public boolean getIsRowVersion()
	throws java.sql.SQLException
	{
		return m_IsRowVersion;
	}

	public String getDescription()
	throws java.sql.SQLException
	{
		return m_Description;
	}

	public String getDefaultValue()
	throws java.sql.SQLException
	{
		return m_DefaultValue;
	}

	@Override
	public XPropertySet createDataDescriptor() {
		return null;
	}


}
