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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.sdb.XOfficeDatabaseDocument;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.provider.PropertiesHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;

public abstract class ConfigBase extends ParameterBase {

    // Connection Infos properties
    public static final String SHOW_SYSTEM_TABLE = "ShowSystemTable";
    public static final String CACHED_ROWSET = "CachedRowSet";
    public static final String CONNECTION_URL = "Url";
    public static final String TYPE_INFO_SETTINGS = "TypeInfoSettings";
    public static final String SYSTEM_TABLE_SETTINGS = "SystemTableSettings";
    public static final String TABLE_TYPES_SETTINGS = "TableTypesSettings";
    public static final String TABLE_SETTINGS = "TableSettings";
    public static final String TABLE_PRIVILEGES_SETTINGS = "TablePrivilegesSettings";

    private static final String DOCUMENT = "Document";
    private static final String PRIVILEGES_SETTINGS = "PrivilegesSettings";
    private static final String AUTO_INCREMENT_CREATION = "AutoIncrementCreation";
    private static final String IGNORE_DRIVER_PRIVILEGES = "IgnoreDriverPrivileges";
    private static final String IGNORE_CURRENCY = "IgnoreCurrency";
    private static final String ADD_INDEX_APPENDIX = "AddIndexAppendix";
    private static final String AUTO_RETRIEVING_STATEMENT = "AutoRetrievingStatement";
    private static final String IS_AUTORETRIEVING_ENABLED = "IsAutoRetrievingEnabled";

    private static final String INDEX_PATTERN = "[(]\\s*(\\d+)\\s*[)]";
    private static final String VALUE_PATTERN = "[=]\\s*([\\w+\\s*\\w+]+)";

    protected Object[] mPrivileges = null;
    protected RowSetData mTableData = null;

    private boolean mAddIndexAppendix = false;
    private boolean mIgnoreCurrency = false;
    private boolean mIgnoreDriverPrivileges = false;

    private String mAutoIncrementCreation = "";
    private String mAutoRetrievingStatement = "";

    private String mUrl = null;
    private PropertyValue[] mInfos;
 
    private Boolean mIsAutoRetrievingEnabled = null;
    private Boolean mShowSystemTable = null;
    private Integer mCachedRowSet = null;

    private XOfficeDatabaseDocument mDocument = null;

    private RowSetData mTableTypeData = null;
    private RowSetData mRewriteTableData = null;
    private RowSetData mTypeInfoData = null;
    private RowSetData mSystemTableData = null;
    private RowSetData mTablePrivilegeData = null;

    private Map<String, String> mTableTypeMap = null;

    private String[] mBaseTableTypes = new String[] {"TABLE", "VIEW"};
    private String[] mTableTypes = null;
    private String[] mViewTypes = new String[] {"VIEW"};

    protected ConfigBase(final XHierarchicalNameAccess config,
                         final XHierarchicalNameAccess opts,
                         final PropertyValue[] infos,
                         final String url,
                         final DatabaseMetaData metadata,
                         final String subProtocol,
                         final boolean rewriteTable)
        throws SQLException, java.sql.SQLException {

        // XXX: Driver.xcs default properties
        Object[] typeInfo = null;
        Object[] tableType = null;
        setPropertiesInfo(infos, typeInfo, tableType);
        mInfos = infos;

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
        Object[] systemTable = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                               SYSTEM_TABLE_SETTINGS);
        Object[] tablePrivilege = (Object[]) PropertiesHelper.getConfigProperties(config, subProtocol,
                                                                                  TABLE_PRIVILEGES_SETTINGS);
        setPropertiesData(tableSetting, typeInfo, systemTable, tableType, tablePrivilege);

