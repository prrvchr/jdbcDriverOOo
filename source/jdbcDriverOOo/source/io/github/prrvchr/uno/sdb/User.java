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

import java.util.Arrays;
import java.util.List;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.UnoHelper;


public class User
    extends Role
    implements XUser
{

    private static final String m_service = User.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.User"};


    // The constructor method:
    public User(Connection connection,
                boolean sensitive,
                String name)
    {
        super(m_service, m_services, connection, sensitive, name, LoggerObjectType.USER);
    }

    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String old, String password)
        throws SQLException
    {
        String sql = DBTools.getChangeUserPasswordQuery(m_connection, getName(), password, isCaseSensitive());
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
    }

    // Private methods:
    @Override
    protected void _addGrantees(List<String> grantees) {
        grantees.addAll(Arrays.asList(getGroups().getElementNames()));
    }

    @Override
    protected int _getGrantPrivilegesResource()
    {
        return Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY;
    }

    @Override
    protected int _getRevokePrivilegesResource()
    {
        return Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY;
    }

}
