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
package io.github.prrvchr.driver.provider;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.driver.helper.DBException;
import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.GeneratedKeys;
import io.github.prrvchr.driver.metadata.TableTypesResultSet;
import io.github.prrvchr.driver.metadata.TypeInfoResultSet;
import io.github.prrvchr.driver.metadata.TypeInfoRows;
import io.github.prrvchr.driver.query.DCLQuery;
import io.github.prrvchr.driver.query.DDLQuery;
import io.github.prrvchr.driver.query.SQLQuery;
import io.github.prrvchr.driver.rowset.BaseRow;
import io.github.prrvchr.driver.rowset.RowColumn;
import io.github.prrvchr.driver.rowset.RowHelper;
import io.github.prrvchr.driver.rowset.RowTable;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;

public class DriverProvider {

    static final String LEVEL_OFF = "OFF";

    protected ConnectionLog mLogger;

    private SQLQuery mSQLConfig;

    private final boolean mWarnings = true;
    private String mSubProtocol;
    private boolean mShowsystem;
    private boolean mUsebookmark;
    private boolean mSqlmode;
    private PropertyValue[] mInfos;
    private java.sql.Statement mStatement = null;
    private XOfficeDatabaseDocument mDocument = null;

    private Boolean mIgnoreDriverPrivileges = null;

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
    private String mSuffix = "";
    private String mCatalogSeparator = "";
    private String mIdentifierQuoteString = "";
    private String mAutoIncrementCreation = "";
    private boolean mAddIndexAppendix = false;
    private boolean mIsAutoRetrievingEnabled = false;
    private String mAutoRetrievingStatement = "";
    private boolean mIgnoreCurrency = false;

    private Object[] mTypeInfoSettings = null;
    private List<String> mPrivilegeNames = null;
    private List<Integer> mPrivilegeValues = null;
    private TypeInfoRows mTypeInfoRows = null;
    private Map<String, String> mTableTypes = null;
    private List<ApiLevel> mSupportedAPILevels = List.of(ApiLevel.COM_SUN_STAR_SDBC,
                                                         ApiLevel.COM_SUN_STAR_SDBCX,
                                                         ApiLevel.COM_SUN_STAR_SDB);

