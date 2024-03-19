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
package io.github.prrvchr.jdbcdriver;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.sdbcx.TableSuper;


public class DBTableHelper
{

    private static class ColumnProperties
    {
        String newname;
        String oldidentifier = "";
        String newidentifier;
        StringBuilder columntype;
        String defaultvalue;
        boolean isautoincrement;
        String autoincrement;
        boolean notnull;
        ColumnProperties(DriverProvider provider, String name, boolean sensitive)
            throws SQLException
        {
            this(DBTools.enquoteIdentifier(provider, name, sensitive), name);
        }
        ColumnProperties(DriverProvider provider, String name1, String name2, boolean sensitive)
            throws SQLException
        {
            // XXX: If it's a new column then name1 is empty...
            this(DBTools.enquoteIdentifier(provider, name1.isBlank() ? name2 : name1, sensitive),
                 DBTools.enquoteIdentifier(provider, name2, sensitive),
                 name2);
        }
        private ColumnProperties(String identifier, String name)
                throws SQLException
        {
            this(identifier, identifier, name);
        }
        private ColumnProperties(String identifier1, String identifier2, String name)
            throws SQLException
        {
            newname = name;
            oldidentifier = identifier1;
            newidentifier = identifier2;
            columntype = new StringBuilder();
            defaultvalue = "";
            isautoincrement = false;
            autoincrement = "";
            notnull = false;
        }
        public String toString() {
            StringBuilder buffer = new StringBuilder(newidentifier);
            buffer.append(" ");
            buffer.append(columntype.toString());
            if (!defaultvalue.isBlank()) {
                buffer.append(" DEFAULT ");
                buffer.append(defaultvalue);
            }
            if (notnull) {
                buffer.append(" NOT NULL");
            }
            if (isautoincrement) {
                buffer.append(" ");
                buffer.append(autoincrement);
            }
            return buffer.toString();
        }
        public Object[] getQueryArguments(String table)
            throws java.sql.SQLException
        {
            List<String> buffer = new ArrayList<String>();
            buffer.add(table);
            buffer.add(oldidentifier);
            buffer.add(newidentifier);
            buffer.add(columntype.toString());
            if (defaultvalue.isBlank()) {
                buffer.add("");
            }
            else {
                buffer.add("DEFAULT " + defaultvalue);
            }
            if (notnull) {
                buffer.add("NOT NULL");
            }
            else {
                buffer.add("");
            }
            if (isautoincrement) {
                buffer.add(autoincrement);
            }
            else {
                buffer.add("");
            }
            return buffer.toArray(new String[0]);
        }
    }


    /** creates a SQL CREATE TABLE statement
     *
     * @param provider
     *      The driver provider.
     * @param property
     *      The descriptor of the new table.
     * @param table
     *      The name of the new table.
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *   The CREATE TABLE statement.
     * @throws java.sql.SQLException
     * @throws SQLException 
     * @throws IllegalArgumentException 
     * @throws WrappedTargetException 
     * @throws IndexOutOfBoundsException 
     * @throws UnknownPropertyException 
     */
    public static List<String> getCreateTableQueries(DriverProvider provider,
                                                     XPropertySet property,
                                                     String table,
                                                     ComposeRule rule,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException, WrappedTargetException, IndexOutOfBoundsException, UnknownPropertyException
    {
        String separator = ", ";
        boolean hasAutoIncrement = false;
        List<String> parts = new ArrayList<String>();
        List<String> queries = new ArrayList<String>();
        XIndexAccess columns = null;
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, property);
        if (supplier != null) {
            columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        }
        if (columns == null || columns.getCount() <= 0) {
            String message = String.format("The '%s' table has no columns, it is not possible to create the table", table);
            throw new SQLException(message);
        }
        // XXX: The first thing to do is to retrieve the columns
        // XXX: to find out if there are any auto-increment columns.
        int count = columns.getCount();
        for (int i = 0; i < count; i++) {
            XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
            if (descriptor == null) {
                continue;
            }
            ColumnProperties column = getStandardColumnProperties(provider, descriptor, sensitive);
            // FIXME: I had the descriptions that worked,
            // FIXME: it doesn't work anymore, I don't understand why?
            if (provider.supportsColumnDescription()) {
                String comment = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
                if (!comment.isEmpty()) {
                    String query = provider.getColumnDescriptionQuery(DBTools.composeColumnName(provider, table, column.newname, sensitive), comment);
                    queries.add(query);
                }
            }
            hasAutoIncrement |= column.isautoincrement;
            parts.add(column.toString());
        }
        // XXX: If the underlying driver allows it, we create the primary key in a DDL command
        // XXX: separate from the one that creates the table. But there are specific cases!!!
        boolean autopk = provider.isAutoIncrementIsPrimaryKey();
        boolean alterpk = provider.isPrimaryKeyAlterable();
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        if ((alterpk && !autopk) || (alterpk && autopk && !hasAutoIncrement)) {
            queries.addAll(0, getCreateConstraintQueries(provider, property, property, "", rule, sensitive));
        }
        // XXX: SQLite only allows creating PK with table creation and if there is no auto increment column (first test)
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        else if ((!alterpk && !hasAutoIncrement) || (alterpk && autopk && hasAutoIncrement)) {
            parts.addAll(DBConstraintHelper.getCreatePrimaryKeyParts(provider, property, sensitive));
        }
        queries.add(0, provider.getCreateTableQuery(table, String.join(separator, parts)));
        return queries;
    }

