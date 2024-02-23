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

import java.io.InputStream;
import java.sql.RowIdLifetime;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XRow;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.XInterface;
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
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.ColumnContainerBase.ExtraColumnInfo;
import io.github.prrvchr.uno.sdbcx.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.TableSuper;
import io.github.prrvchr.uno.sdbcx.TableMain;


public class DBTools
{

    public static class NameComponentSupport
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


    public static NameComponentSupport getNameComponentSupport(ConnectionBase connection,
                                                               ComposeRule rule)
        throws SQLException
    {
        try {
            return getNameComponentSupport(connection.getProvider(), rule);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    public static NameComponentSupport getNameComponentSupport(DriverProvider provider,
                                                               ComposeRule rule)
        throws java.sql.SQLException
    {
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
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
            return new NameComponentSupport(true, true);
        default:
            throw new UnsupportedOperationException("Invalid/unknown enum value");
        }
    }

    /** compose a complete column name from it's up to four parts, regarding to the database meta data composing rules
     */

    public static String composeColumnName(ConnectionBase connection,
                                           String catalog,
                                           String schema,
                                           String table,
                                           String column,
                                           boolean sensitive,
                                           ComposeRule composeRule)
        throws SQLException
    {
        String name = composeTableName(connection, catalog, schema, table, sensitive, composeRule);
        return composeColumnName(connection, name, column, sensitive);
    }

    public static String composeColumnName(ConnectionBase connection,
                                           String table,
                                           String column,
                                           boolean sensitive)
        throws SQLException
    {
        StringBuilder buffer = new StringBuilder(table);
        buffer.append('.');
        buffer.append(quoteName(connection, column, sensitive));
        return buffer.toString();
    }


    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules
     */
    public static String composeTableName(ConnectionBase m_connection,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean sensitive,
                                          ComposeRule composeRule)
        throws SQLException
    {
        if (m_connection == null) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        NameComponentSupport nameComponentSupport = getNameComponentSupport(m_connection, composeRule);
        try {
            java.sql.DatabaseMetaData metadata = m_connection.getProvider().getConnection().getMetaData();
            String quote = m_connection.getProvider().getIdentifierQuoteString();
            
            
            String catalogSeparator = "";
            boolean catalogAtStart = true;
            if (!catalog.isEmpty() && nameComponentSupport.useCatalogs) {
                catalogSeparator = metadata.getCatalogSeparator();
                catalogAtStart = metadata.isCatalogAtStart();
                if (catalogAtStart && !catalogSeparator.isEmpty()) {
                    buffer.append(quoteName(quote, catalog, sensitive));
                    buffer.append(catalogSeparator);
                }
            }
            if (!schema.isEmpty() && nameComponentSupport.useSchemas) {
                buffer.append(quoteName(quote, schema, sensitive));
                buffer.append('.');
            }
            buffer.append(sensitive ? quoteName(quote, table) : table);
            if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && nameComponentSupport.useCatalogs) {
                buffer.append(catalogSeparator);
                buffer.append(quoteName(quote, catalog, sensitive));
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
        System.out.println("DataBaseTools.composeTableName(): Name: " + buffer.toString());
        return buffer.toString();
    }

    public static String composeTableName(ConnectionSuper connection,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean sensitive,
                                          ComposeRule rule)
        throws SQLException
    {
        NameComponentSupport support = getNameComponentSupport(connection, rule);
        return doComposeTableName(connection,
                                  support,
                                  catalog,
                                  schema,
                                  table,
                                  sensitive);
    }

    public static String composeTableName(ConnectionSuper connection,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean catalog,
                                          boolean schema,
                                          boolean sensitive)
        throws SQLException
    {
        NameComponentSupport support = getNameComponentSupport(connection, rule);
        NameComponents component = getTableNameComponents(connection, table);
        return doComposeTableName(connection,
                                  support,
                                  catalog ? component.getCatalog() : "",
                                  schema ? component.getSchema() : "",
                                  component.getTable(),
                                  sensitive);
    }

    public static String composeTableName(ConnectionSuper connection,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean sensitive)
        throws SQLException
    {
        NameComponents component = getTableNameComponents(connection, table);
        NameComponentSupport support = getNameComponentSupport(connection, rule);
        return doComposeTableName(connection,
                                  support,
                                  support.useCatalogs ? component.getCatalog() : "",
                                  support.useSchemas ? component.getSchema() : "",
                                  component.getTable(),
                                  sensitive);
    }

    public static String composeTableName(ConnectionSuper connection,
                                          XPropertySet table,
                                          NameComponentSupport support,
                                          boolean sensitive)
        throws SQLException
    {
        NameComponents component = getTableNameComponents(connection, table);
        return doComposeTableName(connection,
                                  support,
                                  support.useCatalogs ? component.getCatalog() : "",
                                  support.useSchemas ? component.getSchema() : "",
                                  component.getTable(),
                                  sensitive);
    }

    public static String doComposeTableName(ConnectionSuper connection,
                                            NameComponentSupport support,
                                            String catalog,
                                            String schema,
                                            String table,
                                            boolean sensitive)
    {
        StringBuilder buffer = new StringBuilder();
        UnoHelper.ensure(!table.isEmpty(), "At least the table name should be non-empty", connection.getLogger());
        String quote = connection.getProvider().getIdentifierQuoteString();
        
        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && support.useCatalogs) {
            catalogSeparator = connection.getProvider().getCatalogSeparator();
            catalogAtStart = connection.getProvider().isCatalogAtStart();
            
            if (catalogAtStart && !catalogSeparator.isEmpty()) {
                buffer.append(quoteName(quote, catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }
        
        if (!schema.isEmpty() && support.useSchemas) {
            buffer.append(quoteName(quote, schema, sensitive));
            buffer.append(".");
        }
        
        buffer.append(sensitive ? quoteName(quote, table) : table);
        
        if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && support.useCatalogs) {
            buffer.append(catalogSeparator);
            buffer.append(quoteName(quote, catalog, sensitive));
        }
        return buffer.toString();
    }

    public static Object[] getRenameTableArguments(ConnectionSuper connection,
                                                   NameComponents newname,
                                                   TableMain table,
                                                   String fullname,
                                                   boolean reversed,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted full old table name
        args.add(quoteTableName(connection, fullname, rule, sensitive));
        // TODO: {1} quoted new schema name
        args.add(quoteName(connection, newname.getSchema(), sensitive));
        // TODO: {2} quoted full old table name overwritten with the new schema name
        args.add(composeTableName(connection, table.getCatalogName(), newname.getSchema(), table.getName(), sensitive, rule));
        // TODO: {3} quoted new table name
        args.add(quoteName(connection, newname.getTable(), sensitive));
        // TODO: {4} quoted full old table name overwritten with the new table name
        args.add(composeTableName(connection, table.getCatalogName(), table.getSchemaName(), newname.getTable(), sensitive, rule));
        // TODO: {5} quoted full new table name
        args.add(composeTableName(connection, newname.getCatalog(), newname.getSchema(), newname.getTable(), sensitive, rule));
        if (reversed) {
            String buffers = args.get(0);
            args.set(0, args.get(4));
            args.set(4, args.get(2));
            args.set(2, buffers);
        }
        return args.toArray(new Object[0]);
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
                                                   String table,
                                                   boolean sensitive)
        throws SQLException
    {
        boolean usecatalog = UnoHelper.getDefaultPropertyValue(connection.getInfo(), "UseCatalogInSelect", true);
        boolean useschema = UnoHelper.getDefaultPropertyValue(connection.getInfo(), "UseSchemaInSelect", true);
        return composeTableName(connection, usecatalog ? catalog : "", useschema ? schema : "", table, sensitive, ComposeRule.InDataManipulation);
    }

    /** composes a table name for usage in a SELECT statement
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(ConnectionSuper connection,
                                                   XPropertySet table,
                                                   boolean sensitive)
        throws SQLException
    {
        NameComponents component = getTableNameComponents(connection, table);
        return composeTableNameForSelect(connection, component.getCatalog(), component.getSchema(), component.getTable(), sensitive);
    }

    public static NameComponents getTableNameComponents(ConnectionSuper connection,
                                                        XPropertySet table)
        throws SQLException
    {
        try {
            NameComponents component = new NameComponents();
            XPropertySetInfo info = table.getPropertySetInfo();
            if (info != null && hasDescriptorProperty(info, PropertyIds.NAME)) {
                if (hasDescriptorProperty(info, PropertyIds.CATALOGNAME) &&
                    hasDescriptorProperty(info, PropertyIds.SCHEMANAME)) {
                    component.setCatalog(getDescriptorStringValue(table, PropertyIds.CATALOGNAME));
                    component.setSchema(getDescriptorStringValue(table, PropertyIds.SCHEMANAME));
                }
                component.setTable(getDescriptorStringValue(table, PropertyIds.NAME));
            }
            else {
                UnoHelper.ensure(false, "this is not a table object", connection.getLogger());
            }
            return component;
        }
        catch (IllegalArgumentException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
    }

    // quote the given name with the given quote string.

    public static String quoteName(ConnectionBase connection,
                                   String name,
                                   boolean sensitive)
    {
        if (sensitive) {
            return quoteName(connection, name);
        }
        return name;
    }

    public static String quoteName(String quote,
                                   String name,
                                   boolean sensitive)
    {
        if (sensitive) {
            return quoteName(quote, name);
        }
        return name;
    }

    public static String quoteName(ConnectionBase connection,
                                   String name)
    {
        String quote = connection.getProvider().getIdentifierQuoteString();
        return quoteName(quote, name);
    }
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
    public static String quoteTableName(ConnectionBase connection,
                                        String name,
                                        ComposeRule rule,
                                        boolean sensitive)
        throws SQLException
    {
        if (sensitive) {
            NameComponents nameComponents = qualifiedNameComponents(connection, name, rule);
            name = composeTableName(connection, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(), true, rule);
        }
        return name;
    }

    /** unquote the given table name (which may contain a catalog and a schema)
     */
    public static String unQuoteTableName(ConnectionSuper connection,
                                          String name)
    {
        String quote = connection.getProvider().getIdentifierQuoteString();
        return name.replace(quote, "");
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param metadata     meta data describing the connection where you got the table name from
     * @param name     fully qualified table name
     * @param rule       where do you need the name for
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NameComponents qualifiedNameComponents(ConnectionBase connection,
                                                         String name,
                                                         ComposeRule rule)
        throws SQLException
    {
        NameComponents component = new NameComponents();
        NameComponentSupport support = getNameComponentSupport(connection, rule);
        String separator = connection.getProvider().getCatalogSeparator();
        String buffer = name;
        // do we have catalogs ?
        if (support.useCatalogs) {
            if (connection.getProvider().isCatalogAtStart()) {
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
     * @param table
     *    Allow to add special SQL constructs.
     * @param pattern
     *   
     * @return
     *   The CREATE TABLE statement.
     * @throws SQLException
     */
    public static List<String> getCreateTableQueries(ConnectionSuper connection,
                                                     XPropertySet descriptor,
                                                     String table,
                                                     boolean sensitive)
        throws SQLException
    {
        String separator = ", ";
        boolean hasAutoIncrement = false;
        List<String> parts = new ArrayList<String>();
        List<String> queries = new ArrayList<String>();
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
                XPropertySet column = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                if (column == null) {
                    continue;
                }
                
                if (connection.getProvider().supportsColumnDescription()) {
                    String comment = getDescriptorStringValue(column, PropertyIds.DESCRIPTION);
                    if (!comment.isEmpty()) {
                        String name = composeColumnName(connection, table, getDescriptorStringValue(column, PropertyIds.NAME), sensitive);
                        queries.add(getCommentQuery("COLUMN", name, comment));
                    }
                }
                final StringBuilder buffer = new StringBuilder();
                hasAutoIncrement |= _getStandardColumnPartQuery(buffer, connection, column, sensitive);
                parts.add(buffer.toString());
            }
        }
        catch (IllegalArgumentException | WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }

        System.out.println("DataBaseTools.getCreateTableQueries() 1 isAutoIncrementIsPrimaryKey: " + connection.getProvider().isAutoIncrementIsPrimaryKey());
        // The primary key will not be created if one of the columns is auto increment
        // and the auto increment are primary keys (ie: Sqlite)
        if (!connection.getProvider().isAutoIncrementIsPrimaryKey() || !hasAutoIncrement) {
            parts.addAll(getCreateTableKeyParts(connection, descriptor, sensitive));
        }
        queries.add(0, String.format(connection.getProvider().getCreateTableQuery(), table, String.join(separator, parts)));
        return queries;
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
                                                    boolean sensitive)
        throws SQLException
    {
        final StringBuilder buffer = new StringBuilder();
        _getStandardColumnPartQuery(buffer, connection, column, sensitive);
        return buffer.toString();

    }

    private static boolean _getStandardColumnPartQuery(StringBuilder buffer,
                                                      ConnectionSuper connection,
                                                      XPropertySet column,
                                                      boolean sensitive)
        throws SQLException
    {
        boolean hasAutoIncrementValue = false;
        try {
            String name = getDescriptorStringValue(column, PropertyIds.NAME);
            buffer.append(quoteName(connection, name, sensitive));
            buffer.append(' ');
            String typename = getDescriptorStringValue(column, PropertyIds.TYPENAME);
            int datatype = getDescriptorIntegerValue(column, PropertyIds.TYPE);
            int precision = getDescriptorIntegerValue(column, PropertyIds.PRECISION);
            int scale = getDescriptorIntegerValue(column, PropertyIds.SCALE);
            boolean isAutoIncrement = getDescriptorBooleanValue(column, PropertyIds.ISAUTOINCREMENT);
            String autoIncrementValue = "";
            System.out.println("DataBaseTools.getStandardColumnPartQuery() 1 TYPENAME: " + typename + " - TYPE: " + datatype + " - PRECISION: " + precision + " - SCALE: " + scale);
            
            // Check if the user enter a specific string to create auto increment values
            XPropertySetInfo info = column.getPropertySetInfo();
            if (info != null && hasDescriptorProperty(info, PropertyIds.AUTOINCREMENTCREATION)) {
                autoIncrementValue = getDescriptorStringValue(column, PropertyIds.AUTOINCREMENTCREATION);
                hasAutoIncrementValue = !autoIncrementValue.isEmpty();
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
                    XRow row = UnoRuntime.queryInterface(XRow.class, resultset);
                    while (resultset.next()) {
                        String typename2cmp = row.getString(1);
                        int type2cmp = row.getShort(2);
                        prefix = row.getString(4);
                        postfix = row.getString(5);
                        createparams = row.getString(6);
                        // first identical type will be used if typename is empty
                        if (typename.isEmpty() && type2cmp == datatype) {
                            typename = typename2cmp;
                        }
                        System.out.println("DataBaseTools.getStandardColumnPartQuery() 2 typename: " + typename + " - typename2cmp: " + typename2cmp + " - type2cmp: " + type2cmp + " - datatype: " + datatype + " - createparams: " + createparams);
                        if (typename.equalsIgnoreCase(typename2cmp) && type2cmp == datatype && !createparams.isEmpty() && !row.wasNull()) {
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
            
            String defaultvalue = getDescriptorStringValue(column, PropertyIds.DEFAULTVALUE);
            System.out.println("DataBaseTools.getStandardColumnPartQuery() DEFAULT: " + defaultvalue + " - PREFIX: " + prefix + " - POSTFIX: " + postfix + " - PARAM: " + createparams);
            if (!defaultvalue.isEmpty()) {
                buffer.append(" DEFAULT ");
                buffer.append(prefix);
                buffer.append(defaultvalue);
                buffer.append(postfix);
            }
            int isnullable = getDescriptorIntegerValue(column, PropertyIds.ISNULLABLE);
            if (isnullable == ColumnValue.NO_NULLS) {
                buffer.append(" NOT NULL");
            }
            
            if (isAutoIncrement && hasAutoIncrementValue) {
                buffer.append(' ');
                buffer.append(autoIncrementValue);
            }
        }
        catch (IllegalArgumentException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        return hasAutoIncrementValue;
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
                                                      XPropertySet descriptor,
                                                      boolean sensitive)
        throws SQLException
    {
        List<String> parts = new ArrayList<String>();
        try {
            XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
            XIndexAccess keys = keysSupplier.getKeys();
            if (keys != null) {
                boolean hasPrimaryKey = false;
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        StringBuilder buffer = new StringBuilder();
                        int keyType = getDescriptorIntegerValue(columnProperties, PropertyIds.TYPE);
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
                            buffer.append(getColumnNames(connection, columns, sensitive));
                        }
                        else if (keyType == KeyType.UNIQUE) {
                            buffer.append("UNIQUE");
                            buffer.append(getColumnNames(connection, columns, sensitive));
                        }
                        else if (keyType == KeyType.FOREIGN) {
                            int deleteRule = getDescriptorIntegerValue(columnProperties, PropertyIds.DELETERULE);
                            buffer.append("FOREIGN KEY");
                            
                            String referencedTable = getDescriptorStringValue(columnProperties, PropertyIds.REFERENCEDTABLE);
                            NameComponents nameComponents = qualifiedNameComponents(connection, referencedTable, ComposeRule.InDataManipulation);
                            String composedName = composeTableName(connection, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(),
                                                                   true, ComposeRule.InTableDefinitions);
                            if (composedName.isEmpty()) {
                                throw new SQLException();
                            }
                            
                            buffer.append(getColumnNames(connection, columns, sensitive));
                            
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
        }
        catch (UnknownPropertyException | IllegalArgumentException |
               WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        return parts;
    }

    private static String getColumnNames(ConnectionSuper connection,
                                         XIndexAccess columns,
                                         boolean sensitive)
        throws SQLException,
               WrappedTargetException,
               UnknownPropertyException,
               IllegalArgumentException,
               IndexOutOfBoundsException
    {
        String separator = ", ";
        String quote = connection.getProvider().getIdentifierQuoteString();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < columns.getCount(); i++) {
            XPropertySet properties = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
            if (properties != null) {
                String name = getDescriptorStringValue(properties, PropertyIds.NAME);
                names.add(quoteName(quote, name, sensitive));
            }
        }
        StringBuilder buffer = new StringBuilder(" (");
        if (!names.isEmpty()) {
            buffer.append(String.join(separator, names));
        }
        buffer.append(")");
        return buffer.toString();
    }

    /** creates a SQL CREATE VIEW statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new view.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return
     *   The CREATE VIEW statement.
     */
    public static String getCreateViewQuery(ConnectionSuper connection,
                                            XPropertySet descriptor,
                                            boolean sensitive)
        throws SQLException
    {
        try {
            String view = composeTableName(connection, descriptor, ComposeRule.InTableDefinitions, sensitive);
            String command = getDescriptorStringValue(descriptor, PropertyIds.COMMAND);
            return String.format("CREATE VIEW %s AS %s", view, command);
        }
        catch (IllegalArgumentException e) {
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
        String password = "";
        try {
            password = getDescriptorStringValue(descriptor, PropertyIds.PASSWORD);
            password = password.isBlank() ? "" : password;
        }
        catch (IllegalArgumentException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
        }
        if (sensitive) {
            name = quoteName(connection, name);
        }
        else {
            name = name.toUpperCase();
        }
        return String.format("CREATE USER %s PASSWORD '%s'", name, password);
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
        if (sensitive) {
            name = quoteName(connection, name);
        }
        return String.format("DROP USER %s", name);
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
    {
        name = quoteName(connection, name, sensitive);
        password = password.isBlank() ? "" : password;
        return String.format("ALTER USER %s SET PASSWORD '%s'", name, password);
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
        if (sensitive) {
            name = quoteName(connection, name);
        }
        else {
            name = name.toUpperCase();
        }
        return String.format("CREATE ROLE %s", name);
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
        if (sensitive) {
            name = quoteName(connection, name);
        }
        return String.format("DROP ROLE %s", name);
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
        if (sensitive) {
            String quote = connection.getProvider().getIdentifierQuoteString();
            group = quoteName(quote, group);
            user = quoteName(quote, user);
        }
        return String.format("GRANT %s TO %s", group, user);
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
        if (sensitive) {
            String quote = connection.getProvider().getIdentifierQuoteString();
            group = quoteName(quote, group);
            user = quoteName(quote, user);
        }
        String query = connection.getProvider().getRevokeRoleQuery();
        return String.format(query, group, user);
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
        NameComponents component = DBTools.qualifiedNameComponents(connection, name, ComposeRule.InDataManipulation);
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
            throw UnoHelper.getSQLException(e, null);
        }
        return privilege;
    }

    public static String getGrantPrivilegesQuery(ConnectionSuper connection,
                                                 String grantee,
                                                 String name,
                                                 int privilege,
                                                 ComposeRule rule,
                                                 boolean sensitive)
        throws SQLException
    {
        String separator = ", ";
        StringBuilder buffer = new StringBuilder("GRANT ");
        buffer.append(String.join(separator, connection.getProvider().getPrivileges(privilege)));
        buffer.append(" ON ");
        buffer.append(quoteTableName(connection, name, rule, sensitive));
        buffer.append(" TO ");
        buffer.append(quoteName(connection, grantee, sensitive));
        return buffer.toString();
    }

    public static String revokeTableOrViewPrivileges(ConnectionSuper connection,
                                                     String grantee,
                                                     String name,
                                                     int privilege,
                                                     ComposeRule rule,
                                                     boolean sensitive)
        throws SQLException
    {
        List<String> values = connection.getProvider().getPrivileges(privilege);
        String table = quoteTableName(connection, name, rule, sensitive);
        grantee = quoteName(connection, grantee, sensitive);
        String query = connection.getProvider().getRevokeTableOrViewPrivileges(values, table, grantee);
        System.out.println("DataBaseTools.revokeTableOrViewPrivileges() SQL: " + query);
        return query;
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
                    for (int i = 0; i < keys.getCount(); i++) {
                        XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                        if (descriptor != null) {
                            int keyType = 0;
                            keyType = getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
                            if (keyType == KeyType.PRIMARY) {
                                XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
                                keyColumns = columnsSupplier.getColumns();
                                break;
                            }
                        }
                    }
                }
            }
            return keyColumns;
        }
        catch (IndexOutOfBoundsException | IllegalArgumentException | WrappedTargetException e) {
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
                    keyType = getDescriptorIntegerValue(propertySet, PropertyIds.TYPE);
                    if (keyType == KeyType.PRIMARY) {
                        XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, propertySet);
                        keyColumns = columnsSupplier.getColumns();
                        break;
                    }
                }
            }
            return keyColumns;
        }
        catch (IndexOutOfBoundsException | IllegalArgumentException | WrappedTargetException e) {
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
                XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, sourceColumns.getByIndex(i));
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

    public static boolean hasDescriptorProperty(XPropertySetInfo properties,
                                                PropertyIds pid)
    {
        return properties.hasPropertyByName(pid.name);
    }

    public static String getDescriptorStringValue(XPropertySet properties,
                                                  PropertyIds pid)
    {
        try {
            return getDescriptorStringValue(properties, pid, null);
        }
        catch (SQLException e) {
            return "";
        }
    }
    public static String getDescriptorStringValue(XPropertySet properties,
                                                  PropertyIds pid,
                                                  XInterface source)
        throws SQLException
    {
        try {
            return AnyConverter.toString(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new SQLException("Error", source, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    public static boolean getDescriptorBooleanValue(XPropertySet properties,
                                                    PropertyIds pid)
    {
        try {
            return getDescriptorBooleanValue(properties, pid, null);
        }
        catch (SQLException e) {
            return false;
        }
    }
    public static boolean getDescriptorBooleanValue(XPropertySet properties,
                                                    PropertyIds pid,
                                                    XInterface source)
        throws SQLException
    {
        try {
            return AnyConverter.toBoolean(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new SQLException("Error", source, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet properties,
                                                    PropertyIds pid)
    {
        try {
            return getDescriptorIntegerValue(properties, pid, null);
        }
        catch (SQLException e) {
            return 0;
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet properties,
                                                    PropertyIds pid,
                                                    XInterface source)
        throws SQLException
    {
        try {
            return AnyConverter.toInt(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new SQLException("Error", source, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    public static String getCommentQuery(String on, String name, String comment) {
        StringBuilder buffer = new StringBuilder("COMMENT ON ");
        buffer.append(on);
        buffer.append(" ");
        buffer.append(name);
        buffer.append(" IS '");
        buffer.append(comment.replace("'","''"));
        buffer.append("'");
        return buffer.toString();
    }

    public static List<String> getAlterColumnQueries(ConnectionSuper connection,
                                                     TableSuper table,
                                                     XPropertySet descriptor1,
                                                     XPropertySet descriptor2,
                                                     boolean sensitive)
        throws SQLException
    // TODO: see: libreoffice/connectivity/source/drivers/postgresql/
    // TODO: file: pq_xcolumns.cxx method: void alterColumnByDescriptor()
    // FIXME: Added the possibility of changing column type if the contained data is castable
    {
        String name = DBTools.composeTableName(connection, table, ComposeRule.InTableDefinitions, sensitive);

        List<String> queries = new ArrayList<String>();
        String quote = connection.getProvider().getIdentifierQuoteString();
        String name1 = getDescriptorStringValue(descriptor1, PropertyIds.NAME);
        String name2 = getDescriptorStringValue(descriptor2, PropertyIds.NAME);

        if (name1.isEmpty()) {
            // create a new column
            StringBuilder buffer = new StringBuilder("ALTER TABLE ");
            buffer.append(name);
            buffer.append(" ADD COLUMN ");
            buffer.append(getStandardColumnPartQuery(connection, descriptor2, sensitive));
            queries.add(buffer.toString());
        }
        else {
            if(!name1.equals(name2)) {
                // rename a column
                StringBuilder buffer = new StringBuilder("ALTER TABLE ");
                buffer.append(name);
                buffer.append(" ALTER COLUMN ");
                buffer.append(quoteName(quote, name1, sensitive));
                buffer.append(" RENAME TO ");
                buffer.append(quoteName(quote, name2, sensitive));
                queries.add(buffer.toString());
            }

            String type1 = getDescriptorStringValue(descriptor1, PropertyIds.TYPENAME);
            String type2 = getDescriptorStringValue(descriptor2, PropertyIds.TYPENAME);
            String default1 = getDescriptorStringValue(descriptor1, PropertyIds.DEFAULTVALUE);
            String default2 = getDescriptorStringValue(descriptor2, PropertyIds.DEFAULTVALUE);
            if (!type2.equals(type1) || !default2.equals(default1)) {
                StringBuilder buffer = new StringBuilder("ALTER TABLE ");
                buffer.append(name);
                buffer.append(" ALTER COLUMN ");
                buffer.append(getStandardColumnPartQuery(connection, descriptor2, sensitive));
                queries.add(buffer.toString());
            }
            else {
                int nullable1 = getDescriptorIntegerValue(descriptor1, PropertyIds.ISNULLABLE);
                int nullable2 = getDescriptorIntegerValue(descriptor2, PropertyIds.ISNULLABLE);
                if (nullable2 != nullable1) {
                    StringBuilder buffer = new StringBuilder("ALTER TABLE ");
                    buffer.append(name);
                    buffer.append(" ALTER COLUMN ");
                    buffer.append(quoteName(quote, name2, sensitive));
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

        if (connection.getProvider().supportsColumnDescription()) {
            String comment1 = getDescriptorStringValue(descriptor1, PropertyIds.DESCRIPTION);
            String comment2 = getDescriptorStringValue(descriptor2, PropertyIds.DESCRIPTION);
            if (!comment2.equals(comment1)) {
                StringBuilder buffer = new StringBuilder(name);
                buffer.append(".");
                buffer.append(quoteName(quote, name2, sensitive));
                queries.add(getCommentQuery("COLUMN", buffer.toString(), comment2));
            }
        }
        return queries;
    }

    public static boolean executeDDLQuery(ConnectionSuper connection,
                                          String query,
                                          ConnectionLog logger,
                                          String cls,
                                          String method,
                                          int resource,
                                          Object... arguments)
        throws SQLException
    {
        if (query.isBlank()) {
            return false;
        }
        boolean autocommit = false;
        boolean support = connection.getProvider().supportsTransactions();
        java.sql.Connection jdbc = connection.getProvider().getConnection();
        try (java.sql.Statement statement = jdbc.createStatement()) {
            if (support) {
                autocommit = jdbc.getAutoCommit();
                jdbc.setAutoCommit(false);
            }
            System.out.println("DBTools.executeStatement 2 Query: " + query);
            logger.logprb(LogLevel.FINE, cls, method, resource, _addToArgs(arguments, query));
            statement.executeUpdate(query);
            if (support) {
                jdbc.commit();
                jdbc.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e) {
            if (support) {
                try {
                    jdbc.rollback();
                }
                catch (java.sql.SQLException ex) {
                    // pass
                }
            }
            String message = logger.getStringResource(resource + 1, _addToArgs(arguments, e.getMessage()));
            logger.logp(LogLevel.SEVERE, cls, method, message);
            throw new SQLException(message);
        }
        return true;

    }

    public static boolean executeDDLQueries(ConnectionSuper connection,
                                            List<String> queries,
                                            ConnectionLog logger,
                                            String cls,
                                            String method,
                                            int resource,
                                            Object... arguments)
        throws SQLException
    {
        int count = 0;
        boolean autocommit = false;
        boolean support = connection.getProvider().supportsTransactions();
        java.sql.Connection con = connection.getProvider().getConnection();
        try (java.sql.Statement statement = con.createStatement()) {
            if (support) {
                autocommit = con.getAutoCommit();
                con.setAutoCommit(false);
            }
            for (String query : queries) {
                if (query.isBlank()) {
                    continue;
                }
                System.out.println("DBTools.executeStatements 2 Query: " + query);
                logger.logprb(LogLevel.FINE, cls, method, resource, _addToArgs(arguments, query));
                statement.executeUpdate(query);
                count ++;
            }
            if (support) {
                con.commit();
                con.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e) {
            if (support) {
                try {
                    con.rollback();
                }
                catch (java.sql.SQLException ex) {
                    // pass
                }
            }
            String message = logger.getStringResource(resource + 1, _addToArgs(arguments, e.getMessage()));
            logger.logp(LogLevel.SEVERE, cls, method, message);
            throw new SQLException(message);
        }
        return count > 0;
    }

    private static Object[] _addToArgs(Object[] arguments, Object... options)
    {
        List<Object> list = new ArrayList<Object>(Arrays.asList(arguments));
        for (Object option : options) {
            list.add(option);
        }
        return list.toArray(new Object[list.size()]);
    }


    public static java.sql.ResultSet getGeneratedKeys(StatementMain statement,
                                                      String method,
                                                      String sql,
                                                      String upsert)
        throws java.sql.SQLException
    {
        int resource;
        String query = "SELECT 1 WHERE 0 = 1";
        if (statement.getStatement() != null) {
            //String sql = provider.getAutoRetrievingStatement();
            if (sql.isBlank()) {
                return statement.getStatement().getGeneratedKeys();
            }
            DBQueryParser parser = new DBQueryParser(upsert);
            if (parser.isExecuteUpdateStatement() && parser.hasTable()) {
                String table = parser.getTable();
                resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_TABLE;
                statement.getLogger().logprb(LogLevel.FINE, statement.getClass().getName(), method, resource, table, upsert);
                String keys = getGeneratedKeys(statement.getStatement());
                if (!keys.isBlank()) {
                    query = String.format(sql, table, keys);
                }
            }
        }
        resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_QUERY;
        statement.getLogger().logprb(LogLevel.FINE, statement.getClass().getName(), method, resource, query);
        return statement.getGeneratedStatement().executeQuery(query);
    }

    private static String getGeneratedKeys(java.sql.Statement statement)
    {
        String keys = "";
        try(java.sql.ResultSet result = statement.getGeneratedKeys()) {
            java.sql.ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            List<String> rows = new ArrayList<String>();
            while (result.next()) {
                List<String> columns = new ArrayList<String>();
                for (int i = 1; i <= count; i++) {
                    StringBuilder buffer = new StringBuilder(5);
                    buffer.append(statement.enquoteIdentifier(metadata.getColumnName(i), true));
                    buffer.append(" = ");
                    String value = String.format("%s", result.getObject(i));
                    if (metadata.getColumnClassName(i).equals("java.lang.String")) {
                        value = statement.enquoteLiteral(value);
                    }
                    buffer.append(value);
                    columns.add(buffer.toString());
                }
                String row = null;
                if (columns.size() > 1) {
                    StringBuilder buffer = new StringBuilder(3);
                    buffer.append("(");
                    buffer.append(String.join(" AND ", columns));
                    buffer.append(")");
                    row = buffer.toString();
                }
                else if (!columns.isEmpty()){
                    row = columns.get(0);
                }
                if (row != null) {
                    rows.add(row);
                }
            }
            keys = String.join(" OR ", rows);
        }
        catch (java.sql.SQLException e) { 
            // pass
        }
        return keys;
    }

    public static int getPrivileges(Connection connection,
                                    String catalog,
                                    String schema,
                                    String table)
        throws WrappedTargetException
    {
        int privileges = 0;
        try {
            String name = connection.getMetaData().getUserName();
            if (name != null && !name.isBlank()) {
                XGroupsSupplier groups = (XGroupsSupplier) AnyConverter.toObject(XGroupsSupplier.class, connection.getUsers().getByName(name));
                List<String> grantees = new ArrayList<>(List.of(name));
                grantees.addAll(Arrays.asList(groups.getGroups().getElementNames()));
                privileges = getTableOrViewPrivileges(connection, grantees, catalog, schema, table);
            }
        }
        catch (NoSuchElementException | SQLException e) {
            System.out.println("DBTools.getPrivileges() 1 ERROR ******************");
            throw UnoHelper.getWrappedException(e);
        }
        catch (Exception e) {
            System.out.println("DBTools.getPrivileges() 2 ERROR ******************");
        }
        return privileges;
    }


}
