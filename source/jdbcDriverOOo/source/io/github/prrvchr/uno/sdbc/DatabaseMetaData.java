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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData2;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.AnyConverter;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.PrivilegesHelper;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;
import io.github.prrvchr.uno.helper.UnoHelper;


public class DatabaseMetaData
    extends WeakBase
    implements XDatabaseMetaData2 {
    protected final ConnectionBase mConnection;
    protected final java.sql.DatabaseMetaData mMetadata;
    protected final ConnectionLog mLogger;

    // The constructor method:
    public DatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException {
        this(connection, connection.getProvider().getConnection().getMetaData());
    }

    public DatabaseMetaData(final ConnectionBase connection,
                            final java.sql.DatabaseMetaData metadata) {
        mConnection = connection;
        mMetadata = metadata;
        mLogger = new ConnectionLog(connection.getLogger(), LoggerObjectType.METADATA);
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    private Provider getProvider() {
        return mConnection.getProvider();
    }

    private ConfigSQL getConfig() {
        return mConnection.getProvider().getConfigSQL();
    }

    // com.sun.star.sdbc.XDatabaseMetaData2
    @Override
    public PropertyValue[] getConnectionInfo() {
        return getConfig().getConnectionInfo();
    }

    @Override
    public String getURL() throws SQLException {
        return getConfig().getURL();
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        try {
            return mMetadata.allProceduresAreCallable();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        try {
            return mMetadata.allTablesAreSelectable();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        try {
            return mMetadata.dataDefinitionCausesTransactionCommit();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        try {
            return mMetadata.dataDefinitionIgnoredInTransactions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        try {
            boolean detected = mMetadata.deletesAreDetected(type);
            System.out.println("DatabaseMetaData.deletesAreDetected() 1 detected: " + detected);
            return detected;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        try {
            return mMetadata.doesMaxRowSizeIncludeBlobs();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getBestRowIdentifier(Object catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        try {
            return getResultSet(mMetadata.getBestRowIdentifier(getPattern(catalog),
                                getPattern(schema), table, scope, nullable), "getBestRowIdentifier");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        try {
            String value = mMetadata.getCatalogSeparator();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        try {
            String value = mMetadata.getCatalogTerm();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getCatalogs() throws SQLException {
        try {
            RowSetData filter = getConfig().getSytemCatalogFilter();
            java.sql.ResultSet rs = ResultSetHelper.getCustomDataResultSet(mMetadata.getCatalogs(), filter);
            return getResultSet(rs, "getCatalogs");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getColumnPrivileges(Object catalog,
                                          String schema,
                                          String table,
                                          String column) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getColumnPrivileges(getPattern(catalog),
                                                                         getPattern(schema), table, column);
            return getResultSet(resultset, "getColumnPrivileges");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getColumns(Object catalog, String schema, String table, String column) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getColumns(getPattern(catalog),
                                                                getPattern(schema), table, column);
            return getResultSet(resultset, "getColumns");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XConnection getConnection() throws SQLException {
        return mConnection;
    }

    @Override
    public XResultSet getCrossReference(Object catalog1,
                                        String schema1,
                                        String table1,
                                        Object catalog2,
                                        String schema2,
                                        String table2)
            throws SQLException {
        try {
            schema1 = getPattern(schema1);
            schema2 = getPattern(schema2);
            java.sql.ResultSet resultset = mMetadata.getCrossReference(getPattern(catalog1),
                                                                       schema1, table1,
                                                                       getPattern(catalog2),
                                                                       schema2, table2);
            return getResultSet(resultset, "getCrossReference");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        try {
            String value = mMetadata.getDatabaseProductName();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        try {
            String value = mMetadata.getDatabaseProductVersion();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        try {
            return mMetadata.getDefaultTransactionIsolation();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getDriverMajorVersion() {
        return mMetadata.getDriverMajorVersion();
    }

    @Override
    public int getDriverMinorVersion() {
        return mMetadata.getDriverMinorVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        try {
            String value = mMetadata.getDriverName();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getDriverVersion() throws SQLException {
        try {
            String value = mMetadata.getDriverVersion();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_DRIVER_VERSION, value);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getExportedKeys(Object catalog, String schema, String table) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getExportedKeys(getPattern(catalog), getPattern(schema), table);
            return getResultSet(resultset, "getExportedKeys");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        try {
            String value = mMetadata.getExtraNameCharacters();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_EXTRA_NAME_CHARACTERS, value);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        try {
            String value = mMetadata.getIdentifierQuoteString();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_IDENTIFIER_QUOTE, value);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }
    
    @Override
    public XResultSet getImportedKeys(Object catalog, String schema, String table) throws SQLException {
        try {
            System.out.println("DatabaseMetaData.getImportedKeys() 1");
            java.sql.ResultSet rs = mMetadata.getImportedKeys(getPattern(catalog), getPattern(schema), table);
            System.out.println("DatabaseMetaData.getImportedKeys() 2");
            DBTools.printResultSet(rs);
            rs.close();
            java.sql.ResultSet resultset = mMetadata.getImportedKeys(getPattern(catalog), getPattern(schema), table);
            return getResultSet(resultset, "getImportedKeys");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getIndexInfo(Object catalog, String schema, String table, boolean unique, boolean approximate)
        throws SQLException {
        try {
            System.out.println("DatabaseMetaData.getIndexInfo() 1");
            ConfigSQL config = getConfig();
            java.sql.ResultSet resultset = mMetadata.getIndexInfo(config.getMetaDataIdentifier(getPattern(catalog)),
                                                                  config.getMetaDataIdentifier(getPattern(schema)),
                                                                  config.getMetaDataIdentifier(table),
                                                                  unique, approximate);
            return getResultSet(resultset, "getIndexInfo");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        try {
            return mMetadata.getMaxBinaryLiteralLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        try {
            return mMetadata.getMaxCatalogNameLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        try {
            return mMetadata.getMaxCharLiteralLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        try {
            int value = mMetadata.getMaxColumnNameLength();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_MAX_COLUMN_NAME_LENGTH, value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        try {
            return mMetadata.getMaxColumnsInGroupBy();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        try {
            return mMetadata.getMaxColumnsInIndex();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        try {
            return mMetadata.getMaxColumnsInOrderBy();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        try {
            return mMetadata.getMaxColumnsInSelect();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        try {
            return mMetadata.getMaxColumnsInTable();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxConnections() throws SQLException {
        try {
            return mMetadata.getMaxConnections();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        try {
            return mMetadata.getMaxCursorNameLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        try {
            return mMetadata.getMaxIndexLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        try {
            return mMetadata.getMaxProcedureNameLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        try {
            return mMetadata.getMaxRowSize();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        try {
            return mMetadata.getMaxSchemaNameLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        try {
            return mMetadata.getMaxStatementLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxStatements() throws SQLException {
        try {
            return mMetadata.getMaxStatements();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        try {
            int value = mMetadata.getMaxTableNameLength();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_MAX_TABLE_NAME_LENGTH, value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        try {
            return mMetadata.getMaxTablesInSelect();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        try {
            return mMetadata.getMaxUserNameLength();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        try {
            String value = mMetadata.getNumericFunctions();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getPrimaryKeys(Object catalog, String schema, String table) throws SQLException {
        try {
            System.out.println("DatabaseMetaData.getPrimaryKeys() 1");
            java.sql.ResultSet resultset = mMetadata.getPrimaryKeys(getPattern(catalog), getPattern(schema), table);
            return getResultSet(resultset, "getPrimaryKeys");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getProcedureColumns(Object catalog,
                                          String schema,
                                          String table,
                                          String column) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getProcedureColumns(getPattern(catalog),
                                                                         getPattern(schema), table, column);
            return getResultSet(resultset, "getProcedureColumns");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        try {
            String value = mMetadata.getProcedureTerm();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getProcedures(Object catalog, String schema, String table) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getProcedures(getPattern(catalog), getPattern(schema), table);
            return getResultSet(resultset, "getProcedures");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        try {
            String value = mMetadata.getSQLKeywords();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        try {
            String value = mMetadata.getSchemaTerm();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getSchemas() throws SQLException {
        try {
            RowSetData filter = getConfig().getSytemSchemaFilter();
            java.sql.ResultSet rs = ResultSetHelper.getCustomDataResultSet(mMetadata.getSchemas(), filter);
            return getResultSet(rs, "getSchemas");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        try {
            String value = mMetadata.getSearchStringEscape();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getStringFunctions() throws SQLException {
        try {
            String value = mMetadata.getStringFunctions();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        try {
            String value = mMetadata.getSystemFunctions();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getTablePrivileges(Object catalog, String schema, String table)
        throws SQLException {
        try {
            System.out.println("DatabaseMetaData.getTablePrivileges() **********************************");
            ConfigDCL config = getProvider().getConfigDCL();
            java.sql.ResultSet rs = PrivilegesHelper.getTablePrivilegesResultSet(config,
                                                                                 mMetadata,
                                                                                 getPattern(catalog),
                                                                                 getPattern(schema),
                                                                                 table);
            DBTools.printResultSet(rs);
            rs.close();
            XResultSet resultset = null;
            rs = PrivilegesHelper.getTablePrivilegesResultSet(config,
                                                              mMetadata,
                                                              getPattern(catalog),
                                                              getPattern(schema),
                                                              table);
            if (rs != null) {
                resultset = getResultSet(rs, "getTablePrivileges");
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getTableTypes()
        throws SQLException {
        try {
            RowSetData data = getConfig().getTableTypeData();
            java.sql.ResultSet resultset = ResultSetHelper.getCustomDataResultSet(mMetadata.getTableTypes(), data);
            return getResultSet(resultset, "getTableTypes");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getTables(Object catalog, String schema, String table, String[] types)
        throws SQLException {
        try {
            types = getConfig().getTableTypes(types);
            RowSetData data = getConfig().getTableData();
            RowSetData filter = getConfig().getSytemTableFilter();
            RowSetData rewrite = getConfig().getRewriteTableData();
            java.sql.ResultSet rs = ResultSetHelper.getCustomDataResultSet(mMetadata.getTables(getPattern(catalog),
                                                                                               getPattern(schema),
                                                                                               table,
                                                                                               getPattern(types)),
                                                                           data, filter, rewrite);
            return getResultSet(rs, "getTables");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        try {
            String value = mMetadata.getTimeDateFunctions();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getTypeInfo() throws SQLException {
        try {
            //java.sql.ResultSet result = mMetadata.getTypeInfo();
            //DBTools.printResultSet(result);
            //result.close();
            RowSetData data = getConfig().getTypeInfoData();
            java.sql.ResultSet rs = ResultSetHelper.getCustomDataResultSet(mMetadata.getTypeInfo(), data);
            return getResultSet(rs, "getTypeInfo");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getUDTs(Object catalog, String schema, String type, int[] types) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getUDTs(getPattern(catalog), getPattern(schema), type, types);
            return getResultSet(resultset, "getUDTs");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getUserName() throws SQLException {
        try {
            String user = mMetadata.getUserName();
            if (user == null) {
                user = "";
            }
            return user;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getVersionColumns(Object catalog, String schema, String table) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getVersionColumns(getPattern(catalog),
                                                                       getPattern(schema), table);
            return getResultSet(resultset, "getVersionColumns");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        try {
            return mMetadata.insertsAreDetected(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        try {
            return mMetadata.isCatalogAtStart();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        try {
            return mMetadata.isReadOnly();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        try {
            return mMetadata.nullPlusNonNullIsNull();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        try {
            return mMetadata.nullsAreSortedAtEnd();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        try {
            return mMetadata.nullsAreSortedAtStart();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        try {
            return mMetadata.nullsAreSortedHigh();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        try {
            return mMetadata.nullsAreSortedLow();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        try {
            boolean visible = mMetadata.othersDeletesAreVisible(type);
            System.out.println("DatabaseMetaData.othersDeletesAreVisible() 1 visible: " + visible);
            return visible;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        try {
            return mMetadata.othersInsertsAreVisible(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        try {
            return mMetadata.othersUpdatesAreVisible(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        try {
            boolean visible = mMetadata.ownDeletesAreVisible(type);
            System.out.println("DatabaseMetaData.ownDeletesAreVisible() 1 visible: " + visible);
            return visible;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        try {
            return mMetadata.ownInsertsAreVisible(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        try {
            return mMetadata.ownUpdatesAreVisible(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        try {
            return mMetadata.storesLowerCaseIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        try {
            return mMetadata.storesLowerCaseQuotedIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        try {
            return mMetadata.storesMixedCaseIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        try {
            return mMetadata.storesMixedCaseQuotedIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        try {
            return mMetadata.storesUpperCaseIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        try {
            return mMetadata.storesUpperCaseQuotedIdentifiers();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        try {
            return mMetadata.supportsANSI92EntryLevelSQL();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        try {
            return mMetadata.supportsANSI92FullSQL();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        try {
            return mMetadata.supportsANSI92IntermediateSQL();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        try {
            boolean value = mMetadata.supportsAlterTableWithAddColumn();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_SUPPORT_ALTER_TABLE_WITH_ADD_COLUMN,
                           value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        try {
            boolean value = mMetadata.supportsAlterTableWithDropColumn();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_SUPPORT_ALTER_TABLE_WITH_DROP_COLUMN,
                           value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        try {
            return mMetadata.supportsBatchUpdates();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        try {
            return mMetadata.supportsCatalogsInDataManipulation();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        try {
            return mMetadata.supportsCatalogsInIndexDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        try {
            return mMetadata.supportsCatalogsInPrivilegeDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        try {
            return mMetadata.supportsCatalogsInProcedureCalls();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        try {
            return mMetadata.supportsCatalogsInTableDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        try {
            return mMetadata.supportsColumnAliasing();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsConvert(int arg0, int arg1) throws SQLException {
        try {
            return mMetadata.supportsConvert();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        try {
            return mMetadata.supportsCoreSQLGrammar();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        try {
            return mMetadata.supportsCorrelatedSubqueries();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        try {
            return mMetadata.supportsDataDefinitionAndDataManipulationTransactions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        try {
            return mMetadata.supportsDataManipulationTransactionsOnly();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        try {
            return mMetadata.supportsDifferentTableCorrelationNames();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        try {
            return mMetadata.supportsExpressionsInOrderBy();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        try {
            return mMetadata.supportsExtendedSQLGrammar();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        try {
            return mMetadata.supportsFullOuterJoins();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        try {
            return mMetadata.supportsGroupBy();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        try {
            return mMetadata.supportsGroupByBeyondSelect();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        try {
            return mMetadata.supportsGroupByUnrelated();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        try {
            boolean support = mMetadata.supportsIntegrityEnhancementFacility();
            System.out.println("DatabaseMetaData.supportsIntegrityEnhancementFacility() 1 support: " + support);
            return support;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        try {
            return mMetadata.supportsLikeEscapeClause();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        try {
            return mMetadata.supportsLimitedOuterJoins();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        boolean support = false;
        try {
            support = mMetadata.supportsMinimumSQLGrammar();
        } catch (java.sql.SQLException e) {
            // FIXME: SDBC bug. Must be able to throw exception.
            // FIXME: throw UnoHelper.getSQLException(e, this);
        }
        return support;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        try {
            boolean value = mMetadata.supportsMixedCaseIdentifiers();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_SUPPORT_MIXED_CASE_ID, value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        try {
            boolean value = mMetadata.supportsMixedCaseQuotedIdentifiers();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_SUPPORT_MIXED_CASE_QUOTED_ID, value);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        try {
            return mMetadata.supportsMultipleResultSets();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        try {
            return mMetadata.supportsMultipleTransactions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        try {
            return mMetadata.supportsNonNullableColumns();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        try {
            return mMetadata.supportsOpenCursorsAcrossCommit();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        try {
            return mMetadata.supportsOpenCursorsAcrossRollback();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        try {
            return mMetadata.supportsOpenStatementsAcrossCommit();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        try {
            return mMetadata.supportsOpenStatementsAcrossRollback();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        try {
            return mMetadata.supportsOrderByUnrelated();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        try {
            return mMetadata.supportsOuterJoins();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        try {
            boolean support = mMetadata.supportsPositionedDelete();
            System.out.println("DatabaseMetaData.supportsPositionedDelete() 1 support: " + support);
            return support;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        try {
            return mMetadata.supportsPositionedUpdate();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        try {
            return mMetadata.supportsResultSetConcurrency (type, concurrency);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsResultSetType(int arg0) throws SQLException {
        try {
            return mMetadata.supportsResultSetType(arg0);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        try {
            return mMetadata.supportsSchemasInDataManipulation();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        try {
            return mMetadata.supportsSchemasInIndexDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        try {
            return mMetadata.supportsSchemasInPrivilegeDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        try {
            return mMetadata.supportsSchemasInProcedureCalls();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        try {
            return mMetadata.supportsSchemasInTableDefinitions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        try {
            return mMetadata.supportsSelectForUpdate();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        try {
            return mMetadata.supportsStoredProcedures();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        try {
            return mMetadata.supportsSubqueriesInComparisons();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        try {
            return mMetadata.supportsSubqueriesInExists();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        try {
            return mMetadata.supportsSubqueriesInIns();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        try {
            return mMetadata.supportsSubqueriesInQuantifieds();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        try {
            return mMetadata.supportsTableCorrelationNames();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException {
        try {
            return mMetadata.supportsTransactionIsolationLevel(arg0);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        try {
            return mMetadata.supportsTransactions();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsTypeConversion() throws SQLException {
        try {
            return mMetadata.supportsConvert();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        try {
            return mMetadata.supportsUnion();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        try {
            return mMetadata.supportsUnionAll();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        try {
            return mMetadata.updatesAreDetected(type);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        try {
            return mMetadata.usesLocalFilePerTable();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        try {
            return mMetadata.usesLocalFiles();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected XResultSet getResultSet(java.sql.ResultSet result,
                                      String method)
        throws SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_CREATE_RESULTSET, method);
        if (result == null) {
            System.out.println("sdbc.DatabaseMetaData._getResultSet() ERROR method: " + method);
            String msg = mLogger.getStringResource(Resources.STR_LOG_DATABASE_METADATA_CREATE_RESULTSET_ERROR,
                                                       method);
            mLogger.logp(LogLevel.SEVERE, msg);
            java.sql.SQLException e = new java.sql.SQLException(msg, StandardSQLState.SQL_GENERAL_ERROR.text());
            throw UnoHelper.getSQLException(e, this);
        }
        ResultSet resultset = new ResultSet(mConnection, result, null, method);
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_CREATED_RESULTSET_ID,
                       method, resultset.getLogger().getObjectId());
        mConnection.addResultSet(method);
        return resultset;
    }

    protected static String getPattern(Object object) {
        String value = null;
        if (AnyConverter.isString(object)) {
            value = AnyConverter.toString(object);
        }
        return value;
    }

    protected static String getPattern(String value) {
        if (value != null && value.equals("%")) {
            value = null;
        }
        return value;
    }

    protected static String[] getPattern(String[] values) {
        if (values != null) {
            List<String> types = new ArrayList<>(Arrays.asList(values));
            ListIterator<String> iter = types.listIterator();
            while (iter.hasNext()) {
                String value = iter.next();
                if (value == null) {
                    iter.remove();
                }
            }
            if (types.isEmpty()) {
                values = null;
            } else {
                values = types.toArray(new String[0]);
            }
        }
        return values;
    }

}