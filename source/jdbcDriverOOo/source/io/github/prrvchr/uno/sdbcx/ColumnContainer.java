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

import java.lang.reflect.InvocationTargetException;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;

import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.schema.ResultsColumn;


public class ColumnContainer<T extends ColumnSuper>
    extends ContainerSuper<ColumnSuper>
{

    private static final String m_name = ColumnContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    private final Class<T> m_column;

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           Class<T> column,
                           TableBase<T> table)
    {
        super(m_name, m_services, connection);
        m_column = column;
        _refresh(table);
        System.out.println("sdbcx.ColumnContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           Class<T> column,
                           XNameAccess columns)
    {
        super(m_name, m_services, connection);
        m_column = column;
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        int position = 1;
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                m_Elements.add(m_column.getDeclaredConstructor(Connection.class, XPropertySet.class, String.class, Integer.class).newInstance(m_Connection, descriptor, name, position++));
                m_Names.add(name);
            }
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.ColumnContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           Class<T> column,
                           schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        m_column = column;
        try {
            for (schemacrawler.schema.Column c : table.getColumns()) {
                String name = c.getName();
                m_Elements.add(m_column.getDeclaredConstructor(Connection.class, schemacrawler.schema.Column.class, String.class).newInstance(connection, c, name));
                m_Names.add(name);
            }
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public ColumnContainer(Connection connection,
                           Class<T> column)
    {
        super(m_name, m_services, connection);
        m_column = column;
        System.out.println("sdbcx.ColumnContainer()");
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.CallableStatement.getColumns()
    // XXX: - io.github.prrvchr.uno.sdb.PreparedStatement.getColumns()
    public ColumnContainer(Connection connection,
                           Class<T> column,
                           java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        m_column = column;
        _refresh(metadata);
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.ResultSet.getColumns()
    public ColumnContainer(Connection connection,
                           Class<T> column,
                           java.sql.ResultSet result)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        m_column = column;
        if (connection.useSchemaCrawler()) {
            try {
                ResultsCrawler crawler = new ResultsCrawler(result);
                for (ResultsColumn c : crawler.crawl())
                {
                    String name = c.getName();
                    m_Names.add(name);
                    m_Elements.add(m_column.getDeclaredConstructor(Connection.class, ResultsColumn.class, String.class).newInstance(connection, c, name));
                }
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                   InvocationTargetException | NoSuchMethodException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            _refresh(result.getMetaData());
        }
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(ColumnBase column)
    {
        return m_Connection.getProvider().getDropColumnQuery(m_Connection, column);
    }


    public void _refresh(TableBase<T> table)
    {
        m_Names.clear();
        m_Elements.clear();
        try {
            _refresh(table.m_CatalogName, table.m_SchemaName, table.m_Name);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void _refresh(String catalog,
                          String schema,
                          String table)
        throws java.sql.SQLException
    {
        try {
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getColumns(catalog, schema, table, "%");
            while (result.next()) {
                String name = result.getString(4);
                m_Elements.add(m_column.getDeclaredConstructor(Connection.class, java.sql.ResultSet.class, String.class).newInstance(m_Connection, result, name));
                m_Names.add(name);
            }
            result.close();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void _refresh(java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        try {
            for (int position = 1; position <= metadata.getColumnCount(); position++) {
                String name = metadata.getColumnName(position);
                m_Elements.add(m_column.getDeclaredConstructor(Connection.class, java.sql.ResultSetMetaData.class, String.class, Integer.class).newInstance(m_Connection, metadata, name, position));
                m_Names.add(name);
            }
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            int position = m_Elements.size() + 1;
            String name = (String) descriptor.getPropertyValue("Name");
            if (m_Names.contains(name)) {
                throw new ElementExistException();
            }
            T column = m_column.getDeclaredConstructor(Connection.class, XPropertySet.class, String.class, Integer.class).newInstance(m_Connection, descriptor, name, position);
            m_Elements.add(column);
            m_Names.add(name);
            _insertElement(column);
            System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 2 : " + name);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected String _getDropQuery(ColumnSuper element)
        throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }


}
