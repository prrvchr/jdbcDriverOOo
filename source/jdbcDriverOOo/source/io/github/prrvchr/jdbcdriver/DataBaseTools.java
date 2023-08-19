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

import java.io.InputStream;
import java.sql.RowIdLifetime;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XDatabaseMetaData;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XRow;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.css.util.Date;
import io.github.prrvchr.css.util.DateTime;
import io.github.prrvchr.css.util.DateTimeWithTimezone;
import io.github.prrvchr.css.util.DateWithTimezone;
import io.github.prrvchr.css.util.Time;
import io.github.prrvchr.css.util.TimeWithTimezone;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.ColumnContainerBase.ExtraColumnInfo;


public class DataBaseTools
{

    private static class NameComponentSupport
    {
        boolean useCatalogs;
        boolean useSchemas;
        
        NameComponentSupport(boolean useCatalogs, boolean useSchemas)
        {
            this.useCatalogs = useCatalogs;
            this.useSchemas = useSchemas;
        }
    }

    public static class NameComponents
    {
        private String catalog = "";
        private String schema = "";
        private String table = "";

        public NameComponents(String catalog, String schema, String table)
        {
            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
        }

        public NameComponents() {
        }

        public String getCatalog()
        {
            return catalog;
        }

        public void setCatalog(String catalog)
        {
            this.catalog = catalog;
        }
        
        public String getSchema()
        {
            return schema;
        }
        
        public void setSchema(String schema)
        {
            this.schema = schema;
        }
        
        public String getTable() {
            return table;
        }
        
        public void setTable(String table)
        {
            this.table = table;
        }
    }

    private static NameComponentSupport getNameComponentSupport(ConnectionBase m_connection,
                                                                ComposeRule rule)
        throws SQLException
    {
        XDatabaseMetaData metadata = m_connection.getMetaData();
        switch (rule) {
        case InTableDefinitions:
            return new NameComponentSupport(metadata.supportsCatalogsInTableDefinitions(),
                                            metadata.supportsSchemasInTableDefinitions());
        case InIndexDefinitions:
            return new NameComponentSupport(metadata.supportsCatalogsInIndexDefinitions(),
                                            metadata.supportsSchemasInIndexDefinitions());
        case InDataManipulation:
            return new NameComponentSupport(metadata.supportsCatalogsInDataManipulation(),
                                            metadata.supportsSchemasInDataManipulation());
        case InProcedureCalls:
            return new NameComponentSupport(metadata.supportsCatalogsInProcedureCalls(),
                                            metadata.supportsSchemasInProcedureCalls());
        case InPrivilegeDefinitions:
            return new NameComponentSupport(metadata.supportsCatalogsInPrivilegeDefinitions(),
                                            metadata.supportsSchemasInPrivilegeDefinitions());
        case Complete:
            return new NameComponentSupport(true,
                                            true);
        default:
            throw new UnsupportedOperationException("Invalid/unknown enum value");
        }
    }

