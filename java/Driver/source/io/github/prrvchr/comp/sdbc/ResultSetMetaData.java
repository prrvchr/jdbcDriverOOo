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

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSetMetaData;

import io.github.prrvchr.comp.helper.UnoHelper;


public final class ResultSetMetaData
extends WeakBase
implements XResultSetMetaData
{
	private final java.sql.ResultSetMetaData m_Metadata;
	
	// The constructor method:
	public ResultSetMetaData(java.sql.ResultSetMetaData metadata)
	{
		m_Metadata = metadata;
	}


	// com.sun.star.sdbc.XResultSetMetaData:
	@Override
	public String getCatalogName(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getCatalogName(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public int getColumnCount() throws SQLException
	{
		try
		{
			return m_Metadata.getColumnCount();
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public int getColumnDisplaySize(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getColumnDisplaySize(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getColumnLabel(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getColumnLabel(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getColumnName(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getColumnName(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getColumnServiceName(int index) throws SQLException
	{
		// TODO: Implement me!!!
		return "";
	}

	@Override
	public int getColumnType(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getColumnType(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getColumnTypeName(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getColumnTypeName(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public int getPrecision(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getPrecision(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public int getScale(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getScale(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getSchemaName(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getSchemaName(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public String getTableName(int index) throws SQLException
	{
		try
		{
			return m_Metadata.getTableName(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isAutoIncrement(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isAutoIncrement(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isCaseSensitive(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isCaseSensitive(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isCurrency(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isCurrency(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isDefinitelyWritable(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isDefinitelyWritable(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public int isNullable(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isNullable(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isReadOnly(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isReadOnly(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isSearchable(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isSearchable(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isSigned(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isSigned(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}

	@Override
	public boolean isWritable(int index) throws SQLException
	{
		try
		{
			return m_Metadata.isWritable(index);
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, this);
		}
	}


}