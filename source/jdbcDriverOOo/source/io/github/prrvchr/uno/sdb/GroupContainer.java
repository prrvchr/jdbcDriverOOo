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
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.Container;


public class GroupContainer
    extends Container<Group>
{
    private static final String m_service = GroupContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Groups",
                                                "com.sun.star.sdbcx.Container"};


    protected final Connection m_connection;
    private final ConnectionLog m_logger; 

    // The constructor method:
    public GroupContainer(Connection connection,
                          boolean sensitive,
                          List<String> names)
        throws ElementExistException
    {
        this(connection, sensitive, names, LoggerObjectType.GROUPCONTAINER);
    }

    public GroupContainer(Connection connection,
                          boolean sensitive,
                          List<String> names,
                          LoggerObjectType type)
        throws ElementExistException
    {
        super(m_service, m_services, connection, sensitive, names);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), type);
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    @Override
    public void dispose()
    {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_DISPOSING);
        super.dispose();
    }


    @Override
    protected Group appendElement(XPropertySet descriptor)
        throws SQLException
    {
        Group group = null;
        String name = getElementName(descriptor);
        if (_createGroup(descriptor, name)) {
            group = createElement(name);
        }
        return group;
    }

    protected boolean _createGroup(XPropertySet descriptor,
                                   String name)
        throws SQLException
    {
        String query = null;
        try {
            query = DBRoleHelper.getCreateGroupQuery(m_connection.getProvider(), descriptor, name, isCaseSensitive());
            System.out.println("sdbcx.GroupContainer._createGroup() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY, name, query);
            return DBTools.executeSQLQuery(m_connection.getProvider(), query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected Group createElement(String name)
        throws SQLException
    {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CREATE_GROUP);
        Group goup = new Group(m_connection, isCaseSensitive(), name);
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CREATED_GROUP_ID, goup.getLogger().getObjectId());
        return goup;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        String query = null;
        DriverProvider provider = m_connection.getProvider();
        try {
            query = DBRoleHelper.getDropGroupQuery(provider, name, isCaseSensitive());
            System.out.println("sdbcx.GroupContainer.removeDataBaseElement() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_REMOVE_GROUP_QUERY, name, query);
            if (DBTools.executeSQLQuery(provider, query)) {
                // XXX: A role has just been deleted, it should also be deleted from any member user...
                m_connection.getUsersInternal().removeRole(name);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_GROUPS_REMOVE_GROUP_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
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
        return new GroupDescriptor(isCaseSensitive());
    }

    protected void removeRole(String name)
        throws SQLException
    {
        Iterator<Group> groups = getActiveElements();
        while (groups.hasNext()) {
            Users users = groups.next().getUsersInternal();
            if (users.hasByName(name)) {
                System.out.println("sdb.GroupContainer.removeRole() Role: " + name);
                users.removeElement(name);
            }
        }
    }

}
