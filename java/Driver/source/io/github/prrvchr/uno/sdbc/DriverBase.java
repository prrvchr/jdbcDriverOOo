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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.sdbc.DriverPropertyInfo;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDriver;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceComponent;


public abstract class DriverBase
extends ServiceComponent
implements XDriver
{
	//private final URL m_url;
	private final XComponentContext m_xContext;
	private static final String m_identifier = "io.github.prrvchr.HsqlDBDriverOOo";
	private static final String m_DriverClassPath = "JavaDriverClassPath";
	private static final String m_DriverClass = "JavaDriverClass";
	private static final String m_registredProtocol = "sdbc:";
	private static final String m_connectProtocol = "jdbc:";
	private static final String m_driverFolder = "libs";
	private static final Map<String, String> m_subProtocols = Map.ofEntries(Map.entry("hsqldb", "org.hsqldb.jdbcDriver"),
			 																Map.entry("h2",		"org.h2.Driver"));

	// The constructor method:
	public DriverBase(XComponentContext context,
								String name, 
								String[] services)
	throws Exception
	{
		super(name, services);
		System.out.println("Driver.Driver() 1");
		m_xContext = context;
		System.out.println("Driver.Driver() 2");
	}


	// com.sun.star.sdbc.XDriver:
	public XConnection connect(String url, PropertyValue[] info)
	throws SQLException
	{
		System.out.println("sdbc.BaseDriver.connect() 1");
		String location = url;
		if (url.startsWith(m_registredProtocol))
			location = url.replaceFirst(m_registredProtocol, m_connectProtocol);
		Properties properties = UnoHelper.getJavaProperties(info);
		_registerDriver(location, properties);
		java.sql.Connection connection = null;
		System.out.println("sdbc.BaseDriver.connect() 2");
		try
		{
			System.out.println("sdbc.BaseDriver.connect() 3");
			connection = _getConnection(location, properties);
		} catch(java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		System.out.println(url);
		System.out.println(location);
		System.out.println("sdbc.BaseDriver.connect() 4 **************************************************************");
		return _getConnection(m_xContext, connection, url, info);
	}

	private void _registerDriver(String location, Properties properties)
		throws SQLException
	{	
		try
		{
			System.out.println("sdbc.DriverBase._registerDriver() 1");
			DriverManager.getDriver(location);
			System.out.println("sdbc.DriverBase._registerDriver() 2");
		}
		catch (java.sql.SQLException e)
		{
			System.out.println("sdbc.DriverBase._registerDriver() 3");
			String name = _getDriverClass(location, properties, m_DriverClass);
			URL url = _getDriverClassPath(location, properties, m_DriverClassPath);
			System.out.println("sdbc.DriverBase._registerDriver() 4 url: " + url + " name: " + name);
			if (name != null && url != null)
			{
				if (!_registerDriver(url, name))
					_registerDriver(name);
			}
				System.out.println("sdbc.DriverBase._registerDriver() 5");
		}
	}
	private URL _getDriverClassPath(String location, Properties properties, String property)
	{
		
		URL url = null;
		String path = properties.getProperty(property);
		if (path == null || path.isEmpty())
			url = _getDriverClassPath(location);
		else
			url = UnoHelper.getDriverURL(path);
		return url;
	}

	private String _getDriverClass(String url, Properties properties, String property)
	{
		String name = properties.getProperty(property);
		if (name == null || name.isEmpty())
			name = _getDriverClass(url);
		return name;
	}

	private String _getDriverClass(String url)
	{
		String protocol = _getSubProtocol(url);
		if (protocol != null && m_subProtocols.containsKey(protocol))
			return m_subProtocols.get(protocol);
		return null;
	}

	private URL _getDriverClassPath(String url)
	{
		String location = UnoHelper.getPackageLocation(m_xContext, m_identifier, m_driverFolder);
		String protocol = _getSubProtocol(url);
		if (protocol != null)
			return UnoHelper.getDriverURL(location, protocol + ".jar");
		return null;
	}
	private String _getSubProtocol(String url)
	{
		return _getUrlProtocol(url, 1);
	}
	private String _getUrlProtocol(String url, int index)
	{
		String[] protocols = url.split(":");
		if (protocols.length > index)
			return protocols[index];
		return null;
	}

	private boolean _registerDriver(URL url, String name)
		throws SQLException
	{
		java.sql.Driver driver = null;
		boolean registered = false;
		try
		{
			// XXX: Pick your JDBC driver at runtime: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
			Class<?> clazz = Class.forName(name, true, new URLClassLoader(new URL[] {url}));
			driver = new DriverWrapper((java.sql.Driver) clazz.getDeclaredConstructor().newInstance());
		}
		catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
		{
			throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
		}
		try
		{
			DriverManager.registerDriver(driver);
			registered = true;
			System.out.println("sdbc.BaseDriver._registerDriver(url, name) 1");
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		return registered;
	}

	private void _registerDriver(String name)
		throws SQLException
	{
		java.sql.Driver driver = null;
		try
		{
			driver = (java.sql.Driver) Class.forName(name).getDeclaredConstructor().newInstance();
		}
		catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
		{
			throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
		}
		try
		{
			DriverManager.registerDriver(driver);
			System.out.println("sdbc.BaseDriver._registerDriver(name) 1");
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
	}

	
	public boolean acceptsURL(String url)
	throws SQLException
	{
		String protocol = _getSubProtocol(url);
		boolean accept = protocol != null && m_subProtocols.containsKey(protocol);
		return accept && (url.startsWith(m_registredProtocol) || url.startsWith(m_connectProtocol));
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, PropertyValue[] info)
	throws SQLException
	{
		return new DriverPropertyInfo[0];
	}

	public int getMajorVersion()
	{
		return 1;
	}


	public int getMinorVersion()
	{
		return 0;
	}

	abstract protected java.sql.Connection _getConnection(String url,
														  Properties properties)
		throws java.sql.SQLException;

	abstract protected XConnection _getConnection(XComponentContext ctx,
												  java.sql.Connection connection,
												  String url,
												  PropertyValue[] info);


}
