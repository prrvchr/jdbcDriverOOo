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
package io.github.prrvchr.driver.query;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.PropertyValue;
import com.sun.star.sdb.XOfficeDatabaseDocument;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.metadata.TableTypesResultSet;
import io.github.prrvchr.driver.metadata.TypeInfoResultSet;
import io.github.prrvchr.driver.metadata.TypeInfoRows;

public abstract class SQLBase extends ParameterBase {

    // Connection Infos properties
    private static final String DOCUMENT = "Document";
    private static final String TYPE_INFO_SETTINGS = "TypeInfoSettings";
    private static final String TABLE_TYPES_SETTINGS = "TableTypesSettings";
    private static final String TABLE_PRIVILEGES_SETTINGS = "TablePrivilegesSettings";
    private static final String AUTO_INCREMENT_CREATION = "AutoIncrementCreation";
    private static final String IGNORE_DRIVER_PRIVILEGES = "IgnoreDriverPrivileges";
    private static final String IGNORE_CURRENCY = "IgnoreCurrency";
    private static final String ADD_INDEX_APPENDIX = "AddIndexAppendix";
    private static final String AUTO_RETRIEVING_STATEMENT = "AutoRetrievingStatement";
    private static final String IS_AUTORETRIEVING_ENABLED = "IsAutoRetrievingEnabled";
    private static final String SHOW_SYSTEM_TABLE = "ShowSystemTable";
    private static final String USE_BOOKMARK = "UseBookmark";
    private static final String SQL_MODE = "SQLMode";

    protected Object[] mTablePrivileges = null;

    private boolean mShowsystem = false;
    private boolean mUsebookmark;
    private boolean mSqlmode;

    private boolean mAddIndexAppendix = false;
    private boolean mIsAutoRetrievingEnabled = false;
    private boolean mIgnoreCurrency = false;
    private boolean mIgnoreDriverPrivileges = false;

    private String mAutoIncrementCreation = "";
    private String mAutoRetrievingStatement = "";

    private XOfficeDatabaseDocument mDocument = null;
    private TypeInfoRows mTypeInfoRows = null;
    private Map<String, String> mTableTypes = null;

    protected SQLBase(final PropertyValue[] infos,
                      final boolean generatedKeys)
        throws SQLException {
        Boolean autoretrieving = null;
        for (PropertyValue info : infos) {
            switch (info.Name) {
                case DOCUMENT:
                    mDocument = (XOfficeDatabaseDocument) info.Value;
                    break;
                case TYPE_INFO_SETTINGS:
                    mTypeInfoRows = new TypeInfoRows((Object[]) info.Value);
                    break;
                case TABLE_TYPES_SETTINGS:
                    parseTableTypes((Object[]) info.Value);
                    break;
                case TABLE_PRIVILEGES_SETTINGS:
                    mTablePrivileges = (Object[]) info.Value;
                    break;
                case AUTO_INCREMENT_CREATION:
                    mAutoIncrementCreation = (String) info.Value;
                    break;
                case IGNORE_DRIVER_PRIVILEGES:
                    mIgnoreDriverPrivileges = (boolean) info.Value;
                    break;
                case IGNORE_CURRENCY:
                    mIgnoreCurrency = (boolean) info.Value;
                    break;
                case ADD_INDEX_APPENDIX:
                    mAddIndexAppendix = (boolean) info.Value;
                    break;
                case AUTO_RETRIEVING_STATEMENT:
                    mAutoRetrievingStatement = (String) info.Value;
                    break;
                case IS_AUTORETRIEVING_ENABLED:
                    autoretrieving = (boolean) info.Value;
                    break;
                case SHOW_SYSTEM_TABLE:
                    mShowsystem = (boolean) info.Value;
                    break;
                case USE_BOOKMARK:
                    mUsebookmark = (boolean) info.Value;
                    break;
                case SQL_MODE:
                    mSqlmode = (boolean) info.Value;
                    break;
            }
        }
        if (autoretrieving != null) {
            mIsAutoRetrievingEnabled = autoretrieving;
        } else {
            mIsAutoRetrievingEnabled = generatedKeys;
        }

    }

    public boolean useBookmarks(final boolean use) {
        System.out.println("SQLBase.useBookmarks() 1 use: " + use + " - UseBookmark: " + mUsebookmark);
        return use && mUsebookmark;
    }

    public boolean isSQLMode() {
        return mSqlmode;
    }

    public boolean showSystemTable() {
        return mShowsystem;
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

    public java.sql.ResultSet getTypeInfoResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        java.sql.ResultSet result = metadata.getTypeInfo();
        if (mTypeInfoRows != null) {
            result = new TypeInfoResultSet(result, mTypeInfoRows);
        }
        return result;
    }

    public boolean hasTableTypesSettings() {
        return mTableTypes != null;
    }

    public Map<String, String> getTableTypesSettings() {
        return mTableTypes;
    }

    public java.sql.ResultSet getTableTypesResultSet(final java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException {
        java.sql.ResultSet result = metadata.getTableTypes();
        if (hasTableTypesSettings()) {
            result = new TableTypesResultSet(result, mTableTypes);
        }
        return result;
    }

    public boolean hasDocument() {
        return mDocument != null;
    }

    public XOfficeDatabaseDocument getDocument() {
        return mDocument;
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

}