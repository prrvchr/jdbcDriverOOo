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

import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.PrivilegeObject;
import com.sun.star.sdbcx.XAuthorizable;
import com.sun.star.sdbcx.XGroupsSupplier;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.PrivilegesHelper;
import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdbcx.RoleListener;
import io.github.prrvchr.uno.sdbcx.Descriptor;


public abstract class Role
    extends Descriptor
    implements XAuthorizable,
               XGroupsSupplier {

    protected final Connection mConnection;
    protected final Provider mProvider;
    protected final ConnectionLog mLogger; 
    protected Groups mGroups;
    protected GroupContainer mGroupContainer;

    private final ComposeRule mRule = ComposeRule.InDataManipulation;
    private final boolean mIsrole;
    private RoleListener<Group> mListener;

    // The constructor method:
    protected Role(String service,
                   String[] services,
                   Connection connection,
                   GroupContainer groups,
                   String name,
                   boolean sensitive,
                   LoggerObjectType type,
                   boolean isrole) {
        super(service, services, sensitive, name);
        mConnection = connection;
        mProvider = connection.getProvider();
        mGroupContainer = groups;
        mLogger = new ConnectionLog(mProvider.getLogger(), type);
        mIsrole = isrole;
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    // com.sun.star.lang.XComponent
    @Override
    public void dispose() {
        if (mGroups != null) {
            synchronized (mGroups) {
                if (mListener != null) {
                    mGroupContainer.removeContainerListener(mListener);
                }
                mGroups.dispose();
            }
        }
        super.dispose();
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
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, mRule);
                    java.sql.DatabaseMetaData metadata = mProvider.getConnection().getMetaData();
                    if (!mIsrole && getName().equals(metadata.getUserName())) {
                        privileges = PrivilegesHelper.getTablePrivileges(mProvider, metadata, table);
                    } else {
                        privileges = PrivilegesHelper.getGrantablePrivileges(mProvider, getName(), table, mRule);
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
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            try {
                XNameAccess tables = mConnection.getTables();
                if (tables.hasByName(name)) {
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, mRule);
                    privileges = PrivilegesHelper.getTablePrivileges(mProvider, getName(), table, mRule);
                } else {
                    privileges = mProvider.getConfigDCL().getMockPrivileges();
                }
            } catch (Throwable e) {
                String msg = e.getMessage().replaceAll(QueryHelper.TOKEN_NEWLINE, QueryHelper.SPACE);
                getLogger().logprb(LogLevel.SEVERE, Resources.STR_LOG_TABLE_PRIVILEGE_ACCESS_ERROR, msg);
            }
            if (privileges == 0) {
                privileges = mProvider.getConfigDCL().getMockPrivileges();
            }
        }
        return privileges;
    }

    @Override
    public void grantPrivileges(String name,
                                int type,
                                int privilege)
        throws SQLException {
        int res1, res2;
        if (mIsrole) {
            res1 = Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY;
            res2 = Resources.STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY_ERROR;
        } else {
            res1 = Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY;
            res2 = Resources.STR_LOG_USER_GRANT_PRIVILEGE_QUERY_ERROR;
        }
        grantPrivileges(name, type, privilege, res1, res2);
    }

    @Override
    public void revokePrivileges(String name,
                                 int type,
                                 int privilege)
        throws SQLException {
        int res1, res2;
        if (mIsrole) {
            res1 = Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY;
            res2 = Resources.STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY_ERROR;
        } else {
            res1 = Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY;
            res2 = Resources.STR_LOG_USER_REVOKE_PRIVILEGE_QUERY_ERROR;
        }
        revokePrivileges(name, type, privilege, res1, res2);
    }

    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups() {
        return getGroupsInternal();
    }

    protected synchronized Groups getGroupsInternal() {
        checkDisposed();
        if (mGroups == null) {
            refreshGroups();
        }
        return mGroups;
    }

    private void refreshGroups() {
        String[] groups;
        if (mGroups == null) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_GROUPROLES);
        }
        List<Object> values = new ArrayList<>();
        String query = mConnection.getProvider().getConfigDCL().getRoleGroupsQuery(getName(), mIsrole, values);
        if (query != null) {
            int resource = Resources.STR_LOG_CREATE_GROUPROLES_ERROR;
            groups = mConnection.getRoleNames(mLogger, values, query, resource);
        } else {
            groups = new String[0];
            getLogger().logprb(LogLevel.SEVERE, Resources.STR_LOG_CREATE_GROUPROLES_NOT_SUPPORTED);
        }
        if (mGroups == null) {
            mGroups = new Groups(mConnection, mGroupContainer, groups,
                                 getName(), isCaseSensitive(), mIsrole);
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_GROUPROLES_ID,
                           mGroups.getLogger().getObjectId());
            mListener = new RoleListener<Group>(mGroups);
            mGroupContainer.addContainerListener(mListener);
        } else {
            mGroups.refill(groups);
        }
    }

    private void grantPrivileges(String name,
                                 int type,
                                 int privilege,
                                 int res1,
                                 int res2)
        throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            String privileges = String.join(", ", mProvider.getConfigDCL().getPrivileges(privilege));
            try {
                NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, mRule);
                query = PrivilegesHelper.getGrantPrivilegesCommand(mProvider, table, privileges,
                                                                   mIsrole, getName(), mRule, isCaseSensitive());
                getLogger().logprb(LogLevel.INFO, res1, privileges, getName(), name, query);
                System.out.println("Role.grantPrivileges() Query: " + query);
                DBTools.executeSQLQuery(mConnection.getProvider(), query);
            } catch (java.sql.SQLException e) {
                String error = e.getMessage().replaceAll(QueryHelper.TOKEN_NEWLINE, QueryHelper.SPACE);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, error);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw new SQLException(msg);
                //throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    private void revokePrivileges(String name,
                                  int type,
                                  int privilege,
                                  int res1,
                                  int res2)
        throws SQLException {
        if (type == PrivilegeObject.TABLE || type == PrivilegeObject.VIEW) {
            String query = null;
            String privileges = String.join(", ", mProvider.getConfigDCL().getPrivileges(privilege));
            try {
                XNameAccess tables = mConnection.getTables();
                if (tables.hasByName(name)) {
                    NamedComponents table = DBTools.qualifiedNameComponents(mProvider, name, mRule);
                    query = PrivilegesHelper.getRevokePrivilegesCommand(mProvider, table, privileges,
                                                                        mIsrole, getName(), mRule, isCaseSensitive());
                    getLogger().logprb(LogLevel.INFO, res1, privileges, getName(), name, query);
                    DBTools.executeSQLQuery(mProvider, query);
                }
            } catch (java.sql.SQLException e) {
                String error = e.getMessage().replaceAll(QueryHelper.TOKEN_NEWLINE, QueryHelper.SPACE);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, error);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw new SQLException(msg);
            }
        }
    }

}
