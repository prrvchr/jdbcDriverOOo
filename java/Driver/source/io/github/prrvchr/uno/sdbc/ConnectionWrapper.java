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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


public class ConnectionWrapper
	implements Connection
{
	private Connection m_Connection;

	// The constructor method:
	public ConnectionWrapper(Connection connection)
	{
		m_Connection = connection;
	}


	// java.sql.Connection:
	@Override
	public boolean isWrapperFor(Class<?> clazz) throws SQLException {
		return m_Connection.isWrapperFor(clazz);
	}

	@Override
	public <T> T unwrap(Class<T> clazz) throws SQLException {
		return m_Connection.unwrap(clazz);
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		m_Connection.abort(executor);
	}

	@Override
	public void clearWarnings() throws SQLException {
		m_Connection.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		m_Connection.close();
	}

	@Override
	public void commit() throws SQLException {
		m_Connection.commit();
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return m_Connection.createArrayOf(arg0, arg1);
	}

	@Override
	public Blob createBlob() throws SQLException {
		return m_Connection.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		return m_Connection.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return m_Connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return m_Connection.createSQLXML();
	}

	@Override
	public Statement createStatement() throws SQLException {
		return m_Connection.createStatement();
	}

	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return m_Connection.createStatement(arg0, arg1);
	}

	@Override
	public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
		return m_Connection.createStatement(arg0, arg1, arg2);
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return m_Connection.createStruct(arg0, arg1);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return m_Connection.getAutoCommit();
	}

	@Override
	public String getCatalog() throws SQLException {
		return m_Connection.getCatalog();
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return m_Connection.getClientInfo();
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return m_Connection.getClientInfo(name);
	}

	@Override
	public int getHoldability() throws SQLException {
		return m_Connection.getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return m_Connection.getMetaData();
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return m_Connection.getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return m_Connection.getSchema();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return m_Connection.getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		System.out.println("ConnectionWrapper.getTypeMap() 1");
		Map<String, Class<?>> map = m_Connection.getTypeMap();
		System.out.println("ConnectionWrapper.getTypeMap() 2: " + map);
		//map = TypeMap.getDefaultTypeMap();
		//System.out.println("ConnectionWrapper.getTypeMap() 3: " + map);
		return map;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return m_Connection.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return m_Connection.isClosed();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return m_Connection.isReadOnly();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return m_Connection.isValid(timeout);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return m_Connection.nativeSQL(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return m_Connection.prepareCall(sql);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
		return m_Connection.prepareCall(arg0, arg1, arg2);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return m_Connection.prepareCall(arg0, arg1, arg2, arg3);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return m_Connection.prepareStatement(sql);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
		return m_Connection.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
		return m_Connection.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
		return m_Connection.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
		return m_Connection.prepareStatement(arg0, arg1, arg2);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return m_Connection.prepareStatement(arg0, arg1, arg2, arg3);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		m_Connection.releaseSavepoint(savepoint);
	}

	@Override
	public void rollback() throws SQLException {
		m_Connection.rollback();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		m_Connection.rollback(savepoint);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		m_Connection.setAutoCommit(autoCommit);
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		m_Connection.setCatalog(catalog);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		m_Connection.setClientInfo(properties);
	}

	@Override
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		m_Connection.setClientInfo(arg0, arg1);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		m_Connection.setHoldability(holdability);
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		m_Connection.setNetworkTimeout(arg0, arg1);
	}

	@Override
	public void setReadOnly(boolean readonly) throws SQLException {
		m_Connection.setReadOnly(readonly);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return m_Connection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return m_Connection.setSavepoint(name);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		m_Connection.setSchema(schema);
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		m_Connection.setTransactionIsolation(level);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		m_Connection.setTypeMap(map);
	}


}
