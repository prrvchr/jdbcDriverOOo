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
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.RoleHelper;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.ContainerSuper;


public class GroupContainer
    extends ContainerSuper<Group> {
    private static final String SERVICE = GroupContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Container"};

    protected final Connection mConnection;
    private final ConnectionLog mLogger;

    // The constructor method:
    public GroupContainer(Connection connection,
                          String[] names,
                          boolean sensitive) {
        this(connection, names, sensitive, LoggerObjectType.GROUPCONTAINER);
    }

    protected GroupContainer(Connection connection,
                             String[] names,
                             boolean sensitive,
                             LoggerObjectType type) {
        super(SERVICE, SERVICES, connection, sensitive, names);
        mConnection = connection;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), type);
    }

    @Override
    protected List<String> getNamesInternal() {
        return super.getNamesInternal();
    }

    @Override
    protected int getIndexInternal(String name) {
        return super.getIndexInternal(name);
    }

    @Override
    protected int getIndexInternal(int index) {
        return super.getIndexInternal(index);
    }

    @Override
    protected Group getElementByIndex(int idx) throws WrappedTargetException {
        return super.getElementByIndex(idx);
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    @Override
    public void dispose() {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_DISPOSING);
        super.dispose();
    }


    @Override
    protected Group appendElement(XPropertySet descriptor)
        throws SQLException {
        Group group = null;
        String name = getElementName(descriptor);
        if (createGroup(descriptor, name)) {
            group = createElement(name);
        }
        return group;
    }

    protected boolean createGroup(XPropertySet descriptor,
                                  String name)
        throws SQLException {
        String query = null;
        try {
            query = RoleHelper.getCreateGroupCommand(mConnection.getProvider(), descriptor, name, isCaseSensitive());
            System.out.println("sdbcx.GroupContainer._createGroup() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY, name, query);
            return DBTools.executeSQLQuery(mConnection.getProvider(), query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected Group createElement(String name)
        throws SQLException {
        System.out.println("sdbcx.GroupContainer.createElement() name: " + name);
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CREATE_GROUP);
        Group group = new Group(mConnection, this, mConnection.getUsersInternal(), name, isCaseSensitive());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUP_ID, group.getLogger().getObjectId());
        return group;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        String query = null;
        Provider provider = mConnection.getProvider();
        try {
            query = RoleHelper.getDropGroupCommand(provider, name, isCaseSensitive());
            System.out.println("sdbcx.GroupContainer.removeDataBaseElement() SQL: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_REMOVE_GROUP_QUERY, name, query);
            if (DBTools.executeSQLQuery(provider, query)) {
                // XXX: A role has just been deleted, it should also be deleted from any member user...
                mConnection.getUsersInternal().removeRole(name);
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_GROUPS_REMOVE_GROUP_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }

    }

    @Override
    protected void refreshInternal() {
        mConnection.refresh();
    }

    @Override
    protected void refill(String[] names) {
        super.refill(names);
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new GroupDescriptor(isCaseSensitive());
    }

    protected void removeRole(String name)
        throws SQLException {
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
