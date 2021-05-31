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

import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.comp.lang.ServiceProperty;


public abstract class WarningsSupplierProperty
extends ServiceProperty
implements com.sun.star.sdbc.XWarningsSupplier
{
	private WarningsSupplier m_WarningsSupplier;
	public abstract java.sql.Wrapper _getWrapper();
	public abstract XInterface _getInterface();


	// The constructor method:
	public WarningsSupplierProperty()
	{
		m_WarningsSupplier = new WarningsSupplier(_getWrapper(), _getInterface());
	}
	public WarningsSupplierProperty(Map<String, Property> properties)
	{
		super(properties);
		m_WarningsSupplier = new WarningsSupplier(_getWrapper(), _getInterface());
	}


	// com.sun.star.sdbc.XWarningsSupplier:
	@Override
	public void clearWarnings() throws SQLException
	{
		m_WarningsSupplier.clearWarnings();
	}


	@Override
	public Object getWarnings() throws SQLException
	{
		return m_WarningsSupplier.getWarnings();
	}


}
