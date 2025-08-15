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
package io.github.prrvchr.uno.driver.provider;

import java.io.File;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.lib.util.StringHelper;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XMacroExpander;

import io.github.prrvchr.java.instrumentation.InstrumentationAgent;
import io.github.prrvchr.uno.driver.helper.DBException;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.logger.UnoLoggerFinder;


public class DriverManager {

    private static final String DRIVER_NAME = "DriverTypeDisplayName";
    private static final String DRIVER_CLASS = "JavaDriverClass";
    private static final String DRIVER_CLASS_PATH = "JavaDriverClassPath";
    private static final String PLUGIN_JAR_PATH = "/plugin/";
    private static final String JAR = "jar";
    private static final String JAR_URL_PREFIX = JAR + ":";
    private static final String JAR_URL_SUFFIX = "!/";
    private static final String DOT = ".";
    private static final String EXPAND_PROTOCOL = "vnd.sun.star.expand:";

    private static final List<String> mRegisteredDriver = new ArrayList<>();

    public static final boolean isJavaInstrumantationInstalled() {
        return InstrumentationAgent.isSupported();
    }

    public static final void setJavaRowSetFactory(XComponentContext context, String identifier) {
        try {
            if (InstrumentationAgent.isSupported()) {
                addToClassPath(context, identifier, "RowSetFactory.jar");
            }
        } catch (Throwable e) { }
    }

    public static final void setJavaLoggerService(XComponentContext context, String identifier) {
        try {
            if (InstrumentationAgent.isSupported()) {
                // XXX: In order to become the Java logging provider (ie: java.lang.System.Logger)
                // XXX: it is necessary to add the facade archive to the system bootloader search path.
                addToClassPath(context, identifier, "UnoLogger.jar");
                // XXX: We need to provide the LoggerFinder implementation since
                // XXX: the archive we added to the system bootloader is just a facade.
                UnoLoggerFinder.setLoggerFinder(new LoggerFinder(context));
            }
        } catch (Throwable e) { }
    }

    private static final void addToClassPath(XComponentContext context, String identifier, String jar) {
        try {
            String path = UnoHelper.getPackageLocation(context, identifier);
            InstrumentationAgent.addToClassPath(path + PLUGIN_JAR_PATH + jar);
        } catch (Throwable e) { }
    }

    public static final boolean isDriverRegistered(final String clazz) {
        return mRegisteredDriver.contains(clazz);
    }

    public static final String getDriverName(final XHierarchicalNameAccess config, final String subProtocol) {
        return (String) PropertiesHelper.getConfig(config, subProtocol, DRIVER_NAME, subProtocol);
    }

    public static final String getDriverClassName(final XInterface source,
                                                  final XHierarchicalNameAccess config,
                                                  final PropertyValue[] info,
                                                  final String subProtocol,
                                                  final String name)
        throws SQLException {
        String clsname = (String) PropertiesHelper.getConfigProperties(config, info, subProtocol,
                                                                       DRIVER_CLASS, null);
        if (clsname == null || clsname.isBlank()) {
            final int resource = Resources.STR_LOG_DRIVER_CLASS_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, subProtocol,
                                                                                         DRIVER_CLASS);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return clsname;
    }

    public static final String getDriverClassPath(final XComponentContext context,
                                                  final XInterface source,
                                                  final XHierarchicalNameAccess config,
                                                  final PropertyValue[] info,
                                                  final String subProtocol,
                                                  final String name)
        throws SQLException {
        String path = (String) PropertiesHelper.getConfigProperties(config, info, subProtocol,
                                                                    DRIVER_CLASS_PATH, null);
        String url = null;
        if (path != null && !path.isBlank()) {
            url = expandURL(context, source, path);
        }
        if (url == null) {
            final int resource = Resources.STR_LOG_DRIVER_CLASS_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, subProtocol,
                                                                                         DRIVER_CLASS_PATH);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return url;
    }

    public static final Driver getDriverByClassName(final XInterface source,
                                                    final String clspath,
                                                    final String clsname,
                                                    final boolean add)
        throws SQLException {
        Driver driver = null;
        try {
            URL[] urls = null;
            final File[] files = getDriverArchiveFiles(source, clspath, clsname);
            if (add && InstrumentationAgent.isSupported()) {
                urls = getDriverArchiveUrls(source, files, clsname, false);
                try {
                    for (URL archive : urls) {
                        InstrumentationAgent.addToClassPath(archive.toString());
                    }
                } catch (IOException | URISyntaxException e) {
                    // XXX Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                urls = getDriverArchiveUrls(source, files, clsname, true);
            }
            // XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
            driver = getDriverByName(source, urls, clsname);
        } catch (UnsupportedClassVersionError e) {
            final String version = System.getProperty("java.version");
            final int resource = Resources.STR_LOG_DRIVER_UNSUPPORTED_JAVA_VERSION;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, version, clsname);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
        return driver;
    }

    private static final Driver getDriverByName(final XInterface source,
                                                final URL[] urls,
                                                final String clsname)
        throws SQLException {
        final URLClassLoader loader = new URLClassLoader(urls, source.getClass().getClassLoader());
        try {
            Class<?> clazz = Class.forName(clsname, true, loader);
            return (Driver) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException e) {
            final int resource = Resources.STR_LOG_DRIVER_CLASS_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, clsname,
                                                                                         DRIVER_CLASS_PATH,
                                                                                         DRIVER_CLASS);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            final int resource = Resources.STR_LOG_DRIVER_UNEXPECTED_LOADING_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, clsname);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
    }

