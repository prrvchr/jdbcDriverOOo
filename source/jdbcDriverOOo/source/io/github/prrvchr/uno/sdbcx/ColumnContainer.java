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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.schema.ResultsColumn;


public class ColumnContainer
    extends ContainerSuper<Column>
{

    private final TableBase m_Table;

    // The constructor method:
    public ColumnContainer(ConnectionBase connection)
    {
        super(connection);
        m_Table = null;
    }


    public ColumnContainer(ConnectionBase connection,
                           TableBase table)
    {
        super(connection);
        m_Table = table;
        refresh();
    }

    public ColumnContainer(ConnectionBase connection,
                           String catalog,
                           String schema,
                           String table)
    {
        super(connection);
        m_Table = null;
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
        m_Table = null;
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
        m_Table = null;
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
        m_Table = null;
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
                           schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(connection);
        m_Table = null;
        String catalog = table.getSchema().getCatalogName();
        String schema = table.getSchema().getName();
        String tname = table.getName();
        for (schemacrawler.schema.Column column : table.getColumns())
        {
            String name = column.getName();
            m_Elements.add(new Column(connection, column, catalog, schema, tname, name));
            m_Names.add(name);
        }
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(Column column)
    {
        return m_Connection.getProvider().getDropColumnQuery(m_Connection, column);
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh()
    {
        if(m_Table != null) {
            try {
                m_Names.clear();
                m_Elements.clear();
                java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getColumns(m_Table.m_CatalogName, m_Table.m_SchemaName, m_Table.m_Name, "%");
                while (result.next()) {
                    String name = result.getString(4);
                    Column column = new Column(m_Connection, result, m_Table.m_CatalogName, m_Table.m_SchemaName, m_Table.m_Name, name);
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



    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.ColumnContainer.createDataDescriptor() ***************************");
        return new ColumnDescriptor(m_Connection);
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 1");
        try {
            String name = (String) descriptor.getPropertyValue("Name");
            Column column = new Column(m_Connection, descriptor, name);
            m_Elements.add(column);
            m_Names.add(name);
            elementInserted(column);
            System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 2 : " + name);
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
