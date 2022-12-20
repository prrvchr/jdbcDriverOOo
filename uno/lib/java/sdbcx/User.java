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
import java.util.Arrays;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.PrivilegeObject;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.uno.helper.ComposeRule;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class User
    extends Descriptor
    implements XUser,
               XGroupsSupplier
{

    private static final String m_service = User.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.User"};

    private Connection m_connection;
    private GroupContainer m_groups;

    // The constructor method:
    public User(Connection connection,
                boolean sensitive,
                String name)
    {
        super(m_service, m_services, sensitive, name);
        m_connection = connection;
    }


    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String old, String password) throws SQLException {
        String sql = DataBaseTools.getChangeUserPasswordQuery(m_connection, getName(), password, isCaseSensitive());
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
    }


    // com.sun.star.sdbcx.XAuthorizable <- XUser:
    @Override
    public int getGrantablePrivileges(String name, int type)
        throws SQLException
    {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            grantees.addAll(Arrays.asList(getGroups().getElementNames()));
            privileges = DataBaseTools.getTableOrViewGrantablePrivileges(m_connection, grantees, name);
        }
        return privileges;
    }

    @Override
    public int getPrivileges(String name, int type)
        throws SQLException
    {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            //grantees.addAll(Arrays.asList(getGroups().getElementNames()));
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


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups() {
        try {
            if (m_groups == null) {
                m_groups = _refreshGroups();
            }
            return m_groups;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.User.getGroups() ERROR: " + UnoHelper.getStackTrace(e));
        }
        return null;
    }


    private GroupContainer _refreshGroups()
        throws ElementExistException
    {
        ArrayList<String> groups = new ArrayList<>();
        String sql = m_connection.getProvider().getUserGroupsQuery();
        try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(sql)){
            statement.setString(1, getName());
            java.sql.ResultSet result = statement.executeQuery();
            while(result.next()) {
                String group = result.getString(1);
                groups.add(group);
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
        return new UserGroupContainer(m_connection, isCaseSensitive(), groups, getName());
    }


}
