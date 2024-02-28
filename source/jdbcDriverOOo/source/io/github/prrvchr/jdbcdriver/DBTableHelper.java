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
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class DBTableHelper
{

    /** creates a SQL CREATE TABLE statement
     *
     * @param provider
     *      The driver provider.
     * @param descriptor
     *      The descriptor of the new table.
     * @param table
     *      The name of the new table.
     * @param sensitive
     *      Is identifier case sensitive.
     * @return
     *   The CREATE TABLE statement.
     * @throws SQLException
     * @throws java.sql.SQLException 
     * @throws UnknownPropertyException 
     */
    public static List<String> getCreateTableQueries(DriverProvider provider,
                                                     XPropertySet descriptor,
                                                     String table,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException, WrappedTargetException, IndexOutOfBoundsException, UnknownPropertyException
    {
        String separator = ", ";
        System.out.println("DBTableHelper.getCreateTableQueries() 1");
        boolean hasAutoIncrement = false;
        List<String> parts = new ArrayList<String>();
        System.out.println("DBTableHelper.getCreateTableQueries() 2");
        List<String> queries = new ArrayList<String>();
        XIndexAccess columns = null;
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        if (supplier != null) {
            System.out.println("DBTableHelper.getCreateTableQueries() 3");
            columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        }
        if (columns == null || columns.getCount() <= 0) {
            System.out.println("DBTableHelper.getCreateTableQueries() 4");
            String message = String.format("The '%s' table has no columns, it is not possible to create the table", table);
            throw new SQLException(message);
        }
        System.out.println("DBTableHelper.getCreateTableQueries() 5");
        int count = columns.getCount();
        for (int i = 0; i < count; i++) {
            XPropertySet column = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
            if (column == null) {
                continue;
            }
            
            System.out.println("DBTools.getCreateTableQueries() 1 supportsColumnDescription: " + provider.supportsColumnDescription());
            if (provider.supportsColumnDescription()) {
                String comment = DBTools.getDescriptorStringValue(column, PropertyIds.DESCRIPTION);
                if (!comment.isEmpty()) {
                    System.out.println("DBTableHelper.getCreateTableQueries() 2");
                    String name = DBTools.composeColumnName(provider, table, DBTools.getDescriptorStringValue(column, PropertyIds.NAME), sensitive);
                    String query = provider.getColumnDescriptionQuery(name, comment);
                    System.out.println("DBTableHelper.getCreateTableQueries() 3 Description query: " + query);
                    queries.add(query);
                }
            }
            final StringBuilder buffer = new StringBuilder();
            hasAutoIncrement |= getStandardColumnPartQuery(buffer, provider, column, sensitive);
            parts.add(buffer.toString());
        }

        System.out.println("DBTableHelper.getCreateTableQueries() 1 isAutoIncrementIsPrimaryKey: " + provider.isAutoIncrementIsPrimaryKey());
        // The primary key will not be created if one of the columns is auto increment
        // and the auto increment are primary keys (ie: Sqlite)
        if (!provider.isAutoIncrementIsPrimaryKey() || !hasAutoIncrement) {
            parts.addAll(getCreateTableKeyParts(provider, descriptor, sensitive));
        }
        queries.add(0, getCreateTableQuery(table, String.join(separator, parts)));
        return queries;
    }



    public static boolean getAlterColumnQueries(List<String> queries,
                                                DriverProvider provider,
                                                TableSuper table,
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
        boolean renamed = false;
        String name = DBTools.composeTableName(provider, table, ComposeRule.InTableDefinitions, sensitive);
        String quote = provider.getIdentifierQuoteString();
        String name1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.NAME);
        String name2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.NAME);

        if (name1.isEmpty()) {
            // create a new column
            StringBuilder buffer = new StringBuilder("ALTER TABLE ");
            buffer.append(name);
            buffer.append(" ADD COLUMN ");
            buffer.append(getStandardColumnPartQuery(provider, descriptor2, sensitive));
            queries.add(buffer.toString());
        }
        else {
            if(!name1.equals(name2)) {
                // rename a column
                String query = provider.getRenameColumnQuery();
                String oldname = DBTools.quoteName(quote, name1, sensitive);
                String newname = DBTools.quoteName(quote, name2, sensitive);
                queries.add(MessageFormat.format(query, name, oldname, newname));
                renamed = true;
            }

            String type1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.TYPENAME);
            String type2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.TYPENAME);
            String default1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
            String default2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
            if (!type2.equals(type1) || !default2.equals(default1)) {
                StringBuilder buffer = new StringBuilder("ALTER TABLE ");
                buffer.append(name);
                buffer.append(" ALTER COLUMN ");
                buffer.append(getStandardColumnPartQuery(provider, descriptor2, sensitive));
                queries.add(buffer.toString());
            }
            else {
                int nullable1 = DBTools.getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
                int nullable2 = DBTools.getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
                if (!(alterpk && renamed) && (nullable2 != nullable1)) {
                    StringBuilder buffer = new StringBuilder("ALTER TABLE ");
                    buffer.append(name);
                    buffer.append(" ALTER COLUMN ");
                    buffer.append(DBTools.quoteName(quote, name2, sensitive));
                    if (nullable2 == ColumnValue.NO_NULLS) {
                        buffer.append(" SET ");
                    }
                    else {
                        buffer.append(" DROP ");
                    }
                    buffer.append(" NOT NULL");
                    queries.add(buffer.toString());
                }
            }
        }

        System.out.println("DBTableHelper.getAlterColumnQueries() 1 supportsColumnDescription: " + provider.supportsColumnDescription());
        //if (provider.supportsColumnDescription()) {
        if (true) {
            String comment1 = DBTools.getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
            String comment2 = DBTools.getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            System.out.println("DBTableHelper.getAlterColumnQueries() 2 Comment1: " + comment1 + " - Comment2: " + comment2);
            if (!comment2.equals(comment1)) {
                StringBuilder buffer = new StringBuilder(name);
                buffer.append(".");
                buffer.append(DBTools.quoteName(quote, name2, sensitive));
                String query = provider.getColumnDescriptionQuery(buffer.toString(), comment2);
                queries.add(query);
            }
        }
        return renamed;
    }


    /** creates the standard sql statement for the column part of statement.
     * @param provider
     *      The driver provider.
     * @param column
     *      The descriptor of the column.
     * @param sensitive
     *      Is identifier case sensitive.
     * @throws SQLException
     * @throws java.sql.SQLException 
     * @throws IllegalArgumentException 
     */

    private static String getStandardColumnPartQuery(DriverProvider provider,
                                                    XPropertySet column,
                                                    boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException
    {
        final StringBuilder buffer = new StringBuilder();
        getStandardColumnPartQuery(buffer, provider, column, sensitive);
        return buffer.toString();

    }

    private static boolean getStandardColumnPartQuery(StringBuilder buffer,
                                                      DriverProvider provider,
                                                      XPropertySet column,
                                                      boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException
    {
        boolean hasAutoIncrementValue = false;
        String name = DBTools.getDescriptorStringValue(column, PropertyIds.NAME);
        buffer.append(DBTools.quoteName(provider, name, sensitive));
        buffer.append(' ');
        String typename = DBTools.getDescriptorStringValue(column, PropertyIds.TYPENAME);
        int datatype = DBTools.getDescriptorIntegerValue(column, PropertyIds.TYPE);
        int precision = DBTools.getDescriptorIntegerValue(column, PropertyIds.PRECISION);
        int scale = DBTools.getDescriptorIntegerValue(column, PropertyIds.SCALE);
        boolean isAutoIncrement = DBTools.getDescriptorBooleanValue(column, PropertyIds.ISAUTOINCREMENT);
        String autoIncrementValue = "";
        System.out.println("DBTableHelper.getStandardColumnPartQuery() 1 TYPENAME: " + typename + " - TYPE: " + datatype + " - PRECISION: " + precision + " - SCALE: " + scale);
        
        // Check if the user enter a specific string to create auto increment values
        XPropertySetInfo info = column.getPropertySetInfo();
        if (info != null && DBTools.hasDescriptorProperty(info, PropertyIds.AUTOINCREMENTCREATION)) {
            autoIncrementValue = DBTools.getDescriptorStringValue(column, PropertyIds.AUTOINCREMENTCREATION);
            hasAutoIncrementValue = !autoIncrementValue.isEmpty();
        }
        
        // look if we have to use precisions
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
                System.out.println("DBTableHelper.getStandardColumnPartQuery() 2 typename: " + typename + " - typename2cmp: " + typename2cmp + " - type2cmp: " + type2cmp + " - datatype: " + datatype + " - createparams: " + createparams);
                if (typename.equalsIgnoreCase(typename2cmp) && type2cmp == datatype && !createparams.isBlank() && !result.wasNull()) {
                    useliteral = true;
                    System.out.println("DBTableHelper.getStandardColumnPartQuery() 2 useliteral: " + useliteral);
                    break;
                }
            }
        }
        int index = 0;
        if (hasAutoIncrementValue && (index = typename.indexOf(autoIncrementValue)) != -1) {
            typename = typename.substring(0, index);
        }
        
        if ((precision > 0 || scale > 0) && useliteral) {
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
                buffer.append(typename);
            }
            else {
                buffer.append(typename.substring(0, insert));
            }
            buffer.append('(');
            
            if (precision > 0 && !timed) {
                buffer.append(precision);
                if (scale > 0) {
                    buffer.append(',');
                }
            }
            if (scale > 0 || timed) {
                buffer.append(scale);
            }
            
            if (insert == -1) {
                buffer.append(')');
            }
            else {
                if (timed) {
                    buffer.append(')');
                }
                else {
                    insert = typename.indexOf(')', insert);
                }
                buffer.append(typename.substring(insert));
            }
        }
        else {
            buffer.append(typename); // simply add the type name
        }
        
        String defaultvalue = DBTools.getDescriptorStringValue(column, PropertyIds.DEFAULTVALUE);
        System.out.println("DBTableHelper.getStandardColumnPartQuery() DEFAULT: " + defaultvalue + " - PREFIX: " + prefix + " - POSTFIX: " + postfix + " - PARAM: " + createparams);
        if (!defaultvalue.isEmpty()) {
            buffer.append(" DEFAULT ");
            buffer.append(prefix);
            buffer.append(defaultvalue);
            buffer.append(postfix);
        }
        int isnullable = DBTools.getDescriptorIntegerValue(column, PropertyIds.ISNULLABLE);
        if (isnullable == ColumnValue.NO_NULLS) {
            buffer.append(" NOT NULL");
        }
        
        if (isAutoIncrement && hasAutoIncrementValue) {
            buffer.append(' ');
            buffer.append(autoIncrementValue);
        }
        return hasAutoIncrementValue;
    }

    /** creates the keys parts of SQL CREATE TABLE statement.
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
    private static List<String> getCreateTableKeyParts(DriverProvider provider,
                                                       XPropertySet descriptor,
                                                       boolean sensitive)
        throws java.sql.SQLException, SQLException, IllegalArgumentException, WrappedTargetException, UnknownPropertyException, IndexOutOfBoundsException
    {
        List<String> parts = new ArrayList<String>();
            XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
            XIndexAccess keys = keysSupplier.getKeys();
            if (keys != null) {
                boolean hasPrimaryKey = false;
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        StringBuilder buffer = new StringBuilder();
                        int keyType = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.TYPE);
                        XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, columnProperties);
                        XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
                        if (columns == null || columns.getCount() == 0) {
                            throw new SQLException();
                        }
                        if (keyType == KeyType.PRIMARY) {
                            if (hasPrimaryKey) {
                                throw new SQLException();
                            }
                            hasPrimaryKey = true;
                            buffer.append("PRIMARY KEY");
                            buffer.append(getColumnNames(provider, columns, sensitive));
                        }
                        else if (keyType == KeyType.UNIQUE) {
                            buffer.append("UNIQUE");
                            buffer.append(getColumnNames(provider, columns, sensitive));
                        }
                        else if (keyType == KeyType.FOREIGN) {
                            int deleteRule = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.DELETERULE);
                            buffer.append("FOREIGN KEY");
                            
                            String referencedTable = DBTools.getDescriptorStringValue(columnProperties, PropertyIds.REFERENCEDTABLE);
                            NameComponents nameComponents = DBTools.qualifiedNameComponents(provider, referencedTable, ComposeRule.InDataManipulation);
                            String composedName = DBTools.buildName(provider, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(),
                                                                       ComposeRule.InTableDefinitions, true);
                            if (composedName.isEmpty()) {
                                throw new SQLException();
                            }
                            
                            buffer.append(getColumnNames(provider, columns, sensitive));
                            
                            switch (deleteRule) {
                            case KeyRule.CASCADE:
                                buffer.append(" ON DELETE CASCADE");
                                break;
                            case KeyRule.RESTRICT:
                                buffer.append(" ON DELETE RESTRICT");
                                break;
                            case KeyRule.SET_NULL:
                                buffer.append(" ON DELETE SET NULL");
                                break;
                            case KeyRule.SET_DEFAULT:
                                buffer.append(" ON DELETE SET DEFAULT");
                                break;
                            }
                        }
                        parts.add(buffer.toString());
                    }
                }
            }
        return parts;
    }

    private static String getColumnNames(DriverProvider provider,
                                         XIndexAccess columns,
                                         boolean sensitive)
        throws SQLException,
               WrappedTargetException,
               UnknownPropertyException,
               IllegalArgumentException,
               IndexOutOfBoundsException
    {
        String separator = ", ";
        String quote = provider.getIdentifierQuoteString();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < columns.getCount(); i++) {
            XPropertySet properties = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
            if (properties != null) {
                String name = DBTools.getDescriptorStringValue(properties, PropertyIds.NAME);
                names.add(DBTools.quoteName(quote, name, sensitive));
            }
        }
        StringBuilder buffer = new StringBuilder(" (");
        if (!names.isEmpty()) {
            buffer.append(String.join(separator, names));
        }
        buffer.append(")");
        return buffer.toString();
    }

    private static String getCreateTableQuery(String table,
                                             String columns)
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_CREATE_TABLE, table, columns);
    }

}
