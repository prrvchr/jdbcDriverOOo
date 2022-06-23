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

import java.util.ArrayList;
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
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.sdb.Connection;


public abstract class ContainerBase<T extends Item>
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XIndexAccess,
               XAppend,
               XDrop,
               XEnumerationAccess,
               XDataDescriptorFactory
{

    private final String m_name;
    private final String[] m_services;
    protected final Connection m_Connection;
    public final List<T> m_Elements = new ArrayList<T>();
    private final Type m_type;
    private final List<XContainerListener> m_Listeners = new ArrayList<XContainerListener>();


    // The constructor method:
    public ContainerBase(String name,
                         String[] services,
                         Connection connection)
    {
        this(name, services, connection, "com.sun.star.beans.XPropertySet");
    }
    public ContainerBase(String name,
                         String[] services,
                         Connection connection,
                         String type)
    {
        super();
        m_name = name;
        m_services = services;
        m_Connection = connection;
        m_type = new Type(type);
        System.out.println("sdbcx.ContainerBase()");
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_name);
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


    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType()
    {
        return m_type;
    }

    @Override
    public boolean hasElements()
    {
        return !m_Elements.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException
    {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        return m_Elements.get(index);
    }

    @Override
    public int getCount()
    {
        return m_Elements.size();
    }


    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException, IndexOutOfBoundsException
    {
        System.out.println("sdbcx.ContainerBase.dropByIndex()");
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        dropElement(m_Elements.get(index));
    }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException
    {
        System.out.println("sdbcx.ContainerBase.dropByName() 1 ***************************");
    }

    protected void dropElement(T element)
        throws SQLException
    {
        String query = _getDropQuery(element);
        System.out.println("sdbcx.ContainerBase._dropElement() Query: " + query);
        if (query != null) {
            _executeQuery(query);
            // TODO need _refresh();
            _removeElement(element);
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


    // com.sun.star.sdbcx.XAppend
    @Override
    public abstract void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException;


    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener) {
        System.out.println("sdbcx.KeyContainer.addContainerListener() ****************************************");
        m_Listeners.add(listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener) {
       System.out.println("sdbcx.KeyContainer.removeContainerListener() ****************************************");
       if (m_Listeners.contains(listener)) {
           m_Listeners.remove(listener);
       }
    }


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration()
    {
        return new Enumeration(m_Elements.iterator());
    }


    public boolean hasNamedElement(String name)
    {
        for (T element : m_Elements) {
            if (element.m_Name == name) {
                return true;
            }
        }
        return false;
    }


    // Abstract protected methods
    abstract protected String _getDropQuery(T  element)
        throws SQLException;


    // Protected methods
    protected void _removeElement(T element)
    {
        ContainerEvent event = new ContainerEvent();
        event.Source = this;
        event.Accessor = this;
        event.Element = element;
        event.ReplacedElement = element;
        for (XContainerListener listener : m_Listeners) {
            listener.elementRemoved(event);
        }
    }

    protected void _insertElement(T element)
    {
        ContainerEvent event = new ContainerEvent();
        event.Source = this;
        event.Accessor = this;
        event.Element = element;
        event.ReplacedElement = element;
        for (XContainerListener listener : m_Listeners) {
            listener.elementInserted(event);
        }
    }

    protected void _executeQueries(String[] queries)
            throws SQLException
    {
        try {
            java.sql.Statement statement = m_Connection.getWrapper().createStatement();
            for (String query : queries) {
                System.out.println("sdbcx.ContainerBase._executeQueries(): " + query);
                statement.executeUpdate(query);
            }
            statement.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected void _executeQuery(String query)
        throws SQLException
    {
        try {
            java.sql.Statement statement = m_Connection.getWrapper().createStatement();
            statement.executeUpdate(query);
            statement.close();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // Private Class
    private class Enumeration
        extends WeakBase
        implements XEnumeration
    {
        private final java.util.Iterator<T> m_Iterator;

        public Enumeration(java.util.Iterator<T> iterator)
        {
            m_Iterator = iterator;
        }

        @Override
        public boolean hasMoreElements()
        {
            return m_Iterator.hasNext();
        }

        @Override
        public Object nextElement()
            throws NoSuchElementException,
                   WrappedTargetException
        {
            if (!m_Iterator.hasNext()) {
                throw new NoSuchElementException();
            }
            return m_Iterator.next();
        }
    }


}
