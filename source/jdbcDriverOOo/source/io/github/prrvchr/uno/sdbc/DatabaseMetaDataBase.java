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

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBPrivilegesHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class DatabaseMetaDataBase
    extends WeakBase
    implements XDatabaseMetaData2
{
    protected final ConnectionBase m_Connection;
    protected final java.sql.DatabaseMetaData m_Metadata;
    protected final ConnectionLog m_logger;

    // The constructor method:
    public DatabaseMetaDataBase(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        this(connection, connection.getProvider().getConnection().getMetaData());
    }

    public DatabaseMetaDataBase(final ConnectionBase connection,
                                final java.sql.DatabaseMetaData metadata)
    {
        m_Connection = connection;
        m_Metadata = metadata;
        m_logger = new ConnectionLog(connection.getLogger(), LoggerObjectType.METADATA);
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    // com.sun.star.sdbc.XDatabaseMetaData2
    @Override
    public PropertyValue[] getConnectionInfo()
    {
        return m_Connection.getInfo();
    }
    
    @Override
    public String getURL() throws SQLException
    {
        return m_Connection.getUrl();
    }
    
    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        try {
            return m_Metadata.allProceduresAreCallable();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        try {
            return m_Metadata.allTablesAreSelectable();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        try {
            return m_Metadata.dataDefinitionCausesTransactionCommit();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        try {
            return m_Metadata.dataDefinitionIgnoredInTransactions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        try {
            return m_Metadata.deletesAreDetected(type);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        try {
            return m_Metadata.doesMaxRowSizeIncludeBlobs();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public XResultSet getBestRowIdentifier(Object catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException
    {
        try {
            return _getResultSet(m_Metadata.getBestRowIdentifier(_getPattern(catalog), _getPattern(schema), table, scope, nullable), "getBestRowIdentifier");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getCatalogSeparator() throws SQLException
    {
        try {
            String value = m_Metadata.getCatalogSeparator();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getCatalogTerm() throws SQLException
    {
        try {
            String value = m_Metadata.getCatalogTerm();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getCatalogs() throws SQLException
    {
        try {
            return _getResultSet(m_Metadata.getCatalogs(), "getCatalogs");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getColumnPrivileges(Object catalog, String schema, String table, String column) throws SQLException
    {
        try {
            java.sql.ResultSet resultset = m_Metadata.getColumnPrivileges(_getPattern(catalog), _getPattern(schema), table, column);
            return _getResultSet(resultset, "getColumnPrivileges");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace()) {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getColumns(Object catalog, String schema, String table, String column) throws SQLException
    {
        try {
            java.sql.ResultSet resultset = m_Metadata.getColumns(_getPattern(catalog), _getPattern(schema), table, column);
            return _getResultSet(resultset, "getColumns");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XConnection getConnection() throws SQLException
    {
        return m_Connection;
    }

    @Override
    public XResultSet getCrossReference(Object catalog1, String schema1, String table1, Object catalog2, String schema2, String table2)
            throws SQLException
    {
        try {
            schema1 = _getPattern(schema1);
            schema2 = _getPattern(schema2);
            java.sql.ResultSet resultset = m_Metadata.getCrossReference(_getPattern(catalog1), schema1, table1, _getPattern(catalog2), schema2, table2);
            return _getResultSet(resultset, "getCrossReference");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getDatabaseProductName() throws SQLException
    {
        try {
            String value = m_Metadata.getDatabaseProductName();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        try {
            String value = m_Metadata.getDatabaseProductVersion();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        try {
            return m_Metadata.getDefaultTransactionIsolation();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getDriverMajorVersion()
    {
        return m_Metadata.getDriverMajorVersion();
    }

    @Override
    public int getDriverMinorVersion()
    {
        return m_Metadata.getDriverMinorVersion();
    }

    @Override
    public String getDriverName() throws SQLException
    {
        try {
            String value = m_Metadata.getDriverName();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getDriverVersion() throws SQLException
    {
        try {
            String value = m_Metadata.getDriverVersion();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_DATABASE_METADATA_DRIVER_VERSION, value);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getExportedKeys(Object catalog, String schema, String table) throws SQLException
    {
        try {
            java.sql.ResultSet resultset = m_Metadata.getExportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getExportedKeys");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        try {
            String value = m_Metadata.getExtraNameCharacters();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        try {
            String value = m_Metadata.getIdentifierQuoteString();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }
    
    @Override
    public XResultSet getImportedKeys(Object catalog, String schema, String table) throws SQLException
    {
        try {
            java.sql.ResultSet resultset = m_Metadata.getImportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getImportedKeys");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getIndexInfo(Object catalog, String schema, String table, boolean arg3, boolean arg4)
        throws SQLException
    {
        try 
        {
            java.sql.ResultSet resultset = m_Metadata.getIndexInfo(_getPattern(catalog), _getPattern(schema), table, arg3, arg4);
            return _getResultSet(resultset, "getIndexInfo");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxBinaryLiteralLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxCatalogNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxCharLiteralLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnsInGroupBy();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnsInIndex();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnsInOrderBy();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnsInSelect();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        try {
            return m_Metadata.getMaxColumnsInTable();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxConnections() throws SQLException
    {
        try {
            return m_Metadata.getMaxConnections();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxCursorNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxIndexLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxIndexLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxProcedureNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxRowSize() throws SQLException
    {
        try {
            return m_Metadata.getMaxRowSize();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxSchemaNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxStatementLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxStatementLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxStatements() throws SQLException
    {
        try {
            return m_Metadata.getMaxStatements();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        try {
            return m_Metadata.getMaxTableNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        try {
            return m_Metadata.getMaxTablesInSelect();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getMaxUserNameLength()");
            return m_Metadata.getMaxUserNameLength();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return 0;
        }
    }

    @Override
    public String getNumericFunctions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getNumericFunctions()");
            String value = m_Metadata.getNumericFunctions();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getPrimaryKeys(Object catalog, String schema, String table) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getPrimaryKeys()");
            java.sql.ResultSet resultset = m_Metadata.getPrimaryKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getPrimaryKeys");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getProcedureColumns(Object catalog, String schema, String table, String column) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getProcedureColumns()");
            java.sql.ResultSet resultset = m_Metadata.getProcedureColumns(_getPattern(catalog), _getPattern(schema), table, column);
            return _getResultSet(resultset, "getProcedureColumns");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getProcedureTerm() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getProcedureTerm()");
            String value = m_Metadata.getProcedureTerm();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getProcedures(Object catalog, String schema, String table) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getProcedures()");
            java.sql.ResultSet resultset = m_Metadata.getProcedures(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getProcedures");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getSQLKeywords() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getSQLKeywords()");
            String value = m_Metadata.getSQLKeywords();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getSchemaTerm() throws SQLException
    {
        try {
            String value = m_Metadata.getSchemaTerm();
            System.out.println("sdbc.DatabaseMetaData.getSchemaTerm() : " + value);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getSchemas() throws SQLException
    {
        try {
            java.sql.ResultSet result = m_Metadata.getSchemas();
            while (result.next())
            {
                System.out.println("sdbc.DatabaseMetaData.getSchemas() : " + result.getString(1));
            }
            result.close();
            return _getResultSet(m_Metadata.getSchemas(), "getSchemas");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getSearchStringEscape() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getSearchStringEscape()");
            String value = m_Metadata.getSearchStringEscape();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getStringFunctions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getStringFunctions()");
            String value = m_Metadata.getStringFunctions();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public String getSystemFunctions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getSystemFunctions()");
            String value = m_Metadata.getSystemFunctions();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getTablePrivileges(Object catalog, String schema, String table)
        throws SQLException
    {
        System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 1 *****************************");
        try {
            java.sql.ResultSet result = DBPrivilegesHelper.getTablePrivilegesResultSet(m_Connection.getProvider(),
                                                                                       m_Metadata,
                                                                                       _getPattern(catalog),
                                                                                       _getPattern(schema),
                                                                                       table);
            System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 2");
            DBTools.printResultSet(result);
            result = DBPrivilegesHelper.getTablePrivilegesResultSet(m_Connection.getProvider(),
                                                                    m_Metadata,
                                                                    _getPattern(catalog),
                                                                    _getPattern(schema),
                                                                    table);
            return result != null ? _getResultSet(result, "getTablePrivileges") : null;
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getTableTypes() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getTableTypes()");
            java.sql.ResultSet resultset = m_Connection.getProvider().getTableTypesResultSet(m_Metadata);
            while (resultset.next())
            {
                System.out.println("sdbc.DatabaseMetaData.getTableTypes(): Type: " + resultset.getString(1));
            }
            resultset = m_Connection.getProvider().getTableTypesResultSet(m_Metadata);
            return _getResultSet(resultset, "getTableTypes");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public XResultSet getTables(Object catalog, String schema, String table, String[] types) throws SQLException
    {
        System.out.println("sdbc.DatabaseMetaData.getTables() 1");
        try {
            System.out.println("sdbc.DatabaseMetaData.getTables() Catalog: " + _getPattern(catalog) + " - Schema: " + _getPattern(schema) + " - Table: " + table + " - Types: " + _getPattern(types));
            java.sql.ResultSet resultset = m_Metadata.getTables(_getPattern(catalog), _getPattern(schema), table, _getPattern(types));
            return _getResultSet(resultset, "getTables");
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData.getTables() ********************************* ERROR");
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getTimeDateFunctions()");
            String value = m_Metadata.getTimeDateFunctions();
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace()) {
                System.out.println(trace);
            }
            return "";
        }
    }

    @Override
    public XResultSet getTypeInfo() throws SQLException
    {
        try {
            System.out.println("DatabaseMetaData.getTypeInfo() 1");
            java.sql.ResultSet resultset = m_Connection.getProvider().getTypeInfoResultSet(m_Metadata);
            return _getResultSet(resultset, "getTypeInfo");
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getUDTs(Object catalog, String schema, String type, int[] types) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getUDTs()");
            java.sql.ResultSet resultset = m_Metadata.getUDTs(_getPattern(catalog), _getPattern(schema), type, types);
            return _getResultSet(resultset, "getUDTs");
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return null;
        }
    }

    @Override
    public String getUserName() throws SQLException
    {
        try {
            String value = m_Metadata.getUserName();
            System.out.println("sdbc.DatabaseMetaData.getUserName(): " + value);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getVersionColumns(Object catalog, String schema, String table) throws SQLException
    {
        try 
        {
            java.sql.ResultSet resultset = m_Metadata.getVersionColumns(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset, "getVersionColumns");
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean insertsAreDetected(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.insertsAreDetected()");
            return m_Metadata.insertsAreDetected(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        try {
            //System.out.println("sdbc.DatabaseMetaData.isCatalogAtStart() 1");
            boolean value = m_Metadata.isCatalogAtStart();
            //System.out.println("sdbc.DatabaseMetaData.isCatalogAtStart() 2: " + value);
            return value;
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        try {
            return m_Metadata.isReadOnly();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.nullPlusNonNullIsNull()");
            return m_Metadata.nullPlusNonNullIsNull();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.nullsAreSortedAtEnd()");
            return m_Metadata.nullsAreSortedAtEnd();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.nullsAreSortedAtStart()");
            return m_Metadata.nullsAreSortedAtStart();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.nullsAreSortedHigh()");
            return m_Metadata.nullsAreSortedHigh();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.nullsAreSortedLow()");
            return m_Metadata.nullsAreSortedLow();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean othersDeletesAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.othersDeletesAreVisible()");
            return m_Metadata.othersDeletesAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean othersInsertsAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.othersInsertsAreVisible()");
            return m_Metadata.othersInsertsAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean othersUpdatesAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.othersUpdatesAreVisible()");
            return m_Metadata.othersUpdatesAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean ownDeletesAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.ownDeletesAreVisible()");
            return m_Metadata.ownDeletesAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean ownInsertsAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.ownInsertsAreVisible()");
            return m_Metadata.ownInsertsAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean ownUpdatesAreVisible(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.ownUpdatesAreVisible()");
            return m_Metadata.ownUpdatesAreVisible(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesLowerCaseIdentifiers()");
            return m_Metadata.storesLowerCaseIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesLowerCaseQuotedIdentifiers()");
            return m_Metadata.storesLowerCaseQuotedIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesMixedCaseIdentifiers()");
            return m_Metadata.storesMixedCaseIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesMixedCaseQuotedIdentifiers()");
            return m_Metadata.storesMixedCaseQuotedIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesUpperCaseIdentifiers()");
            return m_Metadata.storesUpperCaseIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.storesUpperCaseQuotedIdentifiers()");
            return m_Metadata.storesUpperCaseQuotedIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsANSI92EntryLevelSQL()");
            return m_Metadata.supportsANSI92EntryLevelSQL();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsANSI92FullSQL()");
            return m_Metadata.supportsANSI92FullSQL();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsANSI92IntermediateSQL()");
            return m_Metadata.supportsANSI92IntermediateSQL();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        try {
            return m_Metadata.supportsAlterTableWithAddColumn();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        try {
            return m_Metadata.supportsAlterTableWithDropColumn();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        try {
            return m_Metadata.supportsBatchUpdates();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        try {
            //System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInDataManipulation() 1");
            boolean value = m_Metadata.supportsCatalogsInDataManipulation();
            //System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInDataManipulation() 2: " + value);
            return value;
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        try {
            boolean value = m_Metadata.supportsCatalogsInIndexDefinitions();
            System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInIndexDefinitions(): " + value);
            return value;
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInPrivilegeDefinitions()");
            return m_Metadata.supportsCatalogsInPrivilegeDefinitions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInProcedureCalls()");
            return m_Metadata.supportsCatalogsInProcedureCalls();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsCatalogsInTableDefinitions()");
            return m_Metadata.supportsCatalogsInTableDefinitions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsColumnAliasing()");
            return m_Metadata.supportsColumnAliasing();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsConvert(int arg0, int arg1) throws SQLException
    {
        try {
            return m_Metadata.supportsConvert();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        try {
            return m_Metadata.supportsCoreSQLGrammar();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        try {
            return m_Metadata.supportsCorrelatedSubqueries();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()");
            return m_Metadata.supportsDataDefinitionAndDataManipulationTransactions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsDataManipulationTransactionsOnly()");
            return m_Metadata.supportsDataManipulationTransactionsOnly();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsDifferentTableCorrelationNames()");
            return m_Metadata.supportsDifferentTableCorrelationNames();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsExpressionsInOrderBy()");
            return m_Metadata.supportsExpressionsInOrderBy();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsExtendedSQLGrammar()");
            return m_Metadata.supportsExtendedSQLGrammar();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsFullOuterJoins()");
            return m_Metadata.supportsFullOuterJoins();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsGroupBy()");
            return m_Metadata.supportsGroupBy();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsGroupByBeyondSelect()");
            return m_Metadata.supportsGroupByBeyondSelect();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsGroupByUnrelated()");
            return m_Metadata.supportsGroupByUnrelated();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsIntegrityEnhancementFacility()");
            return m_Metadata.supportsIntegrityEnhancementFacility();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsLikeEscapeClause()");
            return m_Metadata.supportsLikeEscapeClause();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsLimitedOuterJoins()");
            return m_Metadata.supportsLimitedOuterJoins();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsMinimumSQLGrammar()");
            return m_Metadata.supportsMinimumSQLGrammar();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            // FIXME: SDBC bug. Must be able to throw exception.
            // FIXME: throw UnoHelper.getSQLException(e, this);
            return false;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsMixedCaseIdentifiers()");
            return m_Metadata.supportsMixedCaseIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        try {
            return m_Metadata.supportsMixedCaseQuotedIdentifiers();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        try {
            return m_Metadata.supportsMultipleResultSets();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        try {
            return m_Metadata.supportsMultipleTransactions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        try {
            return m_Metadata.supportsNonNullableColumns();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        try {
            return m_Metadata.supportsOpenCursorsAcrossCommit();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        try {
            return m_Metadata.supportsOpenCursorsAcrossRollback();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        try {
            return m_Metadata.supportsOpenStatementsAcrossCommit();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        try {
            return m_Metadata.supportsOpenStatementsAcrossRollback();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        try {
            return m_Metadata.supportsOrderByUnrelated();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        try {
            return m_Metadata.supportsOuterJoins();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        try {
            return m_Metadata.supportsPositionedDelete();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        try {
            return m_Metadata.supportsPositionedUpdate();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        try {
            return m_Metadata.supportsResultSetConcurrency (type, concurrency);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsResultSetType(int arg0) throws SQLException
    {
        try {
            return m_Metadata.supportsResultSetType(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        try {
            return m_Metadata.supportsSchemasInDataManipulation();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        try {
            return m_Metadata.supportsSchemasInIndexDefinitions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        try {
            return m_Metadata.supportsSchemasInPrivilegeDefinitions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        try {
            return m_Metadata.supportsSchemasInProcedureCalls();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        try {
            boolean value = m_Metadata.supportsSchemasInTableDefinitions();
            System.out.println("sdbc.DatabaseMetaData.supportsSchemasInTableDefinitions(): " + value);
            return value;
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        try {
            return m_Metadata.supportsSelectForUpdate();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        try {
            return m_Metadata.supportsStoredProcedures();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        try {
            return m_Metadata.supportsSubqueriesInComparisons();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        try {
            return m_Metadata.supportsSubqueriesInExists();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        try {
            return m_Metadata.supportsSubqueriesInIns();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        try {
            return m_Metadata.supportsSubqueriesInQuantifieds();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        try {
            return m_Metadata.supportsTableCorrelationNames();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException
    {
        try {
            return m_Metadata.supportsTransactionIsolationLevel(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsTransactions() throws SQLException
    {
        try {
            return m_Metadata.supportsTransactions();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsTypeConversion() throws SQLException
    {
        try {
            return m_Metadata.supportsConvert();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsUnion() throws SQLException
    {
        try {
            return m_Metadata.supportsUnion();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        try {
            return m_Metadata.supportsUnionAll();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean updatesAreDetected(int arg0) throws SQLException
    {
        try {
            return m_Metadata.updatesAreDetected(arg0);
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        try {
            return m_Metadata.usesLocalFilePerTable();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        try {
            return m_Metadata.usesLocalFiles();
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbc.DatabaseMetaData ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            return false;
        }
    }

    protected XResultSet _getResultSet(java.sql.ResultSet result,
                                       String method)
        throws SQLException
    {
        ResultSet resultset = null;
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_METADATA_RESULTSET, method);
        if (result != null) {
            resultset = new ResultSet(m_Connection, result);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_METADATA_RESULTSET_ID, resultset.getLogger().getObjectId());
        }
        return resultset;
    }

    protected static String _getPattern(Object object)
    {
        String value = null;
        if (AnyConverter.isString(object)) {
            value = AnyConverter.toString(object);
        }
        return value;
    }

    protected static String _getPattern(String value)
    {
        if (value.equals("%")) {
            return null;
        }
        return value;
    }

    protected static String[] _getPattern(String[] values)
    {
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