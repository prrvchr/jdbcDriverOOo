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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ConstraintHelper;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.KeyHelper;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public final class KeyContainer
    extends ContainerSuper<Key> {
    private static final String SERVICE = KeyContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Keys",
                                              "com.sun.star.sdbcx.Container"};

    protected final TableSuper mTable;
    private final ConnectionLog mLogger; 

    // The constructor method:
    public KeyContainer(TableSuper table,
                        boolean sensitive,
                        List<String> keys)
        throws ElementExistException {
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

    @Override
    protected Key createElement(String name)
        throws SQLException {

        Key key = null;
        try {
            System.out.println("KeyContainer.createElement() 1 Name: " + name);
            if (!name.isEmpty()) {
                key = KeyHelper.readKey(getConnection().getProvider(), mTable, mTable.getNamedComponents(),
                                          name, ComposeRule.InDataManipulation, isCaseSensitive());
            }
        } catch (java.sql.SQLException | ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return key;
    }

    @Override
    protected Key appendElement(XPropertySet descriptor)
        throws SQLException {
        Key key = null;
        try {
            System.out.println("sdbcx.KeyContainer.appendElement() 1");
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            // XXX: For foreign keys, we check if the type between the foreign key and the primary key is the same.
            if (type == KeyType.FOREIGN) {
                checkKeyAppendValid(descriptor);
            }

            String name = getElementName(descriptor);
            if (createNewKey(descriptor, name)) {
                key = createNewElement(descriptor, name);
            }
        } catch (WrappedTargetException | NoSuchElementException e) {
            int resource = Resources.STR_LOG_FKEY_ADD_UNSPECIFIED_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_INVALID_SQL_DATA_TYPE.text(), 0, e);
        }
        return key;
    }

    private void checkKeyAppendValid(XPropertySet descriptor)
        throws WrappedTargetException, NoSuchElementException, SQLException {
        boolean failed = false;
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
            ColumnContainerBase<?> columns2 = tables.getElement(table).getColumnsInternal();
            for (String foreign : columns.keySet()) {
                System.out.println("sdbcx.KeyContainer.appendElement() 3");
                String column = columns.get(foreign);
                System.out.println("sdbcx.KeyContainer.appendElement() 4");
                if (column != null && columns1.hasByName(foreign) && columns2.hasByName(column)) {
                    System.out.println("sdbcx.KeyContainer.appendElement() 5");
                    col1 = columns1.getElement(foreign);
                    col2 = columns2.getElement(column);
                    System.out.println("sdbcx.KeyContainer.appendElement() 6");
                    if (col1.mType != col2.mType) {
                        System.out.println("sdbcx.KeyContainer.appendElement() 7");
                        failed = true;
                    }
                }
            }
        }
        if (failed) {
            System.out.println("sdbcx.KeyContainer.appendElement() 8");
            int resource = Resources.STR_LOG_FKEY_ADD_INVALID_COLUMN_TYPE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName(),
                                                                                   col1.mTypeName, col1.getName(),
                                                                                   col2.mTypeName, col2.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_ERROR_UNSPECIFIED.text(), 0, Any.VOID);
        }
    }

    private boolean createNewKey(XPropertySet descriptor, String key)
            throws SQLException {
        String query = null;
        String name = null;
        ComposeRule rule = ComposeRule.InIndexDefinitions;
        Provider provider = getConnection().getProvider();
        int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
        NamedComponents table = mTable.getNamedComponents();

        if (type == KeyType.PRIMARY && !provider.getConfigDDL().supportsAlterPrimaryKey()) {
            int resource = Resources.STR_LOG_PKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }
        if (type == KeyType.FOREIGN && !provider.getConfigDDL().supportsAlterForeignKey()) {
            int resource = Resources.STR_LOG_FKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        try {
            name = DBTools.buildName(provider, table, rule);
            query = ConstraintHelper.getCreateConstraintQuery(provider, descriptor, table,
                                                              key, rule, isCaseSensitive());
            System.out.println("sdbcx.KeyContainer.createKey() Query: " + query);
            int resource = getCreateKeyResource(type, false);
            getLogger().logprb(LogLevel.INFO, resource, key, name, query);
            return DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            int resource = getCreateKeyResource(type, true);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, key, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (IllegalArgumentException e) {
            int resource = getCreateKeyResource(type, true);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, key, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private int getCreateKeyResource(int type, boolean error) {
        int resource = 0;
        if (type == KeyType.PRIMARY) {
            if (error) {
                resource = Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY;
            }
        } else {
            if (error) {
                resource = Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY;
            }
        }
        return resource;
    }

    private Key createNewElement(XPropertySet descriptor, String oldname)
        throws SQLException {
        try {
            // XXX: Find the name which the database gave the new key
            final int PK_NAME = 6;
            final int FK_NAME = 12;
            int index = PK_NAME;
            int update = 0;
            int delete = 0;
            String referencedName = "";
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            if (type == KeyType.FOREIGN) {
                index = FK_NAME;
                update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
                delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
                referencedName = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
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
                        descriptor.setPropertyValue(PropertyIds.NAME.getName(), name);
                        newname = name;
                        break;
                    }
                }
            }
            List<String> columns = ConstraintHelper.getKeyColumns(provider, descriptor, PropertyIds.NAME, false);
            return new Key(mTable, isCaseSensitive(), newname, referencedName, type, update, delete, columns);
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (UnknownPropertyException | PropertyVetoException | WrappedTargetException | ElementExistException e) {
            throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(),
                                                                                             0, (Exception) e);
        }
    }

    private java.sql.ResultSet getKeyResultSet(java.sql.DatabaseMetaData metadata,
                                               int keytype)
        throws java.sql.SQLException {
        java.sql.ResultSet result = null;
        NamedComponents table = mTable.getNamedComponents();
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
        throws SQLException {
        String query = null;
        String table = null;
        Key key = getElement(index);
        final int type;
        if (key != null) {
            type = key.mType;
        } else {
            type = KeyType.PRIMARY;
        }
        Provider provider = getConnection().getProvider();
        if (type == KeyType.PRIMARY && !provider.getConfigDDL().supportsAlterPrimaryKey()) {
            int resource = Resources.STR_LOG_PKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }
        if (type == KeyType.FOREIGN && !provider.getConfigDDL().supportsAlterForeignKey()) {
            int resource = Resources.STR_LOG_FKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, mTable.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }
        ComposeRule rule = ComposeRule.InTableDefinitions;
        try {
            table = DBTools.composeTableName(provider, mTable, rule, isCaseSensitive());
            String contraint = provider.enquoteIdentifier(name, isCaseSensitive());
            query = provider.getConfigDDL().getDropConstraintCommand(ParameterDDL.getDropConstraint(table, contraint),
                                                                    type);
            System.out.println("sdbcx.KeyContainer.removeDataBaseElement() Query: " + query);
            int resource = getRemoveKeyResource(type, false);
            table = DBTools.composeTableName(provider, mTable, rule, false);
            getLogger().logprb(LogLevel.INFO, resource, name, table, query);
            if (DBTools.executeSQLQuery(provider, query)) {
                // XXX: If we delete a primary key we must also delete the corresponding index.
                if (type == KeyType.PRIMARY) {
                    mTable.getIndexesInternal().removePrimaryKeyIndex();
                } else if (type == KeyType.FOREIGN) {
                    // XXX: If we delete a foreign key we must also delete the corresponding index.
                    mTable.getIndexesInternal().removeForeignKeyIndex(name);
                }
            }
        } catch (java.sql.SQLException e) {
            int resource = getRemoveKeyResource(type, true);
            String msg = getLogger().getStringResource(resource, key, table, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
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
        mTable.refreshKeys();
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new KeyDescriptor(isCaseSensitive());
    }

    protected void renameKeyColumn(int type, String oldname, String newname)
        throws SQLException {
        String name = null;
        Iterator<Key> keys = getActiveElements();
        while (keys.hasNext()) {
            Key key = keys.next();
            if (key.mType != type) {
                continue;
            }
            KeyColumnContainer columns = key.getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameKeyColumn(oldname, newname);
                name = key.getName();
                break;
            }
        }
        if (name != null) {
            mTable.getIndexesInternal().renameIndexColumn(oldname, newname);
        }
    }

    protected void renameForeignKeyColumn(List<String> filter,
                                          String referenced,
                                          String oldname,
                                          String newname)
        throws SQLException {
        Iterator<Key> keys = getActiveElements();
        while (keys.hasNext()) {
            Key key = keys.next();
            if (key.mType != KeyType.FOREIGN || !key.mReferencedTable.equals(referenced)) {
                continue;
            }
            Iterator<KeyColumn> columns = key.getColumnsInternal().getActiveElements(filter);
            while (columns.hasNext()) {
                columns.next().setRelatedColumn(oldname, newname);
            }
        }
    }

}
