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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.PrivilegeObject;
import com.sun.star.sdbcx.XAuthorizable;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.Descriptor;


public abstract class Role
    extends Descriptor
    implements XAuthorizable,
               XGroupsSupplier
{

    protected final Connection m_connection;
    protected final ConnectionLog m_logger; 
    protected GroupContainer m_groups;

    // The constructor method:
    public Role(String service,
                String[] services,
                Connection connection,
                boolean sensitive,
                String name,
                LoggerObjectType type)
    {
        super(service, services, sensitive, name);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), type);
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    // com.sun.star.sdbcx.XAuthorizable:
    @Override
    public int getGrantablePrivileges(String name, int type)
        throws SQLException
    {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            addGrantees(grantees);
            try {
                privileges = DBTools.getTableOrViewGrantablePrivileges(m_connection.getProvider(), grantees, name);
            }
            catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
        return privileges;
    }

    @Override
    public int getPrivileges(String name, int type)
        throws SQLException
    {
        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            List<String> grantees = new ArrayList<>(List.of(getName()));
            addGrantees(grantees);
            try {
                privileges = DBTools.getTableOrViewPrivileges(m_connection.getProvider(), grantees, name);
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
        return privileges;
    }

    @Override
    public void grantPrivileges(String name,
                                int type,
                                int privilege)
        throws SQLException
    {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            try {
                query = DBTools.getGrantPrivilegesQuery(m_connection.getProvider(), getName(), name, privilege, ComposeRule.InDataManipulation, isCaseSensitive());
                getLogger().logprb(LogLevel.INFO, getGrantPrivilegesResource(false), name, query);
                DBTools.executeDDLQuery(m_connection.getProvider(), query);
            }
            catch (java.sql.SQLException e) {
                String msg = SharedResources.getInstance().getResourceWithSubstitution(getGrantPrivilegesResource(true), name, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    @Override
    public void revokePrivileges(String name,
                                 int type,
                                 int privilege)
        throws SQLException
    {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            try {
                query = DBTools.revokeTableOrViewPrivileges(m_connection.getProvider(), getName(), name, privilege, ComposeRule.InDataManipulation, isCaseSensitive());
                getLogger().logprb(LogLevel.INFO, getRevokePrivilegesResource(false), name, query);
                DBTools.executeDDLQuery(m_connection.getProvider(), query);
            }
            catch (java.sql.SQLException e) {
                String msg = SharedResources.getInstance().getResourceWithSubstitution(getRevokePrivilegesResource(true), name, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        try {
            if (m_groups == null) {
                m_groups = _refreshGroups();
            }
            return m_groups;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.User.getGroups() ERROR: " + UnoHelper.getStackTrace(e));
        }
        return null;
    }

    private Groups _refreshGroups()
        throws ElementExistException
    {
        ArrayList<String> groups = new ArrayList<>();
        String sql = m_connection.getProvider().getUserGroupsQuery();
        try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(sql)){
            statement.setString(1, getName());
            java.sql.ResultSet result = statement.executeQuery();
            while(result.next()) {
                String group = result.getString(1);
                groups.add(group);
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_connection);
        }
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPROLE);
        Groups role = new Groups(m_connection, isCaseSensitive(), groups, this);
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPROLE_ID, role.getLogger().getObjectId());
        return role;
    }

    abstract protected void addGrantees(List<String> grantees);
    abstract protected int getGrantPrivilegesResource(boolean error);
    abstract protected int getRevokePrivilegesResource(boolean error);

    protected String getName()
    {
        return super.getName();
    }

}
