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

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSetMetaData;
import java.sql.RowIdLifetime;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.Property;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.XInterface;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.DateTimeWithTimezone;
import com.sun.star.util.DateWithTimezone;
import com.sun.star.util.Time;
import com.sun.star.util.TimeWithTimezone;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.UnoHelper;


public class DBTools {

    public static final Comparator<String> getComparator(boolean sensitive) {
        return new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                int comp;
                if (sensitive) {
                    comp = x.compareTo(y);
                } else {
                    comp = x.compareToIgnoreCase(y);
                }
                return comp;
            }
        };
    }

    public static class NameComponentSupport {
        boolean mUseCatalogs;
        boolean mUseSchemas;
        boolean mCatalogAtStart;
        String mCatalogSeparator;
        String mIdentifierQuote;

        NameComponentSupport(boolean usecatalog,
                             boolean useschema,
                             boolean atstart,
                             String separator,
                             String quote) {
            mUseCatalogs = usecatalog;
            mUseSchemas = useschema;
            mCatalogAtStart = atstart;
            mCatalogSeparator = separator;
            mIdentifierQuote = quote;
        }
    }

    public static class NamedComponents {
        private String mCatalog;
        private String mSchema;
        private String mTable;

        public NamedComponents(String catalog, String schema, String table) {
            mCatalog = catalog;
            mSchema = schema;
            mTable = table;
        }

        public NamedComponents() {
        }

        // Java DataBaseMetadata specific getter
        public String getCatalog() {
            String catalog = null;
            if (mCatalog != null && !mCatalog.isBlank()) {
                catalog = mCatalog;
            }
            return catalog;
        }

        // UNO getter (String can not be null)
        public String getCatalogName() {
            String catalog = "";
            if (mCatalog != null) {
                catalog = mCatalog;
            }
            return catalog;
        }

        public void setCatalog(String catalog) {
            mCatalog = catalog;
        }

        // Java DataBaseMetadata specific getter
        public String getSchema() {
            String schema = null;
            if (mSchema != null && !mSchema.isBlank()) {
                schema = mSchema;
            }
            return schema;
        }

        // UNO getter (String can not be null)
        public String getSchemaName() {
            String schema = "";
            if (mSchema != null) {
                schema = mSchema;
            }
            return schema;
        }

        public void setSchema(String schema) {
            mSchema = schema;
        }

        // Java DataBaseMetadata specific getter
        public String getTable() {
            return mTable;
        }

        public String getTableName() {
            return mTable;
        }

        public void setTable(String table) {
            mTable = table;
        }
    }

    public static NamedComponents getNamedComponents(ResultSetMetaData metadata, int index)
        throws java.sql.SQLException {
        return new NamedComponents(metadata.getCatalogName(index), metadata.getSchemaName(index),
                                   metadata.getTableName(index));
    }

    public static NameComponentSupport getNameComponentSupport(Provider provider,
                                                               ComposeRule rule) {
        NameComponentSupport support;
        switch (rule) {
            case InTableDefinitions:
                support = new NameComponentSupport(provider.supportsCatalogsInTableDefinitions(),
                                                   provider.supportsSchemasInTableDefinitions(),
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            case InIndexDefinitions:
                support = new NameComponentSupport(provider.supportsCatalogsInIndexDefinitions(),
                                                   provider.supportsSchemasInIndexDefinitions(),
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            case InDataManipulation:
                support = new NameComponentSupport(provider.supportsCatalogsInDataManipulation(),
                                                   provider.supportsSchemasInDataManipulation(),
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            case InProcedureCalls:
                support = new NameComponentSupport(provider.supportsCatalogsInProcedureCalls(),
                                                   provider.supportsSchemasInProcedureCalls(),
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            case InPrivilegeDefinitions:
                support = new NameComponentSupport(provider.supportsCatalogsInPrivilegeDefinitions(),
                                                   provider.supportsSchemasInPrivilegeDefinitions(),
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            case Complete:
                support = new NameComponentSupport(true,
                                                   true,
                                                   provider.isCatalogAtStart(),
                                                   provider.getCatalogSeparator(),
                                                   provider.getIdentifierQuoteString());
                break;
            default:
                throw new UnsupportedOperationException("Invalid/unknown enum value");
        }
        return support;
    }

    /** compose a complete column name from it's up to two parts, regarding to the database meta data composing rules.
     *
     * @param provider
     *    The driver provider.
     * @param table
     *    The table name.
     * @param column
     *    The column name.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed column name (ie: with the catalog, schema, table and column name)
     * 
     * @throws java.sql.SQLException, SQLException
     */

    public static String composeColumnName(Provider provider,
                                           String table,
                                           String column,
                                           boolean sensitive)
        throws java.sql.SQLException {
        StringBuilder buffer = new StringBuilder(table);
        buffer.append('.');
        buffer.append(provider.enquoteIdentifier(column, sensitive));
        return buffer.toString();
    }


    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     *
     * @param provider
     *    The driver provider.
     * @param catalog
     *    The table catalog name.
     * @param schema
     *    The table schema name.
     * @param table
     *    The table name.
     * @param sensitive
     *    Is the name case sensitive.
     * @param rule
     *    The naming rule.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     * @throws java.sql.SQLException 
     */
    public static String composeTableName(Provider provider,
                                          String catalog,
                                          String schema,
                                          String table,
                                          boolean sensitive,
                                          ComposeRule rule)
        throws java.sql.SQLException {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return composeTableName(provider, catalog, schema, table, support, sensitive);
    }


    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     *
     * @param provider
     *    The driver provider.
     * @param component
     *    The named component.
     * @param sensitive
     *    Is the name case sensitive.
     * @param rule
     *    The naming rule.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     * @throws java.sql.SQLException 
     */
    public static String composeTableName(Provider provider,
                                          NamedComponents component,
                                          ComposeRule rule,
                                          boolean sensitive)
        throws java.sql.SQLException {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return composeTableName(provider, component, support, sensitive);
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     * @param provider
     *    The driver provider.
     * @param component
     *    The component.
     * @param support
     *    The component.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     * @throws java.sql.SQLException 
     */
    public static String composeTableName(Provider provider,
                                          NamedComponents component,
                                          NameComponentSupport support,
                                          boolean sensitive)
        throws java.sql.SQLException {
        return composeTableName(provider, component.getCatalogName(), component.getSchemaName(),
                                component.getTable(), support, sensitive);
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     * @param provider
     *    The driver provider.
     * @param catalog
     *    The table catalog name.
     * @param schema
     *    The table schema name.
     * @param table
     *    The table name.
     * @param support
     *    The component.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     * @throws java.sql.SQLException 
     */
    public static String composeTableName(Provider provider,
                                          String catalog,
                                          String schema,
                                          String table,
                                          NameComponentSupport support,
                                          boolean sensitive)
        throws java.sql.SQLException {
        StringBuilder buffer = new StringBuilder();
        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && support.mUseCatalogs) {
            catalogSeparator = support.mCatalogSeparator;
            catalogAtStart = support.mCatalogAtStart;
            if (isCatalogValid(catalogAtStart, catalogSeparator)) {
                buffer.append(provider.enquoteIdentifier(catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }
        if (!schema.isEmpty() && support.mUseSchemas) {
            buffer.append(provider.enquoteIdentifier(schema, sensitive));
            buffer.append('.');
        }
        buffer.append(provider.enquoteIdentifier(table, sensitive));
        if (!catalog.isEmpty() && support.mUseCatalogs && isCatalogValid(!catalogAtStart, catalogSeparator)) {
            buffer.append(catalogSeparator);
            buffer.append(provider.enquoteIdentifier(catalog, sensitive));
        }
        System.out.println("DataBaseTools.composeTableName(): Name: " + buffer.toString());
        return buffer.toString();
    }

    public static String buildName(Provider provider,
                                   NamedComponents component,
                                   ComposeRule rule) {
        return buildName(provider, component, rule, false);
    }

    public static String buildName(Provider provider,
                                   NamedComponents component,
                                   ComposeRule rule,
                                   boolean sensitive) {
        return buildName(provider, component.getCatalogName(),
                         component.getSchemaName(), component.getTableName(), rule, sensitive);
    }

    public static String buildName(Provider provider,
                                   String catalog,
                                   String schema,
                                   String table,
                                   ComposeRule rule) {
        return buildName(provider, catalog, schema, table, rule, false);
    }

    public static String buildName(Provider provider,
                                   String catalog,
                                   String schema,
                                   String table,
                                   ComposeRule rule,
                                   boolean sensitive) {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return doComposeTableName(provider, support, catalog, schema, table, sensitive);
    }

    public static String buildName(Provider provider,
                                   NamedComponents component,
                                   NameComponentSupport support,
                                   boolean sensitive) {
        return doComposeTableName(provider, support, component, sensitive);
    }

    public static String composeTableName(Provider provider,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean catalog,
                                          boolean schema,
                                          boolean sensitive) {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        NamedComponents component = getTableNamedComponents(provider, table);
        String catalogName;
        if (catalog) {
            catalogName = component.getCatalogName();
        } else {
            catalogName = "";
        }
        String schemaName;
        if (schema) {
            schemaName = component.getSchemaName();
        } else {
            schemaName = "";
        }
        return doComposeTableName(provider, support, catalogName, schemaName,
                                  component.getTableName(), sensitive);
    }

    public static String composeTableName(Provider provider,
                                          XPropertySet table,
                                          ComposeRule rule,
                                          boolean sensitive) {
        NamedComponents component = getTableNamedComponents(provider, table);
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        String catalogName;
        if (support.mUseCatalogs) {
            catalogName = component.getCatalogName();
        } else {
            catalogName = "";
        }
        String schemaName;
        if (support.mUseSchemas) {
            schemaName = component.getSchemaName();
        } else {
            schemaName = "";
        }
        return doComposeTableName(provider, support, catalogName, schemaName,
                                  component.getTableName(), sensitive);
    }

    public static String composeTableName(Provider provider,
                                          XPropertySet table,
                                          NameComponentSupport support,
                                          boolean sensitive) {
        NamedComponents component = getTableNamedComponents(provider, table);
        String catalogName;
        if (support.mUseCatalogs) {
            catalogName = component.getCatalogName();
        } else {
            catalogName = "";
        }
        String schemaName;
        if (support.mUseSchemas) {
            schemaName = component.getSchemaName();
        } else {
            schemaName = "";
        }
        return doComposeTableName(provider, support, catalogName, schemaName,
                                  component.getTableName(), sensitive);
    }

    public static String doComposeTableName(Provider provider,
                                            NameComponentSupport support,
                                            NamedComponents component,
                                            boolean sensitive) {
        return doComposeTableName(provider, support, component.getCatalog(),
                                  component.getSchema(), component.getTable(), sensitive);
    }

    public static String doComposeTableName(Provider provider,
                                            NameComponentSupport support,
                                            String catalog,
                                            String schema,
                                            String table,
                                            boolean sensitive) {
        StringBuilder buffer = new StringBuilder();

        String catalogSeparator = "";
        boolean catalogAtStart = true;
        if (!catalog.isEmpty() && support.mUseCatalogs) {
            catalogSeparator = support.mCatalogSeparator;
            catalogAtStart = support.mCatalogAtStart;
            
            if (isCatalogValid(catalogAtStart, catalogSeparator)) {
                buffer.append(provider.enquoteIdentifier(catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }

        if (!schema.isEmpty() && support.mUseSchemas) {
            buffer.append(provider.enquoteIdentifier(schema, sensitive));
            buffer.append(".");
        }

        buffer.append(provider.enquoteIdentifier(table, sensitive));

        if (!catalog.isEmpty() && isCatalogValid(!catalogAtStart, catalogSeparator) && support.mUseCatalogs) {
            buffer.append(catalogSeparator);
            buffer.append(provider.enquoteIdentifier(catalog, sensitive));
        }
        return buffer.toString();
    }

    /** composes a table name for usage in a SELECT statement.
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     *
     * @param provider
     *    The driver provider.
     * @param catalog
     *    The table catalog name.
     * @param schema
     *    The table schema name.
     * @param table
     *    The table name.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(Provider provider,
                                                   String catalog,
                                                   String schema,
                                                   String table,
                                                   boolean sensitive)
        throws java.sql.SQLException {
        boolean usecatalog = UnoHelper.getDefaultPropertyValue(provider.getInfos(), "UseCatalogInSelect", true);
        boolean useschema = UnoHelper.getDefaultPropertyValue(provider.getInfos(), "UseSchemaInSelect", true);
        String catalogName;
        if (usecatalog) {
            catalogName = catalog;
        } else {
            catalogName = "";
        }
        String schemaName;
        if (useschema) {
            schemaName = schema;
        } else {
            schemaName = "";
        }
        return buildName(provider, catalogName, schemaName, table, ComposeRule.InDataManipulation, sensitive);
    }

    /** composes a table name for usage in a SELECT statement.
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * 
     * @param provider
     *    The driver provider.
     * @param table
     *    The table as property set.
     * @param sensitive
     *    Is the name case sensitive.
     * @return the composed table name (ie: with the catalog, schema and table name)
     * @throws java.sql.SQLException 
     */
    public static String composeTableNameForSelect(Provider provider,
                                                   XPropertySet table,
                                                   boolean sensitive)
        throws java.sql.SQLException {
        NamedComponents component = getTableNamedComponents(provider, table);
        return composeTableNameForSelect(provider, component.getCatalogName(), component.getSchemaName(),
                                         component.getTableName(), sensitive);
    }

    public static NamedComponents getTableNamedComponents(Provider provider,
                                                          XPropertySet table) {
        NamedComponents component = new NamedComponents();
        if (hasDescriptorProperty(table, PropertyIds.NAME)) {
            if (hasDescriptorProperty(table, PropertyIds.CATALOGNAME)) {
                component.setCatalog(getDescriptorStringValue(table, PropertyIds.CATALOGNAME));
            }
            if (hasDescriptorProperty(table, PropertyIds.SCHEMANAME)) {
                component.setSchema(getDescriptorStringValue(table, PropertyIds.SCHEMANAME));
            }
            component.setTable(getDescriptorStringValue(table, PropertyIds.NAME));
        } else {
            UnoHelper.ensure(false, "this is not a table object", provider.getLogger());
        }
        return component;
    }

    /** quote the given table name (which may contain a catalog and a schema) according
     *      to the rules provided by the meta data.
     *
     * @param provider
     *    The driver provider.
     * @param name
     *    The full unquoted table name.
     * @param sensitive
     *    Is the name case sensitive.
     * @param rule
     *    The naming rule.
     * @return the full quoted table name (ie: with the catalog, schema and table name)
     */
    public static String quoteTableName(Provider provider,
                                        String name,
                                        ComposeRule rule,
                                        boolean sensitive)
        throws java.sql.SQLException {
        if (sensitive) {
            NamedComponents nameComponents = qualifiedNameComponents(provider, name, rule);
            name = composeTableName(provider, nameComponents.getCatalogName(),
                                    nameComponents.getSchemaName(), nameComponents.getTableName(), true, rule);
        }
        return name;
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param provider
     *    The driver provider.
     * @param name
     *    The full unquoted table name.
     * @param rule
     *    The naming rule.
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponents qualifiedNameComponents(Provider provider,
                                                          String name,
                                                          ComposeRule rule)
        throws java.sql.SQLException {
        return qualifiedNameComponents(provider, name, rule, false);
    }


    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param provider
     *    The driver provider.
     * @param name
     *    The full unquoted table name.
     * @param rule
     *    The naming rule.
     * @param unquote
     *    The quoted rule.
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponents qualifiedNameComponents(Provider provider,
                                                          String name,
                                                          ComposeRule rule,
                                                          boolean unquote)
        throws java.sql.SQLException {
        NameComponentSupport support = getNameComponentSupport(provider, rule);
        return qualifiedNameComponents(name, support, unquote);
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param name
     *    The table name.
     * @param support
     *    The component.
     * @param unquote
     *    need to unquote the name before.
     *
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponents qualifiedNameComponents(String name,
                                                          NameComponentSupport support,
                                                          boolean unquote)
        throws java.sql.SQLException {
        NamedComponents component = new NamedComponents();
        String buffer;
        if (unquote) {
            buffer = unQuoteTableName(support, name);
        } else {
            buffer = name;
        }
        // do we have catalogs ?
        if (support.mUseCatalogs) {
            if (support.mCatalogAtStart) {
                // search for the catalog name at the beginning
                int index = buffer.indexOf(support.mCatalogSeparator);
                if (-1 != index) {
                    component.setCatalog(buffer.substring(0, index));
                    buffer = buffer.substring(index + 1);
                }
            } else {
                // catalog name at end
                int index = buffer.lastIndexOf(support.mCatalogSeparator);
                if (-1 != index) {
                    component.setCatalog(buffer.substring(index + 1));
                    buffer = buffer.substring(0, index);
                }
            }
        }
        if (support.mUseSchemas) {
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

    /** unquote the given table name (which may contain a catalog and a schema).
     *
     * @param support
     *    The component.
     * @param name
     *    The table name.
     * @return the unquoted full table name.
     */
    public static String unQuoteTableName(NameComponentSupport support,
                                          String name) {
        return name.replace(support.mIdentifierQuote, "");
    }

    /** creates a SQL CREATE VIEW statement.
     *
     * @param provider
     *    The driver provider.
     * @param descriptor
     *    The descriptor of the new view.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return
     *   The CREATE VIEW statement.
     */
    public static String getCreateViewCommand(Provider provider,
                                              XPropertySet descriptor,
                                              boolean sensitive)
        throws java.sql.SQLException {
        String view = composeTableName(provider, descriptor, ComposeRule.InTableDefinitions, sensitive);
        String command = getDescriptorStringValue(descriptor, PropertyIds.COMMAND);
        return provider.getConfigDDL().getCreateViewCommand(ParameterDDL.getCreateView(view, command));
    }

    /** creates a SQL CREATE VIEW statement.
     *
     * @param provider
     *    The driver provider.
     * @param component
     *    The component name.
     * @param command
     *    The SQL command to create view.
     * @param rule
     *    The naming rule.
     * @param sensitive
     *    Is the name case sensitive.
     * @return The CREATE VIEW statement.
     */
    public static String getCreateViewCommand(Provider provider,
                                              NamedComponents component,
                                              String command,
                                              ComposeRule rule,
                                              boolean sensitive)
        throws java.sql.SQLException {
        String view = composeTableName(provider, component, rule, sensitive);
        return provider.getConfigDDL().getCreateViewCommand(ParameterDDL.getCreateView(view, command));
    }

    public static void cloneDescriptorColumns(XPropertySet source,
                                              XPropertySet destination)
        throws java.sql.SQLException {
        XColumnsSupplier sourceColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, source);
        XIndexAccess sourceColumns = UnoRuntime.queryInterface(XIndexAccess.class, sourceColumnsSupplier.getColumns());
        
        XColumnsSupplier destinationColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, destination);
        XAppend destinationAppend = UnoRuntime.queryInterface(XAppend.class, destinationColumnsSupplier.getColumns());
        
        int count = sourceColumns.getCount();
        for (int i = 0; i < count; i++) {
            try {
                XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class,
                                                                          sourceColumns.getByIndex(i));
                destinationAppend.appendByDescriptor(columnProperties);
            } catch (WrappedTargetException | IndexOutOfBoundsException |
                     IllegalArgumentException | ElementExistException | SQLException exception) {
                throw new java.sql.SQLException("Error", StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
            }
        }
    }

    public static boolean updateObject(java.sql.ResultSet resultset,
                                       int index,
                                       Object any)
        throws SQLException {
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
                    success = updateSequence(resultset, index, any);
                    break;
                case TypeClass.STRUCT_value:
                    success = updateStruct(resultset, index, any);
                    break;
                case TypeClass.INTERFACE_value:
                    success = updateInterface(resultset, index, any);
                    break;
                default:
                    success = false;
            }
            return success;
        } catch (IllegalArgumentException | java.sql.SQLException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private static boolean updateSequence(java.sql.ResultSet resultset,
                                   int index,
                                   Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (AnyConverter.isArray(any)) {
            Object array = AnyConverter.toArray(any);
            if (array instanceof byte[]) {
                resultset.updateBytes(index, (byte[]) array);
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
    }

    private static boolean updateStruct(java.sql.ResultSet resultset,
                                        int index,
                                        Object any)
        throws java.sql.SQLException {
        boolean success = true;
        Object object = AnyConverter.toObject(Object.class, any);
        if (object instanceof Date) {
            resultset.updateObject(index, UnoHelper.getJavaLocalDate((Date) object));
        } else if (object instanceof Time) {
            resultset.updateObject(index, UnoHelper.getJavaLocalTime((Time) object));
        } else if (object instanceof DateTime) {
            resultset.updateObject(index, UnoHelper.getJavaLocalDateTime((DateTime) object));
        } else {
            success = false;
        }
        return success;
    }

    private static boolean updateInterface(java.sql.ResultSet resultset,
                                           int index,
                                           Object any)
        throws java.sql.SQLException, IOException {
        boolean success = true;
        XInputStream stream = UnoRuntime.queryInterface(XInputStream.class,
                                                        AnyConverter.toObject(Object.class, any));
        if (stream != null) {
            InputStream input = new XInputStreamToInputStreamAdapter(stream);
            resultset.updateBinaryStream(index, input, input.available());
        } else {
            success = false;
        }
        return success;
    }

    public static boolean setObject(java.sql.PreparedStatement statement,
                                    int index,
                                    Object any)
        throws SQLException {
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
                    success = setSequence(statement, index, any);
                    break;
                case TypeClass.STRUCT_value:
                    success = setStruct(statement, index, any);
                    break;
                case TypeClass.INTERFACE_value:
                    success = setInterface(statement, index, any);
                    break;
                default:
                    success = false;
            }
            return success;
        } catch (java.sql.SQLException | IllegalArgumentException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private static boolean setSequence(java.sql.PreparedStatement statement,
                                       int index,
                                       Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (AnyConverter.isArray(any)) {
            Object array = AnyConverter.toArray(any);
            if (array instanceof byte[]) {
                statement.setBytes(index, (byte[])array);
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
    }

    private static boolean setStruct(java.sql.PreparedStatement statement,
                                     int index,
                                     Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (any instanceof Date) {
            statement.setObject(index, UnoHelper.getJavaLocalDate((Date) any));
        } else if (any instanceof Time) {
            statement.setObject(index, UnoHelper.getJavaLocalTime((Time) any));
        } else if (any instanceof DateTime) {
            statement.setObject(index, UnoHelper.getJavaLocalDateTime((DateTime) any));
        } else if (any instanceof DateWithTimezone) {
            DateWithTimezone date = (DateWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaLocalDate(date.DateInTZ));
        } else if (any instanceof TimeWithTimezone) {
            TimeWithTimezone time = (TimeWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaOffsetTime(time));
        } else if (any instanceof DateTimeWithTimezone) {
            DateTimeWithTimezone datetime = (DateTimeWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaOffsetDateTime(datetime));
        } else {
            success = false;
        }
        return success;
    }

    private static boolean setInterface(java.sql.PreparedStatement statement,
                                        int index,
                                        Object any)
        throws java.sql.SQLException, IOException {
        boolean success = true;
        XInputStream stream = UnoRuntime.queryInterface(XInputStream.class,
                                                        AnyConverter.toObject(Object.class, any));
        if (stream != null) {
            InputStream input = new XInputStreamToInputStreamAdapter(stream);
            statement.setBinaryStream(index, input, input.available());
        } else {
            success = false;
        }
        return success;
    }

    public static Object getObject(Object object) {
        Object value = Any.VOID;
        if (object != null) {
            if (object instanceof String) {
                value = (String) object;
            } else if (object instanceof Boolean) {
                value = (Boolean) object;
            } else if (object instanceof Integer) {
                value = (Integer) object;
            } else {
                value = getTimedObject(object);
            }
        }
        return value;
    }

    private static Object getTimedObject(Object object) {
        Object value = Any.VOID;
        if (object instanceof java.time.OffsetTime) {
            value  = UnoHelper.getTimeWithTimezone((java.time.OffsetTime) object);
        } else if (object instanceof java.time.OffsetDateTime) {
            value = UnoHelper.getDateTimeWithTimezone((java.time.OffsetDateTime) object);
        } else if (object instanceof java.time.LocalDate) {
            value = UnoHelper.getUnoDate((java.time.LocalDate) object);
        } else if (object instanceof java.time.LocalTime) {
            value = UnoHelper.getUnoTime((java.time.LocalTime) object);
        } else if (object instanceof java.time.LocalDateTime) {
            value = UnoHelper.getDateTime((java.time.LocalDateTime) object);
        } else if (object instanceof java.sql.Date) {
            value = getDate(object);
        } else if (object instanceof java.sql.Time) {
            value = getTime(object);
        } else if (object instanceof java.sql.Timestamp) {
            value = getTimestamp(object);
        }
        return value;
    }

    private static Date getDate(Object object) {
        java.sql.Date date = (java.sql.Date) object;
        return UnoHelper.getUnoDate(date.toLocalDate());
    }

    private static Time getTime(Object object) {
        java.sql.Time time = (java.sql.Time) object;
        return UnoHelper.getUnoTime(time.toLocalTime());
    }

    private static DateTime getTimestamp(Object object) {
        java.sql.Timestamp timestamp = (java.sql.Timestamp) object;
        return UnoHelper.getUnoDateTime(timestamp.toLocalDateTime());
    }

    public static String buildName(Provider provider,
                                   java.sql.ResultSet result,
                                   ComposeRule rule)
        throws java.sql.SQLException {
        String catalog = "";
        String schema = "";
        String table = "";
        final int TABLE_CAT = 1;
        final int TABLE_SCHEM = 2;
        final int TABLE_NAME = 3;
        catalog = result.getString(TABLE_CAT);
        if (result.wasNull()) {
            catalog = "";
        }
        schema = result.getString(TABLE_SCHEM);
        if (result.wasNull()) {
            schema = "";
        }
        table = result.getString(TABLE_NAME);
        if (result.wasNull()) {
            table = "";
        }
        return buildName(provider, catalog, schema, table, rule, false);
    }

    public static boolean useBookmarks(Provider provider) {
        RowIdLifetime lifetime = RowIdLifetime.ROWID_UNSUPPORTED;
        try {
            lifetime = provider.getConnection().getMetaData().getRowIdLifetime();
        } catch (java.sql.SQLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        return lifetime != RowIdLifetime.ROWID_UNSUPPORTED;
    }

    public static boolean supportsService(XPropertySet descriptor,
                                          String service) {
        XServiceInfo info = UnoRuntime.queryInterface(XServiceInfo.class, descriptor);
        return info.supportsService(service);
    }

    public static boolean hasDescriptorProperty(XPropertySet descriptor,
                                                PropertyIds pid) {
        return descriptor.getPropertySetInfo().hasPropertyByName(pid.getName());
    }

    public static String getDescriptorStringValue(XPropertySet descriptor,
                                                  PropertyIds pid) {
        String value;
        try {
            value = getDescriptorStrValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = "";
        }
        return value;
    }

    public static String getDescriptorStrValue(XPropertySet descriptor,
                                               PropertyIds pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toString(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean getDescriptorBooleanValue(XPropertySet descriptor,
                                                    PropertyIds pid) {
        boolean value;
        try {
            value = getDescriptorBoolValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = false;
        }
        return value;
    }

    public static boolean getDescriptorBoolValue(XPropertySet descriptor,
                                                 PropertyIds pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toBoolean(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet descriptor,
                                                PropertyIds pid) {
        int value;
        try {
            value = getDescriptorIntValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = 0;
        }
        return value;
    }

    public static int getDescriptorIntValue(XPropertySet descriptor,
                                            PropertyIds pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toInt(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean executeSQLQuery(Provider provider,
                                          String query)
        throws java.sql.SQLException {
        Object[] parameters =  new Object[]{};
        Integer[] positions = new Integer[]{};
        return executeSQLQuery(provider, query, parameters, positions);
    }

    public static boolean executeSQLQuery(Provider provider,
                                          String query,
                                          Object[] parameters,
                                          Integer[] positions)
        throws java.sql.SQLException {
        boolean result = false;
        if (!query.isBlank()) {
            boolean auto = false;
            boolean support = provider.supportsTransactions();
    
            java.sql.Connection connection = provider.getConnection();
            try {
                if (support) {
                    auto = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                }
                provider.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_EXECUTE_QUERY, query);
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                    executeSQL(statement, parameters, positions);
                }
                if (support) {
                    connection.commit();
                    connection.setAutoCommit(auto);
                }
            } catch (java.sql.SQLException e) {
                if (support) {
                    try {
                        connection.rollback();
                        connection.setAutoCommit(auto);
                    } catch (java.sql.SQLException ex) {
                        e.setNextException(ex);
                    }
                }
                throw e;
            }
            result = true;
        }
        return result;

    }

    public static boolean executeSQLQueries(Provider provider,
                                            List<String> queries)
        throws java.sql.SQLException {
        Object[] parameters =  new Object[]{};
        List<Integer[]> positions = new ArrayList<Integer[]>();
        return executeSQLQueries(provider, queries, parameters, positions);
    }

    public static boolean executeSQLQueries(Provider provider,
                                            List<String> queries,
                                            Object[] parameters,
                                            List<Integer[]> positions)
        throws java.sql.SQLException {
        int count = 0;
        int index = 0;
        boolean auto = false;
        boolean support = provider.supportsTransactions();
        java.sql.Connection connection = provider.getConnection();
        try {
            if (support) {
                auto = connection.getAutoCommit();
                connection.setAutoCommit(false);
            }
            for (String query : queries) {
                if (query.isBlank()) {
                    index ++;
                    continue;
                }
                provider.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_EXECUTE_QUERY, query);
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                    Integer[] position;
                    if (positions.size() > index) {
                        position = positions.get(index);
                    } else {
                        position = new Integer[]{};
                    }
                    executeSQL(statement, parameters, position);
                    index ++;
                    count ++;
                }
            }
            if (support) {
                connection.commit();
                connection.setAutoCommit(auto);
            }
        } catch (java.sql.SQLException e) {
            if (support) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(auto);
                } catch (java.sql.SQLException ex) {
                    e.setNextException(ex);
                }
            }
            throw e;
        }
        return count > 0;
    }

    private static void executeSQL(java.sql.PreparedStatement statement,
                                   Object[] parameters,
                                   Integer[] positions)
        throws java.sql.SQLException {
        setStatementParameters(statement, parameters, positions);
        statement.executeUpdate();
    }

    public static void setStatementParameters(java.sql.PreparedStatement statement,
                                              Object[] parameters,
                                              Integer[] positions)
        throws java.sql.SQLException {
        int i = 1;
        for (int position : positions) {
            statement.setString(i++, (String) parameters[position]);
        }
    }

    public static void printDescriptor(XPropertySet descriptor) {
        for (Property property: descriptor.getPropertySetInfo().getProperties()) {
            String name = property.Name;
            try {
                Object value = descriptor.getPropertyValue(name);
                System.out.println("Name: " + name + " - Value: '" + value.toString() + "'");
            } catch (UnknownPropertyException | WrappedTargetException e) {
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
                } catch (IndexOutOfBoundsException | WrappedTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void printResultSet(java.sql.ResultSet result) throws java.sql.SQLException {
        printResultSet(result, null);
    }

    public static void printResultSet(java.sql.ResultSet result, String[] colNames)
        throws java.sql.SQLException {
        int row = 0;
        ResultSetMetaData metadata = result.getMetaData();
        while (result.next()) {
            row ++;
            System.out.println("Row: " + row + "\t*********************");
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                if (colNames == null || Arrays.asList(colNames).contains( metadata.getColumnName(i))) {
                    System.out.println("Column: " + metadata.getColumnName(i) +
                                       " - Value: '" + result.getString(i) + "'");
                }
            }
        }
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state) {
        return new SQLException(msg, context, state, 0, null);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.uno.Exception  e) {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.lang.IndexOutOfBoundsException e) {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.lang.IllegalArgumentException e) {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               com.sun.star.lang.WrappedTargetException e) {
        return new SQLException(msg, context, state, code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               String state,
                                               int code,
                                               java.sql.SQLException e) {
        SQLException exception = new SQLException(msg, context, state, code, Any.VOID);
        setNextSQLException(e, exception, context);
        return exception;
    }

    public static SQLException getSQLException(java.sql.SQLException e) {
        SQLException exception = new SQLException(e.getMessage());
        exception.ErrorCode = e.getErrorCode();
        exception.SQLState = e.getSQLState();
        setNextSQLException(e, exception, null);
        return exception;
    }

    public static SQLException getSQLException(java.sql.SQLException e,
                                               XInterface context) {
        SQLException exception = new SQLException(e.getMessage());
        exception.Context = context;
        exception.ErrorCode = e.getErrorCode();
        exception.SQLState = e.getSQLState();
        setNextSQLException(e, exception, context);
        return exception;
    }

    private static void setNextSQLException(java.sql.SQLException e,
                                            SQLException next,
                                            XInterface context) {
        Iterator<Throwable> it = e.iterator();
        while (next != null && it.hasNext()) {
            next = getNextSQLException(it, next, context);
        }
    }

    private static SQLException getNextSQLException(Iterator<Throwable> it,
                                                    SQLException exception,
                                                    XInterface context) {
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
        } catch (java.lang.Exception e) { }
        return next;
    }

    // XXX: MessageFormat don't like simple quote!!!
    public static String formatSQLQuery(String query,
                                        Object... arguments) {
        // XXX: If we have a simple quote then we have to double simple quote!!!
        if (query.contains("'")) {
            query = query.replace("'", "''");
        }
        return MessageFormat.format(query, arguments);
    }

    public static int getEvenLength(final int length) {
        int len = length;
        if ((length & 1) != 0) {
            len = length - 1;
        }
        return len;
    }

    private static boolean isCatalogValid(boolean catalogAtStart, String catalogSeparator) {
        return catalogAtStart && !catalogSeparator.isEmpty();
    }

}
