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
package io.github.prrvchr.uno.sdb;


import java.util.ArrayList;
import java.util.List;

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
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XUsersSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.UsersSupplierHelper;
import io.github.prrvchr.uno.sdbc.BaseConnection;
import io.github.prrvchr.uno.sdbcx.Statement;
import io.github.prrvchr.uno.sdbcx.Container;
import io.github.prrvchr.uno.sdbcx.Table;


public final class Connection extends BaseConnection
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
	@SuppressWarnings("unused")
	private final XComponentContext m_xContext;
	private final java.sql.Connection m_Connection;
	
	@SuppressWarnings("unused")
	private final PropertyValue[] m_info;
	
	@SuppressWarnings("unused")
	private final String m_url;
	private static final String m_name = Connection.class.getName();
	private static final String[] m_services = {"com.sun.star.sdb.Connection",
												"com.sun.star.sdbc.Connection",
												"com.sun.star.sdbcx.DatabaseDefinition"};


	// The constructor method:
	public Connection(XComponentContext ctx,
					  java.sql.Connection connection,
					  PropertyValue[] info,
					  String url)
	{
		super(ctx, m_name, m_services, connection, info, url);
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
		XNameAccess tables = null;
		//try {
		//	tables = SchemaCrawler.getTables(m_Connection);
		//} catch (java.sql.SQLException e) {
		//	e.printStackTrace();
		//}
		tables = _getTables();
		return tables;
	}

	public XNameAccess _getTables()
	{
		List<String> names = new ArrayList<String>();
		List<Table> tables = new ArrayList<Table>();
		try {
			java.sql.DatabaseMetaData metadata = m_Connection.getMetaData();
			String[] types = {"TABLE", "VIEW", "ALIAS", "SYNONYM"};
			java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
			while (result.next())
			{
				Table table = new Table(metadata, result);
				tables.add(table);
				names.add(table.getName());
			}
			result.close();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}
		return new Container<Table>(tables, names);
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


	protected XStatement _getStatement(XComponentContext ctx,
									   java.sql.Statement statement)
	throws java.sql.SQLException
	{
		return new Statement(ctx, this, statement);
	}

	protected XPreparedStatement _getPreparedStatement(XComponentContext ctx,
													   java.sql.PreparedStatement statement)
	throws java.sql.SQLException
	{
		return new PreparedStatement(ctx, this, statement);
	}

	protected XPreparedStatement _getCallableStatement(XComponentContext ctx,
													   java.sql.CallableStatement statement)
	throws java.sql.SQLException
	{
		return new CallableStatement(ctx, this, statement);
	}


}