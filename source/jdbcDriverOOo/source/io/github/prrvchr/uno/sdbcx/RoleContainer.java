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
package io.github.prrvchr.uno.sdbcx;

import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.config.ParameterDCL;
import io.github.prrvchr.uno.driver.container.BiMap;
import io.github.prrvchr.uno.driver.container.BiMapSuper;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdb.Role;


public abstract class RoleContainer<T extends Role>
    extends ContainerSuper<T> {

    // Name of The Role holding this role container
    protected String mName;
    // A literal (USER, ROLE, null or an empty string) necessary for Firebird SQL queries
    protected String mRole;
    protected ConnectionSuper mConnection;
    protected final ConnectionLog mLogger; 
    protected final Provider mProvider;
    // The type of The Role (Group or User)
    protected final boolean mIsrole;
    private Role mOwner;
    // The constructor method:
    protected RoleContainer(String service,
                            String[] services,
                            ConnectionSuper connection,
                            Role owner,
                            BiMap<T> bimap,
                            String[] names,
                            String name,
                            boolean sensitive,
                            boolean isrole,
                            String rolename,
                            LoggerObjectType type) {
        super(service, services, owner,
              new BiMapSuper<T>(DBTools.getComparator(sensitive), bimap, names),
              sensitive);
                
        mConnection = connection;
        mProvider = connection.getProvider();
        mSensitive = sensitive;
        mName = name;
        mRole = rolename;
        mOwner = owner;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), type);
        mIsrole = isrole;
    }

    protected abstract ConnectionLog getLogger();
    protected abstract Provider getProvider();

    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose() {
        System.out.println("RoleContainer.dispose() Class: " + this.getClass().getName());
    }

    protected abstract T createRoleElement(String name) throws SQLException;

    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (mOwner) {
            System.out.println("sdbcx.Container.dropByIndex() 1 index: " + index);
            try {
                String name = mBimap.getName(index);
                if (revokeRole(name)) {
                    removeContainerElement(index, false);
                }
                System.out.println("sdbcx.Container.dropByIndex() 2 name: " + name);
            } catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e, this);
            }
        }
    }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException {
        if (!hasByName(name)) {
            System.out.println("sdbcx.Container.dropByName() ERROR: " + name);
            throw new NoSuchElementException();
        }
        synchronized (mOwner) {
            System.out.println("sdbcx.Container.dropByName() 1 name: " + name);
            try {
                if (revokeRole(name)) {
                    int index = mBimap.getIndex(name);
                    removeContainerElement(index, false);
                }
                System.out.println("sdbcx.Container.dropByName() 2");
            } catch (java.sql.SQLException e) {
                throw DBTools.getSQLException(e, this);
            }
        }
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException, ElementExistException {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyID.NAME);
        if (hasByName(name)) {
            throw new ElementExistException();
        }
        try {
            if (grantRole(name)) {
                T element = mBimap.addElement(name, null);
                // XXX: notify our container listeners
                broadcastElementInserted(element, name);
            }
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    // Protected methods
    protected boolean grantRole(String name)
        throws java.sql.SQLException {
        boolean granted = false;
        if (mIsrole) {
            int res1 = Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY;
            int res2 = Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY_ERROR;
            granted = grantRole(mName, name, res1, res2);
        } else {
            int res1 = Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY;
            int res2 = Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY_ERROR;
            granted = grantRole(name, mName, res1, res2);
        }
        return granted;
    }

    private boolean grantRole(String role1, String role2, int res1, int res2)
        throws java.sql.SQLException {
        String query = null;
        System.out.println("RoleContainer.grantRole() 1 Role1: " + role1 + " - Role2: " + role2);
        try {
            Map<String, Object> Arguments = ParameterDCL.getAlterRoleArguments(getProvider().getNamedSupport(),
                                                                               role1, role2,
                                                                               mIsrole, mRole,
                                                                               isCaseSensitive());
            query = getProvider().getConfigDCL().getGrantRoleCommand(Arguments);
            System.out.println("RoleContainer.grantRole() 2 query: " + query);
            getLogger().logprb(LogLevel.INFO, res1, role1, role2, query);
            return DBTools.executeSQLQuery(getProvider(), query);
        } catch (java.sql.SQLException e) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, role1, role2, query);
            getLogger().logp(LogLevel.SEVERE, msg, e);
            throw new java.sql.SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private boolean revokeRole(String name)
        throws java.sql.SQLException {
        boolean revoked = false;
        if (mIsrole) {
            int res1 = Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY;
            int res2 = Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY_ERROR;
            revoked = revokeRole(mName, name, res1, res2);
        } else {
            int res1 = Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY;
            int res2 = Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY_ERROR;
            revoked = revokeRole(name, mName, res1, res2);
        }
        return revoked;
    }

    private boolean revokeRole(String role1, String role2, int res1, int res2)
        throws java.sql.SQLException {
        String query = null;
        System.out.println("RoleContainer.revokeRole() 1 Role1: " + role1 + " - Role2: " + role2);
        try {
            Map<String, Object> Arguments = ParameterDCL.getAlterRoleArguments(getProvider().getNamedSupport(),
                                                                               role1, role2,
                                                                               mIsrole, mRole,
                                                                               isCaseSensitive());
            query = getProvider().getConfigDCL().getRevokeRoleCommand(Arguments);
            System.out.println("RoleContainer.revokeRole() 2 query: " + query);
            getLogger().logprb(LogLevel.INFO, res1, role1, role2, query);
            return DBTools.executeSQLQuery(getProvider(), query);
        } catch (java.sql.SQLException e) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(res2, role1, role2, query);
            getLogger().logp(LogLevel.SEVERE, msg, e);
            throw new java.sql.SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    protected void removeContainerElement(String name, boolean dispose) {
        super.removeContainerElement(name, dispose);
    }

    protected void removeElement(String name) {
        int index = mBimap.getIndex(name);
        removeContainerElement(index, false);
    }

    protected boolean isCaseSensitive() {
        return mSensitive;
    }

    @Override
    protected XPropertySet createDescriptor() {
        return null;
    }

    @Override
    protected T createElement(String name) throws java.sql.SQLException {
        return null;
    }

    @Override
    protected void refreshInternal() {
        
    }

    @Override
    protected T appendElement(XPropertySet descriptor) throws java.sql.SQLException {
        return null;
    }

    @Override
    protected void removeDataBaseElement(int index, String name) throws java.sql.SQLException {
        
    }

}
