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

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.PrivilegeObject;
import com.sun.star.sdbcx.XAuthorizable;
import com.sun.star.sdbcx.XGroupsSupplier;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.PrivilegesHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.Descriptor;


public abstract class Role
    extends Descriptor
    implements XAuthorizable,
               XGroupsSupplier
{

    protected final Connection m_connection;
    protected final DriverProvider m_provider;
    protected final ConnectionLog m_logger; 
    protected Groups m_groups;
    private final boolean m_isrole;

    // The constructor method:
    public Role(String service,
                String[] services,
                Connection connection,
                boolean sensitive,
                String name,
                LoggerObjectType type,
                boolean isrole)
    {
        super(service, services, sensitive, name);
        m_connection = connection;
        m_provider = connection.getProvider();
        m_logger = new ConnectionLog(m_provider.getLogger(), type);
        m_isrole = isrole;
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
        if (m_provider.ignoreDriverPrivileges()) {
            return m_provider.getMockPrivileges();
        }

        int privileges = 0;
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            try {
                XNameAccess tables = m_connection.getTables();
                if (tables.hasByName(name)) {
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(m_provider, name, rule);
                    java.sql.DatabaseMetaData metadata = m_provider.getConnection().getMetaData();
                    if (!m_isrole && getName().equals(metadata.getUserName())) {
                        privileges = PrivilegesHelper.getTablePrivileges(m_provider, metadata, table);
                    }
                    else {
                        privileges = PrivilegesHelper.getGrantablePrivileges(m_provider, getName(), table, rule);
                    }
                }
                else {
                    privileges = m_provider.getMockPrivileges();
                }
            }
            catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
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
            try {
                XNameAccess tables = m_connection.getTables();
                if (tables.hasByName(name)) {
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(m_provider, name, rule);
                    privileges = PrivilegesHelper.getTablePrivileges(m_provider, getName(), table, rule);
                }
                else {
                    privileges = m_provider.getMockPrivileges();
                }
            }
            catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
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
            String privileges = String.join(", ", m_provider.getPrivileges(privilege));
            try {
                ComposeRule rule = ComposeRule.InDataManipulation;
                NamedComponents table = DBTools.qualifiedNameComponents(m_provider, name, rule);
                query = PrivilegesHelper.getGrantPrivilegesQuery(m_provider, table, privileges, m_isrole, getName(), rule, isCaseSensitive());
                int resource = m_isrole ?
                               Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY :
                               Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY;
                getLogger().logprb(LogLevel.INFO, resource, privileges, getName(), name, query);
                System.out.println("sdb.Role.grantPrivileges() Query: " + query);
                DBTools.executeSQLQuery(m_connection.getProvider(), query);
            }
            catch (java.sql.SQLException e) {
                int resource = m_isrole ?
                               Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY_ERROR :
                               Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, privileges, getName(), name, query);
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
            String privileges = String.join(", ", m_provider.getPrivileges(privilege));
            try {
                XNameAccess tables = m_connection.getTables();
                if (tables.hasByName(name)) {
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(m_provider, name, rule);
                    query = PrivilegesHelper.getRevokePrivilegesQuery(m_provider, table, privileges, m_isrole, getName(), rule, isCaseSensitive());
                    int resource = m_isrole ?
                                   Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY :
                                   Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY;
                    getLogger().logprb(LogLevel.INFO, resource, privileges, getName(), name, query);
                    DBTools.executeSQLQuery(m_provider, query);
                }
            }
            catch (java.sql.SQLException e) {
                int resource = m_isrole ?
                               Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY_ERROR :
                               Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, privileges, getName(), name, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        checkDisposed();
        if (m_groups == null) {
            refreshGroups();
        }
        return m_groups;
    }

    void refreshGroups()
    {
        String query = m_connection.getProvider().getRoleGroupsQuery(m_isrole);
        if (query == null) {
            return;
        }
        ArrayList<String> groups = new ArrayList<>();
        try (java.sql.PreparedStatement statement = m_connection.getProvider().getConnection().prepareStatement(query))
        {
            statement.setString(1, getName());
            try (java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next()) {
                    String group = result.getString(1);
                    if (!result.wasNull()) {
                        groups.add(group.strip());
                    }
                }
            }
            if (m_groups == null) {
                m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPROLES);
                m_groups = new Groups(m_connection, isCaseSensitive(), getName(), groups, m_isrole);
                m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPROLES_ID, m_groups.getLogger().getObjectId());
            }
            else {
                m_groups.refill(groups);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected Groups getGroupsInternal()
    {
        return m_groups;
    }

}
