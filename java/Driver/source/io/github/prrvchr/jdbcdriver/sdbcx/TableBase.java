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
package io.github.prrvchr.jdbcdriver.sdbcx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.container.NamedServiceProperty;
import io.github.prrvchr.uno.helper.UnoHelper;


public class TableBase
    extends NamedServiceProperty
    implements XColumnsSupplier
{

    private XNameAccess m_xColumns = null;
    protected final String m_CatalogName;
    protected final String m_SchemaName;
    @SuppressWarnings("unused")
    private final String m_Description;
    private final String m_Type;
    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_CatalogName", UnoHelper.getProperty("CatalogName", "string", readonly));
        map.put("m_SchemaName", UnoHelper.getProperty("SchemaName", "string", readonly));
        map.put("m_Description", UnoHelper.getProperty("Description", "string", readonly));
        map.put("m_Type", UnoHelper.getProperty("Type", "string", readonly));
        return map;
    }
    private static Map<String, Property> _getPropertySet(Map<String, Property> properties)
    {
        Map<String, Property> map = _getPropertySet();
        map.putAll(properties);
        return map;
    }

    // The constructor method:
    public TableBase(String service,
                     String[] services,
                     java.sql.DatabaseMetaData metadata,
                     java.sql.ResultSet result,
                     String name)
    throws java.sql.SQLException
    {
        super(service, services, _getPropertySet(), name);
        m_CatalogName = result.getString(1);
        m_SchemaName = result.getString(2);
        m_Type = result.getString(4);
        m_Description = result.getString(5);
        m_xColumns = _getTableColumns(metadata);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }
    public TableBase(String service,
                     String[] services,
                     java.sql.DatabaseMetaData metadata,
                     java.sql.ResultSet result,
                     String name,
                     Map<String, Property> properties)
        throws java.sql.SQLException
    {
        super(service, services, _getPropertySet(properties), name);
        m_CatalogName = result.getString(1);
        m_SchemaName = result.getString(2);
        m_Type = result.getString(4);
        m_Description = result.getString(5);
        m_xColumns = _getTableColumns(metadata);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }

    public TableBase(String service,
                     String[] services,
                     schemacrawler.schema.Table table,
                     String catalog,
                     String name)
    {
        super(service, services, _getPropertySet(), name);
        m_CatalogName = catalog;
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_xColumns = _getTableColumns(table);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }
    public TableBase(String service,
                     String[] services,
                     schemacrawler.schema.Table table,
                     String catalog,
                     String name,
                     Map<String, Property> properties)
    {
        super(service, services, _getPropertySet(properties), name);
        m_CatalogName = catalog;
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_xColumns = _getTableColumns(table);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
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
            System.out.println("sdbcx.TableBase._getTableColumns() ORDINAL: " + c.getOrdinalPosition());
        }
        return new Container<Column>(columns, names);
    }

    private XNameAccess _getTableColumns(java.sql.DatabaseMetaData metadata)
    throws java.sql.SQLException
    {
        String name = null;
        List<String> names = new ArrayList<String>();
        List<Column> columns = new ArrayList<Column>();
        java.sql.ResultSet result = metadata.getColumns(m_CatalogName, m_SchemaName, m_Name, "%");
        while (result.next())
        {
            name = result.getString(4);
            Column column = new Column(result, m_CatalogName, m_SchemaName, m_Name, name);
            columns.add(column);
            names.add(name);
            System.out.println("sdbcx.TableBase._getTableColumns() ORDINAL: " + result.getInt(17));
        }
        result.close();
        return new Container<Column>(columns, names);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        return m_xColumns;
    }


}
