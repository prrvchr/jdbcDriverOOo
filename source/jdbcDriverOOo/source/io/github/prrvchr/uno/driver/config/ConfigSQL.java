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
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.provider.PropertiesHelper;


public class ConfigSQL extends ConfigBase {

    private static final String SQL_COMMAND_SUFFIX = "SQLCommandSuffix";
    private static final String SUPPORTS_DCL_QUERY = "SupportsDCLQuery";

    private static final String EMPTY_RESULTSET_QUERY = "SELECT * FROM %s WHERE 0 = 1";
    private static final String GENERATED_KEY_QUERY = "SELECT * FROM %s WHERE %s";
    private static final String METADATA_RESULTSET_QUERY = "SELECT {0} FROM {1} WHERE 0 = 1";

    private static final String[] TABLE_PRIVILEGES_COLUMNS = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                                                              "GRANTOR", "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"};
    private static final String[] DEFAULT_TABLE_PRIVILEGES = {"SELECT", "INSERT", "UPDATE", "DELETE"};


    private final String mKeyPrefix = "${";
    private final String mKeySuffix = "}";
    private final String mKeyPattern = "[$][{]([\\w+\\.*]+)}";

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
        throws SQLException, java.sql.SQLException {
        this(config, opts, infos, url, metadata, subProtocol, true);
    }
    protected ConfigSQL(final XHierarchicalNameAccess config,
                        final XNameAccess opts,
                        final PropertyValue[] infos,
                        final String url,
                        final DatabaseMetaData metadata,
                        final String subProtocol,
                        final boolean rewriteTable)
        throws SQLException, java.sql.SQLException {
        super(config, opts, infos, url, metadata, subProtocol, rewriteTable);
        System.out.println("SQLQuery() 1");
        mConfig = config;
        mSubProtocol = subProtocol;
        mIdentifierQuote =  metadata.getIdentifierQuoteString();
        mCommandSuffix = getDriverCommandSuffix(config, subProtocol);
        mBooleanProperties = new HashMap<>();
        mStringProperties = new HashMap<>();
        mStringsProperties = new HashMap<>();
        System.out.println("SQLQuery() 2");
    }

    public String enquoteIdentifier(String identifier) {
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

    public String getEmptyResultSetQuery(String tableName) {
        return String.format(EMPTY_RESULTSET_QUERY, tableName);
    }

    public String getGeneratedKeyQuery(String tableName, String predicate) {
        return String.format(GENERATED_KEY_QUERY, tableName, predicate);
    }

    public String getResultSetMetaDataQuery() {
        return METADATA_RESULTSET_QUERY;
    }

    private static String getDriverCommandSuffix(XHierarchicalNameAccess config, String subprotocol) {
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

    protected Boolean getPropertyBoolean(String key) {
        return getPropertyBoolean(key, null);
    }

    protected Boolean getPropertyBoolean(String key, Boolean value) {
        if (!mBooleanProperties.containsKey(key)) {
            try {
                mBooleanProperties.put(key, getConfigurationBoolean(key, value));
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return mBooleanProperties.get(key);
    }

    protected String getPropertyString(String key) {
        return getPropertyString(key, null);
    }

    protected String getPropertyString(String key, String value) {
        if (!mStringProperties.containsKey(key)) {
            try {
                mStringProperties.put(key, getConfigurationString(key, value));
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return mStringProperties.get(key);
    }

    protected String[] getPropertyStrings(String key) {
        return getPropertyStrings(key, null);
    }

    protected String[] getPropertyStrings(String key, String[] values) {
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
        throws java.sql.SQLException {
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
                                  List<Object> values,
                                  String token) {
        StringBuilder template = new StringBuilder(command);
        for (String key : getFormatKeys(command)) {
            String parameter = mKeyPrefix + key + mKeySuffix;
            int index = template.indexOf(parameter);
            if (index != -1) {
                template.replace(index, index + parameter.length(), token);
                values.add(parameters.get(key));
            }
        }
        return template.toString();
    }

    private Boolean getConfigurationBoolean(final String name, Boolean value) throws NoSuchElementException {
        return (Boolean) getConfiguration(mConfig, mSubProtocol, name, value);
    }

    private String getConfigurationString(final String name, String value)
        throws NoSuchElementException {
        value = (String) getConfiguration(mConfig, mSubProtocol, name, value);
        System.out.println("Connection.getConfigurationString() 1 value: " + value);
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
        Matcher matcher = Pattern.compile(mKeyPattern).matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys.toArray(new String[keys.size()]);
    }

    private boolean getSupportsDCLQuery() {
        return getPropertyBoolean(SUPPORTS_DCL_QUERY, true);
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