    // The constructor method:
    public DriverProvider(final XComponentContext context,
                          final XInterface source,
                          final ResourceBasedEventLogger logger,
                          final XHierarchicalNameAccess driver,
                          final XHierarchicalNameAccess config,
                          final String url,
                          final PropertyValue[] infos,
                          ApiLevel level)
        throws SQLException {
        System.out.println("jdbcdriver.DriverProvider() 1");
        String location = url.replaceFirst(DriverPropertiesHelper.REGISTRED_PROTOCOL,
                                           DriverPropertiesHelper.CONNECT_PROTOCOL);
        DriverManagerHelper.isBootloaderOk();
        try {

            if (!DriverManagerHelper.isDriverRegistered(location)) {
                System.out.println("jdbcdriver.DriverProvider() 2");
                DriverManagerHelper.registerDriver(context, source, driver, logger, url, infos);
            }
            mSubProtocol = url.split(":")[1];
            // XXX: SQLCommandSuffix is needed for building query from sql command.
            mSuffix = DriverPropertiesHelper.getConfigStringProperty(driver, mSubProtocol, "SQLCommandSuffix", mSuffix);
            mLogger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);

            mInfos = infos;

            setDriverProperties(driver);

            mShowsystem = UnoHelper.getConfigurationOption(config, "ShowSystemTable", false);
            mUsebookmark = UnoHelper.getConfigurationOption(config, "UseBookmark", true);
            mSqlmode = UnoHelper.getConfigurationOption(config, "SQLMode", false);
            System.out.println("jdbcdriver.DriverProvider() 3");

            setSystemProperties(logger, driver, infos);
            String newUrl = getConnectionUrl(logger, driver, infos, location);
            System.out.println("jdbcdriver.DriverProvider() 4 new Url: " + newUrl);

            java.sql.Connection connection = DriverManager.getConnection(newUrl, getJdbcConnectionProperties(infos));
            System.out.println("jdbcdriver.DriverProvider() 5");

            java.sql.DatabaseMetaData metadata = connection.getMetaData();
            setConnectionMetaData(driver, metadata);
            Object[] privileges = setInfoProperties(infos, metadata);
            setPrivileges(privileges);

            switch (level.service()) {
                case "com.sun.star.sdb":
                    mSQLConfig = new DCLQuery(driver, mSubProtocol, mIdentifierQuoteString, privileges);
                    break;
                case "com.sun.star.sdbcx":
                    mSQLConfig = new DDLQuery(driver, mSubProtocol, mIdentifierQuoteString);
                    break;
                case "com.sun.star.sdbc":
                    mSQLConfig = new SQLQuery(driver, mSubProtocol, mIdentifierQuoteString);
                    break;
            }
            if (mIgnoreDriverPrivileges == null) {
                mIgnoreDriverPrivileges = SQLQuery.ignoreDriverPrivileges(driver, mSubProtocol);
            }
            // XXX: We do not keep the connection but the statement
            // XXX: which allows us to find the connection if necessary.
            mStatement = connection.createStatement();
            System.out.println("jdbcdriver.DriverProvider() 6 **********************************************");
        } catch (java.sql.SQLException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("jdbcdriver.DriverProvider() ERROR: "  + sw.toString());
            int resource = Resources.STR_LOG_NO_SYSTEM_CONNECTION;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("jdbcdriver.DriverProvider() ERROR: "  + sw.toString());
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
        mIdentifierQuoteString = DriverPropertiesHelper.getConfigStringProperty(driver, mSubProtocol,
                                                                                "IdentifierQuoteString",
                                                                                metadata.getIdentifierQuoteString());
        System.out.println("DriverProvider() 1 IdentifierQuoteString: '" + mIdentifierQuoteString + "'");

        
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

    public boolean useBookmark() {
        return mUsebookmark;
    }

    public boolean useSQLMode(final java.sql.ResultSet result)
        throws java.sql.SQLException {
        return mSqlmode || !isResultSetUpdatable(result);
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
        String command = getAutoRetrievingStatement();
        if (!isAutoRetrievingEnabled() || command == null) {
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

    public boolean hasDocument() {
        return mDocument != null;
    }

    public boolean supportService(final ApiLevel service) {
        return mSupportedAPILevels.contains(service);
    }

    public XOfficeDatabaseDocument getDocument() {
        return mDocument;
    }

    public String getAutoIncrementCreation() {
        return mAutoIncrementCreation;
    }

    public boolean isIgnoreCurrencyEnabled() {
        return mIgnoreCurrency;
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
        if (!mShowsystem) {
            List<String> types = new ArrayList<>(List.of("TABLE", "VIEW"));
            if (hasTableTypesSettings()) {
                setTableTypes(types);
            }
            tabletypes = types.toArray(new String[0]);
        }
        return tabletypes;
    }

    private void setTableTypes(List<String> types) {
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            if (mTableTypes.containsValue(type)) {
                for (Entry<String, String> entry : mTableTypes.entrySet()) {
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
        if (hasTableTypesSettings() && mTableTypes.containsKey(type)) {
            tabletype = mTableTypes.get(type);
        }
        return tabletype;
    }

    public String[] getPrivileges() {
        return mPrivilegeNames.toArray(new String[0]);
    }

    public int getPrivileges(final List<String> privileges) {
        int flags = 0;
        for (String privilege : privileges) {
            flags |= getPrivilege(privilege);
        }
        return flags;
    }

    public boolean hasPrivilege(final String privilege) {
        return mPrivilegeNames.contains(privilege);
    }

    public int getPrivilege(final String privilege) {
        int flag = 0;
        if (mPrivilegeNames.contains(privilege)) {
            flag = mPrivilegeValues.get(mPrivilegeNames.indexOf(privilege));
        }
        return flag;
    }

    public int getMockPrivileges() {
        int privileges = 0;
        for (Integer value : mPrivilegeValues) {
            privileges += value;
        }
        return privileges;
    }

    public String[] getPrivileges(final int privilege) {
        List<String> flags = new ArrayList<>();
        for (int value: mPrivilegeValues) {
            if ((privilege & value) == value) {
                flags.add(mPrivilegeNames.get(mPrivilegeValues.indexOf(value)));
            }
        }
        return flags.toArray(new String[0]);
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
    public boolean useBookmarks(final boolean use) {
        System.out.println("DriverProvider.useBookmarks() 1 use: " + use + " - UseBookmark: " + mUsebookmark);
        return use && mUsebookmark;
    }
    public String getCatalogSeparator() {
        return mCatalogSeparator;
    }
    public String getIdentifierQuoteString() {
        return mIdentifierQuoteString;
    }

    // connection infos cache data
    public boolean isAutoRetrievingEnabled() {
        return mIsAutoRetrievingEnabled;
    }

    public String getAutoRetrievingStatement() {
        return mAutoRetrievingStatement;
    }

    public boolean ignoreDriverPrivileges() {
        boolean ignorePrivileges = true;
        if (mIgnoreDriverPrivileges != null) {
            ignorePrivileges = mIgnoreDriverPrivileges;
        } else {
            ignorePrivileges = mSQLConfig.ignoreDriverPrivileges();
        }
        return ignorePrivileges;
    }

    public boolean addIndexAppendix() {
        return mAddIndexAppendix;
    }

    public boolean hasTableTypesSettings() {
        return mTableTypes != null;
    }

    public java.sql.ResultSet getTableTypesResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        java.sql.ResultSet result = metadata.getTableTypes();
        if (hasTableTypesSettings()) {
            result = new TableTypesResultSet(result, mTableTypes);
        }
        return result;
    }

    public java.sql.ResultSet getTypeInfoResultSet()
        throws java.sql.SQLException {
        return getTypeInfoResultSet(getConnection().getMetaData());
    }

    public java.sql.ResultSet getTypeInfoResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        java.sql.ResultSet result = metadata.getTypeInfo();
        if (mTypeInfoSettings != null) {
            if (mTypeInfoRows == null) {
                mTypeInfoRows = new TypeInfoRows(mTypeInfoSettings);
            }
            result = new TypeInfoResultSet(result, mTypeInfoRows);
        }
        return result;
    }

    public int getGeneratedKeysOption() {
        int keyOption;
        if (isAutoRetrievingEnabled()) {
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

    private String getConnectionUrl(final ResourceBasedEventLogger logger,
                                    final XHierarchicalNameAccess driver,
                                    final PropertyValue[] infos,
                                    final String url) {
        String newUrl = new String(url);
        String suffix = getUrlSuffix(driver, infos);
        if (suffix != null) {
            newUrl += suffix;
            logger.logprb(LogLevel.INFO, Resources.STR_LOG_DRIVER_CONNECT_WITH_URL, newUrl);
        }
        return newUrl;
    }

    public Properties getJdbcConnectionProperties(final PropertyValue[] infos) {
        Properties properties = new Properties();
        for (PropertyValue info : infos) {
            String property = info.Name;
            if (isLibreOfficeProperty(property) || isInternalProperty(property)) {
                continue;
            }
            System.out.println("DriverProvider.getJdbcConnectionProperties() ********************* Name: " + property);
            properties.setProperty(property, String.format("%s", info.Value));
            String msg = "DriverProvider.getJdbcConnectionProperties() ********************* Value: ";
            System.out.println(msg + info.Value);
        }
        return properties;
    }

    private boolean isLibreOfficeProperty(final String property) {
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

    private boolean isInternalProperty(final String property) {
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

    private String getUrlSuffix(final XHierarchicalNameAccess config,
                                final PropertyValue[] infos) {
        String value = (String) DriverPropertiesHelper.getConfigMetaData(config, infos, mSubProtocol,
                                                                         "UrlSuffix", null);
        if (value != null) {
            value = getFormatedPropertyValue(config, infos, value);
        }
        return value;
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

    private Object[] setInfoProperties(final PropertyValue[] infos,
                                       final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        Object[] privileges = null;
        boolean autoretrieving = getAutoRetrieving(metadata, infos);
        for (PropertyValue info : infos) {
            switch (info.Name) {
                case "Document":
                    mDocument = (XOfficeDatabaseDocument) info.Value;
                    break;
                case "TypeInfoSettings":
                    mTypeInfoSettings = (Object[]) info.Value;
                    break;
                case "TableTypesSettings":
                    parseTableTypes((Object[]) info.Value);
                    break;
                case "TablePrivilegesSettings":
                    privileges = (Object[]) info.Value;
                    break;
                case "AutoIncrementCreation":
                    mAutoIncrementCreation = (String) info.Value;
                    break;
                case "IgnoreDriverPrivileges":
                    mIgnoreDriverPrivileges = (Boolean) info.Value;
                    break;
                case "IgnoreCurrency":
                    mIgnoreCurrency = (boolean) info.Value;
                    break;
                case "AddIndexAppendix":
                    mAddIndexAppendix = (boolean) info.Value;
                    break;
                case "AutoRetrievingStatement":
                    if (autoretrieving) {
                        mAutoRetrievingStatement = (String) info.Value;
                    }
                    break;
                case "IsAutoRetrievingEnabled":
                    if (autoretrieving) {
                        mIsAutoRetrievingEnabled = (boolean) info.Value;
                    }
                    break;
                case "ShowSystemTable":
                    mShowsystem = (boolean) info.Value;
                    break;
                case "UseBookmark":
                    mUsebookmark = (boolean) info.Value;
                    break;
                case "SQLMode":
                    mSqlmode = (boolean) info.Value;
                    break;
            }
        }
        return privileges;
    }
    private void setPrivileges(final Object[] privileges) {
        boolean parsed = false;
        if (privileges != null) {
            parsed = parsePrivileges(privileges);
        }
        if (!parsed) {
            setDefaultPrivileges();
        }
    }

    private void parseTableTypes(final Object[] infos) {
        Map<String, String> types = null;
        try {
            types = new TreeMap<>();
            int count = DBTools.getEvenLength(infos.length);
            for (int i = 0; i < count; i += 2) {
                types.put (infos[i].toString(), infos[i + 1].toString());
            }
            mTableTypes = types;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean parsePrivileges(final Object[] infos) {
        boolean parsed = false;
        try {
            mPrivilegeNames = new ArrayList<>();
            mPrivilegeValues  = new ArrayList<>();
            int count = DBTools.getEvenLength(infos.length);
            for (int i = 0; i < count; i += 2) {
                mPrivilegeNames.add(infos[i].toString());
                mPrivilegeValues.add(Integer.parseInt(infos[i + 1].toString()));
            }
            parsed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsed;
    }

    private void setDefaultPrivileges() {
        mPrivilegeNames = List.of("SELECT",
                                   "INSERT",
                                   "UPDATE",
                                   "DELETE",
                                   "READ",
                                   "CREATE",
                                   "ALTER",
                                   "REFERENCES",
                                   "DROP");
        mPrivilegeValues = List.of(Privilege.SELECT,
                                    Privilege.INSERT,
                                    Privilege.UPDATE,
                                    Privilege.DELETE,
                                    Privilege.READ,
                                    Privilege.CREATE,
                                    Privilege.ALTER,
                                    Privilege.REFERENCE,
                                    Privilege.DROP);
    }

    private boolean getAutoRetrieving(final java.sql.DatabaseMetaData metadata,
                                      final PropertyValue[] infos)
        throws java.sql.SQLException {
        Boolean support = false;
        support = (Boolean) DriverPropertiesHelper.getInfosProperty(infos, "IsAutoRetrievingEnabled", null);
        // FIXME: If IsAutoRetrievingEnabled is not set, we retrieve the option from the underlying metadata driver.
        // FIXME: This allows you to correct possible failures of certain drivers (ie: like for Derby)
        System.out.println("DriverProvider.getAutoRetrieving() 1 support: " + support);
        if (support == null) {
            support = metadata.supportsGetGeneratedKeys();
        }
        System.out.println("DriverProvider.getAutoRetrieving() 2 support: " + metadata.supportsGetGeneratedKeys());
        System.out.println("DriverProvider.getAutoRetrieving() 3 support: " + support);
        return support;
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

    public String getSQLQuery(String command) {
        if (!mSuffix.isBlank()) {
            command += mSuffix;
        }
        return command;
    }

    private Boolean getDriverBooleanProperty(final XHierarchicalNameAccess driver,
                                             final String name,
                                             final Boolean dflt) {
        return DriverPropertiesHelper.getConfigBooleanProperty(driver, mSubProtocol, name , dflt);
    }
}