    /** creates a SQL Column part statement
    *
    * @param queries
    *      The queries to add to.
    * @param provider
    *      The driver provider.
    * @param table
    *      The table of the column.
    * @param descriptor1
    *      The descriptor of the old column.
    * @param descriptor2
    *      The descriptor of the new column.
    * @param alterpk
    *      Is the modified column primary key.
    * @param sensitive
    *      Is identifier case sensitive.
    * @return
    *      The binary result (ie: 1 -> renamed, 2 -> type changed ...)
    * @throws java.sql.SQLException
    * @throws SQLException 
    * @throws IllegalArgumentException 
    * @throws WrappedTargetException 
    * @throws IndexOutOfBoundsException 
    * @throws UnknownPropertyException 
    */
    // XXX: This method is called from 2 places:
    // XXX: - ColumnContainerBase.createColumn() for any new column.
    // XXX: - TableSuper.alterColumn() for already existing columns.
    public static byte getAlterColumnQueries(List<String> queries,
                                             DriverProvider provider,
                                             TableSuper<?> table,
                                             XPropertySet descriptor1,
                                             XPropertySet descriptor2,
                                             boolean alterpk,
                                             boolean sensitive)
        throws java.sql.SQLException, SQLException
    // TODO: see: libreoffice/connectivity/source/drivers/postgresql/
    // TODO: file: pq_xcolumns.cxx method: void alterColumnByDescriptor()
    // FIXME: Added the possibility of changing column type if the contained data can be cast
    // FIXME: Added the possibility of renaming a primary key
    {
        System.out.println("DBTableHelper.getAlterColumnQueries() Descriptor1: " + descriptor1);

        String query;
        // XXX: Result is binary result (ie: 1 -> renamed, 2 -> type changed ...)
        byte result = 0;
        String name = DBTools.composeTableName(provider, table, ComposeRule.InTableDefinitions, sensitive);
        String name1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.NAME);

