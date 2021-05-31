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
package io.github.prrvchr.comp.sdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.comp.helper.UnoHelper;


public class WarningsSupplier
implements com.sun.star.sdbc.XWarningsSupplier
{
	private java.sql.Wrapper m_Wrapper;
	private XInterface m_Interface;


	public WarningsSupplier(java.sql.Wrapper wrapper, XInterface component)
	{
		m_Wrapper = wrapper;
		m_Interface = component;
	}


	// com.sun.star.sdbc.XWarningsSupplier:
	@Override
	public void clearWarnings() throws SQLException
	{
		try
		{
			if (m_Wrapper.isWrapperFor(Connection.class))
			{
				m_Wrapper.unwrap(Connection.class).clearWarnings();
			}
			else if(m_Wrapper.isWrapperFor(ResultSet.class))
			{
				m_Wrapper.unwrap(ResultSet.class).clearWarnings();
			}
			else if(m_Wrapper.isWrapperFor(Statement.class))
			{
				m_Wrapper.unwrap(Statement.class).clearWarnings();
			}
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, m_Interface);
		}
	}


	@Override
	public Object getWarnings() throws SQLException
	{
		java.sql.SQLWarning warning = null;
		try
		{
			if (m_Wrapper.isWrapperFor(Connection.class))
			{
				warning = m_Wrapper.unwrap(Connection.class).getWarnings();
			}
			else if(m_Wrapper.isWrapperFor(ResultSet.class))
			{
				warning = m_Wrapper.unwrap(ResultSet.class).getWarnings();
			}
			else if(m_Wrapper.isWrapperFor(Statement.class))
			{
				warning = m_Wrapper.unwrap(Statement.class).getWarnings();
			}
		} catch (java.sql.SQLException e)
		{
			throw UnoHelper.getException(e, m_Interface);
		}
		if (warning != null)
		{
			return UnoHelper.getWarning(warning, m_Interface);
		}
		// FIXME: XWarningsSupplier:getWarnings() returns <void> until a new warning is reported for the object.
		// FIXME: https://www.openoffice.org/api/docs/common/ref/com/sun/sun/star/sdbc/XWarningsSupplier.html
		return null;
	}


}
