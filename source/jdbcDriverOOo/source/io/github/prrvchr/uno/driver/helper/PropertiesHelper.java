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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;

import io.github.prrvchr.uno.driver.config.ConfigSQL;

public class PropertiesHelper {

    public static final String CONNECT_PROTOCOL = "jdbc:";
    public static final String REGISTRED_PROTOCOL = "xdbc:";

    public static final String getJdbcUrl(final String url) {
        return url.replaceFirst(REGISTRED_PROTOCOL, CONNECT_PROTOCOL);
    }

    public static final boolean hasSubProtocol(final String url) {
        String[] protocol = url.split(":");
        return protocol.length > 1 && !protocol[1].isBlank();
    }

    public static final String getSubProtocol(final String url) {
        String subprotocol = null;
        if (hasSubProtocol(url)) {
            subprotocol = url.split(":")[1];
        }
        return subprotocol;
    }

    public static final Properties getJdbcConnectionProperties(final PropertyValue[] infos) {
        Properties properties = new Properties();
        for (PropertyValue info : infos) {
            String property = info.Name;
            if (isLibreOfficeProperty(property) || isInternalProperty(property)) {
                continue;
            }
            properties.setProperty(property, String.format("%s", info.Value));
        }
        return properties;
    }

    public static final String getConfigPropertiesPath(final String protocol,
                                                       final String name) {
        return getConfigPath(protocol, "Properties", name);
    }

    public static final String getDefaultConfigPropertiesPath(final String name) {
        return getDefaultConfigPath("Properties", name);
    }

    public static final String getConfigMetaDataPath(final String protocol,
                                                     final String name) {
        return getConfigPath(protocol, "MetaData", name);
    }

    public static final String getDefaultConfigMetaDataPath(final String name) {
        return getDefaultConfigPath("MetaData", name);
    }

    private static final String getConfigPath(final String protocol,
                                              final String path,
                                              final String name) {
        return getConfigPath(protocol, path + "/" + name) + "/Value";
    }

    private static final String getConfigPath(final String protocol,
                                              final String name) {
        return "Installed/" + REGISTRED_PROTOCOL + protocol + ":*/" + name;
    }

    public static final String getDefaultConfigPath(final String path,
                                                    final String name) {
        return getDefaultConfigPath(path + "/" + name) + "/Value";
    }

    public static final String getDefaultConfigPath(final String name) {
        return "Installed/" + REGISTRED_PROTOCOL + "*/" + name;
    }

    public static final Object getConfigProperties(final XHierarchicalNameAccess driver,
                                                   final PropertyValue[] infos,
                                                   final String protocol,
                                                   final String name,
                                                   final Object dflt) {
        Object value = dflt;
        if (hasInfosProperty(infos, name)) {
            value = getInfosProperty(infos, name, null);
        } else {
            value = getConfigProperties(driver, protocol, name, null);
        }
        return value;
    }

    public static final Object getConfigMetaData(final XHierarchicalNameAccess driver,
                                                 final PropertyValue[] infos,
                                                 final String protocol,
                                                 final String name,
                                                 final Object dflt) {
        Object value = dflt;
        if (hasInfosProperty(infos, name)) {
            value = getInfosProperty(infos, name, null);
        } else {
            value = getConfigMetaData(driver, protocol, name, null);
        }
        return value;
    }


    public static final boolean hasInfosProperty(final PropertyValue[] infos,
                                                 final String name) {
        boolean hasProperty = false;
        for (PropertyValue info : infos) {
            if (name.equals(info.Name)) {
                hasProperty = true;
                break;
            }
        }
        return hasProperty;
    }

    public static final Object getInfosProperty(final PropertyValue[] infos,
                                                final String name,
                                                final Object dflt) {
        Object value = dflt;
        for (PropertyValue info : infos) {
            if (name.equals(info.Name)) {
                value = info.Value;
                break;
            }
        }
        return value;
    }

