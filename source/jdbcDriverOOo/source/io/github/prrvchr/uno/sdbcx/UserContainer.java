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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdb.Connection;


public class UserContainer
    extends ContainerSuper<User>
{

    private static final String m_name = UserContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    public UserContainer(Connection connection)
    {
        super(m_name, m_services, connection);
        refresh();
    }

    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(User user)
    {
        return m_Connection.getProvider().getDropUserQuery(m_Connection, user.m_Name);
    }



    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.UserContainer.createDataDescriptor() ***************************");
        return null;
    }


    public void refresh()
    {
        m_Names.clear();
        m_Elements.clear();
        String query = m_Connection.getProvider().getUserQuery();
        if (query != null) {
            try {
                java.sql.Statement statement = m_Connection.getWrapper().createStatement();
                java.sql.ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    String name = result.getString(1);
                    System.out.println("sdbcx.UserContainer.refresh() 2 : " + name);
                    m_Elements.add(new User(m_Connection, name));
                    m_Names.add(name);
                }
                result.close();
                statement.close();
            }
            catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.UserContainer.appendByDescriptor() ****************************");
    }


}
