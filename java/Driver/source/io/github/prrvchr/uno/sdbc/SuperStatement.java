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
	private int m_FetchDirection;
	private int m_FetchSize;
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
		m_CursorName = cursor;
	}
	public String getCursorName()
	{
		return m_CursorName;
	}
	public void setFetchDirection(int value)
	{
		m_FetchDirection = value;
	}
	public int getFetchDirection()
	{
		return m_FetchDirection;
	}
	public void setFetchSize(int value)
	{
		m_FetchSize = value;
	}
	public int getFetchSize()
	{
		return m_FetchSize;
	}
	public void setMaxFieldSize(int value)
	{
		m_MaxFieldSize = value;
	}
	public int getMaxFieldSize()
	{
		return m_MaxFieldSize;
	}
	public void setMaxRows(int value)
	{
		m_MaxRows = value;
	}
	public int getMaxRows()
	{
		return m_MaxRows;
	}
	public void setQueryTimeout(int value)
	{
		m_QueryTimeout = value;
	}
	public int getQueryTimeout()
	{
		return m_QueryTimeout;
	}
	public void setResultSetConcurrency(int value)
	{
		m_ResultSetConcurrency = value;
	}
	public int getResultSetConcurrency()
	{
		return m_ResultSetConcurrency;
	}
	public void setResultSetType(int value)
	{
		System.out.println("SuperStatement.setResultSetType(): " + value);
		m_ResultSetType = value;
	}
	public int getResultSetType()
	{
		System.out.println("SuperStatement.getResultSetType(): " + m_ResultSetType);
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


}
