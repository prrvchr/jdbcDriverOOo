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
import java.sql.SQLException;
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

import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.PropertyIds;


public class KeyHelper {


    public static String[] getPrimaryKeyColumns(Provider provider,
                                                NamedComponent table,
                                                String keyname) {
        String[] columns = null;
        try {
            DatabaseMetaData metadata = provider.getConnection().getMetaData();
            columns = readPrimaryKeyColumns(metadata, table, keyname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }

    public static String[] readPrimaryKeyColumns(DatabaseMetaData metadata,
                                                 NamedComponent table,
                                                 String keyname)
        throws java.sql.SQLException {
        final int COLUMN_NAME = 4;
        final int PK_NAME = 6;
        ArrayList<String> cols = new ArrayList<>();
        String name = null;
        boolean fetched = false;
        int type = KeyType.PRIMARY;
        try (ResultSet result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                String column = result.getString(COLUMN_NAME);
                cols.add(column);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(PK_NAME);
                    name = getKeyName(pk, table.getTable(), type);
                }
            }
        }
        String[] columns = null;
        if (name != null && name.equals(keyname)) {
            columns = cols.toArray(new String[0]);
        }
        return columns;
    }

    public static final String[] getForeignKeyColumns(Provider provider,
                                                      NamedComponent table,
                                                      String keyname) {
        String[] columns = null;
        DatabaseMetaData metadata;
        try {
            metadata = provider.getConnection().getMetaData();
            ForeignKeyProperties properties = getForeignKeyProperties(metadata, table, keyname);
            columns = properties.getColumns();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }

    public static final ForeignKeyProperties getForeignKeyProperties(DatabaseMetaData metadata,
                                                                     NamedComponent table,
                                                                     String keyname)
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
        try (ResultSet result = metadata.getImportedKeys(table.getCatalog(), table.getSchema(), table.getTable())) {
            while (result.next()) {
                NamedComponent component = new NamedComponent();
                String value = result.getString(PKTABLE_CAT);
                if (!result.wasNull()) {
                    component.setCatalog(value);
                }
                value = result.getString(PKTABLE_SCHEM);
                if (!result.wasNull()) {
                    component.setSchema(value);
                }
                component.setTable(result.getString(PKTABLE_NAME));
                String column = result.getString(FKCOLUMN_NAME);
                int update = result.getInt(UPDATE_RULE);
                int delete = result.getInt(DELETE_RULE);
                String name = result.getString(FK_NAME);

                if (isValidForeingKey(result, name)) {
                    if (!oldname.equals(name)) {
                        if (properties != null && oldname.equals(keyname)) {
                            break;
                        }
                        properties = new ForeignKeyProperties(component, update, delete);
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



    public static String getKeyFromDescriptor(XPropertySet descriptor,
                                              Map<String, String> ref)
        throws java.sql.SQLException {
        String table = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        if (supplier != null) {
            XNameAccess columns = UnoRuntime.queryInterface(XNameAccess.class, supplier.getColumns());
            try {
                for (String foreign : columns.getElementNames()) {
                    if (columns.hasByName(foreign)) {
                        XPropertySet column;
                        column = UnoRuntime.queryInterface(XPropertySet.class, columns.getByName(foreign));
                        if (column != null) {
                            String primay = DBTools.getDescriptorStringValue(column, PropertyIds.RELATEDCOLUMN);
                            ref.put(foreign, primay);
                        }
                    }
                }
            } catch (NoSuchElementException | WrappedTargetException e) {
                e.printStackTrace();
                throw new java.sql.SQLException(e);
            }
        }
        return table;
    }

    public static String[] refreshKeys(Provider provider,
                                       NamedComponent table)
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
                                                                     NamedComponent table,
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
                    NamedComponent component = new NamedComponent();
                    value = result.getString(FKTABLE_CAT);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(FKTABLE_SCHEM);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(FKTABLE_NAME));
                    name = ComponentHelper.buildName(provider.getNamedSupport(rule), component);
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
                                                 NamedComponent table,
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
                    NamedComponent component = new NamedComponent();
                    value = result.getString(FKTABLE_CAT);
                    if (!result.wasNull()) {
                        component.setCatalog(value);
                    }
                    value = result.getString(FKTABLE_SCHEM);
                    if (!result.wasNull()) {
                        component.setSchema(value);
                    }
                    component.setTable(result.getString(FKTABLE_NAME));
                    name = ComponentHelper.buildName(provider.getNamedSupport(rule), component);
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
                                           NamedComponent table)
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
                                           NamedComponent table)
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

    private static boolean isValidForeingKey(java.sql.ResultSet result, String name)
        throws java.sql.SQLException {
        return !result.wasNull() && !name.isEmpty();
    }

    // XXX: Private helper function
    public static class ForeignKeyProperties {
        public int mUpdate;
        public int mDelete;
        public NamedComponent mTable;
        public List<String> mColumns = new ArrayList<>();

        private ForeignKeyProperties(NamedComponent component,
                                     int update,
                                     int delete)
            throws java.sql.SQLException {
            mTable = component;
            mUpdate = update;
            mDelete = delete;
        }

        public String[] getColumns() {
            return mColumns.toArray(new String[0]);
        }
    }


}
