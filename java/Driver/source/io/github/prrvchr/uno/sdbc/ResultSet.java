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

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;


public final class ResultSet
extends SuperResultSet<ResultSet>
{
	private static final String m_name = ResultSet.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbc.ResultSet"};
	private java.sql.ResultSet m_ResultSet;


	// The constructor method:
	public ResultSet(XComponentContext ctx,
                     java.sql.ResultSet resultset)
	{
		super(ctx, resultset);
		m_ResultSet = resultset;
	}
	public ResultSet(XComponentContext ctx,
                     XInterface statement,
                     java.sql.ResultSet resultset)
	{
		super(ctx, statement, resultset);
		m_ResultSet = resultset;
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String _getImplementationName()
	{
		return m_name;
	}
	@Override
	public String[] _getServiceNames() {
		return m_services;
	}


	// com.sun.star.sdbc.XWarningsSupplier:
	@Override
	public java.sql.Wrapper _getWrapper(){
		return m_ResultSet;
	}
	@Override
	public XInterface _getInterface()
	{
		return this;
	}


}
