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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.sun.star.uno.Exception;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.helper.DBTableHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;


public abstract class TableContainerSuper<C extends ConnectionSuper, T extends TableSuper<?>>
    extends TableContainerMain<C, T>
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

    @Override
    public void dispose()
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_TABLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean createDataBaseElement(XPropertySet descriptor,
                                            String name)
        throws SQLException
    {
        List<String> queries = new ArrayList<String>();
        try {
            String type = "TABLE";
            if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.TABLETYPE)) {
                type = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TABLETYPE);
            }
            ComposeRule rule = ComposeRule.InTableDefinitions;
            String table = DBTools.composeTableName(m_Connection.getProvider(), descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
            queries = DBTableHelper.getCreateTableQueries(m_Connection.getProvider(), descriptor, table, type, rule, isCaseSensitive());
            String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!description.isEmpty() && m_Connection.getProvider().supportsTableDescription()) {
                String query = m_Connection.getProvider().getTableDescriptionQuery(table, description);
                queries.add(query);
            }
            for (String query : queries) {
                System.out.println("TableContainerSuper._createDataBaseElement() Queries: " + query);
            }
            if (!queries.isEmpty()) {
                for (String query : queries) {
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name, query);
                }
                return DBTools.executeSQLQueries(m_Connection.getProvider(), queries);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (IllegalArgumentException | WrappedTargetException |
               IndexOutOfBoundsException | UnknownPropertyException e) {
             int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
             String query = String.join("> <", queries);
             String msg = getLogger().getStringResource(resource, name, query);
             getLogger().logp(LogLevel.SEVERE, msg);
             throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, (Exception) e);
         }
        return false;
    }

    @Override
    public T createElement(String name)
        throws SQLException
    {
        T table = null;
        try {
            NamedComponents component = DBTools.qualifiedNameComponents(m_Connection.getProvider(), name, ComposeRule.InDataManipulation);
            try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
                System.out.println("TableContainerSuper.createElement() 1");
                if (result.next()) {
                    System.out.println("TableContainerSuper.createElement() 2");
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

    private java.sql.ResultSet _getcreateElementResultSet(NamedComponents table)
            throws java.sql.SQLException
        {
            System.out.println("TableContainerSuper._getcreateElementResultSet() 1 " + table.getCatalog() + " - " + table.getSchema() + " - " + table.getTableName());
            java.sql.DatabaseMetaData metadata = m_Connection.getProvider().getConnection().getMetaData();
            return metadata.getTables(table.getCatalog(), table.getSchema(), table.getTableName(), null);
        }

    @Override
    public void removeDataBaseElement(int index,
                                      String name)
        throws SQLException
    {
        String query = null;
        try {
            boolean isview = false;
            T element = getElement(name);
            if (element != null) {
                isview = element.m_Type.toUpperCase().contains("VIEW");
            }
            if (isview) {
                ViewContainer views = m_Connection.getViewsInternal();
                views.dropByName(name);
                return;
            }
            NamedComponents cpt = DBTools.qualifiedNameComponents(m_Connection.getProvider(), name, ComposeRule.InDataManipulation);
            String table = DBTools.buildName(m_Connection.getProvider(), cpt.getCatalogName(), cpt.getSchemaName(),
                                             cpt.getTableName(), ComposeRule.InDataManipulation, isCaseSensitive());
            query = m_Connection.getProvider().getDropTableQuery(table);
            System.out.println("TableContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name, query);
            DBTools.executeSQLQuery(m_Connection.getProvider(), query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (NoSuchElementException e) {
            int resource = Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    // XXX: This is the Java implementation of com.sun.star.sdbcx.XContainer interface for the
    // XXX: com.sun.star.sdbcx.XRename interface available for the com.sun.star.sdbcx.XTable and XView
    // XXX: This is called from TableSuper.rename(String name) (ie: com.sun.star.sdbcx.XRename)
    // XXX: If renamed table are part of a foreign key the referenced table name is not any more valid.
    // XXX: So we need to rename the referenced table name in all other table having a foreign keys referencing this table.
    protected void renameReferencedTableName(List<String> filter,
                                             String oldname,
                                             String newname)
        throws SQLException
    {
        Iterator<T> tables = getActiveElements(filter);
        while (tables.hasNext()) {
            T table = tables.next();
            // XXX: We are looking for foreign key on other table.
            if (table.getName().equals(newname)) {
                continue;
            }
            Iterator<Key> keys = table.getKeysInternal().getActiveElements();
            while (keys.hasNext()) {
                Key key = keys.next();
                if (key.m_ReferencedTable.equals(oldname)) {
                    key.m_ReferencedTable = newname;
                }
            }
        }
    }

    // XXX: If the renamed column is a foreign key we need to rename the RelatedColumn on the KeyColumn to.
    protected void renameForeignKeyColumn(Map<String, List<String>> filters,
                                          String referenced,
                                          String oldname,
                                          String newname)
        throws SQLException
    {
        Iterator<String> tables = getActiveNames(filters.keySet());
        while (tables.hasNext()) {
            // XXX: We are looking for foreign key on other table.
            String table = tables.next();
            getElement(table).getKeysInternal().renameForeignKeyColumn(filters.get(table), referenced, oldname, newname);
        }
    }

    protected abstract T getTable(NamedComponents component,
                                  String type,
                                  String remarks);

}
