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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.IndexType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.IndexHelper;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.driver.query.DDLParameter;
import io.github.prrvchr.uno.helper.SharedResources;


public final class IndexContainer
    extends Container<Index> {
    private static final String SERVICE = IndexContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Indexes",
                                              "com.sun.star.sdbcx.Container"};

    private final TableSuper mTable;
    private final ConnectionLog mLogger;

    // The constructor method:
    public IndexContainer(TableSuper table,
                          boolean sensitive,
                          List<String> indexes)
        throws ElementExistException {
        super(SERVICE, SERVICES, table, sensitive, indexes);
        mTable = table;
        mLogger = new ConnectionLog(table.getLogger(), LoggerObjectType.INDEXCONTAINER);
    }

    protected ConnectionSuper getConnection() {
        return mTable.getConnection();
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    public void dispose() {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_INDEXES_DISPOSING);
        super.dispose();
    }

    @Override
    protected Index createElement(String name)
        throws SQLException {
        Index index = null;
        try {
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            String separator = metadata.getCatalogSeparator();
            String qualifier = "";
            String subname;
            int position = -1;
            if (separator != null && !separator.isBlank()) {
                position = name.indexOf(separator);
            }
            if (position >= 0) {
                qualifier = name.substring(0, position);
                subname = name.substring(position + 1);
            } else {
                subname = name;
            }
            index = createIndex(metadata, qualifier, subname);
        } catch (java.sql.SQLException | ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return index;
    }

    private Index createIndex(java.sql.DatabaseMetaData metadata,
                              String qualifier,
                              String subname)
        throws java.sql.SQLException, ElementExistException {
        int type = -1;
        Index index = null;
        boolean found = false;
        boolean unique = false;
        List<String> columns = new ArrayList<>();
        final int NON_UNIQUE = 4;
        final int INDEX_QUALIFIER = 5;
        final int INDEX_NAME = 6;
        final int TYPE = 7;
        final int COLUMN_NAME = 9;
        NamedComponents table = mTable.getNamedComponents();
        try (java.sql.ResultSet result = metadata.getIndexInfo(table.getCatalog(),
                                                               table.getSchema(),
                                                               table.getTable(),
                                                               false, false)) {
            while (result.next()) {
                unique  = !result.getBoolean(NON_UNIQUE);
                if ((qualifier.isEmpty() || qualifier.equals(result.getString(INDEX_QUALIFIER)))
                                         && subname.equals(result.getString(INDEX_NAME))) {
                    found = true;
                    type = result.getShort(TYPE);
                    String columnName = result.getString(COLUMN_NAME);
                    if (!result.wasNull()) {
                        columns.add(columnName);
                    }
                }
            }
        }
        if (found) {
            Boolean primary = IndexHelper.isPrimaryKeyIndex(metadata, table, subname);
            boolean clustered = type == IndexType.CLUSTERED;
            index = new Index(mTable, isCaseSensitive(), subname, qualifier, unique, primary, clustered, columns);
        }
        return index;
    }

    @Override
    protected Index appendElement(XPropertySet descriptor)
        throws SQLException {
        Index index = null;
        String name = getElementName(descriptor);
        if (createIndex(descriptor, name)) {
            index = createElement(name);
        }
        return index;
    }

    private boolean createIndex(XPropertySet descriptor,
                                String name)
        throws SQLException {
        boolean created = false;
        String query = null;
        try {
            if (getConnection() != null) {
                ComposeRule rule = ComposeRule.InIndexDefinitions;
                DriverProvider provider = getConnection().getProvider();
                boolean unique = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISUNIQUE);
                XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
                XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
                List<String> indexes = new ArrayList<>();
                for (int i = 0; i < columns.getCount(); i++) {
                    XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                    String column = DBTools.getDescriptorStringValue(property, PropertyIds.NAME);
                    String index = provider.enquoteIdentifier(column, isCaseSensitive());
                    if (!unique && provider.addIndexAppendix()) {
                        if (DBTools.getDescriptorBooleanValue(property, PropertyIds.ISASCENDING)) {
                            index += " ASC";
                        } else {
                            index += " DESC";
                        }
                    }
                    indexes.add(index);
                }
                if (!indexes.isEmpty()) {
                    String table = DBTools.composeTableName(provider, mTable, rule, isCaseSensitive());
                    String index = provider.enquoteIdentifier(name, isCaseSensitive());
                    Map<String, Object> arguments = DDLParameter.getAddIndex(table, index, indexes);
                    query = provider.getDDLQuery().getAddIndexCommand(arguments, unique);
                    System.out.println("sdbcx.IndexContainer.createIndex() 1 Query: " + query);
                    table = DBTools.composeTableName(provider, mTable, rule, false);
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY, name, table, query);
                    created = DBTools.executeSQLQuery(provider, query);
                }
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (WrappedTargetException | IndexOutOfBoundsException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return created;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String elementName)
        throws SQLException {
        if (getConnection() == null) {
            return;
        }
        String name;
        int len = elementName.indexOf('.');
        name = elementName.substring(len + 1);
        String query = null;
        String table = null;
        try {
            ComposeRule rule = ComposeRule.InIndexDefinitions;
            DriverProvider provider = getConnection().getProvider();
            table = DBTools.composeTableName(provider, mTable, rule, isCaseSensitive());
            String constraint = provider.enquoteIdentifier(name, isCaseSensitive());
            query = provider.getDDLQuery().getDropConstraintCommand(DDLParameter.getDropConstraint(table, constraint),
                                                                    KeyType.UNIQUE);
            table = DBTools.composeTableName(provider, mTable, rule, false);
            System.out.println("sdbcx.IndexContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY, name, table, query);
            DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.IndexContainer.refreshInternal() *********************************");
        mTable.refreshIndexes();
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new IndexDescriptor(isCaseSensitive());
    }

    protected void removePrimaryKeyIndex()
        throws SQLException {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            Index index = Indexes.next();
            if (index.mIsPrimaryKeyIndex) {
                removeElement(index.getName(), false);
                break;
            }
        }
    }

    protected void removeForeignKeyIndex(String name)
            throws SQLException {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            Index index = Indexes.next();
            if (name.equals(index.getName())) {
                removeElement(name, false);
                break;
            }
        }
    }

    protected void renameIndexColumn(String oldname,
                                     String newname)
        throws SQLException {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            IndexColumnContainer columns = Indexes.next().getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameIndexColumn(oldname, newname);
                break;
            }
        }
    }

}
