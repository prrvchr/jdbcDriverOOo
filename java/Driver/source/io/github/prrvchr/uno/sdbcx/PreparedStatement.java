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
package io.github.prrvchr.uno.sdbcx;

import java.sql.SQLException;

import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.BaseConnection;


public final class PreparedStatement
extends BasePreparedStatement
{
	private static String m_name = PreparedStatement.class.getName();
	private static String[] m_services = {"com.sun.star.sdbc.PreparedStatement",
										  "com.sun.star.sdbcx.PreparedStatement"};
	private java.sql.Connection m_Connection;
	private java.sql.PreparedStatement m_Statement = null;
	private String m_Sql;

	
	// The constructor method:
	public PreparedStatement(XComponentContext context,
							 BaseConnection xConnection,
							 java.sql.Connection connection,
							 String sql)
	throws SQLException
	{
		super(context, m_name, m_services, xConnection);
		m_Connection = connection;
		m_Sql = sql;
		System.out.println("sdbcx.PreparedStatement() 1");
	}


	protected XResultSet _getResultSet(XComponentContext ctx,
									   java.sql.ResultSet resultset)
	throws java.sql.SQLException
	{
		return new ResultSet(ctx, this, resultset);
	}


	protected java.sql.PreparedStatement _getStatement()
	{
		if (m_Statement == null)
		{
			try {
				m_Statement = m_Connection.prepareStatement(m_Sql, getResultSetType(), getResultSetConcurrency());
				_setStatement(m_Statement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_Statement;
	}


	protected java.sql.PreparedStatement _getWrapper()
	{
		return m_Statement;
	}


}
