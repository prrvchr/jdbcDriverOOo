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
package io.github.prrvchr.jdbcdriver.helper;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class DBTableHelper
{

    private static class ColumnProperties
    {
        private String newname;
        private String oldidentifier = "";
        private String newidentifier;
        private StringBuilder columntype;
        private String defaultvalue;
        private boolean isautoincrement;
        private String autoincrement;
        private boolean notnull;
        private boolean isrowversion;

        ColumnProperties(DriverProvider provider, String name, boolean sensitive)
            throws java.sql.SQLException
        {
            this(DBTools.enquoteIdentifier(provider, name, sensitive), name);
        }
        ColumnProperties(DriverProvider provider, String name1, String name2, boolean sensitive)
            throws java.sql.SQLException
        {
            // XXX: If it's a new column then name1 is empty...
            this(DBTools.enquoteIdentifier(provider, name1.isBlank() ? name2 : name1, sensitive),
                 DBTools.enquoteIdentifier(provider, name2, sensitive),
                 name2);
        }
        private ColumnProperties(String identifier, String name)
                throws java.sql.SQLException
        {
            this(identifier, identifier, name);
        }
        private ColumnProperties(String identifier1, String identifier2, String name)
            throws java.sql.SQLException
        {
            newname = name;
            oldidentifier = identifier1;
            newidentifier = identifier2;
            columntype = new StringBuilder();
            defaultvalue = "";
            isautoincrement = false;
            autoincrement = "";
            notnull = false;
            isrowversion = false;
        }
        public String toString() {
            // XXX: We try to construct the Column part needed for Table creation
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
        public Object[] toArguments(String table)
            throws java.sql.SQLException
        {
            // XXX: We try to have arguments to be able to fill two query:
            // XXX: - ALTER TABLE {0} ALTER COLUMN {1} {3} {4} {5} {6}
            // XXX: - ALTER TABLE {0} ALTER COLUMN {1} RENAME TO {2}
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
        public boolean isRowVersion() {
            return isrowversion;
        }
        public boolean isAutoIncrement() {
            return isautoincrement;
        }
        public String getName() {
            return newidentifier;
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
     * @param type
     *      The type of the new table (ie: TABLE or TEXT TABLE).
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *   The CREATE TABLE statement.
     * @throws java.sql.SQLException
     * @throws SQLException 
     * @throws WrappedTargetException 
     * @throws IndexOutOfBoundsException 
     * @throws UnknownPropertyException 
     */
    public static List<String> getCreateTableQueries(DriverProvider provider,
                                                     XPropertySet property,
                                                     String table,
                                                     String type,
                                                     ComposeRule rule,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException, WrappedTargetException, IndexOutOfBoundsException, UnknownPropertyException
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
        List<String> columnversion = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
            if (descriptor == null) {
                continue;
            }
            ColumnProperties column = getStandardColumnProperties(provider, descriptor, sensitive);
            if (provider.supportsColumnDescription()) {
                String comment = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
                if (!comment.isEmpty()) {
                    queries.add(getColumnDescriptionQuery(provider, table, column.newname, comment, sensitive));
                }
            }
            hasAutoIncrement |= column.isAutoIncrement();
            if (column.isRowVersion()) {
                columnversion.add(column.getName());
            }
            parts.add(column.toString());
        }
        boolean versioning = !columnversion.isEmpty();
        if (versioning) {
            parts.add(provider.getSystemVersioningColumnQuery(columnversion));
        }
        // XXX: If the underlying driver allows it, we create the primary key in a DDL command
        // XXX: separate from the one that creates the table. But there are specific cases!!!
        boolean autopk = provider.isAutoIncrementIsPrimaryKey();
        boolean alterpk = provider.supportsAlterPrimaryKey();
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        if ((alterpk && !autopk) || (alterpk && autopk && !hasAutoIncrement)) {
            queries.addAll(0, getCreateConstraintQueries(provider, property, property, "", rule, sensitive));
        }
        // XXX: SQLite only allows creating PK with table creation and if there is no auto increment column (first test)
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        else if ((!alterpk && !hasAutoIncrement) || (alterpk && autopk && hasAutoIncrement)) {
            parts.addAll(DBConstraintHelper.getCreatePrimaryKeyParts(provider, property, sensitive));
        }
        queries.add(0, provider.getCreateTableQuery(type, table, String.join(separator, parts), versioning));
        return queries;
    }

    private static String getColumnDescriptionQuery(DriverProvider provider,
                                                    String table,
                                                    String column,
                                                    String comment,
                                                    boolean sensitive)
        throws java.sql.SQLException
    {
        String name = DBTools.composeColumnName(provider, table, column, sensitive);
        comment = provider.enquoteLiteral(comment);
        String query = provider.getColumnDescriptionQuery(name, comment);
        System.out.println("DBTableHelper.getCreateTableQueries() Comment: " + comment + " - Query: " + query);
        return query;
    }

    /** creates a SQL Column part statement
    *
    * @param queries
    *      The list of queries to fill.
    * @param provider
    *      The driver provider.
    * @param table
    *      The table of the column.
    * @param descriptor1
    *      The descriptor of the old column.
    * @param descriptor2
    *      The descriptor of the new column.
    * @param alterkey
    *      Is the modified column primary key.
    * @param sensitive
    *      Is identifier case sensitive.
    * @return
    *      The binary result (ie: 1 -> renamed, 2 -> type changed ...)
    * @throws java.sql.SQLException
    * @throws SQLException 
    */
    // XXX: This method is called from 2 places:
    // XXX: - ColumnContainerBase.createColumn() for any new column.
    // XXX: - TableSuper.alterColumn() for already existing columns.
    public static byte getAlterColumnQueries(List<String> queries,
                                             DriverProvider provider,
                                             TableSuper<?> table,
                                             XPropertySet descriptor1,
                                             XPropertySet descriptor2,
                                             boolean alterkey,
                                             boolean sensitive)
        throws java.sql.SQLException, SQLException
    // TODO: see: libreoffice/connectivity/source/drivers/postgresql/
    // TODO: file: pq_xcolumns.cxx method: void alterColumnByDescriptor()
    // XXX: Added the possibility of changing column type if the contained data can be cast
    // XXX: Added the possibility of renaming a primary key
    // XXX: Added the possibility of adding or removing Identity (auto increment on column)
    {

        String query;
        // XXX: Result is binary result (ie: 1 -> renamed, 2 -> type changed ...)
        byte result = 0;
        String name = DBTools.composeTableName(provider, table, ComposeRule.InTableDefinitions, sensitive);
        String name1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.NAME);

        ColumnProperties column = getStandardColumnProperties(provider, name1, descriptor2, sensitive);
        if (name1.isEmpty()) {
            // XXX: Create a new column
            query = provider.getAddColumnQuery(name, column.toString());
            queries.add(query);
        }
        else {
            // XXX: Modify an existing column
            Object[] arguments = column.toArguments(name);
            if (!name1.equals(column.newname)) {
                // Rename a column
                query = provider.getRenameColumnQuery();
                queries.add(DBTools.formatSQLQuery(query, arguments));
                result |= 1;
            }

            // XXX: Identity have been changed?
            boolean auto1 = DBTools.getDescriptorBooleanValue(descriptor1, PropertyIds.ISAUTOINCREMENT);
            boolean auto2 = DBTools.getDescriptorBooleanValue(descriptor2, PropertyIds.ISAUTOINCREMENT);
            boolean autochanged = auto1 != auto2;

            // XXX: Type have been changed?
            String type1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.TYPENAME);
            String type2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.TYPENAME);
            boolean typechanged = !type2.equals(type1);

            boolean alldone = false;
            List<String> parts = new ArrayList<String>();
            // XXX: We are forced to process these two changes together because some underlying drivers use the
            // XXX: same DDL command to change the type of a column or assign or remove an Identity constraint.
            if (typechanged || autochanged) {
                // XXX: Identity have been changed
                if (autochanged) {
                    // XXX: An Identity column have been set
                    if (auto2) {
                        // XXX: Does the underlying driver have a specific command to set Identity?
                        if (provider.hasAddIdentityQuery()) {
                            query =  provider.getAddIdentityQuery();
                        }
                        else {
                            query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN);
                            alldone = true;
                        }
                    }
                    // XXX: An Identity column have been drop
                    else {
                        query =  provider.getDropIdentityQuery();
                    }
                    parts.add(DBTools.formatSQLQuery(query, arguments));
                    result |= 2;
                }
                // XXX: type have been changed 
                if (typechanged) {
                    // XXX: Previous commands may have already changed the type?
                    if (!alldone) {
                        int index = parts.size();
                        // XXX: Does the underlying driver have a specific command to change the type?
                        if (provider.hasAlterColumnQuery()) {
                            query =  provider.getAlterColumnQuery();
                        }
                        else {
                            query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_TABLE_ALTER_COLUMN);
                        }
                        // XXX: If an Identity have been added we must first change the type
                        index = (autochanged && auto2) ? 0 : index;
                        parts.add(0, DBTools.formatSQLQuery(query, arguments));
                        alldone = true;
                    }
                    result |= 4;
                }
            }
            queries.addAll(parts);

            // XXX: Primary key & auto-increment column don't have to handle Default and Not Null property,
            // XXX: and some underlying driver doesn't support alteration of column.
            if (!alldone && !alterkey && !auto2 && provider.supportsAlterColumnProperty()) {
                String default1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
                String default2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
                boolean defaultchanged = !default2.equals(default1);
                if (defaultchanged) {
                    if (default2.isBlank()) {
                        query = provider.getColumnResetDefaultQuery();
                    }
                    else {
                        query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_COLUMN_SET_DEFAULT);
                    }
                    queries.add(DBTools.formatSQLQuery(query, arguments));
                    result |= 8;
                }

                int nullable1 = DBTools.getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
                int nullable2 = DBTools.getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
                boolean nullablechanged = nullable2 != nullable1;
                if (nullablechanged) {
                    if (nullable2 == ColumnValue.NO_NULLS) {
                        query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_COLUMN_SET_NOT_NULL);
                    }
                    else {
                        query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_COLUMN_DROP_NOT_NULL);
                    }
                    queries.add(DBTools.formatSQLQuery(query, arguments));
                    result |= 16;
                }
            }
        }

        if (provider.supportsColumnDescription()) {
            String comment1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
            String comment2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            if (!comment2.equals(comment1)) {
                queries.add(getColumnDescriptionQuery(provider, name, column.newidentifier, comment2, sensitive));
                result |= 32;
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
        
        // Check if the user enter a specific string to create auto increment values
        if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.AUTOINCREMENTCREATION)) {
            autoIncrementValue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.AUTOINCREMENTCREATION);
            column.isautoincrement = !autoIncrementValue.isEmpty();
        }
        // Check if the column is a row version (ie: column of system-versioned temporal tables)
        if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.ISROWVERSION)) {
            column.isrowversion = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISROWVERSION);
        }

        // Look if we have to use precisions (ie: SCALE).
        boolean useliteral = false;
        String createparams = "";
        try (java.sql.ResultSet result = provider.getTypeInfoResultSet())
        {
            while (result.next()) {
                String typename2cmp = result.getString(1);
                int type2cmp = result.getShort(2);
                createparams = result.getString(6);
                if (result.wasNull()) {
                    createparams= "";
                }
                // XXX: First identical type will be used if typename is empty
                if (typename.isEmpty() && type2cmp == datatype) {
                    typename = typename2cmp;
                }
                if (type2cmp == datatype && typename.equalsIgnoreCase(typename2cmp) && !createparams.isBlank()) {
                    useliteral = true;
                    break;
                }
            }
        }

        int index = 0;
        if (column.isautoincrement && (index = typename.indexOf(autoIncrementValue)) != -1) {
            typename = typename.substring(0, index);
        }
        // XXX: For type that use precision or scale we need to compose the type name...
        if (useliteral && (precision > 0 || scale > 0)) {
            //XXX: The original code coming from OpenOffice/main/connectivity/java check only for TIMESTAMP...
            //XXX: Now all temporal SQL types with fraction of a second are taken into account.
            boolean timed = datatype == Types.TIME ||
                            datatype == Types.TIME_WITH_TIMEZONE ||
                            datatype == Types.TIMESTAMP ||
                            datatype == Types.TIMESTAMP_WITH_TIMEZONE;
            
            //XXX: The original code coming from OpenOffice/main/connectivity/java search only for parenthesis...
            //XXX: Now the insertion position takes into account the peculiarity of the data types WITH TIME ZONE
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
        // XXX: For type that don't use precision or scale simply add the type name
        else {
            column.columntype.append(typename);
        }

        // XXX: Auto-increment take precedence on Default Value and Not Null property
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
                // FIXME: DefaultValue can't be quoted because if so we can't differentiate
                // FIXME: a not assigned defaultValue from an empty String for VARCHAR
                column.defaultvalue = defaultvalue;
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
        NamedComponents table = DBTools.getTableNamedComponents(provider, property);

        XKeysSupplier supplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = supplier.getKeys();
        if (keys != null) {
            for (int i = 0; i < keys.getCount(); i++) {
                XPropertySet key = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                if (key != null) {
                    queries.add(DBConstraintHelper.getCreateConstraintQuery(provider, key, table, name, rule, sensitive));
                }
            }
        }
        return queries;
    }
}
