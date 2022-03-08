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
import com.sun.star.sdbc.XOutParameters;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XRow;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class SuperCallableStatement
extends SuperPreparedStatement
implements XOutParameters,
           XRow
{

	// The constructor method:
	public SuperCallableStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection xConnection)
	{
		super(context, name, services, xConnection);
	}
	public SuperCallableStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection xConnection,
								  Map<String, Property> properties)
	{
		super(context, name, services, xConnection, properties);
	}

	// com.sun.star.sdbc.XOutParameters:
	@Override
	public void registerNumericOutParameter(int index, int type, int scale) throws SQLException
	{
		try
		{
			this._getStatement().registerOutParameter(index, type, scale);
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
			this._getStatement().registerOutParameter(index, type, name);
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Array array = statement.getArray(index);
			if (!statement.wasNull()) value = new Array(array);
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Blob value = statement.getBlob(index);
			if (!statement.wasNull()) blob = new Blob(statement, value);
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
			java.sql.CallableStatement statement = this._getStatement();
			boolean value = statement.getBoolean(index);
			if (statement.wasNull()) value = false;
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
			java.sql.CallableStatement statement = this._getStatement();
			byte value = statement.getByte(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			byte[] value = statement.getBytes(index);
			if (statement.wasNull()) value = new byte[0];
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Clob value = statement.getClob(index);
			if (!statement.wasNull()) clob = new Clob(statement, value);
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Date value = statement.getDate(index);
			Date date = null;
			if (!statement.wasNull()) date = UnoHelper.getUnoDate(value);
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
			java.sql.CallableStatement statement = this._getStatement();
			double value = statement.getDouble(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			float value = statement.getFloat(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			int value = statement.getInt(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			long value = statement.getLong(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			Object value =  statement.getObject(index);
			//if (!statement.wasNull()) value = statement.getObject(index);
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
			java.sql.CallableStatement statement = this._getStatement();
			short value = statement.getShort(index);
			if (statement.wasNull()) value = 0;
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
			java.sql.CallableStatement statement = this._getStatement();
			String value = statement.getString(index);
			if (statement.wasNull()) value = "";
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Time value = statement.getTime(index);
			Time time = null;
			if (!statement.wasNull()) time = UnoHelper.getUnoTime(value);
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
			java.sql.CallableStatement statement = this._getStatement();
			java.sql.Timestamp value = statement.getTimestamp(index);
			DateTime datetime = null;
			if (!statement.wasNull()) datetime = UnoHelper.getUnoDateTime(value);
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
			return this._getStatement().wasNull();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	abstract protected java.sql.CallableStatement _getStatement();


}
