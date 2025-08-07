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
import java.util.Arrays;
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
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ContainerBase<T extends Descriptor>
    extends WeakBase
    implements XServiceInfo,
               XContainer,
               XNameAccess,
               XIndexAccess,
               XEnumerationAccess,
               XColumnLocate {

    protected Object mLock;
    protected List<T> mElements;
    protected boolean mSensitive;
    private List<String> mNames;
    private final String mService;
    private final String[] mServices;
    private final EventListenerList mContainer = new EventListenerList();

    // The constructor method:
    public ContainerBase(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        mService = service;
        mServices = services;
        mLock = lock;
        mSensitive = sensitive;
        mElements = new ArrayList<>();
    }

    public ContainerBase(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     List<String> names) {
        this(service, services, lock, sensitive);
        mNames = new ArrayList<>();
        for (String name : names) {
            mNames.add(name);
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
        int index = getNamesInternal().indexOf(name);
        return getElementByIndex(index);
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
        return getElementByIndex(index);
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


    // com.sun.star.sdbcx.XColumnLocate
    @Override
    public int findColumn(String name)
        throws SQLException {
        if (!hasByName(name)) {
            String error = String.format("Error Column: %s not fount", name);
            throw new SQLException(error, this, StandardSQLState.SQL_COLUMN_NOT_FOUND.text(), 0, null);
        }
        return getNamesInternal().indexOf(name) + 1;
    }


    // Protected methods
    protected boolean isCaseSensitive() {
        return mSensitive;
    }

    protected Iterator<String> getActiveNames() {
        return getActiveNames(Arrays.asList(getElementNames()));
    }

    protected Iterator<String> getActiveNames(Collection<String> filter) {
        class Elements implements Iterator<String> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mElements.size()) {
                    T element = mElements.get(mIndex);
                    String name = getNamesInternal().get(mIndex);
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
                return getNamesInternal().get(mIndex++);
            }
        }
        return new Elements();
    }


    protected Iterator<T> getActiveElements() {
        return getActiveElements(Arrays.asList(getElementNames()));
    }

    protected Iterator<T> getActiveElements(Collection<String> filter) {
        class Elements implements Iterator<T> {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                boolean next = false;
                while (mIndex < mElements.size()) {
                    T element = mElements.get(mIndex);
                    String name = getNamesInternal().get(mIndex);
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
                return mElements.get(mIndex++);
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
            if (getNamesInternal().contains(name)) {
                try {
                    int index = getNamesInternal().indexOf(name);
                    element = getElementByIndex(index);
                } catch (WrappedTargetException e) {
                    throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
                }
            }
        }
        return element;
    }

    protected void removeContainerElement(int index)
        throws SQLException {
        System.out.println("ContainerBase.removeContainerElement() 1 index: "  + index);
        String name = getNamesInternal().remove(index);
        T element = mElements.remove(index);
        UnoHelper.disposeComponent(element);
        ContainerEvent event = new ContainerEvent(this, name, null, null);
        for (XContainerListener listener : getContainerListeners()) {
            System.out.println("ContainerBase.removeContainerElement() 2 index: "  + index);
            listener.elementRemoved(event);
        }
    }

    // XXX: Shared methods between ContainerBase and ContainerSuper
    // XXX: ContainerBase support duplicate name and the contents will not be sorted.
    // XXX: ContainerSuper does not support duplicate names and the contents will be sorted alphabetically.
    protected List<String> getNamesInternal() {
        return mNames;
    }

    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this);
    }

    // Abstract protected methods
    protected abstract T createElement(int index) throws SQLException;

    // Private methods
    protected T getElementByIndex(int index)
        throws WrappedTargetException {
        T element = mElements.get(index);
        if (element == null) {
            try {
                element = createElement(index);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("ContainerBase.getElementByIndex() 1 index: "  + index);
                try {
                    removeContainerElement(index);
                } catch (Exception ignored) { }
                throw new WrappedTargetException(e.getMessage(), this, e);
            }
            mElements.set(index, element);
        }
        return element;
    }

}
