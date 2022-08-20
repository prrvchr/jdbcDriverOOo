/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.PrivilegeObject;
import com.sun.star.sdbcx.XAuthorizable;
import com.sun.star.sdbcx.XUsersSupplier;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class Group
    extends Descriptor
    implements XUsersSupplier,
               XAuthorizable
{

    private static final String m_service = Group.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Group"};

    private Connection m_connection;
    private UserContainer m_users;

    // The constructor method:
    public Group(Connection connection,
                 boolean sensitive,
                 String name)
    {
        super(m_service, m_services, sensitive, name);
        m_connection = connection;
    }


    // com.sun.star.sdbcx.XAuthorizable:
    @Override
    public int getGrantablePrivileges(String name, int type) throws SQLException {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            privileges = DataBaseTools.getTableOrViewGrantablePrivileges(m_connection, grantees, name);
        }
        return privileges;
    }

    @Override
    public int getPrivileges(String name, int type) throws SQLException {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            privileges = DataBaseTools.getTableOrViewPrivileges(m_connection, grantees, name);
        }
        return privileges;
    }

    @Override
    public void grantPrivileges(String name, int type, int privilege) throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            DataBaseTools.grantTableOrViewPrivileges(m_connection, getName(), name, privilege, ComposeRule.InDataManipulation, isCaseSensitive());
        }
    }

    @Override
    public void revokePrivileges(String name, int type, int privilege) throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            DataBaseTools.revokeTableOrViewPrivileges(m_connection, getName(), name, privilege, ComposeRule.InDataManipulation, isCaseSensitive());
        }
    }


    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public XNameAccess getUsers() {
        try {
            System.out.println("sdbcx.Group.getUsers() 1");
            if (m_users == null) {
                m_users = _refreshUsers();
            }
            System.out.println("sdbcx.Group.getUsers() 2");
            return m_users;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.Group.getUsers() 3" + UnoHelper.getStackTrace(e));
        }
        return null;
    }


    private UserContainer _refreshUsers()
        throws ElementExistException
    {
        ArrayList<String> users = new ArrayList<>();
        String sql = m_connection.getProvider().getGroupUsersQuery();
        System.out.println("sdbcx.Group._refreshUsers() SQL: " + sql);
        try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(sql)){
            statement.setString(1, getName());
            java.sql.ResultSet result = statement.executeQuery();
            while(result.next()) {
                users.add(result.getString(1));
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
        return new GroupUserContainer(m_connection, isCaseSensitive(), users, getName());
    }


}
