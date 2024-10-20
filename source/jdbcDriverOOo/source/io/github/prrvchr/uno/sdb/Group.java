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
import java.util.HashMap;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.XUsersSupplier;

import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class Group
    extends Role
    implements XUsersSupplier
{

    private static final String m_service = Group.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Group"};
    private Users m_users;

    // The constructor method:
    public Group(Connection connection,
                 boolean sensitive,
                 String name)
    {
        super(m_service, m_services, connection, sensitive, name, LoggerObjectType.GROUP, true);
        registerProperties(new HashMap<String, PropertyWrapper>());
    }


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers() {
        try {
            if (m_users == null) {
                refreshUsers();
            }
            return m_users;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Group.getUsers() ERROR: " + UnoHelper.getStackTrace(e));
        }
        return null;
    }

    protected void refreshUsers()
    {
        List<String> users = new ArrayList<>();
        String query = m_connection.getProvider().getGroupUsersQuery();
        if (query != null) {
            try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(query))
            {
                statement.setString(1, getName());
                try (java.sql.ResultSet result = statement.executeQuery())
                {
                    while(result.next()) {
                        String user = result.getString(1);
                        if (!result.wasNull()) {
                            users.add(user);
                        }
                    }
                }
                if (m_users == null) {
                    m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USERROLES);
                    m_users = new Users(m_connection, isCaseSensitive(), getName(), users);
                    m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USERROLES_ID, m_users.getLogger().getObjectId());
                }
                else {
                    m_users.refill(users);
                }
            }
            catch (ElementExistException | java.sql.SQLException e) {
                UnoHelper.getSQLException(e, m_connection);
            }

        }
    }

    protected Users getUsersInternal()
    {
        return m_users;
    }

}
