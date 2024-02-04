/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║ 
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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
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
        m_logger = new ConnectionLog(connection.getLogger(), LoggerObjectType.TABLECONTAINER);
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    public void dispose()
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_TABLES_DISPOSING);
        super.dispose();
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.composeTableName(m_connection, descriptor, ComposeRule.InTableDefinitions, false);
        System.out.println("TableContainer._getElementName() table name: " + name);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }
    
    @Override
    public XPropertySet _appendElement(XPropertySet descriptor,
                                       String name)
        throws SQLException
    {
        XPropertySet table = null;
        if (_createTable(descriptor, name)) {
            table = _createElement(name);
        }
        return table;
    }

    private boolean _createTable(XPropertySet descriptor, String name)
        throws SQLException
    {
        String table = DBTools.composeTableName(m_connection, descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
        List<String> queries = DBTools.getCreateTableQueries(m_connection, descriptor, table, isCaseSensitive());
        String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
        if (!description.isEmpty()) {
            queries.add(DBTools.getCommentQuery("TABLE", table, description));
        }
        if (!queries.isEmpty()) {
            return DBTools.executeDDLQueries(m_connection, queries, m_logger, this.getClass().getName(),
                                             "_createTable", Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name);
        }
        return false;
    }

    @Override
    public XPropertySet _createElement(String name)
        throws SQLException
    {
        TableBase table = null;
        NameComponents component = DBTools.qualifiedNameComponents(m_connection, name, ComposeRule.InDataManipulation);
        try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
            if (result.next()) {
                String type = result.getString(4);
                type = result.wasNull() ? "" : m_connection.getProvider().getTableType(type);
                String remarks = result.getString(5);
                remarks = result.wasNull() ? "" : remarks;
                table = _getTable(component, type, remarks);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
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
            boolean isview = false;
            XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, _getElement(index));
            if (descriptor != null) {
                isview = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TYPE).equals("VIEW");
            }
            if (isview) {
                XDrop views = UnoRuntime.queryInterface(XDrop.class, m_connection.getViews());
                views.dropByName(name);
                return;
            }
            NameComponents nameComponents = DBTools.qualifiedNameComponents(m_connection, name, ComposeRule.InDataManipulation);
            String table = DBTools.composeTableName(m_connection, nameComponents.getCatalog(), nameComponents.getSchema(),
                                                    nameComponents.getTable(), isCaseSensitive(), ComposeRule.InDataManipulation);
            String query = String.format(m_connection.getProvider().getDropTableQuery(), table);
            System.out.println("TableContainer._removeElement() Query: " + query);
            DBTools.executeDDLQuery(m_connection, query, m_logger,  this.getClass().getName(),
                                     "_removeElement", Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name);
        }
        catch (WrappedTargetException | NoSuchElementException e) {
            throw UnoHelper.getSQLException(e, m_connection);
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
