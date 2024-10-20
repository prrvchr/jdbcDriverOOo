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
package io.github.prrvchr.jdbcdriver.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class KeyHelper
{

    public static String getKeyFromDescriptor(DriverProvider provider,
                                              XPropertySet descriptor,
                                              Map<String, String> ref)
    throws WrappedTargetException, NoSuchElementException
    {
        String table = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        if (supplier != null) {
            XNameAccess columns = UnoRuntime.queryInterface(XNameAccess.class, supplier.getColumns());
            for (String foreign : columns.getElementNames()) {
                if (columns.hasByName(foreign)) {
                    XPropertySet column = UnoRuntime.queryInterface(XPropertySet.class, columns.getByName(foreign));
                    if (column != null) {
                        String primay = DBTools.getDescriptorStringValue(column, PropertyIds.RELATEDCOLUMN);
                        ref.put(foreign, primay);
                    }
                }
            }
        }
        return table;
    }


    public static List<String> refreshKeys(DriverProvider provider,
                                           NamedComponents table)
        throws java.sql.SQLException,
               ElementExistException
    {
        List<String> keys = new ArrayList<>();
        refreshPrimaryKeys(keys, provider, table);
        refreshForeignKeys(keys, provider, table);
        return keys;
    }

    public static Key readKey(DriverProvider provider,
                              TableSuper table,
                              NamedComponents component,
                              String keyname,
                              ComposeRule rule,
                              boolean sensitive)
        throws java.sql.SQLException,
               ElementExistException
    {
        Key key = readPrimaryKey(provider, table, component, keyname, sensitive);
        if (key == null) {
            key = readForeignKey(provider, table, component, keyname, rule, sensitive);
        }
        return key;
    }

    public static String getKeyName(String name,
                                    String table,
                                    int type)
    {
        return getKeyName(name, getKeyPrefix(type), table);
    }


    public static boolean isForeignKeyColumn(DriverProvider provider,
                                             NamedComponents table,
                                             String column)
        throws SQLException
    {
        return false;
    }


    public static Map<String, List<String>> getExportedTablesColumns(DriverProvider provider,
                                                                     NamedComponents table,
                                                                     String column,
                                                                     ComposeRule rule)
        throws java.sql.SQLException
    {
        // XXX: Here we need to retrieve all tables having this table / column as foreign key.
        Map<String, List<String>> tables = new TreeMap<String, List<String>>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getExportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                String value = result.getString(4);
                if (!result.wasNull() && column.equals(value)) {
                    NamedComponents component = new NamedComponents();
                    value = result.getString(5);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(6);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(7));
                    String name = DBTools.buildName(provider, component, rule);
                    value = result.getString(8);
                    System.out.println("DBKeyHelper.getExportedTables() 1 Table " + name + " - Column: " + value);
                    if (!tables.containsKey(name)) {
                        tables.put(name, List.of(value));
                    }
                    else {
                        tables.get(name).add(value);
                    }
                }
            }
        }
        return tables;
    }

    public static List<String> getExportedTables(DriverProvider provider,
                                                 NamedComponents table,
                                                 ComposeRule rule)
        throws java.sql.SQLException
    {
     // XXX: Here we need to retrieve all tables having this table as foreign key.
        List<String> tables = new ArrayList<>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getExportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                String value = result.getString(4);
                if (!result.wasNull()) {
                    NamedComponents component = new NamedComponents();
                    value = result.getString(5);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(6);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(7));
                    String name = DBTools.buildName(provider, component, rule);
                    System.out.println("DBKeyHelper.getExportedTables() 1 Table " + name);
                    if (!tables.contains(name)) {
                        tables.add(name);
                    }
                }
            }
        }
        return tables;
    }


    // XXX: Private helper function
    private static class ForeignKeyProperties
    {
        ArrayList<String> columns = new ArrayList<>();
        String table;
        int update;
        int delete;

        ForeignKeyProperties(DriverProvider provider,
                             NamedComponents table,
                             ComposeRule rule,
                             int update,
                             int delete)
            throws java.sql.SQLException
        {
            this.table = DBTools.buildName(provider, table, rule);
            this.update = update;
            this.delete = delete;
        }
    }

    private static void refreshPrimaryKeys(List<String> keys,
                                           DriverProvider provider,
                                           NamedComponents table)
        throws java.sql.SQLException
    {
        int type = KeyType.PRIMARY;
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable())) 
        {
            // XXX: There can only be one primary key per table.
            if (result.next()) {
                String pk = result.getString(6);
                System.out.println("DBKeyHelper.refreshPrimaryKeys() KeyName: '" + pk + "'");
                keys.add(getKeyName(pk, table.getTable(), type));
            }
        }
    }

    private static void refreshForeignKeys(List<String> keys,
                                           DriverProvider provider,
                                           NamedComponents table)
        throws java.sql.SQLException
    {
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getImportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) 
        {
            String previous = "";
            while (result.next()) {
                String name = result.getString(12);
                if (!result.wasNull() && !name.equals(previous)) {
                    keys.add(name);
                    previous = name;
                }
            }
        }
    }

    private static Key readPrimaryKey(DriverProvider provider,
                                      TableSuper table,
                                      NamedComponents component,
                                      String keyname,
                                      boolean sensitive)
        throws java.sql.SQLException,
               ElementExistException
    {
        Key key = null;
        ArrayList<String> columns = new ArrayList<>();
        String name = null;
        boolean fetched = false;
        int type = KeyType.PRIMARY;
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(component.getCatalog(), component.getSchema(), component.getTable()))
        {
            while (result.next()) {
                String column = result.getString(4);
                columns.add(column);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(6);
                    name = getKeyName(pk, component.getTable(), type);
                }
            }
        }
        if (name != null && name.equals(keyname)) {
            key = new Key(table, sensitive, keyname, "", type, 0, 0, columns);
        }
        return key;
    }

    private static Key readForeignKey(DriverProvider provider,
                                      TableSuper table,
                                      NamedComponents component,
                                      String keyname,
                                      ComposeRule rule,
                                      boolean sensitive)
        throws java.sql.SQLException,
               ElementExistException
    {
        Key key = null;
        String oldname = "";
        int type = KeyType.FOREIGN;
        ForeignKeyProperties properties = null;
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        try (java.sql.ResultSet result = metadata.getImportedKeys(component.getCatalog(), component.getSchema(), component.getTable()))
        {
            while (result.next()) {
                NamedComponents fk = new NamedComponents();
                String value = result.getString(1);
                if (!result.wasNull()) {
                    fk.setCatalog(value);
                }
                value = result.getString(2);
                if (!result.wasNull()) {
                    fk.setSchema(value);
                }
                fk.setTable(result.getString(3));

                String column = result.getString(8);
                int update = result.getInt(10);
                int delete = result.getInt(11);
                String name = result.getString(12);
                
                if (!result.wasNull() && !name.isEmpty()) {
                    if (!oldname.equals(name)) {
                        if (properties != null && oldname.equals(keyname)) {
                            break;
                        }
                        properties = new ForeignKeyProperties(provider, fk, rule, update, delete);
                        properties.columns.add(column);
                        oldname = name;
                    }
                    else {
                        if (properties != null) {
                            properties.columns.add(column);
                        }
                    }
                }
            }
        }
        if (properties != null && oldname.equals(keyname)) {
            key = new Key(table, sensitive, oldname, properties.table, type,
                          properties.update, properties.delete, properties.columns);
        }
        return key;
    }

    private static String getKeyPrefix(int type)
    {
        String prefix = null;
        switch (type) {
        case KeyType.PRIMARY:
            prefix = "PK_";
            break;
        case KeyType.FOREIGN:
            prefix = "FK_";
            break;
        case KeyType.UNIQUE:
            prefix = "";
            break;
        }
        return prefix;
    }

    private static String getKeyName(String name,
                                     String prefix,
                                     String table)
    {
        if (name == null || name.isBlank()) {
            name = prefix + table;
        }
        return name;
    }

}
