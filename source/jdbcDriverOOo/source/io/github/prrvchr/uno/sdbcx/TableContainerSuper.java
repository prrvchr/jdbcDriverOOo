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

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.TableHelper;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.driver.query.DDLParameter;


public abstract class TableContainerSuper<T extends TableSuper>
    extends TableContainerMain<T> {

    // The constructor method:
    public TableContainerSuper(String service,
                               String[] services,
                               ConnectionSuper connection,
                               boolean sensitive,
                               List<String> names)
        throws ElementExistException {
        super(service, services, connection, sensitive, names, LoggerObjectType.TABLECONTAINER);
    }

    protected ConnectionSuper getConnection() {
        return mConnection;
    }

    @Override
    public void dispose() {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_TABLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean createDataBaseElement(XPropertySet descriptor,
                                            String name)
        throws SQLException {
        boolean created = false;
        List<String> queries = new ArrayList<String>();
        try {
            String type = "TABLE";
            if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.TYPE)) {
                type = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TYPE);
            }
            ComposeRule rule = ComposeRule.InTableDefinitions;
            DriverProvider provider = mConnection.getProvider();
            String table = DBTools.composeTableName(provider, descriptor, rule, isCaseSensitive());
            queries = TableHelper.getCreateTableQueries(provider, descriptor, table, type, rule, isCaseSensitive());
            String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!description.isEmpty() && provider.getDDLQuery().supportsTableDescription()) {
                Map<String, Object> arguments = DDLParameter.getTableDescription(table, description);
                String query = provider.getDDLQuery().getTableDescriptionCommand(arguments);
                queries.add(query);
            }
            for (String query : queries) {
                System.out.println("TableContainerSuper._createDataBaseElement() Queries: " + query);
            }
            if (!queries.isEmpty()) {
                for (String query : queries) {
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY, name, query);
                }
                created = DBTools.executeSQLQueries(mConnection.getProvider(), queries);
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (IllegalArgumentException | WrappedTargetException |
                 IndexOutOfBoundsException | UnknownPropertyException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, (Exception) e);
        }
        return created;
    }

    @Override
    public T createElement(String name)
        throws SQLException {
        T table = null;
        try {
            NamedComponents component = DBTools.qualifiedNameComponents(mConnection.getProvider(), name,
                                                                        ComposeRule.InDataManipulation);
            try (java.sql.ResultSet result = _getcreateElementResultSet(component)) {
                System.out.println("TableContainerSuper.createElement() 1");
                if (result.next()) {
                    System.out.println("TableContainerSuper.createElement() 2");
                    // CHECKSTYLE:OFF: MagicNumber - Specific for database
                    String type = result.getString(4);
                    // CHECKSTYLE:ON: MagicNumber - Specific for database
                    if (result.wasNull()) {
                        type = "";
                    } else {
                        type = mConnection.getProvider().getTableType(type);
                    }
                    // CHECKSTYLE:OFF: MagicNumber - Specific for database
                    String remarks = result.getString(5);
                    // CHECKSTYLE:ON: MagicNumber - Specific for database
                    if (result.wasNull()) {
                        remarks = "";
                    }
                    table = getTable(component, type, remarks);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return table;
    }

    private java.sql.ResultSet _getcreateElementResultSet(NamedComponents table)
            throws java.sql.SQLException {
        String msg = "TableContainerSuper._getcreateElementResultSet() 1 ";
        System.out.println(msg + table.getCatalog() + " - " + table.getSchema() + " - " + table.getTableName());
        java.sql.DatabaseMetaData metadata = mConnection.getProvider().getConnection().getMetaData();
        return metadata.getTables(table.getCatalog(), table.getSchema(), table.getTable(), null);
    }

    @Override
    public void removeDataBaseElement(int index,
                                      String name)
        throws SQLException {
        String query = null;
        try {
            boolean isview = false;
            TableSuper element = (TableSuper) getElement(name);
            if (element != null) {
                isview = element.mType.toUpperCase().contains("VIEW");
            }
            if (isview) {
                ViewContainer views = mConnection.getViewsInternal();
                views.dropByName(name);
                return;
            }
            DriverProvider provider = mConnection.getProvider();
            ComposeRule rule = ComposeRule.InDataManipulation;
            NamedComponents component = DBTools.qualifiedNameComponents(mConnection.getProvider(), name, rule);
            String table = DBTools.buildName(provider, component, rule, isCaseSensitive());
            query = provider.getDDLQuery().getDropTableCommand(DDLParameter.getDropTable(table));
            System.out.println("TableContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name, query);
            DBTools.executeSQLQuery(mConnection.getProvider(), query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (NoSuchElementException e) {
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
    // XXX: So we need to rename the referenced table name in all other
    // XXX: table having a foreign keys referencing this table.
    protected void renameReferencedTableName(List<String> filter,
                                             String oldname,
                                             String newname)
        throws SQLException {
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
                if (key.mReferencedTable.equals(oldname)) {
                    key.mReferencedTable = newname;
                }
            }
        }
    }

    // XXX: If the renamed column is a foreign key we need to rename the RelatedColumn on the KeyColumn to.
    protected void renameForeignKeyColumn(Map<String, List<String>> filters,
                                          String referenced,
                                          String oldname,
                                          String newname)
        throws SQLException {
        Iterator<String> tables = getActiveNames(filters.keySet());
        while (tables.hasNext()) {
            // XXX: We are looking for foreign key on other table.
            String table = tables.next();
            getElement(table).getKeysInternal().renameForeignKeyColumn(filters.get(table), referenced,
                                                                       oldname, newname);
        }
    }

    protected abstract T getTable(NamedComponents component,
                                  String type,
                                  String remarks);

}
