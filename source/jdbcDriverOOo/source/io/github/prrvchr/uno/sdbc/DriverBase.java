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
package io.github.prrvchr.uno.sdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.lib.uno.helper.ComponentBase;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.UnoLoggerPool;
import io.github.prrvchr.driver.helper.DBException;
import io.github.prrvchr.driver.provider.ApiLevel;
import io.github.prrvchr.driver.provider.DriverManagerHelper;
import io.github.prrvchr.driver.provider.DriverPropertiesHelper;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;


public abstract class DriverBase
    extends ComponentBase
    implements XServiceInfo,
               XDriver {

    public static final String IDENTIFIER = "io.github.prrvchr.jdbcDriverOOo";
    protected final ResourceBasedEventLogger mLogger;
    private final String mService;
    private final String[] mServices;
    private XComponentContext mContext;
    private XHierarchicalNameAccess mConfig;
    private XHierarchicalNameAccess mDriver;

    // The constructor method:
    public DriverBase(final XComponentContext context,
                      final String service, 
                      final String[] services)
        throws Exception {
        mContext = context;
        mService = service;
        mServices = services;
        // XXX: We are loading the logger provider...
        SharedResources.registerClient(context, IDENTIFIER, "resource", "Driver");
        mLogger = new ResourceBasedEventLogger(context, IDENTIFIER, "resource",
                                               "Driver", "io.github.prrvchr.jdbcDriverOOo.Driver");
        UnoLoggerPool.initialize(context, IDENTIFIER);
        // XXX: We are loading configurations...
        mConfig = getDriverConfiguration(context, IDENTIFIER, this);
        mDriver = getDriverConfiguration(context, "org.openoffice.Office.DataAccess.Drivers", this);
        if (isJavaLoggerEnabled()) {
            DriverManagerHelper.setJavaLoggerService(context, IDENTIFIER);
        }
        System.out.println("sdbc.DriverBase.DriverBase() 1");
    }

    private boolean isJavaLoggerEnabled() {
        boolean enabled = false;
        try {
            enabled = (boolean) mConfig.getByHierarchicalName("EnableJavaSystemLogger");
        } catch (NoSuchElementException e) { }
        return enabled;
    }

    // com.sun.star.lang.XComponent:
    @Override
    protected synchronized void postDisposing() {
        mContext = null;
        SharedResources.revokeClient();
        UnoHelper.disposeComponent(mConfig);
        UnoHelper.disposeComponent(mDriver);
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mService);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServices);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServices, service);
    }


    // com.sun.star.sdbc.XDriver:
    public XConnection connect(String url,
                               PropertyValue[] info)
        throws SQLException {
        ConnectionBase connection = null;
        System.out.println("sdbc.DriverBase.connect() 1 Url: " + url);
        mLogger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_CONNECTING_URL, url);
        // XXX: The driver should return NULL if it realizes it is
        // XXX: the wrong kind of driver to connect to the given URL
        if (acceptsURL(url)) {
            ApiLevel apiLevel = getApiLevel(info);
            Properties properties = DriverPropertiesHelper.getJdbcConnectionProperties(info);
            DriverProvider provider = new DriverProvider(mContext, this, mLogger,
                                                         mDriver, mConfig, url, info, properties, apiLevel);
            System.out.println("sdbc.DriverBase.connect() 2 Service: " + apiLevel);
            connection = getConnection(mContext, provider, url, info, properties.stringPropertyNames());
            String services = String.join(", ", connection.getSupportedServiceNames());
            mLogger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_SUCCESS, services,
                           connection.getProvider().getLogger().getObjectId());
        }
        return connection;
    }

    public boolean acceptsURL(String url)
        throws SQLException {
        boolean accept = url.startsWith(DriverPropertiesHelper.REGISTRED_PROTOCOL) &&
                         DriverPropertiesHelper.hasSubProtocol(url);
        return accept;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, PropertyValue[] infos)
        throws SQLException {
        if (!acceptsURL(url)) {
            final int resource = Resources.STR_URI_SYNTAX_ERROR;
            final String message = SharedResources.getInstance().getResourceWithSubstitution(resource, url);
            throw DBException.getSQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR);
        }
        List<DriverPropertyInfo> properties = new ArrayList<DriverPropertyInfo>();
        try {
            String protocol = DriverPropertiesHelper.getSubProtocol(url);
            for (PropertyValue info : infos) {
                String path = DriverPropertiesHelper.getConfigPropertiesPath(protocol, info.Name);
                if (!mDriver.hasByHierarchicalName(path)) {
                    path = DriverPropertiesHelper.getDefaultConfigPropertiesPath(info.Name);
                }
                if (mDriver.hasByHierarchicalName(path)) {
                    String value = null;
                    Object[] values = null;
                    switch (info.Name) {
                        case "IsAutoRetrievingEnabled":
                            Boolean state = (Boolean) mDriver.getByHierarchicalName(path);
                            String[] choices1 = {"false", "true"};
                            properties.add(new DriverPropertyInfo("IsAutoRetrievingEnabled",
                                    "Retrieve generated values.", true, state.toString(), choices1));
                            break;
                        case "AutoRetrievingStatement":
                            value = (String) mDriver.getByHierarchicalName(path);
                            String[] choices2 = {value, };
                            properties.add(new DriverPropertyInfo("AutoRetrievingStatement",
                                    "getGeneratedKey() statement.", true, value, choices2));
                            break;
                        case "AutoIncrementCreation":
                            value = (String) mDriver.getByHierarchicalName(path);
                            String[] choices3 = {value, };
                            properties.add(new DriverPropertyInfo("AutoIncrementCreation",
                                    "Auto-increment creation statement.", true, value, choices3));
                            break;
                        case "RowVersionCreation":
                            values = (Object[]) mDriver.getByHierarchicalName(path);
                            value = (String) values[0];
                            properties.add(new DriverPropertyInfo("RowVersionCreation",
                                    "Row version creation statement.", true, value, (String[]) values));
                            break;
                        case "TypeInfoSettings":
                            values = (Object[]) mDriver.getByHierarchicalName(path);
                            value = (String) values[0];
                            properties.add(new DriverPropertyInfo("TypeInfoSettings",
                                    "Defines how the type info of the database metadata should be manipulated.",
                                    true, "", (String[]) values));
                            break;
                        case "TablePrivilegesSettings":
                            values = (Object[]) mDriver.getByHierarchicalName(path);
                            value = (String) values[0];
                            properties.add(new DriverPropertyInfo("TablePrivilegesSettings",
                                    "Lists privileges supported by the underlying driver.",
                                    true, value, (String[]) values));
                            break;
                    }
                }
            }
        } catch (NoSuchElementException e) {
            throw DBException.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR);
        }