        setProperties(opts, url, metadata, rewriteTable);
    }

    public String getURL() {
        return mUrl;
    }

    public PropertyValue[] getConnectionInfo() {
        return mInfos;
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

    public boolean useCachedRowSet(ResultSet rs, QueryHelper query)
        throws SQLException {
        System.out.println("SQLBase.getCachedRowSetOption() 1 UseBookmark: " + mCachedRowSet);
        boolean use = true;
        try {
            switch (mCachedRowSet) {
                case 0:
                    use = false;
                    break;
                case 1:
                    use = rs.getType() == ResultSet.TYPE_FORWARD_ONLY ||
                          rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY ||
                          !query.hasPrimaryKeys();
                    break;
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return use;
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

    // XXX: this RowSetData will be used in methods:
    // XXX: - TableHelper.useLiteral()
    // XXX: - DatabaseMetaData.getTypeInfo()
    public RowSetData getTypeInfoData() {
        return mTypeInfoData;
    }

    // XXX: this RowSetData will be used in methods:
    // XXX: - ConnectionSuper.refreshTables()
    // XXX: - DatabaseMetaData.getTables()
    public RowSetData getTableData() {
        return mTableData;
    }

    // XXX: this RowSetData will be used in methods:
    // XXX: - DatabaseMetaData.getTables() in sdbc mode
    public RowSetData getRewriteTableData() {
        RowSetData data = null;
        if (mShowSystemTable) {
            data = mRewriteTableData;
        }
        return data;
    }

    // XXX: this RowSetData will be used in methods:
    // XXX: - DatabaseMetaData.getTableType()
    public RowSetData getTableTypeData() {
        return mTableTypeData;
    }

    // XXX: this RowSetData will be used in methods:
    // XXX: - ConnectionSuper.refreshTables()
    // XXX: - DatabaseMetaData.getTables()
    public RowSetData getSytemTableFilter() {
        RowSetData data = null;
        if (!mShowSystemTable) {
            data = mSystemTableData;
        }
        return data;
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

    // XXX: private methods
    private void setPropertiesData(final Object[] tableSetting,
                                   final Object[] typeInfo,
                                   final Object[] systemTable,
                                   final Object[] tableType,
                                   final Object[] tablePrivilege)
        throws java.sql.SQLException {
        if (tableSetting != null) {
            mTableData = parseRowsetData(tableSetting);
        }

        // XXX: If TypeInfoSetting is not provided in the connection information properties
        // XXX: It will be obtained from the Drivers.xcu configuration file if present...
        if (typeInfo != null) {
            mTypeInfoData = parseRowsetData(typeInfo);
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
            System.out.println("ConfigBase.setPropertiesData() 2");
        }
    }

    private void setProperties(final XHierarchicalNameAccess opts,
                               final String url,
                               final java.sql.DatabaseMetaData metadata,
                               final boolean rewriteTable)
        throws SQLException {
        // XXX: Options.xcs default properties
        try {

            setCachedRowSet(opts);
            setShowSystemTable(opts);
            setPropertiesMetaData(url, metadata, rewriteTable);

        } catch (NoSuchElementException | java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private void setCachedRowSet(final XHierarchicalNameAccess opts)
        throws NoSuchElementException {
        // XXX: If CachedRowSet is not provided in the connection information properties
        // XXX: It will be obtained from the Options.xcu configuration file
        System.out.println("ConfigBase.setCachedRowSet() 1");
        if (mCachedRowSet == null) {
            Integer cachedRowSet = 1;
            if (opts.hasByHierarchicalName(CACHED_ROWSET)) {
                Object obj = opts.getByHierarchicalName(CACHED_ROWSET);
                System.out.println("ConfigBase.setCachedRowSet() 2 obj: " + obj.toString());
                if (obj != null && AnyConverter.isInt(obj)) {
                    cachedRowSet = AnyConverter.toInt(obj);
                }
            }
            mCachedRowSet = cachedRowSet;
        }
    }

    private void setShowSystemTable(final XHierarchicalNameAccess opts)
        throws NoSuchElementException {
        // XXX: If ShowSystemTable is not provided in the connection information properties
        // XXX: It will be obtained from the Options.xcu configuration file
        if (mShowSystemTable == null) {
            Boolean showSystemTable = false;
            if (opts.hasByHierarchicalName(SHOW_SYSTEM_TABLE)) {
                Object obj = opts.getByHierarchicalName(SHOW_SYSTEM_TABLE);
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
        throws java.sql.SQLException {
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
        System.out.println("SQLBase.setProperties() 1 TableType: " + String.join(", ", tableTypes));
    }

    private void setPropertiesInfo(PropertyValue[] infos,
                                   @SuppressWarnings("unused") Object[] typeInfo,
                                   @SuppressWarnings("unused") Object[] tableType)
        throws java.sql.SQLException {
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
                    mAutoIncrementCreation = (String) obj;
                    break;
                case IGNORE_DRIVER_PRIVILEGES:
                    mIgnoreDriverPrivileges = (boolean) obj;
                    break;
                case IGNORE_CURRENCY:
                    mIgnoreCurrency = (boolean) obj;
                    break;
                case ADD_INDEX_APPENDIX:
                    mAddIndexAppendix = (boolean) obj;
                    break;
                case AUTO_RETRIEVING_STATEMENT:
                    mAutoRetrievingStatement = (String) obj;
                    break;
                case IS_AUTORETRIEVING_ENABLED:
                    mIsAutoRetrievingEnabled = (Boolean) obj;
                    break;
                case SHOW_SYSTEM_TABLE:
                    if (obj != null && AnyConverter.isBoolean(obj)) {
                        mShowSystemTable = AnyConverter.toBoolean(obj);
                        System.out.println("ConfigBase.setPropertiesInfo() 1 mShowSystemTable: " + obj.toString());
                    }
                    break;
                case CACHED_ROWSET:
                    if (obj != null && AnyConverter.isInt(obj)) {
                        mCachedRowSet = AnyConverter.toInt(obj);
                        System.out.println("ConfigBase.setPropertiesInfo() 2 mCachedRowSet: " + obj.toString());
                    }
                    break;
                case CONNECTION_URL:
                    if (obj != null && AnyConverter.isString(obj)) {
                        mUrl = AnyConverter.toString(obj);
                        System.out.println("ConfigBase.setPropertiesInfo() 3 mUrl: " + obj.toString());
                    }
                    break;
            }
        }
    }

    private RowSetData parseRowsetData(Object[] data) throws java.sql.SQLException {
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

    private Integer getRowSetDataIndex(Pattern pattern, String element) {
        Integer index = null;
        Matcher matcher = pattern.matcher(element);
        if (matcher.find()) {
            index = Integer.valueOf(matcher.group(1).trim());
        }
        return index;
    }

    private String getRowSetDataValue(Pattern pattern, String element) {
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

    private String[] getMetaDataTableTypes(java.sql.DatabaseMetaData metadata) throws java.sql.SQLException {
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

    private RowSetData getRewriteTableData(String[] tableTypes)
        throws java.sql.SQLException {
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

    private void addRowSetDataValue(Map<Integer, List<String>> keys,
                                    Map<String, List<SimpleImmutableEntry<Integer, String>>> values,
                                    Integer keyIndex, String keyValue,
                                    Integer targetIndex, String targetValue) {
        if (!keys.containsKey(keyIndex)) {
            keys.put(keyIndex, new ArrayList<>());
        }
        if (!keys.get(keyIndex).contains(keyValue)) {
            keys.get(keyIndex).add(keyValue);
        }
        // XXX: row to be deleted have not values
        if (targetIndex != null) {
            if (targetValue == null) {
                System.out.println("SQLBase.addRowSetDataValue() targetValue is null 1 **************************");
            }

            String key = RowSetData.getDataKey(keyIndex, keyValue);
            if (!values.containsKey(key)) {
                values.put(key, new ArrayList<>());
            }
            SimpleImmutableEntry<Integer, String> value = new SimpleImmutableEntry<Integer, String>(targetIndex,
                                                                                                    targetValue);
            if (!values.get(key).contains(value)) {
                values.get(key).add(value);
            }
            if (targetValue == null) {
                System.out.println("SQLBase.addRowSetDataValue() targetValue is null 2 key: " + key);
            }
        }
    }

}