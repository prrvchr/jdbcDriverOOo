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

import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XOutParameters;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XRow;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class SuperCallableStatement<T>
extends SuperPreparedStatement<T>
implements XOutParameters,
           XRow
{
	private final java.sql.CallableStatement m_Statement;

	// The constructor method:
	public SuperCallableStatement(XComponentContext context,
                                  XConnection connection,
                                  java.sql.CallableStatement statement)
	{
		super(context, connection, statement);
		m_Statement = statement;
	}
	public SuperCallableStatement(XComponentContext context,
                                  XConnection connection,
                                  java.sql.CallableStatement statement,
                                  Map<String, Property> properties)
	{
		super(context, connection, statement, properties);
		m_Statement = statement;
	}

	// com.sun.star.sdbc.XOutParameters:
	@Override
	public void registerNumericOutParameter(int index, int type, int scale) throws SQLException
	{
		try
		{
			m_Statement.registerOutParameter(index, type, scale);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void registerOutParameter(int index, int type, String name) throws SQLException
	{
		try
		{
			m_Statement.registerOutParameter(index, type, name);
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
			java.sql.Array array = m_Statement.getArray(index);
			if (!m_Statement.wasNull()) value = new Array(array);
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XInputStream getBinaryStream(int index) throws SQLException
	{
		// TODO: Implement me!!!
		return null;
	}

	@Override
	public XBlob getBlob(int index) throws SQLException
	{
		try
		{
			XBlob blob = null;
			java.sql.Blob value = m_Statement.getBlob(index);
			if (!m_Statement.wasNull()) blob = new Blob(m_Statement, value);
			return blob;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public boolean getBoolean(int index) throws SQLException
	{
		try
		{
			boolean value = m_Statement.getBoolean(index);
			if (m_Statement.wasNull()) value = false;
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
			byte value = m_Statement.getByte(index);
			if (m_Statement.wasNull()) value = 0;
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
			byte[] value = m_Statement.getBytes(index);
			if (m_Statement.wasNull()) value = new byte[0];
			return value;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XInputStream getCharacterStream(int index) throws SQLException
	{
		// TODO: Implement me!!!
		return null;
	}

	@Override
	public XClob getClob(int index) throws SQLException
	{
		try
		{
			XClob clob = null;
			java.sql.Clob value = m_Statement.getClob(index);
			if (!m_Statement.wasNull()) clob = new Clob(m_Statement, value);
			return clob;
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public Date getDate(int index) throws SQLException
	{
		try
		{
			java.sql.Date value = m_Statement.getDate(index);
			Date date = null;
			if (!m_Statement.wasNull()) date = UnoHelper.getUnoDate(value);
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
			double value = m_Statement.getDouble(index);
			if (m_Statement.wasNull()) value = 0;
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
			float value = m_Statement.getFloat(index);
			if (m_Statement.wasNull()) value = 0;
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
			int value = m_Statement.getInt(index);
			if (m_Statement.wasNull()) value = 0;
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
			long value = m_Statement.getLong(index);
			if (m_Statement.wasNull()) value = 0;
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
			System.out.println("SuperCallableStatement.getObject() : '" + index + "' - '" + map + "'");
			Object value = null;
			if (!m_Statement.wasNull()) value = m_Statement.getObject(index);
			return value;
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
			short value = m_Statement.getShort(index);
			if (m_Statement.wasNull()) value = 0;
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
			String value = m_Statement.getString(index);
			if (m_Statement.wasNull()) value = "";
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
			java.sql.Time value = m_Statement.getTime(index);
			Time time = null;
			if (!m_Statement.wasNull()) time = UnoHelper.getUnoTime(value);
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
			java.sql.Timestamp value = m_Statement.getTimestamp(index);
			DateTime datetime = null;
			if (!m_Statement.wasNull()) datetime = UnoHelper.getUnoDateTime(value);
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
			return m_Statement.wasNull();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


}
