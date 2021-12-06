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
package io.github.prrvchr.ooo.sdbcx;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;

import io.github.prrvchr.ooo.helper.UnoHelper;
import io.github.prrvchr.ooo.lang.ServiceProperty;


public class Columns
extends ServiceProperty
{
	private static final String m_name = Columns.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbcx.Columns"};
	private final int m_index;
	private final java.sql.ResultSetMetaData m_Metadata;

	// The constructor method:
	public Columns(int index,
                   java.sql.ResultSetMetaData metadata)
	{
		super(_getPropertySet());
		m_index = index;
		m_Metadata = metadata;
	}


	private static Map<String, Property> _getPropertySet()
	{
		short readonly = PropertyAttribute.READONLY;
		Map<String, Property> map = new HashMap<String, Property>();
		Property p1 = UnoHelper.getProperty("Name", "string", readonly);
		map.put(UnoHelper.getPropertyName(p1), p1);
		Property p2 = UnoHelper.getProperty("Type", "long", readonly);
		map.put(UnoHelper.getPropertyName(p2), p2);
		Property p3 = UnoHelper.getProperty("TypeName", "string", readonly);
		map.put(UnoHelper.getPropertyName(p3), p3);
		Property p4 = UnoHelper.getProperty("Precision", "long", readonly);
		map.put(UnoHelper.getPropertyName(p4), p4);
		Property p5 = UnoHelper.getProperty("Scale", "long", readonly);
		map.put(UnoHelper.getPropertyName(p5), p5);
		Property p6 = UnoHelper.getProperty("IsNullable", "long", readonly);
		map.put(UnoHelper.getPropertyName(p6), p6);
		Property p7 = UnoHelper.getProperty("IsAutoIncrement", "boolean", readonly);
		map.put(UnoHelper.getPropertyName(p7), p7);
		Property p8 = UnoHelper.getProperty("IsCurrency", "boolean", readonly);
		map.put(UnoHelper.getPropertyName(p8), p8);
		Property p9 = UnoHelper.getProperty("IsRowVersion", "boolean", readonly);
		map.put(UnoHelper.getPropertyName(p9), p9);
		Property p10 = UnoHelper.getProperty("Description", "string", readonly);
		map.put(UnoHelper.getPropertyName(p10), p10);
		Property p11 = UnoHelper.getProperty("DefaultValue", "string", readonly);
		map.put(UnoHelper.getPropertyName(p11), p11);
		return map;
	}

	public String getName()
	throws java.sql.SQLException
	{
		return m_Metadata.getColumnName(m_index);
	}

	public long getType()
	throws java.sql.SQLException
	{
		return m_Metadata.getColumnType(m_index);
	}

	public String getTypeName()
	throws java.sql.SQLException
	{
		return m_Metadata.getColumnTypeName(m_index);
	}
	

	public long getPrecision()
	throws java.sql.SQLException
	{
		return m_Metadata.getPrecision(m_index);
	}

	public long getScale()
	throws java.sql.SQLException
	{
		return m_Metadata.getScale(m_index);
	}
	

	public long getIsNullable()
	throws java.sql.SQLException
	{
		return m_Metadata.isNullable(m_index);
	}
	
	public boolean getIsAutoIncrement()
	throws java.sql.SQLException
	{
		return m_Metadata.isAutoIncrement(m_index);
	}

	public boolean getIsCurrency()
	throws java.sql.SQLException
	{
		return m_Metadata.isCurrency(m_index);
	}

	public boolean getIsRowVersion()
	throws java.sql.SQLException
	{
		return false;
	}

	public String getDescription()
	throws java.sql.SQLException
	{
		return "";
	}

	public String getDefaultValue()
	throws java.sql.SQLException
	{
		return "";
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String _getImplementationName()
	{
		return m_name;
	}
	@Override
	public String[] _getServiceNames() {
		return m_services;
	}


}
