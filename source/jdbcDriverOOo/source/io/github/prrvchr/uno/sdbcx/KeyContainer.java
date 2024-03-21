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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBConstraintHelper;
import io.github.prrvchr.jdbcdriver.DBTools;
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
    private Map<String, Key> m_keys;

    // The constructor method:
    public KeyContainer(TableSuper<?> table,
                        boolean sensitive,
                        Map<String, Key> keys)
        throws ElementExistException
    {
        super(m_service, m_services, table, sensitive, Arrays.asList(keys.keySet().toArray(new String[keys.size()])));
        m_logger = new ConnectionLog(table.getLogger(), LoggerObjectType.KEYCONTAINER);
        m_keys = keys;
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
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_KEYS_DISPOSING);
        super.dispose();
    }

    @Override
    protected Key createElement(String name)
        throws SQLException
    {
        try {
            Key key = null;
            if (!name.isEmpty()) {
                if (m_keys.containsKey(name)) {
                    key = m_keys.get(name);
                }
                else {
                    key = DBColumnHelper.readKey(getConnection().getProvider(), m_table, m_table.getCatalog(),
                                                 m_table.getSchema(), m_table.getName(), name, isCaseSensitive());
                    if (key != null) {
                        m_keys.put(name, key);
                    }
                }
            }
            return key;
        }
        catch (ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    protected Key appendElement(XPropertySet descriptor)
        throws SQLException
    {
        Key key = null;
        String name = getElementName(descriptor);
        if (createKey(descriptor, name)) {
            key = createElement(descriptor, name);
            m_table.getIndexesInternal().refresh();
        }
        return key;
    }

    private boolean createKey(XPropertySet descriptor, String key)
            throws SQLException
    {
        try {
            DriverProvider provider = getConnection().getProvider();
            if (!provider.supportsAlterPrimaryKey()) {
                int resource = Resources.STR_LOG_KEY_ADD_UNSUPPORTED_FEATURE_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            String catalog = m_table.getCatalogName();
            String schema = m_table.getSchemaName();
            String name = m_table.getName();

            ComposeRule rule = ComposeRule.InTableDefinitions;
            String query = DBConstraintHelper.getCreateConstraintQuery(provider, descriptor, catalog, schema,
                                                                       name, key, rule, isCaseSensitive());
            String table = DBTools.buildName(provider, catalog, schema, name, rule, false);
            System.out.println("sdbcx.KeyContainer.createKey() Query: " + query);
            return DBTools.executeDDLQuery(provider, getLogger(), query,
                                           this.getClass().getName(), "createKey",
                                           Resources.STR_LOG_KEYS_CREATE_KEY_QUERY, key, table);
        }
        catch (java.sql.SQLException | IllegalArgumentException | IndexOutOfBoundsException | WrappedTargetException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    private Key createElement(XPropertySet descriptor, String oldname)
        throws SQLException
    {
        try {
            // XXX: Find the name which the database gave the new key
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            int index = 6;
            int update = 0;
            int delete = 0;
            String referencedName = "";
            if (type == KeyType.FOREIGN) {
                index = 12;
                update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
                delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
                referencedName = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
            }
            String newname = oldname;
            try (java.sql.ResultSet result = getKeyResultSet(type))
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
            List<String> columns = DBConstraintHelper.getKeyColumns(getConnection().getProvider(), descriptor, PropertyIds.NAME, false);
            return new Key(m_table, isCaseSensitive(), newname, referencedName, type, update, delete, columns);
        }
        catch (java.sql.SQLException | IllegalArgumentException |
               UnknownPropertyException | IndexOutOfBoundsException |
               PropertyVetoException | WrappedTargetException | ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    private java.sql.ResultSet getKeyResultSet(int keytype)
        throws java.sql.SQLException
    {
        java.sql.ResultSet result = null;
        java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
        if (keytype == KeyType.FOREIGN) {
            result = metadata.getImportedKeys(m_table.getCatalog(), m_table.getSchema(), m_table.getName());
        }
        else {
            result = metadata.getPrimaryKeys(m_table.getCatalog(), m_table.getSchema(), m_table.getName());
        }
        return result;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        try {
            DriverProvider provider = getConnection().getProvider();
            if (!provider.supportsAlterPrimaryKey()) {
                int resource = Resources.STR_LOG_KEY_REMOVE_UNSUPPORTED_FEATURE_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, m_table.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            Key key = getElement(index);
            final int keyType;
            if (key != null) {
                keyType = key.m_Type;
            }
            else {
                keyType = KeyType.PRIMARY;
            }
            ComposeRule rule = ComposeRule.InTableDefinitions;
            String arg1 = DBTools.composeTableName(provider, m_table, rule, isCaseSensitive());
            String arg2 = DBTools.enquoteIdentifier(provider, name, isCaseSensitive());
            final String command = provider.getDropConstraintQuery(keyType);
            String query = MessageFormat.format(command, arg1, arg2);
            String table = DBTools.composeTableName(provider, m_table, rule, isCaseSensitive());
            System.out.println("sdbcx.KeyContainer.removeDataBaseElement() Query: " + query);
            if (DBTools.executeDDLQuery(provider, getLogger(), query,
                                        this.getClass().getName(), "removeDataBaseElement",
                                        Resources.STR_LOG_KEYS_REMOVE_KEY_QUERY, name, table))
            {
                // XXX: If we delete a primary key we must also delete the corresponding index.
                System.out.println("sdbcx.KeyContainer.removeDataBaseElement() 2");
                if (keyType == KeyType.PRIMARY) {
                    System.out.println("sdbcx.KeyContainer.removeDataBaseElement() 3 Name: " + name);
                    m_table.getIndexesInternal().removePrimaryKeyIndex();
                    System.out.println("sdbcx.KeyContainer.removeDataBaseElement() 4");
                }
                // FIXME: What about foreign keys!!!
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.KeyContainer.refreshInternal() *********************************");
        m_table.refreshKeys();
    }

    @Override
    protected XPropertySet createDescriptor()
    {
        return new KeyDescriptor(isCaseSensitive());
    }

    protected void renameKeyColumn(String oldname, String newname)
        throws SQLException
    {
        for (String name : getElementNames()) {
            KeyColumnContainer columns = getElement(name).getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameKeyColumn(oldname, newname);
                break;
            }
        }
        m_table.getIndexesInternal().renamePrimaryKeyIndexColumn(oldname, newname);
    }

}
