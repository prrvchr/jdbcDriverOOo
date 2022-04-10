/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
import java.util.Arrays;
import java.util.Properties;
import java.util.ServiceLoader;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.DriverPropertyInfo;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDriver;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMacroExpander;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceComponent;
import io.github.prrvchr.jdbcdriver.DefaultDriverProvider;


public abstract class DriverBase
    extends ServiceComponent
    implements XDriver
{
    //private final URL m_url;
    private final XComponentContext m_xContext;
    private static final String m_connectProtocol = "jdbc:";
    private static final String m_registredProtocol = "xdbc:";
    private static final String m_rootDriver = m_registredProtocol + "*";
    private static final String m_driverClassPath = "JavaDriverClassPath";
    private static final String m_driverClass = "JavaDriverClass";
    private static final String m_expandSchema = "vnd.sun.star.expand:";
    private final boolean m_registered;

    // The constructor method:
    public DriverBase(final XComponentContext context,
                      final String name, 
                      final String[] services)
        throws Exception
    {
        super(name, services);
        m_xContext = context;
        final Object config = UnoHelper.getConfiguration(context, "org.openoffice.Office.DataAccess.Drivers");
        m_registered = _isDriverRegistred(config, services);
        System.out.println("sdbc.DriverBase() 1");
    }

    private boolean _isDriverRegistred(final Object config,
                                       final String[] services)
    {
        boolean registred = false;
        try {
            final String driver = _getRegistredDriver(config);
            for (int i = 0; i < services.length; i++) {
                if (services[i].equals(driver)) {
                    registred = true;
                    break;
                }
            }
        } catch (java.lang.Exception e) {}
        return registred;
    }

    private String _getRegistredDriver(final Object config)
    {
        String service = null;
        try {
            final XHierarchicalNameAccess drivers = (XHierarchicalNameAccess) UnoRuntime.queryInterface(XHierarchicalNameAccess.class, config);
            service = (String) drivers.getByHierarchicalName("Installed/" + m_rootDriver + "/Driver");
        } catch (java.lang.Exception e) {}
        return service;
    }

    // com.sun.star.sdbc.XDriver:
    public XConnection connect(String url, PropertyValue[] info)
        throws SQLException
    {
        System.out.println("sdbc.DriverBase.connect() 1");
        if (!acceptsURL(url)) {
            String message = "ERROR sdbc.Driver.connect() can't accepts URL: " + url;
            throw new SQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        String location = url;
        if (url.startsWith(m_registredProtocol))
            location = url.replaceFirst(m_registredProtocol, m_connectProtocol);
        Properties properties = UnoHelper.getJavaProperties(info);
        if (!_isDriverRegistered(location)) {
            _registerDriver(_getUrlProtocol(url), properties);
        }
        XConnection connection = null;
        System.out.println("sdbc.DriverBase.connect() 2 ClassPath: ");
        try
        {
            System.out.println("sdbc.DriverBase.connect() 3");
            connection = _getConnection(m_xContext, _getDriverProvider(url), _getConnection(location, properties), url, info);
        } catch(java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println(url);
        System.out.println(location);
        System.out.println("sdbc.DriverBase.connect() 4 **************************************************************");
        return connection;
    }

    private String _getUrlProtocol(final String url)
    {
        String protocol = String.join(":", Arrays.copyOfRange(url.split(":"), 0, 2));
        return protocol + ":*";
    }

    private boolean _isDriverRegistered(String url)
    {
        try
        {
            System.out.println("sdbc.DriverBase._isDriverRegistered() 1");
            DriverManager.getDriver(url);
            System.out.println("sdbc.DriverBase._isDriverRegistered() 2");
            return true;
        }
        catch (java.sql.SQLException e) {}
        System.out.println("sdbc.DriverBase._isDriverRegistered() 3");
        return false;
    }

    private void _registerDriver(final String protocol,
                                 final Properties properties)
        throws SQLException
    {
        try
        {
            System.out.println("sdbc.DriverBase._registerDriver() 3");
            final Object config = UnoHelper.getConfiguration(m_xContext, "org.openoffice.Office.DataAccess.Drivers");
            final XHierarchicalNameAccess drivers = (XHierarchicalNameAccess) UnoRuntime.queryInterface(XHierarchicalNameAccess.class, config);
            final String name = _getDriverClass(drivers, protocol, properties);
            final URL url = _getDriverClassPath(drivers, protocol, properties);
            System.out.println("sdbc.DriverBase._registerDriver() 4 url: " + url + " name: " + name);
            if (name != null && url != null)
            {
                if (!_registerDriver(url, name))
                    _registerDriver(name);
            }
            System.out.println("sdbc.DriverBase._registerDriver() 5");
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DriverBase._registerDriver() 6 ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            System.out.println("sdbc.DriverBase._registerDriver() 7");
        }
    }

    private String _getDriverClass(final XHierarchicalNameAccess drivers,
                                   final String protocol,
                                   final Properties properties)
        throws NoSuchElementException
    {
        String clazz = properties.getProperty(m_driverClass);
        if (clazz == null) {
            final String property = "Installed/" + protocol + "/Properties/" + m_driverClass + "/Value";
            if (drivers.hasByHierarchicalName(property)) {
                clazz = (String) drivers.getByHierarchicalName(property);
            }
        }
        return clazz;
    }

    private URL _getDriverClassPath(final XHierarchicalNameAccess drivers,
                                    final String protocol,
                                    final Properties properties)
        throws SQLException, NoSuchElementException
    {
        String url = properties.getProperty(m_driverClassPath);
        if (url == null) {
            final String property = "Installed/" + protocol + "/Properties/" + m_driverClassPath + "/Value";
            if (drivers.hasByHierarchicalName(property)) {
                url = (String) drivers.getByHierarchicalName(property);
            }
        }
        if (url != null && !url.isEmpty())
        {
            return _getDriverClassPathUrl(expandDriverClassPath(url));
        }
        return null;
    }

    private String expandDriverClassPath(String url)
    {
        if (url.startsWith(m_expandSchema))
        {
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
        } catch (java.net.MalformedURLException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        return url;
    }

    private boolean _registerDriver(URL url, String name)
        throws SQLException
    {
        java.sql.Driver driver = null;
        boolean registered = false;
        try
        {
            // XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
            Class<?> clazz = Class.forName(name, true, new URLClassLoader(new URL[] {url}, DriverBase.class.getClassLoader()));
            driver = new DriverWrapper((java.sql.Driver) clazz.getDeclaredConstructor().newInstance());
        }
        catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        try
        {
            DriverManager.registerDriver(driver);
            registered = true;
            System.out.println("sdbc.DriverBase._registerDriver(url, name) 1");
        }
        catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
        return registered;
    }

    private void _registerDriver(String name)
        throws SQLException
    {
        java.sql.Driver driver = null;
        try
        {
            driver = (java.sql.Driver) Class.forName(name).getDeclaredConstructor().newInstance();
        }
        catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        try
        {
            DriverManager.registerDriver(driver);
            System.out.println("sdbc.DriverBase._registerDriver(name) 1");
        }
        catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    public boolean acceptsURL(String url)
    throws SQLException
    {
        // FIXME: To be able to load 2 different drivers (sdbc and sdbcx) that accept the same URLs,
        // FIXME: We have to check if it is the driver that is currently registered (ie: m_registered is true)
        return m_registered && url.startsWith(m_registredProtocol);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, PropertyValue[] info)
    throws SQLException
    {
        if (!acceptsURL(url)) {
            String message = "ERROR sdbc.Driver.getPropertyInfo() can't accepts URL: " + url;
            throw new SQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        String[] nochoices = {};
        String[] boolchoices = {"false", "true"};
        return new DriverPropertyInfo [] {
                new DriverPropertyInfo(
                        "JavaDriverClass", "The JDBC driver class name.",
                        true, "", nochoices),
                new DriverPropertyInfo(
                        "JavaDriverClassPath", "The class path where to look for the JDBC driver.",
                        true, "", nochoices),
                new DriverPropertyInfo(
                        "SystemProperties", "Additional properties to set at java.lang.System before loading the driver.",
                        true, "", nochoices),
                new DriverPropertyInfo(
                        "ParameterNameSubstitution", "Change named parameters with '?'.",
                        false, "false", boolchoices),
                new DriverPropertyInfo(
                        "IgnoreDriverPrivileges", "Ignore the privileges from the database driver.",
                        false, "false", boolchoices),
                new DriverPropertyInfo(
                        "IsAutoRetrievingEnabled", "Retrieve generated values.",
                        false, "false", boolchoices),
                new DriverPropertyInfo(
                        "AutoRetrievingStatement", "Auto-increment statement.",
                        false, "", nochoices),
                new DriverPropertyInfo(
                        "GenerateASBeforeCorrelationName", "Generate AS before table correlation names.",
                        false, "true", boolchoices),
                new DriverPropertyInfo(
                        "IgnoreCurrency", "Ignore the currency field from the ResultsetMetaData.",
                        false, "false", boolchoices),
                new DriverPropertyInfo(
                        "EscapeDateTime", "Escape date time format.",
                        false, "true", boolchoices),
                new DriverPropertyInfo(
                        "TypeInfoSettings", "Defines how the type info of the database metadata should be manipulated.",
                        false, "", nochoices),
                new DriverPropertyInfo(
                        "ImplicitCatalogRestriction", "The catalog which should be used in getTables calls, when the caller passed NULL.",
                        false, "", nochoices),
                new DriverPropertyInfo(
                        "ImplicitSchemaRestriction", "The schema which should be used in getTables calls, when the caller passed NULL.",
                        false, "", nochoices)
        };
    }

    public int getMajorVersion()
    {
        return 1;
    }


    public int getMinorVersion()
    {
        return 0;
    }


    private DriverProvider _getDriverProvider(String url)
    {
        System.out.println("sdbc.DriverBase._getDriverProvider() 1");
        ServiceLoader<DriverProvider> loader = ServiceLoader.load(DriverProvider.class, ConnectionBase.class.getClassLoader());
        System.out.println("sdbc.DriverBase._getDriverProvider() 2");
        for (final DriverProvider provider : loader)
        {
            if (provider.acceptsURL(url)) {
                System.out.println("sdbc.DriverBase._getDriverProvider() 3: " + provider.getClass().getName());
                return provider;
            }
        }
        return new DefaultDriverProvider();
    }

    abstract protected java.sql.Connection _getConnection(String url,
                                                          Properties properties)
        throws java.sql.SQLException;

    abstract protected XConnection _getConnection(XComponentContext ctx,
                                                  DriverProvider provider,
                                                  java.sql.Connection connection,
                                                  String url,
                                                  PropertyValue[] info)
        throws java.sql.SQLException;


}
