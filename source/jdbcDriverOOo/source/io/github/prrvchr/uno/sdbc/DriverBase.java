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
package io.github.prrvchr.uno.sdbc;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.DriverPropertyInfo;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDriver;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XMacroExpander;
import com.sun.star.lib.uno.helper.ComponentBase;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.logging.UnoLoggerPool;
import io.github.prrvchr.jdbcdriver.ConnectionService;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderDefault;
import io.github.prrvchr.jdbcdriver.DriverWrapper;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.helper.DBException;
//import io.github.prrvchr.jdbcdriver.helper.DBTools;


public abstract class DriverBase
    extends ComponentBase
    implements XServiceInfo,
               XDriver
{
    private final String m_service;
    private final String[] m_services;
    private XComponentContext m_xContext;
    private XHierarchicalNameAccess m_xConfig;
    private static final String m_connectProtocol = "jdbc:";
    private static final String m_registredProtocol = "xdbc:";
    private static final String m_driverClassPath = "JavaDriverClassPath";
    private static final String m_driverClass = "JavaDriverClass";
    private static final String m_jar = "jar";
    private static final String m_expand = "vnd.sun.star.expand:";
    public static final String m_identifier = "io.github.prrvchr.jdbcDriverOOo";
    private final boolean m_enhanced;

    protected final ResourceBasedEventLogger m_logger;

    // The constructor method:
    public DriverBase(final XComponentContext context,
                      final String service, 
                      final String[] services,
                      boolean enhanced)
        throws SQLException
    {
        m_xContext = context;
        m_service = service;
        m_services = services;
        m_enhanced = enhanced;
        m_xConfig = getDriverConfiguration(context, "org.openoffice.Office.DataAccess.Drivers");

        SharedResources.registerClient(context, m_identifier, "resource", "Driver");
        m_logger = new ResourceBasedEventLogger(context, m_identifier, "resource", "Driver", "io.github.prrvchr.jdbcDriverOOo.Driver");
        UnoLoggerPool.initialize(context, m_identifier);
        System.out.println("sdbc.DriverBase.DriverBase()");
    }
    

    // com.sun.star.lang.XComponent:
    
    @Override
    protected synchronized void postDisposing() {
        m_xContext = null;
        SharedResources.revokeClient();
        UnoHelper.disposeComponent(m_xConfig);
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // com.sun.star.sdbc.XDriver:
    public XConnection connect(String url,
                               PropertyValue[] info)
        throws SQLException
    {
        ConnectionBase connection = null;
        System.out.println("sdbc.DriverBase.connect() 1 Url: " + url);
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_CONNECTING_URL, url);
        // XXX: The driver should return NULL if it realizes it is
        // XXX: the wrong kind of driver to connect to the given URL
        if (!acceptsURL(url)) {
            return connection;
        }
        DriverProvider provider = getDriverProvider(url, info);
        String location = url.replaceFirst(m_registredProtocol, m_connectProtocol);
        String level = provider.getDriverStringProperty(m_xConfig, "DriverLoggerLevel", "-1");
        if (!isDriverRegistered(location)) {
            provider.setSystemProperties(level);
            registerDriver(url, info);
        }

        XHierarchicalNameAccess config = getDriverConfiguration(m_xContext, m_identifier, this);
        provider.setConnection(this, m_logger, m_enhanced, m_xConfig, config, location, info, level);
        String service = ConnectionService.CSS_SDBC_CONNECTION.service();
        service = UnoHelper.getConfigurationOption(config, "ConnectionService", service);
        UnoHelper.disposeComponent(config);
        service = UnoHelper.getDefaultPropertyValue(info, "ConnectionService", service);
        System.out.println("sdbc.DriverBase.connect() 2 Service: " + service);
        connection = getConnection(m_xContext, provider, url, info, ConnectionService.fromString(service));
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_SUCCESS, connection.getProvider().getLogger().getObjectId());
        return connection;
    }

    public boolean acceptsURL(String url)
    throws SQLException
    {
        return url.startsWith(m_registredProtocol) && hasSubProtocol(url);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, PropertyValue[] infos)
    throws SQLException
    {
        if (!acceptsURL(url)) {
            final int resource = Resources.STR_URI_SYNTAX_ERROR;
            final String message = SharedResources.getInstance().getResourceWithSubstitution(resource, url);
            throw DBException.getSQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR);
        }
        List<DriverPropertyInfo> properties = new ArrayList<DriverPropertyInfo>();
        try {
            String protocol = getProtocol(url);
            for (PropertyValue info : infos) {
                String path = DriverProvider.getDriverPropertiesInfo(protocol, info.Name);
                if (m_xConfig.hasByHierarchicalName(path)) {
                    String value = null;
                    Object[] values = null;
                    switch (info.Name) {
                    case "IsAutoRetrievingEnabled":
                        Boolean state = (Boolean) m_xConfig.getByHierarchicalName(path);
                        String[] choices1 = {"false", "true"};
                        properties.add(new DriverPropertyInfo("IsAutoRetrievingEnabled", "Retrieve generated values.", true, state.toString(), choices1));
                        break;
                    case "AutoRetrievingStatement":
                        value = (String) m_xConfig.getByHierarchicalName(path);
                        String[] choices2 = {value, };
                        properties.add(new DriverPropertyInfo("AutoRetrievingStatement", "getGeneratedKey() statement.", true, value, choices2));
                        break;
                    case "AutoIncrementCreation":
                        value = (String) m_xConfig.getByHierarchicalName(path);
                        String[] choices3 = {value, };
                        properties.add(new DriverPropertyInfo("AutoIncrementCreation", "Auto-increment creation statement.", true, value, choices3));
                        break;
                    case "RowVersionCreation":
                        values = (Object[]) m_xConfig.getByHierarchicalName(path);
                        value = (String) values[0];
                        properties.add(new DriverPropertyInfo("RowVersionCreation", "Row version creation statement.", true, value, (String[]) values));
                        break;
                    case "TypeInfoSettings":
                        values = (Object[]) m_xConfig.getByHierarchicalName(path);
                        value = (String) values[0];
                        properties.add(new DriverPropertyInfo("TypeInfoSettings", "Defines how the type info of the database metadata should be manipulated.", true, "", (String[]) values));
                        break;
                    case "TablePrivilegesSettings":
                        values = (Object[]) m_xConfig.getByHierarchicalName(path);
                        value = (String) values[0];
                        properties.add(new DriverPropertyInfo("TablePrivilegesSettings", "Lists privileges supported by the underlying driver.", true, value, (String[]) values));
                        break;
                    }
                }
            }
        } catch (NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
/*
        String[] boolchoices = {"false", "true"};
        String row = "GENERATED ALWAYS AS ROW START";
        String[] rows = {"GENERATED ALWAYS AS ROW START", "GENERATED ALWAYS AS ROW END"};
        // XXX: Name, Description, IsRequired, Value, Choices
        properties.add(new DriverPropertyInfo("JavaDriverClass", "The JDBC driver class name.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("JavaDriverClassPath", "The class path where to look for the JDBC driver.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("SystemProperties", "Additional properties to set at java.lang.System before loading the driver.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("ParameterNameSubstitution", "Change named parameters with '?'.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("IsAutoRetrievingEnabled", "Retrieve generated values.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("AutoRetrievingStatement", "getGeneratedKey() statement.", false, "", new String[0]));
        properties.add(new DriverPropertyInfo("GenerateASBeforeCorrelationName", "Generate AS before table correlation names.", false, "true", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("IgnoreCurrency", "Ignore the currency field from the ResultsetMetaData.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("EscapeDateTime", "Escape date time format.", false, "true", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("ImplicitCatalogRestriction", "The catalog which should be used in getTables calls, when the caller passed NULL.", false, "", new String[0]));
        properties.add(new DriverPropertyInfo("ImplicitSchemaRestriction", "The schema which should be used in getTables calls, when the caller passed NULL.", false, "", new String[0]));
        properties.add(new DriverPropertyInfo("AutoIncrementCreation", "Auto-increment creation statement.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("RowVersionCreation", "Row version creation statement.", true, row, rows.clone()));
        properties.add(new DriverPropertyInfo("IgnoreDriverPrivileges", "Ignore the privileges from the database driver.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("AddIndexAppendix", "Add an appendix (ASC or DESC) when creating the index.", true, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("TypeInfoSettings", "Defines how the type info of the database metadata should be manipulated.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("TablePrivilegesSettings", "Lists privileges supported by the underlying driver.", true, "", new String[0]));
*/
        return properties.toArray(new DriverPropertyInfo[0]);
    }

    public int getMajorVersion()
    {
        return 1;
    }


    public int getMinorVersion()
    {
        return 0;
    }

    // Protected methods:
    protected final XComponentContext getComponentContext() {
        return m_xContext;
    }

    // Private methods:
    private static XHierarchicalNameAccess getDriverConfiguration(final XComponentContext context,
                                                                  final String path)
    throws SQLException
    {
        return getDriverConfiguration(context, path, null);
    }

    private static XHierarchicalNameAccess getDriverConfiguration(final XComponentContext context,
                                                                  final String path,
                                                                  final XInterface source)
    throws SQLException
    {
        try {
            return UnoHelper.getConfiguration(context, path);
        }
        catch (Exception e) {
            int resource = Resources.STR_LOG_CONFIGURATION_LOADING_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, path);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_GENERAL_ERROR);
        }
    }

    private boolean hasSubProtocol(final String url)
    {
        String[] protocol = url.split(":");
        return (protocol.length > 1 && !protocol[1].isBlank());
    }

    private String getProtocol(final String url)
    {
        String protocol = m_registredProtocol;
        if (hasSubProtocol(url)) {
            protocol += url.split(":")[1];
        }
        return protocol;
    }

    private DriverProvider getDriverProvider(final String url,
                                             final PropertyValue[] info)
    {
        ServiceLoader<DriverProvider> loader = ServiceLoader.load(DriverProvider.class, ConnectionBase.class.getClassLoader());
        for (final DriverProvider provider : loader) {
            if (provider.acceptsURL(url)) {
                return provider;
            }
        }
        return new DriverProviderDefault(url, info);
    }

    private boolean isDriverRegistered(final String url)
    {
        try {
            java.sql.DriverManager.getDriver(url);
            return true;
        }
        catch (java.sql.SQLException e) {}
        return false;
    }

    private void registerDriver(final String url,
                                final PropertyValue[] info)
        throws SQLException
    {
        final String protocol = getUrlProtocol(url);
        final String clazz = getDriverClass(protocol, info, url);
        final URL[] path = getDriverClassUrls(protocol, info, clazz, url);
        registerDriver(path, clazz);
    }

    private String getUrlProtocol(final String url)
    {
        String protocol = String.join(":", Arrays.copyOfRange(url.split(":"), 0, 2));
        return protocol + ":*";
    }

    private String getDriverClass(final String protocol,
                                  final PropertyValue[] info,
                                  final String url)
        throws SQLException
    {
        String clazz = UnoHelper.getDefaultPropertyValue(info, m_driverClass, "");
        if (clazz.isBlank()) {
            final String property = getConfigPropertyName(protocol, m_driverClass);
            clazz = getConfigStringProperty(property, url, m_driverClass);
        }
        return clazz;
    }

    private URL[] getDriverClassUrls(final String protocol,
                                     final PropertyValue[] info,
                                     final String clazz,
                                     final String url)
        throws SQLException
    {
        String path = UnoHelper.getDefaultPropertyValue(info, m_driverClassPath, "");
        if (path.isBlank()) {
            final String property = getConfigPropertyName(protocol, m_driverClassPath);
            path = getConfigStringProperty(property, url, m_driverClassPath);
        }
        return getDriverArchiveUrls(expandDriverClassPath(path), clazz);
    }

    private String getConfigPropertyName(final String protocol,
                                         final String property)
    {
        return "Installed/" + protocol + "/Properties/" + property + "/Value";
    }

    private String getConfigStringProperty(final String name,
                                           final String url,
                                           final String property)
        throws SQLException
    {
        try {
            return (String) m_xConfig.getByHierarchicalName(name);
        }
        catch (NoSuchElementException e) {
            final int resource = Resources.STR_LOG_DRIVER_REQUIRED_PARAMETER_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, url, property);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
    }

    private URL[] getDriverArchiveUrls(final String location,
                                       final String clazz)
        throws SQLException
    {
        // XXX: In order to allow the loading of drivers requiring several Java archives,
        // XXX: the JavaDriverClassPath parameter can be a file or a folder
        File path = null;
        try {
            path = new File(new URI(location).normalize());
        }
        catch (URISyntaxException e) {
            throw getClassPathParseError(e, location, clazz);
        }
        if (path == null || !path.exists()) {
            final int resource = Resources.STR_LOG_DRIVER_CLASS_PATH_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location, clazz, m_driverClassPath);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        final List<URL> urls = new ArrayList<>();
        if (path.isDirectory()) {
            for (final File file : path.listFiles()) {
                if (!file.isDirectory()) {
                    addDriverArchiveUrl(urls, file, location, clazz);
                }
            }
        }
        else {
            addDriverArchiveUrl(urls, path, location, clazz);
        }

        // XXX: JavaDriverClassPath must contain at least one archive
        if (urls.isEmpty()) {
            final int resource = path.isDirectory() ? Resources.STR_LOG_DRIVER_CLASS_PATH_EMPTY : Resources.STR_LOG_DRIVER_CLASS_PATH_NO_ARCHIVE;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location, clazz, m_driverClassPath);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT);
        }
        return urls.toArray(new URL[0]);
    }

    private void addDriverArchiveUrl(List<URL> urls,
                                     File file,
                                     String url,
                                     String clazz)
        throws SQLException
    {
        try {
            String name = file.getName();
            if (name.toLowerCase().endsWith("." + m_jar)) {
                String jar = m_jar + ":" + Path.of(file.getAbsolutePath()).toUri() + "!/";
                urls.add(new URL(jar));
                m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, name);
            }
        }
        catch (MalformedURLException e) {
            throw getClassPathParseError(e, url, clazz);
        }
    }

    private SQLException getClassPathParseError(Throwable e,
                                                String url,
                                                String clazz)
    {
        final int resource = Resources.STR_LOG_DRIVER_CLASS_PATH_ERROR;
        final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, url, clazz, m_driverClassPath);
        return DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
    }

    private String expandDriverClassPath(String url)
    {
        if (url.startsWith(m_expand)) {
            final Object service = m_xContext.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
            final XMacroExpander expander = (XMacroExpander) UnoRuntime.queryInterface(XMacroExpander.class, service);
            url = expander.expandMacros(url.replaceFirst(m_expand, ""));
        }
        return url;
    }

    private void registerDriver(final URL[] urls,
                                final String name)
        throws SQLException
    {
        java.sql.Driver driver = null;
        try {
            // XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
            final URLClassLoader loader = new URLClassLoader(urls, DriverBase.class.getClassLoader());
            final Class<?> clazz = Class.forName(name, true, loader);
            driver = new DriverWrapper((java.sql.Driver) clazz.getDeclaredConstructor().newInstance());
        }
        catch(UnsupportedClassVersionError e) {
            final String version = System.getProperty("java.version");
            final int resource = Resources.STR_LOG_DRIVER_UNSUPPORTED_JAVA_VERSION;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, version, name);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
        catch(ClassNotFoundException | IllegalAccessException e) {
            final int resource = Resources.STR_LOG_DRIVER_JAVA_CLASS_NOT_FOUND;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, m_driverClassPath, m_driverClass);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
        catch(NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            final int resource = Resources.STR_LOG_DRIVER_UNEXPECTED_LOADING_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBException.getSQLException(msg, this, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
        try {
            DriverManager.registerDriver(driver);
        }
        catch (java.sql.SQLException e) {
            final int resource = Resources.STR_LOG_DRIVER_REGISTER_ERROR;
            final String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw DBException.getSQLException(msg, StandardSQLState.SQL_UNABLE_TO_CONNECT, DBException.getSQLException(this, e));
        }
    }

    abstract protected ConnectionBase getConnection(final XComponentContext ctx,
                                                    final DriverProvider provider,
                                                    final String url,
                                                    final PropertyValue[] info,
                                                    final ConnectionService service);

}