    public static final Object getConfigMetaData(final XHierarchicalNameAccess driver,
                                                 final String protocol,
                                                 final String name) {
        return getConfigMetaData(driver, protocol, name , null);
    }

    public static final Object getConfig(final XHierarchicalNameAccess driver,
                                         final String protocol,
                                         final String name,
                                         final Object dflt) {
        Object value = dflt;
        String property = getConfigPath(protocol, name);
        if (!driver.hasByHierarchicalName(property)) {
            property = getDefaultConfigPath(name);
        }
        if (driver.hasByHierarchicalName(property)) {
            try {
                value = driver.getByHierarchicalName(property);
            } catch (NoSuchElementException e) { }
        }
        return value;
    }

    public static final Object getConfigMetaData(final XHierarchicalNameAccess driver,
                                                   final String protocol,
                                                   final String name,
                                                   final Object dflt) {
        Object value = dflt;
        String property = getConfigMetaDataPath(protocol, name);
        if (!driver.hasByHierarchicalName(property)) {
            property = getDefaultConfigMetaDataPath(name);
        }
        if (driver.hasByHierarchicalName(property)) {
            try {
                value = driver.getByHierarchicalName(property);
            } catch (NoSuchElementException e) { }
        }
        return value;
    }

    public static final Boolean getConfigBooleanProperty(final XHierarchicalNameAccess driver,
                                                         final String protocol,
                                                         final String name,
                                                         final Boolean dflt) {
        return (Boolean) getConfigMetaData(driver, protocol, name, dflt);
    }

    public static final String getConfigStringProperty(final XHierarchicalNameAccess driver,
                                                       final String protocol,
                                                       final String name,
                                                       final String dflt) {
        return (String) getConfigMetaData(driver, protocol, name, dflt);
    }

    public static final Object getConfigProperties(final XHierarchicalNameAccess driver,
                                                   final String protocol,
                                                   final String name) {
        return getConfigProperties(driver, protocol, name , null);
    }

    public static final Object getConfigProperties(final XHierarchicalNameAccess driver,
                                                   final String protocol,
                                                   final String name,
                                                   final Object dflt) {
        Object value = dflt;
        String property = getConfigPropertiesPath(protocol, name);
        if (!driver.hasByHierarchicalName(property)) {
            property = getDefaultConfigPropertiesPath(name);
        }
        if (driver.hasByHierarchicalName(property)) {
            try {
                value = driver.getByHierarchicalName(property);
            } catch (NoSuchElementException e) { }
        }
        return value;
    }

    public static final Object getConfigCommandsProperty(XHierarchicalNameAccess driver,
                                                         String protocol,
                                                         String suffix,
                                                         String name,
                                                         Object value) {
        value = getConfigMetaData(driver, protocol,name, value);
        if (value != null && !suffix.isBlank()) {
            setSQLQueries((Object[]) value, suffix);
        }
        return value;
    }


    public static final String getConfigCommandProperty(final XHierarchicalNameAccess driver,
                                                        final String protocol,
                                                        final String suffix,
                                                        final String name,
                                                        final String value) {
        return getConfigCommandProperty(driver, protocol, suffix, name, value, false);
    }

    public static final String getConfigCommandProperty(final XHierarchicalNameAccess driver,
                                                        final String protocol,
                                                        final String suffix,
                                                        final String name,
                                                        final String defaultValue,
                                                        final boolean parametric) {
        String value = defaultValue;
        String property = getConfigMetaDataPath(protocol, name);
        if (!driver.hasByHierarchicalName(property)) {
            property = getDefaultConfigMetaDataPath(name);
        }
        if (driver.hasByHierarchicalName(property)) {
            try {
                value = (String) driver.getByHierarchicalName(property);
                if (value != null && !value.isBlank() && !parametric && !suffix.isBlank()) {
                    value += suffix;
                }
            } catch (NoSuchElementException e) { }
        }
        return value;
    }

