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
package io.github.prrvchr.jdbcdriver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class DBKeyHelper
{

    public static List<String> refreshKeys(DriverProvider provider,
                                           String catalog,
                                           String schema,
                                           String table)
        throws java.sql.SQLException,
               ElementExistException
    {
        List<String> keys = new ArrayList<>();
        refreshPrimaryKeys(keys, provider, catalog, schema, table);
        refreshForeignKeys(keys, provider, catalog, schema, table);
        return keys;
    }

    public static Key readKey(DriverProvider provider,
                              TableSuper<?> table,
                              String catalog,
                              String schema,
                              String tablename,
                              String keyname,
                              ComposeRule rule,
                              boolean sensitive)
        throws java.sql.SQLException,
               ElementExistException
    {
        Key key = readPrimaryKey(provider, table, catalog, schema, tablename, keyname, sensitive);
        if (key == null) {
            key = readForeignKey(provider, table, catalog, schema, tablename, keyname, rule, sensitive);
        }
        return key;
    }

    public static String getKeyName(String name,
                                    String table,
                                    List<String> columns,
                                    int type)
    {
        return getKeyName(name, getKeyPrefix(type), table, columns);
    }

    // XXX: Private helper function
    private static class ForeignKeyProperties
    {
        ArrayList<String> columns = new ArrayList<>();
        String table;
        int update;
        int delete;

        ForeignKeyProperties(DriverProvider provider,
                             String catalog,
                             String schema,
                             String table,
                             ComposeRule rule,
                             int update,
                             int delete)
            throws SQLException
        {
            this.table = DBTools.buildName(provider, catalog, schema, table, rule, false);
            this.update = update;
            this.delete = delete;
        }
    }

    private static void refreshPrimaryKeys(List<String> keys,
                                           DriverProvider provider,
                                           String catalog,
                                           String schema,
                                           String table)
        throws java.sql.SQLException
    {
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getPrimaryKeys(catalog, schema, table)) 
        {
            // XXX: There can only be one primary key per table.
            if (result.next()) {
                String name = result.getString(6);
                if (!result.wasNull()) {
                    keys.add(name);
                }
            }
        }
    }

    private static void refreshForeignKeys(List<String> keys,
                                           DriverProvider provider,
                                           String catalog,
                                           String schema,
                                           String table)
        throws java.sql.SQLException
    {
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getImportedKeys(catalog, schema, table)) 
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
                                      TableSuper<?> table,
                                      String catalog,
                                      String schema,
                                      String tablename,
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
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(catalog, schema, tablename))
        {
            while (result.next()) {
                String column = result.getString(4);
                columns.add(column);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(6);
                    if (result.wasNull()) {
                        pk = null;
                    }
                    name = getKeyName(pk, getKeyPrefix(type), tablename, column);
                }
            }
        }
        if (name != null && name.equals(keyname)) {
            key = new Key(table, sensitive, keyname, "", type, 0, 0, columns);
        }
        return key;
    }

    private static Key readForeignKey(DriverProvider provider,
                                      TableSuper<?> table,
                                      String catalogname,
                                      String schemaname,
                                      String tablename,
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
        try (java.sql.ResultSet result = metadata.getImportedKeys(catalogname, schemaname, tablename))
        {
            while (result.next()) {
                String value = result.getString(1);
                String fkcatalog = result.wasNull() ? "" : value;
                value = result.getString(2);
                String fkschema = result.wasNull() ? "" : value;
                String fktable = result.getString(3);

                String column = result.getString(8);
                int update = result.getInt(10);
                int delete = result.getInt(11);
                String name = result.getString(12);
                
                if (!result.wasNull() && !name.isEmpty()) {
                    if (!oldname.equals(name)) {
                        if (properties != null && oldname.equals(keyname)) {
                            break;
                        }
                        properties = new ForeignKeyProperties(provider, fkcatalog, fkschema, fktable, rule, update, delete);
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
                                     String table,
                                     List<String> columns)
    {
        return getKeyName(name, prefix, table, String.join("_", columns));
    }

    private static String getKeyName(String name,
                                     String prefix,
                                     String table,
                                     String columns)
    {
        if (name == null || name.isBlank()) {
            name = String.format("%s%s_%s", prefix, table, columns);
        }
        return name;
    }

}
