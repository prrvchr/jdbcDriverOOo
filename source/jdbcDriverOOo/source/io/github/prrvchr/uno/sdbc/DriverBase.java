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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
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
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
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


public abstract class DriverBase
    extends ComponentBase
    implements XServiceInfo,
               XDriver
{
    private final String m_service;
    private final String[] m_services;
    private XComponentContext m_xContext;
    private static final String m_connectProtocol = "jdbc:";
    private static final String m_registredProtocol = "xdbc:";
    private static final String m_driverClassPath = "JavaDriverClassPath";
    private static final String m_driverClass = "JavaDriverClass";
    private static final String m_expandSchema = "vnd.sun.star.expand:";
    public static final String m_identifier = "io.github.prrvchr.jdbcDriverOOo";
    private final boolean m_enhanced;

    protected final ResourceBasedEventLogger m_logger;

    // The constructor method:
    public DriverBase(final XComponentContext context,
                      final String service, 
                      final String[] services,
                      boolean enhanced)
    {
        m_xContext = context;
        m_service = service;
        m_services = services;
        m_enhanced = enhanced;
        System.out.println("sdbc.DriverBase.DriverBase() 1");
        SharedResources.registerClient(context, m_identifier, "resource", "Driver");
        m_logger = new ResourceBasedEventLogger(context, m_identifier, "resource", "Driver", "io.github.prrvchr.jdbcDriverOOo.Driver");
        UnoLoggerPool.initialize(context, m_identifier);
        System.out.println("sdbc.DriverBase.DriverBase() 2");
    }
    

    private XHierarchicalNameAccess _getDriverConfiguration(String path)
        throws Exception
    {
        return UnoHelper.getConfiguration(m_xContext, path);
    }

    public final XComponentContext getComponentContext() {
        return m_xContext;
    }


    // com.sun.star.lang.XComponent:
    
    @Override
    protected synchronized void postDisposing() {
        m_xContext = null;
        SharedResources.revokeClient();
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
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_CONNECTING_URL, url);
        if (acceptsURL(url)) {
            DriverProvider provider = _getDriverProvider(url, info);
            String location = url.replaceFirst(m_registredProtocol, m_connectProtocol);
            XHierarchicalNameAccess config1;
            try {
                config1 = _getDriverConfiguration("org.openoffice.Office.DataAccess.Drivers");
            }
            catch (Exception e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
            String level = provider.getDriverStringProperty(config1, "DriverLoggerLevel", "-1");
            if (!_isDriverRegistered(location)) {
                provider.setSystemProperties(level);
                _registerDriver(config1, _getUrlProtocol(url), info);
            }

            System.out.println("sdbc.DriverBase.connect() 1");
            XHierarchicalNameAccess config2;
            try {
                config2 = UnoHelper.getConfiguration(m_xContext, m_identifier);
            }
            catch (Exception e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
            try {
                provider.setConnection(m_logger, config1, config2, m_enhanced, location, info, level);
            }
            catch(java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
            UnoHelper.disposeComponent(config1);
            String service = ConnectionService.CSS_SDBC_CONNECTION.service();
            service = UnoHelper.getConfigurationOption(config2, "ConnectionService", service);
            UnoHelper.disposeComponent(config2);
            System.out.println("sdbc.DriverBase.connect() 2 Name: " + service);
            service = UnoHelper.getDefaultPropertyValue(info, "ConnectionService", service);
            System.out.println("sdbc.DriverBase.connect() 3 Service: " + service);
            connection = _getConnection(m_xContext, provider, url, info, ConnectionService.fromString(service));
            m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_SUCCESS, connection.getProvider().getLogger().getObjectId());
            System.out.println("sdbc.DriverBase.connect() 4");
        }
        return connection;
    }

    private String _getUrlProtocol(final String url)
    {
        String protocol = String.join(":", Arrays.copyOfRange(url.split(":"), 0, 2));
        return protocol + ":*";
    }

    private boolean _isDriverRegistered(String url)
    {
        try {
            java.sql.DriverManager.getDriver(url);
            return true;
        }
        catch (java.sql.SQLException e) {}
        return false;
    }

    private void _registerDriver(final XHierarchicalNameAccess config,
                                 final String protocol,
                                 final PropertyValue[] info)
        throws SQLException
    {
        try {
            System.out.println("sdbc.DriverBase._registerDriver() 1");
            final String clazz = _getDriverClass(config, protocol, info);
            final URL path = _getDriverClassPath(config, protocol, info);
            System.out.println("sdbc.DriverBase._registerDriver() 2 url: '" + path + "' name: '" + clazz + "'");
            if (path != null && !clazz.isBlank()) {
                System.out.println("sdbc.DriverBase._registerDriver() 3");
                if (!_registerDriver(path, clazz)) {
                    System.out.println("sdbc.DriverBase._registerDriver() 4");
                    _registerDriver(clazz);
                }
            }
            System.out.println("sdbc.DriverBase._registerDriver() 5");
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DriverBase._registerDriver() 6 ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace()) {
                System.out.println(trace);
            }
            System.out.println("sdbc.DriverBase._registerDriver() 7");
        }
    }

    private String _getDriverClass(final XHierarchicalNameAccess driver,
                                   final String protocol,
                                   final PropertyValue[] info)
        throws NoSuchElementException
    {
        System.out.println("sdbc.DriverBase._getDriverClass() 1");
        String clazz = UnoHelper.getDefaultPropertyValue(info, m_driverClass, "");
        System.out.println("sdbc.DriverBase._getDriverClass() 2");
        if (clazz.isBlank()) {
            final String property = "Installed/" + protocol + "/Properties/" + m_driverClass + "/Value";
            if (driver.hasByHierarchicalName(property)) {
                System.out.println("sdbc.DriverBase._getDriverClass() 3");
                clazz = (String) driver.getByHierarchicalName(property);
            }
        }
        return clazz;
    }

    private URL _getDriverClassPath(final XHierarchicalNameAccess config,
                                    final String protocol,
                                    final PropertyValue[] info)
        throws SQLException, NoSuchElementException
    {
        String url = UnoHelper.getDefaultPropertyValue(info, m_driverClassPath, "");
        if (url.isBlank()) {
            final String property = "Installed/" + protocol + "/Properties/" + m_driverClassPath + "/Value";
            if (config.hasByHierarchicalName(property)) {
                url = (String) config.getByHierarchicalName(property);
            }
        }
        if (!url.isBlank()) {
            return _getDriverClassPathUrl(expandDriverClassPath(url));
        }
        return null;
    }

    private String expandDriverClassPath(String url)
    {
        if (url.startsWith(m_expandSchema)) {
            Object service = m_xContext.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
            XMacroExpander expander = (XMacroExpander) UnoRuntime.queryInterface(XMacroExpander.class, service);
            url = expander.expandMacros(url.replaceFirst(m_expandSchema, ""));
        }
        return url;
    }

    private URL _getDriverClassPathUrl(String location)
        throws SQLException
    {
        URL url = null;
        try {
            url = new URL("jar:" + location + "!/");
        }
        catch (java.net.MalformedURLException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        return url;
    }

    private boolean _registerDriver(URL url, String name)
        throws SQLException
    {
        java.sql.Driver driver = null;
        boolean registered = false;
        try {
            // XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
            Class<?> clazz = Class.forName(name, true, new URLClassLoader(new URL[] {url}, DriverBase.class.getClassLoader()));
            driver = new DriverWrapper((java.sql.Driver) clazz.getDeclaredConstructor().newInstance());
        }
        catch(ClassNotFoundException | NoSuchMethodException |
              InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        try {
            DriverManager.registerDriver(driver);
            registered = true;
            System.out.println("sdbc.DriverBase._registerDriver(url, name) 1");
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return registered;
    }

    private void _registerDriver(String name)
        throws SQLException
    {
        java.sql.Driver driver = null;
        try {
            driver = (java.sql.Driver) Class.forName(name).getDeclaredConstructor().newInstance();
        }
        catch(ClassNotFoundException | NoSuchMethodException |
              InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        if (driver != null) {
            try {
                DriverManager.registerDriver(driver);
                System.out.println("sdbc.DriverBase._registerDriver(name) 1");
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
    }

    public boolean acceptsURL(String url)
    throws SQLException
    {
        System.out.println("sdbc.DriverBase.acceptsURL() 1");
        boolean accept = url.startsWith(m_registredProtocol) && _hasSubProtocol(url);
        System.out.println(String.format("sdbc.DriverBase.acceptsURL() Url: %s - Accept: %s - Enhanced: %s", url, accept, m_enhanced));
        return accept;
    }

    private boolean _hasSubProtocol(String url)
    {
        String[] protocol = url.split(":");
        return (protocol.length > 1 && !protocol[1].isBlank());
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, PropertyValue[] info)
    throws SQLException
    {
        if (!acceptsURL(url)) {
            String message = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_URI_SYNTAX_ERROR, url);
            throw new SQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        String[] boolchoices = {"false", "true"};
        List<DriverPropertyInfo> properties = new ArrayList<DriverPropertyInfo>();
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
        properties.add(new DriverPropertyInfo("IgnoreDriverPrivileges", "Ignore the privileges from the database driver.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("AddIndexAppendix", "Add an appendix (ASC or DESC) when creating the index.", true, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("TypeInfoSettings", "Defines how the type info of the database metadata should be manipulated.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("PrivilegesMapping", "Lists privileges supported by the underlying driver.", true, "", new String[0]));
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


    private DriverProvider _getDriverProvider(String url,
                                              PropertyValue[] info)
    {
        ServiceLoader<DriverProvider> loader = ServiceLoader.load(DriverProvider.class, ConnectionBase.class.getClassLoader());
        for (final DriverProvider provider : loader) {
            if (provider.acceptsURL(url)) {
                return provider;
            }
        }
        return new DriverProviderDefault(url, info);
    }

    abstract protected ConnectionBase _getConnection(XComponentContext ctx,
                                                     DriverProvider provider,
                                                     String url,
                                                     PropertyValue[] info,
                                                     ConnectionService service);


}
