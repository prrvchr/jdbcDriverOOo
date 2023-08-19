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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.ConnectionLog.ObjectType;
import io.github.prrvchr.jdbcdriver.DataBaseTools.NameComponents;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public abstract class TableContainerBase
    extends Container
{

    protected final ConnectionSuper m_connection;
    protected final ConnectionLog m_logger; 

    // The constructor method:
    public TableContainerBase(ConnectionSuper connection,
                              boolean sensitive,
                              List<String> names)
        throws ElementExistException
    {
        super(connection, sensitive, names);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), ObjectType.TABLES);
    }

    public int getObjectId()
    {
        return m_logger.getObjectId();
    }

    public void dispose()
    {
        m_logger.logp(LogLevel.FINE, Resources.STR_LOG_TABLECONTAINER_DISPOSING);
        super.dispose();
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
        String sql = DataBaseTools.getCreateTableQuery(m_connection, descriptor, null, "(M,D)");
        m_logger.logp(LogLevel.FINE, Resources.STR_LOG_TABLECONTAINER_CREATE_TABLE, sql);
        try {
            java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
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
                System.out.println("TableContainerBase._createElement() 1 Type: " + type);
                type = result.wasNull() ? "" : m_connection.getProvider().getTableType(type);
                String remarks = result.getString(5);
                System.out.println("TableContainerBase._createElement() 1 Remarks: " + remarks);
                remarks = result.wasNull() ? "" : remarks;
                table = _getTable(component, type, remarks);
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
            if (isView) {
                XDrop dropView = UnoRuntime.queryInterface(XDrop.class, m_connection.getViews());
                String unquotedName = DataBaseTools.composeTableName(m_connection, nameComponents.getCatalog(), nameComponents.getSchema(),
                                                                     nameComponents.getTable(), false, ComposeRule.InDataManipulation);
                dropView.dropByName(unquotedName);
                return;
            }
            String composedName = DataBaseTools.composeTableName(m_connection, nameComponents.getCatalog(), nameComponents.getSchema(),
                    nameComponents.getTable(), true, ComposeRule.InDataManipulation);
            String sql = String.format(m_connection.getProvider().getDropTableQuery(), composedName);
            System.out.println("TableContainer._removeElement() Query: " + sql);
            java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement();
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

    protected abstract TableBase _getTable(NameComponents component,
                                           String type,
                                           String remarks);


}
