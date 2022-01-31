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
	private java.sql.PreparedStatement m_Statement;


	// The constructor method:
	public SuperPreparedStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection connection,
								  java.sql.PreparedStatement statement,
								  String type)
	{
		super(context, name, services, connection, statement, type);
		m_xConnection = connection;
		m_Statement = statement;
	}

	public SuperPreparedStatement(XComponentContext context,
								  String name,
								  String[] services,
								  BaseConnection connection,
								  java.sql.PreparedStatement statement,
								  String type,
								  Map<String, Property> properties)
	{
		super(context, name, services, connection, statement, type, properties);
		m_Statement = statement;
	}


	// com.sun.star.sdbc.XParameters:
	@Override
	public void clearParameters() throws SQLException
	{
		try
		{
			m_Statement.clearParameters();
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
			java.sql.Array array =  UnoHelper.getSQLArray(m_Statement, value);
			m_Statement.setArray(index, array);
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
			m_Statement.setBinaryStream(index, input, lenght);
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
			java.sql.Blob blob = UnoHelper.getSQLBlob(m_Statement, value);
			m_Statement.setBlob(index, blob);
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
			m_Statement.setBoolean(index, value);
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
			m_Statement.setByte(index, value);
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
			m_Statement.setBytes(index, value);
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
			m_Statement.setCharacterStream(index, reader, lenght);
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
			java.sql.Clob clob = UnoHelper.getSQLClob(m_Statement, value);
			m_Statement.setClob(index, clob);
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
			m_Statement.setDate(index, date);
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
			m_Statement.setDouble(index, value);
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
			m_Statement.setFloat(index, value);
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
			m_Statement.setInt(index, value);
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
			m_Statement.setLong(index, value);
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
			m_Statement.setNull(index, type);
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
			m_Statement.setObject(index, value);
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
			m_Statement.setNull(index, type);
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
			m_Statement.setObject(index, value, type, scale);
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
			m_Statement.setShort(index, value);
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
			m_Statement.setString(index, value);
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
			m_Statement.setTime(index, time);
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
			m_Statement.setTimestamp(index, timestamp);
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
			m_Statement.addBatch();
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
			m_Statement.clearBatch();
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
			return m_Statement.executeBatch();
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
			return m_Statement.execute();
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
			java.sql.ResultSet resultset = m_Statement.executeQuery();
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
			return m_Statement.executeUpdate();
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
			java.sql.ResultSetMetaData metadata = m_Statement.getMetaData();
			return new ResultSetMetaData(metadata);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}


	abstract protected XResultSet _getResultSet(XComponentContext ctx,
												java.sql.ResultSet resultset)
	throws java.sql.SQLException;


}
