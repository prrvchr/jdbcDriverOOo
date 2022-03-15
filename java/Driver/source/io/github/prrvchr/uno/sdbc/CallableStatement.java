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

import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;


public final class CallableStatement
extends SuperCallableStatement
{
	private static final String m_name = CallableStatement.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbc.CallableStatement"};
	private java.sql.Connection m_Connection;
	private java.sql.CallableStatement m_Statement = null;
	private String m_Sql;


	// The constructor method:
	public CallableStatement(XComponentContext context,
							 BaseConnection xConnection,
							 java.sql.Connection connection,
							 String sql)
	throws java.sql.SQLException
	{
		super(context, m_name, m_services, xConnection);
		m_Connection = connection;
		m_Sql= sql;
		System.out.println("sdbc.CallableStatement() 1");
	}


	protected XResultSet _getResultSet(XComponentContext ctx,
									   java.sql.ResultSet resultset)
	throws java.sql.SQLException
	{
		return new ResultSet(ctx, this, resultset);
	}


	protected java.sql.CallableStatement _getStatement()
	{
		if (m_Statement == null)
		{
			try {
				m_Statement = m_Connection.prepareCall(m_Sql, getResultSetType(), getResultSetConcurrency());
				_setStatement(m_Statement);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_Statement;
	}


	protected java.sql.CallableStatement _getWrapper()
	{
		return m_Statement;
	}
}
