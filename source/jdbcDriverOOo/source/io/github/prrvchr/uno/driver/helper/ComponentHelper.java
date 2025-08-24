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
package io.github.prrvchr.uno.driver.helper;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.sun.star.beans.XPropertySet;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.PropertyIds;


public class ComponentHelper {

    public static class NamedComponentSupport {

        private boolean mCatalogsInTableDefinitions;
        private boolean mSchemasInTableDefinitions;
        private boolean mCatalogsInViewDefinitions;
        private boolean mSchemasInViewDefinitions;
        private boolean mCatalogsInSelectDefinitions;
        private boolean mSchemasInSelectDefinitions;
        private boolean mCatalogsInIndexDefinitions;
        private boolean mSchemasInIndexDefinitions;
        private boolean mCatalogsInDataManipulation;
        private boolean mSchemasInDataManipulation;
        private boolean mCatalogsInProcedureCalls;
        private boolean mSchemasInProcedureCalls;
        private boolean mCatalogsInPrivilegeDefinitions;
        private boolean mSchemasInPrivilegeDefinitions;
        private boolean mCatalogAtStart;
        private boolean mIsCaseSensitive;

        private String mCatalogSeparator;
        private String mIdentifierQuoteString;

        public NamedComponentSupport(final java.sql.DatabaseMetaData metadata,
                                     final ConfigSQL config)
            throws SQLException {
            this(metadata, config.useCatalogsInSelectDefinitions(),
                           config.useSchemasInSelectDefinitions(),
                           config.useCatalogsInViewDefinitions(),
                           config.useSchemasInViewDefinitions());
        }

        private NamedComponentSupport(final java.sql.DatabaseMetaData metadata,
                                      final boolean useCatalogInSelect,
                                      final boolean useSchemaInSelect,
                                      final boolean useCatalogInView,
                                      final boolean useSchemaInView)
            throws SQLException {

            mCatalogsInTableDefinitions = metadata.supportsCatalogsInTableDefinitions();
            mSchemasInTableDefinitions = metadata.supportsSchemasInTableDefinitions();
            mCatalogsInViewDefinitions = useCatalogInView && metadata.supportsCatalogsInTableDefinitions();
            mSchemasInViewDefinitions = useSchemaInView && metadata.supportsSchemasInTableDefinitions();
            mCatalogsInSelectDefinitions = useCatalogInSelect && metadata.supportsCatalogsInTableDefinitions();
            mSchemasInSelectDefinitions = useSchemaInSelect && metadata.supportsSchemasInTableDefinitions();
            mCatalogsInIndexDefinitions = metadata.supportsCatalogsInIndexDefinitions();
            mSchemasInIndexDefinitions = metadata.supportsSchemasInIndexDefinitions();
            mCatalogsInDataManipulation = metadata.supportsCatalogsInDataManipulation();
            mSchemasInDataManipulation = metadata.supportsSchemasInDataManipulation();
            mCatalogsInProcedureCalls = metadata.supportsCatalogsInProcedureCalls();
            mSchemasInProcedureCalls = metadata.supportsSchemasInProcedureCalls();
            mCatalogsInPrivilegeDefinitions = metadata.supportsCatalogsInPrivilegeDefinitions();
            mSchemasInPrivilegeDefinitions = metadata.supportsSchemasInPrivilegeDefinitions();
            mCatalogAtStart = metadata.isCatalogAtStart();
            mCatalogSeparator = metadata.getCatalogSeparator();
            String identifierQuoteString = metadata.getIdentifierQuoteString();
            mIdentifierQuoteString = identifierQuoteString;
            mIsCaseSensitive = !identifierQuoteString.isBlank();
        }

