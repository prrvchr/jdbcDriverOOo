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

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.jdbcdriver.helper.DBRoleHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public final class User
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
        super(m_service, m_services, connection, sensitive, name, LoggerObjectType.USER, false);
    }

    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String old, String password)
        throws SQLException
    {
        String query = null;
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            int resource = Resources.STR_LOG_USER_CHANGE_PASSWORD_QUERY;
            query = DBRoleHelper.getChangeUserPasswordQuery(m_connection.getProvider(), getName(), password, isCaseSensitive());
            getLogger().logprb(LogLevel.INFO, resource, getName());
            statement.execute(query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_USER_CHANGE_PASSWORD_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, getName());
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

}