    public static final Path registerDriver(final XInterface source,
                                            final Driver driver,
                                            final String clspath,
                                            final String clsname,
                                            final String name)
        throws SQLException {
        Path path = null;
        Path location = null;
        try {
            URL url = driver.getClass().getProtectionDomain().getCodeSource().getLocation();
            path = Path.of(url.toURI()).toRealPath();
            location = Path.of(new URI(clspath)).toRealPath();
        } catch (Throwable e) {
            throw DBException.getSQLException(e.getMessage(), source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        // XXX: Does the loaded JDBC driver come from the built-in drivers of jdbcDriverOOo?
        if (!path.startsWith(location)) {
            final int resource = Resources.STR_LOG_DRIVER_JAVA_CLASS_NOT_SUPPORTED;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, path, name);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        try {
            java.sql.DriverManager.registerDriver(new DriverWrapper(driver));
        } catch (java.sql.SQLException e) {
            final int resource = Resources.STR_LOG_DRIVER_REGISTRATION_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBException.getSQLException(msg, StandardSQLState.SQL_UNABLE_TO_CONNECT,
                                              DBException.getSQLException(source, e));
        }
        mRegisteredDriver.add(clsname);
        return path;
    }

    private static final String expandURL(final XComponentContext context,
                                          final XInterface source,
                                          final String url)
        throws SQLException {
        String expanded = url;
        if (url.startsWith(EXPAND_PROTOCOL)) {
            try {
                final Object service = context.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
                final XMacroExpander expander = UnoRuntime.queryInterface(XMacroExpander.class, service);
                // decode uric class chars
                String macro = URLDecoder.decode(StringHelper.replace(url.substring(EXPAND_PROTOCOL.length()),
                                                                      '+', "%2B"), "UTF-8");
                // expand macro string
                expanded = expander.expandMacros(macro);
            } catch (UnsupportedEncodingException e) {
                throw DBException.getSQLException(e.getMessage(), source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
            }
        }
        return expanded;
    }

    private static final File[] getDriverArchiveFiles(final XInterface source,
                                                      final String location,
                                                      final String clazz) throws SQLException {
        File file = null;
        try {
            file = new File(new URI(location).normalize());
        } catch (URISyntaxException e) { }
        List<File> files = null;
        if (file != null && file.exists()) {
            files = getDriverArchiveFiles(file);
        }
        // XXX: JavaDriverClassPath must contain at least one archive
        if (files == null || files.isEmpty()) {
            final int resource = getDriverArchiveFilesResource(file);
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location,
                                                                                         clazz, DRIVER_CLASS_PATH);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return files.toArray(new File[files.size()]);
    }

    private static final List<File> getDriverArchiveFiles(final File file) {
        // XXX: In order to allow the loading of drivers requiring several Java archives,
        // XXX: the JavaDriverClassPath parameter can be a file or a folder
        final List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                if (!f.isDirectory() && isArchiveFile(f)) {
                    files.add(f);
                }
            }
        } else if (isArchiveFile(file)) {
            files.add(file);
        }
        return files;
    }

    private static final boolean isArchiveFile(final File file) {
        return file.getName().toLowerCase().endsWith(DOT + JAR);
    }

    private static final URL[] getDriverArchiveUrls(final XInterface source,
                                                    final File[] files,
                                                    final String clazz,
                                                    final boolean jar) throws SQLException {
        String url = "";
        final List<URL> urls = new ArrayList<>();
        try {
            for (File file : files) {
                url = Path.of(file.getAbsolutePath()).toAbsolutePath().toUri().toString();
                if (jar) {
                    url = JAR_URL_PREFIX + url + JAR_URL_SUFFIX;
                }
                urls.add(new URL(url));
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

    private static final int getDriverArchiveFilesResource(final File file) {
        final int resource;
        if (file.isDirectory()) {
            resource = Resources.STR_LOG_DRIVER_CLASS_PATH_EMPTY;
        } else {
            resource = Resources.STR_LOG_DRIVER_CLASS_PATH_NO_ARCHIVE;
        }
        return resource;
    }

}
