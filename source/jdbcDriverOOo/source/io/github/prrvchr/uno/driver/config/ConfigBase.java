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
package io.github.prrvchr.uno.driver.config;

import java.io.IOException;
import java.io.StringReader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.sdb.XOfficeDatabaseDocument;

import io.github.prrvchr.uno.driver.helper.PropertiesHelper;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.DriverManager;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;

public abstract class ConfigBase extends ParameterBase {

    public static final String ADD_TO_CLASS_PATH = "AddDriverToClassPath";

    // Connection Infos properties
    public static final String SHOW_SYSTEM_TABLE = "ShowSystemTable";
    public static final String CACHED_ROWSET = "CachedRowSet";
    public static final String CONNECTION_URL = "Url";
    public static final String TYPE_INFO_SETTINGS = "TypeInfoSettings";
    public static final String TABLE_TYPES_SETTINGS = "TableTypesSettings";
    public static final String TABLE_SETTINGS = "TableSettings";
    public static final String SYSTEM_CATALOG_SETTINGS = "SystemCatalogSettings";
    public static final String SYSTEM_SCHEMA_SETTINGS = "SystemSchemaSettings";
    public static final String SYSTEM_TABLE_SETTINGS = "SystemTableSettings";
    public static final String TABLE_PRIVILEGES_SETTINGS = "TablePrivilegesSettings";

    private static final String DOCUMENT = "Document";
    private static final String PRIVILEGES_SETTINGS = "PrivilegesSettings";
    private static final String AUTO_INCREMENT_CREATION = "AutoIncrementCreation";
    private static final String IGNORE_DRIVER_PRIVILEGES = "IgnoreDriverPrivileges";
    private static final String IGNORE_CURRENCY = "IgnoreCurrency";
    private static final String ADD_INDEX_APPENDIX = "AddIndexAppendix";
    private static final String AUTO_RETRIEVING_STATEMENT = "AutoRetrievingStatement";
    private static final String IS_AUTORETRIEVING_ENABLED = "IsAutoRetrievingEnabled";
    private static final String USE_CATALOG_IN_SELECT = "UseCatalogInSelect";
    private static final String USE_SCHEMA_IN_SELECT = "UseSchemaInSelect";
    private static final String USE_CATALOG_IN_VIEW = "UseCatalogInView";
    private static final String USE_SCHEMA_IN_VIEW = "UseSchemaInView";
    private static final String SYSTEM_PROPERTIES = "SystemProperties";

    private static final String INDEX_PATTERN = "[(]\\s*(\\d+)\\s*[)]";
    private static final String VALUE_PATTERN = "[=]\\s*([\\w+\\s*\\W*]+)";

    protected final boolean mIsInstrumented;
    protected Object[] mPrivileges = null;
    protected RowSetData mTableData = null;
    protected Short mCachedRowSet = null;

    private Boolean mAddIndexAppendix = null;
    private Boolean mIgnoreCurrency = null;
    private Boolean mIgnoreDriverPrivileges = null;

    private String mAutoIncrementCreation = "";
    private String mAutoRetrievingStatement = "";

    private String mUrl = null;
    private PropertyValue[] mInfos;
 
    private Boolean mIsAutoRetrievingEnabled = null;
    private Boolean mShowSystemTable = null;
    private Boolean mUseCatalogsInSelectDefinitions;
    private Boolean mUseSchemasInSelectDefinitions;
    private Boolean mUseCatalogsInViewDefinitions;
    private Boolean mUseSchemasInViewDefinitions;

    private XOfficeDatabaseDocument mDocument = null;

    private RowSetData mTableTypeData = null;
    private RowSetData mRewriteTableData = null;
    private RowSetData mTypeInfoData = null;
    private RowSetData mSystemTableData = null;
    private RowSetData mSystemSchemaData = null;
    private RowSetData mSystemCatalogData = null;
    private RowSetData mTablePrivilegeData = null;

    private Map<String, String> mTableTypeMap = null;

    private String[] mBaseTableTypes = new String[] {"TABLE", "VIEW"};
    private String[] mTableTypes = null;
    private String[] mViewTypes = new String[] {"VIEW"};

