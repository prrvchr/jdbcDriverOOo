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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.util.XRefreshable;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class Container<T extends Descriptor>
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
               XColumnLocate {

    protected InterfaceContainer mContainer = new InterfaceContainer();
    protected InterfaceContainer mRefresh = new InterfaceContainer();
    protected Object mLock;
    private final String mService;
    private final String[] mServices;
    private TreeMap<String, T> mElements;
    private List<String> mNames;
    private boolean mSensitive;
    private Comparator<String> mCaseSensitiveComparator = new Comparator<String>() {
        @Override
        public int compare(String x, String y) {
            int comp;
            if (mSensitive) {
                comp = x.compareTo(y);
            } else {
                comp = x.compareToIgnoreCase(y);
            }
            return comp;
        }
    };

    // The constructor method:
    public Container(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        mService = service;
        mServices = services;
        mLock = lock;
        mSensitive = sensitive;
        mElements = new TreeMap<>(mCaseSensitiveComparator);
        mNames = new ArrayList<>();
    }
    public Container(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     List<String> names)
        throws ElementExistException {
        this(service, services, lock, sensitive);
        for (String name : names) {
            if (mElements.containsKey(name)) {
                throw new ElementExistException(name, this);
            }
            mElements.put(name, null);
            mNames.add(name);
        }
    }


    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose() {
        EventObject event = new EventObject(this);
        mContainer.disposeAndClear(event);
        mRefresh.disposeAndClear(event);
        synchronized (mLock) {
            for (T element : mElements.values()) {
                UnoHelper.disposeComponent(element);
            }
            mElements.clear();
            mNames.clear();
        }
        System.out.println("sdbcx.Container.dispose() Class: " + this.getClass().getName());
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
        Iterator<?> iterator;
        synchronized (mLock) {
            for (T element : mElements.values()) {
                UnoHelper.disposeComponent(element);
            }
            mElements.clear();
            mNames.clear();
            refreshInternal();
            iterator = mRefresh.iterator();
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
            if (!hasByName(name)) {
                throw new NoSuchElementException();
            }
        }
        return getElementByIndex(mNames.indexOf(name));
    }

    @Override
    public String[] getElementNames() {
        synchronized (mLock) {
            return mNames.toArray(new String[mNames.size()]);
        }
    }

    @Override
    public boolean hasByName(String name) {
        synchronized (mLock) {
            return mElements.containsKey(name);
        }
    }


    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType() {
        return new Type(XPropertySet.class);
    }

    @Override
    public boolean hasElements() {
        return !mElements.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        return getElementByIndex(index);
    }

    @Override
    public int getCount() {
        return mElements.size();
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
        removeElement(index);
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
        removeElement(mNames.indexOf(name));
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        synchronized (mLock) {
            return createDescriptor();
        }
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException, ElementExistException {
        ContainerEvent event;
        Iterator<?> iterator;
        synchronized (mLock) {
            T element = appendElement(descriptor);
            if (element == null) {
                String name = getElementName(descriptor);
                String error = String.format("Table: %s can't be created!!!", name);
                throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
            // XXX: appendElement() can change the name!!!
            String name = getElementName(descriptor);
            mElements.put(name, element);
            mNames.add(name);

            // XXX: notify our container listeners
            event = new ContainerEvent(this, name, element, null);
            iterator = mContainer.iterator();
        }
        while (iterator.hasNext()) {
            XContainerListener listener = (XContainerListener) iterator.next();
            listener.elementInserted(event);
        }
    }

    // XXX: For all container but TableContainerMain has its own method
    protected String getElementName(XPropertySet descriptor)
        throws SQLException {
        return DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
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


    // com.sun.star.sdbcx.XColumnLocate
    @Override
    public int findColumn(String name)
        throws SQLException {
        if (!mElements.containsKey(name)) {
            String error = String.format("Error Column: %s not fount", name);
            throw new SQLException(error, this, StandardSQLState.SQL_COLUMN_NOT_FOUND.text(), 0, null);
        }
        return mNames.indexOf(name) + 1;
    }


    // Abstract protected methods
    protected abstract T appendElement(XPropertySet descriptor) throws SQLException;
    protected abstract T createElement(String name) throws SQLException;
    protected abstract void removeDataBaseElement(int index, String name) throws SQLException;
    protected abstract void refreshInternal();

    // Protected methods
    protected boolean isCaseSensitive() {
        return mSensitive;
    }

    protected void refill(List<String> names) {
        // XXX: We only add new elements, as per the C++ implementation.
        for (String name : names) {
            if (!mElements.containsKey(name)) {
                mElements.put(name, null);
                mNames.add(name);
            }
        }
    }

    protected void replaceElement(String oldname, String newname)
        throws SQLException {
        // XXX: We can set the name only for simple name (ie: column, index...)
        replaceElement(oldname, newname, true);
    }

    protected void replaceElement(String oldname, String newname, boolean rename)
        throws SQLException {
        synchronized (mLock) {
            if (!newname.equals(oldname) && mNames.contains(oldname)) {
                T element = mElements.remove(oldname);
                // XXX: We cannot set the name of composed names (ie: table and view)
                if (element != null && rename) {
                    element.setName(newname);
                }
                mElements.put(newname, element);
                mNames.set(mNames.indexOf(oldname), newname);
                ContainerEvent event = new ContainerEvent(this, newname, element, oldname);
                for (Iterator<?> iterator = mContainer.iterator(); iterator.hasNext();) {
                    XContainerListener listener = (XContainerListener) iterator.next();
                    listener.elementReplaced(event);
                }
                EventObject event2 = new EventObject(this);
                for (Iterator<?> iterator2 = mRefresh.iterator(); iterator2.hasNext();) {
                    XRefreshListener listener = (XRefreshListener) iterator2.next();
                    listener.refreshed(event2);
                }
            }
        }
    }

    public Iterator<String> getActiveNames() {
        return getActiveNames(mNames);
    }

    public Iterator<String> getActiveNames(Collection<String> filter) {
        class Elements implements Iterator<String> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mNames.size()) {
                    String name = mNames.get(mIndex);
                    T element = mElements.get(name);
                    if (element != null && filter.contains(name)) {
                        next = true;
                        break;
                    }
                    mIndex++;
                }
                return next;
            }

            @Override
            public String next() {
                return mNames.get(mIndex++);
            }
        }
        return new Elements();
    }


    public Iterator<T> getActiveElements() {
        return getActiveElements(mNames);
    }

    public Iterator<T> getActiveElements(Collection<String> filter) {
        class Elements implements Iterator<T> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mNames.size()) {
                    String name = mNames.get(mIndex);
                    T element = mElements.get(name);
                    if (element != null && filter.contains(name)) {
                        next = true;
                        break;
                    }
                    mIndex++;
                }
                return next;
            }

            @Override
            public T next() {
                String name = mNames.get(mIndex++);
                return mElements.get(name);
            }
        }
        return new Elements();
    }

    protected T getElement(int index)
        throws SQLException {
        synchronized (mLock) {
            try {
                return getElementByIndex(index);
            } catch (WrappedTargetException e) {
                throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    protected T getElement(String name)
        throws SQLException {
        T element = null;
        synchronized (mLock) {
            if (mNames.contains(name)) {
                try {
                    element = getElementByIndex(mNames.indexOf(name));
                } catch (WrappedTargetException e) {
                    throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
                }
            }
        }
        return element;
    }

    protected void removeElement(String name,
                                 boolean really)
        throws SQLException {
        removeElement(mNames.indexOf(name), really);
    }

    private T getElementByIndex(int index)
        throws WrappedTargetException {
        String name = mNames.get(index);
        T element = mElements.get(name);
        if (element == null) {
            try {
                element = createElement(name);
            } catch (SQLException e) {
                try {
                    removeElement(index, false);
                } catch (Exception ignored) { }
                throw new WrappedTargetException(e.getMessage(), this, e);
            }
            mElements.put(name, element);
        }
        return element;
    }

    private void removeElement(int index)
        throws SQLException {
        removeElement(index, true);
    }

    protected void removeElement(int index,
                                 boolean really)
        throws SQLException {
        String name = mNames.get(index);
        if (really) {
            removeDataBaseElement(index, name);
        }
        mNames.remove(index);
        T element = mElements.remove(name);
        UnoHelper.disposeComponent(element);
        ContainerEvent event = new ContainerEvent(this, name, null, null);
        for (Iterator<?> iterator = mContainer.iterator(); iterator.hasNext(); ) {
            XContainerListener listener = (XContainerListener) iterator.next();
            listener.elementRemoved(event);
        }
    }

    protected XPropertySet cloneDescriptor(XPropertySet descriptor) {
        XPropertySet element = createDescriptor();
        UnoHelper.copyProperties(descriptor, element);
        return element;
    }

    protected abstract XPropertySet createDescriptor();


    protected void insertElement(String name,
                                 T element) {
        synchronized (mLock) {
            if (!mElements.containsKey(name)) {
                mElements.put(name, element);
                mNames.add(name);
            }
        }
    }

}
