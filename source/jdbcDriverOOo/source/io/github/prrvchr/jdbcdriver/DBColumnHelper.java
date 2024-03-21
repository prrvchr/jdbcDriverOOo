/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.uno.Any;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;
import io.github.prrvchr.uno.sdbcx.ColumnContainerBase.ExtraColumnInfo;


public class DBColumnHelper
{

    public static class ColumnDescription
    {
        public String columnName;
        public int type;
        public String typeName;
        public int columnSize;
        public int decimalDigits;
        public int nullable;
        public String remarks;
        public String defaultValue;
        public int ordinalPosition;
    }

    private static class KeyProperties
    {
        ArrayList<String> columnNames = new ArrayList<>();
        String referencedTable;
        int type;
        int updateRule;
        int deleteRule;
        KeyProperties(String referencedTable, int type, int updateRule, int deleteRule)
        {
            this.referencedTable = referencedTable;
            this.type = type;
            this.updateRule = updateRule;
            this.deleteRule = deleteRule;
        }
    }

    public static List<ColumnDescription> readColumns(DriverProvider provider,
                                                      String catalog,
                                                      String schema,
                                                      String table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> descriptions = collectColumnDescriptions(provider, catalog, schema, table);
        sanitizeColumnDescriptions(provider, descriptions);
        List<ColumnDescription> columns = new ArrayList<>(descriptions);
        for (ColumnDescription description : descriptions) {
            columns.set(description.ordinalPosition - 1, description);
        }
        return columns;
    }

