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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XContainer;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.InterfaceContainer;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.Type;
import com.sun.star.util.XRefreshable;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.driver.query.DCLParameter;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdb.Role;


public abstract class RoleContainer<T extends Role>
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XNameAccess,
               XIndexAccess,
               XAppend,
               XDrop,
               XEnumerationAccess,
               XDataDescriptorFactory,
               XRefreshable {

    // Name of The Role holding this role container
    protected String mName;
    // A literal (USER, ROLE, null or an empty string) necessary for Firebird SQL queries
    protected String mRole;
    // XXX: Is the XRefreshable interface really necessary, are there listener? I doubt it. The future will tell
    // XXX: In fact here we only manage a list of names
    protected InterfaceContainer mContainer = new InterfaceContainer();
    protected InterfaceContainer mRefresh = new InterfaceContainer();
    protected Object mLock;
    protected final ConnectionLog mLogger; 
    protected final DriverProvider mProvider;
    // The type of The Role (Group or User)
    protected final boolean mIsrole;
    private final String mService;
    private final String[] mServices;
    // The list of roles held by The Role
    private List<String> mNames;
    private boolean mSensitive;
    // A pointer to all roles (User xor Group) in the underlying database
    private Container<T> mRoles;

    // The constructor method:
    protected RoleContainer(String service,
                            String[] services,
                            Connection connection,
                            DriverProvider provider,
                            String name,
                            Container<T> roles,
                            boolean sensitive,
                            List<String> names,
                            boolean isrole,
                            String role,
                            LoggerObjectType type)
        throws ElementExistException {
        mService = service;
        mServices = services;
        mLock = connection;
        mProvider = provider;
        mSensitive = sensitive;
        mName = name;
        mRole = role;
        mRoles = roles;
        mNames = names;
        mLogger = new ConnectionLog(provider.getLogger(), type);
        mIsrole = isrole;
    }

    protected abstract ConnectionLog getLogger();
    protected abstract DriverProvider getProvider();

    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose() {
        System.out.println("sdbcx.RoleContainer.dispose() Class: " + this.getClass().getName());
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mService);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServices);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServices, service);
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        Iterator<?> iterator = mRefresh.iterator();
        EventObject event = new EventObject(this);
        while (iterator.hasNext()) {
            XRefreshListener listener = (XRefreshListener) iterator.next();
            listener.refreshed(event);
        }
    }

    @Override
    public void addRefreshListener(XRefreshListener listener) {
        synchronized (mLock) {
            mRefresh.add(listener);
        }
    }

    @Override
    public void removeRefreshListener(XRefreshListener listener) {
        synchronized (mLock) {
            mRefresh.remove(listener);
        }
    }


    // com.sun.star.container.XNameAccess:
    @Override
    public Object getByName(String name)
        throws NoSuchElementException,
               WrappedTargetException {
        synchronized (mLock) {
            if (!mNames.contains(name)) {
                throw new NoSuchElementException();
            }
        }
        return mRoles.getByName(name);
    }

    @Override
    public String[] getElementNames() {
        synchronized (mLock) {
            return mNames.toArray(new String[0]);
        }
    }

    @Override
    public boolean hasByName(String name) {
        synchronized (mLock) {
            return mNames.contains(name);
        }
    }


    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType() {
        return new Type(XPropertySet.class);
    }

    @Override
    public boolean hasElements() {
        return !mNames.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        String name = mNames.get(index);
        if (!mRoles.hasByName(name)) {
            throw new IndexOutOfBoundsException();
        }
        Object element = null;
        try {
            element = mRoles.getElement(name);
        } catch (SQLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        return element;
    }

    @Override
    public int getCount() {
        return mNames.size();
    }


    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException {
        synchronized (mLock) {
            if (index < 0 || index >= getCount()) {
                throw new IndexOutOfBoundsException();
            }
        }
        String name = mNames.get(index);
        if (revokeRole(name)) {
            removeElement(name);
        }
    }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException {
        synchronized (mLock) {
            if (!hasByName(name)) {
                System.out.println("sdbcx.Container.dropByName() ERROR: " + name);
                throw new NoSuchElementException();
            }
        }
        if (revokeRole(name)) {
            removeElement(name);
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException, ElementExistException {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (hasByName(name)) {
            throw new ElementExistException();
        }
        if (grantRole(name)) {
            mNames.add(name);
            T element = mRoles.getElement(name);
            // XXX: notify our container listeners
            ContainerEvent event = new ContainerEvent(this, name, element, null);
            Iterator<?> iterator = mContainer.iterator();
            while (iterator.hasNext()) {
                XContainerListener listener = (XContainerListener) iterator.next();
                listener.elementInserted(event);
            }
        }
    }


    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener) {
        mContainer.add(listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener) {
        mContainer.remove(listener);
    }


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration() {
        return new ContainerEnumeration(this);
    }


    // Protected methods
    protected boolean grantRole(String name)
        throws SQLException {
        String query = null;
        String role1;
        String role2;
        if (mIsrole) {
            role1 = mName;
            role2 = name;
        } else {
            role1 = name;
            role2 = mName;
        }
        try {
            Map<String, Object> Arguments = DCLParameter.getAlterRoleArguments(getProvider(), role1, role2,
                                                                                  mIsrole, mRole, isCaseSensitive());
            query = getProvider().getDCLQuery().getGrantRoleCommand(Arguments);
            int resource;
            if (mIsrole) {
                resource = Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY;
            } else {
                resource = Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY;
            }
            getLogger().logprb(LogLevel.INFO, resource, role1, role2, query);
            System.out.println("sdbcx.RoleContainer.grantRole() 1 IsRole: " + mIsrole + " - Query: " + query);
            return DBTools.executeSQLQuery(getProvider(), query);
        } catch (java.sql.SQLException e) {
            int resource;
            if (mIsrole) {
                resource = Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY_ERROR;
            }
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, role1, role2, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private boolean revokeRole(String name)
        throws SQLException {
        String query = null;
        String role1;
        String role2;
        if (mIsrole) {
            role1 = mName;
            role2 = name;
        } else {
            role1 = name;
            role2 = mName;
        }
        try {
            Map<String, Object> Arguments = DCLParameter.getAlterRoleArguments(getProvider(), role1, role2,
                                                                                  mIsrole, mRole, isCaseSensitive());
            query = getProvider().getDCLQuery().getRevokeRoleCommand(Arguments);
            int resource;
            if (mIsrole) {
                resource = Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY;
            } else {
                resource = Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY;
            }
            getLogger().logprb(LogLevel.INFO, resource, role1, role2, query);
            System.out.println("sdbcx.RoleContainer.revokeRole() 1 IsRole: " + mIsrole + " - Query: " + query);
            return DBTools.executeSQLQuery(getProvider(), query);
        } catch (java.sql.SQLException e) {
            int resource;
            if (mIsrole) {
                resource = Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY_ERROR;
            }
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, role1, role2, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    protected void removeElement(String name) {
        if (mNames.remove(name)) {
            ContainerEvent event = new ContainerEvent(this, name, null, null);
            for (Iterator<?> iterator = mContainer.iterator(); iterator.hasNext(); ) {
                XContainerListener listener = (XContainerListener) iterator.next();
                listener.elementRemoved(event);
            }
        }
    }

    protected boolean isCaseSensitive() {
        return mSensitive;
    }

    protected void refill(List<String> names) {
        // XXX: We need to remove members of role.
        mNames = names;
    }

}