        public NamedSupport getNameSupport(ComposeRule rule) {
            NamedSupport support;
            switch (rule) {
                case InTableDefinitions:
                    support = new NamedSupport(mCatalogsInTableDefinitions,
                                               mSchemasInTableDefinitions,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InViewDefinitions:
                    support = new NamedSupport(mCatalogsInViewDefinitions,
                                               mSchemasInViewDefinitions,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InSelectDefinitions:
                    support = new NamedSupport(mCatalogsInSelectDefinitions,
                                               mSchemasInSelectDefinitions,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InIndexDefinitions:
                    support = new NamedSupport(mCatalogsInIndexDefinitions,
                                               mSchemasInIndexDefinitions,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InProcedureCalls:
                    support = new NamedSupport(mCatalogsInProcedureCalls,
                                               mSchemasInProcedureCalls,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InPrivilegeDefinitions:
                    support = new NamedSupport(mCatalogsInPrivilegeDefinitions,
                                               mSchemasInPrivilegeDefinitions,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case Complete:
                    support = new NamedSupport(true,
                                               true,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
                case InDataManipulation:
                default:
                    support = new NamedSupport(mCatalogsInDataManipulation,
                                               mSchemasInDataManipulation,
                                               mCatalogAtStart,
                                               mCatalogSeparator,
                                               mIdentifierQuoteString,
                                               mIsCaseSensitive);
                    break;
            }
            return support;
        }

        public boolean isCaseSensitive() {
            return mIsCaseSensitive;
        }
    }

    public static class NamedSupport {

        boolean mUseCatalogs;
        boolean mUseSchemas;
        boolean mCatalogAtStart;
        String mCatalogSeparator;
        String mIdentifierQuote;
        boolean mCaseSensitive;

        private final String mLiteralQuote = "'";

        NamedSupport(boolean usecatalog,
                             boolean useschema,
                             boolean atstart,
                             String separator,
                             String quote,
                             boolean sensitive) {
            mUseCatalogs = usecatalog;
            mUseSchemas = useschema;
            mCatalogAtStart = atstart;
            mCatalogSeparator = separator;
            mIdentifierQuote = quote;
            mCaseSensitive = sensitive;
        }

        public String enquoteIdentifier(String identifier, boolean sensitive) {
            if (sensitive) {
                identifier = mIdentifierQuote + identifier + mIdentifierQuote;
            }
            return identifier;
        }

        public String enquoteIdentifier(String identifier) {
            return enquoteIdentifier(identifier, mCaseSensitive);
        }

        public String enquoteLiteral(String literal) {
            return mLiteralQuote + literal + mLiteralQuote;
        }

        public boolean isCaseSensitive() {
            return mCaseSensitive;
        }

    }

    public static class NamedComponent {
        private String mCatalog;
        private String mSchema;
        private String mTable;

        public NamedComponent(String catalog, String schema, String table) {
            mCatalog = catalog;
            mSchema = schema;
            mTable = table;
        }

        public NamedComponent() {
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

    public static NamedComponent getNamedComponents(ResultSetMetaData metadata, int index)
        throws SQLException {
        return new NamedComponent(metadata.getCatalogName(index),
                                  metadata.getSchemaName(index),
                                  metadata.getTableName(index));
    }

    /** compose a complete column name from it's up to two parts, regarding to the database meta data composing rules.
     *
     * @param support
     *    The NameComponentSupport.
     * @param component
     *    The table NamedComponent.
     * @param column
     *    The column name.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed column name (ie: with the catalog, schema, table and column name)
     * 
     */

    public static String composeColumnName(final NamedSupport support,
                                           final NamedComponent component,
                                           final String column,
                                           boolean sensitive) {
        String table = composeTableName(support, component, sensitive);
        StringBuilder buffer = new StringBuilder(table);
        buffer.append('.');
        buffer.append(support.enquoteIdentifier(column, sensitive));
        return buffer.toString();
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     *
     * @param support
     *    The NameComponentSupport.
     * @param table
     *    The table as a XPropertySet object.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     */
    public static String composeTableName(NamedSupport support,
                                          XPropertySet table)
        throws SQLException {
        return composeTableName(support, table, false);
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     *
     * @param support
     *    The NameComponentSupport.
     * @param table
     *    The table as a XPropertySet object.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     */
    public static String composeTableName(NamedSupport support,
                                          XPropertySet table,
                                          boolean sensitive)
        throws SQLException {
        NamedComponent component = getTableNamedComponents(table);
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
        return doComposeTableName(support, catalogName, schemaName, component.getTableName(), sensitive);
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     *
     * @param support
     *    The NameComponentSupport.
     * @param table
     *    The named component.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the composed table name (ie: with the catalog, schema and table name)
     *
     */
    public static String composeTableName(NamedSupport support,
                                          NamedComponent table,
                                          boolean sensitive) {
        return doComposeTableName(support, table.getCatalogName(),
                                  table.getSchemaName(), table.getTableName(), sensitive);
    }

    /** compose a complete table name from it's up to three parts, regarding to the database meta data composing rules.
     * @param support
     *    The NameComponentSupport.
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
     *
     */
    public static String composeTableName(NamedSupport support,
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
                buffer.append(support.enquoteIdentifier(catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }
        if (!schema.isEmpty() && support.mUseSchemas) {
            buffer.append(support.enquoteIdentifier(schema, sensitive));
            buffer.append('.');
        }
        buffer.append(support.enquoteIdentifier(table, sensitive));
        if (!catalog.isEmpty() && support.mUseCatalogs && isCatalogValid(!catalogAtStart, catalogSeparator)) {
            buffer.append(catalogSeparator);
            buffer.append(support.enquoteIdentifier(catalog, sensitive));
        }
        System.out.println("DataBaseTools.composeTableName(): Name: " + buffer.toString());
        return buffer.toString();
    }

    public static String buildName(NamedSupport support, NamedComponent component) {
        return buildName(support, component, false);
    }

    public static String buildName(NamedSupport support, NamedComponent table, boolean sensitive) {
        return buildName(support, table.getCatalogName(), table.getSchemaName(), table.getTableName(), sensitive);
    }

    public static String buildName(NamedSupport support, String catalog,
                                   String schema, String table, boolean sensitive) {
        return doComposeTableName(support, catalog, schema, table, sensitive);
    }

    public static String buildName(NamedSupport support,
                                   java.sql.ResultSet result)
        throws SQLException {
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
        return buildName(support, catalog, schema, table, false);
    }

    /** composes a table name for usage in a SELECT statement.
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     *
     * @param support
     *    The NameComponentSupport.
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
    public static String composeTableNameForSelect(NamedSupport support,
                                                   String catalog,
                                                   String schema,
                                                   String table,
                                                   boolean sensitive)
        throws SQLException {
        return buildName(support, catalog, schema, table, sensitive);
    }

    /** composes a table name for usage in a SELECT statement.
     *
     * This includes quoting of the table as indicated by the connection's meta data, plus respecting
     * the settings "UseCatalogInSelect" and "UseSchemaInSelect", which might be present
     * in the data source which the connection belongs to.
     * 
     * @param support
     *    The NameComponentSupport.
     * @param table
     *    The table as property set.
     * @param sensitive
     *    Is the name case sensitive.
     * @return the composed table name (ie: with the catalog, schema and table name)
     * @throws java.sql.SQLException
     */
    public static String composeTableNameForSelect(NamedSupport support,
                                                   XPropertySet table,
                                                   boolean sensitive)
        throws SQLException {
        NamedComponent component = getTableNamedComponents(table);
        return composeTableNameForSelect(support, component.getCatalogName(), component.getSchemaName(),
                                         component.getTableName(), sensitive);
    }

    public static NamedComponent getTableNamedComponents(XPropertySet table)
        throws SQLException {
        NamedComponent component = new NamedComponent();
        if (DBTools.hasDescriptorProperty(table, PropertyIds.NAME)) {
            if (DBTools.hasDescriptorProperty(table, PropertyIds.CATALOGNAME)) {
                component.setCatalog(DBTools.getDescriptorStringValue(table, PropertyIds.CATALOGNAME));
            }
            if (DBTools.hasDescriptorProperty(table, PropertyIds.SCHEMANAME)) {
                component.setSchema(DBTools.getDescriptorStringValue(table, PropertyIds.SCHEMANAME));
            }
            component.setTable(DBTools.getDescriptorStringValue(table, PropertyIds.NAME));
        } else {
            String msg = "ComponentHelper::getTableNamedComponents: ERROR: This is not a table object";
            throw new SQLException(msg);
        }
        return component;
    }

    /** quote the given table name (which may contain a catalog and a schema) according
     *      to the rules provided by the meta data.
     *
     * @param support
     *    The NameComponentSupport.
     * @param name
     *    The full unquoted table name.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return the full quoted table name (ie: with the catalog, schema and table name)
     */
    public static String quoteTableName(NamedSupport support,
                                        String name,
                                        boolean sensitive) {
        if (sensitive) {
            NamedComponent table = qualifiedNameComponents(support, name, sensitive);
            name = composeTableName(support, table.getCatalogName(),
                                    table.getSchemaName(), table.getTableName(), true);
        }
        return name;
    }

    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param support
     *    The NameComponentSupport.
     * @param name
     *    The table name.
     *
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponent qualifiedNameComponents(NamedSupport support, String name) {
        return qualifiedNameComponents(support, name, false);
    }

    
    /** split a fully qualified table name (including catalog and schema, if applicable) into its component parts.
     * @param support
     *    The NameComponentSupport.
     * @param name
     *    The table name.
     * @param unquote
     *    need to unquote the name before.
     *
     * @return the NameComponents object with the catalog, schema and table
     */
    public static NamedComponent qualifiedNameComponents(NamedSupport support,
                                                         String name,
                                                         boolean unquote) {
        NamedComponent component = new NamedComponent();
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
                if (index != -1) {
                    component.setCatalog(buffer.substring(0, index));
                    buffer = buffer.substring(index + 1);
                }
            } else {
                // catalog name at end
                int index = buffer.lastIndexOf(support.mCatalogSeparator);
                if (index != -1) {
                    component.setCatalog(buffer.substring(index + 1));
                    buffer = buffer.substring(0, index);
                }
            }
        }
        if (support.mUseSchemas) {
            int index = buffer.indexOf(".");
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
    public static String unQuoteTableName(NamedSupport support, String name) {
        return name.replace(support.mIdentifierQuote, "");
    }

    private static String doComposeTableName(NamedSupport support,
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
                buffer.append(support.enquoteIdentifier(catalog, sensitive));
                buffer.append(catalogSeparator);
            }
        }

        if (!schema.isEmpty() && support.mUseSchemas) {
            buffer.append(support.enquoteIdentifier(schema, sensitive));
            buffer.append(".");
        }

        buffer.append(support.enquoteIdentifier(table, sensitive));

        if (!catalog.isEmpty() && isCatalogValid(!catalogAtStart, catalogSeparator) && support.mUseCatalogs) {
            buffer.append(catalogSeparator);
            buffer.append(support.enquoteIdentifier(catalog, sensitive));
        }
        return buffer.toString();
    }

    private static boolean isCatalogValid(boolean catalogAtStart, String catalogSeparator) {
        return catalogAtStart && !catalogSeparator.isEmpty();
    }

}
