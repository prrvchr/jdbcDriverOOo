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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class BaseStatement
extends SuperStatement
implements XBatchExecution,
		   XStatement
{
	private XConnection m_xConnection;
	private java.sql.Statement m_Statement;
	public boolean m_EscapeProcessing = true;

	private static Map<String, Property> _getPropertySet()
	{
		Map<String, Property> map = new HashMap<String, Property>();
		Property p1 = UnoHelper.getProperty("EscapeProcessing", "boolean");
		map.put(UnoHelper.getPropertyName(p1), p1);
		return map;
	}
	private static Map<String, Property> _getPropertySet(Map<String, Property> properties)
	{
		Map<String, Property> map = _getPropertySet();
		map.putAll(properties);
		return map;
	}


	// The constructor method:
	public BaseStatement(XComponentContext context,
						 String name,
						 String[] services,
						 BaseConnection connection,
						 java.sql.Statement statement,
						 String type)
	{
		super(context, name, services, connection, statement, type, _getPropertySet());
		m_xConnection = connection;
		m_Statement = statement;
	}
	public BaseStatement(XComponentContext context,
						 String name,
						 String[] services,
						 BaseConnection connection,
						 java.sql.Statement statement,
						 String type,
						 Map<String, Property> properties)
	{
		super(context, name, services, connection, statement, type, _getPropertySet(properties));
		m_xConnection = connection;
		m_Statement = statement;
	}


	public boolean getEscapeProcessing()
	{
		return m_EscapeProcessing;
	}
	public void setEscapeProcessing(boolean value) throws SQLException
	{
		m_EscapeProcessing = value;
		try {
			m_Statement.setEscapeProcessing(value);
		} catch (java.sql.SQLException e) {
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XBatchExecution
	@Override
	public void addBatch(String sql) throws SQLException {
		try
		{
			m_Statement.addBatch(sql);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void clearBatch() throws SQLException {
		try
		{
			m_Statement.clearBatch();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int[] executeBatch() throws SQLException {
		try
		{
			return m_Statement.executeBatch();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XStatement:
	@Override
	public boolean execute(String sql) throws SQLException
	{
		try
		{
			System.out.println("BaseStatement.execute() 1 Query: " + sql);
			return m_Statement.execute(sql);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet executeQuery(String sql) throws SQLException
	{
		try
		{
			System.out.println("BaseStatement.executeQuery() 1 Query: " + sql);
			java.sql.ResultSet resultset = m_Statement.executeQuery(sql);
			return _getResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int executeUpdate(String sql) throws SQLException
	{
		try
		{
			System.out.println("BaseStatement.executeUpdate() 1 Query: " + sql);
			return m_Statement.executeUpdate(sql);
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


	abstract protected XResultSet _getResultSet(XComponentContext ctx,
												java.sql.ResultSet resultset)
	throws java.sql.SQLException;


}
