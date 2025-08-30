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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

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
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.container.BiMap;
import io.github.prrvchr.uno.driver.container.BiMapMain;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.helper.ServiceInfo;


public abstract class ContainerMain<T extends Descriptor>
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XNameAccess,
               XIndexAccess,
               XEnumerationAccess {

    protected Object mLock;
    protected BiMap<T> mBimap;
    protected boolean mSensitive;
    private final String mService;
    private final String[] mServices;
    private final EventListenerList mContainer = new EventListenerList();

    // The constructor method:
    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive) {
        this(service, services, lock, null, sensitive);
    }

    public ContainerMain(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     String[] names) {
        this(service, services, lock, null, sensitive, names);
    }

    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         BiMap<T> bimap,
                         boolean sensitive,
                         String[] names) {
        this(service, services, lock, bimap, sensitive);
        if (bimap != null) {
            mBimap = bimap;
        } else {
            mBimap = new BiMapMain<>(names);
        }
    }

    public ContainerMain(String service,
                         String[] services,
                         Object lock,
                         BiMap<T> bimap,
                         boolean sensitive) {
        mService = service;
        mServices = services;
        mLock = lock;
        mSensitive = sensitive;
        if (bimap != null) {
            mBimap = bimap;
        } else {
            mBimap = new BiMapMain<>();
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
            mBimap.clear();
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
        if (!hasByName(name)) {
            throw new NoSuchElementException();
        }
        try {
            return getElementByName(name);
        } catch (SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    @Override
    public String[] getElementNames() {
        return mBimap.getElementNames();
    }

    @Override
    public boolean hasByName(String name) {
        return mBimap.hasByName(name);
    }


    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType() {
        return new Type(XPropertySet.class);
    }

    @Override
    public boolean hasElements() {
        return !mBimap.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException,
               WrappedTargetException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        try {
            return getElementByIndex(index);
        } catch (SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    @Override
    public int getCount() {
        return mBimap.getCount();
    }


    // XXX: For all container but TableContainerMain has its own method
    protected String getElementName(XPropertySet descriptor) throws SQLException {
        return DBTools.getDescriptorStringValue(descriptor, PropertyID.NAME);
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
                while (mIndex < mBimap.getCount()) {
                    String name = mBimap.getName(mIndex);
                    if (filter == null || !filter.contains(name)) {
                        T element = mBimap.getByIndex(mIndex);
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
                return mBimap.getName(mIndex++);
            }
        }
        return new Elements();
    }

    protected Iterator<T> getActiveElements() {
        return getActiveElements(null);
    }

    protected Iterator<T> getActiveElements(Collection<String> filter) {
        return getActiveElements(filter, false);
    }

    protected void removeContainerElement(int index) {
        removeContainerElement(index, true);
    }

    protected void removeContainerElement(String name, boolean dispose) {
        int index = mBimap.getIndex(name);
        removeContainerElement(index, dispose);
    }

    protected void removeContainerElement(int index, boolean dispose) {
        System.out.println("ContainerBase.removeContainerElement() 1 index: "  + index);
        T element = null;
        String name;
        synchronized (mLock) {
            name = mBimap.getName(index);
            element = mBimap.removeElement(index);
        }
        broadcastElementRemoved(element, name);
        if (dispose && element != null) {
            element.dispose();
        }
    }

    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this);
    }

    // Abstract protected methods
    protected abstract T createElement(int index) throws SQLException;

    protected T getElementByName(String name)
        throws SQLException {
        return getElementByIndex(mBimap.getIndex(name));
    }

    protected T getElementByIndex(int index)
        throws SQLException {
        T element = mBimap.getByIndex(index);
        if (element == null) {
            synchronized (mLock) {
                try {
                    element = createElement(index);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("ContainerBase.getElementByIndex() 1 index: "  + index);
                    try {
                        removeContainerElement(index);
                    } catch (Exception ignored) { }
                    throw new SQLException(e.getMessage());
                }
                mBimap.setElement(index, element);
            }
        }
        return element;
    }

    private void broadcastElementRemoved(T element, String name) {
        broadcastContainerElementRemoved(element, name);
        broadcastRefreshed();
    }

    private void broadcastContainerElementRemoved(T element, String name) {
        ContainerEvent event = null;
        for (XContainerListener listener : getContainerListeners()) {
            if (event == null) {
                event = new ContainerEvent(this, name, element, null);
            }
            listener.elementRemoved(event);
        }
    }

    protected abstract void broadcastRefreshed();

    protected Iterator<T> getElements() {
        return getElements(null, true);
    }

    protected Iterator<T> getElements(Collection<String> filter, boolean remove) {
        class Elements implements Iterator<T> {
            private int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < getCount()) {
                    String name = mBimap.getName(mIndex);
                    if (filter == null || !filter.contains(name)) {
                        next = true;
                        break;
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
                try {
                    return getElementByIndex(mIndex++);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new java.util.NoSuchElementException(e);
                }
            }

            @Override
            public void remove() {
                if (remove) {
                    mBimap.removeElement(--mIndex);
                }
            }
        }
        return new Elements();
    }

    public Iterator<T> getActiveElements(Collection<String> filter, boolean remove) {
        class Elements implements Iterator<T> {
            private int mIdx = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIdx < getCount()) {
                    String name = mBimap.getName(mIdx);
                    if (filter == null || !filter.contains(name)) {
                        T element = mBimap.getByIndex(mIdx);
                        if (element != null) {
                            next = true;
                            break;
                        }
                    }
                    mIdx++;
                }
                return next;
            }

            @Override
            public T next() throws java.util.NoSuchElementException {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return mBimap.getByIndex(mIdx++);
            }

            @Override
            public void remove() {
                if (remove) {
                    mBimap.removeElement(--mIdx);
                }
            }
        }
        return new Elements();
    }


}
