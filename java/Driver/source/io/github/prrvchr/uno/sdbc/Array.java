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

import com.sun.star.container.XNameAccess;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XResultSet;

public class Array
extends WeakBase
implements XArray
{
	private final java.sql.Array m_Array;

	// The constructor method:
	public Array(java.sql.Array array)
	{
		m_Array = array;
	}

	@Override
	public Object[] getArray(XNameAccess arg0)
	throws SQLException
	{
		try
		{
			return (Object[]) m_Array.getArray();
		}
		catch (java.sql.SQLException e)
		{
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public Object[] getArrayAtIndex(int index, int count, XNameAccess map)
	throws SQLException
	{
		try
		{
			return (Object[]) m_Array.getArray(index, count);
		}
		catch (java.sql.SQLException e)
		{
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public int getBaseType()
	throws SQLException
	{
		try
		{
			return m_Array.getBaseType();
		}
		catch (java.sql.SQLException e)
		{
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public String getBaseTypeName()
	throws SQLException
	{
		try
		{
			return m_Array.getBaseTypeName();
		}
		catch (java.sql.SQLException e)
		{
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public XResultSet getResultSet(XNameAccess arg0)
	throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XResultSet getResultSetAtIndex(int arg0, int arg1, XNameAccess arg2)
	throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}


}
