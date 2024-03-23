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
import com.sun.star.uno.Exception;

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
    protected boolean createDataBaseElement(XPropertySet descriptor,
                                            String name)
        throws SQLException
    {
        List<String> queries = new ArrayList<String>();
        try {
            ComposeRule rule = ComposeRule.InTableDefinitions;
            System.out.println("TableContainerSuper._createDataBaseElement() 1");
            String table = DBTools.composeTableName(m_Connection.getProvider(), descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
            queries = DBTableHelper.getCreateTableQueries(m_Connection.getProvider(), descriptor, table, rule, isCaseSensitive());
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
                for (String query : queries) {
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name, query);
                }
                return DBTools.executeDDLQueries(m_Connection.getProvider(), queries);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = "<" + String.join("> <", queries) + ">";
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (IllegalArgumentException | WrappedTargetException |
               IndexOutOfBoundsException | UnknownPropertyException e) {
             int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
             String query = "<" + String.join("> <", queries) + ">";
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
        String query = null;
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
            query = m_Connection.getProvider().getDropTableQuery(table);
            System.out.println("TableContainer.removeDataBaseElement() 3 Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name, query);
            DBTools.executeDDLQuery(m_Connection.getProvider(), query);
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
    protected void renameReferencedTableName(String oldname,
                                             String newname)
        throws SQLException
    {
        for (String table: getElementNames()) {
            // XXX: We are looking for foreign key on other table.
            if (table.equals(newname)) {
                continue;
            }
            KeyContainer keys = getElement(table).getKeysInternal();
            for (String name: keys.getElementNames()) {
                Key key = keys.getElement(name);
                if (key.m_ReferencedTable.equals(oldname)) {
                    key.m_ReferencedTable = newname;
                }
            }
        }
    }

    // XXX: If the renamed column is a foreign key we need to rename the Key column name to.
    // XXX: Renaming the foreign key should rename the associated Index column name as well.
    protected void renameForeignKeyColumn(String referenced,
                                          String oldname,
                                          String newname)
        throws SQLException
    {
        for (String table: getElementNames()) {
            // XXX: We are looking for foreign key on other table.
            if (table.equals(referenced)) {
                continue;
            }
            KeyContainer keys = getElement(table).getKeysInternal();
            for (String name: keys.getElementNames()) {
                Key key = keys.getElement(name);
                if (key.m_ReferencedTable.equals(referenced)) {
                    key.m_ReferencedTable = newname;
                }
            }
        }
    }

    protected abstract T getTable(NameComponents component,
                                  String type,
                                  String remarks);

}
