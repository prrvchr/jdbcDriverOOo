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
package io.github.prrvchr.comp.sdb;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XChild;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.NoSupportException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.sdb.XCommandPreparation;
import com.sun.star.sdb.XQueriesSupplier;
import com.sun.star.sdb.XSQLQueryComposer;
import com.sun.star.sdb.XSQLQueryComposerFactory;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XUsersSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.comp.helper.UsersSupplierHelper;


public final class Connection extends io.github.prrvchr.comp.sdbc.Connection
implements XChild,
           XCommandPreparation,
           XGroupsSupplier,
           XMultiServiceFactory,
           XQueriesSupplier,
           XSQLQueryComposerFactory,
           XTablesSupplier,
           XUsersSupplier,
           XViewsSupplier
{
	private final XComponentContext m_xContext;
	private final java.sql.Connection m_Connection;
	private final PropertyValue[] m_info;
	private final String m_url;
	private static final String m_name = Connection.class.getName();
	private static final String[] m_services = {"com.sun.star.sdb.Connection",
                                                "com.sun.star.sdbc.Connection"};
	
	// The constructor method:
	public Connection(XComponentContext ctx,
                      java.sql.Connection connection,
                      PropertyValue[] info,
                      String url,
                      Boolean bookmark)
	{
		super( m_name, m_services, ctx, connection, info, url, bookmark);
		m_xContext = ctx;
		m_Connection = connection;
		m_info = info;
		m_url = url;
	}


	// com.sun.star.container.XChild:
	@Override
	public Object getParent()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getParent() *************************");
		return null;
	}


	@Override
	public void setParent(Object arg0) throws NoSupportException
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getParent() *************************");
	}


	// com.sun.star.sdb.XCommandPreparation:
	@Override
	public XPreparedStatement prepareCommand(String command, int type)
	throws SQLException
	{
		// TODO: Implement me!!!
		System.out.println("Connection.XPreparedStatement() *************************");
		return null;
	}


	// com.sun.star.sdbcx.XGroupsSupplier:
	@Override
	public XNameAccess getGroups()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getGroups() *************************");
		return null;
	}


	// import com.sun.star.lang.XMultiServiceFactory:
	@Override
	public Object createInstance(String service)
	throws Exception
	{
		// TODO: Implement me!!!
		System.out.println("Connection.createInstance() *************************");
		return null;
	}


	@Override
	public Object createInstanceWithArguments(String service, Object[] arguments) throws Exception {
		// TODO: Implement me!!!
		System.out.println("Connection.createInstanceWithArguments() *************************");
		return null;
	}


	@Override
	public String[] getAvailableServiceNames()
	{
		System.out.println("Connection.getAvailableServiceNames() *************************");
		String[] services = {"com.sun.star.sdb.SQLQueryComposer"};
		return services;
	}


	// com.sun.star.sdb.XQueriesSupplier:
	@Override
	public XNameAccess getQueries()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getGroups() *************************");
		return null;
	}


	// com.sun.star.sdb.XSQLQueryComposerFactory:
	@Override
	public XSQLQueryComposer createQueryComposer()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.createQueryComposer() *************************");
		return null;
	}


	// com.sun.star.sdbcx.XTablesSupplier:
	@Override
	public XNameAccess getTables()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getTables() *************************");
		return null;
	}


	// com.sun.star.sdbcx.XUsersSupplier:
	@Override
	public XNameAccess getUsers()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getUsers() *************************");
		UsersSupplierHelper supplier = new UsersSupplierHelper(m_Connection);
		XNameAccess users = supplier.getUsers();
		return users;
	}


	// com.sun.star.sdbcx.XViewsSupplier:
	@Override
	public XNameAccess getViews()
	{
		// TODO: Implement me!!!
		System.out.println("Connection.getViews() *************************");
		return null;
	}


}