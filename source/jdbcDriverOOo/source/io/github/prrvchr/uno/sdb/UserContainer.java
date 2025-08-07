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

import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.RoleHelper;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.ContainerSuper;


public class UserContainer
    extends ContainerSuper<User> {
    private static final String SERVICE = UserContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Container"};

    protected final Connection mConnection;
    private final ConnectionLog mLogger;

    // The constructor method:
    protected UserContainer(Connection connection,
                            List<String> names,
                            boolean sensitive)
        throws ElementExistException {
        this(connection, sensitive, names, LoggerObjectType.USERCONTAINER);
    }

    protected UserContainer(Connection connection,
                            boolean sensitive,
                            List<String> names,
                            LoggerObjectType type)
        throws ElementExistException {
        super(SERVICE, SERVICES, connection, sensitive, names);
        mConnection = connection;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), type);
    }

    @Override
    protected List<String> getIndexes() {
        return getNamesInternal();
    }

    @Override
    protected User getElementByIndex(int index) throws WrappedTargetException {
        return super.getElementByIndex(index);
    }

    @Override
    protected User appendElement(XPropertySet descriptor)
        throws SQLException {
        String name = getElementName(descriptor);
        if (!mConnection.getProvider().getConfigDCL().supportsCreateUser()) {
            int resource = Resources.STR_LOG_USERS_CREATE_USER_FEATURE_NOT_IMPLEMENTED;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name);
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }
        User user = null;
        if (createUser(descriptor, name)) {
            user = createElement(name);
        }
        return user;
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    @Override
    public void dispose() {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_DISPOSING);
        super.dispose();
    }

    @Override
    protected User getElement(int index)
        throws SQLException {
        return super.getElement(index);
    }
    @Override
    protected User getElement(String name)
        throws SQLException {
        return super.getElement(name);
    }

    protected boolean createUser(XPropertySet descriptor,
                                 String name)
        throws SQLException {
        String query = null;
        try {
            query = RoleHelper.getCreateUserCommand(mConnection.getProvider(), descriptor, name, isCaseSensitive());
            System.out.println("sdbcx.UserContainer._createUser() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_CREATE_USER_QUERY, name, query);
            return DBTools.executeSQLQuery(mConnection.getProvider(), query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_USERS_CREATE_USER_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected User createElement(String name)
        throws SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USER);
        User user = new User(mConnection, mConnection.getGroupsInternal(), name, isCaseSensitive());
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USER_ID, user.getLogger().getObjectId());
        return user;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        String query = null;
        try {
            query = RoleHelper.getDropUserCommand(mConnection.getProvider(), name, isCaseSensitive());
            System.out.println("sdbcx.UserContainer.removeDataBaseElement() 1 SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_REMOVE_USER_QUERY, name, query);
            if (DBTools.executeSQLQuery(mConnection.getProvider(), query)) {
                // XXX: A user has just been deleted, they should also be deleted from any role they are a member of...
                System.out.println("sdbcx.UserContainer.removeDataBaseElement() 2");
                //mConnection.getGroupsInternal().removeRole(name);
                System.out.println("sdbcx.UserContainer.removeDataBaseElement() 3");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            int resource = Resources.STR_LOG_USERS_REMOVE_USER_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        } catch (Throwable e) {
            System.out.println("sdbcx.UserContainer.removeDataBaseElement() ERROR");
            e.printStackTrace();
        }
    }

    @Override
    protected void refreshInternal() {
        mConnection.refresh();
    }

    @Override
    protected void refill(List<String> names) {
        super.refill(names);
    }

    @Override
    protected XPropertySet createDescriptor() {
        XPropertySet descriptor = null;
        if (mConnection.getProvider().getConfigDCL().supportsCreateUser()) {
            descriptor =  new UserDescriptor(isCaseSensitive());
        }
        return descriptor;
    }

    protected void removeRole(String name)
        throws SQLException {
        Iterator<User> users = getActiveElements();
        while (users.hasNext()) {
            Groups groups = users.next().getGroupsInternal();
            if (groups.hasByName(name)) {
                System.out.println("sdb.UserContainer.removeRole() Role: " + name);
                groups.removeElement(name);
            }
        }
    }

}
