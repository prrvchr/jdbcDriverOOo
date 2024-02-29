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
import java.util.Arrays;
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
import io.github.prrvchr.uno.sdbcx.ColumnContainerBase.ExtraColumnInfo;
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
     * @throws java.sql.SQLException, SQLException
     */

    public static String composeColumnName(DriverProvider provider,
                                           String catalog,
                                           String schema,
                                           String table,
                                           String column,
                                           boolean sensitive,
                                           ComposeRule composeRule)
        throws java.sql.SQLException, SQLException
    {
        String name = composeTableName(provider, catalog, schema, table, sensitive, composeRule);
        return composeColumnName(provider, name, column, sensitive);
    }

    public static String composeColumnName(DriverProvider provider,
                                           String table,
                                           String column,
                                           boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        StringBuilder buffer = new StringBuilder(table);
        buffer.append('.');
        buffer.append(enquoteIdentifier(provider, column, sensitive));
        return buffer.toString();
    }


    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules
     */
    public static String composeTableName(DriverProvider provider,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean sensitive,
                                          ComposeRule composeRule)
        throws java.sql.SQLException,
               SQLException
    {
        StringBuilder buffer = new StringBuilder();
        NameComponentSupport nameComponentSupport = getNameComponentSupport(provider, composeRule);
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        
        
        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && nameComponentSupport.useCatalogs) {
            catalogSeparator = metadata.getCatalogSeparator();
            catalogAtStart = metadata.isCatalogAtStart();
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
                                   TableMain table,
                                   ComposeRule rule)
        throws java.sql.SQLException
    {
        return buildName(provider,
                         table.getCatalogName(),
                         table.getSchemaName(),
                         table.getName(),
                         rule,
                         false);
    }

    public static String buildName(DriverProvider provider,
                                   TableMain table,
                                   ComposeRule rule,
                                   boolean sensitive)
        throws java.sql.SQLException
    {
        return buildName(provider,
                         table.getCatalogName(),
                         table.getSchemaName(),
                         table.getName(),
                         rule,
                         sensitive);
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
        return doComposeTableName(provider,
                                  support,
                                  catalog,
                                  schema,
                                  table,
                                  sensitive);
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
        NameComponents component = getTableNameComponents(provider, table);
        return doComposeTableName(provider,
                                  support,
                                  catalog ? component.getCatalog() : "",
                                  schema ? component.getSchema() : "",
                                  component.getTable(),
                                  sensitive);
    }

    public static String composeTableName(DriverProvider provider,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        NameComponents component = getTableNameComponents(provider, table);
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return doComposeTableName(provider,
                                  support,
                                  support.useCatalogs ? component.getCatalog() : "",
                                  support.useSchemas ? component.getSchema() : "",
                                  component.getTable(),
                                  sensitive);
    }

    public static String composeTableName(DriverProvider provider,
                                          XPropertySet table,
                                          NameComponentSupport support,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        NameComponents component = getTableNameComponents(provider, table);
        return doComposeTableName(provider,
                                  support,
                                  support.useCatalogs ? component.getCatalog() : "",
                                  support.useSchemas ? component.getSchema() : "",
                                  component.getTable(),
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

    public static Object[] getRenameTableArguments(DriverProvider provider,
                                                   NameComponents newname,
                                                   TableMain table,
                                                   String fullname,
                                                   boolean reversed,
                                                   ComposeRule rule,
                                                   boolean sensitive,
                                                   boolean identifier)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted full old table name
        args.add(identifier ? quoteTableName(provider, fullname, rule, sensitive) : fullname);
        // TODO: {1} quoted / unquoted new schema name
        args.add(identifier ? enquoteIdentifier(provider, newname.getSchema(), sensitive) : newname.getSchema());
        // TODO: {2} quoted / unquoted full old table name overwritten with the new schema name
        if (identifier)
            args.add(buildName(provider, table.getCatalogName(), newname.getSchema(), table.getName(), rule, identifier));
        else
            args.add(buildName(provider, table.getCatalogName(), newname.getSchema(), table.getName(), rule, false));
        // TODO: {3} quoted / unquoted new table name
        args.add(identifier ? enquoteIdentifier(provider, newname.getTable(), sensitive) : newname.getTable());
        // TODO: {4} quoted / unquoted full old table name overwritten with the new table name
        if (identifier)
            args.add(buildName(provider, table.getCatalogName(), table.getSchemaName(), newname.getTable(), rule, sensitive));
        else
            args.add(buildName(provider, table.getCatalogName(), table.getSchemaName(), newname.getTable(), rule, false));
        // TODO: {5} quoted / unquoted new catalog name
        args.add(identifier ? enquoteIdentifier(provider, newname.getCatalog(), sensitive) : newname.getCatalog());
        // TODO: {6} quoted / unquoted full old table name overwritten with the new catalog name
        if (identifier)
            args.add(buildName(provider, newname.getCatalog(), table.getSchemaName(), table.getName(), rule, sensitive));
        else
            args.add(buildName(provider, newname.getCatalog(), table.getSchemaName(), table.getName(), rule, false));
        // TODO: {7} quoted / unquoted full new table name
        if (identifier)
            args.add(buildName(provider, newname.getCatalog(), newname.getSchema(), newname.getTable(), rule, sensitive));
        else
            args.add(buildName(provider, newname.getCatalog(), newname.getSchema(), newname.getTable(), rule, false));
        if (reversed) {
            String buffers = args.get(0);
            args.set(0, args.get(4));
            args.set(4, args.get(2));
            args.set(2, buffers);
        }
        return args.toArray(new Object[0]);
    }

    public static Object[] getAlterViewArguments(DriverProvider provider,
                                                 NameComponents component,
                                                 String fullname,
                                                 String command,
                                                 ComposeRule rule,
                                                 boolean sensitive,
                                                 boolean identifier)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted full view name
        args.add(identifier ? quoteTableName(provider, fullname, rule, sensitive) : fullname);
        // TODO: {1} quoted / unquoted catalog view name
        args.add(identifier ? enquoteIdentifier(provider, component.getCatalog(), sensitive) : component.getCatalog());
        // TODO: {2} quoted / unquoted schema view name
        args.add(identifier ? enquoteIdentifier(provider, component.getSchema(), sensitive) : component.getSchema());
        // TODO: {3} quoted / unquoted view name
        args.add(identifier ? enquoteIdentifier(provider, component.getTable(), sensitive) : component.getTable());
        // TODO: {4} raw view command
        args.add(command);
        for (String arg : args) {
            System.out.println("sdbcx.View.getAlterViewArguments() Args: '" + arg + "'");
        }
        return args.toArray(new Object[0]);
    }


    public static Object[] getViewDefinitionArguments(DriverProvider provider,
                                                      NameComponents component,
                                                      String fullname,
                                                      ComposeRule rule,
                                                      boolean sensitive,
                                                      boolean identifier)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted  full view name
        args.add(identifier ? quoteTableName(provider, fullname, rule, sensitive) : fullname);
        // TODO: {1} quoted / unquoted  catalog view name
        args.add(identifier ? provider.getStatement().enquoteIdentifier(component.getCatalog(), sensitive) : component.getCatalog());
        // TODO: {2} quoted / unquoted  schema view name
        args.add(identifier ? provider.getStatement().enquoteIdentifier(component.getSchema(), sensitive) : component.getSchema());
        // TODO: {3} quoted / unquoted  view name
        args.add(identifier ? provider.getStatement().enquoteIdentifier(component.getTable(), sensitive) : component.getTable());
        // TODO: {4} quoted literal 'SELECT '
        args.add("'SELECT '");
        for (String arg : args) {
            System.out.println("sdbcx.ViewContainer.getViewDefinitionArguments() Args: '" + arg + "'");
        }
        return args.toArray(new Object[0]);
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
        NameComponents component = getTableNameComponents(provider, table);
        return composeTableNameForSelect(provider, component.getCatalog(), component.getSchema(), component.getTable(), sensitive);
    }

    public static NameComponents getTableNameComponents(DriverProvider provider,
                                                        XPropertySet table)
    {
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
        return provider.getStatement().enquoteIdentifier(name, sensitive);
    }

    /** quote the given table name (which may contain a catalog and a schema) according to the rules provided by the meta data
     */
    public static String quoteTableName(DriverProvider provider,
                                        String name,
                                        ComposeRule rule,
                                        boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        if (sensitive) {
            NameComponents nameComponents = qualifiedNameComponents(provider, name, rule);
            name = composeTableName(provider, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(), true, rule);
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
    public static NameComponents qualifiedNameComponents(DriverProvider provider,
                                                         String name,
                                                         ComposeRule rule)
        throws java.sql.SQLException, SQLException
    {
        NameComponents component = new NameComponents();
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
            UnoHelper.ensure(-1 != index, "QualifiedNameComponents : no schema separator!", provider.getLogger());
            if (index != -1) {
                component.setSchema(buffer.substring(0, index));
            }
            buffer = buffer.substring(index + 1);
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
        throws java.sql.SQLException, SQLException
    {
        String view = composeTableName(provider, descriptor, ComposeRule.InTableDefinitions, sensitive);
        String command = getDescriptorStringValue(descriptor, PropertyIds.COMMAND);
        return getCreateViewQuery(view, command);
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
                                            NameComponents component,
                                            String command,
                                            ComposeRule rule,
                                            boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String name = composeTableName(provider, component.getCatalog(), component.getSchema(), component.getTable(), sensitive, rule);
        return getCreateViewQuery(name, command);
    }

    public static String getDropTableQuery(String table)
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_DROP_TABLE, table);
    }


    private static String getCreateViewQuery(String view,
                                             String command)
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_CREATE_VIEW, view, command);
    }

    public static String getDropViewQuery(String view)
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_DROP_VIEW, view);
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
     * @throws java.sql.SQLException 
     */
    public static String getCreateUserQuery(DriverProvider provider,
                                            XPropertySet descriptor,
                                            String name,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        String password = getDescriptorStringValue(descriptor, PropertyIds.PASSWORD);
        name = enquoteIdentifier(provider, name, sensitive);
        password = provider.getStatement().enquoteLiteral(password);
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_CREATE_USER, name, password);
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
     * @throws java.sql.SQLException 
     */
    public static String getDropUserQuery(DriverProvider provider,
                                          String name,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_DROP_USER, enquoteIdentifier(provider, name, sensitive));
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
     * @throws java.sql.SQLException 
     */
    public static String getChangeUserPasswordQuery(DriverProvider provider,
                                                    String name,
                                                    String password,
                                                    boolean sensitive)
        throws java.sql.SQLException
    {
        name = enquoteIdentifier(provider, name, sensitive);
        password = provider.getStatement().enquoteLiteral(password);
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_ALTER_USER, name, password);
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
     * @throws java.sql.SQLException 
     */
    public static String getCreateGroupQuery(DriverProvider provider,
                                             XPropertySet descriptor,
                                             String name,
                                             boolean sensitive)
        throws java.sql.SQLException
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_CREATE_ROLE, enquoteIdentifier(provider, name, sensitive));
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
     * @throws java.sql.SQLException 
     */
    public static String getDropGroupQuery(DriverProvider provider,
                                           String name,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_DROP_ROLE, enquoteIdentifier(provider, name, sensitive));
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
     * @throws java.sql.SQLException 
     */
    public static String getGrantRoleQuery(DriverProvider provider,
                                           String group,
                                           String user,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        group = enquoteIdentifier(provider, group, sensitive);
        user = enquoteIdentifier(provider, user, sensitive);
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_GRANT_ROLE, group, user);
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
     * @throws java.sql.SQLException 
     */
    public static String getRevokeRoleQuery(DriverProvider provider,
                                            String group,
                                            String user,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        group = enquoteIdentifier(provider, group, sensitive);
        user = enquoteIdentifier(provider, user, sensitive);
        String query = provider.getRevokeRoleQuery(DBDefaultQuery.STR_QUERY_REVOKE_ROLE);
        return MessageFormat.format(query, group, user);
    }

    public static int getTableOrViewPrivileges(DriverProvider provider,
                                               List<String> grantees,
                                               String catalog,
                                               String schema,
                                               String table)
    throws SQLException
    {
        NameComponents component = new NameComponents(catalog, schema, table);
        return getTableOrViewPrivileges(provider, grantees, component);
    }

    public static int getTableOrViewPrivileges(DriverProvider provider,
                                               List<String> grantees,
                                               String name)
    throws java.sql.SQLException, SQLException
    {
        NameComponents component = qualifiedNameComponents(provider, name, ComposeRule.InDataManipulation);
        return getTableOrViewPrivileges(provider, grantees, component);
    }

    public static int getTableOrViewPrivileges(DriverProvider provider,
                                               List<String> grantees,
                                               NameComponents component)
    throws SQLException
    {
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE ";
        return _getTableOrViewPrivileges(provider, grantees, component, sql);
    }

    public static int getTableOrViewGrantablePrivileges(DriverProvider provider,
                                                        List<String> grantees,
                                                        String name)
    throws java.sql.SQLException, SQLException
    {
        NameComponents component = DBTools.qualifiedNameComponents(provider, name, ComposeRule.InDataManipulation);
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE IS_GRANTABLE = 'YES' AND ";
        return _getTableOrViewPrivileges(provider, grantees, component, sql);
    }

    private static int _getTableOrViewPrivileges(DriverProvider provider,
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
        try (java.sql.PreparedStatement statement = provider.getConnection().prepareStatement(sql)){
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
                privilege |= provider.getPrivilege(result.getString(1));
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, null);
        }
        return privilege;
    }

    public static String getGrantPrivilegesQuery(DriverProvider provider,
                                                 String grantee,
                                                 String name,
                                                 int privilege,
                                                 ComposeRule rule,
                                                 boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String separator = ", ";
        String right = String.join(separator, provider.getPrivileges(privilege));
        String table = quoteTableName(provider, name, rule, sensitive);
        String user = enquoteIdentifier(provider, grantee, sensitive);
        return MessageFormat.format(DBDefaultQuery.STR_QUERY_GRANT_PRIVILEGE, right, table, user);
    }

    public static String revokeTableOrViewPrivileges(DriverProvider provider,
                                                     String grantee,
                                                     String name,
                                                     int privilege,
                                                     ComposeRule rule,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        List<String> values = provider.getPrivileges(privilege);
        String table = quoteTableName(provider, name, rule, sensitive);
        grantee = enquoteIdentifier(provider, grantee, sensitive);
        String query = provider.getRevokeTableOrViewPrivileges(values, table, grantee);
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
    public static Map<String,ExtraColumnInfo> collectColumnInformation(DriverProvider provider,
                                                                       String composedName,
                                                                       String columnName)
        throws java.sql.SQLException
    {
        Map<String, ExtraColumnInfo> columns = new TreeMap<>();
        String sql = String.format("SELECT %s FROM %s WHERE 0 = 1", columnName, composedName);
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
                columnInfo.isCurrency = metadata.isCurrency(i);
                columnInfo.dataType = provider.getDataType(metadata.getColumnType(i));
                columns.put(newColumnName, columnInfo);
            }
        }
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

    public static String buildName(DriverProvider provider,
                                   java.sql.ResultSet result,
                                   ComposeRule rule)
        throws java.sql.SQLException
    {
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

    public static boolean executeDDLQuery(DriverProvider provider,
                                          String query,
                                          ConnectionLog logger,
                                          String cls,
                                          String method,
                                          int resource,
                                          Object... args)
        throws java.sql.SQLException
    {
        Object[] parameters =  new Object[]{};
        Integer[] positions = new Integer[]{};
        return executeDDLQuery(provider, query, parameters, positions, logger, cls, method, resource, args);
    }

    public static boolean executeDDLQuery(DriverProvider provider,
                                          String query,
                                          Object[] parameters,
                                          Integer[] positions,
                                          ConnectionLog logger,
                                          String cls,
                                          String method,
                                          int resource,
                                          Object... arguments)
        throws java.sql.SQLException
    {
        if (query.isBlank()) {
            return false;
        }
        boolean autocommit = false;
        boolean support = provider.supportsTransactions();
        java.sql.Connection jdbc = provider.getConnection();
        try {
            if (support) {
                autocommit = jdbc.getAutoCommit();
                jdbc.setAutoCommit(false);
            }
            try (java.sql.PreparedStatement statement = jdbc.prepareStatement(query)) {
                executeUpdate(statement, parameters, positions, logger,
                              query, cls, method, resource, arguments);
            }
            catch(java.sql.SQLException e) {
                String message = logger.getStringResource(resource + 1, _addToArgs(arguments, query, e.getMessage()));
                logger.logp(LogLevel.SEVERE, cls, method, message);
                throw new java.sql.SQLException(message, e);
            }
            if (support) {
                jdbc.commit();
                jdbc.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e2) {
            if (support) {
                try {
                    jdbc.rollback();
                    jdbc.setAutoCommit(autocommit);
                }
                catch (java.sql.SQLException ex) {
                    // pass
                }
            }
            throw e2;
        }
        return true;

    }

    public static boolean executeDDLQueries(DriverProvider provider,
                                            List<String> queries,
                                            ConnectionLog logger,
                                            String cls,
                                            String method,
                                            int resource,
                                            Object... args)
        throws java.sql.SQLException
    {
        Object[] parameters =  new Object[]{};
        List<Integer[]> positions = new ArrayList<Integer[]>();
        return executeDDLQueries(provider, queries, parameters, positions, logger, cls, method, resource, args);
    }

    public static boolean executeDDLQueries(DriverProvider provider,
                                            List<String> queries,
                                            Object[] parameters,
                                            List<Integer[]> positions,
                                            ConnectionLog logger,
                                            String cls,
                                            String method,
                                            int resource,
                                            Object... arguments)
        throws java.sql.SQLException
    {
        int count = 0;
        int index = 0;
        boolean autocommit = false;
        boolean support = provider.supportsTransactions();
        java.sql.Connection jdbc = provider.getConnection();
        try {
            if (support) {
                autocommit = jdbc.getAutoCommit();
                jdbc.setAutoCommit(false);
            }
            for (String query : queries) {
                if (query.isBlank()) {
                    index ++;
                    continue;
                }
                try (java.sql.PreparedStatement statement = jdbc.prepareStatement(query)) {
                    Integer[] position = (positions.size() > index) ? positions.get(index) : new Integer[]{};
                    executeUpdate(statement, parameters, position, logger,
                                  query, cls, method, resource, arguments);
                    index ++;
                    count ++;
                }
                catch (java.sql.SQLException e) {
                    String message = logger.getStringResource(resource + 1, _addToArgs(arguments, query, e.getMessage()));
                    logger.logp(LogLevel.SEVERE, cls, method, message);
                    throw new java.sql.SQLException(message, e);
                }
            }
            if (support) {
                jdbc.commit();
                jdbc.setAutoCommit(autocommit);
            }
        }
        catch (java.sql.SQLException e1) {
            if (support) {
                try {
                    jdbc.rollback();
                    jdbc.setAutoCommit(autocommit);
                }
                catch (java.sql.SQLException e2) {
                    // pass
                }
            }
            throw e1;
        }
        return count > 0;
    }

    private static void executeUpdate(java.sql.PreparedStatement statement,
                                      Object[] parameters,
                                      Integer[] positions,
                                      ConnectionLog logger,
                                      String query,
                                      String cls,
                                      String method,
                                      int resource,
                                      Object... arguments)
        throws java.sql.SQLException
    {
        int i = 1;
        for (int position : positions) {
            statement.setString(i++, (String) parameters[position]);
        }
        logger.logprb(LogLevel.FINE, cls, method, resource, _addToArgs(arguments, query));
        statement.executeUpdate();
    }

    private static Object[] _addToArgs(Object[] arguments, Object... options)
    {
        List<Object> list = new ArrayList<Object>(Arrays.asList(arguments));
        for (Object option : options) {
            list.add(option);
        }
        return list.toArray(new Object[0]);
    }


    public final static java.sql.ResultSet getGeneratedResult(java.sql.Statement statement,
                                                              java.sql.Statement generated,
                                                              ConnectionLog logger,
                                                              String cls,
                                                              String method,
                                                              String command,
                                                              String sql)
        throws SQLException, java.sql.SQLException
    {
        int resource;
        java.sql.ResultSet result = null;
        String query = DBDefaultQuery.STR_QUERY_EMPTY_RESULTSET;
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

}
