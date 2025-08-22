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
package io.github.prrvchr.uno.driver.helper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;


public class TableHelper {

    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_IDENTITY = 2;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_DEFAULT_VALUE = 8;
    public static final int COLUMN_NULLABLE = 16;
    public static final int COLUMN_DESCRIPTION = 32;

    public static class ColumnProperties {
        private String mOldName;
        private String mNewName;
        private StringBuilder mColumnType;
        private String mDefaultValue;
        private boolean mIsAutoincrement;
        private String mAutoincrement;
        private boolean mNotNull;
        private boolean mIsRowversion;

        private ColumnProperties(String name) {
            this(name, name);
        }
        private ColumnProperties(String oldname, String newname) {
            mOldName = getDefaultName(oldname, newname);
            mNewName = newname;
            mColumnType = new StringBuilder();
            mDefaultValue = "";
            mIsAutoincrement = false;
            mAutoincrement = "";
            mNotNull = false;
            mIsRowversion = false;
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

        public Map<String, Object> toArguments(Provider provider,
                                               NamedComponent component,
                                               ComposeRule rule,
                                               boolean sensitive)
            throws SQLException {
            // XXX: We try to have arguments to be able to fill two query:
            // XXX: - ALTER TABLE ${TableName} ALTER COLUMN ${Column} ${Type} ${Default} ${Nullable} ${Autoincrement}
            // XXX: - ALTER TABLE ${TableName} ALTER COLUMN ${OldName} RENAME TO ${Column}
            return ParameterDDL.getColumnProperties(provider.getNamedSupport(rule), component, this, sensitive);
        }

        public boolean isRowVersion() {
            return mIsRowversion;
        }

        public boolean isAutoIncrement() {
            return mIsAutoincrement;
        }

        public String getAutoIncrement() {
            return mAutoincrement;
        }

        public String getType() {
            return mColumnType.toString();
        }

        public String getDefaultValue() {
            return mDefaultValue;
        }

        public boolean getNotNull() {
            return mNotNull;
        }

        public String getName() {
            return mOldName;
        }

        public String getNewName() {
            return mNewName;
        }

    }

    /** creates a SQL CREATE TABLE statement.
     *
     * @param provider
     *      The driver provider.
     * @param property
     *      The descriptor of the new table.
     * @param rule
     *      The rule for composing the table name.
     * @param type
     *      The type of the new table (ie: TABLE or TEXT TABLE).
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *   The CREATE TABLE statement.
     * @throws java.sql.SQLException
     */
    public static List<String> getCreateTableQueries(Provider provider,
                                                     XPropertySet property,
                                                     ComposeRule rule,
                                                     String type,
                                                     boolean sensitive)
        throws SQLException {
        XIndexAccess columns = null;
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, property);
        if (supplier != null) {
            columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        }
        if (columns == null || columns.getCount() <= 0) {
            String template = "The '%s' table has no columns, it is not possible to create the table";
            String table = ComponentHelper.composeTableName(provider.getNamedSupport(rule), property, sensitive);
            String message = String.format(template, table);
            throw new SQLException(message);
        }
        // XXX: The first thing to do is to retrieve the columns
        // XXX: to find out if there are any auto-increment columns.
        return getCreateTableQueries(provider, property, rule, columns, type, sensitive);
    }

