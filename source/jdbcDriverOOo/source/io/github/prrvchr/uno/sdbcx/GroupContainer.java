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
package io.github.prrvchr.uno.sdbcx;

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
import io.github.prrvchr.uno.sdb.Connection;


public class GroupContainer
    extends Container
{

    protected final Connection m_connection;
    private final ConnectionLog m_logger; 

    // The constructor method:
    public GroupContainer(Connection connection,
                          boolean sensitive,
                          List<String> names)
        throws ElementExistException
    {
        this(connection, sensitive, names, LoggerObjectType.GROUPCONTAINER);
    }

    public GroupContainer(Connection connection,
                          boolean sensitive,
                          List<String> names,
                          LoggerObjectType type)
        throws ElementExistException
    {
        super(connection, sensitive, names);
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
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        XPropertySet group = null;
        if (_createGroup(descriptor, name)) {
            group = _createElement(name);
        }
        return group;
    }

    protected boolean _createGroup(XPropertySet descriptor,
                                   String name)
        throws SQLException
    {
        String query = DBTools.getCreateGroupQuery(m_connection, descriptor, name, isCaseSensitive());
        System.out.println("sdbcx.GroupContainer._createGroup() SQL: " + query);
        return DBTools.executeDDLQuery(m_connection, query, m_logger, this.getClass().getName(),
                                       "_createGroup", Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY, name);
    }

    @Override
    protected XPropertySet _createElement(String name)
        throws SQLException
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUP);
        Group goup = new Group(m_connection, isCaseSensitive(), name);
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUP_ID, goup.getLogger().getObjectId());
        return goup;
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        String sql = DBTools.getDropGroupQuery(m_connection, name, isCaseSensitive());
        System.out.println("sdbcx.GroupContainer._removeElement() SQL: " + sql);
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
        return new GroupDescriptor(isCaseSensitive());
    }


}
