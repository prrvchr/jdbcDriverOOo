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
import java.net.URL;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.DBException;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;


public class Provider {

    static final String LEVEL_OFF = "OFF";

    protected ConnectionLog mLogger;

    private ConfigSQL mConfig;

    private final boolean mWarnings = true;
    private String mSubProtocol;
    private PropertyValue[] mInfos;
    private java.sql.Statement mStatement = null;

    private Boolean mInsertVisibleInsensitive;
    private Boolean mInsertVisibleSensitive;
    private Boolean mDeleteVisibleInsensitive;
    private Boolean mDeleteVisibleSensitive;
    private Boolean mUpdateVisibleInsensitive;
    private Boolean mUpdateVisibleSensitive;

    private boolean mCatalogsInTableDefinitions;
    private boolean mSchemasInTableDefinitions;
    private boolean mCatalogsInIndexDefinitions;
    private boolean mSchemasInIndexDefinitions;
    private boolean mCatalogsInDataManipulation;
    private boolean mSchemasInDataManipulation;
    private boolean mCatalogsInProcedureCalls;
    private boolean mSchemasInProcedureCalls;
    private boolean mCatalogsInPrivilegeDefinitions;
    private boolean mSchemasInPrivilegeDefinitions;

    private boolean mSupportsTransactions = true;
    private boolean mIsCatalogAtStart = true;

    private String mCatalogSeparator = "";
    private String mIdentifierQuoteString = "";

    // The constructor method:
    public Provider(final XComponentContext ctx,
                    final XInterface source,
                    final ResourceBasedEventLogger logger,
                    final XHierarchicalNameAccess config,
                    final XHierarchicalNameAccess opts,
                    final String url,
                    final PropertyValue[] infos,
                    final Properties properties,
                    final String api)
        throws SQLException {
        System.out.println("jdbcdriver.DriverProvider() 1");
        String location = PropertiesHelper.getJdbcUrl(url);

        try {
            mSubProtocol = PropertiesHelper.getSubProtocol(url);
            final String name = DriverManager.getDriverName(config, mSubProtocol);
            final String clsname = DriverManager.getDriverClassName(source, config, infos,
                                                                    mSubProtocol, name);

            if (!DriverManager.isDriverRegistered(clsname)) {
                boolean add = false;
                String clspath = DriverManager.getDriverClassPath(ctx, source, config,
                                                                  infos, mSubProtocol, name);
                Driver driver = DriverManager.getDriverByClassName(source, clspath, clsname, location, add);
                URL drvurl = driver.getClass().getProtectionDomain().getCodeSource().getLocation();
                Path drvpath = Path.of(drvurl.toURI()).toRealPath();
                DriverManager.registerDriver(source, driver, drvpath, clspath, clsname, name, add);
                logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, drvpath);

                System.out.println("jdbcdriver.DriverProvider() 2");
            }

            // XXX: It is the provider who holds the connection log
            mLogger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);

            mInfos = infos;

            System.out.println("jdbcdriver.DriverProvider() 3");

            setSystemProperties(logger, config, infos);

            java.sql.Connection connection = java.sql.DriverManager.getConnection(location, properties);
            System.out.println("jdbcdriver.DriverProvider() 4");

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
            setDataBaseMetaDataCache(config, metadata);

