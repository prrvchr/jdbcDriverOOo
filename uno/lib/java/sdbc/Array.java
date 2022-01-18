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

import java.sql.JDBCType;
import java.util.Arrays;

import com.sun.star.container.XNameAccess;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XResultSet;

public class Array
extends WeakBase
implements XArray
{
	private final Object[] m_Array;
	private final JDBCType m_Type;

	// The constructor method:
	public Array(java.sql.Array array)
	throws java.sql.SQLException
	{
		m_Array = (Object[]) array.getArray();
		m_Type = JDBCType.valueOf(array.getBaseType());
	}
	public Array(Object[] array,
				 int type)
	{
		m_Array = array;
		m_Type = JDBCType.valueOf(type);
	}

	@Override
	public Object[] getArray(XNameAccess arg0)
	throws SQLException
	{
		return m_Array;
	}

	@Override
	public Object[] getArrayAtIndex(int index, int count, XNameAccess map)
	throws SQLException
	{
		return Arrays.copyOfRange(m_Array, index, index + count);
	}

	@Override
	public int getBaseType()
	throws SQLException
	{
		return m_Type.getVendorTypeNumber();
	}

	@Override
	public String getBaseTypeName()
	throws SQLException
	{
		return m_Type.getName();
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
