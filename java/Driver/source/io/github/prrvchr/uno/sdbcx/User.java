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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceProperty;


public class User
    extends ServiceProperty
    implements XUser
{

    private static final String m_name = User.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.User"};
    @SuppressWarnings("unused")
    private final java.sql.Connection m_Connection;
    @SuppressWarnings("unused")
    private Map<String, String> m_users;
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new HashMap<String, Property>();
        map.put("Name", UnoHelper.getProperty("Name", "string"));
        return map;
    }

    // The constructor method:
    public User(Connection connection)
    {
        super(m_name, m_services, _getPropertySet());
        m_Connection = connection;
    }


    // com.sun.star.sdbcx.XAuthorizable <- XUser:
    @Override
    public int getGrantablePrivileges(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPrivileges(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void grantPrivileges(String arg0, int arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokePrivileges(String arg0, int arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }


    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }


}
