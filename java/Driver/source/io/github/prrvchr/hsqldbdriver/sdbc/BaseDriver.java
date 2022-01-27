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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DriverManager;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XIntrospection;
import com.sun.star.sdbc.DriverPropertyInfo;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDriver;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.hsqldbdriver.helper.DriverHelper;
import io.github.prrvchr.hsqldbdriver.helper.UnoHelper;
import io.github.prrvchr.hsqldbdriver.lang.ServiceComponent;
import io.github.prrvchr.hsqldbdriver.sdb.Connection;


public abstract class BaseDriver
extends ServiceComponent
implements XDriver
{
	//private final URL m_url;
	private final XComponentContext m_xContext;
	private static final String m_identifier = "io.github.prrvchr.HsqlDBDriverOOo";
	private static final String m_protocolName = "sdbc:hsqldb:";
	private static final String m_registredProtocolName = "sdbc:";
	private static final String m_connectProtocolName = "jdbc:";
	private static final String m_driverFolder = "libs";
	private static final String m_driverArchive = "hsqldb.jar";
	private static final String m_driverClassName = "org.hsqldb.jdbcDriver";
	private static URL m_driverUrl;


	public BaseDriver(XComponentContext context,
								String name, 
								String[] services)
	throws Exception
	{
		super(name, services);
		System.out.println("Driver.Driver() 1");
		m_xContext = context;
		String location = UnoHelper.getPackageLocation(context, m_identifier, m_driverFolder);
		m_driverUrl = UnoHelper.getDriverURL(location, m_driverArchive);
		System.out.println("Driver.Driver() 2 " + m_driverUrl.toString());
	}


	// com.sun.star.sdbc.XDriver:
	public XConnection connect(String url, PropertyValue[] info)
	throws SQLException
	{
		System.out.println("Driver.connect() 1");
		java.sql.Connection connection = _getConnection(url, info);
		System.out.println("Driver.connect() 2 **************************************************************");
		return _getConnection(connection, url, info);
	}

	public boolean acceptsURL(String url)
	throws SQLException
	{
		return url.startsWith(m_protocolName);
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

	protected java.sql.Connection _getConnection(String url, PropertyValue[] info)
	throws SQLException
	{
		System.out.println("Driver._getConnection() 1");
		java.sql.Connection connection = null;
		String location = url.replaceFirst(m_registredProtocolName, m_connectProtocolName);
		try
		{
			// XXX: Pick your JDBC driver at runtime
			// XXX: https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
			URLClassLoader loader = new URLClassLoader(new URL[] {m_driverUrl});
			java.sql.Driver driver = (java.sql.Driver) Class.forName(m_driverClassName, true, loader).getDeclaredConstructor().newInstance();
			DriverManager.registerDriver(new DriverHelper(driver));
		}
		catch(InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e)
		{
			System.out.print(e.getMessage());
			throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
		}
		catch (java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		Properties properties = UnoHelper.getConnectionProperties(info);
		try
		{
			connection = DriverManager.getConnection(location, properties);
			String version = connection.getMetaData().getDriverVersion();
			String username = connection.getMetaData().getUserName();
			System.out.println(url);
			System.out.println(location);
			System.out.println(version);
			System.out.println(username);
		} catch(java.sql.SQLException e)
		{
			throw UnoHelper.getSQLException(e, this);
		}
		System.out.println("Driver._getConnection() 2 **************************************************************");
		return connection;
	}

	private XConnection _getConnection(java.sql.Connection con, String url, PropertyValue[] info)
	{
		XConnection connection = null;
		try
		{
			connection = new Connection(m_xContext, con, info, url);
			XIntrospection mri = (XIntrospection) UnoHelper.createService(m_xContext, "mytools.Mri");
			mri.inspect(connection);
		}
		catch(java.lang.Exception e)
		{
			e.getStackTrace();
		}
		return connection;
	}


}
