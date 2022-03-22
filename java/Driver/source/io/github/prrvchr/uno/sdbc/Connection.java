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


import com.sun.star.beans.PropertyValue;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;


public class Connection
	extends ConnectionBase
{
	private static String m_name = Connection.class.getName();
	private static String[] m_services = {"com.sun.star.sdbc.Connection"};

	// The constructor method:
	public Connection(XComponentContext ctx,
					  java.sql.Connection connection,
					  String url,
					  PropertyValue[] info)
	{
		super(ctx, m_name, m_services, connection, info, url);
		System.out.println("sdbc.Connection() 1");
	}


	protected XStatement _getStatement(XComponentContext ctx,
									   java.sql.Connection connection)
	throws java.sql.SQLException
	{
		return new Statement(ctx, this, connection);
	}

	protected XPreparedStatement _getPreparedStatement(XComponentContext ctx,
													   java.sql.Connection connection,
													   String sql)
	throws java.sql.SQLException
	{
		System.out.println("sdbc.Connection._getPreparedStatement() 1: '" + sql + "'");
		return new PreparedStatement(ctx, this, connection, sql);
	}

	protected XPreparedStatement _getCallableStatement(XComponentContext ctx,
													   java.sql.Connection connection,
													   String sql)
	throws java.sql.SQLException
	{
		System.out.println("sdbc.Connection._getCallableStatement() 1: '" + sql + "'");
		return new CallableStatement(ctx, this, connection, sql);
	}


}