            // XXX: We do not keep the connection but the statement
            // XXX: which allows us to find the connection if necessary.
            mStatement = connection.createStatement();
            System.out.println("jdbcdriver.DriverProvider() 5 **********************************************");
        } catch (SQLException e) {
            throw e;
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

    private void setDataBaseMetaDataCache(final XHierarchicalNameAccess driver,
                                          final java.sql.DatabaseMetaData metadata)
            throws java.sql.SQLException {
        // XXX: We need to cache some metadata setting
        mCatalogsInTableDefinitions = metadata.supportsCatalogsInTableDefinitions();
        mSchemasInTableDefinitions = metadata.supportsSchemasInTableDefinitions();
        mCatalogsInIndexDefinitions = metadata.supportsCatalogsInIndexDefinitions();
        mSchemasInIndexDefinitions = metadata.supportsSchemasInIndexDefinitions();
        mCatalogsInDataManipulation = metadata.supportsCatalogsInDataManipulation();
        mSchemasInDataManipulation = metadata.supportsSchemasInDataManipulation();
        mCatalogsInProcedureCalls = metadata.supportsCatalogsInProcedureCalls();
        mSchemasInProcedureCalls = metadata.supportsSchemasInProcedureCalls();
        mCatalogsInPrivilegeDefinitions = metadata.supportsCatalogsInPrivilegeDefinitions();
        mSchemasInPrivilegeDefinitions = metadata.supportsSchemasInPrivilegeDefinitions();
        mSupportsTransactions = metadata.supportsTransactions() &&
                                getDriverBooleanProperty(driver, "SupportTransaction", true);
        mIsCatalogAtStart = metadata.isCatalogAtStart();
        mCatalogSeparator = metadata.getCatalogSeparator();
        mIdentifierQuoteString = metadata.getIdentifierQuoteString();
    }

    public ConnectionLog getLogger() {
        return mLogger;
    }

    public PropertyValue[] getInfos() {
        return mInfos;
    }

    public String enquoteLiteral(final String literal)
        throws java.sql.SQLException {
        return getStatement().enquoteLiteral(literal);
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public boolean isResultSetUpdatable(final java.sql.ResultSet result)
        throws java.sql.SQLException {
        return result.getConcurrency() == ResultSet.CONCUR_UPDATABLE;
    }

    public String enquoteIdentifier(String identifier) {
        return enquoteIdentifier(identifier, true);
    }

    public String enquoteIdentifier(String identifier,
                                    final boolean always) {
        // XXX: enquoteIdentifier don't support blank string (ie: catalog or schema name can be empty)
        // XXX: mySQL don't support Statement.enquoteIdentifier()
        // XXX: It seems that double quotes are used instead of backticks
        //if (always && !identifier.isBlank()) {
        //    identifier = getStatement().enquoteIdentifier(identifier, always);
        //}
        if (always) {
            identifier = mConfig.enquoteIdentifier(identifier);
        }
        return identifier;
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

    public boolean supportWarningsSupplier() {
        return mWarnings;
    }

    public boolean isInsertVisible(final ResultSet result)
        throws java.sql.SQLException {
        return isResultSetUpdatable(result) && isInsertVisible(result.getType());
    }

    private boolean isInsertVisible(final int rstype)
        throws java.sql.SQLException {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && mInsertVisibleInsensitive != null) {
            visible = mInsertVisibleInsensitive;
        } else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && mInsertVisibleSensitive != null) {
            visible = mInsertVisibleSensitive;
        } else {
            visible = getConnection().getMetaData().ownInsertsAreVisible(rstype);
        }
        return visible;
    }

    public boolean isUpdateVisible(final ResultSet result)
        throws java.sql.SQLException {
        return isResultSetUpdatable(result) && isUpdateVisible(result.getType());
    }

    private boolean isUpdateVisible(final int rstype)
        throws java.sql.SQLException {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && mUpdateVisibleInsensitive != null) {
            visible = mUpdateVisibleInsensitive;
        } else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && mUpdateVisibleSensitive != null) {
            visible = mUpdateVisibleSensitive;
        } else {
            visible = getConnection().getMetaData().ownUpdatesAreVisible(rstype);
        }
        return visible;
    }

    public boolean isDeleteVisible(final ResultSet result)
        throws java.sql.SQLException {
        return isResultSetUpdatable(result) && isDeleteVisible(result.getType());
    }

    // FIXME: We only consider 2 cases here:
    // FIXME: - Deletions are visible for ResultSet that actually delete rows.
    // FIXME: - Deletions are not visible for ResultSet that do not actually delete rows
    // FIXME:   (ie: replaced with an empty or invalid row or deletion is not visible)
    private boolean isDeleteVisible(final int rstype)
        throws java.sql.SQLException {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && mDeleteVisibleInsensitive != null) {
            visible = mDeleteVisibleInsensitive;
        } else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && mDeleteVisibleSensitive != null) {
            visible = mDeleteVisibleSensitive;
        } else {
            visible = getConnection().getMetaData().ownDeletesAreVisible(rstype);
            if (visible) {
                visible = !getConnection().getMetaData().deletesAreDetected(rstype);
            }
        }
        return visible;
    }

    public void setHoldability(final int holdability) {
        try {
            getConnection().setHoldability(holdability);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // DatabaseMetadata cache data
    public boolean supportsTransactions() {
        return mSupportsTransactions;
    }

    public boolean isCatalogAtStart() {
        return mIsCatalogAtStart;
    }

    public String getCatalogSeparator() {
        return mCatalogSeparator;
    }

    public String getIdentifierQuoteString() {
        return mIdentifierQuoteString;
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

    public boolean supportsCatalogsInTableDefinitions() {
        return mCatalogsInTableDefinitions;
    }
    public boolean supportsSchemasInTableDefinitions() {
        return mSchemasInTableDefinitions;
    }

    public boolean supportsCatalogsInIndexDefinitions() {
        return mCatalogsInIndexDefinitions;
    }
    public boolean supportsSchemasInIndexDefinitions() {
        return mSchemasInIndexDefinitions;
    }

    public boolean supportsCatalogsInDataManipulation() {
        return mCatalogsInDataManipulation;
    }
    public boolean supportsSchemasInDataManipulation() {
        return mSchemasInDataManipulation;
    }

    public boolean supportsCatalogsInProcedureCalls() {
        return mCatalogsInProcedureCalls;
    }
    public boolean supportsSchemasInProcedureCalls() {
        return mSchemasInProcedureCalls;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return mCatalogsInPrivilegeDefinitions;
    }
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return mSchemasInPrivilegeDefinitions;
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

    private Boolean getDriverBooleanProperty(final XHierarchicalNameAccess driver,
                                             final String name,
                                             final Boolean dflt) {
        return PropertiesHelper.getConfigBooleanProperty(driver, mSubProtocol, name , dflt);
    }
}