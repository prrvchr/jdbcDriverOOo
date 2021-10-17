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
package io.github.prrvchr.comp.sdbc;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.InputStreamToXInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.sdbc.XRow;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;


import io.github.prrvchr.comp.helper.UnoHelper;
import io.github.prrvchr.comp.sdbc.Array;
import io.github.prrvchr.comp.sdbc.Clob;

public abstract class SuperResultSet<T>
extends WarningsSupplierProperty
implements XCloseable,
           XColumnLocate,
           XResultSet,
           XResultSetMetaDataSupplier,
           XRow
{
	@SuppressWarnings("unused")
	private final XComponentContext m_xContext;
	private final XInterface m_xStatement;
	private final java.sql.ResultSet m_ResultSet;

	private static Map<String, Property> _getPropertySet()
	{
		Map<String, Property> map = new HashMap<String, Property>();
		short readonly = PropertyAttribute.READONLY;
		Property p1 = UnoHelper.getProperty("CursorName", "string", readonly);
		map.put(UnoHelper.getPropertyName(p1), p1);
		Property p2 = UnoHelper.getProperty("FetchDirection", "long");
		map.put(UnoHelper.getPropertyName(p2), p2);
		Property p3 = UnoHelper.getProperty("FetchSize", "long");
		map.put(UnoHelper.getPropertyName(p3), p3);
		Property p4 = UnoHelper.getProperty("ResultSetConcurrency", "long", readonly);
		map.put(UnoHelper.getPropertyName(p4), p4);
		Property p5 = UnoHelper.getProperty("ResultSetType", "long", readonly);
		map.put(UnoHelper.getPropertyName(p5), p5);
		return map;
	}
	private static Map<String, Property> _getPropertySet(Map<String, Property> properties)
	{
		Map<String, Property> map = _getPropertySet();
		map.putAll(properties);
		return map;
	}


	// The constructor method:
	public SuperResultSet(XComponentContext ctx,
                          java.sql.ResultSet resultset)
	{
		super(_getPropertySet());
		m_xContext = ctx;
		m_xStatement = null;
		m_ResultSet = resultset;
	}
	public SuperResultSet(XComponentContext ctx,
                          XInterface statement,
                          java.sql.ResultSet resultset)
	{
		super(_getPropertySet());
		m_xContext = ctx;
		m_xStatement = statement;
		m_ResultSet = resultset;
	}
	public SuperResultSet(XComponentContext ctx,
                          XInterface statement,
                          java.sql.ResultSet resultset,
                          Map<String, Property> properties)
	{
		super(_getPropertySet(properties));
		m_xContext = ctx;
		m_xStatement = statement;
		m_ResultSet = resultset;
	}
	

	public String getCursorName() throws SQLException
	{
		try
		{
			return m_ResultSet.getCursorName();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	public int getFetchDirection() throws SQLException
	{
		try
		{
			return m_ResultSet.getFetchDirection();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}
	public void setFetchDirection(int direction) throws SQLException
	{
		try
		{
			m_ResultSet.setFetchDirection(direction);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}

	public int getFetchSize() throws SQLException
	{
		try
		{
			return m_ResultSet.getFetchSize();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}
	public void setFetchSize(int size) throws SQLException
	{
		try
		{
			m_ResultSet.setFetchSize(size);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}
	
	public int getResultSetConcurrency() throws SQLException
	{
		try
		{
			return m_ResultSet.getConcurrency();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}

	public int getResultSetType() throws SQLException
	{
		try
		{
			return m_ResultSet.getType();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		
	}

	// com.sun.star.sdbc.XCloseable
	@Override
	public void close() throws SQLException
	{
		try
		{
			m_ResultSet.close();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XColumnLocate:
	@Override
	public int findColumn(String name) throws SQLException
	{
		try
		{
			return m_ResultSet.findColumn(name);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XResultSet:
	@Override
	public boolean absolute(int row) throws SQLException
	{
		try
		{
			return m_ResultSet.absolute(row);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void afterLast() throws SQLException
	{
		try
		{
			m_ResultSet.afterLast();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void beforeFirst() throws SQLException
	{
		try
		{
			m_ResultSet.beforeFirst();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}

	}

	@Override
	public boolean first() throws SQLException
	{
		try
		{
			return m_ResultSet.first();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getRow() throws SQLException
	{
		try
		{
			return m_ResultSet.getRow();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public com.sun.star.uno.XInterface getStatement() throws SQLException
	{
		return m_xStatement;
	}

	@Override
	public boolean isAfterLast() throws SQLException
	{
		try
		{
			return m_ResultSet.isAfterLast();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean isBeforeFirst() throws SQLException
	{
		try
		{
			return m_ResultSet.isBeforeFirst();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean isFirst() throws SQLException
	{
		try
		{
			return m_ResultSet.isFirst();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean isLast() throws SQLException
	{
		try
		{
			return m_ResultSet.isLast();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean last() throws SQLException
	{
		try
		{
			return m_ResultSet.last();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean next() throws SQLException
	{
		try
		{
			return m_ResultSet.next();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean previous() throws SQLException
	{
		try
		{
			return m_ResultSet.previous();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void refreshRow() throws SQLException
	{
		try
		{
			m_ResultSet.refreshRow();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean relative(int row) throws SQLException
	{
		try
		{
			return m_ResultSet.relative(row);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean rowDeleted() throws SQLException
	{
		try
		{
			return m_ResultSet.rowDeleted();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean rowInserted() throws SQLException
	{
		try
		{
			return m_ResultSet.rowInserted();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean rowUpdated() throws SQLException
	{
		try
		{
			return m_ResultSet.rowUpdated();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XResultSetMetaDataSupplier:
	@Override
	public XResultSetMetaData getMetaData() throws SQLException
	{
		try
		{
			java.sql.ResultSetMetaData metadata = m_ResultSet.getMetaData();
			return new ResultSetMetaData(metadata);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XRow:
	@Override
	public XArray getArray(int index) throws SQLException
	{
		try
		{
			XArray value = null;
			java.sql.Array array = m_ResultSet.getArray(index);
			if (!m_ResultSet.wasNull()) value = new Array(array);
			return value;
		} catch (java.sql.SQLException e) {
			throw UnoHelper.getSQLException(e, this);
		} 
	}

	@Override
	public XInputStream getBinaryStream(int index) throws SQLException
	{
		try
		{
			InputStream input = m_ResultSet.getBinaryStream(index);
			return new InputStreamToXInputStreamAdapter(input);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XBlob getBlob(int index) throws SQLException
	{
		try
		{
			XBlob value = null;
			java.sql.Blob blob = m_ResultSet.getBlob(index);
			if (!m_ResultSet.wasNull())
			{
				java.sql.Statement statement = m_ResultSet.getStatement();
				value = new Blob(statement, blob);
			}
			return value;
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean getBoolean(int index) throws SQLException
	{
		try
		{
			boolean value = m_ResultSet.getBoolean(index);
			if (m_ResultSet.wasNull()) value = false;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public byte getByte(int index) throws SQLException
	{
		try
		{
			byte value = m_ResultSet.getByte(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public byte[] getBytes(int index) throws SQLException
	{
		try
		{
			byte[] value = m_ResultSet.getBytes(index);
			if (m_ResultSet.wasNull()) value = new byte[0];
			return value;
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XInputStream getCharacterStream(int index) throws SQLException
	{
		try
		{
			InputStream input = m_ResultSet.getAsciiStream(index);
			return new InputStreamToXInputStreamAdapter(input);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XClob getClob(int index) throws SQLException
	{
		try
		{
			XClob value = null;
			java.sql.Clob clob = m_ResultSet.getClob(index);
			if (!m_ResultSet.wasNull())
			{
				java.sql.Statement statement = m_ResultSet.getStatement();
				value = new Clob(statement, clob);
			}
			return value;
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public Date getDate(int index) throws SQLException
	{
		try
		{
			java.sql.Date value = m_ResultSet.getDate(index);
			Date date = new Date();
			if (!m_ResultSet.wasNull()) date = UnoHelper.getUnoDate(date, value);
			return date;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public double getDouble(int index) throws SQLException
	{
		try
		{
			double value = m_ResultSet.getDouble(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public float getFloat(int index) throws SQLException
	{
		try
		{
			float value = m_ResultSet.getFloat(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int getInt(int index) throws SQLException
	{
		try
		{
			int value = m_ResultSet.getInt(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public long getLong(int index) throws SQLException
	{
		try
		{
			long value = m_ResultSet.getLong(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public Object getObject(int index, XNameAccess map) throws SQLException
	{
		try
		{
			return m_ResultSet.getObject(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XRef getRef(int index) throws SQLException
	{
		// TODO: Implement me!!!
		return null;
	}

	@Override
	public short getShort(int index) throws SQLException
	{
		try
		{
			short value = m_ResultSet.getShort(index);
			if (m_ResultSet.wasNull()) value = 0;
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public String getString(int index) throws SQLException
	{
		try
		{
			String value = m_ResultSet.getString(index);
			if (m_ResultSet.wasNull()) value = "";
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public Time getTime(int index) throws SQLException
	{
		try
		{
			java.sql.Time value = m_ResultSet.getTime(index);
			Time time = new Time();
			if (!m_ResultSet.wasNull()) time = UnoHelper.getUnoTime(time, value);
			return time;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public DateTime getTimestamp(int index) throws SQLException
	{
		try
		{
			java.sql.Timestamp value = m_ResultSet.getTimestamp(index);
			DateTime datetime = new DateTime();
			if (!m_ResultSet.wasNull()) datetime = UnoHelper.getUnoDateTime(datetime, value);
			return datetime;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean wasNull() throws SQLException
	{
		try
		{
			return m_ResultSet.wasNull();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


}
