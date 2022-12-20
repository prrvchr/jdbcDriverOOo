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

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XChild;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.NoSupportException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdb.XCommandPreparation;
import com.sun.star.sdb.XQueriesSupplier;
import com.sun.star.sdb.XSQLQueryComposer;
import com.sun.star.sdb.XSQLQueryComposerFactory;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XUsersSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.DriverProvider;
import io.github.prrvchr.uno.helper.Resources;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbc.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbcx.Group;
import io.github.prrvchr.uno.sdbcx.GroupContainer;
import io.github.prrvchr.uno.sdbcx.Statement;
import io.github.prrvchr.uno.sdbcx.User;
import io.github.prrvchr.uno.sdbcx.UserContainer;


public final class Connection
    extends ConnectionSuper
    implements XChild,
               XCommandPreparation,
               XQueriesSupplier,
               XSQLQueryComposerFactory,
               XMultiServiceFactory,
               XUsersSupplier,
               XGroupsSupplier
{

    private static final String m_service = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Connection",
                                                "com.sun.star.sdbc.Connection",
                                                "com.sun.star.sdbcx.DatabaseDefinition"};
    private UserContainer m_Users = null;
    private GroupContainer m_Groups = null;
    private static final boolean m_crawler = false;

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      ResourceBasedEventLogger logger,
                      boolean enhanced)
    {
        super(ctx, m_service, m_services, provider, logger, enhanced, m_crawler);
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (m_Users != null) {
            m_Users.dispose();
        }
        if (m_Groups != null) {
            m_Groups.dispose();
        }
        super.postDisposing();
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



    /*public XSingleSelectQueryComposer createSingleSelectQueryComposer()
        throws Exception
    {
        final XMultiServiceFactory connectionFactory = UnoRuntime.queryInterface( XMultiServiceFactory.class, m_connection );
        return UnoRuntime.queryInterface(
            XSingleSelectQueryComposer.class, connectionFactory.createInstance( "com.sun.star.sdb.SingleSelectQueryComposer" ) );
    }*/


    // com.sun.star.sdb.XSQLQueryComposerFactory:
    @Override
    public XSQLQueryComposer createQueryComposer()
    {
        System.out.println("sdb.Connection.createQueryComposer() *************************");
        return null;
    }


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public synchronized XNameAccess getUsers()
    {
        checkDisposed();
        if (m_Users == null) {
            _refreshUsers();
        }
        return m_Users;
    }

    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        checkDisposed();
        if (m_Groups == null) {
            _refreshGroups();
        }
        return m_Groups;
    }


    public synchronized UserContainer getUsersInternal()
    {
        return m_Users;
    }

    protected XStatement _getStatement()
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID, statement.getObjectId());
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql)
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID, statement.getObjectId());
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
    {
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        m_statements.put(statement, statement);
        m_logger.log(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID, statement.getObjectId());
        return statement;
    }

    public synchronized void _refresh()
    {
        super._refresh();
        _refreshUsers();
        _refreshGroups();
    }


    public void _refreshUsers()
    {
        try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
            java.sql.ResultSet result = statement.executeQuery(getProvider().getUserQuery());
            List<String> names = new ArrayList<>();
            System.out.println("sdb.Connection._refreshUsers() 1");
            while (result.next()) {
                String name = result.getString(1);
                System.out.println("sdb.Connection._refreshUsers() User Name: " + name);
                names.add(name);
            }
            System.out.println("sdb.Connection._refreshUsers() 2");
            result.close();
            if (m_Users == null) {
                m_Users = new UserContainer(this, getProvider().isCaseSensitive(User.class.getName()), names);
            }
            else {
                m_Users.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    public void _refreshGroups()
    {
        try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
            java.sql.ResultSet result = statement.executeQuery(getProvider().getGroupQuery());
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = result.getString(1);
                System.out.println("sdb.Connection._refreshGroups() Group Name: " + name);
                names.add(name);
            }
            result.close();
            if (m_Groups == null) {
                m_Groups = new GroupContainer(this, getProvider().isCaseSensitive(Group.class.getName()), names);
            }
            else {
                m_Groups.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    public Table getTable(boolean sensitive,
                          String catalog,
                          String schema,
                          String name,
                          String type,
                          String remarks)
    {
        return new Table(this, sensitive, catalog, schema, name, type, remarks);
    }


    /*    public synchronized XNameAccess getTables1()
    {
        m_catalog = SchemaCrawler.getCatalog(provider.getConnection());
        for (final schemacrawler.schema.Table t : m_catalog.getTables()) {
            System.out.println("Connection.Connection() 2 Table Type: " + t.getTableType().getTableType());
        }
        System.out.println("sdb.Connection.getTables() 1");
        checkDisposed();
        if (m_Tables == null) {
            try {
                System.out.println("sdb.Connection.getTables() 2");
                m_Tables = m_crawler ? SchemaCrawler.getTables(m_provider, this) :
                                       new TableContainer(this);
                System.out.println("sdb.Connection.getTables() 3");
            }
            catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("sdb.Connection.getTables() 4");
        return m_Tables;
    }*/


}