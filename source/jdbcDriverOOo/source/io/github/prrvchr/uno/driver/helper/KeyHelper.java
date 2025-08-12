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
package io.github.prrvchr.uno.driver.helper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.PropertyIds;


public class KeyHelper {

    public static String getKeyFromDescriptor(XPropertySet descriptor,
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

    public static String[] refreshKeys(Provider provider,
                                       NamedComponents table)
        throws java.sql.SQLException {
        List<String> keys = new ArrayList<>();
        refreshPrimaryKeys(keys, provider, table);
        refreshForeignKeys(keys, provider, table);
        return keys.toArray(new String[0]);
    }

    public static String getKeyName(String name,
                                    String table,
                                    int type) {
        return getKeyName(name, getKeyPrefix(type), table);
    }

    public static Map<String, List<String>> getExportedTablesColumns(Provider provider,
                                                                     NamedComponents table,
                                                                     String column,
                                                                     ComposeRule rule)
        throws java.sql.SQLException {
        // XXX: Here we need to retrieve all tables having this table / column as foreign key.
        String value, name;
        final int PKCOLUMN_NAME = 4;
        final int FKTABLE_CAT = 5;
        final int FKTABLE_SCHEM = 6;
        final int FKTABLE_NAME = 7;
        final int FKCOLUMN_NAME = 8;
        Map<String, List<String>> tables = new TreeMap<>();
        DatabaseMetaData dbmd = provider.getConnection().getMetaData();
        try (ResultSet result = dbmd.getExportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                value = result.getString(PKCOLUMN_NAME);
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
                    name = DBTools.buildName(provider, component, rule);
                    value = result.getString(FKCOLUMN_NAME);
                    if (!tables.containsKey(name)) {
                        tables.put(name, new ArrayList<>());
                    }
                    tables.get(name).add(value);
                }
            }
        }
        return tables;
    }

    public static List<String> getExportedTables(Provider provider,
                                                 NamedComponents table,
                                                 ComposeRule rule)
        throws java.sql.SQLException {
        // XXX: Here we need to retrieve all tables having this table as foreign key.
        String value, name;
        final int PKCOLUMN_NAME = 4;
        final int FKTABLE_CAT = 5;
        final int FKTABLE_SCHEM = 6;
        final int FKTABLE_NAME = 7;
        List<String> tables = new ArrayList<>();
        DatabaseMetaData dbmd = provider.getConnection().getMetaData();
        try (ResultSet result = dbmd.getExportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                value = result.getString(PKCOLUMN_NAME);
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
                    name = DBTools.buildName(provider, component, rule);
                    if (!tables.contains(name)) {
                        tables.add(name);
                    }
                }
            }
        }
        return tables;
    }

    private static void refreshPrimaryKeys(List<String> keys,
                                           Provider provider,
                                           NamedComponents table)
        throws java.sql.SQLException {
        int type = KeyType.PRIMARY;
        final int PK_NAME = 6;
        DatabaseMetaData dbmd = provider.getConnection().getMetaData();
        try (ResultSet result = dbmd.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            // XXX: There can only be one primary key per table.
            if (result.next()) {
                String pk = result.getString(PK_NAME);
                keys.add(getKeyName(pk, table.getTable(), type));
            }
        }
    }

    private static void refreshForeignKeys(List<String> keys,
                                           Provider provider,
                                           NamedComponents table)
        throws java.sql.SQLException {
        String previous = "";
        final int FK_NAME = 12;
        DatabaseMetaData dbmd = provider.getConnection().getMetaData();
        try (ResultSet result = dbmd.getImportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                String name = result.getString(FK_NAME);
                if (!result.wasNull() && !name.equals(previous)) {
                    keys.add(name);
                    previous = name;
                }
            }
        }
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
