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

import com.sun.star.beans.XPropertySet;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.schema.ResultsColumn;


public class ColumnContainer
    extends Container<Column>

{

    private final String m_CatalogName;
    private final String m_SchemaName;
    private final String m_TableName;

    // The constructor method:
    public ColumnContainer(ConnectionBase connection,
                           String catalog,
                           String schema,
                           String table)
    {
        super(connection);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        refresh();
    }

    public ColumnContainer(ConnectionBase connection,
                           java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        this(connection, metadata, "", "", "");
    }
    public ColumnContainer(ConnectionBase connection,
                           java.sql.ResultSetMetaData metadata,
                           String catalog,
                           String schema,
                           String table)
        throws java.sql.SQLException
    {
        super(connection);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        m_Names.clear();
        m_Elements.clear();
        for (int i = 1; i <= metadata.getColumnCount(); i++)
        {
            String name = metadata.getColumnName(i);
            m_Names.add(name);
            m_Elements.add(new Column(connection, metadata, i, name));
        }
    }


    public ColumnContainer(ConnectionBase connection,
                           java.sql.ResultSet result)
        throws java.sql.SQLException
    {
        this(connection, result, "", "", "");
    }
    public ColumnContainer(ConnectionBase connection,
                           java.sql.ResultSet result,
                           String catalog,
                           String schema,
                           String table)
        throws java.sql.SQLException
    {
        super(connection);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        m_Names.clear();
        m_Elements.clear();
        if (connection.useSchemaCrawler()) {
            ResultsCrawler crawler = new ResultsCrawler(result);
            for (ResultsColumn column : crawler.crawl())
            {
                String name = column.getName();
                m_Names.add(name);
                m_Elements.add(new Column(connection, column, name));
            }
        }
        else {
            
        }
    }

    public ColumnContainer(ConnectionBase connection,
                           String catalog,
                           String schema,
                           String table,
                           String column)
        throws java.sql.SQLException
    {
        super(connection);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        m_Names.clear();
        m_Elements.clear();
        java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getColumns(catalog, schema, table, column);
        while (result != null && result.next())
        {
            String name = result.getString(4);
            m_Elements.add(new Column(m_Connection, result, catalog, schema, table, name));
            m_Names.add(name);
        }
        result.close();
    }

    public ColumnContainer(ConnectionBase connection,
                           schemacrawler.schema.Table  table)
        throws java.sql.SQLException
    {
        super(connection);
        m_CatalogName = table.getSchema().getCatalogName();
        m_SchemaName = table.getSchema().getName();
        m_TableName = table.getName();
        m_Names.clear();
        m_Elements.clear();
        for (schemacrawler.schema.Column column : table.getColumns())
        {
            String name = column.getName();
            m_Elements.add(new Column(connection, column, m_CatalogName, m_SchemaName, m_TableName, name));
            m_Names.add(name);
        }
    }

    // com.sun.star.util.XRefreshable
    @Override
    public void refresh()
    {
        try {
            m_Names.clear();
            m_Elements.clear();
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getColumns(m_CatalogName, m_SchemaName, m_TableName, "%");
            while (result.next()) {
                String name = result.getString(4);
                Column column = new Column(m_Connection, result, m_CatalogName, m_SchemaName, m_TableName, name);
                m_Elements.add(column);
                m_Names.add(name);
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.ColumnContainer.createDataDescriptor() ***************************");
        return new Column(m_Connection, m_CatalogName, m_SchemaName, m_TableName);
    }


}
