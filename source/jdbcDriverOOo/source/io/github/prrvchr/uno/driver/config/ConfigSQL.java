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
package io.github.prrvchr.uno.driver.config;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;

import io.github.prrvchr.uno.driver.helper.PropertiesHelper;
import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;


public class ConfigSQL extends ConfigBase {


    public static final String KEY_PREFIX = "${";
    public static final String KEY_SUFFIX = "}";
    public static final String KEY_PATTERN = "[$][{]([\\w+\\.*]+)}";

    private static final String SQL_COMMAND_SUFFIX = "SQLCommandSuffix";
    private static final String SUPPORTS_DCL_QUERY = "SupportsDCLQuery";
    private static final String QUOTED_METADATA = "QuotedMetaData";
    private static final String COMPLETED_METADATA = "CompletedMetaData";

    private static final String EMPTY_RESULTSET_QUERY = "SELECT * FROM %s WHERE 0 = 1";
    private static final String GENERATED_KEY_QUERY = "SELECT * FROM %s WHERE %s";
    private static final String METADATA_RESULTSET_QUERY = "SELECT {0} FROM {1} WHERE 0 = 1";

    private static final String[] TABLE_PRIVILEGES_COLUMNS = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                                                              "GRANTOR", "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"};
    private static final String[] DEFAULT_TABLE_PRIVILEGES = {"SELECT", "INSERT", "UPDATE", "DELETE"};

    private final String mIdentifierQuote;
    private final String mSubProtocol;
    private final String mCommandSuffix;
    private final XHierarchicalNameAccess mConfig;

    // A cache of configuration values
    private final Map<String, Boolean> mBooleanProperties;
    private final Map<String, String> mStringProperties;
    private final Map<String, String[]> mStringsProperties;

    // The constructor method:
    public ConfigSQL(final XHierarchicalNameAccess config,
                     final XNameAccess opts,
                     final PropertyValue[] infos,
                     final String url,
                     final DatabaseMetaData metadata,
                     final String subProtocol)
        throws SQLException {
        this(config, opts, infos, url, metadata, subProtocol, true);
    }
    protected ConfigSQL(final XHierarchicalNameAccess config,
                        final XNameAccess opts,
                        final PropertyValue[] infos,
                        final String url,
                        final DatabaseMetaData metadata,
                        final String subProtocol,
                        final boolean rewriteTable)
        throws SQLException {
        super(config, opts, infos, url, metadata, subProtocol, rewriteTable);
        mConfig = config;
        mSubProtocol = subProtocol;
        mIdentifierQuote =  metadata.getIdentifierQuoteString();
        mCommandSuffix = getDriverCommandSuffix(config, subProtocol);
        mBooleanProperties = new HashMap<>();
        mStringProperties = new HashMap<>();
        mStringsProperties = new HashMap<>();
    }

    public boolean useCachedRowSet(final ResultSet rs, final QueryHelper query)
        throws SQLException {
        boolean use = false;
        if (mIsInstrumented) {
            switch (mCachedRowSet) {
                case 0:
                    use = false;
                    break;
                case 1:
                    use = hasRequiredMetaData(query) && isEditableResultSet(rs, query);
                    break;
                case 2:
                    use = true;
                    break;
            }
        }
        return use;
    }

    public boolean needCompletedMetaData() {
        return getPropertyBoolean(COMPLETED_METADATA, false);
    }

    public String getMetaDataIdentifier(String identifier) {
        if (identifier != null && getQuotedMetaData() && !identifier.toUpperCase().equals(identifier)) {
            identifier = enquoteIdentifier(identifier);
        }
        return identifier;
    }

    public String enquoteIdentifier(final String identifier) {
        return mIdentifierQuote + identifier + mIdentifierQuote;
    }

    public boolean supportsDCLQuery() {
        return getSupportsDCLQuery();
    }

    public String[] getTablePrivilegesColumns() {
        return TABLE_PRIVILEGES_COLUMNS;
    }
    

    public String[] getDefaultTablePrivileges() {
        return DEFAULT_TABLE_PRIVILEGES;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getTablePrivileges()
    public java.sql.ResultSet getMetaDataTablePrivileges(final java.sql.DatabaseMetaData md,
                                                         final String catalog,
                                                         final String schema,
                                                         final String table)
        throws SQLException {
        java.sql.ResultSet rs = null;
        if (mIsInstrumented) {
            if (ignoreDriverPrivileges()) {
                String user = md.getUserName();
                String[] privileges = getDefaultTablePrivileges();
                String[] columns = getTablePrivilegesColumns();
                rs = ResultSetHelper.getDefaultTablePrivilegesResultset(privileges, columns,
                                                                        catalog, schema, table, user);
            } else {
                String[] columns = getTablePrivilegesColumns();
                RowSetData data = getTablePrivilegeData();
                rs = ResultSetHelper.getTablePrivilegesResultset(md.getTablePrivileges(catalog, schema, table),
                                                                 columns, data);
            }
        } else {
            rs = md.getTablePrivileges(catalog, schema, table);
        }
        return rs;
    }

    public String getEmptyResultSetQuery(final String tableName) {
        return String.format(EMPTY_RESULTSET_QUERY, tableName);
    }

    public String getGeneratedKeyQuery(final String tableName, final String predicate) {
        return String.format(GENERATED_KEY_QUERY, tableName, predicate);
    }

    public String getResultSetMetaDataQuery() {
        return METADATA_RESULTSET_QUERY;
    }

    protected Boolean getPropertyBoolean(final String key) {
        return getPropertyBoolean(key, null);
    }

    protected Boolean getPropertyBoolean(final String key, final Boolean value) {
        if (!mBooleanProperties.containsKey(key)) {
            try {
                mBooleanProperties.put(key, getConfigurationBoolean(key, value));
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return mBooleanProperties.get(key);
    }

    protected String getPropertyString(final String key) {
        return getPropertyString(key, null);
    }

    protected String getPropertyString(final String key, final String value) {
        if (!mStringProperties.containsKey(key)) {
            try {
                mStringProperties.put(key, getConfigurationString(key, value));
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return mStringProperties.get(key);
    }

    protected String[] getPropertyStrings(final String key) {
        return getPropertyStrings(key, null);
    }

    protected String[] getPropertyStrings(final String key, final String[] values) {
        if (!mStringsProperties.containsKey(key)) {
            try {
                mStringsProperties.put(key, getConfigurationStrings(key, values));
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return mStringsProperties.get(key);
    }

    protected String getIdentifiersAsString(final List<String> identifiers)
        throws SQLException {
        return String.join(getSeparator(), identifiers);
    }

    protected final String format(final String command,
                                  final Map<String, Object> parameters) {
        List<Object> values = new ArrayList<>();
        String template = format(command, parameters, values, "%s");
        return String.format(template, values.toArray());
    }

    protected final String format(final String command,
                                  final Map<String, Object> parameters,
                                  final List<Object> values,
                                  final String token) {
        StringBuilder template = new StringBuilder(command);
        for (String key : getFormatKeys(command)) {
            String parameter = KEY_PREFIX + key + KEY_SUFFIX;
            int index = template.indexOf(parameter);
            if (index != -1) {
                template.replace(index, index + parameter.length(), token);
                values.add(parameters.get(key));
            }
        }
        return template.toString();
    }

    private boolean hasRequiredMetaData(final QueryHelper query) {
        return !needCompletedMetaData() || query.isSingleTableSelect();
    }

    private boolean isEditableResultSet(final ResultSet rs, final QueryHelper query)
        throws SQLException {
        return rs.getType() == ResultSet.TYPE_FORWARD_ONLY ||
               rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY ||
               !query.hasPrimaryKeys();
    }

    private static String getDriverCommandSuffix(final XHierarchicalNameAccess config, final String subprotocol) {
        String suffix = "";
        try {
            String path = PropertiesHelper.getConfigMetaDataPath(subprotocol, SQL_COMMAND_SUFFIX);
            if (config.hasByHierarchicalName(path)) {
                suffix = (String) config.getByHierarchicalName(path);
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return suffix;
    }

    private Boolean getConfigurationBoolean(final String name, final Boolean value)
        throws NoSuchElementException {
        return (Boolean) getConfiguration(mConfig, mSubProtocol, name, value);
    }

    private String getConfigurationString(final String name, String value)
        throws NoSuchElementException {
        value = (String) getConfiguration(mConfig, mSubProtocol, name, value);
        if (!mCommandSuffix.isBlank() && value != null && !value.isBlank()) {
            value += mCommandSuffix;
        }
        return value;
    }

    private String[] getConfigurationStrings(final String name, String[] values)
        throws NoSuchElementException {
        values = (String[]) getConfiguration(mConfig, mSubProtocol, name, values);
        if (!mCommandSuffix.isBlank()) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                values[i] = value + mCommandSuffix;
            }
        }
        return values;
    }

    private final String[] getFormatKeys(final String template) {
        List<String> keys = new ArrayList<>();
        Matcher matcher = Pattern.compile(KEY_PATTERN).matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys.toArray(new String[keys.size()]);
    }

    private boolean getSupportsDCLQuery() {
        return getPropertyBoolean(SUPPORTS_DCL_QUERY, true);
    }

    private boolean getQuotedMetaData() {
        return getPropertyBoolean(QUOTED_METADATA, false);
    }

    private static final Object getConfiguration(final XHierarchicalNameAccess config,
                                                 final String subProtocol,
                                                 final String name,
                                                 Object value) {
        try {
            String path = PropertiesHelper.getConfigMetaDataPath(subProtocol, name);
            if (config.hasByHierarchicalName(path)) {
                value = config.getByHierarchicalName(path);
            } else {
                path = PropertiesHelper.getDefaultConfigMetaDataPath(name);
                if (config.hasByHierarchicalName(path)) {
                    value = config.getByHierarchicalName(path);
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return value;
    }

}