    /** compose a complete column name from it's up to four parts, regarding to the database meta data composing rules
     */
    public static String composeColumnName(ConnectionBase m_connection,
                                           String catalog,
                                           String schema,
                                           String table,
                                           String column,
                                           boolean quoted,
                                           ComposeRule composeRule)
        throws SQLException
    {
        StringBuilder composedName = new StringBuilder();
        try {
            java.sql.DatabaseMetaData metadata = m_connection.getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            composedName.append(composeTableName(m_connection, catalog, schema, table, quoted, composeRule));
            composedName.append('.');
            composedName.append(quoted ? quoteName(quote, column) : column);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
        return composedName.toString();
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules
     */
    public static String composeTableName(ConnectionBase m_connection,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean quoted,
                                          ComposeRule composeRule)
        throws SQLException
    {
        if (m_connection == null) {
            return "";
        }
        StringBuilder composedName = new StringBuilder();
        NameComponentSupport nameComponentSupport = getNameComponentSupport(m_connection, composeRule);
        try {
            java.sql.DatabaseMetaData metadata = m_connection.getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            
            
            String catalogSeparator = "";
            boolean catalogAtStart = true;
            if (!catalog.isEmpty() && nameComponentSupport.useCatalogs) {
                catalogSeparator = metadata.getCatalogSeparator();
                catalogAtStart = metadata.isCatalogAtStart();
                if (catalogAtStart && !catalogSeparator.isEmpty()) {
                    composedName.append(quoted ? quoteName(quote, catalog) : catalog);
                    composedName.append(catalogSeparator);
                }
            }
            if (!schema.isEmpty() && nameComponentSupport.useSchemas) {
                composedName.append(quoted ? quoteName(quote, schema) : schema);
                composedName.append('.');
            }
            composedName.append(quoted ? quoteName(quote, table) : table);
            if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && nameComponentSupport.useCatalogs) {
                composedName.append(catalogSeparator);
                composedName.append(quoted ? quoteName(quote, catalog) : catalog);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
        System.out.println("DataBaseTools.composeTableName(): Name: " + composedName.toString());
        return composedName.toString();
    }

    public static String composeTableName(ConnectionSuper connection,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean catalog,
                                          boolean schema,
                                          boolean quoted)
        throws SQLException
    {
        NameComponents component = getTableNameComponents(connection, table);
        return doComposeTableName(connection, catalog ? "" : component.getCatalog(),
                                  schema ? "" : component.getSchema(), component.getTable(), quoted, rule);
    }

    public static String doComposeTableName(ConnectionSuper connection,
                                            String catalog,
                                            String schema,
                                            String table,
                                            boolean quoted,
                                            ComposeRule rule)
        throws SQLException
    {
        StringBuilder composedName = new StringBuilder();
        NameComponentSupport nameComponentSupport = getNameComponentSupport(connection, rule);
        try {
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            UnoHelper.ensure(!table.isEmpty(), "At least the table name should be non-empty", connection.getLogger());
            String quoteString = metadata.getIdentifierQuoteString();
            
            String catalogSeparator = "";
            boolean catalogAtStart = true;
            if (!catalog.isEmpty() && nameComponentSupport.useCatalogs) {
                catalogSeparator = metadata.getCatalogSeparator();
                catalogAtStart = metadata.isCatalogAtStart();
                
                if (catalogAtStart && !catalogSeparator.isEmpty()) {
                    composedName.append(quoted ? quoteName(quoteString, catalog) : catalog);
                    composedName.append(catalogSeparator);
                }
            }
            
            if (!schema.isEmpty() && nameComponentSupport.useSchemas) {
                composedName.append(quoted ? quoteName(quoteString, schema) : schema);
                composedName.append(".");
            }
            
            composedName.append(quoted ? quoteName(quoteString, table) : table);
            
            if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && nameComponentSupport.useCatalogs) {
                composedName.append(catalogSeparator);
                composedName.append(quoted ? quoteName(quoteString, catalog) : catalog);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return composedName.toString();
    }

    /** composes a table name for usage in a SELECT statement
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(ConnectionSuper connection,
                                                   String catalog,
                                                   String schema,
                                                   String table)
        throws SQLException
    {
        boolean usecatalog = UnoHelper.getDefaultPropertyValue(connection.getInfo(), "UseCatalogInSelect", true);
        boolean useschema = UnoHelper.getDefaultPropertyValue(connection.getInfo(), "UseSchemaInSelect", true);
        return doComposeTableName(connection, usecatalog ? catalog : "", useschema ? schema : "", table, true, ComposeRule.InDataManipulation);
    }

    /** composes a table name for usage in a SELECT statement
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(ConnectionSuper connection,
                                                   XPropertySet table)
        throws SQLException
    {
        NameComponents component = getTableNameComponents(connection, table);
        return composeTableNameForSelect(connection, component.getCatalog(), component.getSchema(), component.getTable());
    }

    public static NameComponents getTableNameComponents(ConnectionSuper connection,
                                                        XPropertySet table)
        throws SQLException
    {
        try {
            NameComponents component = new NameComponents();
            XPropertySetInfo info = table.getPropertySetInfo();
            if (info != null && info.hasPropertyByName(PropertyIds.NAME.name)) {
                if (info.hasPropertyByName(PropertyIds.CATALOGNAME.name)
                        && info.hasPropertyByName(PropertyIds.SCHEMANAME.name)) {
                    component.setCatalog(AnyConverter.toString(table.getPropertyValue(PropertyIds.CATALOGNAME.name)));
                    component.setSchema(AnyConverter.toString(table.getPropertyValue(PropertyIds.SCHEMANAME.name)));
                }
                component.setTable(AnyConverter.toString(table.getPropertyValue(PropertyIds.NAME.name)));
            }
            else {
                UnoHelper.ensure(false, "this is not a table object", connection.getLogger());
            }
            return component;
        }
        catch (IllegalArgumentException | WrappedTargetException | UnknownPropertyException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
    }

    /** quote the given name with the given quote string.
     */
    public static String quoteName(String quote,
                                   String name)
    {
        if (!quote.isEmpty() && quote.codePointAt(0) != ' ') {
            return quote + name + quote;
        }
        return name;
    }

    /** quote the given table name (which may contain a catalog and a schema) according to the rules provided by the meta data
     */
    public static String quoteTableName(ConnectionSuper connection,
                                        String name,
                                        ComposeRule rule)
        throws SQLException
    {
        NameComponents nameComponents = qualifiedNameComponents(connection, name, rule);
        return doComposeTableName(connection, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(), true, rule);
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param metadata     meta data describing the connection where you got the table name from
     * @param name     fully qualified table name
     * @param rule       where do you need the name for
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NameComponents qualifiedNameComponents(ConnectionSuper connection,
                                                         String name,
                                                         ComposeRule rule)
        throws SQLException
    {
        NameComponents component = new NameComponents();
        NameComponentSupport support = getNameComponentSupport(connection, rule);
        XDatabaseMetaData metadata = connection.getMetaData();
        UnoHelper.ensure(metadata, "QualifiedNameComponents : invalid meta data!", connection.getLogger());
        String separator = metadata.getCatalogSeparator();
        String buffer = name;
        // do we have catalogs ?
        if (support.useCatalogs) {
            if (metadata.isCatalogAtStart()) {
                // search for the catalog name at the beginning
                int index = buffer.indexOf(separator);
                if (-1 != index) {
                    component.setCatalog(buffer.substring(0, index));
                    buffer = buffer.substring(index + 1);
                }
            }
            else {
                // catalog name at end
                int index = buffer.lastIndexOf(separator);
                if (-1 != index) {
                    component.setCatalog(buffer.substring(index + 1));
                    buffer = buffer.substring(0, index);
                }
            }
        }
        if (support.useSchemas) {
            int index = buffer.indexOf(".");
            UnoHelper.ensure(-1 != index, "QualifiedNameComponents : no schema separator!", connection.getLogger());
            if (index != -1) {
                component.setSchema(buffer.substring(0, index));
            }
            buffer = buffer.substring(index + 1);
        }
        component.setTable(buffer);
        return component;
    }

    /** creates a SQL CREATE TABLE statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new table.
     * @param helper
     *    Allow to add special SQL constructs.
     * @param pattern
     *   
     * @return
     *   The CREATE TABLE statement.
     * @throws SQLException
     */
    public static String getCreateTableQuery(ConnectionSuper connection,
                                             XPropertySet descriptor,
                                             ISQLStatementHelper helper,
                                             String pattern)
        throws SQLException
    {
        String table = DataBaseTools.composeTableName(connection, descriptor, ComposeRule.InTableDefinitions, false, false, true);
        List<String> parts = getCreateTableColumnParts(connection, descriptor, helper, pattern, table);
        if (connection.getProvider().supportCreateTableKeyParts()) {
            parts.addAll(getCreateTableKeyParts(connection, descriptor));
        }
        return String.format(connection.getProvider().getCreateTableQuery(), table, String.join(",", parts));
    }

    /** creates the columns parts of the SQL CREATE TABLE statement.
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new table.
     * @param helper
     *    Allow to add special SQL constructs.
     * @param pattern
     *   
     * @return
     *   The columns parts.
     * @throws SQLException
     */
    public static List<String> getCreateTableColumnParts(ConnectionSuper connection,
                                                         XPropertySet descriptor,
                                                         ISQLStatementHelper helper,
                                                         String pattern,
                                                         String table)
        throws SQLException
    {
        List<String> parts = new ArrayList<String>();
        try {
            XIndexAccess columns = null;
            XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            if (supplier != null) {
                columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            }
            if (columns == null || columns.getCount() <= 0) {
                String message = String.format("The '%s' table has no columns, it is not possible to create the table", table);
                throw new SQLException(message);
            }
            int count = columns.getCount();
            for (int i = 0; i < count; i++) {
                XPropertySet column;
                column = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
                if (column != null) {
                    parts.add(getStandardColumnPartQuery(connection, column, helper, pattern));
               }
            }
        }
        catch (IllegalArgumentException | WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        return parts;
    }

    /** creates the standard sql statement for the column part of statement.
     *  @param connection
     *      The connection.
     *  @param column
     *      The descriptor of the column.
     *  @param helper
     *      Allow to add special SQL constructs.
     *  @param pattern
     *      
     * @throws SQLException
     */
    public static String getStandardColumnPartQuery(ConnectionSuper connection,
                                                    XPropertySet column,
                                                    ISQLStatementHelper helper,
                                                    String pattern)
        throws SQLException
    {
        final String quote = connection.getMetaData().getIdentifierQuoteString();
        final StringBuilder sql = new StringBuilder();
        try {
            sql.append(quoteName(quote, AnyConverter.toString(column.getPropertyValue(PropertyIds.NAME.name))));
            sql.append(' ');
            
            String typename = AnyConverter.toString(column.getPropertyValue(PropertyIds.TYPENAME.name));
            int datatype = AnyConverter.toInt(column.getPropertyValue(PropertyIds.TYPE.name));
            int precision = AnyConverter.toInt(column.getPropertyValue(PropertyIds.PRECISION.name));
            int scale = AnyConverter.toInt(column.getPropertyValue(PropertyIds.SCALE.name));
            boolean isAutoIncrement = AnyConverter.toBoolean(column.getPropertyValue(PropertyIds.ISAUTOINCREMENT.name));
            System.out.println("DataBaseTools.getStandardColumnPartQuery() 1 TYPENAME: " + typename + " - TYPE: " + datatype + " - PRECISION: " + precision + " - SCALE: " + scale);
            
            // check if the user enter a specific string to create autoincrement values
            String autoIncrementValue = "";
            XPropertySetInfo columnPropertiesInfo = column.getPropertySetInfo();
            if (columnPropertiesInfo != null && columnPropertiesInfo.hasPropertyByName(PropertyIds.AUTOINCREMENTCREATION.name)) {
                autoIncrementValue = AnyConverter.toString(column.getPropertyValue(PropertyIds.AUTOINCREMENTCREATION.name));
            }
            
            // look if we have to use precisions
            boolean useliteral = false;
            String prefix = "";
            String postfix = "";
            String createparams = "";
            XResultSet resultset = null;
            try {
                resultset = connection.getMetaData().getTypeInfo();
                if (resultset != null) {
                    XRow result = UnoRuntime.queryInterface(XRow.class, resultset);
                    while (resultset.next()) {
                        String typename2cmp = result.getString(1);
                        int type2cmp = result.getShort(2);
                        //FIXME: Make sure prefix and suffix values are not null
                        prefix = result.getString(4);
                        prefix = result.wasNull() ? "" : prefix;
                        postfix = result.getString(5);
                        postfix = result.wasNull() ? "" : postfix;
                        createparams = result.getString(6);
                        createparams = result.wasNull() ? "" : createparams;
                        // first identical type will be used if typename is empty
                        if (typename.isEmpty() && type2cmp == datatype) {
                            typename = typename2cmp;
                        }
                        System.out.println("DataBaseTools.getStandardColumnPartQuery() 2 typename: " + typename + " - typename2cmp: " + typename2cmp + " - type2cmp: " + type2cmp + " - datatype: " + datatype + " - createparams: " + createparams);
                        if (typename.equalsIgnoreCase(typename2cmp) && type2cmp == datatype && !createparams.isEmpty()) {
                            useliteral = true;
                            System.out.println("DataBaseTools.getStandardColumnPartQuery() 2 useliteral: " + useliteral);
                            break;
                        }
                    }
                }
            }
            finally {
                Tools.close(resultset);
            }
            int index = 0;
            if (!autoIncrementValue.isEmpty() && (index = typename.indexOf(autoIncrementValue)) != -1) {
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
                    sql.append(typename);
                }
                else {
                    sql.append(typename.substring(0, insert));
                }
                sql.append('(');
                if (precision > 0 && !timed) {
                    sql.append(precision);
                    if (scale > 0 || (!pattern.isEmpty() && createparams.indexOf(pattern) != -1)) {
                        sql.append(',');
                    }
                }
                if (scale > 0 || (!pattern.isEmpty() && createparams.indexOf(pattern) != -1) || timed) {
                    sql.append(scale);
                }
                if (insert == -1) {
                    sql.append(')');
                }
                else if (timed) {
                    sql.append(')');
                    sql.append(typename.substring(insert));
                }
                else {
                    insert = typename.indexOf(')', insert);
                    sql.append(typename.substring(insert));
                }
            }
            else {
                sql.append(typename); // simply add the type name
            }
            
            String defaultvalue = AnyConverter.toString(column.getPropertyValue(PropertyIds.DEFAULTVALUE.name));
            System.out.println("DataBaseTools.getStandardColumnPartQuery() DEFAULT: " + defaultvalue + " - PREFIX: " + prefix + " - POSTFIX: " + postfix + " - PARAM: " + createparams);
            if (!defaultvalue.isEmpty()) {
                sql.append(" DEFAULT ");
                sql.append(prefix);
                sql.append(defaultvalue);
                sql.append(postfix);
            }
            int isnullable = AnyConverter.toInt(column.getPropertyValue(PropertyIds.ISNULLABLE.name));
            if (isnullable == ColumnValue.NO_NULLS) {
                sql.append(" NOT NULL");
            }
            
            if (isAutoIncrement && !autoIncrementValue.isEmpty()) {
                sql.append(' ');
                sql.append(autoIncrementValue);
            }
            
            if (helper != null) {
                helper.addComment(column, sql);
            }
            
        }
        catch (IllegalArgumentException | WrappedTargetException | UnknownPropertyException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        return sql.toString();
    }

    /** creates the keys parts of SQL CREATE TABLE statement.
     * @param connection
     *      The connection.
     * @param descriptor
     *      The descriptor of the new table.
     *   
     * @return
     *      The keys parts.
     * @throws SQLException
     */
    public static List<String> getCreateTableKeyParts(ConnectionSuper connection,
                                                      XPropertySet descriptor)
        throws SQLException
    {
        List<String> parts = new ArrayList<String>();
        try {
            XDatabaseMetaData metadata = connection.getMetaData();
            XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
            XIndexAccess keys = keysSupplier.getKeys();
            if (keys != null) {
                boolean hasPrimaryKey = false;
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = (XPropertySet) AnyConverter.toObject(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        StringBuilder sql = new StringBuilder();
                        int keyType = AnyConverter.toInt(columnProperties.getPropertyValue(PropertyIds.TYPE.name));
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
                            sql.append("PRIMARY KEY");
                            sql.append(getColumnNames(metadata, columns));
                        }
                        else if (keyType == KeyType.UNIQUE) {
                            sql.append("UNIQUE");
                            sql.append(getColumnNames(metadata, columns));
                        }
                        else if (keyType == KeyType.FOREIGN) {
                            int deleteRule = AnyConverter.toInt(columnProperties.getPropertyValue(PropertyIds.DELETERULE.name));
                            sql.append("FOREIGN KEY");
                            
                            String referencedTable = AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.REFERENCEDTABLE.name));
                            NameComponents nameComponents = qualifiedNameComponents(connection, referencedTable, ComposeRule.InDataManipulation);
                            String composedName = composeTableName(connection, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(),
                                                                   true, ComposeRule.InTableDefinitions);
                            if (composedName.isEmpty()) {
                                throw new SQLException();
                            }
                            
                            sql.append(getColumnNames(metadata, columns));
                            
                            switch (deleteRule) {
                            case KeyRule.CASCADE:
                                sql.append(" ON DELETE CASCADE");
                                break;
                            case KeyRule.RESTRICT:
                                sql.append(" ON DELETE RESTRICT");
                                break;
                            case KeyRule.SET_NULL:
                                sql.append(" ON DELETE SET NULL");
                                break;
                            case KeyRule.SET_DEFAULT:
                                sql.append(" ON DELETE SET DEFAULT");
                                break;
                            }
                        }
                        parts.add(sql.toString());
                    }
                }
            }
        }
        catch (UnknownPropertyException | IllegalArgumentException |
               WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        return parts;
    }

    private static String getColumnNames(XDatabaseMetaData metadata,
                                         XIndexAccess columns)
        throws SQLException,
               WrappedTargetException,
               UnknownPropertyException,
               IllegalArgumentException,
               IndexOutOfBoundsException
    {
        StringBuilder sql = new StringBuilder(" (");
        String quote = metadata.getIdentifierQuoteString();
        int columnCount = columns.getCount();
        String separator = "";
        for (int i = 0; i < columnCount; i++) {
            XPropertySet columnProperties = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
            if (columnProperties != null) {
                sql.append(separator);
                separator = ",";
                String columnName = AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.NAME.name));
                sql.append(quoteName(quote, columnName));
            }
        }
        if (columnCount > 0) {
            sql.append(")");
        }
        return sql.toString();
    }

    /** creates a SQL CREATE VIEW statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new view.
     *
     * @return
     *   The CREATE VIEW statement.
     */
    public static String getCreateViewQuery(ConnectionSuper connection,
                                            XPropertySet descriptor)
        throws SQLException
    {
        try {
            String view = DataBaseTools.composeTableName(connection, descriptor, ComposeRule.InTableDefinitions, false, false, true);
            String command = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.COMMAND.name));
            return String.format("CREATE VIEW %s AS %s", view, command);
        }
        catch (IllegalArgumentException | WrappedTargetException | UnknownPropertyException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
    }

