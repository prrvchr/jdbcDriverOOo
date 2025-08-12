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


import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.sdbcx.RoleContainer;

public final class Groups
    extends RoleContainer<Group> {

    private static final String SERVICE = Groups.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Groups",
                                              "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public Groups(Connection connection,
                  GroupContainer groups,
                  String[] names,
                  String role,
                  boolean sensitive,
                  boolean isrole) {
        // XXX: isrole lets you know the role that holds this class.
        // XXX: Currently it is a role or a user
        super(SERVICE, SERVICES, connection, connection.getProvider(),
              groups, names, role, sensitive, isrole, getRoleName(isrole), LoggerObjectType.GROUPS);
    }

    private GroupContainer getGroups() {
        return (GroupContainer) mRoles;
    }

    protected Connection getConnection() {
        return (Connection) mConnection;
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    protected Provider getProvider() {
        return mProvider;
    }

    @Override
    public XPropertySet createDataDescriptor() {
        return new GroupDescriptor(isCaseSensitive());
    }

    @Override
    public void dispose() {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPROLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected void refill(String[] roles) {
        super.refill(roles);
    }

    @Override
    protected void removeElement(String name) {
        super.removeElement(name);
    }

    protected Group createRoleElement(String name) throws SQLException {
        if (!mNames.contains(name) || !getGroups().getNamesInternal().contains(name)) {
            throw new SQLException();
        }
        try {
            int idx = getGroups().getIndexInternal(name);
            return getGroups().getElementByIndex(idx);
        } catch (WrappedTargetException e) {
            throw new SQLException();
        }
    }

    private static final String getRoleName(boolean isrole) {
        String name = null;
        if (isrole) {
            name = "";
        }
        return name;
    }

}
