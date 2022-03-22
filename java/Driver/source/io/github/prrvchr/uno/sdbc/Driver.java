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

import java.sql.DriverManager;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.sdbc.XConnection;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;


public final class Driver
extends DriverBase
{
	private static final String m_name = Driver.class.getName();
	private static final String[] m_services = {"io.github.prrvchr.HsqlDBDriverOOo.sdbc.Driver",
												"com.sun.star.sdbc.Driver"};

	// The constructor method:
	public Driver(XComponentContext ctx)
	throws Exception
	{
		super(ctx, m_name, m_services);
		System.out.println("sdbc.Driver() 1");
	}

	protected java.sql.Connection _getConnection(String url,
												 Properties properties)
		throws java.sql.SQLException
	{
		return new ConnectionWrapper(DriverManager.getConnection(url, properties));
	}

	protected XConnection _getConnection(XComponentContext ctx,
										 java.sql.Connection connection,
										 String url,
										 PropertyValue[] info)
	{
		return new Connection(ctx, connection, url, info);
	}


	// UNO Service Registration:
	public static XSingleComponentFactory __getComponentFactory(String name)
	{
		XSingleComponentFactory factory = null;
		if (name.equals(m_name))
		{
			factory = Factory.createComponentFactory(Driver.class, m_services);
		}
		return factory;
	}

	public static boolean __writeRegistryServiceInfo(XRegistryKey key)
	{
		return Factory.writeRegistryServiceInfo(m_name, m_services, key);
	}


}
