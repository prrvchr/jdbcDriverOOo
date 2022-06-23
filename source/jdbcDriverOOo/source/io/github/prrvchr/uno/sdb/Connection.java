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

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.SchemaCrawler;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbcx.Statement;
//import io.github.prrvchr.uno.sdbcx.Column;
//import io.github.prrvchr.uno.sdbcx.Table;
//import io.github.prrvchr.uno.sdbcx.TableDescriptor;
import io.github.prrvchr.uno.sdbcx.TableContainer;
import io.github.prrvchr.uno.sdbcx.UserContainer;
import io.github.prrvchr.uno.sdbcx.ViewContainer;


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
                      PropertyValue[] info,
                      boolean enhanced)
        throws java.sql.SQLException
    {
        super(ctx, m_name, m_services, provider, connection, url, info, enhanced, m_crawler);
        System.out.println("sdb.Connection() 1");
    }


    // com.sun.star.container.XChild:
    @Override
    public Object getParent()
    {
        System.out.println("sdb.Connection.getParent() *************************");
        return null;
    }


    @Override
    public void setParent(Object arg0) throws NoSupportException
    {
        System.out.println("sdb.Connection.setParent() *************************");
    }


    // com.sun.star.sdb.XCommandPreparation:
    @Override
    public XPreparedStatement prepareCommand(String command, int type)
    throws SQLException
    {
        System.out.println("sdb.Connection.prepareCommand() *************************");
        return null;
    }


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        System.out.println("sdb.Connection.getGroups() *************************");
        return null;
    }


    // import com.sun.star.lang.XMultiServiceFactory:
    @Override
    public Object createInstance(String service)
    throws Exception
    {
        System.out.println("sdb.Connection.createInstance() *************************");
        return null;
    }


    @Override
    public Object createInstanceWithArguments(String service, Object[] arguments) throws Exception {
        System.out.println("sdb.Connection.createInstanceWithArguments() *************************");
        return null;
    }


    @Override
    public String[] getAvailableServiceNames()
    {
        System.out.println("sdb.Connection.getAvailableServiceNames() *************************");
        String[] services = {"com.sun.star.sdb.SQLQueryComposer"};
        return services;
    }


    // com.sun.star.sdb.XQueriesSupplier:
    @Override
    public XNameAccess getQueries()
    {
        System.out.println("sdb.Connection.getQueries() *************************");
        return null;
    }


    // com.sun.star.sdb.XSQLQueryComposerFactory:
    @Override
    public XSQLQueryComposer createQueryComposer()
    {
        System.out.println("sdb.Connection.createQueryComposer() *************************");
        return null;
    }


    // com.sun.star.sdbcx.XTablesSupplier:
    @Override
    public XNameAccess getTables()
    {
        System.out.println("sdb.Connection.getTables() 1");
        XNameAccess tables = null;
        try {
            System.out.println("sdb.Connection.getTables() 2");
            tables = m_crawler ? SchemaCrawler.getTables(m_provider, this) :
                                 new TableContainer<Table, TableDescriptor, Column>(this, Table.class, TableDescriptor.class);
            System.out.println("sdb.Connection.getTables() 3");
         }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        System.out.println("sdb.Connection.getTables() 4");
        return tables;
    }

    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers()
    {
        System.out.println("sdb.Connection.getUsers() 1");
        return new UserContainer(this);
    }


    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public XNameAccess getViews()
    {
        System.out.println("sdb.Connection.getViews() *************************");
        return new ViewContainer(this);
    }


    protected XStatement _getStatement()
    {
        System.out.println("sdb.Connection._getStatement() *****************************");
        Statement statement = new Statement(this);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql)
    {
        System.out.println("sdb.Connection._getPreparedStatement() *****************************: '" + sql + "'");
        PreparedStatement statement = new PreparedStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
    {
        System.out.println("sdb.Connection._getCallableStatement() *****************************: '" + sql + "'");
        CallableStatement statement = new CallableStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }


}