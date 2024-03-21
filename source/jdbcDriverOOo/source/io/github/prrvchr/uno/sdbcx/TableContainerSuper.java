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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTableHelper;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;


public abstract class TableContainerSuper<T extends TableSuper<?>, C extends ConnectionSuper>
    extends TableContainerMain<T, C>
{

    // The constructor method:
    public TableContainerSuper(String service,
                               String[] services,
                               C connection,
                               boolean sensitive,
                               List<String> names)
        throws ElementExistException
    {
        super(service, services, connection, sensitive, names, LoggerObjectType.TABLECONTAINER);
    }

    public void dispose()
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_TABLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean createDataBaseElement(XPropertySet descriptor, String name)
        throws SQLException
    {
        try {
            ComposeRule rule = ComposeRule.InTableDefinitions;
            System.out.println("TableContainerSuper._createDataBaseElement() 1");
            String table = DBTools.composeTableName(m_Connection.getProvider(), descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
            List<String> queries = DBTableHelper.getCreateTableQueries(m_Connection.getProvider(), descriptor, table, rule, isCaseSensitive());
            String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!description.isEmpty() && m_Connection.getProvider().supportsTableDescription()) {
                String query = m_Connection.getProvider().getTableDescriptionQuery(table, description);
                System.out.println("sdbcx.TableContainerSuper._createDataBaseElement() 2 Description query: " + query);
                queries.add(query);
            }
            for (String query : queries) {
                System.out.println("TableContainerSuper._createDataBaseElement() 3 Queries: " + query);
            }
            if (!queries.isEmpty()) {
                return DBTools.executeDDLQueries(m_Connection.getProvider(), getLogger(), queries, this.getClass().getName(),
                                                 "_createTable", Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name);
            }
        }
        catch (java.sql.SQLException | IllegalArgumentException |
               WrappedTargetException | IndexOutOfBoundsException | UnknownPropertyException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return false;
    }

    @Override
    public T createElement(String name)
        throws SQLException
    {
        T table = null;
        try {
            NameComponents component = DBTools.qualifiedNameComponents(m_Connection.getProvider(), name, ComposeRule.InDataManipulation);
            try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
                if (result.next()) {
                    String type = result.getString(4);
                    type = result.wasNull() ? "" : m_Connection.getProvider().getTableType(type);
                    String remarks = result.getString(5);
                    remarks = result.wasNull() ? "" : remarks;
                    table = getTable(component, type, remarks);
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
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
    public void removeDataBaseElement(int index,
                                      String name)
        throws SQLException
    {
        try {
            System.out.println("TableContainer.removeDataBaseElement() 1 Name " + name);
            boolean isview = false;
            T element = getElement(name);
            System.out.println("TableContainer.removeDataBaseElement() 2 element " + element.m_Type);
            if (element != null) {
                isview = element.m_Type.toUpperCase().contains("VIEW");
            }
            if (isview) {
                ViewContainer views = m_Connection.getViewsInternal();
                views.dropByName(name);
                return;
            }
            NameComponents cpt = DBTools.qualifiedNameComponents(m_Connection.getProvider(), name, ComposeRule.InDataManipulation);
            String table = DBTools.buildName(m_Connection.getProvider(), cpt.getCatalog(), cpt.getSchema(),
                                             cpt.getTable(), ComposeRule.InDataManipulation, isCaseSensitive());
            String query = m_Connection.getProvider().getDropTableQuery(table);
            System.out.println("TableContainer.removeDataBaseElement() 3 Query: " + query);
            DBTools.executeDDLQuery(m_Connection.getProvider(), getLogger(), query, this.getClass().getName(),
                                    "removeDataBaseElement", Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name);
        }
        catch (java.sql.SQLException | NoSuchElementException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    protected abstract T getTable(NameComponents component,
                                  String type,
                                  String remarks);

}
