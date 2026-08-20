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

import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.uno.UnoRuntime;

public class PropertiesHelper {

    public static final String CONNECT_PROTOCOL = "jdbc:";
    public static final String REGISTRED_PROTOCOL = "juda:";
    public static final String SUFFIX_PROTOCOL = ":*";

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

    public static final Properties getConnectionProperties(final PropertyValue[] infos) {
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

    public static final String streamConnectionProperties(final Properties properties) {
        return properties.entrySet().stream()
                .filter(entry -> !entry.getKey().toString().equals("password"))
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    public static final XNameAccess getRootConfiguration(final XHierarchicalNameAccess config, Object dflt) {
        return UnoRuntime.queryInterface(XNameAccess.class, getConfigProperty(config, getConfigRootPath(), dflt));
    }

    public static final String getDependencieProtocol(final XHierarchicalNameAccess config,
                                                      final String[] nodes,
                                                      final String dependencie,
                                                      final String property) {
        String subProtocol = null;
        for (String node : nodes) {
            if (node.startsWith(REGISTRED_PROTOCOL)) {
                StringJoiner path = new StringJoiner("/");
                path.add(getConfigDriverPath(node)).add("Properties").add(property).add("Value");
                String clsname = (String) getConfigProperty(config, path.toString(), null);
                if (clsname != null && clsname.equals(dependencie)) {
                    subProtocol = getNodeProtocol(node);
                    break;
                }
            }
        }
        return subProtocol;
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
        return getConfigDriverPath(REGISTRED_PROTOCOL + protocol + SUFFIX_PROTOCOL) + "/" + name;
    }

    public static final String getDefaultConfigPath(final String path,
                                                    final String name) {
        return getDefaultConfigPath(path + "/" + name) + "/Value";
    }

    public static final String getDefaultConfigPath(final String name) {
        return getDefaultConfigPath() + "/" + name;
    }

    public static final String getDefaultConfigPath() {
        return getConfigDriverPath(REGISTRED_PROTOCOL + "*") ;
    }

    public static final String getConfigDriverPath(String path) {
        return getConfigRootPath() + "/" + path;
    }

    public static final String getConfigRootPath() {
        return "Installed";
    }

    private static final String getNodeProtocol(final String node) {
        return node.replace(REGISTRED_PROTOCOL, "").replace(SUFFIX_PROTOCOL, "");
    }

    public static final Object getConfigProperties(final XHierarchicalNameAccess driver,
                                                   final PropertyValue[] infos,
                                                   final String protocol,
                                                   final String name,
                                                   final Object dflt) {
        Object value = dflt;
        if (hasInfosProperty(infos, name)) {
            value = getInfosProperty(infos, name, dflt);
        } else {
            value = getConfigProperties(driver, protocol, name, dflt);
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
            value = getInfosProperty(infos, name, dflt);
        } else {
            value = getConfigMetaData(driver, protocol, name, dflt);
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
        String property = getConfigPropertiesPath(protocol, name);
        if (!driver.hasByHierarchicalName(property)) {
            property = getDefaultConfigPropertiesPath(name);
        }
        return getConfigProperty(driver, property, dflt);
    }

    public static final Object getConfigProperty(final XHierarchicalNameAccess driver,
                                                 final String property,
                                                 final Object dflt) {
        Object value = null;
        if (driver.hasByHierarchicalName(property)) {
            try {
                value = driver.getByHierarchicalName(property);
            } catch (NoSuchElementException e) { }
        } else {
            value = dflt;
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
        return switch (property) {
            case "JavaDriverClass",
                 "JavaDriverClassPath",
                 "SystemProperties",
                 "CharSet",
                 "AppendTableAliasName",
                 "AppendTableAliasInSelect",
                 "DisplayVersionColumns",
                 "GeneratedValues",
                 "UseIndexDirectionKeyword",
                 "UseKeywordAsBeforeAlias",
                 "AddIndexAppendix",
                 "FormsCheckRequiredFields",
                 "GenerateASBeforeCorrelationName",
                 "EscapeDateTime",
                 "ParameterNameSubstitution",
                 "IsPasswordRequired",
                 "IsAutoRetrievingEnabled",
                 "AutoRetrievingStatement",
                 "UseCatalogInSelect",
                 "UseSchemaInSelect",
                 "AutoIncrementCreation",
                 "Extension",
                 "NoNameLengthLimit",
                 "EnableSQL92Check",
                 "EnableOuterJoinEscape",
                 "BooleanComparisonMode",
                 "IgnoreCurrency",
                 "TypeInfoSettings",
                 "IgnoreDriverPrivileges",
                 "ImplicitCatalogRestriction",
                 "ImplicitSchemaRestriction",
                 "SupportsTableCreation",
                 "UseJava",
                 "Authentication",
                 "PreferDosLikeLineEnds",
                 "PrimaryKeySupport",
                 "RespectDriverResultSetType" -> true;
            default -> false;
        };
    }

    private static final boolean isInternalProperty(final String property) {
        // XXX: These are properties used internally by jdbcDriverOOo,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        return switch (property) {
            case "SystemCatalogSettings",
                 "SystemSchemaSettings",
                 "SystemTableSettings",
                 "TableSettings",
                 "TablePrivilegesSettings",
                 "PrivilegesSettings",
                 "RowVersionCreation",
                 "LogLevel",
                 "InMemoryDataBase",
                 "Type",
                 "Url",
                 "ShowSystemTable",
                 "ResultSetType",
                 "UseCachedRowSet",
                 "JavaDriverDependencies" -> true;
            default -> false;
        };
    }

}
