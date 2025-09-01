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

import java.sql.Driver;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.driver.config.ConfigBase;
import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.helper.ComposeRule;
import io.github.prrvchr.uno.driver.helper.PropertiesHelper;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponentSupport;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;


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
                    final ResourceBasedEventLogger logger,
                    final XHierarchicalNameAccess config,
                    final XNameAccess opts,
                    final String url,
                    final PropertyValue[] infos,
                    final Properties properties,
                    final String api)
        throws java.sql.SQLException {
        String location = PropertiesHelper.getJdbcUrl(url);

        mSubProtocol = PropertiesHelper.getSubProtocol(url);

        Driver driver;
        if (DriverManager.isDriverRegistered(mSubProtocol)) {
            driver = DriverManager.getDriver(mSubProtocol);
        } else {
            boolean add = ConfigBase.addDriverToClassPath(opts);
            driver = DriverManager.registerDriver(ctx, config, infos, mSubProtocol, add);
            logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, mSubProtocol);
        }

        // XXX: It is the provider who holds the connection log
        mLogger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);

        mInfos = infos;

        //setSystemProperties(logger, config, infos);

        java.sql.Connection connection = driver.connect(location, properties);
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

    }

    public NamedSupport getNamedSupport() {
        return getNamedSupport(ComposeRule.Complete);
    }

    public boolean hasWarnings() {
        return !mConfig.isInstrumented();
    }

    public java.sql.SQLWarning getWarnings() {
        String msg = mLogger.getStringResource(Resources.STR_LOG_DRIVER_JAVA_INSTRUMENTATION_ERROR);
        return new java.sql.SQLWarning(msg, StandardSQLState.SQL_GENERAL_ERROR.text());
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

}