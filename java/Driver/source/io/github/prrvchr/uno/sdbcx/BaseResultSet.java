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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.SuperResultSet;


public abstract class BaseResultSet<T>
extends SuperResultSet<T>
implements XRowLocate
{
	private final java.sql.ResultSet m_ResultSet;
	private boolean m_IsBookmarkable = true;

	private static Map<String, Property> _getPropertySet()
	{
		short readonly = PropertyAttribute.READONLY;
		Map<String, Property> map = new HashMap<String, Property>();
		Property p1 = UnoHelper.getProperty("IsBookmarkable", "boolean", readonly);
		map.put(UnoHelper.getPropertyName(p1), p1);
		return map;
	}


	// The constructor method:
	public BaseResultSet(XComponentContext ctx,
                         XInterface statement,
                         java.sql.ResultSet resultset)
	{
		super(ctx, statement, resultset, _getPropertySet());
		m_ResultSet = resultset;
	}


	public boolean getIsBookmarkable()
	{
		System.out.println("ResultSet.getIsBookmarkable()");
		return m_IsBookmarkable;
	}


	// com.sun.star.sdbcx.XRowLocate:
	@Override
	public int compareBookmarks(Object bookmark1, Object bookmark2)
	throws SQLException
	{
		System.out.println("ResultSet.compareBookmarks()");
		return 0;
	}


	@Override
	public Object getBookmark()
	throws SQLException
	{
		System.out.println("ResultSet.getBookmark()");
		return null;
	}


	@Override
	public boolean hasOrderedBookmarks()
	throws SQLException
	{
		System.out.println("ResultSet.hasOrderedBookmarks()");
		return false;
	}


	@Override
	public int hashBookmark(Object arg0)
	throws SQLException
	{
		System.out.println("ResultSet.hashBookmark()");
		return 0;
	}


	@Override
	public boolean moveRelativeToBookmark(Object arg0, int arg1)
	throws SQLException
	{
		System.out.println("ResultSet.moveRelativeToBookmark()");
		return false;
	}


	@Override
	public boolean moveToBookmark(Object arg0)
	throws SQLException
	{
		System.out.println("ResultSet.moveToBookmark()");
		return false;
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
