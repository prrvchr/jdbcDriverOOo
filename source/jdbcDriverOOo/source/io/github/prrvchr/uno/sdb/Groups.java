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

import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.sdbcx.RoleContainer;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;

public final class Groups
    extends RoleContainer<Group>
{
    private static final String m_service = Groups.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Groups",
                                                "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public Groups(Connection connection,
                  boolean sensitive,
                  String role,
                  List<String> names,
                  boolean isrole)
        throws ElementExistException
    {
        super(m_service, m_services, connection, connection.getProvider(), role, connection.getGroupsInternal(), sensitive, names, isrole, isrole ? "" : null, LoggerObjectType.GROUPS);
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }
    protected DriverProvider getProvider()
    {
        return m_provider;
    }

    @Override
    public XPropertySet createDataDescriptor() {
        return new GroupDescriptor(isCaseSensitive());
    }

    @Override
    public void dispose()
    {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPROLES_DISPOSING);
        super.dispose();
    }

    @Override
    protected void refill(List<String> roles)
    {
        super.refill(roles);
    }

    @Override
    protected void removeElement(String name)
    {
        super.removeElement(name);
    }

}
