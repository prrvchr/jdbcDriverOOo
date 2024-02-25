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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class TableContainerSuper
    extends TableContainerMain<TableSuper>
{

    // The constructor method:
    public TableContainerSuper(String service,
                              String[] services,
                              ConnectionSuper connection,
                              boolean sensitive,
                              List<String> names)
        throws ElementExistException
    {
        super(service, services, connection, sensitive, names, LoggerObjectType.TABLECONTAINER);
    }

    public void dispose()
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_TABLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean _createDataBaseElement(XPropertySet descriptor, String name)
        throws SQLException
    {
        String table = DBTools.composeTableName(m_Connection, descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
        List<String> queries = DBTools.getCreateTableQueries(m_Connection, descriptor, table, isCaseSensitive());
        String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
        if (!description.isEmpty() && m_Connection.getProvider().supportsTableDescription()) {
            queries.add(m_Connection.getProvider().getTableDescriptionQuery(table, description));
        }
        if (!queries.isEmpty()) {
            return DBTools.executeDDLQueries(m_Connection, queries, m_logger, this.getClass().getName(),
                                             "_createTable", Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name);
        }
        return false;
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.composeTableName(m_Connection, descriptor, ComposeRule.InTableDefinitions, false);
        System.out.println("TableContainer._getElementName() table name: " + name);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    public TableSuper _createElement(String name)
        throws SQLException
    {
        TableSuper table = null;
        NameComponents component = DBTools.qualifiedNameComponents(m_Connection, name, ComposeRule.InDataManipulation);
        try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
            if (result.next()) {
                String type = result.getString(4);
                type = result.wasNull() ? "" : m_Connection.getProvider().getTableType(type);
                String remarks = result.getString(5);
                remarks = result.wasNull() ? "" : remarks;
                table = _getTable(component, type, remarks);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_Connection);
        }
        return table;
    }

    private java.sql.ResultSet _getcreateElementResultSet(NameComponents component)
            throws java.sql.SQLException
        {
            String catalog = component.getCatalog().isEmpty() ? null : component.getCatalog();
            String schema = component.getSchema().isEmpty() ? null : component.getSchema();
            java.sql.DatabaseMetaData metadata = m_Connection.getProvider().getConnection().getMetaData();
            return metadata.getTables(catalog, schema, component.getTable(), null);
        }

    @Override
    public void _removeElement(int index,
                               String name)
        throws SQLException
    {
        try {
            System.out.println("TableContainer._removeElement() 1 Name " + name);
            boolean isview = false;
            TableSuper element = getElement(name);
            System.out.println("TableContainer._removeElement() 2 element " + element.m_Type);
            if (element != null) {
                isview = element.m_Type.toUpperCase().contains("VIEW");
            }
            if (isview) {
                ViewContainer views = m_Connection.getViewsInternal();
                views.dropByName(name);
                return;
            }
            NameComponents cpt = DBTools.qualifiedNameComponents(m_Connection, name, ComposeRule.InDataManipulation);
            String table = DBTools.getTableName(m_Connection, cpt.getCatalog(), cpt.getSchema(),
                                                cpt.getTable(), ComposeRule.InDataManipulation, isCaseSensitive());
            String query = DBTools.getDropTableQuery(table);
            System.out.println("TableContainer._removeElement() 3 Query: " + query);
            DBTools.executeDDLQuery(m_Connection, query, m_logger, this.getClass().getName(),
                                    "_removeElement", Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name);
        }
        catch (NoSuchElementException e) {
            throw UnoHelper.getSQLException(e, m_Connection);
        }
    }

    protected void insertElement(String name,
                                 TableSuper element)
    {
        synchronized (m_Connection) {
            if (!m_Elements.containsKey(name)) {
                m_Elements.put(name, element);
                m_Names.add(name);
            }
        }
    }

    protected abstract TableSuper _getTable(NameComponents component,
                                           String type,
                                           String remarks);


}