    protected ConfigBase(final XHierarchicalNameAccess config,
                         final XNameAccess opts,
                         final PropertyValue[] infos,
                         final String url,
                         final DatabaseMetaData metadata,
                         final String subProtocol,
                         final boolean rewriteTable)
        throws SQLException {
        mIsInstrumented = DriverManager.isJavaInstrumantationInstalled();

        // XXX: Driver.xcs default properties
        Object[] typeInfo = null;
        Object[] tableType = null;

        setPropertiesInfo(infos, typeInfo, tableType);

        mInfos = infos;

        setPropertiesConfig(config, metadata, subProtocol);

        if (typeInfo == null) {
            typeInfo = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                       TYPE_INFO_SETTINGS);
        }
        if (tableType == null) {
            tableType = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                        TABLE_TYPES_SETTINGS);
        }

        Object[] tableSetting = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                TABLE_SETTINGS);
        Object[] systemCatalog = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                 SYSTEM_CATALOG_SETTINGS);
        Object[] systemSchema = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                SYSTEM_SCHEMA_SETTINGS);
        Object[] systemTable = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                               SYSTEM_TABLE_SETTINGS);
        Object[] tablePrivilege = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                  TABLE_PRIVILEGES_SETTINGS);
        setPropertiesData(typeInfo, tableType, tableSetting,
                          systemCatalog, systemSchema, systemTable, tablePrivilege);

        setProperties(opts, url, metadata, rewriteTable);
        setSystemProperties(config, subProtocol);
    }

    public static final boolean addDriverToClassPath(XNameAccess opts) {
        boolean add = true;
        try {
            if (opts.hasByName(ADD_TO_CLASS_PATH)) {
                Object obj = opts.getByName(ADD_TO_CLASS_PATH);
                if (obj != null && AnyConverter.isBoolean(obj)) {
                    add = AnyConverter.toBoolean(obj);
                }
            }
        } catch (NoSuchElementException | WrappedTargetException e) {
            e.printStackTrace();
        }
        return add;
    }

    public String getURL() {
        return mUrl;
    }

    public PropertyValue[] getConnectionInfo() {
        return mInfos;
    }

    public boolean useCatalogsInSelectDefinitions() {
        return mUseCatalogsInSelectDefinitions;
    }

    public boolean useSchemasInSelectDefinitions() {
        return mUseSchemasInSelectDefinitions;
    }

    public boolean useCatalogsInViewDefinitions() {
        return mUseCatalogsInViewDefinitions;
    }

    public boolean useSchemasInViewDefinitions() {
        return mUseSchemasInViewDefinitions;
    }

    public String getTableType(String type) {
        if (mTableTypeMap != null && mTableTypeMap.containsKey(type)) {
            type = mTableTypeMap.get(type);
        }
        return type;
    }

    public String[] getViewTypes() {
        return mViewTypes;
    }

    public String[] getTableTypes() {
        return getTableTypes(mTableTypes);
    }

    public String[] getTableTypes(String[] tabletypes) {
        for (String value : tabletypes) {
            if (value != null && value.equals("%")) {
                tabletypes = mTableTypes;
                break;
            }
        }
        if (!mShowSystemTable) {
            tabletypes = new String[] {"TABLE", "VIEW"};
        }
        if (tabletypes != null && mTableTypeMap != null) {
            List<String> types = Arrays.asList(tabletypes);
            for (int i = 0; i < types.size(); i++) {
                if (mTableTypeMap.containsKey(types.get(i))) {
                    types.set(i, mTableTypeMap.get(types.get(i)));
                }
            }
            tabletypes = types.toArray(new String[0]);
        }
        return tabletypes;
    }

    public boolean showSystemTable() {
        return mShowSystemTable;
    }

    public boolean isAutoRetrievingEnabled() {
        return mIsAutoRetrievingEnabled;
    }

    public boolean isIgnoreCurrencyEnabled() {
        return mIgnoreCurrency;
    }

    public String getAutoIncrementCreation() {
        return mAutoIncrementCreation;
    }

    public String getAutoRetrievingStatement() {
        return mAutoRetrievingStatement;
    }

    public boolean addIndexAppendix() {
        return mAddIndexAppendix;
    }

    public boolean ignoreDriverPrivileges() {
        return mIgnoreDriverPrivileges;
    }

    private RowSetData getRewriteTableData() {
        RowSetData data = null;
        if (mShowSystemTable) {
            data = mRewriteTableData;
        }
        return data;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getTypeInfo()
    // XXX: - TableHelper.userLiteral()
    public java.sql.ResultSet getMetaDataTypeInfo(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mTypeInfoData);
        }
        return rs;
    }


    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getTableType()
    public java.sql.ResultSet getMetaDataTableTypes(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mTableTypeData);
        }
        return rs;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getCatalogs()
    public java.sql.ResultSet getMetaDataCatalogs(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented && !mShowSystemTable) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mSystemCatalogData);

        }
        return rs;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getSchemas()
    public java.sql.ResultSet getMetaDataSchemas(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented && !mShowSystemTable) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mSystemSchemaData);
        }
        return rs;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - DatabaseMetaData.getTables()
    public java.sql.ResultSet getMetaDataTables(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented && !mShowSystemTable) {
            RowSetData rewrite = getRewriteTableData();
            rs = ResultSetHelper.getCustomDataResultSet(rs, mTableData, mSystemTableData, rewrite);
        }
        return rs;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - ConnectionSuper.getTableNames()
    public java.sql.ResultSet getResultSetTable(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented && !mShowSystemTable) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mTableData, mSystemTableData);
        }
        return rs;
    }

    // XXX: this ResultSet will be used in methods:
    // XXX: - ConnectionSuper.getViewNames()
    public java.sql.ResultSet getResultSetView(java.sql.ResultSet rs)
        throws SQLException {
        if (mIsInstrumented && !mShowSystemTable) {
            rs = ResultSetHelper.getCustomDataResultSet(rs, mSystemTableData);
        }
        return rs;
    }

    // XXX: this RowSetData will be used in methods:
    // XXX: - PrivilegesHelper.getTablePrivilegesResultSet()
    public RowSetData getTablePrivilegeData() {
        return mTablePrivilegeData;
    }

    public boolean hasTableTypesSettings() {
        return mTableTypeMap != null;
    }

    public Map<String, String> getTableTypesSettings() {
        return mTableTypeMap;
    }

    public boolean hasDocument() {
        return mDocument != null;
    }

    public XOfficeDatabaseDocument getDocument() {
        return mDocument;
    }

    public boolean isInstrumented() {
        return mIsInstrumented;
    }

    // XXX: private methods
    private void setPropertiesConfig(final XHierarchicalNameAccess config,
                                     final DatabaseMetaData metadata,
                                     final String subProtocol)
        throws SQLException {

        Boolean useCatalog = Boolean.valueOf(metadata.supportsCatalogsInTableDefinitions());
        Boolean useSchema = Boolean.valueOf(metadata.supportsSchemasInTableDefinitions());

        if (mUseCatalogsInSelectDefinitions == null) {
            mUseCatalogsInSelectDefinitions = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                             USE_CATALOG_IN_SELECT,
                                                                                             useCatalog);
        }

        if (mUseSchemasInSelectDefinitions == null) {
            mUseSchemasInSelectDefinitions = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                            USE_SCHEMA_IN_SELECT,
                                                                                            useSchema);
        }

        if (mUseCatalogsInViewDefinitions == null) {
            mUseCatalogsInViewDefinitions = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                           USE_CATALOG_IN_VIEW,
                                                                                           useCatalog);
        }

        if (mUseSchemasInViewDefinitions == null) {
            mUseSchemasInViewDefinitions = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                          USE_SCHEMA_IN_VIEW,
                                                                                          useSchema);
        }

        if (mIgnoreDriverPrivileges == null) {
            mIgnoreDriverPrivileges = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                     ADD_INDEX_APPENDIX,
                                                                                     Boolean.valueOf(false));
        }

        if (mIgnoreCurrency == null) {
            mIgnoreCurrency = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                             ADD_INDEX_APPENDIX,
                                                                             Boolean.valueOf(true));
        }

        if (mAddIndexAppendix == null) {
            mAddIndexAppendix = (Boolean) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                               ADD_INDEX_APPENDIX,
                                                                               Boolean.valueOf(true));
        }
    }

    private void setPropertiesData(final Object[] typeInfo,
                                   final Object[] tableType,
                                   final Object[] tableSetting,
                                   final Object[] systemCatalog,
                                   final Object[] systemSchema,
                                   final Object[] systemTable,
                                   final Object[] tablePrivilege)
        throws SQLException {
        if (tableSetting != null) {
            mTableData = parseRowsetData(tableSetting);
        }

        // XXX: If TypeInfoSetting is not provided in the connection information properties
        // XXX: It will be obtained from the Drivers.xcu configuration file if present...
        if (typeInfo != null) {
            mTypeInfoData = parseRowsetData(typeInfo);
        }

        if (systemCatalog != null) {
            mSystemCatalogData = parseRowsetData(systemCatalog);
        }

        if (systemSchema != null) {
            mSystemSchemaData = parseRowsetData(systemSchema);
        }

        if (systemTable != null) {
            mSystemTableData = parseRowsetData(systemTable);
        }

        if (tableType != null) {
            mTableTypeData = parseRowsetData(tableType);
            mTableTypeMap = parseTableTypes(tableType);
        }

        if (tablePrivilege != null) {
            mTablePrivilegeData = parseRowsetData(tablePrivilege);
        }
    }

    private void setProperties(final XNameAccess opts,
                               final String url,
                               final java.sql.DatabaseMetaData metadata,
                               final boolean rewriteTable)
        throws SQLException {
        // XXX: Options.xcs default properties
        try {

            setCachedRowSet(opts);
            setShowSystemTable(opts);
            setPropertiesMetaData(url, metadata, rewriteTable);

        } catch (NoSuchElementException | WrappedTargetException e) {
            throw new SQLException(e);
        }
    }

    private void setCachedRowSet(final XNameAccess opts)
        throws NoSuchElementException, WrappedTargetException {
        // XXX: If CachedRowSet is not provided in the connection information properties
        // XXX: It will be obtained from the Options.xcu configuration file
        if (mCachedRowSet == null) {
            short cachedRowSet = 1;
            if (opts.hasByName(CACHED_ROWSET)) {
                Object obj = opts.getByName(CACHED_ROWSET);
                if (obj != null && AnyConverter.isShort(obj)) {
                    cachedRowSet = AnyConverter.toShort(obj);
                }
            }
            mCachedRowSet = cachedRowSet;
        }
    }

    private void setShowSystemTable(final XNameAccess opts)
        throws NoSuchElementException, WrappedTargetException {
        // XXX: If ShowSystemTable is not provided in the connection information properties
        // XXX: It will be obtained from the Options.xcu configuration file
        if (mShowSystemTable == null) {
            Boolean showSystemTable = false;
            if (opts.hasByName(SHOW_SYSTEM_TABLE)) {
                Object obj = opts.getByName(SHOW_SYSTEM_TABLE);
                if (obj != null && AnyConverter.isBoolean(obj)) {
                    showSystemTable = AnyConverter.toBoolean(obj);
                }
            }
            mShowSystemTable = showSystemTable;
        }
    }

    private void setPropertiesMetaData(final String url,
                                       final java.sql.DatabaseMetaData metadata,
                                       final boolean rewriteTable)
        throws SQLException {
        // XXX: If Url not provided in the connection information properties it will be obtained
        // XXX: from the connection and overwrite in the DataBaseMetaData.getURL() method
        if (mUrl == null) {
            mUrl = url;
        }

        // XXX: If IsAutoRetrievingEnabled is not provided in the connection information properties
        // XXX: It will be obtained from the DataBaseMetaData.supportsGetGeneratedKeys() method
        if (mIsAutoRetrievingEnabled == null) {
            mIsAutoRetrievingEnabled = metadata.supportsGetGeneratedKeys();
        }

        String[] tableTypes = getMetaDataTableTypes(metadata);
        if (rewriteTable) {
            mRewriteTableData = getRewriteTableData(tableTypes);
        }
        mTableTypes = tableTypes;
    }

    @SuppressWarnings("unused")
    private void setPropertiesInfo(final PropertyValue[] infos,
                                   Object[] typeInfo,
                                   Object[] tableType)
        throws SQLException {
        Object obj;
        for (PropertyValue info : infos) {

            obj = info.Value;
            switch (info.Name) {
                case DOCUMENT:
                    mDocument = (XOfficeDatabaseDocument) obj;
                    break;
                case TYPE_INFO_SETTINGS:
                    typeInfo = (Object[]) obj;
                    break;
                case TABLE_TYPES_SETTINGS:
                    tableType = (Object[]) obj;
                    break;
                case PRIVILEGES_SETTINGS:
                    mPrivileges = (Object[]) obj;
                    break;
                case AUTO_INCREMENT_CREATION:
                    mAutoIncrementCreation = getStringConfig(obj, mAutoIncrementCreation);
                case IGNORE_DRIVER_PRIVILEGES:
                    mIgnoreDriverPrivileges = getBooleanConfig(obj, mIgnoreDriverPrivileges);
                    break;
                case IGNORE_CURRENCY:
                    mIgnoreCurrency = getBooleanConfig(obj, mIgnoreCurrency);
                    break;
                case ADD_INDEX_APPENDIX:
                    mAddIndexAppendix = getBooleanConfig(obj, mAddIndexAppendix);
                    break;
                case AUTO_RETRIEVING_STATEMENT:
                    mAutoRetrievingStatement = getStringConfig(obj, mAutoRetrievingStatement);
                    break;
                case IS_AUTORETRIEVING_ENABLED:
                    mIsAutoRetrievingEnabled = getBooleanConfig(obj, mIsAutoRetrievingEnabled);
                    break;
                case SHOW_SYSTEM_TABLE:
                    mShowSystemTable = getBooleanConfig(obj, mShowSystemTable);
                    break;
                case CACHED_ROWSET:
                    mCachedRowSet = getShortConfig(obj, mCachedRowSet);
                    break;
                case CONNECTION_URL:
                    mUrl = getStringConfig(obj, mUrl);
                    break;
                case USE_CATALOG_IN_SELECT:
                    mUseCatalogsInSelectDefinitions = getBooleanConfig(obj, mUseCatalogsInSelectDefinitions);
                    break;
                case USE_SCHEMA_IN_SELECT:
                    mUseSchemasInSelectDefinitions = getBooleanConfig(obj, mUseSchemasInSelectDefinitions);
                    break;
                case USE_CATALOG_IN_VIEW:
                    mUseCatalogsInViewDefinitions = getBooleanConfig(obj, mUseCatalogsInViewDefinitions);
                    break;
                case USE_SCHEMA_IN_VIEW:
                    mUseSchemasInViewDefinitions = getBooleanConfig(obj, mUseSchemasInViewDefinitions);
                    break;
            }
        }
    }

    private Short getShortConfig(final Object obj, Short value) {
        if (obj != null && AnyConverter.isShort(obj)) {
            value = AnyConverter.toShort(obj);
        }
        return value;
    }

    private Boolean getBooleanConfig(final Object obj, Boolean value) {
        if (obj != null && AnyConverter.isBoolean(obj)) {
            value = AnyConverter.toBoolean(obj);
        }
        return value;
    }

    private String getStringConfig(final Object obj, String value) {
        if (obj != null && AnyConverter.isString(obj)) {
            value = AnyConverter.toString(obj);
        }
        return value;
    }

    private RowSetData parseRowsetData(final Object[] data) throws SQLException {
        Map<Integer, List<String>> keys = new HashMap<>();
        Map<String, List<SimpleImmutableEntry<Integer, String>>> values = new HashMap<>();
        Pattern idxPattern = Pattern.compile(INDEX_PATTERN);
        Pattern valPattern = Pattern.compile(VALUE_PATTERN);
        int size = DBTools.getEvenLength(data.length);
        for (int i = 0; i < size; i += 2) {
            // XXX populate keys
            String key = (String) data[i];
            Integer keyIndex = getRowSetDataIndex(idxPattern, key);
            String keyValue = getRowSetDataValue(valPattern, key);
            if (keyIndex == null || keyValue == null) {
                continue;
            }

            // XXX populate values
            String value = (String) data[i + 1];
            addRowSetDataValue(keys, values, keyIndex, keyValue,
                               getRowSetDataIndex(idxPattern, value),
                               getRowSetDataValue(valPattern, value));
        }
        return new RowSetData(keys, values);
    }

    private Integer getRowSetDataIndex(final Pattern pattern, final String element) {
        Integer index = null;
        Matcher matcher = pattern.matcher(element);
        if (matcher.find()) {
            index = Integer.valueOf(matcher.group(1).trim());
        }
        return index;
    }

    private String getRowSetDataValue(final Pattern pattern, final String element) {
        String value = null;
        Matcher matcher = pattern.matcher(element);
        if (matcher.find()) {
            value = matcher.group(1).trim();
            if (value.toLowerCase().equals("null")) {
                value = null;
            }
        }
        return value;
    }

    private Map<String, String> parseTableTypes(final Object[] data) {
        Map<String, String> types = null;
        try {
            types = new HashMap<>();
            int count = DBTools.getEvenLength(data.length);
            Pattern pattern = Pattern.compile(VALUE_PATTERN);
            for (int i = 0; i < count; i += 2) {
                String key = getRowSetDataValue(pattern, data[i + 1].toString());
                String value = getRowSetDataValue(pattern, data[i].toString());
                if (key != null && value != null) {
                    types.put (key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    private String[] getMetaDataTableTypes(final java.sql.DatabaseMetaData metadata) throws SQLException {
        List<String> types = new ArrayList<>();
        try (ResultSet rs = metadata.getTableTypes()) {
            int count = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                if (count > 0) {
                    String type = rs.getString(1);
                    if (!rs.wasNull()) {
                        types.add(type);
                    }
                }
            }
        }
        return types.toArray(new String[0]);
    }

    private RowSetData getRewriteTableData(final String[] tableTypes)
        throws SQLException {
        final Integer TABLE_TYPE = 4;
        Map<Integer, List<String>> keys = new HashMap<>();
        Map<String, List<SimpleImmutableEntry<Integer, String>>> values = new HashMap<>();
        List<String> baseTypes = Arrays.asList(mBaseTableTypes);
        for (String type : tableTypes) {
            if (!baseTypes.contains(type)) {
                addRowSetDataValue(keys, values, TABLE_TYPE, type, TABLE_TYPE, "TABLE");
            }
        }
        return new RowSetData(keys, values);
    }

    private void addRowSetDataValue(final Map<Integer, List<String>> keys,
                                    final Map<String, List<SimpleImmutableEntry<Integer, String>>> values,
                                    final Integer keyIndex, String keyValue,
                                    final Integer targetIndex, String targetValue) {
        if (!keys.containsKey(keyIndex)) {
            keys.put(keyIndex, new ArrayList<>());
        }
        if (!keys.get(keyIndex).contains(keyValue)) {
            keys.get(keyIndex).add(keyValue);
        }
        // XXX: row to be deleted have not values
        if (targetIndex != null) {

            String key = RowSetData.getDataKey(keyIndex, keyValue);
            if (!values.containsKey(key)) {
                values.put(key, new ArrayList<>());
            }
            SimpleImmutableEntry<Integer, String> value = new SimpleImmutableEntry<Integer, String>(targetIndex,
                                                                                                    targetValue);
            if (!values.get(key).contains(value)) {
                values.get(key).add(value);
            }
        }
    }
 
    private void setSystemProperties(final XHierarchicalNameAccess config, final String subProtocol) {
        System.out.println("ConfigBase.setSystemProperties() 1");
        Object[] properties = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                              SYSTEM_PROPERTIES);
        if (properties != null) {
            System.out.println("ConfigBase.setSystemProperties() 2");
            StringJoiner buffer = new StringJoiner("\\n");
            for (Object property : properties) {
                buffer.add(property.toString());
            }
            if (buffer.length() > 0) {
                System.out.println("ConfigBase.setSystemProperties() 3 Buffer: " + buffer.toString());
                try {
                    Properties p = new Properties(System.getProperties());
                    p.load(new StringReader(buffer.toString()));
                    // Set the system properties
                    System.setProperties(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("ConfigBase.setSystemProperties() 4");
    }
}