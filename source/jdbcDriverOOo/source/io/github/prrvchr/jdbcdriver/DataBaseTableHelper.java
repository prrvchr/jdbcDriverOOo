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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class DataBaseTableHelper
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

    public static List<ColumnDescription> readColumns(ConnectionSuper connection,
                                                      TableSuper table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> descriptions = collectColumnDescriptions(connection, table);
        sanitizeColumnDescriptions(connection, descriptions);
        List<ColumnDescription> columns = new ArrayList<>(descriptions);
        for (ColumnDescription description : descriptions) {
            columns.set(description.ordinalPosition - 1, description);
        }
        return columns;
    }

    private static List<ColumnDescription> collectColumnDescriptions(ConnectionSuper connection,
                                                                     TableSuper table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> columns = new ArrayList<>();
        java.sql.ResultSet result = connection.getProvider().getConnection().getMetaData().getColumns(table.getCatalog(), table.getSchema(), table.getName(), "%");
        while (result.next()) {
            ColumnDescription description = new ColumnDescription();
            description.columnName = result.getString(4);
            description.type = connection.getProvider().getDataType(result.getInt(5));
            description.typeName = result.getString(6);
            description.columnSize = result.getInt(7);
            int decimalDigits = result.getInt(9);
            description.decimalDigits = result.wasNull() ? 0 : decimalDigits;
            description.nullable = result.getInt(11);
            String remarks = result.getString(12);
            description.remarks = result.wasNull() ? "" : remarks;
            String defaultValue = result.getString(13);
            description.defaultValue = result.wasNull() ? "" : defaultValue;
            description.ordinalPosition = result.getInt(17);
            columns.add(description);
        }
        result.close();
        return columns;
    }

    private static void sanitizeColumnDescriptions(ConnectionSuper connection,
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
        UnoHelper.ensure(!hasduplicates && !hasgaps, "database provided invalid ORDINAL_POSITION values!", connection.getLogger());
        // what's left is that the range might not be from 1 to <column count>, but for instance
        // 0 to <column count>-1.
        int offset = ordinals.iterator().next() - 1;
        for (ColumnDescription description : descriptions) {
            description.ordinalPosition -= offset;
        }
    }

    public static Map<String, Key> readKeys(ConnectionSuper connection,
                                            boolean sensitive,
                                            TableSuper table)
        throws SQLException,
               ElementExistException
    {
        Map<String, Key> keys = new TreeMap<>();
        readPrimaryKey(connection, table, sensitive, keys);
        readForeignKeys(connection, table, sensitive, keys);
        return keys;
    }

    private static void readPrimaryKey(ConnectionSuper connection,
                                       TableSuper table,
                                       boolean sensitive,
                                       Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        ArrayList<String> columns = new ArrayList<>();
        String name = null;
        boolean fetched = false;
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
            while (result.next()) {
                String column = result.getString(4);
                columns.add(column);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(6);
                    if (result.wasNull()) {
                        name = String.format("PK_%s_%s", table.getName(), column);
                    }
                    else {
                        name = pk;
                    }
                }
                System.out.println("DataBaseTableHelper.readPrimaryKey() Column name: " + column + " - Primary Key: " + name);
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        if (name != null) {
            keys.put(name, new Key(table, sensitive, name, "", KeyType.PRIMARY, 0, 0, columns));
        }
    }

    private static void readForeignKeys(ConnectionSuper connection,
                                        TableSuper table,
                                        boolean sensitive,
                                        Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        String oldFkName = "";
        KeyProperties keyProperties = null;
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getImportedKeys(table.getCatalog(), table.getSchema(), table.getName());
            while (result.next()) {
                String catalogReturned = result.getString(1);
                if (result.wasNull()) {
                    catalogReturned = "";
                }
                String schemaReturned = result.getString(2);
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
                        String referencedName = DBTools.getTableName(connection, catalogReturned, schemaReturned, nameReturned,
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
            result.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }        
        if (keyProperties != null) {
            Key key = new Key(table, sensitive, oldFkName, keyProperties.referencedTable, keyProperties.type,
                              keyProperties.updateRule, keyProperties.deleteRule, keyProperties.columnNames);
            keys.put(oldFkName, key);
        }
    }

    public static ArrayList<String> readIndexes(ConnectionSuper connection,
                                                TableSuper table)
        throws SQLException
    {
        ArrayList<String> names = new ArrayList<>();
        String separator = connection.getMetaData().getCatalogSeparator();
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), false, false);
            String previous = "";
            System.out.println("sdbcx.IndexContainer.readIndexes() 1");
            while (result.next()) {
                System.out.println("sdbcx.IndexContainer.readIndexes() Qualifier: " + result.getString(5) + " - Name: " + result.getString(6));
                String name = result.getString(5);
                if (!result.wasNull() && !name.isEmpty()) {
                    name += separator;
                }
                name += result.getString(6);
                if (!name.isEmpty()) {
                    // don't insert the name if the last one we inserted was the same
                    if (!previous.equals(name)) {
                        System.out.println("sdbcx.IndexContainer.readIndexes() add Name: " + name);
                        names.add(name);
                        previous = name;
                    }
                }
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }        
        return names;
    }


}
