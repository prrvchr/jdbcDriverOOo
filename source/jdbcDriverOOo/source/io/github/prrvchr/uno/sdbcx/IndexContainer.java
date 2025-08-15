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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.IndexHelper;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public final class IndexContainer
    extends ContainerBase<Index> {

    private static final String SERVICE = IndexContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Indexes",
                                              "com.sun.star.sdbcx.Container"};

    private final TableSuper mTable;
    private final ConnectionLog mLogger;

    // The constructor method:
    public IndexContainer(TableSuper table,
                          boolean sensitive,
                          String[] indexes) {
        super(SERVICE, SERVICES, table, sensitive, indexes);
        mTable = table;
        System.out.println("sdbcx.IndexContainer() indexes: " + String.join(", ", indexes));
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

    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException {
        synchronized (mLock) {
            if (index < 0 || index >= getCount()) {
                throw new IndexOutOfBoundsException();
            }
            try {
                dropByName(mBimap.getName(index));
            } catch (NoSuchElementException e) {
                throw new IndexOutOfBoundsException(e);
            }
        }
    }

    @Override
    public void dropByName(String name)
        throws SQLException,
               NoSuchElementException {
        if (!hasByName(name)) {
            throw new NoSuchElementException();
        }
        synchronized (mLock) {
            System.out.println("sdbcx.IndexContainer() dropByName: " + name);
            // XXX: we need to test if the index come from a foreign key
            // XXX: and if not then it's really an index
            try {
                boolean really = true;
                if (mTable.getKeysInternal().hasByName(name)) {
                    mTable.getKeysInternal().removeElement(name, true);
                    really = false;
                }
                removeElement(name, really);
            } catch (java.sql.SQLException e) {
                throw new SQLException(e);
            }
        }
    }

    @Override
    protected Index createElement(String name)
        throws java.sql.SQLException {
        Index index = null;
        Provider provider = getConnection().getProvider();
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        boolean start = metadata.isCatalogAtStart();
        String separator = metadata.getCatalogSeparator();
        String qualifier = "";
        String subname;
        int position = -1;
        if (separator != null && !separator.isBlank()) {
            if (start) {
                position = name.indexOf(separator);
            } else {
                position = name.lastIndexOf(separator);
            }
        }
        if (position >= 0) {
            if (start) {
                qualifier = name.substring(0, position);
                subname = name.substring(position + 1);
            } else {
                qualifier = name.substring(position + 1);
                subname = name.substring(0, position);
            }
        } else {
            subname = name;
        }
        NamedComponents component = mTable.getNamedComponents();
        index = IndexHelper.createIndex(provider, metadata, mTable, component,
                                        qualifier, subname, isCaseSensitive());
        return index;
    }

    @Override
    protected Index appendElement(XPropertySet descriptor)
        throws java.sql.SQLException {
        Index index = null;
        String name = getElementName(descriptor);
        if (createIndex(descriptor, name)) {
            index = createElement(name);
        }
        return index;
    }

    private boolean createIndex(XPropertySet descriptor,
                                String name)
        throws java.sql.SQLException {
        boolean created = false;
        String query = null;
        try {
            if (getConnection() != null) {
                ComposeRule rule = ComposeRule.InIndexDefinitions;
                Provider provider = getConnection().getProvider();
                boolean unique = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISUNIQUE);
                XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
                XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
                List<String> indexes = new ArrayList<>();
                for (int i = 0; i < columns.getCount(); i++) {
                    XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                    String column = DBTools.getDescriptorStringValue(property, PropertyIds.NAME);
                    String index = provider.enquoteIdentifier(column, isCaseSensitive());
                    if (!unique && provider.getConfigSQL().addIndexAppendix()) {
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
                    Map<String, Object> arguments = ParameterDDL.getAddIndex(table, index,
                                                                             indexes.toArray(new String[0]));
                    query = provider.getConfigDDL().getAddIndexCommand(arguments, unique);
                    System.out.println("sdbcx.IndexContainer.createIndex() 1 Query: " + query);
                    table = DBTools.composeTableName(provider, mTable, rule, false);
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY, name, table, query);
                    created = DBTools.executeSQLQuery(provider, query);
                }
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), e);
        } catch (WrappedTargetException | IndexOutOfBoundsException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), e);
        }
        return created;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String elementName)
        throws java.sql.SQLException {
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
            Provider provider = getConnection().getProvider();
            table = DBTools.composeTableName(provider, mTable, rule, isCaseSensitive());
            String constraint = provider.enquoteIdentifier(name, isCaseSensitive());
            query = provider.getConfigDDL().getDropConstraintCommand(ParameterDDL.getDropConstraint(table, constraint),
                                                                    KeyType.UNIQUE);
            table = DBTools.composeTableName(provider, mTable, rule, false);
            System.out.println("sdbcx.IndexContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY, name, table, query);
            DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
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
        Iterator<Index> it = getActiveElements();
        while (it.hasNext()) {
            Index index = it.next();
            if (index.mIsPrimaryKeyIndex) {
                it.remove();
                break;
            }
        }
    }

    protected void removeForeignKeyIndex(String name)
            throws SQLException {
        Iterator<Index> it = getActiveElements();
        while (it.hasNext()) {
            Index index = it.next();
            if (name.equals(index.getName())) {
                it.remove();
                break;
            }
        }
    }

    protected void renameIndexColumn(String oldname,
                                     String newname)
        throws SQLException {
        Iterator<Index> it = getActiveElements();
        while (it.hasNext()) {
            IndexColumns columns = it.next().getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameIndexColumn(oldname, newname);
                break;
            }
        }
    }

}