    public static final String[] getFormatKeys(final String template) {
        List<String> keys = new ArrayList<>();

        Matcher matcher = Pattern.compile(ConfigSQL.KEY_PATTERN).matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }

        return keys.toArray(new String[0]);
    }

    public static final String format(final String template,
                                      final Map<String, Object> parameters) {
        StringBuilder newTemplate = new StringBuilder(template);
        List<Object> valueList = new ArrayList<>();

        for (String key : getFormatKeys(template)) {
            String paramName = ConfigSQL.KEY_PREFIX + key + ConfigSQL.KEY_SUFFIX;
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                valueList.add(parameters.get(key));
            }
        }

        return String.format(newTemplate.toString(), valueList.toArray());
    }

    public static final Map<String, Object> getKeysArgument(final XHierarchicalNameAccess config,
                                                            final PropertyValue[] infos,
                                                            final String protocol,
                                                            final String[] keys) {
        Map<String, Object> arguments = new HashMap<>();
        for (String key : keys) {
            Object value = null;
            if (hasInfosProperty(infos, key)) {
                value = getInfosProperty(infos, key, null);
            } else {
                value = getConfigStringProperty(config, protocol, key, null);
            }
            if (value != null) {
                arguments.put(key, value);
            }
        }
        return arguments;
    }

    private static final void setSQLQueries(final Object[] queries,
                                            final String suffix) {
        // XXX: We need to be able to add a suffix to SQL commands.
        // XXX: This allows us to support drivers requiring a semicolon at the end of each command
        // XXX: while still being able to provide default SQL / DDL commands for these drivers.
        for (int i = 0; i < queries.length; i++) {
            String value = (String) queries[i];
            // XXX: An blank query can exist in multi-query commands and should be left blank.
            if (!value.isBlank()) {
                queries[i] += suffix;
            }
        }
    }

    private static final boolean isLibreOfficeProperty(final String property) {
        // XXX: These are properties used internally by LibreOffice,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        // XXX: see: connectivity/source/drivers/jdbc/tools.cxx createStringPropertyArray()
        boolean is = false;
        switch (property) {
            case "JavaDriverClass":
            case "JavaDriverClassPath":
            case "SystemProperties":
            case "CharSet":
            case "AppendTableAliasName":
            case "AppendTableAliasInSelect":
            case "DisplayVersionColumns":
            case "GeneratedValues":
            case "UseIndexDirectionKeyword":
            case "UseKeywordAsBeforeAlias":
            case "AddIndexAppendix":
            case "FormsCheckRequiredFields":
            case "GenerateASBeforeCorrelationName":
            case "EscapeDateTime":
            case "ParameterNameSubstitution":
            case "IsPasswordRequired":
            case "IsAutoRetrievingEnabled":
            case "AutoRetrievingStatement":
            case "UseCatalogInSelect":
            case "UseSchemaInSelect":
            case "AutoIncrementCreation":
            case "Extension":
            case "NoNameLengthLimit":
            case "EnableSQL92Check":
            case "EnableOuterJoinEscape":
            case "BooleanComparisonMode":
            case "IgnoreCurrency":
            case "TypeInfoSettings":
            case "IgnoreDriverPrivileges":
            case "ImplicitCatalogRestriction":
            case "ImplicitSchemaRestriction":
            case "SupportsTableCreation":
            case "UseJava":
            case "Authentication":
            case "PreferDosLikeLineEnds":
            case "PrimaryKeySupport":
            case "RespectDriverResultSetType":
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    private static final boolean isInternalProperty(final String property) {
        // XXX: These are properties used internally by jdbcDriverOOo,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        boolean is = false;
        switch (property) {
            case "TablePrivilegesSettings":
            case "PrivilegesSettings":
            case "RowVersionCreation":
            case "LogLevel":
            case "InMemoryDataBase":
            case "Type":
            case "Url":
            case "ShowSystemTable":
            case "CachedRowSet":
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

}
