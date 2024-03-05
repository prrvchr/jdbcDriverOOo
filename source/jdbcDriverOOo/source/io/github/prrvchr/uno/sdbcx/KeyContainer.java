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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBDefaultQuery;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;


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
        System.out.println("sdbcx.KeyContainer() 1");
        m_logger = new ConnectionLog(table.getLogger(), LoggerObjectType.KEYCONTAINER);
        m_keys = keys;
        m_table = table;
        System.out.println("sdbcx.KeyContainer() 2");
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
        System.out.println("sdbcx.KeyContainer.createElement() 1 Name: " + name);
        try {
            Key key = null;
            if (!name.isEmpty()) {
                if (m_keys.containsKey(name)) {
                    key = m_keys.get(name);
                }
                else {
                    System.out.println("sdbcx.KeyContainer.createElement() 2");
                    key = DBColumnHelper.readKey(getConnection().getProvider(), m_table, name, isCaseSensitive());
                    if (key != null) {
                        System.out.println("sdbcx.KeyContainer.createElement() 3");
                        m_keys.put(name, key);
                    }
                }
            }
            System.out.println("sdbcx.KeyContainer.createElement() 4");
            return key;
        }
        catch (ElementExistException e) {
            throw new SQLException(e);
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

    private boolean createKey(XPropertySet descriptor, String name)
            throws SQLException
    {
        try {
            List<Object> arguments = new ArrayList<Object>();
            ComposeRule rule = ComposeRule.InTableDefinitions;
            arguments.add(DBTools.buildName(getConnection().getProvider(), m_table, rule, isCaseSensitive()));
            arguments.add(String.join(", ", getKeyColumns(descriptor, PropertyIds.NAME, isCaseSensitive())));
            String command;
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            if (type == KeyType.PRIMARY) {
                command = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_PRIMARY_KEY;
            }
            else if (type == KeyType.FOREIGN) {
                command = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_FOREIGN_KEY;
                String referencedName = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
                System.out.println("sdbcx.KeyContainer.createKey() ReferencedTable: " + referencedName);
                arguments.add(DBTools.quoteTableName(getConnection().getProvider(), referencedName, rule, isCaseSensitive()));
                arguments.add(String.join(", ", getKeyColumns(descriptor, PropertyIds.RELATEDCOLUMN, isCaseSensitive())));
                int update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
                int delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
                arguments.add(getKeyRuleString(true, update));
                arguments.add(getKeyRuleString(false, delete));
            }
            else {
                throw new SQLException();
            }
            String query = MessageFormat.format(command, arguments.toArray(new Object[0]));
            String table = DBTools.buildName(getConnection().getProvider(), m_table, rule, false);
            System.out.println("sdbcx.KeyContainer.createKey() Query: " + query);
            return DBTools.executeDDLQuery(getConnection().getProvider(), query, getLogger(),
                                           this.getClass().getName(), "createKey",
                                           Resources.STR_LOG_KEYS_CREATE_KEY_QUERY, name, table);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_table);
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException | WrappedTargetException e) {
            throw new SQLException(e);
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
            List<String> columns = getKeyColumns(descriptor, PropertyIds.NAME, false);
            return new Key(m_table, isCaseSensitive(), newname, referencedName, type, update, delete, columns);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_table);
        }
        catch (IllegalArgumentException | UnknownPropertyException | IndexOutOfBoundsException |
               PropertyVetoException | WrappedTargetException | ElementExistException e1) {
            throw new SQLException(e1);
        }
    }

    private List<String> getKeyColumns(XPropertySet descriptor, PropertyIds name, boolean sensitive)
        throws java.sql.SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
        List<String> columns = new ArrayList<String>();
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        for (int i = 0; i < indexes.getCount(); i++) {
            XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
            String value = DBTools.getDescriptorStringValue(property, name);
            columns.add(DBTools.enquoteIdentifier(getConnection().getProvider(), value, sensitive));
        }
        return columns;
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

    private String getKeyRuleString(boolean isUpdate,
                                    int rule)
    {
        String keyRule = "";
        switch (rule) {
        case KeyRule.CASCADE:
            keyRule = isUpdate ? "ON UPDATE CASCADE" : "ON DELETE CASCADE";
            break;
        case KeyRule.RESTRICT:
            keyRule = isUpdate ? "ON UPDATE RESTRICT" : "ON DELETE RESTRICT";
            break;
        case KeyRule.SET_NULL:
            keyRule = isUpdate ? "ON UPDATE SET NULL" : "ON DELETE SET NULL";
            break;
        case KeyRule.SET_DEFAULT:
            keyRule = isUpdate ? "ON UPDATE SET DEFAULT" : "ON DELETE SET DEFAULT";
            break;
        }
        return keyRule;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        try {
            Key key = getElement(index);
            final int keyType;
            if (key != null) {
                keyType = key.m_Type;
            }
            else {
                keyType = KeyType.PRIMARY;
            }
            List<Object> arguments = new ArrayList<Object>();
            ComposeRule rule = ComposeRule.InTableDefinitions;
            arguments.add(DBTools.composeTableName(getConnection().getProvider(), m_table, rule, isCaseSensitive()));
            final String command;
            if (keyType == KeyType.PRIMARY) {
                command = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_PRIMARY_KEY;
            }
            else {
                command = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_CONSTRAINT;
                arguments.add(DBTools.enquoteIdentifier(getConnection().getProvider(), name, isCaseSensitive()));
            }
            String query = MessageFormat.format(command, arguments.toArray(new Object[0]));
            String table = DBTools.composeTableName(getConnection().getProvider(), m_table, rule, isCaseSensitive());
            System.out.println("sdbcx.KeyContainer.removeDataBaseElement() 1 Query: " + query);
            if (DBTools.executeDDLQuery(getConnection().getProvider(), query, getLogger(),
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
            throw UnoHelper.getSQLException(e, m_table);
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
        m_table.getIndexesInternal().renamePrimaryKeyIndex(oldname, newname);
    }


}
