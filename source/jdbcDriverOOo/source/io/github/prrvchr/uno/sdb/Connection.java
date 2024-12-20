/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
import com.sun.star.uno.XInterface;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.sdbcx.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.ViewContainer;


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

    protected DriverProvider getProvider()
    {
        return super.getProvider();
    }
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      String url,
                      PropertyValue[] info)
    {
        super(ctx, m_service, m_services, provider, url, info);
        System.out.println("sdb.Connection() *************************");
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
    public XInterface getParent()
    {
        XInterface parent = null;
        System.out.println("sdb.Connection.getParent() 1 *************************");
        if (getProvider().hasDocument()) {
            System.out.println("sdb.Connection.getParent() 2 *************************");
            parent = getProvider().getDocument().getDataSource();
        }
        return parent;
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
        return getUsersInternal();
    }

    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        return getGroupsInternal();
    }


    protected synchronized GroupContainer getGroupsInternal()
    {
        checkDisposed();
        if (m_Groups == null) {
            refreshGroups();
        }
        return m_Groups;
    }

    protected synchronized UserContainer getUsersInternal()
    {
        checkDisposed();
        if (m_Users == null) {
            refreshUsers();
        }
        return m_Users;
    }

    protected XStatement _getStatement()
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID, services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement _getPreparedStatement(String sql)
        throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID, services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement _getCallableStatement(String sql)
        throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID, services, statement.getLogger().getObjectId());
        return statement;
    }

    public synchronized void refresh()
    {
        super.refresh();
        refreshUsers();
        refreshGroups();
    }


    public void refreshUsers()
    {
        String query = getProvider().getUsersQuery();
        if (query == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        try (java.sql.Statement statement = getProvider().getConnection().createStatement())
        {
            try (java.sql.ResultSet result = statement.executeQuery(query))
            {
                while (result.next()) {
                    String name = result.getString(1);
                    if (!result.wasNull() && !name.isBlank()) {
                        names.add(name);
                    }
                }
            }
            if (m_Users == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USERS);
                m_Users = new UserContainer(this, getProvider().isCaseSensitive(User.class.getName()), names);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USERS_ID, m_Users.getLogger().getObjectId());
            }
            else {
                m_Users.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    public void refreshGroups()
    {
        String query = getProvider().getGroupsQuery();
        if (query == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        try (java.sql.Statement statement = getProvider().getConnection().createStatement())
        {
            try (java.sql.ResultSet result = statement.executeQuery(query))
            {
                while (result.next()) {
                    String name = result.getString(1);
                    if (!result.wasNull() && !name.isBlank()) {
                        names.add(name);
                    }
                }
            }
            if (m_Groups == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPS);
                m_Groups = new GroupContainer(this, getProvider().isCaseSensitive(Group.class.getName()), names);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPS_ID, m_Groups.getLogger().getObjectId());
            }
            else {
                m_Groups.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    @Override
    protected TableContainer getTableContainer(List<String> names)
        throws ElementExistException
    {
        TableContainer tables = new TableContainer(this, getProvider().isCaseSensitive(null), names);
        System.out.println("sdb.Connection.getTableContainer() *************************");
        return tables;
    }

    @Override
    protected ViewContainer getViewContainer(List<String> names)
        throws ElementExistException
    {
        ViewContainer views = new ViewContainer(this, getProvider().isCaseSensitive(null), names);
        System.out.println("sdb.Connection.getViewContainer() *************************");
        return views;
    }

}