/*
        String[] boolchoices = {"false", "true"};
        String row = "GENERATED ALWAYS AS ROW START";
        String[] rows = {"GENERATED ALWAYS AS ROW START", "GENERATED ALWAYS AS ROW END"};
        // XXX: Name, Description, IsRequired, Value, Choices
        properties.add(new DriverPropertyInfo("JavaDriverClass", "The JDBC driver class name.",
                                              true, "", new String[0]));
        properties.add(new DriverPropertyInfo("JavaDriverClassPath",
            "The class path where to look for the JDBC driver.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("SystemProperties",
            "Additional properties to set at java.lang.System before loading the driver.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("ParameterNameSubstitution",
            "Change named parameters with '?'.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("IsAutoRetrievingEnabled",
            "Retrieve generated values.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("AutoRetrievingStatement",
            "getGeneratedKey() statement.", false, "", new String[0]));
        properties.add(new DriverPropertyInfo("GenerateASBeforeCorrelationName",
            "Generate AS before table correlation names.", false, "true", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("IgnoreCurrency",
            "Ignore the currency field from the ResultsetMetaData.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("EscapeDateTime",
            "Escape date time format.", false, "true", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("ImplicitCatalogRestriction",
            "The catalog which should be used in getTables calls, when the caller passed NULL.",
             false, "", new String[0]));
        properties.add(new DriverPropertyInfo("ImplicitSchemaRestriction",
            "The schema which should be used in getTables calls, when the caller passed NULL.",
             false, "", new String[0]));
        properties.add(new DriverPropertyInfo("AutoIncrementCreation",
            "Auto-increment creation statement.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("RowVersionCreation",
            "Row version creation statement.", true, row, rows.clone()));
        properties.add(new DriverPropertyInfo("IgnoreDriverPrivileges",
            "Ignore the privileges from the database driver.", false, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("AddIndexAppendix",
            "Add an appendix (ASC or DESC) when creating the index.", true, "false", boolchoices.clone()));
        properties.add(new DriverPropertyInfo("TypeInfoSettings",
            "Defines how the type info of the database metadata should be manipulated.", true, "", new String[0]));
        properties.add(new DriverPropertyInfo("TablePrivilegesSettings",
            "Lists privileges supported by the underlying driver.", true, "", new String[0]));
*/
        System.out.println("sdbc.DriverBase.getPropertyInfo() 2");
        return properties.toArray(new DriverPropertyInfo[0]);
    }

    public int getMajorVersion() {
        return 1;
    }


    public int getMinorVersion() {
        return 0;
    }

    // Protected methods:
    protected final XComponentContext getComponentContext() {
        return mContext;
    }

    // Private methods:
    private ApiLevel getApiLevel(PropertyValue[] info) {
        String apilevel = ApiLevel.COM_SUN_STAR_SDBC.service();
        apilevel = UnoHelper.getConfigurationOption(mConfig, "ApiLevel", apilevel);
        apilevel = UnoHelper.getDefaultPropertyValue(info, "ApiLevel", apilevel);
        return ApiLevel.fromString(apilevel);
    }

    private static XHierarchicalNameAccess getDriverConfiguration(final XComponentContext context,
                                                                  final String path,
                                                                  final XInterface source)
        throws SQLException {
        try {
            return UnoHelper.getConfiguration(context, path);
        } catch (Exception e) {
            int resource = Resources.STR_LOG_CONFIGURATION_LOADING_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, path);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_GENERAL_ERROR);
        }
    }

    protected abstract ConnectionBase getConnection(XComponentContext ctx,
                                                    DriverProvider provider,
                                                    String url,
                                                    PropertyValue[] info,
                                                    Set<String> properties);

}
