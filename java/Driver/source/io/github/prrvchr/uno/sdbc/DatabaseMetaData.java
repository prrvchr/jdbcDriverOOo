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

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData2;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class DatabaseMetaData
extends WeakBase
implements XDatabaseMetaData2
{
	private final XComponentContext m_xContext;
	private final XConnection m_xConnection;
	private final java.sql.DatabaseMetaData m_Metadata;
	private final String m_url;
	private final PropertyValue[] m_info;

	// The constructor method:
	public DatabaseMetaData(XComponentContext ctx,
							XConnection connection,
							java.sql.DatabaseMetaData metadata,
							PropertyValue[] info,
							String url)
	{
		m_xContext = ctx;
		m_xConnection = connection;
		m_Metadata = metadata;
		m_info = info;
		m_url = url;
	}


	// com.sun.star.sdbc.XDatabaseMetaData2:
	@Override
	public PropertyValue[] getConnectionInfo()
	{
		return m_info;
	}
	
	@Override
	public String getURL() throws SQLException
	{
		return m_url;
	}
	
	@Override
	public boolean allProceduresAreCallable() throws SQLException
	{
		try
		{
			return m_Metadata.allProceduresAreCallable();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException
	{
		try
		{
			return m_Metadata.allTablesAreSelectable();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException
	{
		try
		{
			return m_Metadata.dataDefinitionCausesTransactionCommit();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean dataDefinitionIgnoredInTransactions() throws SQLException
	{
		try
		{
			return m_Metadata.dataDefinitionIgnoredInTransactions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean deletesAreDetected(int type) throws SQLException
	{
		try
		{
			return m_Metadata.deletesAreDetected(type);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
	{
		try
		{
			return m_Metadata.doesMaxRowSizeIncludeBlobs();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getBestRowIdentifier(Object catalog, String arg1, String arg2, int arg3, boolean arg4)
			throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset =  m_Metadata.getBestRowIdentifier(c, arg1, arg2, arg3, arg4);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getCatalogSeparator() throws SQLException
	{
		try
		{
			return m_Metadata.getCatalogSeparator();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getCatalogTerm() throws SQLException
	{
		try
		{
			return m_Metadata.getCatalogTerm();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getCatalogs() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = m_Metadata.getCatalogs();
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getColumnPrivileges(Object catalog, String arg1, String arg2, String arg3) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getColumnPrivileges(c, arg1, arg2, arg3);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getColumns(Object catalog, String arg1, String arg2, String arg3) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getColumns(c, arg1, arg2, arg3);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XConnection getConnection() throws SQLException
	{
		return m_xConnection;
	}

	@Override
	public XResultSet getCrossReference(Object arg0, String arg1, String arg2, Object arg3, String arg4, String arg5)
			throws SQLException
	{
		try
		{
			String c0 = UnoHelper.getObjectString(arg0);
			String c1 = UnoHelper.getObjectString(arg3);
			java.sql.ResultSet resultset = m_Metadata.getCrossReference(c0, arg1, arg2, c1, arg4, arg5);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getDatabaseProductName() throws SQLException
	{
		try
		{
			return m_Metadata.getDatabaseProductName();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getDatabaseProductVersion() throws SQLException
	{
		try
		{
			return m_Metadata.getDatabaseProductVersion();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException
	{
		try
		{
			return m_Metadata.getDefaultTransactionIsolation();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
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
		try
		{
			return m_Metadata.getDriverName();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getDriverVersion() throws SQLException
	{
		try
		{
			return m_Metadata.getDriverVersion();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getExportedKeys(Object catalog, String arg1, String arg2) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getExportedKeys(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getExtraNameCharacters() throws SQLException
	{
		try
		{
			return m_Metadata.getExtraNameCharacters();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getIdentifierQuoteString() throws SQLException
	{
		try
		{
			return m_Metadata.getIdentifierQuoteString();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}
	
	@Override
	public XResultSet getImportedKeys(Object catalog, String arg1, String arg2) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getImportedKeys(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getIndexInfo(Object catalog, String arg1, String arg2, boolean arg3, boolean arg4)
	{
		java.sql.ResultSet resultset = null;
		try 
		{
			String c = UnoHelper.getObjectString(catalog);
			resultset = m_Metadata.getIndexInfo(c, arg1, arg2, arg3, arg4);
		} catch (java.sql.SQLException e)
		{
			e.printStackTrace();
		}
		return new ResultSet(m_xContext, resultset);

	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxBinaryLiteralLength();
		} catch (java.sql.SQLException e)
	{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxCatalogNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxCharLiteralLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnsInGroupBy();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnsInIndex();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnsInOrderBy();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnsInSelect();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxColumnsInTable();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxConnections() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxConnections();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxCursorNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxIndexLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxIndexLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxProcedureNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxRowSize() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxRowSize();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxSchemaNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxStatementLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxStatementLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxStatements() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxStatements();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxTableNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxTableNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxTablesInSelect();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getMaxUserNameLength() throws SQLException
	{
		try
		{
			return m_Metadata.getMaxUserNameLength();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getNumericFunctions() throws SQLException
	{
		try
		{
			return m_Metadata.getNumericFunctions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getPrimaryKeys(Object catalog, String arg1, String arg2) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getPrimaryKeys(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getProcedureColumns(Object catalog, String arg1, String arg2, String arg3) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getProcedureColumns(c, arg1, arg2, arg3);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getProcedureTerm() throws SQLException
	{
		try
		{
			return m_Metadata.getProcedureTerm();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getProcedures(Object catalog, String arg1, String arg2) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getProcedures(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getSQLKeywords() throws SQLException
	{
		try
		{
			return m_Metadata.getSQLKeywords();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getSchemaTerm() throws SQLException
	{
		try
		{
			return m_Metadata.getSchemaTerm();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getSchemas() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = m_Metadata.getSchemas();
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getSearchStringEscape() throws SQLException
	{
		try
		{
			return m_Metadata.getSearchStringEscape();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getStringFunctions() throws SQLException
	{
		try
		{
			return m_Metadata.getStringFunctions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getSystemFunctions() throws SQLException
	{
		try
		{
			return m_Metadata.getSystemFunctions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getTablePrivileges(Object catalog, String arg1, String arg2) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getTablePrivileges(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getTableTypes() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = m_Metadata.getTableTypes();
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getTables(Object catalog, String arg1, String arg2, String[] arg3) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getTables(c, arg1, arg2, arg3);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getTimeDateFunctions() throws SQLException
	{
		try
		{
			return m_Metadata.getTimeDateFunctions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getTypeInfo() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = m_Metadata.getTypeInfo();
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getUDTs(Object catalog, String arg1, String arg2, int[] arg3) throws SQLException
	{
		try
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getUDTs(c, arg1, arg2, arg3);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getUserName() throws SQLException
	{
		try
		{
			return m_Metadata.getUserName();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getVersionColumns(Object catalog, String arg1, String arg2) throws SQLException
	{
		try 
		{
			String c = UnoHelper.getObjectString(catalog);
			java.sql.ResultSet resultset = m_Metadata.getVersionColumns(c, arg1, arg2);
			return new ResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean insertsAreDetected(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.insertsAreDetected(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException
	{
		try
		{
			return m_Metadata.isCatalogAtStart();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean isReadOnly() throws SQLException
	{
		try
		{
			return m_Metadata.isReadOnly();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean nullPlusNonNullIsNull() throws SQLException
	{
		try
		{
			return m_Metadata.nullPlusNonNullIsNull();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean nullsAreSortedAtEnd() throws SQLException
	{
		try
		{
			return m_Metadata.nullsAreSortedAtEnd();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean nullsAreSortedAtStart() throws SQLException
	{
		try
		{
			return m_Metadata.nullsAreSortedAtStart();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean nullsAreSortedHigh() throws SQLException
	{
		try
		{
			return m_Metadata.nullsAreSortedHigh();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean nullsAreSortedLow() throws SQLException
	{
		try
		{
			return m_Metadata.nullsAreSortedLow();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean othersDeletesAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.othersDeletesAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean othersInsertsAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.othersInsertsAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean othersUpdatesAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.othersUpdatesAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean ownDeletesAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.ownDeletesAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean ownInsertsAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.ownInsertsAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean ownUpdatesAreVisible(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.ownUpdatesAreVisible(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesLowerCaseIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesLowerCaseQuotedIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesMixedCaseIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesMixedCaseQuotedIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesUpperCaseIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.storesUpperCaseQuotedIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException
	{
		try
		{
			return m_Metadata.supportsANSI92EntryLevelSQL();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException
	{
		try
		{
			return m_Metadata.supportsANSI92FullSQL();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException
	{
		try
		{
			return m_Metadata.supportsANSI92IntermediateSQL();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsAlterTableWithAddColumn() throws SQLException
	{
		try
		{
			return m_Metadata.supportsAlterTableWithAddColumn();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsAlterTableWithDropColumn() throws SQLException
	{
		try
		{
			return m_Metadata.supportsAlterTableWithDropColumn();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsBatchUpdates() throws SQLException
	{
		try
		{
			return m_Metadata.supportsBatchUpdates();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCatalogsInDataManipulation();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCatalogsInIndexDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCatalogsInPrivilegeDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCatalogsInProcedureCalls();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCatalogsInTableDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsColumnAliasing() throws SQLException
	{
		try
		{
			return m_Metadata.supportsColumnAliasing();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsConvert(int arg0, int arg1) throws SQLException
	{
		try
		{
			return m_Metadata.supportsConvert();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCoreSQLGrammar();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException
	{
		try
		{
			return m_Metadata.supportsCorrelatedSubqueries();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsDataDefinitionAndDataManipulationTransactions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException
	{
		try
		{
			return m_Metadata.supportsDataManipulationTransactionsOnly();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException
	{
		try
		{
			return m_Metadata.supportsDifferentTableCorrelationNames();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException
	{
		try
		{
			return m_Metadata.supportsExpressionsInOrderBy();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException
	{
		try
		{
			return m_Metadata.supportsExtendedSQLGrammar();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsFullOuterJoins() throws SQLException
	{
		try
		{
			return m_Metadata.supportsFullOuterJoins();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsGroupBy() throws SQLException
	{
		try
		{
			return m_Metadata.supportsGroupBy();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsGroupByBeyondSelect() throws SQLException
	{
		try
		{
			return m_Metadata.supportsGroupByBeyondSelect();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsGroupByUnrelated() throws SQLException
	{
		try
		{
			return m_Metadata.supportsGroupByUnrelated();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsIntegrityEnhancementFacility() throws SQLException
	{
		try
		{
			return m_Metadata.supportsIntegrityEnhancementFacility();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsLikeEscapeClause() throws SQLException
	{
		try
		{
			return m_Metadata.supportsLikeEscapeClause();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsLimitedOuterJoins() throws SQLException
	{
		try
		{
			return m_Metadata.supportsLimitedOuterJoins();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException
	{
		try
		{
			return m_Metadata.supportsMinimumSQLGrammar();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.supportsMixedCaseIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
	{
		try
		{
			return m_Metadata.supportsMixedCaseQuotedIdentifiers();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsMultipleResultSets() throws SQLException
	{
		try
		{
			return m_Metadata.supportsMultipleResultSets();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsMultipleTransactions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsMultipleTransactions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsNonNullableColumns() throws SQLException
	{
		try
		{
			return m_Metadata.supportsNonNullableColumns();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOpenCursorsAcrossCommit() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOpenCursorsAcrossCommit();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOpenCursorsAcrossRollback() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOpenCursorsAcrossRollback();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOpenStatementsAcrossCommit() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOpenStatementsAcrossCommit();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOpenStatementsAcrossRollback() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOpenStatementsAcrossRollback();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOrderByUnrelated() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOrderByUnrelated();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsOuterJoins() throws SQLException
	{
		try
		{
			return m_Metadata.supportsOuterJoins();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsPositionedDelete() throws SQLException
	{
		try
		{
			return m_Metadata.supportsPositionedDelete();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsPositionedUpdate() throws SQLException
	{
		try
		{
			return m_Metadata.supportsPositionedUpdate();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsResultSetConcurrency(int arg0, int arg1) throws SQLException
	{
		try
		{
			return m_Metadata.supportsResultSetConcurrency (arg0, arg1);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsResultSetType(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.supportsResultSetType(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSchemasInDataManipulation();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSchemasInIndexDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSchemasInPrivilegeDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSchemasInProcedureCalls();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSchemasInTableDefinitions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSelectForUpdate() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSelectForUpdate();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsStoredProcedures() throws SQLException
	{
		try
		{
			return m_Metadata.supportsStoredProcedures();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSubqueriesInComparisons();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSubqueriesInExists() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSubqueriesInExists();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSubqueriesInIns() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSubqueriesInIns();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsSubqueriesInQuantifieds() throws SQLException
	{
		try
		{
			return m_Metadata.supportsSubqueriesInQuantifieds();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsTableCorrelationNames() throws SQLException
	{
		try
		{
			return m_Metadata.supportsTableCorrelationNames();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.supportsTransactionIsolationLevel(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsTransactions() throws SQLException
	{
		try
		{
			return m_Metadata.supportsTransactions();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsTypeConversion() throws SQLException
	{
		return false;
	}

	@Override
	public boolean supportsUnion() throws SQLException
	{
		try
		{
			return m_Metadata.supportsUnion();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean supportsUnionAll() throws SQLException
	{
		try
		{
			return m_Metadata.supportsUnionAll();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean updatesAreDetected(int arg0) throws SQLException
	{
		try
		{
			return m_Metadata.updatesAreDetected(arg0);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean usesLocalFilePerTable() throws SQLException
	{
		try
		{
			return m_Metadata.usesLocalFilePerTable();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean usesLocalFiles() throws SQLException
	{
		try
		{
			return m_Metadata.usesLocalFiles();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


}