    /** creates a SQL CREATE USER statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new user.
     * @param name
     *    The name of the new user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The CREATE USER statement.
     * @throws SQLException
     */
    public static String getCreateUserQuery(Connection connection,
                                            XPropertySet descriptor,
                                            String name,
                                            boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            String password = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.PASSWORD.name));
            password = password.isBlank() ? "" : password;
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                name = quoteName(quote, name);
            }
            else {
                name = name.toUpperCase();
            }
            sql = String.format("CREATE USER %s PASSWORD '%s'", name, password);
        }
        catch (IllegalArgumentException | UnknownPropertyException | WrappedTargetException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL DROP USER statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP USER statement.
     * @throws SQLException
     */
    public static String getDropUserQuery(Connection connection,
                                          String name,
                                          boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                name = quoteName(quote, name);
            }
            sql = String.format("DROP USER %s", name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL ALTER USER SET PASSWORD statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the user.
     * @param password
     *    The new password of the user.
     * @param sensitive
     *    Is the name of user case sensitive.
     *   
     * @return
     *   The ALTER USER SET PASSWORD statement.
     * @throws SQLException
     */
    public static String getChangeUserPasswordQuery(Connection connection,
                                                    String name,
                                                    String password,
                                                    boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            password = password.isBlank() ? "" : password;
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                name = quoteName(quote, name);
            }
            sql = String.format("ALTER USER %s SET PASSWORD '%s'", name, password);
        }
        catch (IllegalArgumentException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL CREATE ROLE statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new group.
     * @param name
     *    The name of the new group.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The CREATE ROLE statement.
     * @throws SQLException
     */
    public static String getCreateGroupQuery(Connection connection,
                                             XPropertySet descriptor,
                                             String name,
                                             boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                name = quoteName(quote, name);
            }
            else {
                name = name.toUpperCase();
            }
            sql = String.format("CREATE ROLE %s", name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL DROP ROLE statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the role.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP ROLE statement.
     * @throws SQLException
     */
    public static String getDropGroupQuery(Connection connection,
                                           String name,
                                           boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                name = quoteName(quote, name);
            }
            sql = String.format("DROP ROLE %s", name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL GRANT ROLE statement
     *
     * @param connection
     *    The connection.
     * @param group
     *    The role.
     * @param user
     *    The role member user.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The GRANT ROLE statement.
     * @throws SQLException
     */
    public static String getGrantRoleQuery(Connection connection,
                                           String group,
                                           String user,
                                           boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                group = quoteName(quote, group);
                user = quoteName(quote, user);
            }
            sql = String.format("GRANT %s TO %s", group, user);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    /** creates a SQL REVOKE ROLE statement
     *
     * @param connection
     *    The connection.
     * @param group
     *    The role.
     * @param user
     *    The role member user.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The REVOKE ROLE statement.
     * @throws SQLException
     */
    public static String getRevokeRoleQuery(Connection connection,
                                            String group,
                                            String user,
                                            boolean sensitive)
        throws SQLException
    {
        String sql;
        try {
            if (sensitive) {
                java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
                String quote = metadata.getIdentifierQuoteString();
                group = quoteName(quote, group);
                user = quoteName(quote, user);
            }
            String query = connection.getProvider().getRevokeRoleQuery();
            sql = String.format(query, group, user);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return sql;
    }

    public static int getTableOrViewPrivileges(ConnectionSuper connection,
                                               List<String> grantees,
                                               String catalog,
                                               String schema,
                                               String table)
    throws SQLException
    {
        NameComponents component = new NameComponents(catalog, schema, table);
        return getTableOrViewPrivileges(connection, grantees, component);
    }

    public static int getTableOrViewPrivileges(ConnectionSuper connection,
                                               List<String> grantees,
                                               String name)
    throws SQLException
    {
        NameComponents component = qualifiedNameComponents(connection, name, ComposeRule.InDataManipulation);
        return getTableOrViewPrivileges(connection, grantees, component);
    }

    public static int getTableOrViewPrivileges(ConnectionSuper connection,
                                               List<String> grantees,
                                               NameComponents component)
    throws SQLException
    {
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE ";
        return _getTableOrViewPrivileges(connection, grantees, component, sql);
    }

    public static int getTableOrViewGrantablePrivileges(ConnectionSuper connection,
                                                        List<String> grantees,
                                                        String name)
    throws SQLException
    {
        NameComponents component = DataBaseTools.qualifiedNameComponents(connection, name, ComposeRule.InDataManipulation);
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE IS_GRANTABLE = 'YES' AND ";
        return _getTableOrViewPrivileges(connection, grantees, component, sql);
    }

    private static int _getTableOrViewPrivileges(ConnectionSuper connection,
                                                 List<String> grantees,
                                                 NameComponents component,
                                                 String sql)
        throws SQLException
    {
        int privilege = 0;
        if (!component.getCatalog().isEmpty()) {
            sql += "TABLE_CATALOG = ? AND ";
        }
        if (!component.getSchema().isEmpty()) {
            sql += "TABLE_SCHEMA = ? AND ";
        }
        sql += String.format("TABLE_NAME = ? AND GRANTEE IN (%s)", String.join(", ", new ArrayList<>(Collections.nCopies(grantees.size(), "?"))));
        try (java.sql.PreparedStatement statement = connection.getProvider().getConnection().prepareStatement(sql)){
            int next = 1;
            if (!component.getCatalog().isEmpty()) {
                statement.setString(next++, component.getCatalog());
            }
            if (!component.getSchema().isEmpty()) {
                statement.setString(next++, component.getSchema());
            }
            statement.setString(next++, component.getTable());
            for (String grantee : grantees) {
                statement.setString(next++, grantee);
            }
            java.sql.ResultSet result = statement.executeQuery();
            while (result.next()) {
                privilege |= connection.getProvider().getPrivilege(result.getString(1));
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e);
        }
        return privilege;
    }

    public static void grantTableOrViewPrivileges(ConnectionSuper connection,
                                                  String grantee,
                                                  String name,
                                                  int privilege,
                                                  ComposeRule rule,
                                                  boolean sensitive)
        throws SQLException
    {
        List<String> values = connection.getProvider().getPrivileges(privilege);
        grantee = sensitive ? quoteName(connection.getMetaData().getIdentifierQuoteString(), grantee) : grantee;
        String sql = String.format("GRANT %s ON %s TO %s", String.join(",", values), quoteTableName(connection, name, rule), grantee);
        System.out.println("DataBaseTools.grantTableOrViewPrivileges() SQL: " + sql);
        try (java.sql.Statement statement = connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    public static void revokeTableOrViewPrivileges(ConnectionSuper connection,
                                                   String grantee,
                                                   String name,
                                                   int privilege,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws SQLException
    {
        List<String> values = connection.getProvider().getPrivileges(privilege);
        grantee = sensitive ? quoteName(connection.getMetaData().getIdentifierQuoteString(), grantee) : grantee;
        String query = connection.getProvider().getRevokeTableOrViewPrivileges();
        String sql = String.format(query, String.join(",", values), quoteTableName(connection, name, rule), grantee);
        System.out.println("DataBaseTools.revokeTableOrViewPrivileges() SQL: " + sql);
        try (java.sql.Statement statement = connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
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
    public static Map<String,ExtraColumnInfo> collectColumnInformation(ConnectionSuper connection,
                                                                       String composedName,
                                                                       String columnName)
        throws java.sql.SQLException
    {
        String sql = String.format("SELECT %s FROM %s WHERE 0 = 1", columnName, composedName);
        java.sql.Statement statement = connection.getProvider().getConnection().createStatement();
        statement.setEscapeProcessing(false);
        java.sql.ResultSet result = statement.executeQuery(sql);
        java.sql.ResultSetMetaData metadata = result.getMetaData();

        Map<String, ExtraColumnInfo> columns = new TreeMap<>();
        int count = metadata.getColumnCount();
        UnoHelper.ensure(count > 0, "resultset has empty metadata", connection.getLogger());
        for (int i = 1; i <= count; i++) {
            String newColumnName = metadata.getColumnName(i);
            ExtraColumnInfo columnInfo = new ExtraColumnInfo();
            columnInfo.isAutoIncrement = metadata.isAutoIncrement(i);
            columnInfo.isCurrency = metadata.isCurrency(i);
            columnInfo.dataType = connection.getProvider().getDataType(metadata.getColumnType(i));
            columns.put(newColumnName, columnInfo);
        }
        result.close();
        statement.close();
        return columns;
    }

    /** returns the primary key columns of the table
     */
    public static XNameAccess getPrimaryKeyColumns(XPropertySet table)
        throws java.sql.SQLException {
        try {
            XNameAccess keyColumns = null;
            XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, table);
            if (keysSupplier != null) {
                XIndexAccess keys = keysSupplier.getKeys();
                if (keys != null) {
                    int count = keys.getCount();
                    for (int i = 0; i < count; i++) {
                        XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                        if (propertySet != null) {
                            int keyType = 0;
                            keyType = AnyConverter.toInt(propertySet.getPropertyValue(PropertyIds.TYPE.name));
                            if (keyType == KeyType.PRIMARY) {
                                XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, propertySet);
                                keyColumns = columnsSupplier.getColumns();
                                break;
                            }
                        }
                    }
                }
            }
            return keyColumns;
        } catch (IndexOutOfBoundsException | IllegalArgumentException | WrappedTargetException | UnknownPropertyException e) {
            throw new java.sql.SQLException(e.getMessage());
        }
    }

    /** returns the primary key columns of the table
     */
    public static XNameAccess getPrimaryKeyColumns(XIndexAccess keys)
        throws java.sql.SQLException {
        try {
            XNameAccess keyColumns = null;
            int count = keys.getCount();
            for (int i = 0; i < count; i++) {
                XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                if (propertySet != null) {
                    int keyType = 0;
                    keyType = AnyConverter.toInt(propertySet.getPropertyValue(PropertyIds.TYPE.name));
                    if (keyType == KeyType.PRIMARY) {
                        XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, propertySet);
                        keyColumns = columnsSupplier.getColumns();
                        break;
                    }
                }
            }
            
            
            return keyColumns;
        } catch (IndexOutOfBoundsException | IllegalArgumentException | WrappedTargetException | UnknownPropertyException e) {
            throw new java.sql.SQLException(e.getMessage());
        }
    }

    public static void cloneDescriptorColumns(XPropertySet source,
                                              XPropertySet destination)
        throws SQLException
    {
        System.out.println("DataBaseTools.cloneDescriptorColumns() 1");
        XColumnsSupplier sourceColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, source);
        XIndexAccess sourceColumns = UnoRuntime.queryInterface(XIndexAccess.class, sourceColumnsSupplier.getColumns());
        
        XColumnsSupplier destinationColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, destination);
        XAppend destinationAppend = UnoRuntime.queryInterface(XAppend.class, destinationColumnsSupplier.getColumns());
        
        System.out.println("DataBaseTools.cloneDescriptorColumns() 2");
        int count = sourceColumns.getCount();
        for (int i = 0; i < count; i++) {
            System.out.println("DataBaseTools.cloneDescriptorColumns() 3");
            try {
                XPropertySet columnProperties = (XPropertySet) AnyConverter.toObject(XPropertySet.class, sourceColumns.getByIndex(i));
                destinationAppend.appendByDescriptor(columnProperties);
            }
            catch (WrappedTargetException | IndexOutOfBoundsException | IllegalArgumentException | ElementExistException exception) {
                throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
            }
        }
        System.out.println("DataBaseTools.cloneDescriptorColumns() 4");
    }

    public static boolean updateObject(java.sql.ResultSet resultset,
                                       int index,
                                       Object any)
        throws SQLException
    {
        try {
            boolean success = true;
            Type type = AnyConverter.getType(any);
            switch (type.getTypeClass().getValue()) {
            case TypeClass.VOID_value:
                resultset.updateNull(index);
                break;
            case TypeClass.STRING_value:
                resultset.updateString(index, AnyConverter.toString(any));
                break;
            case TypeClass.BOOLEAN_value:
                resultset.updateBoolean(index, AnyConverter.toBoolean(any));
                break;
            case TypeClass.BYTE_value:
                resultset.updateByte(index, AnyConverter.toByte(any));
                break;
            case TypeClass.UNSIGNED_SHORT_value:
                resultset.updateShort(index, AnyConverter.toUnsignedShort(any));
                break;
            case TypeClass.SHORT_value:
                resultset.updateShort(index, AnyConverter.toShort(any));
                break;
            case TypeClass.CHAR_value:
                resultset.updateString(index, Character.toString(AnyConverter.toChar(any)));
                break;
            case TypeClass.UNSIGNED_LONG_value:
                resultset.updateInt(index, AnyConverter.toUnsignedInt(any));
                break;
            case TypeClass.LONG_value:
                resultset.updateInt(index, AnyConverter.toInt(any));
                break;
            case TypeClass.UNSIGNED_HYPER_value:
                resultset.updateLong(index, AnyConverter.toUnsignedLong(any));
                break;
            case TypeClass.HYPER_value:
                resultset.updateLong(index, AnyConverter.toLong(any));
                break;
            case TypeClass.FLOAT_value:
                resultset.updateFloat(index, AnyConverter.toFloat(any));
                break;
            case TypeClass.DOUBLE_value:
                resultset.updateDouble(index, AnyConverter.toDouble(any));
                break;
            case TypeClass.SEQUENCE_value:
                if (AnyConverter.isArray(any)) {
                    Object array = AnyConverter.toArray(any);
                    if (array instanceof byte[]) {
                        resultset.updateBytes(index, (byte[]) array);
                    }
                    else {
                        success = false;
                    }
                }
                else {
                    success = false;
                }
                break;
            case TypeClass.STRUCT_value:
                Object object = AnyConverter.toObject(Object.class, any);
                if (object instanceof Date) {
                    resultset.updateObject(index, UnoHelper.getJavaLocalDate((Date) object));
                }
                else if (object instanceof Time) {
                    resultset.updateObject(index, UnoHelper.getJavaLocalTime((Time) object));
                }
                else if (object instanceof DateTime) {
                    resultset.updateObject(index, UnoHelper.getJavaLocalDateTime((DateTime) object));
                }
                else {
                    success = false;
                }
                break;
            case TypeClass.INTERFACE_value:
                XInputStream stream = UnoRuntime.queryInterface(XInputStream.class, AnyConverter.toObject(Object.class, any));
                if (stream != null) {
                    InputStream input = new XInputStreamToInputStreamAdapter(stream);
                    resultset.updateBinaryStream(index, input, input.available());
                }
                else {
                    success = false;
                }
                break;
            default:
                success = false;
            }
            return success;
        }
        catch (IllegalArgumentException | java.sql.SQLException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    public static boolean setObject(java.sql.PreparedStatement statement,
                                    int index,
                                    Object any)
        throws SQLException
    {
        Type type = AnyConverter.getType(any);
        try {
            boolean success = true;
            switch (type.getTypeClass().getValue()) {
            case TypeClass.HYPER_value:
                statement.setLong(index, AnyConverter.toLong(any));
                break;
            case TypeClass.UNSIGNED_HYPER_value:
                statement.setLong(index, AnyConverter.toUnsignedLong(any));
                break;
            case TypeClass.VOID_value:
                statement.setNull(index, DataType.VARCHAR);
                break;
            case TypeClass.STRING_value:
                statement.setString(index, AnyConverter.toString(any));
                break;
            case TypeClass.BOOLEAN_value:
                statement.setBoolean(index, AnyConverter.toBoolean(any));
                break;
            case TypeClass.BYTE_value:
                statement.setByte(index, AnyConverter.toByte(any));
                break;
            case TypeClass.SHORT_value:
                statement.setShort(index, AnyConverter.toShort(any));
                break;
            case TypeClass.UNSIGNED_SHORT_value:
                statement.setShort(index, AnyConverter.toUnsignedShort(any));
                break;
            case TypeClass.CHAR_value:
                statement.setString(index, Character.toString(AnyConverter.toChar(any)));
                break;
            case TypeClass.LONG_value:
                statement.setInt(index, AnyConverter.toInt(any));
                break;
            case TypeClass.UNSIGNED_LONG_value:
                statement.setInt(index, AnyConverter.toUnsignedInt(any));
                break;
            case TypeClass.FLOAT_value:
                statement.setFloat(index, AnyConverter.toFloat(any));
                break;
            case TypeClass.DOUBLE_value:
                statement.setDouble(index, AnyConverter.toDouble(any));
                break;
            case TypeClass.SEQUENCE_value:
                if (AnyConverter.isArray(any)) {
                    Object array = AnyConverter.toArray(any);
                    if (array instanceof byte[]) {
                        statement.setBytes(index, (byte[])array);
                    }
                    else {
                        success = false;
                    }
                }
                else {
                    success = false;
                }
                break;
            case TypeClass.STRUCT_value:
                if (any instanceof Date) {
                    statement.setObject(index, UnoHelper.getJavaLocalDate((Date) any));
                }
                else if (any instanceof Time) {
                    statement.setObject(index, UnoHelper.getJavaLocalTime((Time) any));
                }
                else if (any instanceof DateTime) {
                    statement.setObject(index, UnoHelper.getJavaLocalDateTime((DateTime) any));
                }
                else if (any instanceof DateWithTimezone) {
                    DateWithTimezone date = (DateWithTimezone) any;
                    statement.setObject(index, UnoHelper.getJavaLocalDate(date.DateInTZ));
                }
                else if (any instanceof TimeWithTimezone) {
                    TimeWithTimezone time = (TimeWithTimezone) any;
                    statement.setObject(index, UnoHelper.getJavaOffsetTime(time));
                }
                else if (any instanceof DateTimeWithTimezone) {
                    DateTimeWithTimezone datetime = (DateTimeWithTimezone) any;
                    statement.setObject(index, UnoHelper.getJavaOffsetDateTime(datetime));
                }
                else {
                    success = false;
                }
                break;
            case TypeClass.INTERFACE_value:
                XInputStream stream = UnoRuntime.queryInterface(XInputStream.class, AnyConverter.toObject(Object.class, any));
                if (stream != null) {
                    InputStream input = new XInputStreamToInputStreamAdapter(stream);
                    statement.setBinaryStream(index, input, input.available());
                }
                else {
                    success = false;
                }
                break;
            default:
                success = false;
            }
            return success;
        }
        catch (java.sql.SQLException | IllegalArgumentException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    public static Object getObject(Object object,
                                   XNameAccess map)
    {
        Object value = Any.VOID;
        if (object == null) {
            return value;
        }
        if (object instanceof String) {
            value = (String) object;
        }
        else if (object instanceof Boolean) {
            value = (Boolean) object;
        }
        else if (object instanceof Integer) {
            value = (Integer) object;
        }
        else if (object instanceof java.time.OffsetTime) {
            value = UnoHelper.getTimeWithTimezone((java.time.OffsetTime) object);
        }
        else if (object instanceof java.time.OffsetDateTime) {
            value = UnoHelper.getDateTimeWithTimezone((java.time.OffsetDateTime) object);
        }
        else if (object instanceof java.time.LocalDate) {
            value = UnoHelper.getUnoDate((java.time.LocalDate) object);
        }
        else if (object instanceof java.time.LocalTime) {
            value = UnoHelper.getUnoTime((java.time.LocalTime) object);
        }
        else if (object instanceof java.time.LocalDateTime) {
            value = UnoHelper.getDateTime((java.time.LocalDateTime) object);
        }
        else if (object instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) object;
            value = UnoHelper.getUnoDate(date.toLocalDate());
        }
        else if (object instanceof java.sql.Time) {
            java.sql.Time time = (java.sql.Time) object;
            value = UnoHelper.getUnoTime(time.toLocalTime());
        }
        else if (object instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) object;
            value = UnoHelper.getUnoDateTime(timestamp.toLocalDateTime());
        }
        return value;
    }

    public static String buildName(ConnectionSuper connection,
                                   java.sql.ResultSet result,
                                   ComposeRule rule)
        throws SQLException
    {
        try {
            String catalog = result.getString(1);
            if (result.wasNull()) {
                catalog = "";
            }
            String schema = result.getString(2);
            if (result.wasNull()) {
                schema = "";
            }
            String table = result.getString(3);
            if (result.wasNull()) {
                table = "";
            }
            return composeTableName(connection, catalog, schema, table, false, rule);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    public static boolean useBookmarks(ConnectionBase connection) {
        RowIdLifetime lifetime = RowIdLifetime.ROWID_UNSUPPORTED;
        try {
            lifetime = connection.getProvider().getConnection().getMetaData().getRowIdLifetime();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (lifetime != RowIdLifetime.ROWID_UNSUPPORTED);
    }
}
