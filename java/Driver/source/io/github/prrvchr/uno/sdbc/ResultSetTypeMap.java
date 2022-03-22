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

import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class ResultSetTypeMap
extends ResultSet
{
	private final int m_DataType;
	private final int m_TypeName;

	// The constructor method:
	public ResultSetTypeMap(XComponentContext ctx,
			 java.sql.ResultSet resultset)
	{
		this(ctx, resultset, 0, 0);
		System.out.println("sdbc.ResultSetTypeMap() 1");
	}
	public ResultSetTypeMap(XComponentContext ctx,
					 java.sql.ResultSet resultset,
					 int datatype,
					 int typename)
	{
		super(ctx, resultset);
		m_DataType = datatype;
		m_TypeName = typename;
		System.out.println("sdbc.ResultSetTypeMap() 1");
	}

	@Override
	public int getInt(int index) throws SQLException
	{
		try
		{
			System.out.println("sdbc.ResultSetTypeMap.getInt() 1");
			int value = m_ResultSet.getInt(index);
			if (index == m_DataType)
			{
				System.out.println("sdbc.ResultSetTypeMap.getInt() 2 ***************************************** : " + value);
				value = UnoHelper.mapSQLDataType(value);
			}
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
			System.out.println("sdbc.ResultSetTypeMap.getString() 1");
			String value = m_ResultSet.getString(index);
			if (value != null && index == m_TypeName)
			{
				System.out.println("sdbc.ResultSetTypeMap.getString() 2 *****************************************");
				value = UnoHelper.mapSQLDataTypeName(value, getInt(m_DataType));
			}
			return (value != null) ? value : "";
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}
}
