/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
import java.util.Set;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XChild;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.NoSupportException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdb.XCommandPreparation;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XDataSource;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XUsersSupplier;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.sdbcx.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.ViewContainer;


public final class Connection
    extends ConnectionSuper
    implements XUsersSupplier,
               XGroupsSupplier,
               XCommandPreparation,
               XChild {

    private static final String SERVICE = Connection.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.Connection",
                                              "com.sun.star.sdbc.Connection",
                                              "com.sun.star.sdbcx.DatabaseDefinition"};
    private UserContainer mUsers = null;
    private GroupContainer mGroups = null;
    private XDataSource mDataSource = null;

    // The constructor method:
    protected Connection(XComponentContext ctx,
                         Provider provider,
                         String url,
                         PropertyValue[] info,
                         Set<String> properties) {
        super(ctx, SERVICE, SERVICES, provider, url, info, properties);
        mDataSource = DocumentContainer.getDataSource();
        System.out.println("sdb.Connection() *************************");
    }

    protected Provider getProvider() {
        return super.getProvider();
    }

    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (mUsers != null) {
            mUsers.dispose();
            mUsers = null;
        }
        if (mGroups != null) {
            mGroups.dispose();
            mGroups = null;
        }
        super.postDisposing();
    }

    // com.sun.star.container.XChild:
    @Override
    public XInterface getParent() {
        System.out.println("sdb.Connection.getParent() 1 *************************");
        return mDataSource;
    }

    @Override
    public void setParent(Object arg0) throws NoSupportException {
        System.out.println("sdb.Connection.setParent() *************************");
    }


    // com.sun.star.sdb.XCommandPreparation:
    @Override
    public XPreparedStatement prepareCommand(String command, int type)
        throws SQLException {
        System.out.println("sdb.Connection.prepareCommand() *************************");
        return null;
    }

    /*

    // import com.sun.star.lang.XMultiServiceFactory:
    @Override
    public Object createInstance(String service)
        throws Exception {
        System.out.println("sdb.Connection.createInstance() *************************");
        return null;
    }


    @Override
    public Object createInstanceWithArguments(String service, Object[] arguments) throws Exception {
        System.out.println("sdb.Connection.createInstanceWithArguments() *************************");
        return null;
    }


    @Override
    public String[] getAvailableServiceNames() {
        System.out.println("sdb.Connection.getAvailableServiceNames() *************************");
        String[] services = {"com.sun.star.sdb.SQLQueryComposer"};
        return services;
    }


    // com.sun.star.sdb.XQueriesSupplier:
    @Override
    public XNameAccess getQueries() {
        System.out.println("sdb.Connection.getQueries() *************************");
        return null;
    }



    public XSingleSelectQueryComposer createSingleSelectQueryComposer()
        throws Exception {
        final XMultiServiceFactory connectionFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class,
                                                                                 m_connection );
        String service = "com.sun.star.sdb.SingleSelectQueryComposer";
        return UnoRuntime.queryInterface(XSingleSelectQueryComposer.class, connectionFactory.createInstance(service));
    }


    // com.sun.star.sdb.XSQLQueryComposerFactory:
    @Override
    public XSQLQueryComposer createQueryComposer() {
        System.out.println("sdb.Connection.createQueryComposer() *************************");
        return null;
    }*/


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public synchronized XNameAccess getUsers() {
        return getUsersInternal();
    }

    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups() {
        return getGroupsInternal();
    }


    protected synchronized GroupContainer getGroupsInternal() {
        checkDisposed();
        if (mGroups == null) {
            refreshGroups();
        }
        return mGroups;
    }

    protected synchronized UserContainer getUsersInternal() {
        checkDisposed();
        if (mUsers == null) {
            refreshUsers();
        }
        return mUsers;
    }

    protected XStatement getStatement() {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement getPreparedStatement(String sql)
        throws SQLException {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_STATEMENT, sql);
        PreparedStatement statement = new PreparedStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_STATEMENT_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    @Override
    protected XPreparedStatement getCallableStatement(String sql)
        throws SQLException {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARE_CALL, sql);
        CallableStatement statement = new CallableStatement(this, sql);
        getStatements().put(statement, statement);
        String services = String.join(", ", statement.getSupportedServiceNames());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_PREPARED_CALL_ID,
                           services, statement.getLogger().getObjectId());
        return statement;
    }

    public synchronized void refresh() {
        super.refresh();
        refreshUsers();
        refreshGroups();
    }

    public void refreshUsers() {
        String query = getProvider().getConfigDCL().getUsersQuery();
        if (query != null) {
            List<String> names = new ArrayList<>();
            try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
                try (java.sql.ResultSet result = statement.executeQuery(query)) {
                    while (result.next()) {
                        String name = result.getString(1);
                        if (!result.wasNull() && !name.isBlank()) {
                            names.add(name);
                        }
                    }
                }
                if (mUsers == null) {
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USERS);
                    mUsers = new UserContainer(this, getProvider().isCaseSensitive(), names);
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USERS_ID,
                                       mUsers.getLogger().getObjectId());
                } else {
                    mUsers.refill(names);
                }
            } catch (ElementExistException | java.sql.SQLException e) {
                throw new com.sun.star.uno.RuntimeException("Error", e);
            }
        }
    }

    public void refreshGroups() {
        String query = getProvider().getConfigDCL().getGroupsQuery();
        if (query != null) {
            List<String> names = new ArrayList<>();
            try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
                try (java.sql.ResultSet result = statement.executeQuery(query)) {
                    while (result.next()) {
                        String name = result.getString(1);
                        if (!result.wasNull() && !name.isBlank()) {
                            names.add(name);
                        }
                    }
                }
                if (mGroups == null) {
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPS);
                    mGroups = new GroupContainer(this, getProvider().isCaseSensitive(), names);
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPS_ID,
                                       mGroups.getLogger().getObjectId());
                }  else {
                    mGroups.refill(names);
                }
            } catch (ElementExistException | java.sql.SQLException e) {
                throw new com.sun.star.uno.RuntimeException("Error", e);
            }
        }
    }

    @Override
    protected TableContainer getTableContainer(List<String> names)
        throws ElementExistException {
        TableContainer tables = new TableContainer(this, getProvider().isCaseSensitive(), names);
        System.out.println("sdb.Connection.getTableContainer() *************************");
        return tables;
    }

    @Override
    protected ViewContainer getViewContainer(List<String> names)
        throws ElementExistException {
        ViewContainer views = new ViewContainer(this, getProvider().isCaseSensitive(), names);
        System.out.println("sdb.Connection.getViewContainer() *************************");
        return views;
    }

}