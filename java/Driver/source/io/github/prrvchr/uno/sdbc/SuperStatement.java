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
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XMultipleResults;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class SuperStatement
extends WarningsSupplierProperty<java.sql.Statement>
implements XCancellable,
		   XCloseable,
		   XMultipleResults
{
	public final XComponentContext m_xContext;
	private String m_CursorName = "";
	private int m_FetchDirection = java.sql.ResultSet.FETCH_FORWARD;
	private int m_FetchSize = 0;
	private int m_MaxFieldSize = 0;
	private int m_MaxRows = 0;
	private int m_QueryTimeout = 0;
	private int m_ResultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY;
	private int m_ResultSetType = java.sql.ResultSet.TYPE_FORWARD_ONLY;


	private static Map<String, Property> _getPropertySet()
	{
		Map<String, Property> map = new HashMap<String, Property>();
		map.put("CursorName", UnoHelper.getProperty("CursorName", "string"));
		map.put("FetchDirection", UnoHelper.getProperty("FetchDirection", "long"));
		map.put("FetchSize", UnoHelper.getProperty("FetchSize", "long"));
		map.put("MaxFieldSize", UnoHelper.getProperty("MaxFieldSize", "long"));
		map.put("MaxRows", UnoHelper.getProperty("MaxRows", "long"));		
		map.put("QueryTimeout", UnoHelper.getProperty("QueryTimeout", "long"));
		map.put("ResultSetConcurrency", UnoHelper.getProperty("ResultSetConcurrency", "long"));
		map.put("ResultSetType", UnoHelper.getProperty("ResultSetType", "long"));
		return map;
	}
	private static Map<String, Property> _getPropertySet(Map<String, Property> properties)
	{
		Map<String, Property> map = _getPropertySet();
		map.putAll(properties);
		return map;
	}


	// The constructor method:
	public SuperStatement(XComponentContext context,
						  String name,
						  String[] services,
						  Map<String, Property> properties)
	{
		super(name, services, _getPropertySet(properties));
		m_xContext = context;
	}
	public SuperStatement(XComponentContext context,
						  String name,
						  String[] services)
	{
		super(name, services, _getPropertySet());
		m_xContext = context;
	}

	public void setCursorName(String cursor)
	{
		if (_getWrapper() == null)
		{
			m_CursorName = cursor;
		}
		else
		{
			try {
				_getWrapper().setCursorName(cursor);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public String getCursorName()
	{
		return m_CursorName;
	}

	public void setFetchDirection(int value)
	{
		if (_getWrapper() == null)
		{
			m_FetchDirection = value;
		}
		else
		{
			try {
				_getWrapper().setFetchDirection(value);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int getFetchDirection()
	{
		if (_getWrapper() == null)
		{
			return m_FetchDirection;
		}
		else
		{
			try {
				return _getWrapper().getFetchDirection();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_FetchDirection;
			}
		}
	}

	public void setFetchSize(int value)
	{
		if (_getWrapper() == null)
		{
			m_FetchSize = value;
		}
		else
		{
			try {
				_getWrapper().setFetchSize(value);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int getFetchSize()
	{
		if (_getWrapper() == null)
		{
			return m_FetchSize;
		}
		else
		{
			try {
				return _getWrapper().getFetchSize();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_FetchSize;
			}
		}
	}

	public void setMaxFieldSize(int value)
	{
		if (_getWrapper() == null)
		{
			m_MaxFieldSize = value;
		}
		else
		{
			try {
				_getWrapper().setMaxFieldSize(value);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public int getMaxFieldSize()
	{
		if (_getWrapper() == null)
		{
			return m_MaxFieldSize;
		}
		else
		{
			try {
				return _getWrapper().getMaxFieldSize();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_MaxFieldSize;
			}
		}
	}
	public void setMaxRows(int value)
	{
		if (_getWrapper() == null)
		{
			m_MaxRows = value;
		}
		else
		{
			try {
				_getWrapper().setMaxRows(value);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int getMaxRows()
	{
		if (_getWrapper() == null)
		{
			return m_MaxRows;
		}
		else
		{
			try {
				return _getWrapper().getMaxRows();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_MaxRows;
			}
		}
	}
	public void setQueryTimeout(int value)
	{
		if (_getWrapper() == null)
		{
			m_QueryTimeout = value;
		}
		else
		{
			try {
				_getWrapper().setQueryTimeout(value);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int getQueryTimeout()
	{
		if (_getWrapper() == null)
		{
			return m_QueryTimeout;
		}
		else
		{
			try {
				return _getWrapper().getQueryTimeout();
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return m_QueryTimeout;
			}
		}
	}

	public void setResultSetConcurrency(int value)
	{
		System.out.println("SuperStatement.setResultSetConcurrency() Value: " + value);
		m_ResultSetConcurrency = value;
	}
	public int getResultSetConcurrency()
	{
		return m_ResultSetConcurrency;
	}

	public void setResultSetType(int value)
	{
		System.out.println("SuperStatement.setResultSetType() Value: " + value);
		m_ResultSetType = value;
	}
	public int getResultSetType()
	{
		return m_ResultSetType;
	}

	// com.sun.star.util.XCancellable:
	@Override
	public void cancel()
	{
		try
		{
			this._getStatement().cancel();
		} catch (java.sql.SQLException e)
		{
			// pass
		}
	}

	// com.sun.star.sdbc.XCloseable
	@Override
	public void close() throws SQLException
	{
		try
		{
			this._getStatement().close();
		} catch (java.sql.SQLException e)
		{
			// pass
		}
	}


	// com.sun.star.sdbc.XMultipleResults:
	@Override
	public boolean getMoreResults() throws SQLException
	{
		try
		{
			return this._getStatement().getMoreResults();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet getResultSet() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = this._getStatement().getResultSet();
			return new ResultSet(m_xContext, this, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getUpdateCount() throws SQLException
	{
		try
		{
			return this._getStatement().getUpdateCount();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	protected abstract java.sql.Statement _getStatement();


	protected void _setStatement(java.sql.Statement statement)
	throws java.sql.SQLException
	{
		if (m_CursorName != "")
			statement.setCursorName(m_CursorName);
		statement.setFetchDirection(m_FetchDirection);
		statement.setFetchSize(m_FetchSize);
		statement.setMaxFieldSize(m_MaxFieldSize);
		statement.setMaxRows(m_MaxRows);
		statement.setQueryTimeout(m_QueryTimeout);
	}


}
