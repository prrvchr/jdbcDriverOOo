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
package io.github.prrvchr.driver.helper;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.query.DDLParameter;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class TableHelper {

    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_IDENTITY = 2;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_DEFAULT_VALUE = 8;
    public static final int COLUMN_NULLABLE = 16;
    public static final int COLUMN_DESCRIPTION = 32;

    private static class ColumnProperties {
        private String mNewName;
        private String mOldIdentifier = "";
        private String mNewIdentifier;
        private StringBuilder mColumnType;
        private String mDefaultValue;
        private boolean mIsAutoincrement;
        private String mAutoincrement;
        private boolean mNotNull;
        private boolean mIsRowversion;

        private ColumnProperties(String identifier1, String identifier2, String name)
            throws java.sql.SQLException {
            mNewName = name;
            mOldIdentifier = identifier1;
            mNewIdentifier = identifier2;
            mColumnType = new StringBuilder();
            mDefaultValue = "";
            mIsAutoincrement = false;
            mAutoincrement = "";
            mNotNull = false;
            mIsRowversion = false;
        }

        private ColumnProperties(String identifier, String name)
                throws java.sql.SQLException {
            this(identifier, identifier, name);
        }

        ColumnProperties(DriverProvider provider, String name, boolean sensitive)
            throws java.sql.SQLException {
            this(provider.enquoteIdentifier(name, sensitive), name);
        }
        ColumnProperties(DriverProvider provider, String name1, String name2, boolean sensitive)
            throws java.sql.SQLException {
            // XXX: If it's a new column then name1 is empty...
            this(provider.enquoteIdentifier(getDefaultName(name1, name2), sensitive),
                 provider.enquoteIdentifier(name2, sensitive),
                 name2);
        }

        private static final String getDefaultName(String name1, String name2) {
            String dfltName = name1;
            if (name1.isBlank()) {
                dfltName = name2;
            } else {
                dfltName = name1;
            }
            return dfltName;
        }

        public String toString() {
            // XXX: We try to construct the Column part needed for Table creation
            StringBuilder builder = new StringBuilder(mNewIdentifier);
            builder.append(" ");
            builder.append(mColumnType.toString());
            if (!mDefaultValue.isBlank()) {
                builder.append(" DEFAULT ");
                builder.append(mDefaultValue);
            }
            if (mNotNull) {
                builder.append(" NOT NULL");
            }
            if (mIsAutoincrement) {
                builder.append(" ");
                builder.append(mAutoincrement);
            }
            return builder.toString();
        }

        public Map<String, Object> toArguments(String table)
            throws java.sql.SQLException {
            // XXX: We try to have arguments to be able to fill two query:
            // XXX: - ALTER TABLE ${TableName} ALTER COLUMN ${Column} ${Type} ${Default} ${Nullable} ${Autoincrement}
            // XXX: - ALTER TABLE ${TableName} ALTER COLUMN ${OldName} RENAME TO ${Column}
            return DDLParameter.getColumnProperties(table, mOldIdentifier, mNewIdentifier, mColumnType.toString(),
                                                    mDefaultValue, mNotNull, mIsAutoincrement,
                                                    mAutoincrement, toString());
        }

        public boolean isRowVersion() {
            return mIsRowversion;
        }

        public boolean isAutoIncrement() {
            return mIsAutoincrement;
        }

        public String getName() {
            return mNewIdentifier;
        }
    }

    /** creates a SQL CREATE TABLE statement.
     *
     * @param provider
     *      The driver provider.
     * @param property
     *      The descriptor of the new table.
     * @param table
     *      The name of the new table.
     * @param type
     *      The type of the new table (ie: TABLE or TEXT TABLE).
     * @param rule
     *      The rule for composing the table name.
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
        throws java.sql.SQLException, SQLException, WrappedTargetException,
               IndexOutOfBoundsException, UnknownPropertyException {
        XIndexAccess columns = null;
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, property);
        if (supplier != null) {
            columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        }
        if (columns == null || columns.getCount() <= 0) {
            String template = "The '%s' table has no columns, it is not possible to create the table";
            String message = String.format(template, table);
            throw new SQLException(message);
        }
        // XXX: The first thing to do is to retrieve the columns
        // XXX: to find out if there are any auto-increment columns.
        return getCreateTableQueries(provider, property, columns,
                                     table, type, rule, sensitive);
    }

    private static List<String> getCreateTableQueries(DriverProvider provider,
                                                      XPropertySet property,
                                                      XIndexAccess columns,
                                                      String table,
                                                      String type,
                                                      ComposeRule rule,
                                                      boolean sensitive)
        throws SQLException, java.sql.SQLException {
        List<String> queries = new ArrayList<>();
        List<String> parts = new ArrayList<String>();
        List<String> versions = new ArrayList<>();
        boolean hasAutoIncrement = setCreateTableColumn(provider, columns, queries, parts, versions, table, sensitive);
        boolean versioning = !versions.isEmpty() && provider.getDDLQuery().supportsSystemVersioning();
        if (versioning) {
            parts.add(provider.getDDLQuery().getSystemVersioningColumnQuery(versions));
        }
        // XXX: If the underlying driver allows it, we create the primary key in a DDL command
        // XXX: separate from the one that creates the table. But there are specific cases!!!
        boolean autopk = provider.isAutoIncrementIsPrimaryKey();
        boolean alterpk = provider.getDDLQuery().supportsAlterPrimaryKey();
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        if (alterpk && !autopk || isCreationSupported(alterpk, autopk, !hasAutoIncrement)) {
            queries.addAll(0, getCreateConstraintQueries(provider, property, property, "", rule, sensitive));
        // XXX: SQLite only allows creating PK with table creation and if there is no auto increment column (first test)
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        } else if (!alterpk && !hasAutoIncrement || isCreationSupported(alterpk, autopk, hasAutoIncrement)) {
            parts.addAll(ConstraintHelper.getCreatePrimaryKeyParts(provider, property, sensitive));
        }
        addCreateTableCommand(provider, queries, parts, type, table, versioning);
        return queries;
    }

    private static boolean setCreateTableColumn(final DriverProvider provider,
                                                final XIndexAccess columns,
                                                final List<String> queries,
                                                final List<String> parts,
                                                final List<String> versions,
                                                final String table,
                                                final boolean sensitive)
        throws SQLException, java.sql.SQLException {
        boolean hasAutoIncrement = false;
        try {
            for (int i = 0, count = columns.getCount(); i < count; i++) {
                XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                if (descriptor == null) {
                    continue;
                }
                hasAutoIncrement |= getCreateTableColumn(provider, queries, parts, versions,
                                                         descriptor, table, sensitive);
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException | IllegalArgumentException e) {
            throw new SQLException(e.getMessage());
        }
        return hasAutoIncrement;

    }
    private static void addCreateTableCommand(final DriverProvider provider,
                                              final List<String> queries,
                                              final List<String> parts,
                                              final String type,
                                              final String table,
                                              final boolean versioning) {
        Map<String, Object> arguments = new HashMap<>();
        String key = DDLParameter.setCreateTable(arguments, type, table, parts);
        queries.add(0, provider.getDDLQuery().getCreateTableCommand(arguments, versioning, key));
    }

    private static boolean getCreateTableColumn(DriverProvider provider,
                                                List<String> queries,
                                                List<String> parts,
                                                List<String> columnversion,
                                                XPropertySet descriptor,
                                                String table,
                                                boolean sensitive)
        throws IllegalArgumentException, SQLException, java.sql.SQLException {
        boolean hasAutoIncrement = false;
        ColumnProperties column = getStandardColumnProperties(provider, descriptor, sensitive);
        if (provider.getDDLQuery().supportsColumnDescription()) {
            String comment = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!comment.isEmpty()) {
                queries.add(getColumnDescriptionQuery(provider, table, column.mNewName, comment, sensitive));
            }
        }
        hasAutoIncrement |= column.isAutoIncrement();
        if (column.isRowVersion()) {
            columnversion.add(column.getName());
        }
        parts.add(column.toString());
        return hasAutoIncrement;
    }

    private static boolean isCreationSupported(boolean supportsAlterPk,
                                               boolean isAutoIncrementPk,
                                               boolean autoIncrement) {
        return supportsAlterPk && isAutoIncrementPk && autoIncrement;
    }

    private static String getColumnDescriptionQuery(DriverProvider provider,
                                                    String table,
                                                    String column,
                                                    String comment,
                                                    boolean sensitive)
        throws java.sql.SQLException {
        String name = DBTools.composeColumnName(provider, table, column, sensitive);
        comment = provider.enquoteLiteral(comment);
        Map<String, Object> arguments = DDLParameter.getColumnDescription(name, comment);
        String query = provider.getDDLQuery().getColumnDescriptionCommand(arguments);
        System.out.println("DBTableHelper.getCreateTableQueries() Comment: " + comment + " - Query: " + query);
        return query;
    }

    /** creates a SQL Column part statement.
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
    public static Integer getAlterColumnQueries(List<String> queries,
                                                DriverProvider provider,
                                                TableSuper table,
                                                XPropertySet descriptor1,
                                                XPropertySet descriptor2,
                                                boolean alterkey,
                                                boolean sensitive)
        throws java.sql.SQLException, SQLException {
    // XXX: see: libreoffice/connectivity/source/drivers/postgresql/
    // XXX: file: pq_xcolumns.cxx method: void alterColumnByDescriptor()
    // XXX: Added the possibility of changing column type if the contained data can be cast
    // XXX: Added the possibility of renaming a primary key
    // XXX: Added the possibility of adding or removing Identity (auto increment on column) {

        String query;
        // XXX: Result is binary result (ie: 1 -> renamed, 2 -> type changed ...)
        Integer result = 0;
        String name = DBTools.composeTableName(provider, table, ComposeRule.InTableDefinitions, sensitive);
        String name1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.NAME);

        ColumnProperties column = getStandardColumnProperties(provider, name1, descriptor2, sensitive);
        if (name1.isEmpty()) {
            // XXX: Create a new column
            query = provider.getDDLQuery().getAddColumnCommand(DDLParameter.getAddColumn(name, column.toString()));
            queries.add(query);
        } else {
            setAlterColumnQueries(provider, queries, column,
                                  descriptor1, descriptor2, alterkey, name, name1, result);
        }

        if (provider.getDDLQuery().supportsColumnDescription()) {
            String comment1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
            String comment2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            if (!comment2.equals(comment1)) {
                queries.add(getColumnDescriptionQuery(provider, name, column.mNewIdentifier, comment2, sensitive));
                result |= COLUMN_DESCRIPTION;
            }
        }
        for (String q: queries) {
            System.out.println("DBTableHelper.getAlterColumnQueries() Query: " + q);
        }
        return result;
    }

    private static void setAlterColumnQueries(DriverProvider provider,
                                              List<String> queries,
                                              ColumnProperties column,
                                              XPropertySet descriptor1,
                                              XPropertySet descriptor2,
                                              boolean alterkey,
                                              String table,
                                              String name,
                                              Integer result)
        throws java.sql.SQLException {
        // XXX: Modify an existing column
        Map<String, Object> arguments = column.toArguments(table);
        if (!name.equals(column.mNewName)) {
            setColumnName(provider, queries, arguments, result);
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
        // XXX: Identity have been changed
        if (autochanged) {
            alldone = setColumnIdentity(provider, queries, parts, arguments, auto2, result);
        }
        // XXX: type have been changed 
        if (typechanged) {
            alldone = setColumnType(provider, parts, arguments, autochanged, auto2, alldone, result);
        }
        queries.addAll(parts);

        // XXX: Primary key & auto-increment column don't have to handle Default and Not Null property,
        // XXX: and some underlying driver doesn't support alteration of column.
        if (!alldone && !alterkey && !auto2 && provider.getDDLQuery().supportsAlterColumnProperties()) {
            String default1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
            String default2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
            boolean defaultchanged = !default2.equals(default1);
            if (defaultchanged) {
                setColumnDefaultValue(provider, queries, arguments, default2, result);
            }

            int nullable1 = DBTools.getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
            int nullable2 = DBTools.getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
            boolean nullablechanged = nullable2 != nullable1;
            if (nullablechanged) {
                setColumnNullable(provider, queries, arguments, nullable2, result);
            }
        }
    }

    private static void setColumnName(DriverProvider provider,
                                      List<String> queries,
                                      Map<String, Object> arguments,
                                      Integer result) {
        // Rename a column
        String query = provider.getDDLQuery().getRenameColumnCommand(arguments);
        queries.add(query);
        result |= COLUMN_NAME;
    }

    private static boolean setColumnIdentity(DriverProvider provider,
                                             List<String> queries,
                                             List<String> parts,
                                             Map<String, Object> arguments,
                                             boolean autoincrement,
                                             Integer result) {

        String query;
        boolean alldone = false;
        // XXX: An Identity column have been set
        if (autoincrement) {
            // XXX: Does the underlying driver have a specific command to set Identity?
            if (provider.getDDLQuery().hasColumnAddIdentityCommand()) {
                query =  provider.getDDLQuery().getColumnAddIdentityCommand(arguments);
            } else {
                query = provider.getDDLQuery().getAlterColumnCommand(arguments);
                alldone = true;
            }
        // XXX: An Identity column have been drop
        } else {
            query =  provider.getDDLQuery().getColumnDropIdentityCommand(arguments);
        }
        parts.add(query);
        result |= COLUMN_IDENTITY;
        return alldone;
    }

    private static boolean setColumnType(DriverProvider provider,
                                         List<String> parts,
                                         Map<String, Object> arguments,
                                         boolean autochanged,
                                         boolean autoincrement,
                                         boolean alldone,
                                         Integer result) {
        // XXX: Previous commands may have already changed the type?
        if (!alldone) {
            // XXX: Does the underlying driver have a specific command to change the type?
            String query = null;
            if (provider.getDDLQuery().hasColumnSetDataTypeCommand()) {
                query = provider.getDDLQuery().getColumnSetDataTypeCommand(arguments);
            } else if (provider.getDDLQuery().hasAlterColumnCommand()) {
                query =  provider.getDDLQuery().getAlterColumnCommand(arguments);
            }
            if (query != null) {
                // XXX: If an Identity have been added we must first change the type
                int index = parts.size();
                if (autochanged && autoincrement) {
                    index = 0;
                }
                parts.add(index, query);
            }
            alldone = true;
        }
        result |= COLUMN_TYPE;
        return alldone;
    }

    private static void setColumnDefaultValue(DriverProvider provider,
                                              List<String> queries,
                                              Map<String, Object> arguments,
                                              String defaultvalue,
                                              Integer result) {
        String query;
        if (defaultvalue.isBlank()) {
            query = provider.getDDLQuery().getColumnDropDefaultCommand(arguments);
        } else {
            query = provider.getDDLQuery().getColumnSetDefaultCommand(arguments);
        }
        queries.add(query);
        result |= COLUMN_DEFAULT_VALUE;
    }

    private static void setColumnNullable(DriverProvider provider,
                                          List<String> queries,
                                          Map<String, Object> arguments,
                                          int nullable,
                                          Integer result) {
        String query;
        if (nullable == ColumnValue.NO_NULLS) {
            query = provider.getDDLQuery().getColumnSetNotNullCommand(arguments);
        } else {
            query = provider.getDDLQuery().getColumnDropNotNullCommand(arguments);
        }
        queries.add(query);
        result |= COLUMN_NULLABLE;
    }

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                XPropertySet descriptor,
                                                                boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(provider, newname, sensitive);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                String oldname,
                                                                XPropertySet descriptor,
                                                                boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(provider, oldname, newname, sensitive);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    /** creates the standard sql statement for the column part of statement.
     * @param provider
     *      The driver provider.
     * @param column
     *      Column properties.
     * @param descriptor
     *      The descriptor of the column.
     * @return
     *      The column properties.
     * @throws SQLException
     * @throws java.sql.SQLException 
     * @throws IllegalArgumentException 
     */

    private static ColumnProperties getStandardColumnProperties(DriverProvider provider,
                                                                ColumnProperties column,
                                                                XPropertySet descriptor)
        throws java.sql.SQLException, SQLException, IllegalArgumentException {
        boolean isAutoIncrement = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISAUTOINCREMENT);
        String typename = DBTools.getDescriptorStringValue(descriptor, PropertyIds.TYPENAME);
        int datatype = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
        int precision = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.PRECISION);
        int scale = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.SCALE);
        String autoIncrementValue = "";
        
        // Check if the user enter a specific string to create auto increment values
        if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.AUTOINCREMENTCREATION)) {
            autoIncrementValue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.AUTOINCREMENTCREATION);
            column.mIsAutoincrement = !autoIncrementValue.isEmpty();
        }
        // Check if the column is a row version (ie: column of system-versioned temporal tables)
        if (DBTools.hasDescriptorProperty(descriptor, PropertyIds.ISROWVERSION)) {
            column.mIsRowversion = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISROWVERSION);
        }

        // Look if we have to use precisions (ie: SCALE).
        boolean useliteral = useLiteral(provider, typename, datatype);

        int index = typename.indexOf(autoIncrementValue);
        if (column.mIsAutoincrement && index != -1) {
            typename = typename.substring(0, index);
        }
        // XXX: For type that use precision or scale we need to compose the type name...
        if (useliteral && (precision > 0 || scale > 0)) {
            //XXX: The original code coming from OpenOffice/main/connectivity/java check only for TIMESTAMP...
            //XXX: Now all temporal SQL types with fraction of a second are taken into account.
            setStandardColumnProperties(column, typename, datatype, precision, scale);
        // XXX: For type that don't use precision or scale simply add the type name
        } else {
            column.mColumnType.append(typename);
        }

        // XXX: Auto-increment take precedence on Default Value and Not Null property
        String defaultvalue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DEFAULTVALUE);
        int isnullable = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.ISNULLABLE);
        if (isAutoIncrement && column.mIsAutoincrement) {
            column.mAutoincrement = autoIncrementValue;
        } else {
            setDefaultAndNotNull(column, defaultvalue, isnullable);
        }
        return column;
    }

    private static final boolean useLiteral(DriverProvider provider, String typename, int datatype)
        throws java.sql.SQLException {
        boolean useliteral = false;
        String createparams = "";
        try (java.sql.ResultSet result = provider.getTypeInfoResultSet()) {
            while (result.next()) {
                String typename2cmp = result.getString(1);
                int type2cmp = result.getShort(2);
                // CHECKSTYLE:OFF: MagicNumber - Specific for database
                createparams = result.getString(6);
                // CHECKSTYLE:ON: MagicNumber - Specific for database
                if (result.wasNull()) {
                    createparams = "";
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
        return useliteral;
    }

    private static void setStandardColumnProperties(ColumnProperties column,
                                                    String typename,
                                                    int datatype,
                                                    int precision,
                                                    int scale) {
        //XXX: The original code coming from OpenOffice/main/connectivity/java check only for TIMESTAMP...
        //XXX: Now all temporal SQL types with fraction of a second are taken into account.
        boolean timed = isTimedDataType(datatype);
        
        //XXX: The original code coming from OpenOffice/main/connectivity/java search only for parenthesis...
        //XXX: Now the insertion position takes into account the peculiarity of the data types WITH TIME ZONE
        int insert;
        if (isTimeZonedDataType(datatype)) {
            insert = typename.indexOf(' ');
        } else {
            insert = typename.indexOf('(');
        }
        if (insert == -1) {
            column.mColumnType.append(typename);
        } else {
            column.mColumnType.append(typename.substring(0, insert));
        }
        column.mColumnType.append('(');
        
        if (precision > 0 && !timed) {
            column.mColumnType.append(precision);
            if (scale > 0) {
                column.mColumnType.append(',');
            }
        }
        if (scale > 0 || timed) {
            column.mColumnType.append(scale);
        }
        
        if (insert == -1) {
            column.mColumnType.append(')');
        } else {
            if (timed) {
                column.mColumnType.append(')');
            } else {
                insert = typename.indexOf(')', insert);
            }
            column.mColumnType.append(typename.substring(insert));
        }
    }

    private static boolean isTimedDataType(int datatype) {
        return datatype == Types.TIME ||
               datatype == Types.TIME_WITH_TIMEZONE ||
               datatype == Types.TIMESTAMP ||
               datatype == Types.TIMESTAMP_WITH_TIMEZONE;
    }

    private static boolean isTimeZonedDataType(int datatype) {
        return datatype == Types.TIME_WITH_TIMEZONE || datatype == Types.TIMESTAMP_WITH_TIMEZONE;
    }

    private static void setDefaultAndNotNull(ColumnProperties column, String defaultvalue, int isnullable) {
        if (isnullable == ColumnValue.NO_NULLS) {
            column.mNotNull = true;
        }
        if (!defaultvalue.isEmpty()) {
            // FIXME: DefaultValue can't be quoted because if so we can't differentiate
            // FIXME: a not assigned defaultValue from an empty String for VARCHAR
            column.mDefaultValue = defaultvalue;
        }
    }

    /** creates the Primary or Foreign Key SQL ALTER TABLE statement.
     * @param provider
     *      The driver provider.
     * @param descriptor
     *      The descriptor of the new table.
     * @param property
     *      The property of the new table.
     * @param name
     *      The name of the new table.
     * @param rule
     *      The naming rule for the new table.
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *      The keys parts.
     * @throws SQLException
     * @throws java.sql.SQLException 
     */

    private static List<String> getCreateConstraintQueries(DriverProvider provider,
                                                           XPropertySet descriptor,
                                                           XPropertySet property,
                                                           String name,
                                                           ComposeRule rule,
                                                           boolean sensitive)
        throws java.sql.SQLException, SQLException {
        List<String> queries = new ArrayList<String>();
        NamedComponents table = DBTools.getTableNamedComponents(provider, property);

        XKeysSupplier supplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = supplier.getKeys();
        try {
            if (keys != null) {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet key = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (key != null) {
                        queries.add(ConstraintHelper.getCreateConstraintQuery(provider, key, table,
                                                                              name, rule, sensitive));
                    }
                }
            }
        } catch (IllegalArgumentException | WrappedTargetException | IndexOutOfBoundsException e) {
            throw new SQLException(e.getMessage());
        }
        return queries;
    }
}
