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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.driver.helper.DBException;
import io.github.prrvchr.driver.helper.GeneratedKeys;
import io.github.prrvchr.driver.metadata.TableTypesResultSet;
import io.github.prrvchr.driver.query.DCLQuery;
import io.github.prrvchr.driver.query.DDLQuery;
import io.github.prrvchr.driver.query.SQLQuery;
import io.github.prrvchr.driver.rowset.BaseRow;
import io.github.prrvchr.driver.rowset.RowColumn;
import io.github.prrvchr.driver.rowset.RowHelper;
import io.github.prrvchr.driver.rowset.RowTable;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;

public class DriverProvider {

    static final String LEVEL_OFF = "OFF";

    protected ConnectionLog mLogger;

    private SQLQuery mSQLConfig;

    private final boolean mWarnings = true;
    private String mSubProtocol;
    private PropertyValue[] mInfos;
    private java.sql.Statement mStatement = null;

    // XXX: Default setting for ResultSet
    private boolean mUseSQLDelete = false;
    private boolean mUseSQLInsert = false;
    private boolean mUseSQLUpdate = false;

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

    private List<ApiLevel> mSupportedAPILevels = List.of(ApiLevel.COM_SUN_STAR_SDBC,
                                                         ApiLevel.COM_SUN_STAR_SDBCX,
                                                         ApiLevel.COM_SUN_STAR_SDB);

    // The constructor method:
    public DriverProvider(final XComponentContext ctx,
                          final XInterface source,
                          final ResourceBasedEventLogger logger,
                          final XHierarchicalNameAccess drvConfig,
                          final XHierarchicalNameAccess optConfig,
                          final String url,
                          final PropertyValue[] infos,
                          final Properties properties,
                          final ApiLevel level)
        throws SQLException {
        System.out.println("jdbcdriver.DriverProvider() 1");
        String location = DriverPropertiesHelper.getJdbcUrl(url);

        try {
            mSubProtocol = DriverPropertiesHelper.getSubProtocol(url);
            final String name = DriverManagerHelper.getDriverName(drvConfig, mSubProtocol);
            final String clsname = DriverManagerHelper.getDriverClassName(source, drvConfig, infos,
                                                                          mSubProtocol, name);

            if (!DriverManagerHelper.isDriverRegistered(clsname)) {
                String clspath = DriverManagerHelper.getDriverClassPath(ctx, source, drvConfig,
                                                                        infos, mSubProtocol, name);
                Driver driver = DriverManagerHelper.getDriverByClassName(source, clspath, clsname);
                String drvpath = driver.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                DriverManagerHelper.registerDriver(source, driver, drvpath, clspath, clsname, name);
                logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_ARCHIVE_LOADING, drvpath);

                System.out.println("jdbcdriver.DriverProvider() 2");
            }

            // XXX: It is the provider who holds the connection log
            mLogger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);

            mInfos = infos;

            setDriverProperties(drvConfig);

            System.out.println("jdbcdriver.DriverProvider() 3");

            setSystemProperties(logger, drvConfig, infos);

            java.sql.Connection connection = DriverManager.getConnection(location, properties);
            System.out.println("jdbcdriver.DriverProvider() 4");

            java.sql.DatabaseMetaData metadata = connection.getMetaData();
            boolean generatedKeys = metadata.supportsGetGeneratedKeys();
            String identifierQuote = metadata.getIdentifierQuoteString();
            setConnectionMetaData(drvConfig, metadata, identifierQuote);

            switch (level.service()) {
                case "com.sun.star.sdb":
                    mSQLConfig = new DCLQuery(drvConfig, infos, generatedKeys, mSubProtocol, identifierQuote);
                    break;
                case "com.sun.star.sdbcx":
                    mSQLConfig = new DDLQuery(drvConfig, infos, generatedKeys, mSubProtocol, identifierQuote);
                    break;
                case "com.sun.star.sdbc":
                    mSQLConfig = new SQLQuery(drvConfig, infos, generatedKeys, mSubProtocol, identifierQuote);
                    break;
            }

