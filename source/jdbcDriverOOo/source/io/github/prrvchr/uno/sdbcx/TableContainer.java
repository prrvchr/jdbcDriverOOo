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

import java.util.List;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.ComposeRule;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.DataBaseTools.NameComponents;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class TableContainer
    extends Container
{

    private final ConnectionSuper m_connection;

    // The constructor method:
    public TableContainer(ConnectionSuper connection,
                          boolean sensitive,
                          List<String> names)
        throws ElementExistException
    {
        super(connection, sensitive, names);
        m_connection = connection;
    }

    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(TableBase table)
        throws SQLException
    {
        return m_connection.getProvider().getDropTableQuery(m_connection, table.getCatalogName(), table.getSchemaName(), table.getName());
    }


    @Override
    protected String _getElementName(XPropertySet object)
        throws SQLException
    {
        NameComponents components = DataBaseTools.getTableNameComponents(m_connection, object);
        return DataBaseTools.composeTableName(m_connection, components.getCatalog(), components.getSchema(), components.getTable(), false, ComposeRule.InDataManipulation);
    }

    @Override
    public XPropertySet _appendElement(XPropertySet descriptor,
                                       String name)
        throws SQLException
    {
        _createTable(descriptor);
        return _createElement(name);
    }

    private void _createTable(XPropertySet descriptor)
        throws SQLException
    {
        try (java.sql.Statement statement = getConnection().getProvider().getConnection().createStatement()){
            String sql = DataBaseTools.getCreateTableQuery(m_connection, descriptor, null, "(M,D)");
            System.out.println("sdbcx.TableContainer._createTable() SQL: " + sql);
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
    }

    @Override
    public XPropertySet _createElement(String name)
        throws SQLException
    {
        TableBase table = null;
        NameComponents component = DataBaseTools.qualifiedNameComponents(m_connection, name, ComposeRule.InDataManipulation);
        try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
            if (result.next()) {
                String type = result.getString(4);
                type = result.wasNull() ? "" : m_connection.getProvider().getTableType(type);
                String remarks = result.getString(5);
                remarks = result.wasNull() ? "" : remarks;
                table = m_connection.getTable(isCaseSensitive(), component.getCatalog(), component.getSchema(),
                                              component.getTable(), type, remarks);
            }
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
        return table;
    }

    private java.sql.ResultSet _getcreateElementResultSet(NameComponents component)
        throws java.sql.SQLException
    {
        String catalog = component.getCatalog().isEmpty() ? null : component.getCatalog();
        String schema = component.getSchema().isEmpty() ? null : component.getSchema();
        java.sql.DatabaseMetaData metadata = m_connection.getProvider().getConnection().getMetaData();
        return metadata.getTables(catalog, schema, component.getTable(), null);

    }
    
    
    @Override
    public void _removeElement(int index,
                               String name)
        throws SQLException
    {
        try {
            Object object = _getElement(index);
            NameComponents nameComponents = DataBaseTools.qualifiedNameComponents(m_connection, name, ComposeRule.InDataManipulation);
            boolean isView = false;
            XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, object);
            if (propertySet != null) {
                isView = AnyConverter.toString(propertySet.getPropertyValue(PropertyIds.TYPE.name)).equals("VIEW");
            }
            
            String composedName = DataBaseTools.composeTableName(m_connection, nameComponents.getCatalog(), nameComponents.getSchema(),
                                                                 nameComponents.getTable(), true, ComposeRule.InDataManipulation);
            if (isView) {
                XDrop dropView = UnoRuntime.queryInterface(XDrop.class, m_connection.getViews());
                String unquotedName = DataBaseTools.composeTableName(m_connection, nameComponents.getCatalog(), nameComponents.getSchema(),
                                                                     nameComponents.getTable(), false, ComposeRule.InDataManipulation);
                dropView.dropByName(unquotedName);
                return;
            }
            String sql = "DROP TABLE " + composedName;
            System.out.println("TableContainer._removeElement() Query: " + sql);
            java.sql.Statement statement = getConnection().getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (WrappedTargetException | UnknownPropertyException | NoSuchElementException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
    }

    public synchronized void _refresh()
    {
        m_connection._refresh();
    }

    public ConnectionSuper getConnection()
    {
        return m_connection;
    }

    @Override
    protected XPropertySet _createDescriptor() {
        return new TableDescriptor(isCaseSensitive());
    }

/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.Connection.getTables()
    public TableContainer(Connection connection)
    {
        super(m_name, m_services, connection);
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
                m_Elements.add(new Table(m_Connection, catalog, schema, name, type, description));
                // FIXME: We must construct a unique name!!!
                m_Names.add(_getElementName(catalog, schema, name));
                System.out.println("TableContainer.TableContainer() 4");
            }
            result.close();
        }
        catch (java.lang.IllegalArgumentException | java.lang.SecurityException e) {
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
                          Catalog catalog)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        try {
            for (final schemacrawler.schema.Table t : catalog.getTables()) {
                String schema = t.getSchema().getName();
                String name = t.getName();
                m_Elements.add(new Table(m_Connection, t));
                // FIXME: We must construct a unique name!!!
                m_Names.add(_getElementName(t.getSchema().getCatalogName(), schema, name));
            }
        }
        catch (java.lang.IllegalArgumentException | java.lang.SecurityException e) {
            UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
    }*/


}
