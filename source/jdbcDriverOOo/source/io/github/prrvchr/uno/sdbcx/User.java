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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class User
    extends Item
    implements XUser
{

    private static final String m_name = User.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.User"};
    private final XNameAccess m_Tables;

    // The constructor method:
    public User(Connection connection,
                String name)
    {
        super(m_name, m_services, connection, name);
        m_Tables = connection.getTables();
    }


    // com.sun.star.sdbcx.XUser:
    @Override
    public void changePassword(String arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.User.changePassword()");
    }


    // com.sun.star.sdbcx.XAuthorizable <- XUser:
    @Override
    public int getGrantablePrivileges(String name, int type) throws SQLException {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.User.getGrantablePrivileges() Name: " + name + " - Type: " + type);
        return 511;
    }

    @Override
    public int getPrivileges(String name, int type) throws SQLException {
        int privilege = 0;
        try {
            System.out.println("sdbcx.User.getPrivileges() Name: " + name + " - Type: " + type);
            XPropertySet table = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, m_Tables.getByName(name));
            privilege = (int) table.getPropertyValue("Privileges");
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            UnoHelper.getSQLException(e);
        }
        System.out.println("sdbcx.User.getPrivileges() Privileges: " + privilege);
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


}