    private static List<String> getCreateTableQueries(Provider provider,
                                                      XPropertySet property,
                                                      ComposeRule rule,
                                                      XIndexAccess columns,
                                                      String type,
                                                      boolean sensitive)
        throws SQLException {
        List<String> queries = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        List<String> versions = new ArrayList<>();
        boolean hasAutoIncrement = setCreateTableColumn(provider, property, rule, columns,
                                                        queries, parts, versions, sensitive);
        boolean versioning = !versions.isEmpty() && provider.getConfigDDL().supportsSystemVersioning();
        if (versioning) {
            parts.add(provider.getConfigDDL().getSystemVersioningColumnQuery(versions));
        }
        // XXX: If the underlying driver allows it, we create the primary key in a DDL command
        // XXX: separate from the one that creates the table. But there are specific cases!!!
        boolean autopk = provider.getConfigDDL().isAutoIncrementIsPrimaryKey();
        boolean alterpk = provider.getConfigDDL().supportsAlterPrimaryKey();
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        if (alterpk && !autopk || isCreationSupported(alterpk, autopk, !hasAutoIncrement)) {
            queries.addAll(0, getCreateConstraintQueries(provider, property, property, "", rule, sensitive));
        // XXX: SQLite only allows creating PK with table creation and if there is no auto increment column (first test)
        // XXX: MariaDB only permit to create auto increment if the PK is created while the table creation (second test)
        } else if (!alterpk && !hasAutoIncrement || isCreationSupported(alterpk, autopk, hasAutoIncrement)) {
            parts.addAll(ConstraintHelper.getCreatePrimaryKeyParts(provider.getNamedSupport(rule),
                                                                   property, sensitive));
        }
        addCreateTableCommand(provider, property, rule, queries, parts, type, versioning, sensitive);
        return queries;
    }

    private static boolean setCreateTableColumn(final Provider provider,
                                                final XPropertySet property,
                                                final ComposeRule rule,
                                                final XIndexAccess columns,
                                                final List<String> queries,
                                                final List<String> parts,
                                                final List<String> versions,
                                                final boolean sensitive)
        throws SQLException {
        boolean hasAutoIncrement = false;
        try {
            for (int i = 0, count = columns.getCount(); i < count; i++) {
                XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                if (descriptor == null) {
                    continue;
                }
                hasAutoIncrement |= getCreateTableColumn(provider, property, rule, queries, parts, versions,
                                                         descriptor, sensitive);
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException | IllegalArgumentException e) {
            throw new SQLException(e.getMessage());
        }
        return hasAutoIncrement;
    }

    private static void addCreateTableCommand(final Provider provider,
                                              final XPropertySet property,
                                              final ComposeRule rule,
                                              final List<String> queries,
                                              final List<String> parts,
                                              final String type,
                                              final boolean versioning,
                                              final boolean sensitive)
        throws SQLException {
        Map<String, Object> arguments = new HashMap<>();
        String key = ParameterDDL.setCreateTable(arguments, provider.getNamedSupport(rule),
                                                 property, type, parts, sensitive);
        queries.add(0, provider.getConfigDDL().getCreateTableCommand(arguments, versioning, key));
    }

    private static boolean getCreateTableColumn(final Provider provider,
                                                final XPropertySet property,
                                                final ComposeRule rule,
                                                final List<String> queries,
                                                final List<String> parts,
                                                final List<String> columnversion,
                                                final XPropertySet descriptor,
                                                boolean sensitive)
        throws SQLException {
        boolean hasAutoIncrement = false;
        ColumnProperties column = getStandardColumnProperties(provider, descriptor);
        if (provider.getConfigDDL().supportsColumnDescription()) {
            String comment = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            if (!comment.isEmpty()) {
                NamedComponent component = ComponentHelper.getTableNamedComponents(property);
                queries.add(getColumnDescriptionQuery(provider, component, rule, column, comment, sensitive));
            }
        }
        hasAutoIncrement |= column.isAutoIncrement();
        if (column.isRowVersion()) {
            columnversion.add(column.getNewName());
        }
        parts.add(ParameterDDL.getColumnDescription(column));
        return hasAutoIncrement;
    }

    private static boolean isCreationSupported(boolean supportsAlterPk,
                                               boolean isAutoIncrementPk,
                                               boolean autoIncrement) {
        return supportsAlterPk && isAutoIncrementPk && autoIncrement;
    }

    private static String getColumnDescriptionQuery(final Provider provider,
                                                    final NamedComponent component,
                                                    final ComposeRule rule,
                                                    final ColumnProperties column,
                                                    final String comment,
                                                    final boolean sensitive)
        throws SQLException {
        Map<String, Object> arguments = ParameterDDL.getColumnDescription(provider.getNamedSupport(rule), component,
                                                                          column.getNewName(), comment, sensitive);
        String query = provider.getConfigDDL().getColumnDescriptionCommand(arguments);
        return query;
    }

    /** Creates a SQL Add Column DDL query.
    *
    * @param queries
    *      The list of queries to fill.
    * @param provider
    *      The driver provider.
    * @param component
    *      The table NamedComponent.
    * @param rule
    *      The table name rule.
    * @param descriptor
    *      The descriptor of the new column.
    * @param sensitive
    *      Is identifier case sensitive.
    * @throws java.sql.SQLException 
    */
    // XXX: This method is called from:
    // XXX: - ColumnContainerBase.createColumn() for any new column.
    public static void getAddColumnQueries(List<String> queries,
                                           Provider provider,
                                           NamedComponent component,
                                           ComposeRule rule,
                                           XPropertySet descriptor,
                                           boolean sensitive)
        throws SQLException {
        // XXX: Create a new column
        ColumnProperties column = getStandardColumnProperties(provider, "", descriptor);
        Map<String, Object> arguments = ParameterDDL.getAddColumn(provider.getNamedSupport(rule),
                                                                  component, column, sensitive);
        String query = provider.getConfigDDL().getAddColumnCommand(arguments);
        queries.add(query);
        if (provider.getConfigDDL().supportsColumnDescription()) {
            String comment = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DESCRIPTION);
            queries.add(getColumnDescriptionQuery(provider, component, rule, column, comment, sensitive));
        }
    }

