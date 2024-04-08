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

import java.util.Iterator;
import java.util.List;

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

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBParameterHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
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
               XRefreshable
{

    private final String m_service;
    private final String[] m_services;
    protected String m_name;
    protected String m_role;
    private Container<T> m_roles;
    private List<String> m_Names;
    private boolean m_sensitive;
    protected InterfaceContainer m_container = new InterfaceContainer();
    protected InterfaceContainer m_refresh = new InterfaceContainer();
    protected Object m_lock;
    protected final ConnectionLog m_logger; 
    protected final DriverProvider m_provider;
    protected final boolean m_isrole;

    // The constructor method:
    public RoleContainer(String service,
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
        throws ElementExistException
    {
        m_service = service;
        m_services = services;
        m_lock = connection;
        m_provider = provider;
        m_sensitive = sensitive;
        m_name = name;
        m_role = role;
        m_roles = roles;
        m_Names = names;
        m_logger = new ConnectionLog(provider.getLogger(), type);
        m_isrole = isrole;
    }

    protected abstract ConnectionLog getLogger();
    protected abstract DriverProvider getProvider();

    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose()
    {
        System.out.println("sdbcx.RoleContainer.dispose() Class: " + this.getClass().getName());
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        Iterator<?> iterator = m_refresh.iterator();
        EventObject event = new EventObject(this);
        while (iterator.hasNext()) {
            XRefreshListener listener = (XRefreshListener) iterator.next();
            listener.refreshed(event);
        }
    }

    @Override
    public void addRefreshListener(XRefreshListener listener) {
        synchronized (m_lock) {
            m_refresh.add(listener);
        }
    }

    @Override
    public void removeRefreshListener(XRefreshListener listener) {
        synchronized (m_lock) {
            m_refresh.remove(listener);
        }
    }


    // com.sun.star.container.XNameAccess:
    @Override
    public Object getByName(String name)
        throws NoSuchElementException,
               WrappedTargetException
    {
        synchronized (m_lock) {
            if (!m_Names.contains(name)) {
                throw new NoSuchElementException();
            }
        }
        return m_roles.getByName(name);
    }

    @Override
    public String[] getElementNames()
    {
        synchronized (m_lock) {
            return m_Names.toArray(new String[0]);
        }
    }

    @Override
    public boolean hasByName(String name)
    {
        synchronized (m_lock) {
            return m_Names.contains(name);
        }
    }


    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType()
    {
        return new Type(XPropertySet.class);
    }

    @Override
    public boolean hasElements()
    {
        return !m_Names.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException
    {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        String name = m_Names.get(index);
        if (!m_roles.hasByName(name)) {
            throw new IndexOutOfBoundsException();
        }
        Object element = null;
        try {
            element = m_roles.getElement(name);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return element;
    }

    @Override
    public int getCount()
    {
        return m_Names.size();
    }


    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException
    {
        synchronized (m_lock) {
            if (index < 0 || index >= getCount()) {
                throw new IndexOutOfBoundsException();
            }
        }
        String name = m_Names.get(index);
        if (revokeRole(name)) {
            removeElement(name);
        }
   }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException
    {
        synchronized (m_lock) {
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
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (hasByName(name)) {
            throw new ElementExistException();
        }
        if (grantRole(name)) {
            m_Names.add(name);
            T element = m_roles.getElement(name);
            // XXX: notify our container listeners
            ContainerEvent event = new ContainerEvent(this, name, element, null);
            Iterator<?> iterator = m_container.iterator();
            while (iterator.hasNext()) {
                XContainerListener listener = (XContainerListener) iterator.next();
                listener.elementInserted(event);
            }
        }
    }


    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener)
    {
        m_container.add(listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener)
    {
        m_container.remove(listener);
    }


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration()
    {
        return new ContainerEnumeration(this);
    }


    // Protected methods
    protected boolean grantRole(String name)
        throws SQLException
    {
        String query = null;
        String role1 = m_isrole ? m_name : name;
        String role2 = m_isrole ? name : m_name;
        try {
            Object[] Arguments = DBParameterHelper.getAlterRoleArguments(getProvider(), role1, role2, m_isrole, m_role, isCaseSensitive());
            query = getProvider().getGrantRoleQuery(Arguments);
            int resource = m_isrole ?
                           Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY :
                           Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY;
            getLogger().logprb(LogLevel.INFO, resource, role1, role2, query);
            System.out.println("sdbcx.RoleContainer.grantRole() 1 IsRole: " + m_isrole + " - Query: " + query);
            return DBTools.executeDDLQuery(getProvider(), query);
        }
        catch (java.sql.SQLException e) {
            int resource = m_isrole ?
                           Resources.STR_LOG_GROUPROLES_GRANT_ROLE_QUERY_ERROR :
                           Resources.STR_LOG_USERROLES_GRANT_ROLE_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, role1, role2, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private boolean revokeRole(String name)
        throws SQLException
    {
        String query = null;
        String role1 = m_isrole ? m_name : name;
        String role2 = m_isrole ? name : m_name;
        try {
            Object[] Arguments = DBParameterHelper.getAlterRoleArguments(getProvider(), role1, role2, m_isrole, m_role, isCaseSensitive());
            query = getProvider().getRevokeRoleQuery(Arguments);
            int resource = m_isrole ?
                           Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY :
                           Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY;
            getLogger().logprb(LogLevel.INFO, resource, role1, role2, query);
            System.out.println("sdbcx.RoleContainer.revokeRole() 1 IsRole: " + m_isrole + " - Query: " + query);
            return DBTools.executeDDLQuery(getProvider(), query);
       }
        catch (java.sql.SQLException e) {
            int resource = m_isrole ?
                           Resources.STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY_ERROR :
                           Resources.STR_LOG_USERROLES_REVOKE_ROLE_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, role1, role2, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    protected void removeElement(String name)
    {
        if (m_Names.remove(name)) {
            ContainerEvent event = new ContainerEvent(this, name, null, null);
            for (Iterator<?> iterator = m_container.iterator(); iterator.hasNext(); ) {
                XContainerListener listener = (XContainerListener) iterator.next();
                listener.elementRemoved(event);
            }
        }
    }

    protected boolean isCaseSensitive()
    {
        return m_sensitive;
    }

    protected void refill(List<String> names)
    {
        // XXX: We need to remove members of role.
        m_Names = names;
    }

}