    private static List<ColumnDescription> collectColumnDescriptions(DriverProvider provider,
                                                                     String catalog,
                                                                     String schema,
                                                                     String table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> columns = new ArrayList<>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getColumns(catalog, schema, table, "%"))
        {
            while (result.next()) {
                ColumnDescription description = new ColumnDescription();
                description.columnName = result.getString(4);
                description.type = provider.getDataType(result.getInt(5));
                description.typeName = result.getString(6);
                int ivalue = result.getInt(7);
                description.columnSize = result.wasNull() ? 0 : ivalue;
                ivalue = result.getInt(9);
                description.decimalDigits = result.wasNull() ? 0 : ivalue;
                description.nullable = result.getInt(11);
                String svalue = result.getString(12);
                description.remarks = result.wasNull() ? "" : svalue;
                svalue = result.getString(13);
                description.defaultValue = result.wasNull() ? "" : svalue;
                description.ordinalPosition = result.getInt(17);
                columns.add(description);
            }
        }
        return columns;
    }

    private static void sanitizeColumnDescriptions(DriverProvider provider,
                                                   List<ColumnDescription> descriptions)
    {
        if (descriptions.isEmpty()) {
            return;
        }
        Set<Integer> ordinals = new TreeSet<>();
        int max = Integer.MIN_VALUE;
        for (ColumnDescription description : descriptions) {
            ordinals.add(description.ordinalPosition);
            if (max < description.ordinalPosition) {
                max = description.ordinalPosition;
            }
        }
        // we need to have as many different ordinals as we have different columns
        boolean hasduplicates = ordinals.size() != descriptions.size();
        // and it needs to be a continuous range
        boolean hasgaps = (max - ordinals.iterator().next() + 1) != descriptions.size();
        // if that's not the case, normalize it
        UnoHelper.ensure(!hasduplicates && !hasgaps, "database provided invalid ORDINAL_POSITION values!", provider.getLogger());
        // what's left is that the range might not be from 1 to <column count>, but for instance
        // 0 to <column count>-1.
        int offset = ordinals.iterator().next() - 1;
        for (ColumnDescription description : descriptions) {
            description.ordinalPosition -= offset;
        }
    }


    /** collects the information about auto increment, currency and data type for the given column name.
     * The column must be quoted, * is also valid.
     * @param connection
     *     The connection.
     * @param composedName
     *    The quoted table name. ccc.sss.ttt
     * @param columnName
     *    The name of the column, or *
     * @return
     *    The information about the column(s).
     */
    public static Map<String, ExtraColumnInfo> collectColumnInformation(DriverProvider provider,
                                                                        String composedName,
                                                                        String columnName)
        throws java.sql.SQLException
    {
        Map<String, ExtraColumnInfo> columns = new TreeMap<>();
        String sql = MessageFormat.format(DBDefaultQuery.STR_QUERY_METADATA_RESULTSET, columnName, composedName);
        java.sql.Statement statement = provider.getStatement();
        statement.setEscapeProcessing(false);
        try (java.sql.ResultSet result = statement.executeQuery(sql))
        {
            System.out.println("DBColumnHelper.collectColumnInformation() 1");
            java.sql.ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            UnoHelper.ensure(count > 0, "resultset has empty metadata", provider.getLogger());
            for (int i = 1; i <= count; i++) {
                String newColumnName = metadata.getColumnName(i);
                ExtraColumnInfo columnInfo = new ExtraColumnInfo();
                columnInfo.isAutoIncrement = metadata.isAutoIncrement(i);
                boolean iscurrency = provider.isIgnoreCurrencyEnabled() ? false : metadata.isCurrency(i);
                System.out.println(String.format("DBColumnHelper.collectColumnInformation() 2 isCurrency: %s", iscurrency));
                columnInfo.isCurrency = iscurrency;
                columnInfo.dataType = provider.getDataType(metadata.getColumnType(i));
                columns.put(newColumnName, columnInfo);
            }
        }
        return columns;
    }



    public static Key readKey(DriverProvider provider,
                              TableSuper<?> table,
                              String catalog,
                              String schema,
                              String tablename,
                              String keyname,
                              boolean sensitive)
        throws SQLException,
               ElementExistException
    {
        try {
            Key key = readPrimaryKey(provider, table, catalog, schema, tablename, keyname, sensitive);
            if (key == null) {
                key = readForeignKey(provider, table, catalog, schema, tablename, keyname, sensitive);
            }
            return key;
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), table, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
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
                        name = String.format("PK_%s_%s", tablename, column);
                    }
                    else {
                        name = pk;
                    }
                }
                System.out.println("DBColumnHelper.readPrimaryKey() Column name: " + column + " - Primary Key: " + keyname);
            }
        }
        if (name != null && name.equals(keyname)) {
            key = new Key(table, sensitive, keyname, "", KeyType.PRIMARY, 0, 0, columns);
        }
        return key;
    }

    private static Key readForeignKey(DriverProvider provider,
                                      TableSuper<?> table,
                                      String catalog,
                                      String schema,
                                      String tablename,
                                      String keyname,
                                      boolean sensitive)
        throws SQLException,
               ElementExistException
    {
        Key key = null;
        String oldFkName = "";
        KeyProperties keyProperties = null;
        try {
            java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getImportedKeys(catalog, schema, tablename))
            {
                while (result.next()) {
                    String value = result.getString(1);
                    String catalogReturned = result.wasNull() ? "" : value;
                    value = result.getString(2);
                    String schemaReturned = result.wasNull() ? "" : value;
                    String nameReturned = result.getString(3);
                    
                    String foreignKeyColumn = result.getString(8);
                    int updateRule = result.getInt(10);
                    int deleteRule = result.getInt(11);
                    String fkName = result.getString(12);
                    
                    if (!result.wasNull() && !fkName.isEmpty()) {
                        if (!oldFkName.equals(fkName)) {
                            if (keyProperties != null && oldFkName.equals(keyname)) {
                                break;
                            }
                            String referencedName = DBTools.buildName(provider, catalogReturned, schemaReturned, nameReturned,
                                                                      ComposeRule.InDataManipulation, sensitive);
                            keyProperties = new KeyProperties(referencedName, KeyType.FOREIGN, updateRule, deleteRule);
                            keyProperties.columnNames.add(foreignKeyColumn);
                            oldFkName = fkName;
                        }
                        else {
                            if (keyProperties != null) {
                                keyProperties.columnNames.add(foreignKeyColumn);
                            }
                        }
                    }
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        if (keyProperties != null && oldFkName.equals(keyname)) {
            key = new Key(table, sensitive, oldFkName, keyProperties.referencedTable, keyProperties.type,
                          keyProperties.updateRule, keyProperties.deleteRule, keyProperties.columnNames);
        }
        return key;
    }


    public static Map<String, Key> readKeys(DriverProvider provider,
                                            TableSuper<?> table,
                                            String catalog,
                                            String schema,
                                            String tablename,
                                            boolean sensitive)
        throws SQLException,
               ElementExistException
    {
        Map<String, Key> keys = new TreeMap<>();
        readPrimaryKeys(provider, table, catalog, schema, tablename, sensitive, keys);
        readForeignKeys(provider, table, catalog, schema, tablename, sensitive, keys);
        return keys;
    }

    private static void readPrimaryKeys(DriverProvider provider,
                                       TableSuper<?> table,
                                       String catalog,
                                       String schema,
                                       String tablename,
                                       boolean sensitive,
                                       Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        ArrayList<String> columns = new ArrayList<>();
        String name = null;
        boolean fetched = false;
        try {
            java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getPrimaryKeys(catalog, schema, tablename))
            {
                while (result.next()) {
                    String column = result.getString(4);
                    columns.add(column);
                    if (!fetched) {
                        fetched = true;
                        String pk = result.getString(6);
                        if (!result.wasNull()) {
                            name = pk;
                        }
                    }
                    System.out.println("DBColumnHelper.readPrimaryKey() Column name: " + column + " - Primary Key: " + name);
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        if (fetched) {
            name = getKeyName(name, tablename, columns, KeyType.PRIMARY);
            keys.put(name, new Key(table, sensitive, name, "", KeyType.PRIMARY, 0, 0, columns));
        }
    }

    public static String getKeyName(String name,
                                    String table,
                                    List<String> columns,
                                    int type)
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
        return getKeyName(name, table, columns, prefix);
    }

    public static String getKeyName(String name,
                                    String table,
                                    List<String> columns,
                                    String prefix)
    {
        if (name == null || name.isBlank()) {
            name = String.format("%s%s_%s", prefix, table, String.join("_", columns));
        }
        return name;
    }

    private static void readForeignKeys(DriverProvider provider,
                                        TableSuper<?> table,
                                        String catalog,
                                        String schema,
                                        String tablename,
                                        boolean sensitive,
                                        Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        String oldFkName = "";
        KeyProperties keyProperties = null;
        try {
            java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getImportedKeys(catalog, schema, tablename))
            {
                while (result.next()) {
                    String value = result.getString(1);
                    String catalogReturned = result.wasNull() ? "" : value;
                    value = result.getString(2);
                    String schemaReturned = result.wasNull() ? "" : value;
                    String nameReturned = result.getString(3);
                    
                    String foreignKeyColumn = result.getString(8);
                    int updateRule = result.getInt(10);
                    int deleteRule = result.getInt(11);
                    String fkName = result.getString(12);
                    
                    if (!result.wasNull() && !fkName.isEmpty()) {
                        if (!oldFkName.equals(fkName)) {
                            if (keyProperties != null) {
                                Key key = new Key(table, sensitive, oldFkName, keyProperties.referencedTable, keyProperties.type,
                                                  keyProperties.updateRule, keyProperties.deleteRule, keyProperties.columnNames);
                                
                                keys.put(oldFkName, key);
                            }
                            String referencedName = DBTools.buildName(provider, catalogReturned, schemaReturned, nameReturned,
                                                                      ComposeRule.InDataManipulation, sensitive);
                            keyProperties = new KeyProperties(referencedName, KeyType.FOREIGN, updateRule, deleteRule);
                            keyProperties.columnNames.add(foreignKeyColumn);
                            oldFkName = fkName;
                        }
                        else {
                            if (keyProperties != null) {
                                keyProperties.columnNames.add(foreignKeyColumn);
                            }
                        }
                    }
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        if (keyProperties != null) {
            Key key = new Key(table, sensitive, oldFkName, keyProperties.referencedTable, keyProperties.type,
                              keyProperties.updateRule, keyProperties.deleteRule, keyProperties.columnNames);
            keys.put(oldFkName, key);
        }
    }

    public static ArrayList<String> readIndexes(DriverProvider provider,
                                                String catalog,
                                                String schema,
                                                String table,
                                                boolean qualified)
        throws java.sql.SQLException
    {
        ArrayList<String> names = new ArrayList<>();
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        String separator = metadata.getCatalogSeparator();
        System.out.println("DBColumnHelper.readIndexes() 1");
        try (java.sql.ResultSet result = metadata.getIndexInfo(catalog, schema, table, false, false))
        {
            String previous = "";
            while (result.next()) {
                System.out.println("DBColumnHelper.readIndexes() Qualifier: " + result.getString(5) + " - Name: " + result.getString(6));
                StringBuilder buffer = new StringBuilder();
                if (qualified) {
                    String qualifier = result.getString(5);
                    if (!result.wasNull() && !qualifier.isEmpty()) {
                        buffer.append(qualifier);
                        buffer.append(separator);
                    }
                }
                buffer.append(result.getString(6));
                String name = buffer.toString();
                // XXX: Don't insert the name if the last one we inserted was the same
                if (!result.wasNull() && !name.isEmpty() && !previous.equals(name)) {
                    System.out.println("DBColumnHelper.readIndexes() add Name: " + name);
                    names.add(name);
                    previous = name;
                }
            }
        }
        return names;
    }


    public static String readIndexName(DriverProvider provider,
                                       String catalog,
                                       String schema,
                                       String table,
                                       String index)
        throws java.sql.SQLException
    {
        String newname = index;
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        String separator = metadata.getCatalogSeparator();
        System.out.println("DBColumnHelper.readIndexName() 1");
        try (java.sql.ResultSet result = metadata.getIndexInfo(catalog, schema, table, false, false))
        {
            while (result.next()) {
                System.out.println("DBColumnHelper.readIndexName() 2 Qualifier: " + result.getString(5) + " - Name: " + result.getString(6));
                String qualifier = result.getString(5);
                if (result.wasNull()) {
                    qualifier = "";
                }
                else if (!qualifier.isEmpty()){
                    qualifier += separator;
                }
                String name = result.getString(6);
                if (!result.wasNull() && !name.isEmpty() && index.equals(name)) {
                    newname = qualifier + name;
                    break;
                }
            }
        }
        System.out.println("DBColumnHelper.readIndexName() 3 NewName: " + newname);
        return newname;
    }


    public static boolean isPrimaryKeyIndex(java.sql.DatabaseMetaData metadata,
                                            String catalog,
                                            String schema,
                                            String table,
                                            String name)
        throws java.sql.SQLException
    {
        boolean primary = false;
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(catalog, schema, table))
        {
            // XXX: There can be only one primary key
            if (result.next()) {
                primary = name.equals(result.getString(6));
            }
        }
        return primary;
    }

}
