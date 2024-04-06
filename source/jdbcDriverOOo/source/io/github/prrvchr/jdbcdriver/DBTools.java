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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.Property;
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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XColumnsSupplier;
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

    public static class NamedComponents
    {
        private String catalog = "";
        private String schema = "";
        private String table = "";

        public NamedComponents(String catalog, String schema, String table)
        {
            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
        }

        public NamedComponents() {
        }

        // Java DataBaseMetadata specific getter
        public String getCatalog() {
            return catalog.isBlank() ? null : catalog;
        }

        // UNO getter (String can not be null)
        public String getCatalogName()
        {
            return catalog;
        }

        public void setCatalog(String catalog)
        {
            this.catalog = catalog;
        }

        // Java DataBaseMetadata specific getter
        public String getSchema() {
            return schema.isBlank() ? null : schema;
        }

        public String getSchemaName()
        {
            return schema;
        }

        public void setSchema(String schema)
        {
            this.schema = schema;
        }

        // Java DataBaseMetadata specific getter
        public String getTable() {
            return table;
        }

        public String getTableName() {
            return table;
        }

        public void setTable(String table)
        {
            this.table = table;
        }
    }

    public static NameComponentSupport getNameComponentSupport(DriverProvider provider,
                                                               ComposeRule rule)
    {
        switch (rule) {
        case InTableDefinitions:
            return new NameComponentSupport(provider.supportsCatalogsInTableDefinitions(),
                                            provider.supportsSchemasInTableDefinitions());
        case InIndexDefinitions:
            return new NameComponentSupport(provider.supportsCatalogsInIndexDefinitions(),
                                            provider.supportsSchemasInIndexDefinitions());
        case InDataManipulation:
            return new NameComponentSupport(provider.supportsCatalogsInDataManipulation(),
                                            provider.supportsSchemasInDataManipulation());
        case InProcedureCalls:
            return new NameComponentSupport(provider.supportsCatalogsInProcedureCalls(),
                                            provider.supportsSchemasInProcedureCalls());
        case InPrivilegeDefinitions:
            return new NameComponentSupport(provider.supportsCatalogsInPrivilegeDefinitions(),
                                            provider.supportsSchemasInPrivilegeDefinitions());
        case Complete:
            return new NameComponentSupport(true, true);
        default:
            throw new UnsupportedOperationException("Invalid/unknown enum value");
        }
    }

    /** compose a complete column name from it's up to two parts, regarding to the database meta data composing rules
     * @throws java.sql.SQLException, SQLException
     */

    public static String composeColumnName(DriverProvider provider,
                                           String table,
                                           String column,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        StringBuilder buffer = new StringBuilder(table);
        buffer.append('.');
        buffer.append(enquoteIdentifier(provider, column, sensitive));
        return buffer.toString();
    }


    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules
     * @throws java.sql.SQLException 
     */
    public static String composeTableName(DriverProvider provider,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean sensitive,
                                          ComposeRule rule)
        throws java.sql.SQLException
    {
        StringBuilder buffer = new StringBuilder();
        NameComponentSupport nameComponentSupport = getNameComponentSupport(provider, rule);

        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && nameComponentSupport.useCatalogs) {
            catalogSeparator = provider.getCatalogSeparator();
            catalogAtStart = provider.isCatalogAtStart();
            if (catalogAtStart && !catalogSeparator.isEmpty()) {
                buffer.append(enquoteIdentifier(provider, catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }
        if (!schema.isEmpty() && nameComponentSupport.useSchemas) {
            buffer.append(enquoteIdentifier(provider, schema, sensitive));
            buffer.append('.');
        }
        buffer.append(enquoteIdentifier(provider, table, sensitive));
        if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && nameComponentSupport.useCatalogs) {
            buffer.append(catalogSeparator);
            buffer.append(enquoteIdentifier(provider, catalog, sensitive));
        }
        System.out.println("DataBaseTools.composeTableName(): Name: " + buffer.toString());
        return buffer.toString();
    }

    public static String buildName(DriverProvider provider,
                                   NamedComponents component,
                                   ComposeRule rule)
        throws java.sql.SQLException
    {
        return buildName(provider, component, rule, false);
    }

    public static String buildName(DriverProvider provider,
                                   NamedComponents component,
                                   ComposeRule rule,
                                   boolean sensitive)
        throws java.sql.SQLException
    {
        return buildName(provider, component.getCatalogName(), component.getSchemaName(), component.getTableName(), rule, sensitive);
    }

    public static String buildName(DriverProvider provider,
                                   String catalog,
                                   String schema,
                                   String table,
                                   ComposeRule rule)
        throws java.sql.SQLException
    {
        return buildName(provider, catalog, schema, table, rule, false);
    }

    public static String buildName(DriverProvider provider,
                                   String catalog,
                                   String schema,
                                   String table,
                                   ComposeRule rule,
                                   boolean sensitive)
        throws java.sql.SQLException
    {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return doComposeTableName(provider, support, catalog, schema, table, sensitive);
    }

    public static String composeTableName(DriverProvider provider,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean catalog,
                                          boolean schema,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        NamedComponents component = getTableNamedComponents(provider, table);
        return doComposeTableName(provider,
                                  support,
                                  catalog ? component.getCatalogName() : "",
                                  schema ? component.getSchemaName() : "",
                                  component.getTableName(),
                                  sensitive);
    }

    public static String composeTableName(DriverProvider provider,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        NamedComponents component = getTableNamedComponents(provider, table);
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return doComposeTableName(provider,
                                  support,
                                  support.useCatalogs ? component.getCatalogName() : "",
                                  support.useSchemas ? component.getSchemaName() : "",
                                  component.getTableName(),
                                  sensitive);
    }

    public static String composeTableName(DriverProvider provider,
                                          XPropertySet table,
                                          NameComponentSupport support,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        NamedComponents component = getTableNamedComponents(provider, table);
        return doComposeTableName(provider,
                                  support,
                                  support.useCatalogs ? component.getCatalogName() : "",
                                  support.useSchemas ? component.getSchemaName() : "",
                                  component.getTableName(),
                                  sensitive);
    }

    public static String doComposeTableName(DriverProvider provider,
                                            NameComponentSupport support,
                                            String catalog,
                                            String schema,
                                            String table,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        StringBuilder buffer = new StringBuilder();
        UnoHelper.ensure(!table.isEmpty(), "At least the table name should be non-empty", provider.getLogger());

        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && support.useCatalogs) {
            catalogSeparator = provider.getCatalogSeparator();
            catalogAtStart = provider.isCatalogAtStart();
            
            if (catalogAtStart && !catalogSeparator.isEmpty()) {
                buffer.append(enquoteIdentifier(provider, catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }

        if (!schema.isEmpty() && support.useSchemas) {
            buffer.append(enquoteIdentifier(provider, schema, sensitive));
            buffer.append(".");
        }

        buffer.append(enquoteIdentifier(provider, table, sensitive));

        if (!catalog.isEmpty() && !catalogAtStart && !catalogSeparator.isEmpty() && support.useCatalogs) {
            buffer.append(catalogSeparator);
            buffer.append(enquoteIdentifier(provider, catalog, sensitive));
        }
        return buffer.toString();
    }

    /** composes a table name for usage in a SELECT statement
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * @throws java.sql.SQLException 
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(DriverProvider provider,
                                                   String catalog,
                                                   String schema,
                                                   String table,
                                                   boolean sensitive)
        throws java.sql.SQLException
    {
        boolean usecatalog = UnoHelper.getDefaultPropertyValue(provider.getInfos(), "UseCatalogInSelect", true);
        boolean useschema = UnoHelper.getDefaultPropertyValue(provider.getInfos(), "UseSchemaInSelect", true);
        return buildName(provider, usecatalog ? catalog : "", useschema ? schema : "", table, ComposeRule.InDataManipulation, sensitive);
    }

    /** composes a table name for usage in a SELECT statement
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * @throws java.sql.SQLException 
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(DriverProvider provider,
                                                   XPropertySet table,
                                                   boolean sensitive)
        throws java.sql.SQLException
    {
        NamedComponents component = getTableNamedComponents(provider, table);
        return composeTableNameForSelect(provider, component.getCatalogName(), component.getSchemaName(), component.getTableName(), sensitive);
    }

    public static NamedComponents getTableNamedComponents(DriverProvider provider,
                                                        XPropertySet table)
    {
        NamedComponents component = new NamedComponents();
        XPropertySetInfo info = table.getPropertySetInfo();
        if (info != null && hasDescriptorProperty(info, PropertyIds.NAME)) {
            if (hasDescriptorProperty(info, PropertyIds.CATALOGNAME)) {
                component.setCatalog(getDescriptorStringValue(table, PropertyIds.CATALOGNAME));
            }
            if (hasDescriptorProperty(info, PropertyIds.SCHEMANAME)) {
                component.setSchema(getDescriptorStringValue(table, PropertyIds.SCHEMANAME));
            }
            component.setTable(getDescriptorStringValue(table, PropertyIds.NAME));
        }
        else {
            UnoHelper.ensure(false, "this is not a table object", provider.getLogger());
        }
        return component;
    }

    // quote the given name with the given quote string.

    public static String enquoteIdentifier(DriverProvider provider,
                                           String name,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        // XXX: enquoteIdentifier don't support blank string (ie: catalog or schema name can be empty)
        if (sensitive && !name.isBlank()) {
            name = provider.getStatement().enquoteIdentifier(name, sensitive);
        }
        return name;
    }

    /** quote the given table name (which may contain a catalog and a schema) according to the rules provided by the meta data
     */
    public static String quoteTableName(DriverProvider provider,
                                        String name,
                                        ComposeRule rule,
                                        boolean sensitive)
        throws java.sql.SQLException
    {
        if (sensitive) {
            NamedComponents nameComponents = qualifiedNameComponents(provider, name, rule);
            name = composeTableName(provider, nameComponents.getCatalogName(), nameComponents.getSchemaName(), nameComponents.getTableName(), true, rule);
        }
        return name;
    }

    /** unquote the given table name (which may contain a catalog and a schema)
     */
    public static String unQuoteTableName(DriverProvider provider,
                                          String name)
    {
        String quote = provider.getIdentifierQuoteString();
        return name.replace(quote, "");
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param metadata     meta data describing the connection where you got the table name from
     * @param name     fully qualified table name
     * @param rule       where do you need the name for
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponents qualifiedNameComponents(DriverProvider provider,
                                                         String name,
                                                         ComposeRule rule)
        throws java.sql.SQLException
    {
        NamedComponents component = new NamedComponents();
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        String separator = provider.getCatalogSeparator();
        String buffer = name;
        // do we have catalogs ?
        if (support.useCatalogs) {
            if (provider.isCatalogAtStart()) {
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
            //UnoHelper.ensure(-1 != index, "QualifiedNameComponents : no schema separator!", provider.getLogger());
            if (index != -1) {
                component.setSchema(buffer.substring(0, index));
                buffer = buffer.substring(index + 1);
            }
        }
        component.setTable(buffer);
        return component;
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
    public static String getCreateViewQuery(DriverProvider provider,
                                            XPropertySet descriptor,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        String view = composeTableName(provider, descriptor, ComposeRule.InTableDefinitions, sensitive);
        String command = getDescriptorStringValue(descriptor, PropertyIds.COMMAND);
        return getCreateViewQuery(provider, view, command);
    }

    /** creates a SQL CREATE VIEW statement
     *
     * @param view
     *    The full cotted view name.
     * @param command
     *    The command of the new view.
     *
     * @return
     *   The CREATE VIEW statement.
     */
    public static String getCreateViewQuery(DriverProvider provider,
                                            NamedComponents component,
                                            String command,
                                            ComposeRule rule,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        String name = composeTableName(provider, component.getCatalogName(), component.getSchemaName(), component.getTableName(), sensitive, rule);
        return getCreateViewQuery(provider, name, command);
    }

    private static String getCreateViewQuery(DriverProvider provider,
                                             String view,
                                             String sql)
    {
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_CREATE_VIEW);
        return formatSQLQuery(command, view, sql);
    }

    public static String getDropViewQuery(DriverProvider provider,
                                          String view)
    {
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_DROP_VIEW);
        return formatSQLQuery(command, view);
    }

    /** returns the primary key columns of the table
     * @throws SQLException 
     */
    public static XNameAccess getPrimaryKeyColumns(XPropertySet table)
        throws java.sql.SQLException
    {
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
        catch (IndexOutOfBoundsException | WrappedTargetException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    /** returns the primary key columns of the table
     */
    public static XNameAccess getPrimaryKeyColumns(XIndexAccess keys)
        throws SQLException {
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
            throw new SQLException(e.getMessage());
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

    public static String buildName(DriverProvider provider,
                                   java.sql.ResultSet result,
                                   ComposeRule rule)
        throws java.sql.SQLException
    {
        String catalog = "";
        String schema = "";
        String table = "";
        catalog = result.getString(1);
        if (result.wasNull()) {
            catalog = "";
        }
        schema = result.getString(2);
        if (result.wasNull()) {
            schema = "";
        }
        table = result.getString(3);
        if (result.wasNull()) {
            table = "";
        }
        return buildName(provider, catalog, schema, table, rule, false);
    }

    public static boolean useBookmarks(DriverProvider provider) {
        RowIdLifetime lifetime = RowIdLifetime.ROWID_UNSUPPORTED;
        try {
            lifetime = provider.getConnection().getMetaData().getRowIdLifetime();
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
        catch (java.sql.SQLException e) {
            return "";
        }
    }
    public static String getDescriptorStringValue(XPropertySet properties,
                                                  PropertyIds pid,
                                                  XInterface source)
        throws java.sql.SQLException
    {
        try {
            return AnyConverter.toString(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean getDescriptorBooleanValue(XPropertySet properties,
                                                    PropertyIds pid)
    {
        try {
            return getDescriptorBooleanValue(properties, pid, null);
        }
        catch (java.sql.SQLException e) {
            return false;
        }
    }
    public static boolean getDescriptorBooleanValue(XPropertySet properties,
                                                    PropertyIds pid,
                                                    XInterface source)
        throws java.sql.SQLException
    {
        try {
            return AnyConverter.toBoolean(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet properties,
                                                    PropertyIds pid)
    {
        try {
            return getDescriptorIntegerValue(properties, pid, null);
        }
        catch (java.sql.SQLException e) {
            return 0;
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet properties,
                                                    PropertyIds pid,
                                                    XInterface source)
        throws java.sql.SQLException
    {
        try {
            return AnyConverter.toInt(properties.getPropertyValue(pid.name));
        }
        catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean executeDDLQuery(DriverProvider provider,
                                          String query)
        throws java.sql.SQLException
    {
        Object[] parameters =  new Object[]{};
        Integer[] positions = new Integer[]{};
        return executeDDLQuery(provider, query, parameters, positions);
    }

    public static boolean executeDDLQuery(DriverProvider provider,
                                          String query,
                                          Object[] parameters,
                                          Integer[] positions)
        throws java.sql.SQLException
    {
        if (query.isBlank()) {
            return false;
        }
        boolean autocommit = false;
        boolean support = provider.supportsTransactions();
        System.out.println("DBTools.executeDDLQuery() Support Transaction:" + support);

        java.sql.Connection jdbc = provider.getConnection();
        try {
            if (support) {
                autocommit = jdbc.getAutoCommit();
                jdbc.setAutoCommit(false);
            }
            try (java.sql.PreparedStatement statement = jdbc.prepareStatement(query)) {
                executeDDL(statement, parameters, positions);
            }
            if (support) {
                jdbc.commit();
                jdbc.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e) {
            if (support) {
                try {
                    jdbc.rollback();
                    jdbc.setAutoCommit(autocommit);
                }
                catch (java.sql.SQLException ex) {
                    e.setNextException(ex);
                }
            }
            throw e;
        }
        return true;

    }

    public static boolean executeDDLQueries(DriverProvider provider,
                                            List<String> queries)
        throws java.sql.SQLException
    {
        Object[] parameters =  new Object[]{};
        List<Integer[]> positions = new ArrayList<Integer[]>();
        return executeDDLQueries(provider, queries, parameters, positions);
    }

    public static boolean executeDDLQueries(DriverProvider provider,
                                            List<String> queries,
                                            Object[] parameters,
                                            List<Integer[]> positions)
        throws java.sql.SQLException
    {
        int count = 0;
        int index = 0;
        boolean autocommit = false;
        boolean support = provider.supportsTransactions();
        System.out.println("DBTools.executeDDLQueries() Support Transaction:" + support);
        java.sql.Connection connection = provider.getConnection();
        try {
            if (support) {
                autocommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
            }
            for (String query : queries) {
                if (query.isBlank()) {
                    index ++;
                    continue;
                }
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                    Integer[] position = (positions.size() > index) ? positions.get(index) : new Integer[]{};
                    executeDDL(statement, parameters, position);
                    index ++;
                    count ++;
                }
            }
            if (support) {
                connection.commit();
                connection.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e) {
            if (support) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(autocommit);
                }
                catch (java.sql.SQLException ex) {
                    e.setNextException(ex);
                }
            }
            throw e;
        }
        return count > 0;
    }

    private static void executeDDL(java.sql.PreparedStatement statement,
                                   Object[] parameters,
                                   Integer[] positions)
        throws java.sql.SQLException
    {
        setStatementParameters(statement, parameters, positions);
        statement.executeUpdate();
    }

    public static void setStatementParameters(java.sql.PreparedStatement statement,
                                              Object[] parameters,
                                              Integer[] positions)
        throws java.sql.SQLException
    {
        int i = 1;
        for (int position : positions) {
            statement.setString(i++, (String) parameters[position]);
        }
    }

    public final static java.sql.ResultSet getGeneratedResult(DriverProvider provider,
                                                              java.sql.Statement statement,
                                                              java.sql.Statement generated,
                                                              ConnectionLog logger,
                                                              String cls,
                                                              String method,
                                                              String command,
                                                              String sql)
        throws java.sql.SQLException
    {
        int resource;
        java.sql.ResultSet result = null;
        String query = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_EMPTY_RESULTSET);
        //String sql = provider.getAutoRetrievingStatement();
        if (command != null) {
            if (command.isBlank()) {
                result = statement.getGeneratedKeys();
            }
            else {
                DBQueryParser parser = new DBQueryParser(sql);
                if (parser.isExecuteUpdateStatement() && parser.hasTable()) {
                    String table = parser.getTable();
                    resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_TABLE;
                    logger.logprb(LogLevel.FINE, cls, method, resource, table, sql);
                    String keys = getGeneratedKeys(statement);
                    if (!keys.isBlank()) {
                        query = String.format(command, table, keys);
                    }
                }
            }
        }
        if (result == null) {
            resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_QUERY;
            logger.logprb(LogLevel.FINE, cls, method, resource, query);
            result = generated.executeQuery(query);
        }
        return result;
    }



    public static String getGeneratedColumnNames(java.sql.ResultSet result, int count)
        throws java.sql.SQLException 
    {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            names.add(result.getMetaData().getColumnName(i));
        }
        return String.join(", ", names);
    }

    public static String getGeneratedKeys(java.sql.Statement statement)
        throws java.sql.SQLException
    {
        String keys = "";
        try(java.sql.ResultSet result = statement.getGeneratedKeys())
        {
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
        return keys;
    }

    public static void printDescriptor(XPropertySet descriptor)
    {
        for (Property property: descriptor.getPropertySetInfo().getProperties()) {
            String name = property.Name;
            try {
                Object value = descriptor.getPropertyValue(name);
                System.out.println("Name: " + name + " - Value: '" + value.toString() + "'");
            }
            catch (UnknownPropertyException | WrappedTargetException e) {
                e.printStackTrace();
            }
        }
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        if (supplier != null) {
            XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            for (int i = 0; i < indexes.getCount(); i++) {
                try {
                    XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
                    if (property != null) {
                        printDescriptor(property);
                    }
                }
                catch (IndexOutOfBoundsException | WrappedTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void printResultSet(java.sql.ResultSet result)
        throws java.sql.SQLException
    {
        java.sql.ResultSetMetaData metadata = result.getMetaData();
        while (result.next()) {
            System.out.println("Row: " + result.getRow() + "\t*********************");
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                System.out.println("Column: " + metadata.getColumnName(i) + " - Value: '" + result.getString(i) + "'");
            }
        }
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.uno.Exception  e)
    {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.lang.IndexOutOfBoundsException e)
    {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.lang.WrappedTargetException e)
    {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               java.sql.SQLException e)
    {
        SQLException exception = new SQLException(msg, context, state, code, Any.VOID);
        setNextSQLException(e, exception, context);
        return exception;
    }

    public static SQLException getSQLException(java.sql.SQLException e)
    {
        SQLException exception = new SQLException(e.getMessage());
        exception.ErrorCode = e.getErrorCode();
        exception.SQLState = e.getSQLState();
        setNextSQLException(e, exception, null);
        return exception;
    }

    public static SQLException getSQLException(java.sql.SQLException e,
                                               XInterface context)
    {
        SQLException exception = new SQLException(e.getMessage());
        exception.Context = context;
        exception.ErrorCode = e.getErrorCode();
        exception.SQLState = e.getSQLState();
        setNextSQLException(e, exception, context);
        return exception;
    }

    private static void setNextSQLException(java.sql.SQLException e,
                                            SQLException next,
                                            XInterface context)
    {
        Iterator<Throwable> it = e.iterator();
        while (next != null && it.hasNext()) {
            next = getNextSQLException(it, next, context);
        }
    }

    private static SQLException getNextSQLException(Iterator<Throwable> it,
                                                    SQLException exception,
                                                    XInterface context)
    {
        SQLException next = null;
        try {
            java.sql.SQLException e = (java.sql.SQLException) it.next();
            next = new SQLException(e.getMessage());
            next.ErrorCode = e.getErrorCode();
            next.SQLState = e.getSQLState();
            if (context != null) {
                next.Context = context;
            }
            exception.NextException = next;
        }
        catch (java.lang.Exception e) { }
        return next;
    }

    // XXX: MessageFormat don't like simple quote!!!
    public static String formatSQLQuery(String query,
                                        Object... arguments)
    {
        // XXX: If we have a simple quote then we have to double simple quote!!!
        if (query.contains("'")) {
            query = query.replace("'", "''");
        }
        return MessageFormat.format(query, arguments);
    }

    public static int getEvenLength(final int length)
    {
        if ((length & 1) != 0) {
            return length - 1;
        }
        return length;
    }

}
