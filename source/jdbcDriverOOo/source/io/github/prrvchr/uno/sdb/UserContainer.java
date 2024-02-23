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

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.Container;


public class UserContainer
    extends Container<User>
{
    private static final String m_service = UserContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Users",
                                                "com.sun.star.sdbcx.Container"};
    protected final Connection m_connection;
    private final ConnectionLog m_logger; 


    // The constructor method:
    public UserContainer(Connection connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        this(connection, sensitive, names, LoggerObjectType.USERCONTAINER);
    }

    public UserContainer(Connection connection,
                         boolean sensitive,
                         List<String> names,
                         LoggerObjectType type)
        throws ElementExistException
    {
        super(m_service, m_services, connection, sensitive, names);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), type);
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected User _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        User user = null;
        if (_createUser(descriptor, name)) {
            user = _createElement(name);
        }
        return user;
    }

    protected boolean _createUser(XPropertySet descriptor,
                                  String name)
        throws SQLException
    {
        String query = DBTools.getCreateUserQuery(m_connection, descriptor, name, isCaseSensitive());
        System.out.println("sdbcx.UserContainer._createUser() SQL: " + query);
        return DBTools.executeDDLQuery(m_connection, query, m_logger, this.getClass().getName(),
                                       "_createView", Resources.STR_LOG_USERS_CREATE_USER_QUERY, name);
    }

    @Override
    protected User _createElement(String name)
        throws SQLException
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_USER);
        User user = new User(m_connection, isCaseSensitive(), name);
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_USER_ID, user.getLogger().getObjectId());
        return user;
    }


    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        String sql = DBTools.getDropUserQuery(m_connection, name, isCaseSensitive());
        System.out.println("sdbcx.UserContainer._removeElement() SQL: " + sql);
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
    }

    @Override
    protected void _refresh()
    {
        m_connection._refresh();
    }

    @Override
    protected XPropertySet _createDescriptor()
    {
        return new UserDescriptor(isCaseSensitive());
    }


}
