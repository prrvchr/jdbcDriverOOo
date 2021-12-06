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
package io.github.prrvchr.uno.sdb;

import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.sdbcx.BaseResultSet;
import io.github.prrvchr.uno.sdbcx.ColumnsSupplier;


public final class ResultSet
extends BaseResultSet<ResultSet>
implements XColumnsSupplier
{
	private static final String m_name = ResultSet.class.getName();
	private static final String[] m_services = {"com.sun.star.sdb.ResultSet",
                                                "com.sun.star.sdbc.ResultSet", 
                                                "com.sun.star.sdbcx.ResultSet"};
	private final java.sql.ResultSet m_ResultSet;


	// The constructor method:
	public ResultSet(XComponentContext ctx,
                     XInterface statement,
                     java.sql.ResultSet resultset)
	{
		super(ctx, statement, resultset);
		m_ResultSet = resultset;
	}


	// com.sun.star.sdbcx.XColumnsSupplier:
	@Override
	public XNameAccess getColumns()
	{
		try
		{
			java.sql.ResultSetMetaData metadata = m_ResultSet.getMetaData();
			return ColumnsSupplier.getColumns(metadata);
		}
		catch (java.sql.SQLException e)
		{
			// pass
		}
		return null;
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String _getImplementationName()
	{
		return m_name;
	}
	@Override
	public String[] _getServiceNames()
	{
		return m_services;
	}


}
