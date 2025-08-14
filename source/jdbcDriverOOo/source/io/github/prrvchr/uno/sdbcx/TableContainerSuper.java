/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.TableHelper;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;


public abstract class TableContainerSuper<T extends TableSuper>
    extends TableContainerMain<T> {

    // XXX: In order to be able to remove correctly any reference after deleting
    // XXX: a table, I need to keep track of all tables with a foreign key.
    // XXX: Only loading a foreign Key will add entry to mReferencedTables
    private Map<TableSuper, Set<TableSuper>> mReferencedTables = new HashMap<>();

    // The constructor method:
    public TableContainerSuper(String service,
                               String[] services,
                               ConnectionSuper connection,
                               boolean sensitive,
                               String[] names)
        throws ElementExistException {
        super(service, services, connection, sensitive, names, LoggerObjectType.TABLECONTAINER);
    }

    protected ConnectionSuper getConnection() {
        return mConnection;
    }

    protected boolean isReferencedTable(TableSuper primary) {
        return mReferencedTables.containsKey(primary);
    }

    protected void addReferencedTables(TableSuper primary, TableSuper foreign) {
        if (!mReferencedTables.containsKey(primary)) {
            mReferencedTables.put(primary, new HashSet<>());
        }
        Set<TableSuper> refs = mReferencedTables.get(primary);
        if (!refs.contains(foreign)) {
            refs.add(foreign);
        }
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
        List<String> queries = new ArrayList<>();
        try {
            String type = "TABLE";
            if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.TYPE)) {
                type = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TYPE);
            }
            ComposeRule rule = ComposeRule.InTableDefinitions;
            Provider provider = mConnection.getProvider();
            String table = DBTools.composeTableName(provider, descriptor, rule, isCaseSensitive());
            queries = TableHelper.getCreateTableQueries(provider, descriptor, table, type, rule, isCaseSensitive());
            String description = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!description.isEmpty() && provider.getConfigDDL().supportsTableDescription()) {
                Map<String, Object> arguments = ParameterDDL.getTableDescription(table, description);
                String query = provider.getConfigDDL().getTableDescriptionCommand(arguments);
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
        } catch (SQLException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw new SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (IllegalArgumentException e) {
            int resource = Resources.STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw new SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return created;
    }

    @Override
    public T createElement(String name)
        throws SQLException {
        T table = null;
        final int TABLE_TYPE = 4; 
        final int REMARKS = 5; 
        try {
            NamedComponents component = DBTools.qualifiedNameComponents(mConnection.getProvider(), name,
                                                                        ComposeRule.InDataManipulation);
            try (java.sql.ResultSet result = getcreateElementResultSet(component)) {
                if (result.next()) {
                    String type = result.getString(TABLE_TYPE);
                    if (result.wasNull()) {
                        type = "";
                    } else {
                        type = mConnection.getProvider().getConfigSQL().getTableType(type);
                    }
                    String remarks = result.getString(REMARKS);
                    if (result.wasNull()) {
                        remarks = "";
                    }
                    table = getTable(component, type, remarks);
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e.getMessage(), StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return table;
    }

    private java.sql.ResultSet getcreateElementResultSet(NamedComponents table)
            throws SQLException {
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
            TableSuper element = (TableSuper) getElementByName(name);
            if (element != null) {
                isview = element.mType.toUpperCase().contains("VIEW");
            }
            if (isview) {
                ViewContainer views = mConnection.getViewsInternal();
                views.removeElement(name, true);
                return;
            }
            Provider provider = mConnection.getProvider();
            ComposeRule rule = ComposeRule.InDataManipulation;
            NamedComponents component = DBTools.qualifiedNameComponents(mConnection.getProvider(), name, rule);
            String table = DBTools.buildName(provider, component, rule, isCaseSensitive());
            query = provider.getConfigDDL().getDropTableCommand(ParameterDDL.getDropTable(table));
            System.out.println("TableContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name, query);
            DBTools.executeSQLQuery(mConnection.getProvider(), query);
        } catch (SQLException e) {
            int resource = Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw new SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }


    // XXX: ColumnListener methods
    protected void removeReferencedColumns(ColumnBase column, String name) {
        for (TableSuper table : mReferencedTables.get(column.getTableInternal())) {
            removeReferencedColumns(column.getTableInternal(), table, name);
        }
    }


    // XXX: TableListener methods
    protected void removeForeignKeyTables(TableSuper table, String name) {
        if (mReferencedTables.containsKey(table)) {
            removeForeignKeyTables(table, name);
            mReferencedTables.remove(table);
        }
    }


    protected abstract T getTable(NamedComponents component,
                                  String type,
                                  String remarks);

    // XXX: ColumnListener methods
    @SuppressWarnings("unused")
    private void removeReferencedColumns(TableSuper primary, TableSuper foreign, String name) {
        //removeReferencedKeyColumns(primary, foreign, name);
        //removeReferencedIndexColumns(foreign, name);
    }

    @SuppressWarnings("unused")
    private void removeReferencedKeyColumns(TableSuper primary, TableSuper foreign, String name) {
        Iterator<Key> it = foreign.getKeysInternal().getActiveElements();
        while (it.hasNext()) {
            Key key = it.next();
            if (key.getTypeInternal() == KeyType.FOREIGN &&
                key.getRefTableInternal().equals(primary)) {
                KeyColumns columns = key.getColumnsInternal();
                Iterator<KeyColumn> iter = columns.getActiveElements();
                while (iter.hasNext()) {
                    KeyColumn column = iter.next();
                    if (column.getName().equals(name)) {
                        iter.remove();
                        System.out.println("TableContainer.removeReferencedKeyColumns() 1 **************");
                    }
                }
                if (!columns.hasElements()) {
                    it.remove();
                    System.out.println("TableContainer.removeReferencedKeyColumns() 2 **************");
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void removeReferencedIndexColumns(TableSuper foreign, String name) {
        Iterator<Index> it = foreign.getIndexesInternal().getActiveElements();
        while (it.hasNext()) {
            Index index = it.next();
            IndexColumns columns = index.getColumnsInternal();
            Iterator<IndexColumn> iter = columns.getActiveElements();
            while (iter.hasNext()) {
                IndexColumn column = iter.next();
                if (column.getName().equals(name)) {
                    iter.remove();
                    System.out.println("TableContainer.removeReferencedIndexColumns() 1 **************");
                }
            }
            if (!columns.hasElements()) {
                it.remove();
                System.out.println("TableContainer.removeReferencedIndexColumns() 2 **************");
            }
        }
    }



    @SuppressWarnings("unused")
    private void removeForeignKeyTables1(TableSuper t, String name) {
        for (TableSuper table : mReferencedTables.get(t)) {
            Iterator<Key> it = table.getKeysInternal().getActiveElements();
            while (it.hasNext()) {
                Key key = it.next();
                if (key.getReferencedTableInternal().equals(name)) {
                    it.remove();
                    System.out.println("TableContainer.removeForeignKeyTables() 1 **************");
                }
            }
        }
    }

}
