/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData2;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.sdbc.XRow;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class DatabaseMetaDataBase
    extends WeakBase
    implements XDatabaseMetaData2
{
    protected final ConnectionBase m_Connection;
    protected final java.sql.DatabaseMetaData m_Metadata;

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
        System.out.println("sdbc.DatabaseMetaDataBase() 1");
    }


    // com.sun.star.sdbc.XDatabaseMetaData2:
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
            System.out.println("sdbc.DatabaseMetaData.allProceduresAreCallable()");
            return m_Metadata.allProceduresAreCallable();
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
    public boolean allTablesAreSelectable() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.allTablesAreSelectable()");
            return m_Metadata.allTablesAreSelectable();
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
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.dataDefinitionCausesTransactionCommit()");
            return m_Metadata.dataDefinitionCausesTransactionCommit();
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
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.dataDefinitionIgnoredInTransactions()");
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
            System.out.println("sdbc.DatabaseMetaData.deletesAreDetected()");
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
            System.out.println("sdbc.DatabaseMetaData.doesMaxRowSizeIncludeBlobs()");
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
            System.out.println("sdbc.DatabaseMetaData.getBestRowIdentifier()");
            return _getResultSet(m_Metadata.getBestRowIdentifier(_getPattern(catalog), _getPattern(schema), table, scope, nullable));
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
            System.out.println("sdbc.DatabaseMetaData.getCatalogSeparator() 1");
            String value = m_Metadata.getCatalogSeparator();
            System.out.println("sdbc.DatabaseMetaData.getCatalogSeparator(): " + value);
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
            System.out.println("sdbc.DatabaseMetaData.getCatalogTerm(): " + value);
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
            java.sql.ResultSet result = m_Metadata.getCatalogs();
            while (result.next())
            {
                System.out.println("sdbc.DatabaseMetaData.getCatalogs() : " + result.getString(1));
            }
            result.close();
            return _getResultSet(m_Metadata.getCatalogs());
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
            System.out.println("sdbc.DatabaseMetaData.getColumnPrivileges()");
            java.sql.ResultSet resultset = m_Metadata.getColumnPrivileges(_getPattern(catalog), _getPattern(schema), table, column);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getColumns()");
            java.sql.ResultSet resultset = m_Metadata.getColumns(_getPattern(catalog), _getPattern(schema), table, column);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getCrossReference()");
            schema1 = _getPattern(schema1);
            schema2 = _getPattern(schema2);
            java.sql.ResultSet resultset = m_Metadata.getCrossReference(_getPattern(catalog1), schema1, table1, _getPattern(catalog2), schema2, table2);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getDatabaseProductName()");
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
            System.out.println("sdbc.DatabaseMetaData.getDatabaseProductVersion()");
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
            System.out.println("sdbc.DatabaseMetaData.getDefaultTransactionIsolation()");
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
        System.out.println("sdbc.DatabaseMetaData.getDriverMajorVersion()");
        return m_Metadata.getDriverMajorVersion();
    }

    @Override
    public int getDriverMinorVersion()
    {
        System.out.println("sdbc.DatabaseMetaData.getDriverMinorVersion()");
        return m_Metadata.getDriverMinorVersion();
    }

    @Override
    public String getDriverName() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getDriverName()");
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
            System.out.println("sdbc.DatabaseMetaData.getDriverVersion()");
            String value = m_Metadata.getDriverVersion();
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
            System.out.println("sdbc.DatabaseMetaData.getExportedKeys()");
            java.sql.ResultSet resultset = m_Metadata.getExportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getExtraNameCharacters()");
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
            System.out.println("sdbc.DatabaseMetaData.getIdentifierQuoteString() 1");
            String value = m_Metadata.getIdentifierQuoteString();
            System.out.println("sdbc.DatabaseMetaData.getIdentifierQuoteString() 2: " + value);
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
            System.out.println("sdbc.DatabaseMetaData.getImportedKeys()");
            java.sql.ResultSet resultset = m_Metadata.getImportedKeys(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getIndexInfo()");
            java.sql.ResultSet resultset = m_Metadata.getIndexInfo(_getPattern(catalog), _getPattern(schema), table, arg3, arg4);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getMaxBinaryLiteralLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxCatalogNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxCharLiteralLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnsInGroupBy()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnsInIndex()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnsInOrderBy()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnsInSelect()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxColumnsInTable()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxConnections()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxCursorNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxIndexLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxProcedureNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxRowSize()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxSchemaNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxStatementLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxStatements()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxTableNameLength()");
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
            System.out.println("sdbc.DatabaseMetaData.getMaxTablesInSelect()");
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
            return _getResultSet(resultset);
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
            return _getResultSet(resultset);
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
            return _getResultSet(resultset);
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
            return _getResultSet(m_Metadata.getSchemas());
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
    public XResultSet getTablePrivileges(Object catalog, String schema, String table) throws SQLException
    {
        try {
            XResultSet result = null;
            System.out.println("sdbc.DatabaseMetaData.getTablePrivileges()");
            if (_isIgnoreDriverPrivilegesEnabled()) {
                System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 2 ***********************************");
                result = _getTablePrivileges(catalog, schema, table);
            }
            else {
                java.sql.ResultSet resultset = m_Metadata.getTablePrivileges(_getPattern(catalog), _getPattern(schema), table);
                System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 3 ColumnCount != 7 :" + resultset.getMetaData().getColumnCount());
                if (resultset != null) {
                    result = _getResultSet(resultset);
                    // we have to check the result columns for the tables privileges #106324#
                    XResultSetMetaDataSupplier supplier = UnoRuntime.queryInterface(XResultSetMetaDataSupplier.class, result);
                    XResultSetMetaData metadata = null;
                    if (supplier != null) {
                        metadata = supplier.getMetaData();
                    }
                    if (metadata != null && metadata.getColumnCount() != 7)
                    {
                        System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 4 ***********************************");
                        // here we know that the count of column doesn't match
                        Map<Integer,Integer> columns = new TreeMap<>();
                        String[] privileges = new String[] {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                                                            "GRANTOR", "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"};
                        int count = metadata.getColumnCount();
                        int length = privileges.length;
                        for (int i = 1; i <= count; i++) {
                            String columnName = metadata.getColumnName(i);
                            for (int j = 0; j < length; j++) {
                                if (columnName.equals(privileges[j])) {
                                    columns.put(i, j);
                                    break;
                                }
                            }
                        }
                        // fill our own resultset
                        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
                        XRow row = UnoRuntime.queryInterface(XRow.class, result);
                        while (result.next()) {
                            CustomRowSet[] rowset = new CustomRowSet[7];
                            for (int i = 0; i < rowset.length; i++) {
                                rowset[i] = new CustomRowSet(null, true);
                            }
                            for (Map.Entry<Integer,Integer> column : columns.entrySet()) {
                                String value = row.getString(column.getKey());
                                if (row.wasNull()) {
                                    rowset[column.getValue()].setNull();
                                } else {
                                    rowset[column.getValue()].setString(value);
                                }
                            }
                            rows.add(rowset);
                        }
                        result = new CustomResultSet(_getTablesPrivilegesMetadata(), rows);
                    }
                }
            }
            System.out.println("sdbc.DatabaseMetaData.getTablePrivileges() 5 ***********************************");
            return result;
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

    private final boolean _isIgnoreDriverPrivilegesEnabled()
    {
        return UnoHelper.getDefaultPropertyValue(m_Connection.getInfo(), "IgnoreDriverPrivileges", false);
    }

    @Override
    public XResultSet getTableTypes() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getTableTypes()");
            java.sql.ResultSet resultset = m_Metadata.getTableTypes();
            while (resultset.next())
            {
                System.out.println("sdbc.DatabaseMetaData.getTableTypes(): Type: " + resultset.getString(1));
            }
            return _getResultSet(m_Metadata.getTableTypes());
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
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getTypeInfo()");
            java.sql.ResultSet resultset = m_Metadata.getTypeInfo();
            return _getResultSet(resultset);
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
    public XResultSet getUDTs(Object catalog, String schema, String type, int[] types) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.getUDTs()");
            java.sql.ResultSet resultset = m_Metadata.getUDTs(_getPattern(catalog), _getPattern(schema), type, types);
            return _getResultSet(resultset);
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
            System.out.println("sdbc.DatabaseMetaData.getUserName()");
            String value = m_Metadata.getUserName();
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
    public XResultSet getVersionColumns(Object catalog, String schema, String table) throws SQLException
    {
        try 
        {
            System.out.println("sdbc.DatabaseMetaData.getVersionColumns()");
            java.sql.ResultSet resultset = m_Metadata.getVersionColumns(_getPattern(catalog), _getPattern(schema), table);
            return _getResultSet(resultset);
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
            boolean value = m_Metadata.supportsAlterTableWithAddColumn();
            System.out.println("sdbc.DatabaseMetaData.supportsAlterTableWithAddColumn(): " + value);
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
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        try {
            boolean value = m_Metadata.supportsAlterTableWithDropColumn();
            System.out.println("sdbc.DatabaseMetaData.supportsAlterTableWithDropColumn(): " + value);
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
    public boolean supportsBatchUpdates() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsBatchUpdates()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsConvert()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsCoreSQLGrammar()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsCorrelatedSubqueries()");
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
            boolean value = m_Metadata.supportsMixedCaseQuotedIdentifiers();
            System.out.println("sdbc.DatabaseMetaData.supportsMixedCaseQuotedIdentifiers(): " + value);
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
    public boolean supportsMultipleResultSets() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsMultipleResultSets()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsMultipleTransactions()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsNonNullableColumns()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOpenCursorsAcrossCommit()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOpenCursorsAcrossRollback()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOpenStatementsAcrossCommit()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOpenStatementsAcrossRollback()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOrderByUnrelated()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsOuterJoins()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsPositionedDelete()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsPositionedUpdate()");
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
            boolean value = m_Metadata.supportsResultSetConcurrency (type, concurrency);
            System.out.println("sdbc.DatabaseMetaData.supportsResultSetConcurrency() Type: " + type + " - Concurrency: " + concurrency + " - Value: " + value);
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
    public boolean supportsResultSetType(int arg0) throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsResultSetType()");
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
            //System.out.println("sdbc.DatabaseMetaData.supportsSchemasInDataManipulation() 1");
            boolean value = m_Metadata.supportsSchemasInDataManipulation();
            //System.out.println("sdbc.DatabaseMetaData.supportsSchemasInDataManipulation() 2: " + value);
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
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        try {
            boolean value = m_Metadata.supportsSchemasInIndexDefinitions();
            System.out.println("sdbc.DatabaseMetaData.supportsSchemasInIndexDefinitions(): " +  value);
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
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        try {
            System.out.println("sdbc.DatabaseMetaData.supportsSchemasInPrivilegeDefinitions()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSchemasInProcedureCalls()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSelectForUpdate()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsStoredProcedures()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSubqueriesInComparisons()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSubqueriesInExists()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSubqueriesInIns()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsSubqueriesInQuantifieds()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsTableCorrelationNames()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsTransactionIsolationLevel()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsTransactions()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsTypeConversion()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsUnion()");
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
            System.out.println("sdbc.DatabaseMetaData.supportsUnionAll()");
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
            System.out.println("sdbc.DatabaseMetaData.updatesAreDetected()");
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
            System.out.println("sdbc.DatabaseMetaData.usesLocalFilePerTable()");
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
            System.out.println("sdbc.DatabaseMetaData.usesLocalFiles()");
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


    protected XResultSet _getResultSet(java.sql.ResultSet resultset)
    {
        XResultSet result = null;
        try {
            if (resultset != null)
                result = m_Connection.getProvider().getResultSet(m_Connection, resultset);
        }
        catch (java.lang.Exception e)
        {
            System.out.println("sdbc.DatabaseMetaData._getResultSet() ********************************* ERROR: ");
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
        }
        return result;
    }


    // XDatabaseMetaData.getTypeInfo:
    protected XResultSet _getTypeInfo()
            throws java.sql.SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        java.sql.ResultSet resultset = m_Metadata.getTypeInfo();
        while (resultset.next())
        {
            System.out.println("sdbc.DatabaseMetaDataBase.getTypeInfo()");
            rows.add(_getTypeInfoRowSet(resultset));
        }
        resultset.close();
        return new CustomResultSet(_getTypeInfoMetadata(), rows);
    }

    protected CustomRowSet[] _getTypeInfoRowSet(java.sql.ResultSet result)
            throws java.sql.SQLException
    {
        CustomRowSet[] row = new CustomRowSet[18];
        row[0] = new CustomRowSet(result.getString(1), result.wasNull());
        row[1] = new CustomRowSet(m_Connection.getProvider().getDataType(result.getInt(2)));
        row[2] = new CustomRowSet(result.getLong(3));
        row[3] = new CustomRowSet(result.getString(4), result.wasNull());
        row[4] = new CustomRowSet(result.getString(5), result.wasNull());
        row[5] = new CustomRowSet(result.getString(6), result.wasNull());
        row[6] = new CustomRowSet(result.getShort(7));
        row[7] = new CustomRowSet(result.getBoolean(8));
        row[8] = new CustomRowSet(result.getShort(9));
        row[9] = new CustomRowSet(result.getBoolean(10));
        row[10] = new CustomRowSet(result.getBoolean(11));
        row[11] = new CustomRowSet(result.getBoolean(12));
        System.out.println("sdbc.DatabaseMetaDataBase.getTypeInfo() TypeName: " + result.getString(1) + " - AutoIncrement: " + result.getBoolean(12));
        row[12] = new CustomRowSet(result.getString(13), result.wasNull());
        row[13] = new CustomRowSet(result.getShort(14));
        row[14] = new CustomRowSet(result.getShort(15));
        row[15] = new CustomRowSet(result.getLong(16));
        row[16] = new CustomRowSet(result.getLong(17));
        row[17] = new CustomRowSet(result.getLong(18));
        return row;
    }

    protected XResultSetMetaData _getTypeInfoMetadata()
    {
        CustomColumn[] columns = new CustomColumn[18];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TYPE_NAME");
        columns[0].setNullable(ColumnValue.NO_NULLS);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        columns[1] = new CustomColumn();
        columns[1].setColumnName("DATA_TYPE");
        columns[1].setNullable(ColumnValue.NO_NULLS);
        columns[1].setColumnDisplaySize(3);
        columns[1].setPrecision(0);
        columns[1].setScale(0);
        columns[1].setColumnType(DataType.SMALLINT);
        columns[2] = new CustomColumn();
        columns[2].setColumnName("PRECISION");
        columns[2].setNullable(ColumnValue.NO_NULLS);
        columns[2].setColumnDisplaySize(3);
        columns[2].setPrecision(0);
        columns[2].setScale(0);
        columns[2].setColumnType(DataType.INTEGER);
        columns[3] = new CustomColumn();
        columns[3].setColumnName("LITERAL_PREFIX");
        columns[3].setNullable(ColumnValue.NULLABLE);
        columns[3].setColumnDisplaySize(3);
        columns[3].setPrecision(0);
        columns[3].setScale(0);
        columns[3].setColumnType(DataType.VARCHAR);
        columns[4] = new CustomColumn();
        columns[4].setColumnName("LITERAL_SUFFIX");
        columns[4].setNullable(ColumnValue.NULLABLE);
        columns[4].setColumnDisplaySize(0);
        columns[4].setPrecision(0);
        columns[4].setScale(0);
        columns[4].setColumnType(DataType.VARCHAR);
        columns[5] = new CustomColumn();
        columns[5].setColumnName("CREATE_PARAMS");
        columns[5].setNullable(ColumnValue.NULLABLE);
        columns[5].setColumnDisplaySize(3);
        columns[5].setPrecision(0);
        columns[5].setScale(0);
        columns[5].setColumnType(DataType.VARCHAR);
        columns[6] = new CustomColumn();
        columns[6].setColumnName("NULLABLE");
        columns[6].setNullable(ColumnValue.NO_NULLS);
        columns[6].setColumnDisplaySize(0);
        columns[6].setPrecision(0);
        columns[6].setScale(0);
        columns[6].setColumnType(DataType.SMALLINT);
        columns[7] = new CustomColumn();
        columns[7].setColumnName("CASE_SENSITIVE");
        columns[7].setNullable(ColumnValue.NO_NULLS);
        columns[7].setColumnDisplaySize(3);
        columns[7].setPrecision(0);
        columns[7].setScale(0);
        columns[7].setColumnType(DataType.BOOLEAN);
        columns[8] = new CustomColumn();
        columns[8].setColumnName("SEARCHABLE");
        columns[8].setNullable(ColumnValue.NO_NULLS);
        columns[8].setColumnDisplaySize(0);
        columns[8].setPrecision(0);
        columns[8].setScale(0);
        columns[8].setColumnType(DataType.SMALLINT);
        columns[9] = new CustomColumn();
        columns[9].setColumnName("UNSIGNED_ATTRIBUTE");
        columns[9].setNullable(ColumnValue.NO_NULLS);
        columns[9].setColumnDisplaySize(3);
        columns[9].setPrecision(0);
        columns[9].setScale(0);
        columns[9].setColumnType(DataType.BOOLEAN);
        columns[10] = new CustomColumn();
        columns[10].setColumnName("FIXED_PREC_SCALE");
        columns[10].setNullable(ColumnValue.NO_NULLS);
        columns[10].setColumnDisplaySize(0);
        columns[10].setPrecision(0);
        columns[10].setScale(0);
        columns[10].setColumnType(DataType.BOOLEAN);
        columns[11] = new CustomColumn();
        columns[11].setColumnName("AUTO_INCREMENT");
        columns[11].setNullable(ColumnValue.NO_NULLS);
        columns[11].setColumnDisplaySize(3);
        columns[11].setPrecision(0);
        columns[11].setScale(0);
        columns[11].setColumnType(DataType.BOOLEAN);
        columns[12] = new CustomColumn();
        columns[12].setColumnName("LOCAL_TYPE_NAME");
        columns[12].setNullable(ColumnValue.NULLABLE);
        columns[12].setColumnDisplaySize(0);
        columns[12].setPrecision(0);
        columns[12].setScale(0);
        columns[12].setColumnType(DataType.VARCHAR);
        columns[13] = new CustomColumn();
        columns[13].setColumnName("MINIMUM_SCALE");
        columns[13].setNullable(ColumnValue.NO_NULLS);
        columns[13].setColumnDisplaySize(3);
        columns[13].setPrecision(0);
        columns[13].setScale(0);
        columns[13].setColumnType(DataType.SMALLINT);
        columns[14] = new CustomColumn();
        columns[14].setColumnName("MAXIMUM_SCALE");
        columns[14].setNullable(ColumnValue.NO_NULLS);
        columns[14].setColumnDisplaySize(0);
        columns[14].setPrecision(0);
        columns[14].setScale(0);
        columns[14].setColumnType(DataType.SMALLINT);
        columns[15] = new CustomColumn();
        columns[15].setColumnName("SQL_DATA_TYPE");
        columns[15].setNullable(ColumnValue.NO_NULLS);
        columns[15].setColumnDisplaySize(3);
        columns[15].setPrecision(0);
        columns[15].setScale(0);
        columns[15].setColumnType(DataType.INTEGER);
        columns[16] = new CustomColumn();
        columns[16].setColumnName("SQL_DATETIME_SUB");
        columns[16].setNullable(ColumnValue.NO_NULLS);
        columns[16].setColumnDisplaySize(0);
        columns[16].setPrecision(0);
        columns[16].setScale(0);
        columns[16].setColumnType(DataType.INTEGER);
        columns[17] = new CustomColumn();
        columns[17].setColumnName("NUM_PREC_RADIX");
        columns[17].setNullable(ColumnValue.NO_NULLS);
        columns[17].setColumnDisplaySize(0);
        columns[17].setPrecision(0);
        columns[17].setScale(0);
        columns[17].setColumnType(DataType.INTEGER);
        return new CustomResultSetMetaData(columns);
    }

    // XDatabaseMetaData.getTableTypes:
    protected XResultSet _getTableTypes()
        throws java.sql.SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        java.sql.ResultSet resultset = m_Metadata.getTableTypes();
        while (resultset.next())
        {
            System.out.println("sdbc.DatabaseMetaDataBase._getTableTypes() : " + resultset.getString(1));
            rows.add(_getTableTypesRowSet(resultset));
        }
        resultset.close();
        return new CustomResultSet(_getTableTypesMetadata(), rows);
    }

    protected CustomRowSet[] _getTableTypesRowSet(java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[1];
            row[0] =  new CustomRowSet(_mapDatabaseTableTypes(result.getString(1)), result.wasNull());
            return row;
        }

    protected XResultSetMetaData _getTableTypesMetadata()
    {
        CustomColumn[] columns = new CustomColumn[1];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TABLE_TYPE");
        columns[0].setNullable(ColumnValue.NO_NULLS);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        return new CustomResultSetMetaData(columns);
    }


    // XDatabaseMetaData.getTables:
    protected XResultSet _getTables(String catalog,
                                    String schema,
                                    String table,
                                    String[] types)
        throws java.sql.SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        java.sql.ResultSet result = m_Metadata.getTables(catalog, schema, table, types);
        while (result.next())
        {
            System.out.println("sdbc.DatabaseMetaDataBase._getTables() " + result.getString(1) + "." + result.getString(2) + "." + result.getString(3)  + " - Type: " + result.getString(4) +  "\nRemarks: " + result.getString(5));
            rows.add(_getTablesRowSet(result));
        }
        result.close();
        return new CustomResultSet(_getTablesMetadata(), rows);
    }

    protected CustomRowSet[] _getTablesRowSet(java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[5];
            row[0] = new CustomRowSet(result.getString(1), result.wasNull());
            row[1] = new CustomRowSet(result.getString(2), result.wasNull());
            row[2] = new CustomRowSet(result.getString(3), result.wasNull());
            row[3] = new CustomRowSet(_mapDatabaseTableTypes(result.getString(4)), result.wasNull());
            row[4] = new CustomRowSet(result.getString(5), result.wasNull());
           return row;
        }

    private XResultSetMetaData _getTablesMetadata()
    {
        CustomColumn[] columns = new CustomColumn[5];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TABLE_CAT");
        columns[0].setNullable(ColumnValue.NULLABLE);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        columns[1] = new CustomColumn();
        columns[1].setColumnName("TABLE_SCHEM");
        columns[1].setNullable(ColumnValue.NULLABLE);
        columns[1].setColumnDisplaySize(3);
        columns[1].setPrecision(0);
        columns[1].setScale(0);
        columns[1].setColumnType(DataType.VARCHAR);
        columns[2] = new CustomColumn();
        columns[2].setColumnName("TABLE_NAME");
        columns[2].setNullable(ColumnValue.NO_NULLS);
        columns[2].setColumnDisplaySize(3);
        columns[2].setPrecision(0);
        columns[2].setScale(0);
        columns[2].setColumnType(DataType.VARCHAR);
        columns[3] = new CustomColumn();
        columns[3].setColumnName("TABLE_TYPE");
        columns[3].setNullable(ColumnValue.NO_NULLS);
        columns[3].setColumnDisplaySize(3);
        columns[3].setPrecision(0);
        columns[3].setScale(0);
        columns[3].setColumnType(DataType.VARCHAR);
        columns[4] = new CustomColumn();
        columns[4].setColumnName("REMARKS");
        columns[4].setNullable(ColumnValue.NULLABLE);
        columns[4].setColumnDisplaySize(3);
        columns[4].setPrecision(0);
        columns[4].setScale(0);
        columns[4].setColumnType(DataType.VARCHAR);
        return new CustomResultSetMetaData(columns);
    }


    // XDatabaseMetaData.getColumns:
    protected XResultSet _getColumns(String catalog, String schema, String table, String column)
            throws java.sql.SQLException
        {
        System.out.println("sdbc.DatabaseMetaDataBase._getColumns() 1 Catalog: " + catalog + " - Schema: " + schema + " - Table: " + table + " - Column: " + column);

        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
            java.sql.ResultSet resultset = m_Metadata.getColumns(catalog, schema, table, column);
            while (resultset.next()) {
                System.out.println("sdbc.DatabaseMetaDataBase._getColumns() 2");
                rows.add(_getColumnsRowSet(resultset));
            }
            resultset.close();
            System.out.println("sdbc.DatabaseMetaDataBase._getColumns() 3");
            return new CustomResultSet(_getColumnsMetadata(), rows);
        }

    protected CustomRowSet[] _getColumnsRowSet(java.sql.ResultSet result)
            throws java.sql.SQLException
        {
            CustomRowSet[] row = new CustomRowSet[18];
            row[0] =  new CustomRowSet(result.getString(1), result.wasNull());
            row[1] =  new CustomRowSet(result.getString(2), result.wasNull());
            row[2] =  new CustomRowSet(result.getString(3), result.wasNull());
            row[3] =  new CustomRowSet(result.getString(4), result.wasNull());
            row[4] =  new CustomRowSet(m_Connection.getProvider().getDataType(result.getShort(5)));
            row[5] =  new CustomRowSet(result.getString(6), result.wasNull());
            row[6] =  new CustomRowSet(result.getLong(7));
            row[7] =  new CustomRowSet(result.getString(8), result.wasNull());
            row[8] =  new CustomRowSet(result.getLong(9));
            row[9] =  new CustomRowSet(result.getLong(10));
            row[10] = new CustomRowSet(result.getLong(11));
            row[11] = new CustomRowSet(result.getString(12), result.wasNull());
            row[12] = new CustomRowSet(result.getString(13), result.wasNull());
            row[13] = new CustomRowSet(result.getLong(14));
            row[14] = new CustomRowSet(result.getLong(15));
            row[15] = new CustomRowSet(result.getLong(16));
            row[16] = new CustomRowSet(result.getInt(17));
            row[17] = new CustomRowSet(result.getString(18), result.wasNull());
            return row;
        }

    protected XResultSetMetaData _getColumnsMetadata()
    {
        CustomColumn[] columns = new CustomColumn[18];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TABLE_CAT");
        columns[0].setNullable(ColumnValue.NULLABLE);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        columns[1] = new CustomColumn();
        columns[1].setColumnName("TABLE_SCHEM");
        columns[1].setNullable(ColumnValue.NULLABLE);
        columns[1].setColumnDisplaySize(3);
        columns[1].setPrecision(0);
        columns[1].setScale(0);
        columns[1].setColumnType(DataType.VARCHAR);
        columns[2] = new CustomColumn();
        columns[2].setColumnName("TABLE_NAME");
        columns[2].setNullable(ColumnValue.NO_NULLS);
        columns[2].setColumnDisplaySize(3);
        columns[2].setPrecision(0);
        columns[2].setScale(0);
        columns[2].setColumnType(DataType.VARCHAR);
        columns[3] = new CustomColumn();
        columns[3].setColumnName("COLUMN_NAME");
        columns[3].setNullable(ColumnValue.NO_NULLS);
        columns[3].setColumnDisplaySize(3);
        columns[3].setPrecision(0);
        columns[3].setScale(0);
        columns[3].setColumnType(DataType.VARCHAR);
        columns[4] = new CustomColumn();
        columns[4].setColumnName("DATA_TYPE");
        columns[4].setNullable(ColumnValue.NO_NULLS);
        columns[4].setColumnDisplaySize(3);
        columns[4].setPrecision(0);
        columns[4].setScale(0);
        columns[4].setColumnType(DataType.SMALLINT);
        columns[5] = new CustomColumn();
        columns[5].setColumnName("TYPE_NAME");
        columns[5].setNullable(ColumnValue.NO_NULLS);
        columns[5].setColumnDisplaySize(3);
        columns[5].setPrecision(0);
        columns[5].setScale(0);
        columns[5].setColumnType(DataType.VARCHAR);
        columns[6] = new CustomColumn();
        columns[6].setColumnName("COLUMN_SIZE");
        columns[6].setNullable(ColumnValue.NO_NULLS);
        columns[6].setColumnDisplaySize(3);
        columns[6].setPrecision(0);
        columns[6].setScale(0);
        columns[6].setColumnType(DataType.INTEGER);
        columns[7] = new CustomColumn();
        columns[7].setColumnName("BUFFER_LENGTH");
        columns[7].setNullable(ColumnValue.NULLABLE);
        columns[7].setColumnDisplaySize(3);
        columns[7].setPrecision(0);
        columns[7].setScale(0);
        columns[7].setColumnType(DataType.INTEGER);
        columns[8] = new CustomColumn();
        columns[8].setColumnName("DECIMAL_DIGITS");
        columns[8].setNullable(ColumnValue.NO_NULLS);
        columns[8].setColumnDisplaySize(3);
        columns[8].setPrecision(0);
        columns[8].setScale(0);
        columns[8].setColumnType(DataType.INTEGER);
        columns[9] = new CustomColumn();
        columns[9].setColumnName("NUM_PREC_RADIX");
        columns[9].setNullable(ColumnValue.NO_NULLS);
        columns[9].setColumnDisplaySize(3);
        columns[9].setPrecision(0);
        columns[9].setScale(0);
        columns[9].setColumnType(DataType.INTEGER);
        columns[10] = new CustomColumn();
        columns[10].setColumnName("NULLABLE");
        columns[10].setNullable(ColumnValue.NO_NULLS);
        columns[10].setColumnDisplaySize(3);
        columns[10].setPrecision(0);
        columns[10].setScale(0);
        columns[10].setColumnType(DataType.INTEGER);
        columns[11] = new CustomColumn();
        columns[11].setColumnName("REMARKS");
        columns[11].setNullable(ColumnValue.NULLABLE);
        columns[11].setColumnDisplaySize(3);
        columns[11].setPrecision(0);
        columns[11].setScale(0);
        columns[11].setColumnType(DataType.VARCHAR);
        columns[12] = new CustomColumn();
        columns[12].setColumnName("COLUMN_DEF");
        columns[12].setNullable(ColumnValue.NULLABLE);
        columns[12].setColumnDisplaySize(3);
        columns[12].setPrecision(0);
        columns[12].setScale(0);
        columns[12].setColumnType(DataType.VARCHAR);
        columns[13] = new CustomColumn();
        columns[13].setColumnName("SQL_DATA_TYPE");
        columns[13].setNullable(ColumnValue.NULLABLE);
        columns[13].setColumnDisplaySize(3);
        columns[13].setPrecision(0);
        columns[13].setScale(0);
        columns[13].setColumnType(DataType.INTEGER);
        columns[14] = new CustomColumn();
        columns[14].setColumnName("SQL_DATETIME_SUB");
        columns[14].setNullable(ColumnValue.NULLABLE);
        columns[14].setColumnDisplaySize(3);
        columns[14].setPrecision(0);
        columns[14].setScale(0);
        columns[14].setColumnType(DataType.INTEGER);
        columns[15] = new CustomColumn();
        columns[15].setColumnName("CHAR_OCTET_LENGTH");
        columns[15].setNullable(ColumnValue.NO_NULLS);
        columns[15].setColumnDisplaySize(3);
        columns[15].setPrecision(0);
        columns[15].setScale(0);
        columns[15].setColumnType(DataType.INTEGER);
        columns[16] = new CustomColumn();
        columns[16].setColumnName("ORDINAL_POSITION");
        columns[16].setNullable(ColumnValue.NO_NULLS);
        columns[16].setColumnDisplaySize(3);
        columns[16].setPrecision(0);
        columns[16].setScale(0);
        columns[16].setColumnType(DataType.INTEGER);
        columns[17] = new CustomColumn();
        columns[17].setColumnName("IS_NULLABLE");
        columns[17].setNullable(ColumnValue.NO_NULLS);
        columns[17].setColumnDisplaySize(3);
        columns[17].setPrecision(0);
        columns[17].setScale(0);
        columns[17].setColumnType(DataType.VARCHAR);
        return new CustomResultSetMetaData(columns);
    }


    // XDatabaseMetaData.getTablePrivileges:
    private XResultSet _getTablePrivileges(Object catalog,
                                           String schema,
                                           String table)
            throws SQLException
    {
        ArrayList<CustomRowSet[]> rows = new ArrayList<>();
        String[] privileges = {"SELECT", "INSERT", "UPDATE", "DELETE",
                               "READ", "CREATE", "ALTER", "REFERENCE", "DROP"};
        XResultSet result = getTables(catalog, schema, table, new String[] {"VIEW", "TABLE", "%"});
        String username = getUserName();
        XRow row = UnoRuntime.queryInterface(XRow.class, result);
        while (result.next()) {
            for (String privilege : privileges) {
                rows.add(_getPrivilegesRowSet(row, username, privilege));
            }
        }
        return new CustomResultSet(_getTablesPrivilegesMetadata(), rows);
    }

    protected CustomRowSet[] _getPrivilegesRowSet(XRow result, String username, String privilege)
            throws SQLException
    {
        CustomRowSet[] row = new CustomRowSet[7];
        row[0] = new CustomRowSet(result.getString(1), result.wasNull());
        row[1] = new CustomRowSet(result.getString(2), result.wasNull());
        row[2] = new CustomRowSet(result.getString(3), result.wasNull());
        row[3] = new CustomRowSet(null, true);
        row[4] = new CustomRowSet(username, false);
        row[5] = new CustomRowSet(privilege, false);
        row[6] = new CustomRowSet("YES", false);
        return row;
    }

    private XResultSetMetaData _getTablesPrivilegesMetadata()
    {
        CustomColumn[] columns = new CustomColumn[7];
        columns[0] = new CustomColumn();
        columns[0].setColumnName("TABLE_CAT");
        columns[0].setNullable(ColumnValue.NULLABLE);
        columns[0].setColumnDisplaySize(3);
        columns[0].setPrecision(0);
        columns[0].setScale(0);
        columns[0].setColumnType(DataType.VARCHAR);
        columns[1] = new CustomColumn();
        columns[1].setColumnName("TABLE_SCHEM");
        columns[1].setNullable(ColumnValue.NULLABLE);
        columns[1].setColumnDisplaySize(3);
        columns[1].setPrecision(0);
        columns[1].setScale(0);
        columns[1].setColumnType(DataType.VARCHAR);
        columns[2] = new CustomColumn();
        columns[2].setColumnName("TABLE_NAME");
        columns[2].setNullable(ColumnValue.NO_NULLS);
        columns[2].setColumnDisplaySize(3);
        columns[2].setPrecision(0);
        columns[2].setScale(0);
        columns[2].setColumnType(DataType.VARCHAR);
        columns[3] = new CustomColumn();
        columns[3].setColumnName("GRANTOR");
        columns[3].setNullable(ColumnValue.NULLABLE);
        columns[3].setColumnDisplaySize(0);
        columns[3].setPrecision(0);
        columns[3].setScale(0);
        columns[3].setColumnType(DataType.VARCHAR);
        columns[4] = new CustomColumn();
        columns[4].setColumnName("GRANTEE");
        columns[4].setNullable(ColumnValue.NO_NULLS);
        columns[4].setColumnDisplaySize(0);
        columns[4].setPrecision(0);
        columns[4].setScale(0);
        columns[4].setColumnType(DataType.VARCHAR);
        columns[5] = new CustomColumn();
        columns[5].setColumnName("PRIVILEGE");
        columns[5].setNullable(ColumnValue.NULLABLE);
        columns[5].setColumnDisplaySize(0);
        columns[5].setPrecision(0);
        columns[5].setScale(0);
        columns[5].setColumnType(DataType.VARCHAR);
        columns[6] = new CustomColumn();
        columns[6].setColumnName("IS_GRANTABLE");
        columns[6].setNullable(ColumnValue.NULLABLE);
        columns[6].setColumnDisplaySize(0);
        columns[6].setPrecision(0);
        columns[6].setScale(0);
        columns[6].setColumnType(DataType.VARCHAR);
        return new CustomResultSetMetaData(columns);
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


    abstract protected String _mapDatabaseTableTypes(String type);
    abstract protected String _mapDatabaseTableType(String schema, String type);


}