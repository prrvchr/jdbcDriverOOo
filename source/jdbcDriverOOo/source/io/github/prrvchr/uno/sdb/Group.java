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

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.XUsersSupplier;

import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class Group
    extends Role
    implements XUsersSupplier {

    private static final String SERVICE = Group.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Group"};
    private Users mUsers;

    // The constructor method:
    public Group(Connection connection,
                 boolean sensitive,
                 String name) {
        super(SERVICE, SERVICES, connection, sensitive, name, LoggerObjectType.GROUP, true);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers() {
        XNameAccess users = null;
        try {
            if (mUsers == null) {
                refreshUsers();
            }
            users = mUsers;
        } catch (java.lang.Exception e) {
            System.out.println("sdbcx.Group.getUsers() ERROR: " + UnoHelper.getStackTrace(e));
        }
        return users;
    }

    protected void refreshUsers() {
        List<String> users = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        String query = mConnection.getProvider().getConfigDCL().getGroupUsersQuery(getName(), values);
        if (query != null) {
            try (PreparedStatement statement = mConnection.getProvider().getConnection().prepareStatement(query)) {
                for (int i = 0; i < values.size(); i++) {
                    statement.setObject(i + 1, values.get(i));
                }
                try (java.sql.ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String user = result.getString(1);
                        if (!result.wasNull()) {
                            users.add(user);
                        }
                    }
                }
                if (mUsers == null) {
                    mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USERROLES);
                    mUsers = new Users(mConnection, isCaseSensitive(), getName(), users);
                    mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USERROLES_ID,
                                   mUsers.getLogger().getObjectId());
                } else {
                    mUsers.refill(users);
                }
            } catch (ElementExistException | java.sql.SQLException e) {
                UnoHelper.getSQLException(e, mConnection);
            }

        }
    }

    protected Users getUsersInternal() {
        return mUsers;
    }

}
