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
import java.util.Arrays;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;
import schemacrawler.schema.Catalog;


public class TableContainer<T extends TableBase<C>, D extends TableDescriptorBase<C>, C extends ColumnSuper>
    extends ContainerSuper<TableBase<C>>
{

    private static final String m_name = TableContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    private final Class<T> m_table;
    private final Class<D> m_descriptor;

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.Connection.getTables()
    public TableContainer(Connection connection,
                          Class<T> table,
                          Class<D> descriptor)
    {
        super(m_name, m_services, connection);
        m_table = table;
        m_descriptor = descriptor;
        try {
            String value;
            String[] types = m_Connection.getProvider().getTableTypes();
            System.out.println("TableContainer.TableContainer() 1 Type: " + Arrays.toString(types));
            //String[] types = null;
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getTables(null, null, "%", types);
            System.out.println("TableContainer.TableContainer() 2");
            while (result.next()) {
                System.out.println("TableContainer.TableContainer() 3");
                value = result.getString(1);
                String catalog = result.wasNull() ? "" : value;
                value = result.getString(2);
                String schema = result.wasNull() ? "" : value;
                String name = result.getString(3);
                value = result.getString(4);
                String type = result.wasNull() ? "" : m_Connection.getProvider().getTableType(value);
                value = result.getString(5);
                String description = result.wasNull() ? "" : value;
                m_Elements.add(m_table.getDeclaredConstructor(Connection.class, String.class, String.class, String.class, String.class, String.class).newInstance(m_Connection, catalog, schema, name, type, description));
                // FIXME: We must construct a unique name!!!
                m_Names.add(_getTableName(catalog, schema, name));
                System.out.println("TableContainer.TableContainer() 4");
            }
            result.close();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        System.out.println("sdbcx.TableContainer() : " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.jdbcdriver.SchemaCrawler.getTables()
    public TableContainer(Connection connection,
                          Class<T> table,
                          Class<D> descriptor,
                          Catalog catalog)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        m_table = table;
        m_descriptor = descriptor;
        try {
            for (final schemacrawler.schema.Table t : catalog.getTables()) {
                String schema = t.getSchema().getName();
                String name = t.getName();
                m_Elements.add(m_table.getDeclaredConstructor(Connection.class, schemacrawler.schema.Table.class).newInstance(m_Connection, t));
                // FIXME: We must construct a unique name!!!
                m_Names.add(_getTableName(t.getSchema().getCatalogName(), schema, name));
            }
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(TableBase<C> table)
        throws SQLException
    {
        return m_Connection.getProvider().getDropTableQuery(m_Connection, table.m_CatalogName, table.m_SchemaName, table.m_Name);
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdbcx.TableContainer.createDataDescriptor() 1 ***************************");
        XPropertySet descriptor = null;
        try {
            descriptor = m_descriptor.getDeclaredConstructor(Connection.class).newInstance(m_Connection);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.TableContainer.createDataDescriptor() 2");
        return descriptor;
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.TableContainer.appendByDescriptor() 1");
        try {
            String catalog = (String) descriptor.getPropertyValue("CatalogName");
            String schema = (String) descriptor.getPropertyValue("SchemaName");
            String table = (String) descriptor.getPropertyValue("Name");
            String name = _getTableName(catalog, schema, table);
            if (m_Names.contains(name)) {
                 throw new ElementExistException();
            }
            System.out.println("sdbcx.TableContainer.appendByDescriptor() 2: " + name);
            T newtable = m_table.getDeclaredConstructor(Connection.class, XPropertySet.class, String.class).newInstance(m_Connection, descriptor, table);
            String[] queries = m_Connection.getProvider().getCreateTableQueries(m_Connection, descriptor, catalog, schema, table);
            System.out.println("sdbcx.TableContainer.appendByDescriptor() 3");
            _executeQueries(queries);
            m_Elements.add(newtable);
            m_Names.add(name);
            _insertElement(newtable);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    private String _getTableName(String catalog,
                                 String schema,
                                 String table)
    {
        return String.format("%s.%s", schema, table);
    }


}
