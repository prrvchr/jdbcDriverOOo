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
package io.github.prrvchr.hsqldb.comp.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Wrapper;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.hsqldb.comp.helper.ServiceHelper;
import io.github.prrvchr.hsqldb.comp.helper.UnoHelper;


public abstract class WarningsSupplierHelper<T> extends ServiceHelper
implements XWarningsSupplier
{
	private final Wrapper m_wrapper;
	private XInterface m_component;


	// The constructor method:
	public WarningsSupplierHelper(String name,
		                          String[] services,
		                          Wrapper wrapper)
	{
		super(name, services);
		m_wrapper = wrapper;
		@SuppressWarnings({ "unchecked" })
		T component = (T) this;
		m_component = (XInterface) component;
	}


	// com.sun.star.sdbc.XWarningsSupplier:
	@Override
	public void clearWarnings() throws SQLException
	{
		try
		{
			if (m_wrapper.isWrapperFor(Connection.class))
			{
				Connection wrapper = m_wrapper.unwrap(Connection.class);
				wrapper.clearWarnings();
			}
			else if(m_wrapper.isWrapperFor(ResultSet.class))
			{
				ResultSet wrapper = m_wrapper.unwrap(ResultSet.class);
				wrapper.clearWarnings();
			}
			else if(m_wrapper.isWrapperFor(Statement.class))
			{
				Statement wrapper = m_wrapper.unwrap(Statement.class);
				wrapper.clearWarnings();
			}
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, m_component);
		}
	}


	@Override
	public Object getWarnings() throws SQLException
	{
		java.sql.SQLWarning w = null;
		try
		{
			if (m_wrapper.isWrapperFor(Connection.class))
			{
				Connection wrapper = m_wrapper.unwrap(Connection.class);
				w = wrapper.getWarnings();
			}
			else if(m_wrapper.isWrapperFor(ResultSet.class))
			{
				ResultSet wrapper = m_wrapper.unwrap(ResultSet.class);
				w = wrapper.getWarnings();
			}
			else if(m_wrapper.isWrapperFor(Statement.class))
			{
				Statement wrapper = m_wrapper.unwrap(Statement.class);
				w = wrapper.getWarnings();
			}
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, m_component);
		}
		if (w != null)
		{
			return UnoHelper.getWarning(w, m_component);
		}
		// FIXME: XWarningsSupplier:getWarnings() returns <void> until a new warning is reported for the object.
		// FIXME: https://www.openoffice.org/api/docs/common/ref/com/sun/sun/star/sdbc/XWarningsSupplier.html
		return null;
	}


}
