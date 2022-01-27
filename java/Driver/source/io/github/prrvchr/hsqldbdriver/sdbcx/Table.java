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
package io.github.prrvchr.hsqldbdriver.sdbcx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.hsqldbdriver.helper.UnoHelper;


public class Table
extends ContainerElement
implements XColumnsSupplier
{
	private static final String m_name = Table.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbcx.Table"};
	private java.sql.DatabaseMetaData m_Metadata = null;
	private XNameAccess m_xColumns = null;
	private final String m_CatalogName;
	private final String m_SchemaName;
	private final String m_Description;
	private final String m_Type;

	// The constructor method:
	public Table(java.sql.DatabaseMetaData metadata,
				 java.sql.ResultSet result)
	throws java.sql.SQLException
	{
		super(m_name, m_services, result.getString(3), _getPropertySet());
		m_Metadata = metadata;
		m_CatalogName = result.getString(1);
		m_SchemaName = result.getString(2);
		m_Type = result.getString(4);
		m_Description = result.getString(5);
		System.out.println("Table.Table() Schema: " + m_SchemaName + " - Name: " + getName() + " - Type: " + m_Type);
	}
	public Table(schemacrawler.schema.Table table)
	{
		super(m_name, m_services, table.getName(), _getPropertySet());
		m_CatalogName = "";
		m_SchemaName = table.getSchema().getName();
		m_Type = table.getTableType().getTableType();
		m_Description = table.getRemarks();
		m_xColumns = _getTableColumns(table);
	}


	private XNameAccess _getTableColumns(schemacrawler.schema.Table table)
	{
		List<String> names = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		for (schemacrawler.schema.Column c : table.getColumns())
		{
			Column column = new Column(c);
			columns.add(column);
			names.add(column.getName());
		}
		return new Container<Column>(columns, names);
	}

	private XNameAccess _getTableColumns()
	throws java.sql.SQLException
	{
		List<String> names = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		java.sql.ResultSet result = m_Metadata.getColumns(m_CatalogName, m_SchemaName, getName(), "%");
		while (result.next())
		{
			Column column = new Column(result);
			columns.add(column);
			names.add(column.getName());
		}
		result.close();
		return new Container<Column>(columns, names);
	}

	
	private static Map<String, Property> _getPropertySet()
	{
		short readonly = PropertyAttribute.READONLY;
		Map<String, Property> map = new HashMap<String, Property>();
		Property p1 = UnoHelper.getProperty("Name", "string", readonly);
		map.put(UnoHelper.getPropertyName(p1), p1);
		Property p2 = UnoHelper.getProperty("CatalogName", "string", readonly);
		map.put(UnoHelper.getPropertyName(p2), p2);
		Property p3 = UnoHelper.getProperty("SchemaName", "string", readonly);
		map.put(UnoHelper.getPropertyName(p3), p3);
		Property p4 = UnoHelper.getProperty("Description", "string", readonly);
		map.put(UnoHelper.getPropertyName(p4), p4);
		Property p5 = UnoHelper.getProperty("Type", "string", readonly);
		map.put(UnoHelper.getPropertyName(p5), p5);
		return map;
	}

	
	public String getCatalogName()
	{
		return m_CatalogName;
	}

	public String getSchemaName()
	{
		return m_SchemaName;
	}

	public String getDescription()
	{
		return m_Description;
	}

	public String getType()
	{
		return m_Type;
	}


	// com.sun.star.sdbcx.XColumnsSupplier:
	@Override
	public XNameAccess getColumns()
	{
		if (m_xColumns == null)
		{
			try {
				m_xColumns = _getTableColumns();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_xColumns;
	}


}
