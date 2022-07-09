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
import java.util.Collections;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.jdbcdriver.DataBaseTools.NameComponents;
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
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE IS_GRANTABLE = 'YES' AND ";
        return _getPrivileges(name, sql);
    }

    @Override
    public int getPrivileges(String name, int type)
        throws SQLException
    {
        String sql = "SELECT PRIVILEGE_TYPE FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE ";
        return _getPrivileges(name, sql);
    }

    private int _getPrivileges(String name,
                               String sql)
        throws SQLException
    {
        String[] groups = getGroups().getElementNames();
        NameComponents component = DataBaseTools.qualifiedNameComponents(m_connection, name, ComposeRule.InDataManipulation);
        int privilege = 0;
        if (!component.getCatalog().isEmpty()) {
            sql += "TABLE_CATALOG = ? AND ";
        }
        if (!component.getSchema().isEmpty()) {
            sql += "TABLE_SCHEMA = ? AND ";
        }
        sql += String.format("TABLE_NAME = ? AND GRANTEE IN (%s)", String.join(", ", new ArrayList<>(Collections.nCopies(groups.length + 1, "?"))));
        try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(sql)){
            int next = 1;
            if (!component.getCatalog().isEmpty()) {
                statement.setString(next++, component.getCatalog());
            }
            if (!component.getSchema().isEmpty()) {
                statement.setString(next++, component.getSchema());
            }
            statement.setString(next++, component.getTable());
            statement.setString(next++, getName());
            for (String group : groups) {
                statement.setString(next++, group);
            }
            java.sql.ResultSet result = statement.executeQuery();
            while (result.next()) {
                privilege |= m_connection.getProvider().getPrivilege(result.getString(1));
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e);
        }
        return privilege;
    }

    @Override
    public void grantPrivileges(String name, int type, int privilege) throws SQLException {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.User.grantPrivileges() Name: " + name + " - Type: " + type + " - Privilege: " + privilege);
    }

    @Override
    public void revokePrivileges(String name, int type, int privilege) throws SQLException {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.User.revokePrivileges() Name: " + name + " - Type: " + type + " - Privilege: " + privilege);
    }


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups() {
        try {
            System.out.println("sdbcx.User.getGroups() 1");
            if (m_groups == null) {
                m_groups = _refreshGroups();
            }
            System.out.println("sdbcx.User.getGroups() 2");
            return m_groups;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.User.getGroups() 3" + UnoHelper.getStackTrace(e));
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
        return new GroupContainer(m_connection, isCaseSensitive(), groups);
    }


}
