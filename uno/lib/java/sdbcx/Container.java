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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.sun.star.beans.UnknownPropertyException;
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
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.util.XRefreshable;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.sdbc.StandardSQLState;


public abstract class Container
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XNameAccess,
               XIndexAccess,
               XAppend,
               XDrop,
               XEnumerationAccess,
               XDataDescriptorFactory,
               XRefreshable,
               XColumnLocate
{

    private static final String m_service = Container.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    private TreeMap<String, XPropertySet> m_Elements;
    private List<String> m_Names;
    private boolean m_sensitive;
    private InterfaceContainer m_container = new InterfaceContainer();
    private InterfaceContainer m_refresh = new InterfaceContainer();
    protected Object m_lock;
    private Comparator<String> caseSensitiveComparator = new Comparator<String>() {
        @Override
        public int compare(String x, String y) {
            if (m_sensitive) {
                return x.compareTo(y);
            }
            else {
                return x.compareToIgnoreCase(y);
            }
        }
    };

    // The constructor method:
    public Container(Object lock,
                     boolean sensitive) {
        m_lock = lock;
        m_sensitive = sensitive;
        m_Elements = new TreeMap<>(caseSensitiveComparator);
        m_Names = new ArrayList<String>();
    }
    public Container(Object lock,
                     boolean sensitive,
                     List<String> names)
        throws ElementExistException
    {
        this(lock, sensitive);
        for (String name : names) {
            if (m_Elements.containsKey(name)) {
                throw new ElementExistException(name, this);
            }
            m_Elements.put(name, null);
            m_Names.add(name);
        }
    }


    public void refill(List<String> names)
    {
        // We only add new elements, as per the C++ implementation.
        for (String name : names) {
            if (!m_Elements.containsKey(name)) {
                m_Elements.put(name, null);
                m_Names.add(name);
            }
        }
    }


    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose() {
        System.out.println("sdbcx.Container.dispose() 1 Class: " + this.getClass().getName());
        EventObject event = new EventObject(this);
        m_container.disposeAndClear(event);
        m_refresh.disposeAndClear(event);
        synchronized (m_lock) {
            for (XPropertySet element : m_Elements.values()) {
                UnoHelper.disposeComponent(element);
            }
            m_Elements.clear();
            m_Names.clear();
        }
        System.out.println("sdbcx.Container.dispose() 2 Class: " + this.getClass().getName());
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
        System.out.println("sdbcx.Container.refresh() 1 Class: " + this.getClass().getName());
        Iterator<?> iterator;
        synchronized (m_lock) {
            for (XPropertySet element : m_Elements.values()) {
                UnoHelper.disposeComponent(element);
            }
            m_Elements.clear();
            m_Names.clear();
            _refresh();
            iterator = m_refresh.iterator();
        }
        if (iterator == null) {
            // early disposal
            return;
        }
        EventObject event = new EventObject(this);
        while (iterator.hasNext()) {
            XRefreshListener listener = (XRefreshListener) iterator.next();
            listener.refreshed(event);
        }
        System.out.println("sdbcx.Container.refresh() 2 Class: " + this.getClass().getName());
    }

    @Override
    public void addRefreshListener(XRefreshListener listener) {
        System.out.println("sdbcx.Container.addRefreshListener() Class: " + this.getClass().getName());
        synchronized (m_lock) {
            m_refresh.add(listener);
        }
    }

    @Override
    public void removeRefreshListener(XRefreshListener listener) {
        System.out.println("sdbcx.Container.removeRefreshListener() Class: " + this.getClass().getName());
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
            if (!hasByName(name)) {
                System.out.println("sdbcx.Container.getByName() ERROR: " + name);
                throw new NoSuchElementException();
            }
        }
        return _getElement(m_Names.indexOf(name));
    }

    @Override
    public String[] getElementNames()
    {
        synchronized (m_lock) {
            return m_Names.toArray(new String[m_Names.size()]);
        }
    }

    @Override
    public boolean hasByName(String name)
    {
        synchronized (m_lock) {
            boolean value = m_Elements.containsKey(name);
            if (!value) {
                System.out.println("sdbcx.Container.hasByName() ERROR **********************************\nClass: " + this.getClass().getName()  + " - Name: " + name);
            }
            return value;
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
        return _getElement(index);
    }

    @Override
    public int getCount()
    {
        return m_Elements.size();
    }


    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException
    {
        System.out.println("sdbcx.Container.dropByIndex()");
        synchronized (m_lock) {
            if (index < 0 || index >= getCount()) {
                throw new IndexOutOfBoundsException();
            }
        }
        _removeElement(index);
    }

    @Override
    public void dropByName(String name)
        throws SQLException,
               NoSuchElementException
    {
        System.out.println("sdbcx.Container.dropByName() 1 Class: " + this.getClass().getName() + " - Name: " + name);
        synchronized (m_lock) {
            if (!hasByName(name)) {
                System.out.println("sdbcx.Container.dropByName() ERROR: " + name);
                throw new NoSuchElementException();
            }
        }
        _removeElement(m_Names.indexOf(name));
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        synchronized (m_lock) {
            return _createDescriptor();
        }
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.Container.appendByDescriptor() 1");
        ContainerEvent event;
        Iterator<?> iterator;
        synchronized (m_lock) {
            String name = _getElementName(descriptor);
            if (m_Elements.containsKey(name)) {
                 throw new ElementExistException();
            }
            System.out.println("sdbcx.Container.appendByDescriptor() 2: Class: " + this.getClass().getName() + " - Name: " + name);
            XPropertySet element = _appendElement(descriptor, name);
            if (element == null) {
                throw new RuntimeException();
            }
            name = _getElementName(element);
            XPropertySet value = m_Elements.get(name);
            if (value == null) {
                System.out.println("sdbcx.Container.appendByDescriptor() ***************************" + name);
                // XXX: This can happen when the derived class does not include it itself
                m_Elements.put(name, element);
                m_Names.add(name);
            }
            // notify our container listeners
            event = new ContainerEvent(this, name, element, null);
            iterator = m_container.iterator();
        }
        while (iterator.hasNext()) {
            XContainerListener listener = (XContainerListener) iterator.next();
            listener.elementInserted(event);
        }

    }

    protected String _getElementName(XPropertySet object)
        throws SQLException
    {
        try {
            return AnyConverter.toString(object.getPropertyValue(PropertyIds.NAME.name));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener) {
        System.out.println("sdbcx.Container.addContainerListener() Class: " + this.getClass().getName() + " - Count: " + getCount());
        m_container.add(listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener) {
       System.out.println("sdbcx.Container.removeContainerListener() Class: " + this.getClass().getName());
        m_container.remove(listener);
    }


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration()
    {
        return new ContainerEnumeration(this);
    }


    // com.sun.star.sdbcx.XColumnLocate
    @Override
    public int findColumn(String name)
        throws SQLException
    {
        System.out.println("sdbcx.ColumnContainer.findColumn() *******************************");
        if (!m_Elements.containsKey(name)) {
            String error = String.format("Error Column: %s not fount", name);
            throw new SQLException(error, this, StandardSQLState.SQL_COLUMN_NOT_FOUND.text(), 0, null);
        }
        return m_Names.indexOf(name) + 1;
    }


    // Abstract protected methods
    protected abstract XPropertySet _appendElement(XPropertySet descriptor, String name)
        throws SQLException,
               ElementExistException;
    protected abstract XPropertySet _createElement(String name) throws SQLException;
    protected abstract void _removeElement(int index, String name) throws SQLException;
    protected abstract void _refresh();


    // Protected methods
    protected Object _getElement(int index)
        throws WrappedTargetException
    {
        String name = m_Names.get(index);
        XPropertySet element = m_Elements.get(name);
        if (element == null) {
            try {
                element = _createElement(name);
            }
            catch (SQLException e) {
                try {
                    _removeElement(index, false);
                }
                catch (Exception ignored) {
                }
                throw new WrappedTargetException(e.getMessage(), this, e);
            }
            m_Elements.put(name, element);
        }
        return element;
    }

    private void _removeElement(int index)
        throws SQLException
    {
        _removeElement(index, true);
    }

    private void _removeElement(int index,
                                boolean really)
        throws SQLException
    {
        String name = m_Names.get(index);
        if (really) {
            _removeElement(index, name);
        }
        m_Names.remove(index);
        XPropertySet element = m_Elements.remove(name);
        UnoHelper.disposeComponent(element);
        ContainerEvent event = new ContainerEvent(this, name, null, null);
        for (Iterator<?> iterator = m_container.iterator(); iterator.hasNext(); ) {
            XContainerListener listener = (XContainerListener) iterator.next();
            listener.elementRemoved(event);
        }
    }

    public boolean isCaseSensitive() {
        return m_sensitive;
    }

    protected XPropertySet _cloneDescriptor(XPropertySet descriptor) {
        XPropertySet element = _createDescriptor();
        UnoHelper.copyProperties(descriptor, element);
        return element;
    }

    public void insertElement(String name,
                              XPropertySet element)
    {
        synchronized (m_lock) {
            if (!m_Elements.containsKey(name)) {
                m_Elements.put(name, element);
                m_Names.add(name);
            }
        }
    }

    protected abstract XPropertySet _createDescriptor();


}
