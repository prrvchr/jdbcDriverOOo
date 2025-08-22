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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.driver.config.ConfigBase;
import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.DBException;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponentSupport;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;


public class Provider {

    static final String LEVEL_OFF = "OFF";

    protected ConnectionLog mLogger;

    private ConfigSQL mConfig;

    private String mSubProtocol;
    private PropertyValue[] mInfos;
    private java.sql.Statement mStatement = null;

    private boolean mSupportsTransactions = true;

    private NamedComponentSupport mNamedComponentSupport;

    // The constructor method:
    public Provider(final XComponentContext ctx,
                    final XInterface source,
                    final ResourceBasedEventLogger logger,
                    final XHierarchicalNameAccess config,
                    final XNameAccess opts,
                    final String url,
                    final PropertyValue[] infos,
                    final Properties properties,
                    final String api)
        throws SQLException {
        String location = PropertiesHelper.getJdbcUrl(url);

        try {
            mSubProtocol = PropertiesHelper.getSubProtocol(url);
            final String name = DriverManager.getDriverName(config, mSubProtocol);
            final String clsname = DriverManager.getDriverClassName(source, config, infos,
                                                                    mSubProtocol, name);

            if (!DriverManager.isDriverRegistered(clsname)) {
                boolean add = ConfigBase.addDriverToClassPath(opts);
                String clspath = DriverManager.getDriverClassPath(ctx, source, config,
                                                                  infos, mSubProtocol, name);
                System.out.println("jdbcdriver.Provider() 1 ClassPath: "  + clspath + " - ClassName: " + clsname);
                System.out.println("jdbcdriver.Provider() 2 Location: "  + location);

                Driver driver = DriverManager.getDriverByClassName(source, clspath, clsname, add);
                Path path = DriverManager.registerDriver(source, driver, clspath, clsname, name);
                logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, path);

            }

            // XXX: It is the provider who holds the connection log
            mLogger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);

            mInfos = infos;

            setSystemProperties(logger, config, infos);

            java.sql.Connection connection = java.sql.DriverManager.getConnection(location, properties);
            java.sql.DatabaseMetaData metadata = connection.getMetaData();

            // XXX: Get the corresponding query composer at the API level
            switch (api) {
                case "sdb":
                    mConfig = new ConfigDCL(config, opts, infos, url, metadata, mSubProtocol);
                    break;
                case "sdbcx":
                    mConfig = new ConfigDDL(config, opts, infos, url, metadata, mSubProtocol);
                    break;
                case "sdbc":
                    mConfig = new ConfigSQL(config, opts, infos, url, metadata, mSubProtocol);
                    break;
            }

            // XXX: keep some DataBaseMetaData data in cache...
            mSupportsTransactions = metadata.supportsTransactions();
            mNamedComponentSupport = new NamedComponentSupport(metadata, mConfig);

            // XXX: We do not keep the connection but the statement
            // XXX: which allows us to find the connection if necessary.
            mStatement = connection.createStatement();

        } catch (java.sql.SQLException e) {
            System.out.println("jdbcdriver.DriverProvider() ERROR: SQLState: "  + e.getSQLState());
            throw new SQLException(e.getMessage(), null, e.getSQLState(), 0, null);
        } catch (Throwable e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("jdbcdriver.DriverProvider() ERROR: "  + sw.toString());
            int resource = Resources.STR_LOG_NO_SYSTEM_CONNECTION;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
    }

    public NamedSupport getNamedSupport() {
        return getNamedSupport(ComposeRule.Complete);
    }

    public NamedSupport getNamedSupport(ComposeRule rule) {
        return mNamedComponentSupport.getNameSupport(rule);
    }

    public ConnectionLog getLogger() {
        return mLogger;
    }

    public PropertyValue[] getInfos() {
        return mInfos;
    }

    public boolean isCaseSensitive() {
        return mNamedComponentSupport.isCaseSensitive();
    }

    public ConfigSQL getConfigSQL() {
        return mConfig;
    }

    public ConfigDDL getConfigDDL() {
        return (ConfigDDL) mConfig;
    }

    public ConfigDCL getConfigDCL() {
        return (ConfigDCL) mConfig;
    }

    public int getDataType(final int type) {
        return type;
    }

    public String[] getViewTypes() {
        return new String[]{"VIEW"};
    }

    public String getTableType(final String type) {
        String tabletype = type;
        if (mConfig.hasTableTypesSettings() && mConfig.getTableTypesSettings().containsKey(type)) {
            tabletype = mConfig.getTableTypesSettings().get(type);
        }
        return tabletype;
    }

    public boolean acceptsURL(final String url) {
        boolean accept = false;
        if (url.startsWith(PropertiesHelper.REGISTRED_PROTOCOL) &&
            PropertiesHelper.hasSubProtocol(url)) {
            accept = true;
        }
        return accept;
    }

    // DatabaseMetadata cache data
    public boolean supportsTransactions() {
        return mSupportsTransactions;
    }

    public int getGeneratedKeysOption() {
        int keyOption;
        if (mConfig.isAutoRetrievingEnabled()) {
            keyOption = java.sql.Statement.RETURN_GENERATED_KEYS;
        } else {
            keyOption = java.sql.Statement.NO_GENERATED_KEYS;
        }
        return keyOption;
    }

    public java.sql.Connection getConnection()
        throws java.sql.SQLException {
        return mStatement.getConnection();
    }

    public java.sql.Connection getJdbcConnection()
        throws SQLException {
        try {
            return mStatement.getConnection();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public java.sql.Statement getStatement()
        throws java.sql.SQLException {
        return mStatement;
    }

    public void closeConnection()
        throws java.sql.SQLException {
        if (mStatement != null) {
            java.sql.Connection connection = mStatement.getConnection();
            mStatement.close();
            mStatement = null;
            connection.close();
        }
    }

    private void setSystemProperties(final ResourceBasedEventLogger logger,
                                     final XHierarchicalNameAccess config,
                                     final PropertyValue[] infos)
        throws SQLException {
        Object value = PropertiesHelper.getConfigMetaData(config, infos, mSubProtocol,
                                                          "SystemProperties", null);
        if (value != null && value instanceof String[]) {
            String template = String.join(System.lineSeparator(), (String[]) value);
            Properties properties = new Properties();
            try {
                properties.load(new StringReader(getFormatedPropertyValue(config, infos, template)));
            } catch (IOException e) {
                String msg = String.join("><", (String[]) value);
                logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_DRIVER_SETTING_SYSTEM_PROPERTIES_ERROR, msg);
            } finally {
                System.setProperties(properties);
                String msg = System.getProperties().entrySet().toString();
                logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_SETTING_SYSTEM_PROPERTIES, msg);
            }
        }
    }

    private String getFormatedPropertyValue(final XHierarchicalNameAccess config,
                                            final PropertyValue[] infos,
                                            final String template) {
        String value = template;
        String[] keys = PropertiesHelper.getFormatKeys(template);
        Map<String, Object> arguments = PropertiesHelper.getKeysArgument(config, infos, mSubProtocol, keys);
        if (!arguments.isEmpty()) {
            value = PropertiesHelper.format(template, arguments);
        }
        return value;
    }

    public Object[] getDriverProperties(final XHierarchicalNameAccess driver,
                                        final String name) {
        return getDriverProperties(driver, name , null);
    }

    public Object[] getDriverProperties(final XHierarchicalNameAccess driver,
                                        final String name,
                                        final Object[] values) {
        return (Object[]) PropertiesHelper.getConfigMetaData(driver, mSubProtocol, name , values);
    }

}