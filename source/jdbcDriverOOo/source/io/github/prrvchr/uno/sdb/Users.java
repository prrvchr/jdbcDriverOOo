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
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;


public class Users
    extends UserContainer
{

    private final Group m_Group;

    // The constructor method:
    public Users(Connection connection,
                    boolean sensitive,
                    List<String> names,
                    Group group)
        throws ElementExistException
    {
        super(connection, sensitive, names, LoggerObjectType.USERS);
        m_Group = group;
    }

    @Override
    protected boolean _createUser(XPropertySet descriptor,
                                  String name)
        throws SQLException
    {
        String query = DBTools.getGrantRoleQuery(m_connection, m_Group.getName(), name, isCaseSensitive());
        System.out.println("sdbcx.GroupUserContainer._createUser() SQL: " + query);
        return DBTools.executeDDLQuery(m_connection, query, m_Group.getLogger(), this.getClass().getName(),
                                       "_createUser", Resources.STR_LOG_USERROLE_CREATE_USER_QUERY, name);
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        String query = DBTools.getRevokeRoleQuery(m_connection, m_Group.getName(), name, isCaseSensitive());
        System.out.println("sdbcx.GroupUserContainer._removeElement() SQL: " + query);
        DBTools.executeDDLQuery(m_connection, query, m_Group.getLogger(), this.getClass().getName(),
                                "_removeElement", Resources.STR_LOG_USERROLE_REMOVE_USER_QUERY, name);
    }


}
