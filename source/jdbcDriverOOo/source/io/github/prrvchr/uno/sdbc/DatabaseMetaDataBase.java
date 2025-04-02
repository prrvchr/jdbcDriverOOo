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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData2;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.AnyConverter;

import io.github.prrvchr.driver.helper.DBException;
import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.PrivilegesHelper;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class DatabaseMetaDataBase
    extends WeakBase
    implements XDatabaseMetaData2 {
    protected final ConnectionBase mConnection;
    protected final java.sql.DatabaseMetaData mMetadata;
    protected final ConnectionLog mLogger;

    // The constructor method:
    public DatabaseMetaDataBase(final ConnectionBase connection)
        throws java.sql.SQLException {
        this(connection, connection.getProvider().getConnection().getMetaData());
    }

    public DatabaseMetaDataBase(final ConnectionBase connection,
                                final java.sql.DatabaseMetaData metadata) {
        mConnection = connection;
        mMetadata = metadata;
        mLogger = new ConnectionLog(connection.getLogger(), LoggerObjectType.METADATA);
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    // com.sun.star.sdbc.XDatabaseMetaData2
    @Override
    public PropertyValue[] getConnectionInfo() {
        return mConnection.getInfo();
    }
    
    @Override
    public String getURL() throws SQLException {
        return mConnection.getUrl();
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
            return mMetadata.deletesAreDetected(type);
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
            return _getResultSet(mMetadata.getBestRowIdentifier(_getPattern(catalog),
                                 _getPattern(schema), table, scope, nullable), "getBestRowIdentifier");
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
            return _getResultSet(mMetadata.getCatalogs(), "getCatalogs");
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
            java.sql.ResultSet resultset = mMetadata.getColumnPrivileges(_getPattern(catalog),
                                                                         _getPattern(schema), table, column);
            return _getResultSet(resultset, "getColumnPrivileges");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getColumns(Object catalog, String schema, String table, String column) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getColumns(_getPattern(catalog),
                                                                _getPattern(schema), table, column);
            return _getResultSet(resultset, "getColumns");
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
            schema1 = _getPattern(schema1);
            schema2 = _getPattern(schema2);
            java.sql.ResultSet resultset = mMetadata.getCrossReference(_getPattern(catalog1),
                                                                       schema1, table1,
                                                                       _getPattern(catalog2),
                                                                       schema2, table2);
            return _getResultSet(resultset, "getCrossReference");
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
            java.sql.ResultSet resultset = mMetadata.getExportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getExportedKeys");
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
            java.sql.ResultSet resultset = mMetadata.getImportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getImportedKeys");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getIndexInfo(Object catalog, String schema, String table, boolean arg3, boolean arg4)
        throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getIndexInfo(_getPattern(catalog),
                                                                  _getPattern(schema), table, arg3, arg4);
            return _getResultSet(resultset, "getIndexInfo");
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
            java.sql.ResultSet resultset = mMetadata.getPrimaryKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getPrimaryKeys");
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
            java.sql.ResultSet resultset = mMetadata.getProcedureColumns(_getPattern(catalog),
                                                                         _getPattern(schema), table, column);
            return _getResultSet(resultset, "getProcedureColumns");
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
            java.sql.ResultSet resultset = mMetadata.getProcedures(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getProcedures");
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
            java.sql.ResultSet result = mMetadata.getSchemas();
            return _getResultSet(result, "getSchemas");
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
            XResultSet resultset = null;
            java.sql.ResultSet result = PrivilegesHelper.getTablePrivilegesResultSet(mConnection.getProvider(),
                                                                                     mMetadata,
                                                                                     _getPattern(catalog),
                                                                                     _getPattern(schema),
                                                                                     table);
            DBTools.printResultSet(result);
            result = PrivilegesHelper.getTablePrivilegesResultSet(mConnection.getProvider(),
                                                                  mMetadata,
                                                                  _getPattern(catalog),
                                                                  _getPattern(schema),
                                                                  table);
            if (result != null) {
                resultset = _getResultSet(result, "getTablePrivileges");
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
            java.sql.ResultSet resultset = mConnection.getProvider().getTableTypesResultSet(mMetadata);
            return _getResultSet(resultset, "getTableTypes");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getTables(Object catalog, String schema, String table, String[] types)
        throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getTables(_getPattern(catalog), _getPattern(schema),
                                                               table, _getPattern(types));
            return _getResultSet(resultset, "getTables");
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
            DBTools.printResultSet(mConnection.getProvider().getTypeInfoResultSet(mMetadata));
            java.sql.ResultSet resultset = mConnection.getProvider().getTypeInfoResultSet(mMetadata);
            return _getResultSet(resultset, "getTypeInfo");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getUDTs(Object catalog, String schema, String type, int[] types) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getUDTs(_getPattern(catalog), _getPattern(schema), type, types);
            return _getResultSet(resultset, "getUDTs");
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getUserName() throws SQLException {
        try {
            String value = mMetadata.getUserName();
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getVersionColumns(Object catalog, String schema, String table) throws SQLException {
        try {
            java.sql.ResultSet resultset = mMetadata.getVersionColumns(_getPattern(catalog),
                                                                       _getPattern(schema), table);
            return _getResultSet(resultset, "getVersionColumns");
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
            return mMetadata.othersDeletesAreVisible(type);
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
            return mMetadata.ownDeletesAreVisible(type);
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
            return mMetadata.supportsIntegrityEnhancementFacility();
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
            return mMetadata.supportsPositionedDelete();
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

    protected XResultSet _getResultSet(java.sql.ResultSet result,
                                       String method)
        throws SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_CREATE_RESULTSET, method);
        if (result == null) {
            System.out.println("sdbc.DatabaseMetaData._getResultSet() ERROR method: " + method);
            String message = mLogger.getStringResource(Resources.STR_LOG_DATABASE_METADATA_CREATE_RESULTSET_ERROR,
                                                       method);
            mLogger.logp(LogLevel.SEVERE, message);
            throw DBException.getSQLException(message, this, StandardSQLState.SQL_GENERAL_ERROR);
        }
        ResultSet resultset = new ResultSet(mConnection, result);
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_CREATED_RESULTSET_ID,
                       method, resultset.getLogger().getObjectId());
        return resultset;
    }

    protected static String _getPattern(Object object) {
        String value = null;
        if (AnyConverter.isString(object)) {
            value = AnyConverter.toString(object);
        }
        return value;
    }

    protected static String _getPattern(String value) {
        String pattern = value;
        if (value.equals("%")) {
            pattern = null;
        }
        return pattern;
    }

    protected static String[] _getPattern(String[] values) {
        String[] types = values;
        for (String value : values) {
            if (value.equals("%")) {
                types = null;
                break;
            }
        }
        return types;
    }

}