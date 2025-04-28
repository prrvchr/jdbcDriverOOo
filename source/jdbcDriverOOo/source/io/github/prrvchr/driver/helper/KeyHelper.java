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
package io.github.prrvchr.driver.helper;

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

import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class KeyHelper {

    public static String getKeyFromDescriptor(DriverProvider provider,
                                              XPropertySet descriptor,
                                              Map<String, String> ref)
        throws WrappedTargetException, NoSuchElementException {
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
               ElementExistException {
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
               ElementExistException {
        Key key = readPrimaryKey(provider, table, component, keyname, sensitive);
        if (key == null) {
            key = readForeignKey(provider, table, component, keyname, rule, sensitive);
        }
        return key;
    }

    public static String getKeyName(String name,
                                    String table,
                                    int type) {
        return getKeyName(name, getKeyPrefix(type), table);
    }


    public static boolean isForeignKeyColumn(DriverProvider provider,
                                             NamedComponents table,
                                             String column)
        throws SQLException {
        return false;
    }


    public static Map<String, List<String>> getExportedTablesColumns(DriverProvider provider,
                                                                     NamedComponents table,
                                                                     String column,
                                                                     ComposeRule rule)
        throws java.sql.SQLException {
        // XXX: Here we need to retrieve all tables having this table / column as foreign key.
        final int PKCOLUMN_NAME = 4;
        final int FKTABLE_CAT = 5;
        final int FKTABLE_SCHEM = 6;
        final int FKTABLE_NAME = 7;
        final int FKCOLUMN_NAME = 8;
        Map<String, List<String>> tables = new TreeMap<String, List<String>>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getExportedKeys(table.getCatalog(),
                                                                                                table.getSchema(),
                                                                                                table.getTable())) {
            while (result.next()) {
                String value = result.getString(PKCOLUMN_NAME);
                if (!result.wasNull() && column.equals(value)) {
                    NamedComponents component = new NamedComponents();
                    value = result.getString(FKTABLE_CAT);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(FKTABLE_SCHEM);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(FKTABLE_NAME));
                    String name = DBTools.buildName(provider, component, rule);
                    value = result.getString(FKCOLUMN_NAME);
                    System.out.println("DBKeyHelper.getExportedTables() 1 Table " + name + " - Column: " + value);
                    if (!tables.containsKey(name)) {
                        tables.put(name, List.of(value));
                    } else {
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
        throws java.sql.SQLException {
        // XXX: Here we need to retrieve all tables having this table as foreign key.
        final int PKCOLUMN_NAME = 4;
        final int FKTABLE_CAT = 5;
        final int FKTABLE_SCHEM = 6;
        final int FKTABLE_NAME = 7;
        List<String> tables = new ArrayList<>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getExportedKeys(table.getCatalog(),
                                                                                                table.getSchema(),
                                                                                                table.getTable())) {
            while (result.next()) {
                String value = result.getString(PKCOLUMN_NAME);
                if (!result.wasNull()) {
                    NamedComponents component = new NamedComponents();
                    value = result.getString(FKTABLE_CAT);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(FKTABLE_SCHEM);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(FKTABLE_NAME));
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
    private static class ForeignKeyProperties {
        ArrayList<String> mColumns = new ArrayList<>();
        String mTable;
        int mUpdate;
        int mDelete;

        ForeignKeyProperties(DriverProvider provider,
                             NamedComponents table,
                             ComposeRule rule,
                             int update,
                             int delete)
            throws java.sql.SQLException {
            mTable = DBTools.buildName(provider, table, rule);
            mUpdate = update;
            mDelete = delete;
        }
    }

    private static void refreshPrimaryKeys(List<String> keys,
                                           DriverProvider provider,
                                           NamedComponents table)
        throws java.sql.SQLException {
        int type = KeyType.PRIMARY;
        final int PK_NAME = 6;
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getPrimaryKeys(table.getCatalog(),
                                                                                               table.getSchema(),
                                                                                               table.getTable())) {
            // XXX: There can only be one primary key per table.
            if (result.next()) {
                String pk = result.getString(PK_NAME);
                keys.add(getKeyName(pk, table.getTable(), type));
            }
        }
    }

    private static void refreshForeignKeys(List<String> keys,
                                           DriverProvider provider,
                                           NamedComponents table)
        throws java.sql.SQLException {
        String previous = "";
        final int FK_NAME = 12;
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getImportedKeys(table.getCatalog(),
                                                                                                table.getSchema(),
                                                                                                table.getTable())) {
            while (result.next()) {
                String name = result.getString(FK_NAME);
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
               ElementExistException {
        Key key = null;
        final int COLUMN_NAME = 4;
        final int PK_NAME = 6;
        ArrayList<String> columns = new ArrayList<>();
        String name = null;
        boolean fetched = false;
        int type = KeyType.PRIMARY;
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(component.getCatalog(),
                                                                 component.getSchema(),
                                                                 component.getTable())) {
            while (result.next()) {
                String column = result.getString(COLUMN_NAME);
                columns.add(column);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(PK_NAME);
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
               ElementExistException {
        Key key = null;
        ForeignKeyProperties properties = getForeignKeyProperties(provider, component, keyname, rule);
        if (properties != null) {
            key = new Key(table, sensitive, keyname, properties.mTable, KeyType.FOREIGN,
                          properties.mUpdate, properties.mDelete, properties.mColumns);
        }
        return key;
    }

    private static ForeignKeyProperties getForeignKeyProperties(DriverProvider provider,
                                                                NamedComponents component,
                                                                String keyname,
                                                                ComposeRule rule)
        throws java.sql.SQLException {
        String oldname = "";
        final int PKTABLE_CAT = 1;
        final int PKTABLE_SCHEM = 2;
        final int PKTABLE_NAME = 3;
        final int FKCOLUMN_NAME = 8;
        final int UPDATE_RULE = 10;
        final int DELETE_RULE = 11;
        final int FK_NAME = 12;
        ForeignKeyProperties properties = null;
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        try (java.sql.ResultSet result = metadata.getImportedKeys(component.getCatalog(),
                                                                  component.getSchema(),
                                                                  component.getTable())) {
            while (result.next()) {
                NamedComponents fk = new NamedComponents();
                String value = result.getString(PKTABLE_CAT);
                if (!result.wasNull()) {
                    fk.setCatalog(value);
                }
                value = result.getString(PKTABLE_SCHEM);
                if (!result.wasNull()) {
                    fk.setSchema(value);
                }
                fk.setTable(result.getString(PKTABLE_NAME));
                String column = result.getString(FKCOLUMN_NAME);
                int update = result.getInt(UPDATE_RULE);
                int delete = result.getInt(DELETE_RULE);
                String name = result.getString(FK_NAME);

                if (isValidForeingKey(result, name)) {
                    if (!oldname.equals(name)) {
                        if (properties != null && oldname.equals(keyname)) {
                            break;
                        }
                        properties = new ForeignKeyProperties(provider, fk, rule, update, delete);
                        properties.mColumns.add(column);
                        oldname = name;
                    } else if (properties != null) {
                        properties.mColumns.add(column);
                    }
                }
            }
        }
        if (!oldname.equals(keyname)) {
            properties = null;
        }
        return properties;
    }

    private static boolean isValidForeingKey(java.sql.ResultSet result, String name)
        throws java.sql.SQLException {
        return !result.wasNull() && !name.isEmpty();
    }

    private static String getKeyPrefix(int type) {
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
                                     String table) {
        if (name == null || name.isBlank()) {
            name = prefix + table;
        }
        return name;
    }

}