    /** Has the column identity changed.
    *
    * @param flags
    *      The column change status.
    * @param property
    *      The property to check change.
    * @return
    *      The change status as boolean
    */
    public static boolean hasPropertyChanged(int flags, int property) {
        return hasColumnProperty(flags, property);
    }


    /** Has the column identity changed.
    *
    * @param flags
    *      The column change status.
    * @return
    *      The change status as boolean
    */
    public static boolean hasColumnIdentityChanged(int flags) {
        return hasColumnProperty(flags, COLUMN_IDENTITY);
    }

    /** Has the column identity changed.
    *
    * @param flags
    *      The column change status.
    * @return
    *      The change status as boolean
    */
    public static boolean hasColumnTypeChanged(int flags) {
        return hasColumnProperty(flags, COLUMN_TYPE);
    }

    /** Get changes as byte.
    *
    * @param descriptor1
    *      The old column descriptor.
    * @param descriptor2
    *      The new column descriptor.
    * @param oldname
    *      The old the column name.
    * @param newname
    *      The new the column name.
    * @return
    *      The binary status (ie: 1 -> renamed, 2 -> type changed ...) as int
    */
    public static int getAlterColumnChanges(XPropertySet descriptor1,
                                            XPropertySet descriptor2,
                                            String oldname,
                                            String newname) {
        int changes = 0;
        // XXX: Column name have been changed?
        if (!oldname.equals(newname)) {
            changes |= COLUMN_NAME;
        }
        // XXX: Identity have been changed?
        boolean auto1 = DBTools.getDescriptorBooleanValue(descriptor1, PropertyIds.ISAUTOINCREMENT);
        boolean auto2 = DBTools.getDescriptorBooleanValue(descriptor2, PropertyIds.ISAUTOINCREMENT);
        if (auto1 != auto2) {
            changes |= COLUMN_IDENTITY;
        }
        // XXX: Type have been changed?
        String type1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.TYPENAME);
        String type2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.TYPENAME);
        if (!type2.equals(type1)) {
            changes |= COLUMN_TYPE;
        }
        // XXX: Column default value have been changed?
        String default1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
        String default2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
        if (!default2.equals(default1)) {
            changes |= COLUMN_DEFAULT_VALUE;
        }
        // XXX: Column nullable constraint have been changed?
        int nullable1 = DBTools.getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
        int nullable2 = DBTools.getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
        if (nullable2 != nullable1) {
            changes |= COLUMN_NULLABLE;
        }
        // XXX: Column description have been changed?
        String comment1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
        String comment2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
        if (!comment2.equals(comment1)) {
            changes |= COLUMN_DESCRIPTION;
        }
        return changes;
    }


    /** creates a SQL Column part statement.
    *
    * @param queries
    *      The list of queries to fill.
    * @param provider
    *      The driver provider.
    * @param component
    *      The component of the table.
    * @param rule
    *      The rule of the component.
    * @param oldname
    *      The old column name.
    * @param descriptor1
    *      The descriptor of the old column.
    * @param descriptor2
    *      The descriptor of the new column.
    * @param flags
    *      The column changed properties flags.
    * @param alterkey
    *      Is the modified column primary or foreign key.
    * @param sensitive
    *      Is identifier case sensitive.
    * @return
    *      The binary result (ie: 1 -> renamed, 2 -> type changed ...)
    * @throws java.sql.SQLException
    */
    // XXX: This method is called from:
    // XXX: - TableSuper.alterColumn() for already existing columns.
    public static Integer getAlterColumnQueries(List<String> queries,
                                                Provider provider,
                                                NamedComponent component,
                                                ComposeRule rule,
                                                String oldname,
                                                XPropertySet descriptor1,
                                                XPropertySet descriptor2,
                                                int flags,
                                                boolean alterkey,
                                                boolean sensitive)
        throws SQLException {
        // XXX: see: libreoffice/connectivity/source/drivers/postgresql/
        // XXX: file: pq_xcolumns.cxx method: void alterColumnByDescriptor()
        // XXX: Added the possibility of changing column type if the contained data can be cast
        // XXX: Added the possibility of renaming a primary key
        // XXX: Added the possibility of adding or removing Identity (auto increment on column) {
        ColumnProperties column = getStandardColumnProperties(provider, oldname, descriptor2);
        int result = setAlterColumnQueries(provider, component, rule, queries, column, descriptor2, flags,
                                           alterkey, sensitive);
        // XXX: Column description have been changed?
        if (hasPropertyChanged(flags, COLUMN_DESCRIPTION) && provider.getConfigDDL().supportsColumnDescription()) {
            String comment = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            queries.add(getColumnDescriptionQuery(provider, component, rule, column, comment, sensitive));
            result |= COLUMN_DESCRIPTION;
        }
        return result;
    }

    private static boolean hasColumnProperty(int flags, int property) {
        return (flags & property) == property;
    }

    private static int setAlterColumnQueries(Provider provider,
                                             NamedComponent component,
                                             ComposeRule rule,
                                             List<String> queries,
                                             ColumnProperties column,
                                             XPropertySet descriptor,
                                             int flags,
                                             boolean alterkey,
                                             boolean sensitive)
        throws SQLException {

        // XXX: Modify an existing column
        int results = 0;
        boolean autoincrement = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISAUTOINCREMENT);
        Map<String, Object> arguments = column.toArguments(provider, component, rule, sensitive);

        // XXX: Column name have changed
        if (hasPropertyChanged(flags, COLUMN_NAME)) {
            results |= setColumnName(provider, queries, arguments);
        }

        List<String> parts = new ArrayList<>();
        // XXX: We are forced to process these two changes together because some underlying drivers use the
        // XXX: same DDL command to change the type of a column or assign or remove an Identity constraint.
        // XXX: Identity have been changed
        if (hasPropertyChanged(flags, COLUMN_IDENTITY)) {
            results |= setColumnIdentity(provider, queries, parts, arguments, autoincrement);
        }
        // XXX: type have been changed 
        if (hasPropertyChanged(flags, COLUMN_TYPE)) {
            results |= setColumnType(provider, parts, arguments, results, autoincrement);
        }
        
        if (!isPropertiesSet(flags, results, COLUMN_IDENTITY, COLUMN_TYPE) &&
            provider.getConfigDDL().hasAlterColumnCommand()) {
            queries.add(provider.getConfigDDL().getAlterColumnCommand(arguments));
            results = updateResults(flags, results, COLUMN_IDENTITY, COLUMN_TYPE);
        } else if (!parts.isEmpty()) {
            queries.addAll(parts);
        }

        // XXX: Primary key & auto-increment column don't have to handle Default and Not Null property,
        // XXX: and some underlying driver doesn't support alteration of column.
        if (!alterkey && !autoincrement && !isAllPropertiesSet(flags, results, COLUMN_DESCRIPTION)) {
            results |= setColumnProperties(provider, descriptor, queries, arguments, flags);
        }
        return results;
    }

    private static boolean isPropertiesSet(int flags, int results, int... properties) {
        boolean isset = true;
        int i = 0;
        while (i < properties.length && isset) {
            int property = properties[i++];
            if (hasColumnProperty(flags, property) && !hasColumnProperty(results, property)) {
                isset = false;
            }
        }
        return isset;
    }

    private static boolean isAllPropertiesSet(int flags, int results, int... discarding) {
        int discarted = IntStream.of(discarding).sum();
        return (flags | discarted) == (results | discarted);
    }

    private static int updateResults(int flags, int results, int... properties) {
        for (int property : properties) {
            if (hasColumnProperty(flags, property)) {
                results |= property;
            }
        }
        return results;
    }

    private static int setColumnProperties(Provider provider,
                                           XPropertySet descriptor,
                                           List<String> queries,
                                           Map<String, Object> arguments,
                                           int flags) {
        int result = 0;
        if (hasPropertyChanged(flags, COLUMN_DEFAULT_VALUE)) {
            result |= setColumnDefaultValue(provider, descriptor, queries, arguments);
        }

        if (hasPropertyChanged(flags, COLUMN_NULLABLE)) {
            result |= setColumnNullable(provider, descriptor, queries, arguments);
        }
        return result;
    }

    private static int setColumnName(Provider provider,
                                      List<String> queries,
                                      Map<String, Object> arguments) {
        // Rename a column
        int result = 0;
        if (provider.getConfigDDL().hasAlterColumnNameCommand()) {
            queries.add(provider.getConfigDDL().getAlterColumnNameCommand(arguments));
            result = COLUMN_NAME;
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static int setColumnIdentity(Provider provider,
                                         List<String> queries,
                                         List<String> parts,
                                         Map<String, Object> arguments,
                                         boolean autoincrement) {

        // XXX: An Identity column have been set
        String query = null;
        int result = 0;
        if (!autoincrement) {
            // XXX: An Identity column have been drop
            query =  provider.getConfigDDL().getColumnDropIdentityCommand(arguments);
        } else if (provider.getConfigDDL().hasColumnAddIdentityCommand()) {
            // XXX: Does the underlying driver have a specific command to set Identity?
            query =  provider.getConfigDDL().getColumnAddIdentityCommand(arguments);
        }
        if (query != null) {
            parts.add(query);
            result = COLUMN_IDENTITY;
        }
        return result;
    }

    private static int setColumnType(Provider provider,
                                     List<String> parts,
                                     Map<String, Object> arguments,
                                     int results,
                                     boolean autoincrement) {
        // XXX: Does the underlying driver have a specific command to change the type?
        int result = 0;
        String query = null;
        if (provider.getConfigDDL().hasAlterColumnTypeCommand()) {
            query = provider.getConfigDDL().getAlterColumnTypeCommand(arguments);
        } else if (provider.getConfigDDL().hasAlterColumnCommand()) {
            query =  provider.getConfigDDL().getAlterColumnCommand(arguments);
        }
        if (query != null) {
            // XXX: If an Identity have been added we must first change the type
            int index = parts.size();
            if (hasPropertyChanged(results, COLUMN_IDENTITY) && autoincrement) {
                index = 0;
            }
            parts.add(index, query);
            result = COLUMN_TYPE;
        }
        return result;
    }

    private static int setColumnDefaultValue(Provider provider,
                                             XPropertySet descriptor,
                                             List<String> queries,
                                             Map<String, Object> arguments) {
        int result = 0;
        String query = null;
        String defaultValue = DBTools.getDescriptorStringValue(descriptor, PropertyIds.DEFAULTVALUE);
        if (defaultValue.isBlank()) {
            if (provider.getConfigDDL().hasColumnDropDefaultCommand()) {
                query = provider.getConfigDDL().getColumnDropDefaultCommand(arguments);
            }
        } else if (provider.getConfigDDL().hasColumnSetDefaultCommand()) {
            query = provider.getConfigDDL().getColumnSetDefaultCommand(arguments);
        }
        if (query != null) {
            queries.add(query);
            result = COLUMN_DEFAULT_VALUE;
        }
        return result;
    }

    private static int setColumnNullable(Provider provider,
                                         XPropertySet descriptor,
                                         List<String> queries,
                                         Map<String, Object> arguments) {
        String query = null;
        int result = 0;
        int nullable = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.ISNULLABLE);
        if (nullable == ColumnValue.NO_NULLS) {
            if (provider.getConfigDDL().hasColumnSetNotNullCommand()) {
                query = provider.getConfigDDL().getColumnSetNotNullCommand(arguments);
            }
        } else if (provider.getConfigDDL().hasColumnDropNotNullCommand()) {
            query = provider.getConfigDDL().getColumnDropNotNullCommand(arguments);
        }
        if (query != null) {
            queries.add(query);
            result = COLUMN_NULLABLE;
        }
        return result;
    }

    private static ColumnProperties getStandardColumnProperties(Provider provider,
                                                                XPropertySet descriptor)
        throws SQLException {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(newname);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    private static ColumnProperties getStandardColumnProperties(Provider provider,
                                                                String oldname,
                                                                XPropertySet descriptor)
        throws SQLException {
        String newname = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        ColumnProperties column = new ColumnProperties(oldname, newname);
        return getStandardColumnProperties(provider, column, descriptor);
    }

    /** creates the standard SQL statement for the column part of statement.
     * @param provider
     *      The driver provider.
     * @param column
     *      Column properties.
     * @param descriptor
     *      The descriptor of the column.
     * @return
     *      The column properties.
     * @throws java.sql.SQLException 
     */

    private static ColumnProperties getStandardColumnProperties(Provider provider,
                                                                ColumnProperties column,
                                                                XPropertySet descriptor)
        throws SQLException {
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

    private static final boolean useLiteral(Provider provider, String typename, int datatype)
        throws SQLException {
        boolean useliteral = false;
        String createparams = "";
        final int TYPE_NAME = 1;
        final int DATA_TYPE = 2;
        final int CREATE_PARAMS = 6;
        RowSetData data = provider.getConfigSQL().getTypeInfoData();
        DatabaseMetaData metadata = provider.getConnection().getMetaData();
        try (ResultSet result = ResultSetHelper.getCustomDataResultSet(metadata.getTypeInfo(), data)) {
            while (result.next()) {
                String typename2cmp = result.getString(TYPE_NAME);
                int type2cmp = result.getShort(DATA_TYPE);
                createparams = result.getString(CREATE_PARAMS);
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
     * @throws java.sql.SQLException 
     */

    private static List<String> getCreateConstraintQueries(Provider provider,
                                                           XPropertySet descriptor,
                                                           XPropertySet property,
                                                           String name,
                                                           ComposeRule rule,
                                                           boolean sensitive)
        throws java.sql.SQLException {
        List<String> queries = new ArrayList<>();
        NamedComponent table = ComponentHelper.getTableNamedComponents(property);

        XKeysSupplier supplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = supplier.getKeys();
        try {
            if (keys != null) {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet key = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (key != null) {
                        queries.add(ConstraintHelper.getCreateConstraintQuery(provider.getConfigDDL(),
                                                                              provider.getNamedSupport(rule),
                                                                              key, table, name, sensitive));
                    }
                }
            }
        } catch (IllegalArgumentException | WrappedTargetException | IndexOutOfBoundsException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
        return queries;
    }
}