        ColumnProperties column = getStandardColumnProperties(provider, name1, descriptor2, sensitive);
        Object[] arguments = column.getQueryArguments(name);
        if (name1.isEmpty()) {
            // Create a new column
            query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_COLUMN;
            queries.add(MessageFormat.format(query, name, column.toString()));
        }
        else {
            // Modify an existing column
            if (!name1.equals(column.newname)) {
                // Rename a column
                query = provider.getRenameColumnQuery(DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN_RENAME);
                queries.add(MessageFormat.format(query, arguments));
                result += 1;
            }

            String type1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.TYPENAME);
            String type2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.TYPENAME);
            boolean typechanged = !type2.equals(type1);
            boolean alldone = !provider.hasColumnSetTypeQuery();
            if (typechanged) {
                // FIXME: Does the underlying driver have a specific command to change the type?
                if (alldone) {
                    query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN;
                }
                else {
                    query = provider.getColumnSetTypeQuery();
                }
                queries.add(MessageFormat.format(query, arguments));
                result += 2;
            }
            alldone &= typechanged;
            boolean altercolumn = provider.isColumnPropertyAlterable();
            String default1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
            String default2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
            //FIXME: Primary key column don't have to handle Default property
            if (altercolumn && !alterpk && !alldone && !default2.equals(default1)) {
                if (default2.isBlank()) {
                    query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN_RESET_DEFAULT;
                    query = provider.getColumnResetDefaultQuery(query);
                }
                else {
                    query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN_SET_DEFAULT;
                }
                queries.add(MessageFormat.format(query, arguments));
                result += 4;
            }
            int nullable1 = DBTools.getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
            int nullable2 = DBTools.getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
            //FIXME: Primary key column don't have to handle Not Null property
            if (altercolumn && !alterpk && !alldone && (nullable2 != nullable1)) {
                if (nullable2 == ColumnValue.NO_NULLS) {
                    query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN_SET_NOT_NULL;
                }
                else {
                    query = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN_RESET_NOT_NULL;
                }
                queries.add(MessageFormat.format(query, arguments));
                result += 8;
            }
        }
        if (provider.supportsColumnDescription()) {
            String comment1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
            String comment2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            System.out.println("DBTableHelper.getAlterColumnQueries() 11 Comment1: " + comment1 + " - Comment2: " + comment2);
            if (!comment2.equals(comment1)) {
                StringBuilder buffer = new StringBuilder(name);
                buffer.append(".");
                buffer.append(column.newidentifier);
                String comment = provider.getStatement().enquoteLiteral(comment2);
                query = provider.getColumnDescriptionQuery(buffer.toString(), comment);
                queries.add(query);
                result += 16;
            }
        }
        for (String q: queries) {
            System.out.println("DBTableHelper.getAlterColumnQueries() Query: " + q);
        }
        return result;
    }

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                XPropertySet descriptor,
                                                                boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException
    {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(provider, newname, sensitive);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                String oldname,
                                                                XPropertySet descriptor,
                                                                boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException
    {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(provider, oldname, newname, sensitive);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    /** creates the standard sql statement for the column part of statement.
     * @param provider
     *      The driver provider.
     * @param descriptor
     *      The descriptor of the column.
     * @param sensitive
     *      Is identifier case sensitive.
     * @throws SQLException
     * @throws java.sql.SQLException 
     * @throws IllegalArgumentException 
     */

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                ColumnProperties column,
                                                                XPropertySet descriptor)
        throws java.sql.SQLException, SQLException, IllegalArgumentException
    {
        boolean isAutoIncrement = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISAUTOINCREMENT);
        String typename = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TYPENAME);
        int datatype = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
        int precision = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.PRECISION);
        int scale = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.SCALE);
        String autoIncrementValue = "";
        System.out.println("DBTableHelper.getStandardColumnPartQuery() 1 TYPENAME: " + typename + " - TYPE: " + datatype + " - PRECISION: " + precision + " - SCALE: " + scale);
        
        // Check if the user enter a specific string to create auto increment values
        XPropertySetInfo info = descriptor.getPropertySetInfo();
        if (info != null && DBTools.hasDescriptorProperty(info, PropertyIds.AUTOINCREMENTCREATION)) {
            autoIncrementValue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.AUTOINCREMENTCREATION);
            column.isautoincrement = !autoIncrementValue.isEmpty();
        }
        
        // Look if we have to use precisions (ie: SCALE).
        boolean useliteral = false;
        String prefix = "";
        String postfix = "";
        String createparams = "";
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getTypeInfo()){
            while (result.next()) {
                String typename2cmp = result.getString(1);
                int type2cmp = result.getShort(2);
                String value = result.getString(4);
                prefix = result.wasNull() ? prefix : value;
                value = result.getString(5);
                postfix = result.wasNull() ? postfix : value;
                value = result.getString(6);
                createparams = result.wasNull() ? createparams : value;
                // first identical type will be used if typename is empty
                if (typename.isEmpty() && type2cmp == datatype) {
                    typename = typename2cmp;
                }
                if (typename.equalsIgnoreCase(typename2cmp) && type2cmp == datatype && !createparams.isBlank() && !result.wasNull()) {
                    useliteral = true;
                    break;
                }
            }
        }
        int index = 0;
        if (column.isautoincrement && (index = typename.indexOf(autoIncrementValue)) != -1) {
            typename = typename.substring(0, index);
        }
        // XXX: For type that use precisions we need to compose the type name...
        if (useliteral && (precision > 0 || scale > 0)) {
            //FIXME: The original code coming from OpenOffice/main/connectivity/java check only for TIMESTAMP...
            //FIXME: Now all temporal SQL types with fraction of a second are taken into account.
            boolean timed = datatype == Types.TIME ||
                            datatype == Types.TIME_WITH_TIMEZONE ||
                            datatype == Types.TIMESTAMP ||
                            datatype == Types.TIMESTAMP_WITH_TIMEZONE;
            
            //FIXME: The original code coming from OpenOffice/main/connectivity/java search only for parenthesis...
            //FIXME: Now the insertion position takes into account the peculiarity of the data types WITH TIME ZONE
            int insert =    datatype == Types.TIME_WITH_TIMEZONE ||
                            datatype == Types.TIMESTAMP_WITH_TIMEZONE ?
                            typename.indexOf(' ') : typename.indexOf('(');
            if (insert == -1) {
                column.columntype.append(typename);
            }
            else {
                column.columntype.append(typename.substring(0, insert));
            }
            column.columntype.append('(');
            
            if (precision > 0 && !timed) {
                column.columntype.append(precision);
                if (scale > 0) {
                    column.columntype.append(',');
                }
            }
            if (scale > 0 || timed) {
                column.columntype.append(scale);
            }
            
            if (insert == -1) {
                column.columntype.append(')');
            }
            else {
                if (timed) {
                    column.columntype.append(')');
                }
                else {
                    insert = typename.indexOf(')', insert);
                }
                column.columntype.append(typename.substring(insert));
            }
        }
        // XXX: For type that don't use precisions simply add the type name
        else {
            column.columntype.append(typename);
        }

        // FIXME: Auto-increment take precedence on Default Value and Not Null property
        String defaultvalue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DEFAULTVALUE);
        int isnullable = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.ISNULLABLE);
        if (isAutoIncrement && column.isautoincrement) {
            column.autoincrement = autoIncrementValue;
        }
        else {
            if (isnullable == ColumnValue.NO_NULLS) {
                column.notnull = true;
            }
            if (!defaultvalue.isEmpty()) {
                column.defaultvalue = prefix + defaultvalue + postfix;
            }
        }
        return column;
    }

    /** creates the Primary or Foreign Key SQL ALTER TABLE statement.
     * @param provider
     *      The driver provider.
     * @param descriptor
     *      The descriptor of the new table.
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *      The keys parts.
     * @throws SQLException
     * @throws IndexOutOfBoundsException 
     * @throws UnknownPropertyException 
     * @throws WrappedTargetException 
     * @throws IllegalArgumentException 
     * @throws java.sql.SQLException 
     */

    private static List<String> getCreateConstraintQueries(DriverProvider provider,
                                                           XPropertySet descriptor,
                                                           XPropertySet property,
                                                           String name,
                                                           ComposeRule rule,
                                                           boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException, WrappedTargetException, UnknownPropertyException, IndexOutOfBoundsException
    {
        List<String> queries = new ArrayList<String>();
        String catalog = DBTools.getDescriptorStringValue(property, PropertyIds.CATALOGNAME);
        String schema = DBTools.getDescriptorStringValue(property, PropertyIds.SCHEMANAME);
        String table = DBTools.getDescriptorStringValue(property, PropertyIds.NAME);

        XKeysSupplier supplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = supplier.getKeys();
        if (keys != null) {
            for (int i = 0; i < keys.getCount(); i++) {
                XPropertySet key = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                if (key != null) {
                    queries.add(DBConstraintHelper.getCreateConstraintQuery(provider, key, catalog, schema, table, name, rule, sensitive));
                }
            }
        }
        return queries;
    }
}
