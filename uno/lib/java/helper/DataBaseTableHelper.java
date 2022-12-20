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
package io.github.prrvchr.uno.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.Key;
import io.github.prrvchr.uno.sdbcx.TableBase;


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
                                                      TableBase table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> columnDescriptions = collectColumnDescriptions(connection, table);
        sanitizeColumnDescriptions(columnDescriptions);
        List<ColumnDescription> columnsByOrdinal = new ArrayList<>(columnDescriptions);
        for (ColumnDescription columnDescription : columnDescriptions) {
            columnsByOrdinal.set(columnDescription.ordinalPosition - 1, columnDescription);
        }
        return columnsByOrdinal;
    }

    private static List<ColumnDescription> collectColumnDescriptions(ConnectionSuper connection,
                                                                     TableBase table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> columns = new ArrayList<>();
        java.sql.ResultSet result = connection.getProvider().getConnection().getMetaData().getColumns(table.getCatalogName(), table.getSchemaName(), table.getName(), "%");
        while (result.next()) {
            ColumnDescription columnDescription = new ColumnDescription();
            columnDescription.columnName = result.getString(4);
            columnDescription.type = connection.getProvider().getDataType(result.getInt(5));
            columnDescription.typeName = result.getString(6);
            columnDescription.columnSize = result.getInt(7);
            columnDescription.decimalDigits = result.getInt(9);
            columnDescription.nullable = result.getInt(11);
            columnDescription.remarks = result.getString(12);
            columnDescription.defaultValue = result.getString(13);
            columnDescription.ordinalPosition = result.getInt(17);
            columns.add(columnDescription);
        }
        result.close();
        return columns;
    }

    private static void sanitizeColumnDescriptions(List<ColumnDescription> columnDescriptions)
    {
        if (columnDescriptions.isEmpty()) {
            return;
        }
        Set<Integer> usedOrdinals = new TreeSet<>();
        int maxOrdinal = Integer.MIN_VALUE;
        for (ColumnDescription columnDescription : columnDescriptions) {
            usedOrdinals.add(columnDescription.ordinalPosition);
            if (maxOrdinal < columnDescription.ordinalPosition) {
                maxOrdinal = columnDescription.ordinalPosition;
            }
        }
        // we need to have as many different ordinals as we have different columns
        boolean hasDuplicates = usedOrdinals.size() != columnDescriptions.size();
        // and it needs to be a continuous range
        boolean hasGaps = (maxOrdinal - usedOrdinals.iterator().next() + 1) != columnDescriptions.size();
        // if that's not the case, normalize it
        UnoHelper.ensure(!hasDuplicates && !hasGaps, "database provided invalid ORDINAL_POSITION values!");
        // what's left is that the range might not be from 1 to <column count>, but for instance
        // 0 to <column count>-1.
        int offset = usedOrdinals.iterator().next() - 1;
        for (ColumnDescription columnDescription : columnDescriptions) {
            columnDescription.ordinalPosition -= offset;
        }
    }

    public static Map<String, Key> readKeys(ConnectionSuper connection,
                                            boolean sensitive,
                                            TableBase table)
        throws SQLException,
               ElementExistException
    {
        Map<String, Key> keys = new TreeMap<>();
        readPrimaryKey(connection, table, sensitive, keys);
        readForeignKeys(connection, table, sensitive, keys);
        return keys;
    }

    private static void readPrimaryKey(ConnectionSuper connection,
                                       TableBase table,
                                       boolean sensitive,
                                       Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        ArrayList<String> columns = new ArrayList<>();
        String name = null;
        try {
            boolean fetched = false;
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result  = metadata.getPrimaryKeys(table.getCatalogName(), table.getSchemaName(), table.getName());
            while (result.next()) {
                String columnName = result.getString(4);
                System.out.println("DataBaseTableHelper.readPrimaryKey() Column name: " + result.getString(4) + " - Primary Key: " + result.getString(6));
                columns.add(columnName);
                if (!fetched) {
                    fetched = true;
                    String pk = result.getString(6);
                    if (result.wasNull()) {
                        name = String.format("PK_%s_%s", table.getName(), columnName);
                    }
                    else {
                        name = pk;
                    }
                }
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, connection);
        }
        if (name != null) {
            keys.put(name, new Key(table, sensitive, name, "", KeyType.PRIMARY, 0, 0, columns));
        }
    }

    private static void readForeignKeys(ConnectionSuper connection,
                                        TableBase table,
                                        boolean sensitive,
                                        Map<String, Key> keys)
        throws SQLException,
               ElementExistException
    {
        String oldFkName = "";
        KeyProperties keyProperties = null;
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getImportedKeys(table.getCatalogName(), table.getSchemaName(), table.getName());
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
                        String referencedName = DataBaseTools.composeTableName(connection, catalogReturned, schemaReturned, nameReturned,
                                                                               false, ComposeRule.InDataManipulation);
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
            UnoHelper.getSQLException(e, connection);
        }        
        if (keyProperties != null) {
            Key key = new Key(table, sensitive, oldFkName, keyProperties.referencedTable, keyProperties.type,
                              keyProperties.updateRule, keyProperties.deleteRule, keyProperties.columnNames);
            keys.put(oldFkName, key);
        }
    }

    public static ArrayList<String> readIndexes(ConnectionSuper connection,
                                                TableBase table)
        throws SQLException
    {
        ArrayList<String> names = new ArrayList<>();
        String separator = connection.getMetaData().getCatalogSeparator();
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getIndexInfo(table.getCatalogName(), table.getSchemaName(), table.getName(), false, false);
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
            UnoHelper.getSQLException(e, connection);
        }        
        return names;
    }


}
