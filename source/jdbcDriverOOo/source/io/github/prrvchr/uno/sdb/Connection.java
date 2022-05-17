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
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbcx.Container;
import io.github.prrvchr.uno.sdbcx.Statement;
import io.github.prrvchr.uno.sdbcx.Table;
import io.github.prrvchr.uno.sdbcx.User;
import io.github.prrvchr.uno.sdbcx.View;


public final class Connection
    extends ConnectionBase
    implements XChild,
               XCommandPreparation,
               XQueriesSupplier,
               XSQLQueryComposerFactory,
               XMultiServiceFactory,
               XTablesSupplier,
               XUsersSupplier,
               XViewsSupplier,
               XGroupsSupplier
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
        super(ctx, m_name, m_services, provider, connection, url, info, m_crawler);
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
        System.out.println("sdb.Connection.getTables() 1");
        XNameAccess tables = null;
        if (m_crawler)
        {
            try
            {
                System.out.println("sdb.Connection.getTables() 2");
                tables = SchemaCrawler.getTables(m_Connection, m_provider);
                System.out.println("sdb.Connection.getTables() 3");
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("sdb.Connection.getTables() 4");
            tables = _getTables();
            System.out.println("sdb.Connection.getTables() 5");
        return tables;
    }

    public XNameAccess _getTables()
    {
        List<String> names = new ArrayList<String>();
        List<Table> tables = new ArrayList<Table>();
        try {
            java.sql.DatabaseMetaData metadata = m_Connection.getMetaData();
            String[] types = m_provider.getTableTypes();
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            while (result != null && result.next())
            {
                String catalog = result.getString(1);
                String schema = result.getString(2);
                String name = result.getString(3);
                String type = m_provider.getTableType(result.getString(4));
                String description = result.getString(5);
                Table table = new Table(m_provider, metadata, catalog, schema, name, type, description);
                tables.add(table);
                names.add(name);
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return new Container<Table>(m_Connection, m_provider, tables, names, "com.sun.star.beans.XPropertySet");
    }


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers()
    {
        System.out.println("Connection.getUsers() 1");
        List<User> users = new ArrayList<User>();
        List<String> names = new ArrayList<String>();
        String query = m_provider.getUserQuery();
        if (query != null) {
            try {
                String name = null;
                java.sql.Statement statement = m_Connection.createStatement();
                java.sql.ResultSet result = statement.executeQuery(query);
                while (result != null && result.next()) {
                    name = result.getString(1);
                    System.out.println("sdb.Connection.getUsers() 2 : " + name);
                    User user = new User(m_Connection, name);
                    users.add(user);
                    names.add(name);
                }
                result.close();
                statement.close();
            }
            catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return new Container<User>(m_Connection, m_provider, users, names, "com.sun.star.beans.XPropertySet");
    }


    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public XNameAccess getViews()
    {
        // TODO: Implement me!!!
        XNameAccess views = _getViews();
        System.out.println("Connection.getViews() *************************");
        return views;
    }

    public XNameAccess _getViews()
    {
        String catalog = null;
        String schema = null;
        String name = null;
        List<String> names = new ArrayList<String>();
        List<View> views = new ArrayList<View>();
        try {
            java.sql.DatabaseMetaData metadata = m_Connection.getMetaData();
            String[] types = {"VIEW"};
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            String query = m_provider.getViewQuery();
            while (result.next())
            {
                catalog = result.getString(1);
                schema = result.getString(2);
                name = result.getString(3);
                View view = new View(m_Connection, query, catalog, schema, name);
                views.add(view);
                names.add(name);
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return new Container<View>(m_Connection, m_provider, views, names, "com.sun.star.sdbcx.View", TypeClass.SERVICE);
    }

    protected XStatement _getStatement(XComponentContext ctx,
                                       DriverProvider provider,
                                       java.sql.Connection connection)
    throws java.sql.SQLException
    {
        return new Statement(ctx, provider, this, connection, m_info);
    }

    protected XPreparedStatement _getPreparedStatement(XComponentContext ctx,
                                                       DriverProvider provider,
                                                       java.sql.Connection connection,
                                                       String sql)
    throws java.sql.SQLException
    {
        System.out.println("sdb.Connection._getPreparedStatement() 1: '" + sql + "'");
        return new PreparedStatement(ctx, provider, this, connection, sql, m_info);
    }
         
    protected XPreparedStatement _getCallableStatement(XComponentContext ctx,
                                                       DriverProvider provider,
                                                       java.sql.Connection connection,
                                                       String sql)
    throws java.sql.SQLException
    {
        System.out.println("sdb.Connection._getCallableStatement() 1: '" + sql + "'");
        return new CallableStatement(ctx, provider, this, connection, sql, m_info);
    }


}