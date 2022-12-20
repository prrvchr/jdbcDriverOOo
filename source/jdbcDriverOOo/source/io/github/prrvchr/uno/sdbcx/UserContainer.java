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

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class UserContainer
    extends Container
{

    protected final Connection m_connection;

    // The constructor method:
    public UserContainer(Connection connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        super(connection, sensitive, names);
        m_connection = connection;
    }


    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        _createUser(descriptor, name);
        return _createElement(name);
    }

    protected void _createUser(XPropertySet descriptor,
                               String name)
        throws SQLException
    {
        String sql = DataBaseTools.getCreateUserQuery(m_connection, descriptor, name, isCaseSensitive());
        System.out.println("sdbcx.UserContainer._createUser() SQL: " + sql);
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
    }

    @Override
    protected XPropertySet _createElement(String name)
        throws SQLException
    {
        return new User(m_connection, isCaseSensitive(), name);
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        String sql = DataBaseTools.getDropUserQuery(m_connection, name, isCaseSensitive());
        System.out.println("sdbcx.UserContainer._removeElement() SQL: " + sql);
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            statement.execute(sql);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
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
