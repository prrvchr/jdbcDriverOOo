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
package io.github.prrvchr.jdbcdriver.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.helper.UnoHelper;
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

    public static List<ColumnDescription> readColumns(DriverProvider provider,
                                                      NamedComponents table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> descriptions = collectColumnDescriptions(provider, table);
        sanitizeColumnDescriptions(provider, descriptions);
        List<ColumnDescription> columns = new ArrayList<>(descriptions);
        for (ColumnDescription description : descriptions) {
            columns.set(description.ordinalPosition - 1, description);
        }
        return columns;
    }

    private static List<ColumnDescription> collectColumnDescriptions(DriverProvider provider,
                                                                     NamedComponents table)
        throws java.sql.SQLException
    {
        List<ColumnDescription> columns = new ArrayList<>();
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getColumns(table.getCatalog(), table.getSchema(), table.getTable(), "%"))
        {
            while (result.next()) {
                ColumnDescription description = new ColumnDescription();
                String svalue = result.getString(4);
                description.columnName = result.wasNull() ? "" : svalue;
                int ivalue = result.getInt(5);
                description.type = provider.getDataType(result.wasNull() ? 0 : ivalue);
                svalue = result.getString(6);
                description.typeName = result.wasNull() ? "" : svalue;
                ivalue = result.getInt(7);
                description.columnSize = result.wasNull() ? 0 : ivalue;
                ivalue = result.getInt(9);
                description.decimalDigits = result.wasNull() ? 0 : ivalue;
                description.nullable = result.getInt(11);
                svalue = result.getString(12);
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
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_METADATA_RESULTSET);
        String sql = DBTools.formatSQLQuery(command, columnName, composedName);
        java.sql.Statement statement = provider.getStatement();
        statement.setEscapeProcessing(false);
        try (java.sql.ResultSet result = statement.executeQuery(sql))
        {
            java.sql.ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            UnoHelper.ensure(count > 0, "resultset has empty metadata", provider.getLogger());
            for (int i = 1; i <= count; i++) {
                String newColumnName = metadata.getColumnName(i);
                ExtraColumnInfo columnInfo = new ExtraColumnInfo();
                columnInfo.isAutoIncrement = metadata.isAutoIncrement(i);
                boolean iscurrency = provider.isIgnoreCurrencyEnabled() ? false : metadata.isCurrency(i);
                columnInfo.isCurrency = iscurrency;
                columnInfo.dataType = provider.getDataType(metadata.getColumnType(i));
                columns.put(newColumnName, columnInfo);
            }
        }
        return columns;
    }

}
