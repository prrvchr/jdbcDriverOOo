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
package io.github.prrvchr.hsqldbdriver.sdbc;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.hsqldbdriver.lang.ServiceComponent;


public abstract class WarningsSupplierComponent<T extends java.sql.Wrapper>
extends ServiceComponent
implements com.sun.star.sdbc.XWarningsSupplier
{
	private final T m_Wrapper;

	// The constructor method:
	public WarningsSupplierComponent(String name,
									 String[] services,
									 T wrapper)
	{
		super(name , services);
		m_Wrapper = wrapper;
	}


	// com.sun.star.sdbc.XWarningsSupplier:
	@Override
	public void clearWarnings() throws SQLException
	{
		WarningsSupplier.clearWarnings(m_Wrapper, this);
	}


	@Override
	public Object getWarnings() throws SQLException
	{
		return WarningsSupplier.getWarnings(m_Wrapper, this);
	}


}
