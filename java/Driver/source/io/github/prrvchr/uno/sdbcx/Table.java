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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.helper.UnoHelper;


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
	@SuppressWarnings("unused")
	private final String m_Description;
	private final String m_Type;
	private static Map<String, Property> _getPropertySet()
	{
		short readonly = PropertyAttribute.READONLY;
		Map<String, Property> map = new HashMap<String, Property>();
		map.put("m_CatalogName", UnoHelper.getProperty("CatalogName", "string", readonly));
		map.put("m_SchemaName", UnoHelper.getProperty("SchemaName", "string", readonly));
		map.put("m_Description", UnoHelper.getProperty("Description", "string", readonly));
		map.put("m_Type", UnoHelper.getProperty("Type", "string", readonly));
		return map;
	}

	// The constructor method:
	public Table(java.sql.DatabaseMetaData metadata,
				 java.sql.ResultSet result,
				 String name)
	throws java.sql.SQLException
	{
		super(m_name, m_services, _getPropertySet(), name);
		m_Metadata = metadata;
		m_CatalogName = result.getString(1);
		m_SchemaName = result.getString(2);
		m_Type = result.getString(4);
		m_Description = result.getString(5);
		System.out.println("Table.Table() Schema: " + m_SchemaName + " - Name: " + m_Name + " - Type: " + m_Type);
	}
	public Table(schemacrawler.schema.Table table,
				 String name)
	{
		super(m_name, m_services, _getPropertySet(), name);
		m_CatalogName = "";
		m_SchemaName = table.getSchema().getName();
		m_Type = table.getTableType().getTableType();
		m_Description = table.getRemarks();
		m_xColumns = _getTableColumns(table);
	}


	private XNameAccess _getTableColumns(schemacrawler.schema.Table table)
	{
		String name = null;
		List<String> names = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		for (schemacrawler.schema.Column c : table.getColumns())
		{
			name = c.getName();
			Column column = new Column(c, m_CatalogName, m_SchemaName, m_Name, name);
			columns.add(column);
			names.add(name);
		}
		return new Container<Column>(columns, names);
	}

	private XNameAccess _getTableColumns()
	throws java.sql.SQLException
	{
		String name = null;
		List<String> names = new ArrayList<String>();
		List<Column> columns = new ArrayList<Column>();
		java.sql.ResultSet result = m_Metadata.getColumns(m_CatalogName, m_SchemaName, m_Name, "%");
		while (result.next())
		{
			name = result.getString(4);
			Column column = new Column(result, m_CatalogName, m_SchemaName, m_Name, name);
			columns.add(column);
			names.add(name);
		}
		result.close();
		return new Container<Column>(columns, names);
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
