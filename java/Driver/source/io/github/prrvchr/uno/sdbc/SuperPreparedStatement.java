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

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XParameters;
import com.sun.star.sdbc.XPreparedBatchExecution;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class SuperPreparedStatement
extends SuperStatement
implements XParameters,
		   XPreparedBatchExecution,
		   XPreparedStatement,
		   XResultSetMetaDataSupplier
{
	private XConnection m_xConnection;


	// The constructor method:
	public SuperPreparedStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection xConnection)
	{
		super(context, name, services);
		m_xConnection = xConnection;
	}

	public SuperPreparedStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection xConnection,
								  Map<String, Property> properties)
	{
		super(context, name, services, properties);
		m_xConnection = xConnection;
	}


	// com.sun.star.sdbc.XParameters:
	@Override
	public void clearParameters() throws SQLException
	{
		try
		{
			this._getStatement().clearParameters();
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setArray(int index, XArray value) throws SQLException
	{
		try
		{
			java.sql.PreparedStatement statement = this._getStatement();
			java.sql.Array array =  UnoHelper.getSQLArray(statement, value);
			statement.setArray(index, array);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setBinaryStream(int index, XInputStream value, int lenght) throws SQLException
	{
		try
		{
			InputStream input = new XInputStreamToInputStreamAdapter(value);
			this._getStatement().setBinaryStream(index, input, lenght);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setBlob(int index, XBlob value) throws SQLException
	{
		try
		{
			java.sql.PreparedStatement statement = this._getStatement();
			java.sql.Blob blob = UnoHelper.getSQLBlob(statement, value);
			statement.setBlob(index, blob);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setBoolean(int index, boolean value) throws SQLException
	{
		try
		{
			this._getStatement().setBoolean(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setByte(int index, byte value) throws SQLException
	{
		try
		{
			this._getStatement().setByte(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setBytes(int index, byte[] value) throws SQLException
	{
		try
		{
			this._getStatement().setBytes(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setCharacterStream(int index, XInputStream value, int lenght) throws SQLException
	{
		try
		{
			InputStream input = new XInputStreamToInputStreamAdapter(value);
			Reader reader = new java.io.InputStreamReader(input);
			this._getStatement().setCharacterStream(index, reader, lenght);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setClob(int index, XClob value) throws SQLException
	{
		try
		{
			java.sql.PreparedStatement statement = this._getStatement();
			java.sql.Clob clob = UnoHelper.getSQLClob(statement, value);
			statement.setClob(index, clob);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setDate(int index, Date value) throws SQLException
	{
		try
		{
			java.sql.Date date = UnoHelper.getJavaDate(value);
			this._getStatement().setDate(index, date);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setDouble(int index, double value) throws SQLException
	{
		try
		{
			this._getStatement().setDouble(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setFloat(int index, float value) throws SQLException
	{
		try
		{
			this._getStatement().setFloat(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setInt(int index, int value) throws SQLException
	{
		try
		{
			this._getStatement().setInt(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setLong(int index, long value) throws SQLException
	{
		try
		{
			this._getStatement().setLong(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setNull(int index, int type) throws SQLException
	{
		try
		{
			this._getStatement().setNull(index, type);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setObject(int index, Object value) throws SQLException
	{
		try
		{
			this._getStatement().setObject(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setObjectNull(int index, int type, String name) throws SQLException
	{
		try
		{
			this._getStatement().setNull(index, type);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setObjectWithInfo(int index, Object value, int type, int scale) throws SQLException
	{
		try
		{
			this._getStatement().setObject(index, value, type, scale);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setRef(int index, XRef value) throws SQLException
	{
		// TODO: Implement me!!!
	}

	@Override
	public void setShort(int index, short value) throws SQLException
	{
		try
		{
			this._getStatement().setShort(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setString(int index, String value) throws SQLException
	{
		try
		{
			this._getStatement().setString(index, value);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setTime(int index, Time value) throws SQLException
	{
		try
		{
			java.sql.Time time = UnoHelper.getJavaTime(value);
			this._getStatement().setTime(index, time);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void setTimestamp(int index, DateTime value) throws SQLException
	{
		try
		{
			java.sql.Timestamp timestamp = UnoHelper.getJavaDateTime(value);
			this._getStatement().setTimestamp(index, timestamp);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XPreparedBatchExecution:
	@Override
	public void addBatch() throws SQLException
	{
		try
		{
			this._getStatement().addBatch();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public void clearBatch() throws SQLException
	{
		try
		{
			this._getStatement().clearBatch();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int[] executeBatch() throws SQLException
	{
		try
		{
			return this._getStatement().executeBatch();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	// com.sun.star.sdbc.XPreparedStatement:
	@Override
	public boolean execute() throws SQLException
	{
		try
		{
			return this._getStatement().execute();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public XResultSet executeQuery() throws SQLException
	{
		try
		{
			java.sql.ResultSet resultset = this._getStatement().executeQuery();
			return _getResultSet(m_xContext, resultset);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	@Override
	public int executeUpdate() throws SQLException
	{
		try
		{
			return this._getStatement().executeUpdate();
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


	// com.sun.star.sdbc.XResultSetMetaDataSupplier:
	@Override
	public XResultSetMetaData getMetaData() throws SQLException
	{
		try
		{
			java.sql.ResultSetMetaData metadata = this._getStatement().getMetaData();
			return new ResultSetMetaData(metadata);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	abstract protected XResultSet _getResultSet(XComponentContext ctx,
												java.sql.ResultSet resultset)
	throws java.sql.SQLException;


	abstract protected java.sql.PreparedStatement _getStatement();


}