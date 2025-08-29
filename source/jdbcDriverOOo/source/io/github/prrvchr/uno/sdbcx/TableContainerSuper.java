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

import java.sql.DatabaseMetaData;
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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.TableHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComposeRule;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.SharedResources;


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
            if (DBTools.hasDescriptorProperty(descriptor, PropertyID.TYPE)) {
                type = DBTools.getDescriptorStringValue(descriptor, PropertyID.TYPE);
            }
            Provider provider = mConnection.getProvider();
            ComposeRule rule = ComposeRule.InTableDefinitions;
            NamedSupport support = provider.getNamedSupport(rule);
            DatabaseMetaData metadata = provider.getConnection().getMetaData();
            queries = TableHelper.getCreateTableQueries(provider.getConfigDDL(), metadata,
                                                        support, descriptor, type, isCaseSensitive());
            String description = DBTools.getDescriptorStringValue(descriptor, PropertyID.DESCRIPTION);
            if (!description.isEmpty() && provider.getConfigDDL().supportsTableDescription()) {
                String table = ComponentHelper.composeTableName(provider.getNamedSupport(rule),
                                                                descriptor, isCaseSensitive());
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
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg, e);
            throw new SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
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
            ComposeRule rule = ComposeRule.InDataManipulation;
            NamedSupport support = mConnection.getProvider().getNamedSupport(rule);
            NamedComponent component = ComponentHelper.qualifiedNameComponents(support, name);
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
            throw new SQLException(e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return table;
    }

    private java.sql.ResultSet getcreateElementResultSet(NamedComponent table)
            throws SQLException {
        java.sql.DatabaseMetaData metadata = mConnection.getProvider().getConnection().getMetaData();
        return metadata.getTables(table.getCatalog(), table.getSchema(), table.getTable(), null);
    }

    @Override
    public void removeDataBaseElement(int index,
                                      String name)
        throws SQLException {
        boolean isview = false;
        TableSuper element = (TableSuper) getElementByName(name);
        if (element != null) {
            isview = element.mType.toUpperCase().contains("VIEW");
        }
        if (isview) {
            ViewContainer views = mConnection.getViewsInternal();
            views.removeElement(name, true);
        } else {
            removeDataBaseElement(name);
        }
    }

    private void removeDataBaseElement(String name) throws SQLException {
        String query = null;
        try {
            Provider provider = mConnection.getProvider();
            ComposeRule rule = ComposeRule.InTableDefinitions;
            NamedSupport support = mConnection.getProvider().getNamedSupport(rule);
            NamedComponent component = ComponentHelper.qualifiedNameComponents(support, name);
            String table = ComponentHelper.buildName(support, component, isCaseSensitive());
            query = provider.getConfigDDL().getDropTableCommand(ParameterDDL.getDropTable(table));
            System.out.println("TableContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY, name, query);
            DBTools.executeSQLQuery(mConnection.getProvider(), query);
        } catch (SQLException e) {
            int resource = Resources.STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg, e);
            throw new SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
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


    protected abstract T getTable(NamedComponent component,
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
