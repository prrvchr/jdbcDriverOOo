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
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.SchemaCrawler;
import io.github.prrvchr.jdbcdriver.sdbcx.Container;
import io.github.prrvchr.jdbcdriver.sdbcx.Statement;
import io.github.prrvchr.jdbcdriver.sdbcx.Table;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.UsersSupplierHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public final class Connection
    extends ConnectionBase
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

    private static final String m_name = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Connection",
                                                "com.sun.star.sdbc.Connection",
                                                "com.sun.star.sdbcx.DatabaseDefinition"};
    private static final boolean m_crawler = false;

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      java.sql.Connection connection,
                      String url,
                      PropertyValue[] info)
        throws java.sql.SQLException
    {
        super(ctx, m_name, m_services, provider, connection, info, url, m_crawler);
        System.out.println("sdb.Connection() 1");
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
        System.out.println("Connection.prepareCommand() *************************");
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
        System.out.println("Connection.getQueries() *************************");
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
        if (m_crawler)
        {
            try
            {
                System.out.println("sdb.Connection.getTables() 1");
                tables = SchemaCrawler.getTables(m_Connection);
                System.out.println("sdb.Connection.getTables() 2");
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        else
            tables = _getTables();
        return tables;
    }

    public XNameAccess _getTables()
    {
        String name = null;
        List<String> names = new ArrayList<String>();
        List<Table> tables = new ArrayList<Table>();
        try {
            java.sql.DatabaseMetaData metadata = m_Connection.getMetaData();
            String[] types = {"TABLE", "VIEW", "ALIAS", "SYNONYM"};
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            boolean privileges = UnoHelper.getDefaultDriverInfo(m_info, "IgnoreDriverPrivileges", false);
            while (result.next())
            {
                name = result.getString(3);
                Table table = new Table(metadata, result, name, privileges);
                tables.add(table);
                names.add(name);
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return new Container<Table>(tables, names, "com.sun.star.sdb.Table", TypeClass.SERVICE);
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
                                       DriverProvider provider,
                                       java.sql.Connection connection)
    throws java.sql.SQLException
    {
        return new Statement(ctx, provider, this, connection);
    }

    protected XPreparedStatement _getPreparedStatement(XComponentContext ctx,
                                                       DriverProvider provider,
                                                       java.sql.Connection connection,
                                                       String sql)
    throws java.sql.SQLException
    {
        System.out.println("sdb.Connection._getPreparedStatement() 1: '" + sql + "'");
        return new PreparedStatement(ctx, provider, this, connection, sql);
    }
         
    protected XPreparedStatement _getCallableStatement(XComponentContext ctx,
                                                       DriverProvider provider,
                                                       java.sql.Connection connection,
                                                       String sql)
    throws java.sql.SQLException
    {
        System.out.println("sdb.Connection._getCallableStatement() 1: '" + sql + "'");
        return new CallableStatement(ctx, provider, this, connection, sql);
    }


}