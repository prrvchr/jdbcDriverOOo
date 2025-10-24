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

import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComposeRule;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.ConstraintHelper;
import io.github.prrvchr.uno.driver.helper.KeyHelper;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.helper.KeyHelper.ForeignKeyProperties;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.SharedResources;


public final class KeyContainer
    extends ContainerBase<Key> {

    private static final String SERVICE = KeyContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Keys",
                                              "com.sun.star.sdbcx.Container"};

    protected final TableSuper mTable;
    private final ConnectionLog mLogger; 

    // The constructor method:
    public KeyContainer(TableSuper table,
                        boolean sensitive,
                        String[] keys) {
        super(SERVICE, SERVICES, table, sensitive, keys);
        mLogger = new ConnectionLog(table.getLogger(), LoggerObjectType.KEYCONTAINER);
        mTable = table;
    }

    protected ConnectionSuper getConnection() {
        return mTable.getConnection();
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    public void dispose() {
        mLogger.logprb(LogLevel.INFO, Resources.STR_LOG_KEYS_DISPOSING);
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
        throws SQLException, NoSuchElementException {
        System.out.println("sdbcx.KeyContainer() dropByName: " + name);
        // XXX: we need to delete any corresponding index
        if (!hasByName(name)) {
            System.out.println("sdbcx.Container.dropByName() ERROR: " + name);
            throw new NoSuchElementException();
        }
        try {
            removeElement(name, true);
            if (mTable.getIndexesInternal().hasByName(name)) {
                mTable.getIndexesInternal().removeElement(name, false);
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    protected Key createElement(String name)
        throws java.sql.SQLException {

        Key key = null;
        if (!name.isEmpty()) {
            ComposeRule rule = ComposeRule.InDataManipulation;
            System.out.println("KeyContainer.createElement() 1 Name: " + name);
            key = readKey(name, rule);
            // XXX: For foreign keys to track any changes on the referenced table name,
            // XXX: this table itself is loaded and referenced by the key.
            // XXX: This avoids listeners and for the moment I haven't found anything simpler...
            if (key.getTypeInternal() == KeyType.FOREIGN) {
                getConnection().getTablesInternal().addReferencedTables(key.getRefTableInternal(), key.getTable());
            }
        }
        System.out.println("KeyContainer.createElement() 2 Name: " + name);
        return key;
    }


    private Key readKey(String keyname, ComposeRule rule)
        throws java.sql.SQLException {
        Key key;
        Provider provider = getConnection().getProvider();
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        NamedComponent component = mTable.getNamedComponents();
        String[] columns = KeyHelper.readPrimaryKeyColumns(metadata, component, keyname);
        if (columns != null) {
            key = new Key(mTable, null, isCaseSensitive(), keyname, KeyType.PRIMARY, 0, 0, columns);
        } else {
            key = readForeignKey(provider, metadata, component, keyname, rule);
        }
        return key;
    }

    private Key readForeignKey(Provider provider,
                               java.sql.DatabaseMetaData metadata,
                               NamedComponent component,
                               String keyname,
                               ComposeRule rule)
        throws java.sql.SQLException {
        Key key = null;
        ForeignKeyProperties properties = KeyHelper.getForeignKeyProperties(metadata, component, keyname);
        if (properties != null) {
            String tablename = ComponentHelper.buildName(provider.getNamedSupport(rule), properties.mTable);
            TableSuper refTable = getConnection().getTablesInternal().getElementByName(tablename);
            key = new Key(mTable, refTable, isCaseSensitive(), keyname, KeyType.FOREIGN,
                          properties.mUpdate, properties.mDelete, properties.getColumns());
        }
        return key;
    }

    @Override
    protected Key appendElement(XPropertySet descriptor)
        throws java.sql.SQLException {
        Key key = null;
        System.out.println("sdbcx.KeyContainer.appendElement() 1");
        int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.TYPE);
        // XXX: For foreign keys, we check if the type between the foreign key and the primary key is the same.
        if (type == KeyType.FOREIGN) {
            checkKeyAppendValid(descriptor);
        }

        String name = getElementName(descriptor);
        if (createNewKey(descriptor, name)) {
            key = createNewElement(descriptor, name);
        }
        return key;
    }

    private void checkKeyAppendValid(XPropertySet descriptor)
        throws java.sql.SQLException {
        boolean failed = true;
        ColumnSuper col1 = null;
        ColumnSuper col2 = null;
        System.out.println("sdbcx.KeyContainer.appendElement() 2");
        Map<String, String> columns = new TreeMap<>();
        String table = KeyHelper.getKeyFromDescriptor(descriptor, columns);
        System.out.println("sdbcx.KeyContainer.appendElement() 3 Table: " + table + " ************** ");
        ColumnContainerBase<?> columns1 = mTable.getColumnsInternal();
        TableContainerSuper<?> tables = mTable.getConnection().getTablesInternal();
        System.out.println("sdbcx.KeyContainer.appendElement() 3");
        if (tables.hasByName(table)) {
            ColumnContainerBase<?> columns2 = tables.getElementByName(table).getColumnsInternal();
            for (String foreign : columns.keySet()) {
                System.out.println("sdbcx.KeyContainer.appendElement() 3");
                String column = columns.get(foreign);
                System.out.println("sdbcx.KeyContainer.appendElement() 4");
                if (column != null && columns1.hasByName(foreign) && columns2.hasByName(column)) {
                    System.out.println("sdbcx.KeyContainer.appendElement() 5");
                    col1 = columns1.getElementByName(foreign);
                    col2 = columns2.getElementByName(column);
                    System.out.println("sdbcx.KeyContainer.appendElement() 6");
                    if (col1.getTypeInternal() == col2.getTypeInternal()) {
                        System.out.println("sdbcx.KeyContainer.appendElement() 7");
                        failed = false;
                    }
                }
            }
        }
        if (failed) {
            System.out.println("sdbcx.KeyContainer.appendElement() 8");
            int resource = Resources.STR_LOG_FKEY_ADD_INVALID_COLUMN_TYPE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName(),
                                                                                   col1.getTypeNameInternal(),
                                                                                   col1.getName(),
                                                                                   col2.getTypeNameInternal(),
                                                                                   col2.getName());
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_ERROR_UNSPECIFIED.text());
        }
    }

    private boolean createNewKey(XPropertySet descriptor, String key)
        throws java.sql.SQLException {
        Provider provider = getConnection().getProvider();
        int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.TYPE);
        if (type == KeyType.PRIMARY && !provider.getConfigDDL().supportsAlterPrimaryKey()) {
            int resource = Resources.STR_LOG_PKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text());
        }
        if (type == KeyType.FOREIGN && !provider.getConfigDDL().supportsAlterForeignKey()) {
            int resource = Resources.STR_LOG_FKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text());
        }
        int res1, res2;
        if (type == KeyType.PRIMARY) {
            res1 = Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY;
            res2 = Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY_ERROR;
        } else {
            res1 = Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY;
            res2 = Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY_ERROR;
        }
        return createNewKey(provider, descriptor, key, res1, res2);
    }

    private boolean createNewKey(Provider provider, XPropertySet descriptor, String key, int res1, int res2)
        throws java.sql.SQLException {
        String query = null;
        ComposeRule rule = ComposeRule.InIndexDefinitions;
        String name = mTable.composeTableName(rule);
        try {
            NamedComponent table = mTable.getNamedComponents();
            query = ConstraintHelper.getCreateConstraintQuery(provider.getConfigDDL(),
                                                              provider.getNamedSupport(rule),
                                                              descriptor, table, key, isCaseSensitive());
            System.out.println("sdbcx.KeyContainer.createKey() Query: " + query);
            getLogger().logprb(LogLevel.INFO, res1, key, name, query);
            return DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, key, name, query);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (IllegalArgumentException e) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, key, name, query);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private Key createNewElement(XPropertySet descriptor, String oldname)
        throws java.sql.SQLException {
        try {
            // XXX: Find the name which the database gave the new key
            final int PK_NAME = 6;
            final int FK_NAME = 12;
            int index = PK_NAME;
            int update = 0;
            int delete = 0;
            TableSuper refTable = null;
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.TYPE);
            if (type == KeyType.FOREIGN) {
                index = FK_NAME;
                update = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.UPDATERULE);
                delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.DELETERULE);
                String tablename = DBTools.getDescriptorStringValue(descriptor, PropertyID.REFERENCEDTABLE);
                refTable = getConnection().getTablesInternal().getElementByName(tablename);
            }
            String newname = oldname;
            Provider provider = getConnection().getProvider();
            java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
            try (java.sql.ResultSet result = getKeyResultSet(metadata, type)) {
                while (result.next()) {
                    String name = result.getString(index);
                    // XXX: This name wasn't inserted yet so it must be the new one
                    if (!hasByName(name)) {
                        // XXX: Now that the key has been created we know its name and we need to update
                        // XXX: the descriptor name in order to be able to insert it into the key container.
                        descriptor.setPropertyValue(PropertyID.NAME.getName(), name);
                        newname = name;
                        break;
                    }
                }
            }
            ComposeRule rule = ComposeRule.InDataManipulation;
            String[] columns = ConstraintHelper.getKeyColumns(provider.getNamedSupport(rule),
                                                              descriptor, PropertyID.NAME, false);
            return new Key(mTable, refTable, isCaseSensitive(), newname, type, update, delete, columns);
        } catch (java.sql.SQLException | UnknownPropertyException | PropertyVetoException | WrappedTargetException e) {
            throw new java.sql.SQLException(e.getMessage(), StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private java.sql.ResultSet getKeyResultSet(java.sql.DatabaseMetaData metadata,
                                               int keytype)
        throws java.sql.SQLException {
        java.sql.ResultSet result = null;
        NamedComponent table = mTable.getNamedComponents();
        if (keytype == KeyType.FOREIGN) {
            result = metadata.getImportedKeys(table.getCatalog(), table.getSchema(), table.getTable());
        } else {
            result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable());
        }
        return result;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws java.sql.SQLException {
        String query = null;
        String table = null;
        Key key = getElementByIndex(index);
        final int type;
        if (key != null) {
            type = key.getTypeInternal();
        } else {
            type = KeyType.PRIMARY;
        }
        Provider provider = getConnection().getProvider();
        if (type == KeyType.PRIMARY && !provider.getConfigDDL().supportsAlterPrimaryKey()) {
            int resource = Resources.STR_LOG_PKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text());
        }
        if (type == KeyType.FOREIGN && !provider.getConfigDDL().supportsAlterForeignKey()) {
            int resource = Resources.STR_LOG_FKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text());
        }
        ComposeRule rule = ComposeRule.InTableDefinitions;
        NamedSupport support = provider.getNamedSupport(rule);
        try {
            table = ComponentHelper.composeTableName(support, mTable, isCaseSensitive());
            String contraint = support.enquoteIdentifier(name, isCaseSensitive());
            query = provider.getConfigDDL().getDropConstraintCommand(ParameterDDL.getDropConstraint(table, contraint),
                                                                    type);
            System.out.println("sdbcx.KeyContainer.removeDataBaseElement() Query: " + query);
            int resource = getRemoveKeyResource(type, false);
            table = ComponentHelper.composeTableName(support, mTable, false);
            getLogger().logprb(LogLevel.INFO, resource, name, table, query);
            if (!DBTools.executeSQLQuery(provider, query)) {
                System.out.println("sdbcx.KeyContainer.removeDataBaseElement() ERROR");
                // XXX: If we delete a primary key we must also delete the corresponding index.
                //if (type == KeyType.PRIMARY) {
                //    mTable.getIndexesInternal().removePrimaryKeyIndex();
                //} else if (type == KeyType.FOREIGN) {
                //    // XXX: If we delete a foreign key we must also delete the corresponding index.
                //    mTable.getIndexesInternal().removeForeignKeyIndex(name);
                //}
            }
        } catch (java.sql.SQLException e) {
            int resource = getRemoveKeyResource(type, true);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, resource,
                                                                                   key, table, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private int getRemoveKeyResource(int type, boolean error) {
        int resource = 0;
        if (type == KeyType.PRIMARY) {
            if (error) {
                resource = Resources.STR_LOG_KEYS_REMOVE_PKEY_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_KEYS_REMOVE_PKEY_QUERY;
            }
        } else if (type == KeyType.FOREIGN) {
            if (error) {
                resource = Resources.STR_LOG_KEYS_REMOVE_FKEY_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_KEYS_REMOVE_FKEY_QUERY;
            }
        }
        return resource;
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.KeyContainer.refreshInternal() *********************************");
        mTable.refreshKeys();
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new KeyDescriptor(isCaseSensitive());
    }

}
