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

import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBRoleHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.sdbcx.Container;


public class UserContainer
    extends Container<User>
{
    private static final String m_service = UserContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Users",
                                                "com.sun.star.sdbcx.Container"};
    protected final Connection m_connection;
    private final ConnectionLog m_logger; 

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    @Override
    public void dispose()
    {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_DISPOSING);
        super.dispose();
    }

    @Override
    protected User getElement(int index)
        throws SQLException
    {
        return super.getElement(index);
    }
    @Override
    protected User getElement(String name)
        throws SQLException
    {
        return super.getElement(name);
    }


    // The constructor method:
    public UserContainer(Connection connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        this(connection, sensitive, names, LoggerObjectType.USERCONTAINER);
    }

    public UserContainer(Connection connection,
                         boolean sensitive,
                         List<String> names,
                         LoggerObjectType type)
        throws ElementExistException
    {
        super(m_service, m_services, connection, sensitive, names);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), type);
    }

    @Override
    protected User appendElement(XPropertySet descriptor)
        throws SQLException
    {
        User user = null;
        String name = getElementName(descriptor);
        if (_createUser(descriptor, name)) {
            user = createElement(name);
        }
        return user;
    }

    protected boolean _createUser(XPropertySet descriptor,
                                  String name)
        throws SQLException
    {
        String query = null;
        try {
            query = DBRoleHelper.getCreateUserQuery(m_connection.getProvider(), descriptor, name, isCaseSensitive());
            System.out.println("sdbcx.UserContainer._createUser() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_CREATE_USER_QUERY, name, query);
            return DBTools.executeDDLQuery(m_connection.getProvider(), query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_USERS_CREATE_USER_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected User createElement(String name)
        throws SQLException
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USER);
        User user = new User(m_connection, isCaseSensitive(), name);
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USER_ID, user.getLogger().getObjectId());
        return user;
    }


    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        String query = null;
        try {
            query = DBRoleHelper.getDropUserQuery(m_connection.getProvider(), name, isCaseSensitive());
            System.out.println("sdbcx.UserContainer.removeDataBaseElement() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_USERS_REMOVE_USER_QUERY, name, query);
            if (DBTools.executeDDLQuery(m_connection.getProvider(), query)) {
                // XXX: A user has just been deleted, they should also be deleted from any role they are a member of...
                m_connection.getGroupsInternal().removeRole(name);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_USERS_REMOVE_USER_QUERY_ERROR;
            String msg = getLogger().getStringResource(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected void refreshInternal()
    {
        m_connection.refresh();
    }

    @Override
    protected void refill(List<String> names)
    {
        super.refill(names);
    }

    @Override
    protected XPropertySet createDescriptor()
    {
        return new UserDescriptor(isCaseSensitive());
    }

    protected void removeRole(String name)
        throws SQLException
    {
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
