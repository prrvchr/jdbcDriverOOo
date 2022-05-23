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
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterTable;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XIndexesSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class TableBase
    extends Item
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    private XNameAccess m_xColumns = null;
    private XIndexAccess m_xKeys = null;
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    @SuppressWarnings("unused")
    private String m_Description = "";
    private String m_Type = "";
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
                     ConnectionBase connection,
                     String catalog,
                     String schema,
                     String name,
                     String type,
                     String description)
    throws java.sql.SQLException
    {
        super(service, services, connection, _getPropertySet(), name);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Type = type;
        m_Description = description != null ? description : "";
        m_xColumns = new ColumnContainer(connection, catalog, schema, name);
        m_xKeys = new KeyContainer(connection, this);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type + "\nDecription: " + m_Description);
    }
    public TableBase(String service,
                     String[] services,
                     ConnectionBase connection,
                     String catalog,
                     String schema,
                     String name,
                     String type,
                     String description,
                     Map<String, Property> properties)
        throws java.sql.SQLException
    {
        super(service, services, connection, _getPropertySet(properties), name);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Type = type;
        m_Description = description;
        m_xColumns = new ColumnContainer(connection, catalog, schema, name);
        m_xKeys = new KeyContainer(connection, this);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }

    public TableBase(String service,
                     String[] services,
                     ConnectionBase connection,
                     schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(service, services, connection, _getPropertySet(), table.getName());
        m_CatalogName = table.getSchema().getCatalogName();
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_xColumns = new ColumnContainer(connection, table);
        m_xKeys = new KeyContainer(connection, this);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }
    public TableBase(String service,
                     String[] services,
                     ConnectionBase connection,
                     schemacrawler.schema.Table table,
                     Map<String, Property> properties)
        throws java.sql.SQLException
    {
        super(service, services, connection, _getPropertySet(properties), table.getName());
        m_CatalogName = table.getSchema().getCatalogName();;
        m_SchemaName = table.getSchema().getName();
        m_Type = table.getTableType().getTableType();
        m_Description = table.getRemarks();
        m_xColumns = new ColumnContainer(connection, table);
        m_xKeys = new KeyContainer(connection, this);
        System.out.println("sdbcx.TableBase.TableBase() : " + m_CatalogName + "." + m_SchemaName + "." + m_Name + " - Type: " + m_Type);
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        return m_xColumns;
    }


    // com.sun.star.sdbcx.XAlterTable:
    @Override
    public void alterColumnByIndex(int index, XPropertySet properties)
        throws SQLException, IndexOutOfBoundsException
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.alterColumnByIndex()");
    }

    @Override
    public void alterColumnByName(String name, XPropertySet properties)
        throws SQLException, NoSuchElementException
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.alterColumnByName()");
    }


    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.getKeys() ***************************************************");
        return m_xKeys;
    }


    // com.sun.star.sdbcx.XIndexesSupplier
    @Override
    public XNameAccess getIndexes() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.getIndexes() ***************************************************");
        return null;
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.TableBase.createDataDescriptor() ***************************************************");
        return null;
    }


}