            // XXX: We do not keep the connection but the statement
            // XXX: which allows us to find the connection if necessary.
            mStatement = connection.createStatement();
            System.out.println("jdbcdriver.DriverProvider() 5 **********************************************");
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("jdbcdriver.DriverProvider() ERROR: "  + sw.toString());
            int resource = Resources.STR_LOG_NO_SYSTEM_CONNECTION;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
    }

    private void setDriverProperties(final XHierarchicalNameAccess driver) {
        try {
            mInsertVisibleInsensitive = getDriverBooleanProperty(driver, "InsertVisibleInsensitive", null);
            mInsertVisibleSensitive = getDriverBooleanProperty(driver, "InsertVisibleSensitive", null);
            mDeleteVisibleInsensitive = getDriverBooleanProperty(driver, "DeleteVisibleInsensitive", null);
            mDeleteVisibleSensitive = getDriverBooleanProperty(driver, "DeleteVisibleSensitive", null);
            mUpdateVisibleInsensitive = getDriverBooleanProperty(driver, "UpdateVisibleInsensitive", null);
            mUpdateVisibleSensitive = getDriverBooleanProperty(driver, "UpdateVisibleSensitive", null);

            mUseSQLDelete = getDriverBooleanProperty(driver, "UseSQLDelete", mUseSQLDelete);
            mUseSQLInsert = getDriverBooleanProperty(driver, "UseSQLInsert", mUseSQLInsert);
            mUseSQLUpdate = getDriverBooleanProperty(driver, "UseSQLUpdate", mUseSQLUpdate);

            mSupportedAPILevels = getSupportedAPILevels(driver, "SupportedApiLevels");
        } catch (Exception e) {
            System.out.println("jdbcdriver.DriverProvider() ERROR: " + e );
            e.printStackTrace();
        }
    }

    private void setConnectionMetaData(final XHierarchicalNameAccess driver,
                                       final java.sql.DatabaseMetaData metadata,
                                       final String identifierQuote)
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
        mIdentifierQuoteString = identifierQuote;
    }

    public ConnectionLog getLogger() {
        return mLogger;
    }

    public PropertyValue[] getInfos() {
        return mInfos;
    }

    public List<ApiLevel> getAPILevels() {
        return mSupportedAPILevels;
    }

    public String enquoteLiteral(final String literal)
        throws java.sql.SQLException {
        return getStatement().enquoteLiteral(literal);
    }

    public boolean isCaseSensitive(final String clazz) {
        return true;
    }

    public boolean useSQLMode(final java.sql.ResultSet result)
        throws java.sql.SQLException {
        return mSQLConfig.isSQLMode() || !isResultSetUpdatable(result);
    }

    public boolean useSQLDelete() {
        return mUseSQLDelete;
    }

    public boolean useSQLInsert() {
        return mUseSQLInsert;
    }

    public boolean useSQLUpdate() {
        return mUseSQLUpdate;
    }

    public void setGeneratedKeys(final Statement statement,
                                 final RowTable table,
                                 final BaseRow row)
        throws java.sql.SQLException {
        System.out.println("DriverProvider.setGeneratedKeys() 1");
        String command = mSQLConfig.getAutoRetrievingStatement();
        if (!mSQLConfig.isAutoRetrievingEnabled() || command == null) {
            System.out.println("DriverProvider.setGeneratedKeys() 2");
            return;
        }
        java.sql.ResultSet result = null;
        Map<String, RowColumn> columns = table.getColumnNames();
        if (command.isBlank()) {
            System.out.println("DriverProvider.setGeneratedKeys() 3");
            result = statement.getGeneratedKeys();
        } else {
            System.out.println("DriverProvider.setGeneratedKeys() 4");
            result = GeneratedKeys.getGeneratedResult(this, statement, table, columns, command);
        }
        if (result != null) {
            System.out.println("DriverProvider.setGeneratedKeys() 5");
            ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            if (result.next()) {
                for (int i = 1; i <= count; i++) {
                    // XXX: We are looking for column name
                    String name = metadata.getColumnName(i);
                    if (columns.containsKey(name)) {
                        // XXX: It is important to preserve the type of the original ResultSet columns
                        RowColumn column = columns.get(name);
                        Object value = RowHelper.getResultSetValue(result, i, column.getType());
                        System.out.println("DriverProvider.setGeneratedKeys() 6 value: " + value);
                        row.setColumnObject(column.getIndex(), value);
                    }
                }
            }
        }
        System.out.println("DriverProvider.setGeneratedKeys() 7");
    }

    public boolean isResultSetUpdatable(final java.sql.ResultSet result)
        throws java.sql.SQLException {
        return result.getConcurrency() == ResultSet.CONCUR_UPDATABLE;
    }

    public String enquoteIdentifier(String identifier)
        throws java.sql.SQLException {
        return enquoteIdentifier(identifier, true);
    }

    public String enquoteIdentifier(String identifier,
                                    final boolean always)
        throws java.sql.SQLException {
        // XXX: enquoteIdentifier don't support blank string (ie: catalog or schema name can be empty)
        // XXX: mySQL don't support Statement.enquoteIdentifier()
        // XXX: It seems that double quotes are used instead of backticks
        //if (always && !identifier.isBlank()) {
        //    identifier = getStatement().enquoteIdentifier(identifier, always);
        //}
        if (always) {
            identifier = mIdentifierQuoteString + identifier + mIdentifierQuoteString;
        }
        return identifier;
    }

    public boolean supportService(final ApiLevel service) {
        return mSupportedAPILevels.contains(service);
    }

    public SQLQuery getSQLQuery() {
        return mSQLConfig;
    }

    public DDLQuery getDDLQuery() {
        return (DDLQuery) mSQLConfig;
    }

    public DCLQuery getDCLQuery() {
        return (DCLQuery) mSQLConfig;
    }

    public int getDataType(final int type) {
        return type;
    }

    public String[] getTableTypes() {
        String[] tabletypes = null;
        if (!mSQLConfig.showSystemTable()) {
            List<String> types = new ArrayList<>(List.of("TABLE", "VIEW"));
            if (mSQLConfig.hasTableTypesSettings()) {
                setTableTypes(types);
            }
            tabletypes = types.toArray(new String[0]);
        }
        return tabletypes;
    }

    private void setTableTypes(List<String> types) {
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            if (mSQLConfig.getTableTypesSettings().containsValue(type)) {
                for (Entry<String, String> entry : mSQLConfig.getTableTypesSettings().entrySet()) {
                    if (entry.getValue().equals(type)) {
                        types.set(i, entry.getKey());
                        break;
                    }
                }
            }
        }
    }

    public String[] getViewTypes() {
        return new String[]{"VIEW"};
    }

    public String getTableType(final String type) {
        String tabletype = type;
        if (mSQLConfig.hasTableTypesSettings() && mSQLConfig.getTableTypesSettings().containsKey(type)) {
            tabletype = mSQLConfig.getTableTypesSettings().get(type);
        }
        return tabletype;
    }

    public boolean acceptsURL(final String url) {
        boolean accept = false;
        if (url.startsWith(DriverPropertiesHelper.REGISTRED_PROTOCOL) &&
            DriverPropertiesHelper.hasSubProtocol(url)) {
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

    // connection infos cache data
    public java.sql.ResultSet getTableTypesResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        java.sql.ResultSet result = metadata.getTableTypes();
        if (mSQLConfig.hasTableTypesSettings()) {
            result = new TableTypesResultSet(result, mSQLConfig.getTableTypesSettings());
        }
        return result;
    }

    public java.sql.ResultSet getTypeInfoResultSet()
        throws java.sql.SQLException {
        return getTypeInfoResultSet(getConnection().getMetaData());
    }

    public java.sql.ResultSet getTypeInfoResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        return mSQLConfig.getTypeInfoResultSet(metadata);
    }

    public int getGeneratedKeysOption() {
        int keyOption;
        if (mSQLConfig.isAutoRetrievingEnabled()) {
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
        Object value = DriverPropertiesHelper.getConfigMetaData(config, infos, mSubProtocol,
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
        String[] keys = DriverPropertiesHelper.getFormatKeys(template);
        Map<String, Object> arguments = DriverPropertiesHelper.getKeysArgument(config, infos, mSubProtocol, keys);
        if (!arguments.isEmpty()) {
            value = DriverPropertiesHelper.format(template, arguments);
        }
        return value;
    }

    public DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException {
        return new DatabaseMetaData(connection);
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
        return (Object[]) DriverPropertiesHelper.getConfigMetaData(driver, mSubProtocol, name , values);
    }

    private List<ApiLevel> getSupportedAPILevels(final XHierarchicalNameAccess driver,
                                                 final String name) {
        List<ApiLevel> services = null;
        Object[] supported = (Object[]) DriverPropertiesHelper.getConfigMetaData(driver, mSubProtocol, name, null);
        if (supported != null) {
            services = new ArrayList<>();
            for (Object service: supported) {
                services.add(ApiLevel.fromString(service.toString()));
            }
        } else {
            services = mSupportedAPILevels;
        }
        System.out.println("DriverProvider.getSupportedAPILevels() Service: " + services.size());
        return services;
    }

    private Boolean getDriverBooleanProperty(final XHierarchicalNameAccess driver,
                                             final String name,
                                             final Boolean dflt) {
        return DriverPropertiesHelper.getConfigBooleanProperty(driver, mSubProtocol, name , dflt);
    }
}