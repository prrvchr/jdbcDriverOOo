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

import java.util.HashMap;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.helper.RoleHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class User
    extends Role
    implements XUser {

    private static final String SERVICE = User.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.User"};


    // The constructor method:
    public User(Connection connection,
                GroupContainer groups,
                String name,
                boolean sensitive) {
        super(SERVICE, SERVICES, connection, groups, name, sensitive, LoggerObjectType.USER, false);
        registerProperties(new HashMap<PropertyID, PropertyWrapper>());
    }

    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String old, String password)
        throws SQLException {
        String query = null;
        try (java.sql.Statement statement = mConnection.getProvider().getConnection().createStatement()) {
            int resource = Resources.STR_LOG_USER_CHANGE_PASSWORD_QUERY;
            ConfigDCL config = mConnection.getProvider().getConfigDCL();
            NamedSupport support = mConnection.getProvider().getNamedSupport();
            query = RoleHelper.getChangeUserPasswordCommand(config, support, getName(),
                                                            password, isCaseSensitive());
            getLogger().logprb(LogLevel.INFO, resource, getName());
            statement.execute(query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_USER_CHANGE_PASSWORD_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, getName());
            throw UnoHelper.getSQLException(new java.sql.SQLException(msg, e.getSQLState(), e.getErrorCode(), e), this);
        }
    }

}
