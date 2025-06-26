/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.PrivilegesHelper;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.Descriptor;


public abstract class Role
    extends Descriptor
    implements XAuthorizable,
               XGroupsSupplier {

    protected final Connection mConnection;
    protected final Provider mProvider;
    protected final ConnectionLog mLogger; 
    protected Groups mGroups;
    private final boolean mIsrole;

    // The constructor method:
    protected Role(String service,
                   String[] services,
                   Connection connection,
                   boolean sensitive,
                   String name,
                   LoggerObjectType type,
                   boolean isrole) {
        super(service, services, sensitive, name);
        mConnection = connection;
        mProvider = connection.getProvider();
        mLogger = new ConnectionLog(mProvider.getLogger(), type);
        mIsrole = isrole;
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    // com.sun.star.sdbcx.XAuthorizable:
    @Override
    public int getGrantablePrivileges(String name, int type)
        throws SQLException {
        int privileges = 0;
        if (mProvider.getConfigSQL().ignoreDriverPrivileges()) {
            privileges = mProvider.getConfigDCL().getMockPrivileges();
        } else if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            try {
                XNameAccess tables = mConnection.getTables();
                if (tables.hasByName(name)) {
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, rule);
                    java.sql.DatabaseMetaData metadata = mProvider.getConnection().getMetaData();
                    if (!mIsrole && getName().equals(metadata.getUserName())) {
                        privileges = PrivilegesHelper.getTablePrivileges(mProvider, metadata, table);
                    } else {
                        privileges = PrivilegesHelper.getGrantablePrivileges(mProvider, getName(), table, rule);
                    }
                } else {
                    privileges = mProvider.getConfigDCL().getMockPrivileges();
                }
            } catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
        return privileges;
    }

    @Override
    public int getPrivileges(String name, int type)
        throws SQLException {
        int privileges = 0;
        System.out.println("sdb.Role.getPrivileges() 1 table: " + name);
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            try {
                XNameAccess tables = mConnection.getTables();
                if (tables.hasByName(name)) {
                    System.out.println("sdb.Role.getPrivileges() 2");
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, rule);
                    privileges = PrivilegesHelper.getTablePrivileges(mProvider, getName(), table, rule);
                } else {
                    privileges = mProvider.getConfigDCL().getMockPrivileges();
                }
            } catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
        return privileges;
    }

    @Override
    public void grantPrivileges(String name,
                                int type,
                                int privilege)
        throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            String privileges = String.join(", ", mProvider.getConfigDCL().getPrivileges(privilege));
            try {
                ComposeRule rule = ComposeRule.InDataManipulation;
                NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, rule);
                query = PrivilegesHelper.getGrantPrivilegesCommand(mProvider, table, privileges,
                                                                 mIsrole, getName(), rule, isCaseSensitive());
                int resource;
                if (mIsrole) {
                    resource = Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY;
                } else {
                    resource = Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY;
                }
                getLogger().logprb(LogLevel.INFO, resource, privileges, getName(), name, query);
                System.out.println("sdb.Role.grantPrivileges() Query: " + query);
                DBTools.executeSQLQuery(mConnection.getProvider(), query);
            } catch (java.sql.SQLException e) {
                int resource;
                if (mIsrole) {
                    resource = Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY_ERROR;
                } else {
                    resource = Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY_ERROR;
                }
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, privileges,
                                                                                       getName(), name, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    @Override
    public void revokePrivileges(String name,
                                 int type,
                                 int privilege)
        throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            String privileges = String.join(", ", mProvider.getConfigDCL().getPrivileges(privilege));
            try {
                XNameAccess tables = mConnection.getTables();
                if (tables.hasByName(name)) {
                    ComposeRule rule = ComposeRule.InDataManipulation;
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, rule);
                    query = PrivilegesHelper.getRevokePrivilegesCommand(mProvider, table, privileges,
                                                                        mIsrole, getName(), rule, isCaseSensitive());
                    int resource;
                    if (mIsrole) {
                        resource = Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY;
                    } else {
                        resource = Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY;
                    }
                    getLogger().logprb(LogLevel.INFO, resource, privileges, getName(), name, query);
                    DBTools.executeSQLQuery(mProvider, query);
                }
            } catch (java.sql.SQLException e) {
                int resource;
                if (mIsrole) {
                    resource = Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY_ERROR;
                } else {
                    resource = Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY_ERROR;
                }
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, privileges,
                                                                                       getName(), name, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }


    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups() {
        checkDisposed();
        if (mGroups == null) {
            refreshGroups();
        }
        return mGroups;
    }

    void refreshGroups() {
        List<Object> values = new ArrayList<>();
        String query = mConnection.getProvider().getConfigDCL().getRoleGroupsQuery(getName(), mIsrole, values);
        if (query != null) {
            ArrayList<String> groups = new ArrayList<>();
            try (java.sql.PreparedStatement smt = mConnection.getProvider().getConnection().prepareStatement(query)) {
                for (int i = 0; i < values.size(); i++) {
                    smt.setObject(i + 1, values.get(i));
                }
                try (java.sql.ResultSet result = smt.executeQuery()) {
                    while (result.next()) {
                        String group = result.getString(1);
                        if (!result.wasNull()) {
                            groups.add(group.strip());
                        }
                    }
                }
                if (mGroups == null) {
                    mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPROLES);
                    mGroups = new Groups(mConnection, isCaseSensitive(), getName(), groups, mIsrole);
                    mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPROLES_ID,
                                   mGroups.getLogger().getObjectId());
                } else {
                    mGroups.refill(groups);
                }
            } catch (ElementExistException | java.sql.SQLException e) {
                throw new com.sun.star.uno.RuntimeException("Error", e);
            }
        }
    }

    protected Groups getGroupsInternal() {
        return mGroups;
    }

}
