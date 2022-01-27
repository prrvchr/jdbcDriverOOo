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
package io.github.prrvchr.hsqldbdriver.sdbcx;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.uno.XComponentContext;


import io.github.prrvchr.hsqldbdriver.helper.UnoHelper;
import io.github.prrvchr.hsqldbdriver.sdbc.BaseConnection;
import io.github.prrvchr.hsqldbdriver.sdbc.SuperPreparedStatement;


public abstract class BasePreparedStatement
extends SuperPreparedStatement
{
	private boolean m_UseBookmarks = false;

	private static Map<String, Property> _getPropertySet()
	{
		Map<String, Property> map = new HashMap<String, Property>();
		Property p1 = UnoHelper.getProperty("UseBookmarks", "boolean");
		map.put(UnoHelper.getPropertyName(p1), p1);
		return map;
	}


	// The constructor method:
	public BasePreparedStatement(XComponentContext context,
								 String name,
								 String[] services,
								 BaseConnection connection,
								 java.sql.PreparedStatement statement,
								 String type)
	{
		super(context, name, services, connection, statement, type, _getPropertySet());
	}


	public boolean getUseBookmarks()
	{
		System.out.println("BasePreparedStatement.getUseBookmarks() : " + m_UseBookmarks);
		return m_UseBookmarks;
	}
	public void setUseBookmarks(boolean value)
	{
		System.out.println("BasePreparedStatement.setUseBookmarks() : " + value);
		m_UseBookmarks = value;
	}


}
