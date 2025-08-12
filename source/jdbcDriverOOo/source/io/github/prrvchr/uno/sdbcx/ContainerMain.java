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
import java.util.Iterator;
import java.util.List;

import javax.swing.event.EventListenerList;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XContainer;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ContainerMain<T extends Descriptor>
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XNameAccess,
               XIndexAccess,
               XEnumerationAccess {

    protected Object mLock;
    protected List<T> mElements;
    protected boolean mSensitive;
    private List<String> mNames;
    private final String mService;
    private final String[] mServices;
    private final EventListenerList mContainer = new EventListenerList();

    // The constructor method:
    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive) {
        this(service, services, lock, sensitive, true);
    }

    public ContainerMain(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     String[] names) {
        this(service, services, lock, sensitive, true);
        for (String name : names) {
            mNames.add(name);
            mElements.add(null);
        }
    }

    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive,
                         boolean master) {
        mService = service;
        mServices = services;
        mLock = lock;
        mSensitive = sensitive;
        mElements = new ArrayList<>();
        if (master) {
            mNames = new ArrayList<>();
        }
    }

    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive,
                         String[] names,
                         boolean master) {
        this(service, services, lock, sensitive, master);
        for (int i = 0; i < names.length; i++) {
            mElements.add(null);
        }
    }


    protected XContainerListener[] getContainerListeners() {
        return mContainer.getListeners(XContainerListener.class);
    }

    // Would be from com.sun.star.lang.XComponent ;)
    public void dispose() {
        //EventObject event = new EventObject(this);
        //mContainer.disposeAndClear(event);
        synchronized (mLock) {
            getNamesInternal().clear();
            for (T element : mElements) {
                UnoHelper.disposeComponent(element);
            }
            mElements.clear();
        }
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
        int idx = getIndexInternal(name);
        return getElementByIndex(idx);
    }

    @Override
    public String[] getElementNames() {
        synchronized (mLock) {
            return getNamesInternal().toArray(new String[0]);
        }
    }

    @Override
    public boolean hasByName(String name) {
        synchronized (mLock) {
            return getNamesInternal().contains(name);
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
        return getElementByIndex(getIndexInternal(index));
    }

    @Override
    public int getCount() {
        return mElements.size();
    }

    // XXX: For all container but TableContainerMain has its own method
    protected String getElementName(XPropertySet descriptor)
        throws SQLException {
        return DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
    }


    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener) {
        mContainer.add(XContainerListener.class, listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener) {
        mContainer.remove(XContainerListener.class, listener);
    }


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration() {
        return createEnumerationInternal();
    }


    // Protected methods
    protected boolean isCaseSensitive() {
        return mSensitive;
    }

    protected Iterator<String> getActiveNames() {
        return getActiveNames(null);
    }

    protected Iterator<String> getActiveNames(Collection<String> filter) {
        class Elements implements Iterator<String> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mElements.size()) {
                    String name = getNamesInternal().get(mIndex);
                    if (filter == null || !filter.contains(name)) {
                        T element = mElements.get(getIndexInternal(mIndex));
                        if (element != null) {
                            next = true;
                            break;
                        }
                    }
                    mIndex++;
                }
                return next;
            }

            @Override
            public String next() throws java.util.NoSuchElementException {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return getNamesInternal().get(mIndex++);
            }
        }
        return new Elements();
    }


    protected Iterator<T> getActiveElements() {
        return getActiveElements(null);
    }

    protected Iterator<T> getActiveElements(Collection<String> filter) {
        class Elements implements Iterator<T> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mElements.size()) {
                    String name = getNamesInternal().get(mIndex);
                    if (filter == null || !filter.contains(name)) {
                        T element = mElements.get(getIndexInternal(mIndex));
                        if (element != null) {
                            next = true;
                            break;
                        }
                    }
                    mIndex++;
                }
                return next;
            }

            @Override
            public T next() throws java.util.NoSuchElementException {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return mElements.get(getIndexInternal(mIndex++));
            }

            @Override
            public void remove() {
                removeContainerElement(getIndexInternal(--mIndex));
            }
        }
        return new Elements();
    }

    protected T getElement(int index)
        throws SQLException {
        synchronized (mLock) {
            try {
                return getElementByIndex(getIndexInternal(index));
            } catch (WrappedTargetException e) {
                throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    protected T getElement(String name)
        throws SQLException {
        T element = null;
        synchronized (mLock) {
            if (getNamesInternal().contains(name)) {
                try {
                    int idx = getIndexInternal(name);
                    element = getElementByIndex(idx);
                } catch (WrappedTargetException e) {
                    throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
                }
            }
        }
        return element;
    }

    protected void removeContainerElement(int idx) {
        System.out.println("ContainerBase.removeContainerElement() 1 index: "  + idx);
        String name = getNamesInternal().remove(idx);
        T element = mElements.remove(idx);
        ContainerEvent event = new ContainerEvent(this, name, element, null);
        for (XContainerListener listener : getContainerListeners()) {
            System.out.println("ContainerBase.removeContainerElement() 2 index: "  + idx);
            listener.elementRemoved(event);
        }
        UnoHelper.disposeComponent(element);
    }

    // XXX: Shared methods between ContainerBase and ContainerSuper
    // XXX: ContainerBase support duplicate name and the contents will not be sorted.
    // XXX: ContainerSuper does not support duplicate names and the contents will be sorted alphabetically.
    protected List<String> getNamesInternal() {
        return mNames;
    }
    protected int getIndexInternal(int index) {
        return index;
    }
    protected int getIndexInternal(String name) {
        return mNames.indexOf(name);
    }

    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this);
    }

    // Abstract protected methods
    protected abstract T createElement(int idx) throws SQLException;

    // Private methods
    protected T getElementByName(String name)
        throws WrappedTargetException {
        int idx = getIndexInternal(name);
        return getElementByIndex(idx);
    }

    protected T getElementByIndex(int idx)
        throws WrappedTargetException {
        T element = mElements.get(idx);
        if (element == null) {
            try {
                element = createElement(idx);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("ContainerBase.getElementByIndex() 1 index: "  + idx);
                try {
                    removeContainerElement(idx);
                } catch (Exception ignored) { }
                throw new WrappedTargetException(e.getMessage(), this, e);
            }
            mElements.set(idx, element);
        }
        return element;
    }

}
