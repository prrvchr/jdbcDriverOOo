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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBConstraintHelper;
import io.github.prrvchr.jdbcdriver.DBKeyHelper;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public final class KeyContainer
    extends Container<Key>
{
    private static final String m_service = KeyContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Keys",
                                                "com.sun.star.sdbcx.Container"};

    protected final TableSuper<?> m_table;
    private final ConnectionLog m_logger; 

    // The constructor method:
    public KeyContainer(TableSuper<?> table,
                        boolean sensitive,
                        List<String> keys)
        throws ElementExistException
    {
        super(m_service, m_services, table, sensitive, keys);
        m_logger = new ConnectionLog(table.getLogger(), LoggerObjectType.KEYCONTAINER);
        m_table = table;
    }

    protected ConnectionSuper getConnection()
    {
        return m_table.getConnection();
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    public void dispose()
    {
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_KEYS_DISPOSING);
        super.dispose();
    }

    @Override
    protected Key createElement(String name)
        throws SQLException
    {

        Key key = null;
        try {
            System.out.println("KeyContainer.createElement() 1 Name: " + name);
            if (!name.isEmpty()) {
                key = DBKeyHelper.readKey(getConnection().getProvider(), m_table, m_table.getNamedComponents(),
                                          name, ComposeRule.InDataManipulation, isCaseSensitive());
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return key;
    }

    @Override
    protected Key appendElement(XPropertySet descriptor)
        throws SQLException
    {
        Key key = null;
        DBTools.printDescriptor(descriptor);
        String name = getElementName(descriptor);
        if (createKey(descriptor, name)) {
            key = createElement(descriptor, name);
            //m_table.getIndexesInternal().refresh();
        }
        return key;
    }

    private boolean createKey(XPropertySet descriptor, String key)
            throws SQLException
    {
        String query = null;
        String name = null;
        ComposeRule rule = ComposeRule.InIndexDefinitions;
        DriverProvider provider = getConnection().getProvider();
        int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
        NamedComponents table = m_table.getNamedComponents();

        if (type == KeyType.PRIMARY && !provider.supportsAlterPrimaryKey()) {
            int resource = Resources.STR_LOG_PKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }
        if (type == KeyType.FOREIGN && !provider.supportsAlterForeignKey()) {
            int resource = Resources.STR_LOG_FKEY_ADD_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        try {
            name = DBTools.buildName(provider, table, rule);
            query = DBConstraintHelper.getCreateConstraintQuery(provider, descriptor, table, key, rule, isCaseSensitive());
            System.out.println("sdbcx.KeyContainer.createKey() Query: " + query);
            int resource = getCreateKeyResource(type, false);
            getLogger().logprb(LogLevel.INFO, resource, key, name, query);
            return DBTools.executeDDLQuery(provider, query);
        }
        catch (java.sql.SQLException e) {
            int resource = getCreateKeyResource(type, true);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException | WrappedTargetException e) {
            int resource = getCreateKeyResource(type, true);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, (Exception) e);
        }
    }

    private int getCreateKeyResource(int type, boolean error) {
        int resource = 0;
        if (type == KeyType.PRIMARY) {
            resource = error ? 
                        Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY_ERROR :
                        Resources.STR_LOG_KEYS_CREATE_PKEY_QUERY;
        }
        else {
            resource = error ? 
                        Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY_ERROR :
                        Resources.STR_LOG_KEYS_CREATE_FKEY_QUERY;
        }
        return resource;
    }

    private Key createElement(XPropertySet descriptor, String oldname)
        throws SQLException
    {
        try {
            // XXX: Find the name which the database gave the new key
            int index = 6;
            int update = 0;
            int delete = 0;
            String referencedName = "";
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            if (type == KeyType.FOREIGN) {
                index = 12;
                update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
                delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
                referencedName = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
            }
            String newname = oldname;
            DriverProvider provider = getConnection().getProvider();
            java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
            try (java.sql.ResultSet result = getKeyResultSet(metadata, type))
            {
                while (result.next()) {
                    String name = result.getString(index);
                    // XXX: This name wasn't inserted yet so it must be the new one
                    if (!hasByName(name)) {
                        // XXX: Now that the key has been created we know its name and we need to update
                        // XXX: the descriptor name in order to be able to insert it into the key container.
                        descriptor.setPropertyValue(PropertyIds.NAME.name, name);
                        newname = name;
                        break;
                    }
                }
            }
            List<String> columns = DBConstraintHelper.getKeyColumns(provider, descriptor, PropertyIds.NAME, false);
            return new Key(m_table, isCaseSensitive(), newname, referencedName, type, update, delete, columns);
        }
        catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (UnknownPropertyException | IndexOutOfBoundsException |
                PropertyVetoException | WrappedTargetException | ElementExistException e) {
             throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, (Exception) e);
        }
    }

    private java.sql.ResultSet getKeyResultSet(java.sql.DatabaseMetaData metadata,
                                               int keytype)
        throws java.sql.SQLException
    {
        java.sql.ResultSet result = null;
        NamedComponents table = m_table.getNamedComponents();
        if (keytype == KeyType.FOREIGN) {
            result = metadata.getImportedKeys(table.getCatalog(), table.getSchema(), table.getTable());
        }
        else {
            result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable());
        }
        return result;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        String query = null;
        String table = null;
        Key key = getElement(index);
        final int type;
        if (key != null) {
            type = key.m_Type;
        }
        else {
            type = KeyType.PRIMARY;
        }
            DriverProvider provider = getConnection().getProvider();
            if (type == KeyType.PRIMARY && !provider.supportsAlterPrimaryKey()) {
                int resource = Resources.STR_LOG_PKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            if (type == KeyType.FOREIGN && !provider.supportsAlterForeignKey()) {
                int resource = Resources.STR_LOG_FKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            ComposeRule rule = ComposeRule.InTableDefinitions;
        try {
            table = DBTools.composeTableName(provider, m_table, rule, false);
            String arg1 = DBTools.composeTableName(provider, m_table, rule, isCaseSensitive());
            String arg2 = DBTools.enquoteIdentifier(provider, name, isCaseSensitive());
            final String command = provider.getDropConstraintQuery(type);
            query = MessageFormat.format(command, arg1, arg2);
            System.out.println("sdbcx.KeyContainer.removeDataBaseElement() Query: " + query);
            int resource = getRemoveKeyResource(type, false);
            getLogger().logprb(LogLevel.INFO, resource, name, table, query);
            if (DBTools.executeDDLQuery(provider, query))
            {
                // XXX: If we delete a primary key we must also delete the corresponding index.
                if (type == KeyType.PRIMARY) {
                    m_table.getIndexesInternal().removePrimaryKeyIndex();
                }
                // XXX: If we delete a foreign key we must also delete the corresponding index.
                else if (type == KeyType.FOREIGN) {
                    m_table.getIndexesInternal().removeForeignKeyIndex(name);
                }
            }
        }
        catch (java.sql.SQLException e) {
            int resource = getRemoveKeyResource(type, true);
            String msg = getLogger().getStringResource(resource, key, table, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private int getRemoveKeyResource(int type, boolean error) {
        int resource = 0;
        if (type == KeyType.PRIMARY) {
            resource = error ?
                    Resources.STR_LOG_KEYS_REMOVE_PKEY_QUERY_ERROR :
                    Resources.STR_LOG_KEYS_REMOVE_PKEY_QUERY;
        }
        else if (type == KeyType.FOREIGN) {
            resource = error ?
                    Resources.STR_LOG_KEYS_REMOVE_FKEY_QUERY_ERROR :
                    Resources.STR_LOG_KEYS_REMOVE_FKEY_QUERY;
        }
        return resource;
    }

    @Override
    protected void refreshInternal() {
        m_table.refreshKeys();
    }

    @Override
    protected XPropertySet createDescriptor()
    {
        return new KeyDescriptor(isCaseSensitive());
    }

    protected void renameKeyColumn(int type, String oldname, String newname)
        throws SQLException
    {
        String name = null;
        Iterator<Key> keys = getActiveElements();
        while (keys.hasNext()) {
            Key key = keys.next();
            if (key.m_Type != type) {
                continue;
            }
            KeyColumnContainer columns = key.getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameKeyColumn(oldname, newname);
                name = key.m_Name;
                break;
            }
        }
        if (name != null) {
            m_table.getIndexesInternal().renameIndexColumn(oldname, newname);
        }
    }

    protected void renameForeignKeyColumn(List<String> filter,
                                          String referenced,
                                          String oldname,
                                          String newname)
        throws SQLException
    {
        Iterator<Key> keys = getActiveElements();
        while (keys.hasNext()) {
            Key key = keys.next();
            if (key.m_Type != KeyType.FOREIGN || !key.m_ReferencedTable.equals(referenced)) {
                continue;
            }
            Iterator<KeyColumn> columns = key.getColumnsInternal().getActiveElements(filter);
            while (columns.hasNext()) {
                columns.next().setRelatedColumn(oldname, newname);
            }
        }
    }

}
