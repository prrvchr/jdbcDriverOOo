/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.ComposeRule;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class KeyContainer
    extends Container
{

    protected final TableBase m_table;
    private Map<String, Key> m_keys;

    // The constructor method:
    public KeyContainer(TableBase table,
                        Object lock,
                        boolean sensitive,
                        Map<String, Key> keys)
        throws ElementExistException
    {
        super(lock, sensitive, Arrays.asList(keys.keySet().toArray(new String[keys.size()])));
        System.out.println("sdbcx.KeyContainer() 1");
        for (Map.Entry<String, Key> entry : keys.entrySet()) {
            System.out.print(entry.getKey() + " => " + entry.getValue().m_ReferencedTable);
            XIndexAccess cols = UnoRuntime.queryInterface(XIndexAccess.class, entry.getValue().getColumns());
            try {
                System.out.print("" + cols.getCount() + " columns:");
                for (int i = 0; i < cols.getCount(); i++) {
                    XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cols.getByIndex(i));
                    System.out.print(" " + AnyConverter.toString(properties.getPropertyValue(PropertyIds.NAME.name)));
                }
                System.out.println("");
            }
            catch (WrappedTargetException | IllegalArgumentException | IndexOutOfBoundsException | UnknownPropertyException e) {
                e.printStackTrace();
            }
            catch (java.lang.IndexOutOfBoundsException | java.lang.IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        m_keys = keys;
        m_table = table;
        System.out.println("sdbcx.KeyContainer() 2");
    }

    @Override
    protected XPropertySet _createElement(String name)
        throws SQLException
    {
        System.out.println("sdbcx.KeyContainer._createElement() 1 Name: " + name);
        Key key = null;
        if (!name.isEmpty()) {
            System.out.println("sdbcx.KeyContainer._createElement() 2");
            key = m_keys.get(name);
        }
        if (key == null) { // we have a primary key with a system name
            // FIXME: so why was this exactly the same?
            System.out.println("sdbcx.KeyContainer._createElement() 3 ***********************************");
            key = m_keys.get(name);
        }
        System.out.println("sdbcx.KeyContainer._createElement() 4");
        return key;
    }
    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException,
               ElementExistException
    {
        if (_getConnection() == null) {
            return null;
        }
        try {
            int keyType = AnyConverter.toInt(descriptor.getPropertyValue(PropertyIds.TYPE.name));
            String keyTypeString;
            if (keyType == KeyType.PRIMARY) {
                keyTypeString = "PRIMARY KEY";
            }
            else if (keyType == KeyType.FOREIGN) {
                keyTypeString = "FOREIGN KEY";
            }
            else {
                throw new SQLException();
            }

            String referencedName = "";
            int updateRule = 0;
            int deleteRule = 0;
            if (keyType == KeyType.FOREIGN) {
                referencedName = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.REFERENCEDTABLE.name));
                System.out.println("sdbcx.KeyContainer._appendElement() ReferencedTable: " + referencedName);
                updateRule = AnyConverter.toInt(descriptor.getPropertyValue(PropertyIds.UPDATERULE.name));
                deleteRule = AnyConverter.toInt(descriptor.getPropertyValue(PropertyIds.DELETERULE.name));
            }
            
            java.sql.DatabaseMetaData metadata = _getConnection().getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            String tableName = DataBaseTools.doComposeTableName(_getConnection(), m_table.m_CatalogName, m_table.m_SchemaName,m_table.getName(), isCaseSensitive(), ComposeRule.InTableDefinitions);

            List<String> cols = new ArrayList<String>();
            XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
            for (int i = 0; i < columns.getCount(); i++) {
                XPropertySet columnProperties = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
                cols.add(DataBaseTools.quoteName(quote, AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.NAME.name))));
            }
            String sql = String.format("ALTER TABLE %s ADD %s (%s)", tableName, keyTypeString, String.join(",", cols));
            if (keyType == KeyType.FOREIGN) {
                String quotedTableName = DataBaseTools.quoteTableName(_getConnection(), referencedName, ComposeRule.InTableDefinitions);
                cols = new ArrayList<String>();
                for (int i = 0; i < columns.getCount(); i++) {
                    XPropertySet columnProperties = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
                    cols.add(DataBaseTools.quoteName(quote, AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.RELATEDCOLUMN.name))));
                }
                sql += String.format(" REFERENCES %s (%s) %s %s", quotedTableName, String.join(",", cols),
                                     getKeyRuleString(true, updateRule), getKeyRuleString(false, deleteRule));
            }
            java.sql.Statement statement = _getConnection().getProvider().getConnection().createStatement();
            System.out.println("sdbcx.KeyContainer._appendElement() SQL: " + sql);
            statement.execute(sql);
            statement.close();
            // find the name which the database gave the new key
            String newname = name;
            java.sql.ResultSet result = null;
            final int column;
            if (keyType == KeyType.FOREIGN) {
                result = metadata.getImportedKeys(m_table.getCatalogName(), m_table.getSchemaName(), m_table.getName());
                column = 12;
            }
            else {
                result = metadata.getPrimaryKeys(m_table.getCatalogName(), m_table.getSchemaName(), m_table.getName());
                column = 6;
            }
            while (result.next()) {
                String oldname = result.getString(column);
                if (!hasByName(oldname)) { // this name wasn't inserted yet so it must be the new one
                    descriptor.setPropertyValue(PropertyIds.NAME.name, oldname);
                    newname = oldname;
                    break;
                }
            }
            result.close(); 
            m_keys.put(newname, new Key(m_table, isCaseSensitive(), newname, referencedName, keyType, updateRule, deleteRule, new ArrayList<String>()));
            return _createElement(newname);
        }
        catch (WrappedTargetException | UnknownPropertyException | IndexOutOfBoundsException | PropertyVetoException e) {
            throw UnoHelper.getSQLException(e, m_table);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_table);
        }
    }

    protected String getKeyRuleString(boolean isUpdate,
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
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        ConnectionSuper connection = m_table.getConnection();
        if (connection == null) {
            return;
        }
        try {
            XPropertySet key = (XPropertySet) AnyConverter.toObject(XPropertySet.class, _getElement(index));
            String tableName = DataBaseTools.composeTableName(connection, m_table, ComposeRule.InTableDefinitions, false, false, true);
            final int keyType;
            if (key != null) {
                keyType = AnyConverter.toInt(key.getPropertyValue(PropertyIds.TYPE.name));
            }
            else {
                keyType = KeyType.PRIMARY;
            }
            final String sql;
            if (keyType == KeyType.PRIMARY) {
                sql = String.format("ALTER TABLE %s DROP PRIMARY KEY", tableName);
            }
            else {
                sql = String.format("ALTER TABLE %s %s %s", tableName, getDropForeignKey(), getForeignKeyName(connection, name));
            }
            java.sql.Statement statement = connection.getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (WrappedTargetException | UnknownPropertyException e) {
            UnoHelper.getSQLException(e, m_table);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_table);
        }
    }
    
    private String getDropForeignKey()
    {
        return "DROP CONSTRAINT";
    }
    private String getForeignKeyName(ConnectionSuper connection,
                                     String name)
        throws SQLException
    {
        return DataBaseTools.quoteName(connection.getMetaData().getIdentifierQuoteString(), name);
    }

    @Override
    protected void _refresh() {
        //throw new NotImplementedException("");
    }
    

    public ConnectionSuper _getConnection()
    {
        return m_table.getConnection();
    }

    @Override
    protected XPropertySet _createDescriptor()
    {
        return new KeyDescriptor(isCaseSensitive());
    }


    /*    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public KeyContainer(TableBase table)
    {
        super(m_name, m_services, table.m_Connection);
        System.out.println("sdbcx.KeyContainer()");
        m_table = table;
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public KeyContainer(Connection connection,
                        TableBase table1,
                        schemacrawler.schema.Table table)
    {
        super(m_name, m_services, connection);
        m_table = table1;
        try {
            if (table.hasPrimaryKey()) {
                for (PrimaryKey key : table.getAlternateKeys()) {
                    m_Elements.add(new Key(m_Connection, table1, key));
                }
            }
            if (table.hasForeignKeys()) {
                for (ForeignKey key : table.getForeignKeys()) {
                    m_Elements.add(new Key(m_Connection, table1, key));
                }
            }
            System.out.println("sdbcx.KeyContainer.refresh() Number of Key: " + getCount());
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }

    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public KeyContainer(Connection connection,
                        TableBase table)
    {
        super(m_name, m_services, connection);
        m_table = table;
        try {
            Map<String, Key> keys = new LinkedHashMap<String, Key>();
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getPrimaryKeys(null, table.m_SchemaName, table.m_Name);
            while (result.next()) {
                String column = result.getString(4);
                int position = result.getShort(5);
                String name = result.getString(6);
                if (keys.containsKey(name)) {
                    System.out.println("sdbcx.KeyContainer.refresh() Add Keycolumn to Key: " + column + " - Name: " + name);
                    keys.get(name)._addColumn(new KeyColumn(m_Connection, table, column, position));
                }
                else {
                    System.out.println("sdbcx.KeyContainer.refresh() Create New Key: " + column + " - Name: " + name);
                    Key key = new Key(m_Connection, table, name, column, position);
                    keys.put(name, key);
                    m_Elements.add(key);
                }
            }
            System.out.println("sdbcx.KeyContainer.refresh() Number of Key: " + getCount());
            result.close();
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public KeyContainer(Connection connection,
                        XIndexAccess keys,
                        TableBase table)
    {
        super(m_name, m_services, connection);
        m_table = table;
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, keys)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                Key key = new Key(m_Connection, table, descriptor, name);
                m_Elements.add(key);
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("sdbcx.KeyContainer(): " + getCount());
    }*/


}
