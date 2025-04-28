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
package io.github.prrvchr.driver.provider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

//import com.sun.star.lib.unoloader.UnoClassLoader;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.lib.util.StringHelper;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XMacroExpander;

import io.github.prrvchr.driver.helper.DBException;
import io.github.prrvchr.uno.agent.UnoAgent;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.logger.UnoLoggerFinder;


public class DriverManagerHelper {

    private static final String DRIVER_CLASS = "JavaDriverClass";
    private static final String DRIVER_CLASS_PATH = "JavaDriverClassPath";
    private static final String PLUGIN_JAR_PATH = "/plugin/UnoLogger.jar";
    private static final String JAR = "jar";
    private static final String DOT = ".";
    private static final String EXPAND_PROTOCOL = "vnd.sun.star.expand:";

    public static final void setJavaLoggerService(XComponentContext context, String identifier) {
        try {
            if (UnoAgent.isSupported()) {
                String jar = UnoHelper.getPackageLocation(context, identifier) + PLUGIN_JAR_PATH;
                // XXX: In order to become the Java logging provider (ie: java.lang.System.Logger)
                // XXX: it is necessary to add the facade archive to the system bootloader search path.
                UnoAgent.addToClassPath(jar);
                // XXX: We need to provide the LoggerFinder implementation since
                // XXX: the archive we added to the system bootloader is just a facade.
                UnoLoggerFinder.setLoggerFinder(new DriverLoggerFinder(context));
            }
        } catch (Throwable e) { }
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

    public static final boolean isDriverRegistered(final String url) {
        boolean registered = false;
        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements() && !registered) {
                Driver driver = drivers.nextElement();
                if (driver.acceptsURL(url)) {
                    registered = true;
                }
            }
        } catch (java.sql.SQLException e) { }
        return registered;
    }

    public static final void registerDriver(final XComponentContext context,
                                            final XInterface source,
                                            final XHierarchicalNameAccess config,
                                            final ResourceBasedEventLogger logger,
                                            final String subProtocol,
                                            final PropertyValue[] info)
        throws SQLException {
        System.out.println("DriverManagerHelper.registerDriver() 1");
        final String clazz = (String) DriverPropertiesHelper.getConfigProperties(config, info, subProtocol,
                                                                                 DRIVER_CLASS, null);
        System.out.println("DriverManagerHelper.registerDriver() 2 Class: " + clazz);
        final File[] files = getDriverClassFiles(context, source, config, logger, subProtocol, info, clazz);
        System.out.println("DriverManagerHelper.registerDriver() 3 Files length: " + files.length);
        registerJdbcDriver(source, logger, files, clazz);
    }

    private static final File[] getDriverClassFiles(final XComponentContext context,
                                                    final XInterface source,
                                                    final XHierarchicalNameAccess config,
                                                    final ResourceBasedEventLogger logger,
                                                    final String protocol,
                                                    final PropertyValue[] info,
                                                    final String clazz)
        throws SQLException {
        System.out.println("DriverManagerHelper.getDriverClassFiles() 1");
        File[] files = new File[0];
        Object path = DriverPropertiesHelper.getConfigProperties(config, info, protocol, DRIVER_CLASS_PATH, null);
        if (clazz != null && path != null) {
            System.out.println("DriverManagerHelper.getDriverClassFiles() 2");
            files = getDriverArchiveFiles(source, logger, expandURL(context, source, (String) path), clazz);
        }
        return files;
    }

    private static final String expandURL(final XComponentContext context,
                                          final XInterface source,
                                          final String url)
        throws SQLException {
        String expanded = url;
        if (url != null && url.startsWith(EXPAND_PROTOCOL)) {
            try {
                final Object service = context.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
                final XMacroExpander expander = UnoRuntime.queryInterface(XMacroExpander.class, service);
                // decode uric class chars
                String macro = URLDecoder.decode(StringHelper.replace(url.substring(EXPAND_PROTOCOL.length()),
                                                                      '+', "%2B"), "UTF-8");
                // expand macro string
                expanded = expander.expandMacros(macro);
            } catch (UnsupportedEncodingException e) {
                SQLException ex = new SQLException(e, e.getMessage());
                ex.Context = source;
                throw ex;
            }
        }
        return expanded;
    }

    private static final File[] getDriverArchiveFiles(final XInterface source,
                                                      final ResourceBasedEventLogger logger,
                                                      final String location,
                                                      final String clazz) throws SQLException {
        // XXX: In order to allow the loading of drivers requiring several Java archives,
        // XXX: the JavaDriverClassPath parameter can be a file or a folder
        File file = getArchiveFile(source, location, clazz);
        final List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                if (!f.isDirectory() && isArchiveFile(f)) {
                    System.out.println("DriverManagerHelper.getDriverArchiveFiles() 1 File: " + f.getName());
                    files.add(f);
                }
            }
        } else if (isArchiveFile(file)) {
            System.out.println("DriverManagerHelper.getDriverArchiveFiles() 2 File: " + file.getName());
            files.add(file);
        }

        // XXX: JavaDriverClassPath must contain at least one archive
        if (files.isEmpty()) {
            final int resource;
            if (file.isDirectory()) {
                resource = Resources.STR_LOG_DRIVER_CLASS_PATH_EMPTY;
            } else {
                resource = Resources.STR_LOG_DRIVER_CLASS_PATH_NO_ARCHIVE;
            }
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource,
                                                          location, clazz, DRIVER_CLASS_PATH);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return files.toArray(new File[files.size()]);
    }

    private static final File getArchiveFile(final XInterface source,
                                             final String location,
                                             final String clazz) throws SQLException {
        File file = null;
        try {
            file = new File(new URI(location).normalize());
        } catch (URISyntaxException e) {
            throw getClassPathParseError(source, e, location, clazz);
        }
        if (file == null || !file.exists()) {
            final int resource = Resources.STR_LOG_DRIVER_CLASS_PATH_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location,
                                                                                         clazz, DRIVER_CLASS_PATH);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return file;
    }

    private static final void registerJdbcDriver(final XInterface source,
                                                 final ResourceBasedEventLogger logger,
                                                 final File[] files,
                                                 final String name)
        throws SQLException {
        java.sql.Driver driver = null;
        try {
            System.out.println("DriverManagerHelper.registerJdbcDriver() 1");
            driver = getDriverByServiceLoader(source, logger, files, name);
            if (driver == null) {
                System.out.println("DriverManagerHelper.registerJdbcDriver() 2");
                driver = getDriverByClassName(source, logger, files, name);
                System.out.println("DriverManagerHelper.registerJdbcDriver() 3");
            }
            System.out.println("DriverManagerHelper.registerJdbcDriver() 4 Driver: " + driver.getMajorVersion());
            if (driver != null) {
                System.out.println("DriverManagerHelper.registerJdbcDriver() 5");
                DriverManager.registerDriver(driver);
                System.out.println("DriverManagerHelper.registerJdbcDriver() 6");
            }
        } catch (java.sql.SQLException e) {
            final int resource = Resources.STR_LOG_DRIVER_REGISTER_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBException.getSQLException(msg, StandardSQLState.SQL_UNABLE_TO_CONNECT,
                                              DBException.getSQLException(source, e));
        }
    }

    private static final java.sql.Driver getDriverByServiceLoader(final XInterface source,
                                                                  final ResourceBasedEventLogger logger,
                                                                  final File[] files,
                                                                  final String name) {
        java.sql.Driver driver = null;
        Iterator<java.sql.Driver> drivers = null;
        try {
            URL[] urls = getDriverArchiveUrls(source, logger, files, name, "", "");
            drivers = ServiceLoader.load(java.sql.Driver.class, new URLClassLoader(urls)).iterator();
            if (drivers != null && drivers.hasNext()) {
                driver = new DriverWrapper(drivers.next());
            }
        } catch (SQLException | ServiceConfigurationError | SecurityException | NoSuchElementException e) {
            logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_DRIVER_SERVICE_LOADER_ERROR, name);
        }
        return driver;
    }

    private static final java.sql.Driver getDriverByClassName(final XInterface source,
                                                              final ResourceBasedEventLogger logger,
                                                              final File[] files,
                                                              final String name) throws SQLException {
        java.sql.Driver driver = null;
        try {
            // XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
            URL[] urls = getDriverArchiveUrls(source, logger, files, name, JAR + ":", "!/");
            final URLClassLoader loader = new URLClassLoader(urls, source.getClass().getClassLoader());
            final Class<?> clazz = Class.forName(name, true, loader);
            driver = new DriverWrapper((java.sql.Driver) clazz.getDeclaredConstructor().newInstance());
        } catch (UnsupportedClassVersionError e) {
            final String version = System.getProperty("java.version");
            final int resource = Resources.STR_LOG_DRIVER_UNSUPPORTED_JAVA_VERSION;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, version, name);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            final int resource = Resources.STR_LOG_DRIVER_JAVA_CLASS_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name,
                                                                                         DRIVER_CLASS_PATH,
                                                                                         DRIVER_CLASS);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            final int resource = Resources.STR_LOG_DRIVER_UNEXPECTED_LOADING_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
        return driver;
    }

    private static final boolean isArchiveFile(final File file) {
        return file.getName().toLowerCase().endsWith(DOT + JAR);
    }

    private static final URL[] getDriverArchiveUrls(final XInterface source,
                                                    final ResourceBasedEventLogger logger,
                                                    final File[] files,
                                                    final String clazz,
                                                    String prefix,
                                                    String suffix) throws SQLException {
        String url = "";
        final List<URL> urls = new ArrayList<>();
        try {
            for (File file : files) {
                url = prefix + Path.of(file.getAbsolutePath()).toUri() + suffix;
                System.out.println("DriverManagerHelper.getDriverArchiveUrls() 1 url: " + url);
                urls.add(new URL(url));
                System.out.println("DriverManagerHelper.getDriverArchiveUrls() 2 url: " + url);
                logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, url);
            }
        } catch (MalformedURLException e) {
            throw getClassPathParseError(source, e, url, clazz);
        }
        return urls.toArray(new URL[urls.size()]);
    }

    private static final SQLException getClassPathParseError(final XInterface source,
                                                             final Throwable e,
                                                             final String url,
                                                             final String clazz) {
        final int resource = Resources.STR_LOG_DRIVER_CLASS_PATH_ERROR;
        final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, url,
                                                        clazz, DRIVER_CLASS_PATH);
        return DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
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
            case "RowVersionCreation":
            case "LogLevel":
            case "InMemoryDataBase":
            case "Type":
            case "Url":
            case "ApiLevel":
            case "ShowSystemTable":
            case "UseBookmark":
            case "SQLMode":
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

}
