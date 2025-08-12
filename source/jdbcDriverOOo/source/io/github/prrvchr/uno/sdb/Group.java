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
import java.util.HashMap;
import java.util.List;

import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.XUsersSupplier;

import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.sdbcx.RoleListener;


public final class Group
    extends Role
    implements XUsersSupplier {

    private static final String SERVICE = Group.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Group"};
    private Users mUsers;
    private UserContainer mUserContainer;
    private RoleListener<User> mListener;

    // The constructor method:
    public Group(Connection connection,
                 GroupContainer groups,
                 UserContainer users,
                 String name,
                 boolean sensitive) {
        super(SERVICE, SERVICES, connection, groups, name, sensitive, LoggerObjectType.GROUP, true);
        mUserContainer = users;
        registerProperties(new HashMap<String, PropertyWrapper>());
    }

    // com.sun.star.lang.XComponent
    @Override
    public void dispose() {
        if (mUsers != null) {
            synchronized (mUsers) {
                if (mListener != null) {
                    mUserContainer.removeContainerListener(mListener);
                }
                mUsers.dispose();
            }
        }
        super.dispose();
    }

    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers() {
        return getUsersInternal();
    }

    protected Users getUsersInternal() {
        checkDisposed();
        if (mUsers == null) {
            refreshUsers();
        }
        return mUsers;
    }

    protected void refreshUsers() {
        String[] users;
        if (mUsers == null) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USERROLES);
        }
        List<Object> values = new ArrayList<>();
        String query = mConnection.getProvider().getConfigDCL().getGroupUsersQuery(getName(), values);
        if (query != null) {
            int resource = Resources.STR_LOG_CREATE_USERROLES_ERROR;
            users = mConnection.getRoleNames(mLogger, values, query, resource);
        } else {
            users = new String[0];
            getLogger().logprb(LogLevel.SEVERE, Resources.STR_LOG_CREATE_USERROLES_NOT_SUPPORTED);
        }
        if (mUsers == null) {
            mUsers = new Users(mConnection, mUserContainer, users, getName(), isCaseSensitive());
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USERROLES_ID,
                           mUsers.getLogger().getObjectId());
            mListener = new RoleListener<User>(mUsers);
            mUserContainer.addContainerListener(mListener);
        } else {
            mUsers.refill(users);
        }
    }
}
