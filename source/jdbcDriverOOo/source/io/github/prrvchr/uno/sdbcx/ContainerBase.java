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

import java.util.Collection;
import java.util.Iterator;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XContainerListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lib.uno.helper.InterfaceContainer;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.Any;
import com.sun.star.util.XRefreshListener;
import com.sun.star.util.XRefreshable;

import io.github.prrvchr.uno.driver.container.BiMap;
import io.github.prrvchr.uno.driver.container.BiMapMain;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ContainerBase<T extends Descriptor>
    extends ContainerMain<T>
    implements XAppend,
               XDrop,
               XDataDescriptorFactory,
               XRefreshable {

    private InterfaceContainer mRefresh = new InterfaceContainer();

    // The constructor method:
    public ContainerBase(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        super(service, services, lock, new BiMapMain<>(), sensitive);
    }

    public ContainerBase(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive,
                         String[] names) {
        super(service, services, lock, new BiMapMain<>(names), sensitive);
    }

    public ContainerBase(String service,
                         String[] services,
                         Object lock,
                         BiMap<T> bimap,
                         boolean sensitive) {
        super(service, services, lock, bimap, sensitive);
    }

    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        Iterator<?> iterator;
        synchronized (mLock) {
            mBimap.clear();
            refreshInternal();
            iterator = mRefresh.iterator();
        }
        // early disposal
        if (iterator != null) {
            broadcastRefreshed(iterator);
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

    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException,
               IndexOutOfBoundsException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        try {
            removeElement(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException {
        if (!hasByName(name)) {
            throw new NoSuchElementException();
        }
        try {
            removeElement(name, true);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return createDescriptor();
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException, ElementExistException {
        try {
            T element = appendElement(descriptor);
            if (element == null) {
                String name = getElementName(descriptor);
                String error = String.format("Table: %s can't be created!!!", name);
                throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
            // XXX: appendElement() can change the name!!!
            String name = getElementName(descriptor);
            synchronized (mLock) {
                mBimap.addElement(name, element);
            }

            broadcastElementInserted(element, name);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // Protected methods
    protected void refill(String[] names) {
        synchronized (mLock) {
            // XXX: We only add new elements, as per the C++ implementation.
            for (String name : names) {
                if (!hasByName(name)) {
                    mBimap.addElement(name, null);
                }
            }
        }
    }

    // XXX: For all container but TableContainerMain has its own method
    protected String getElementName(XPropertySet descriptor) throws java.sql.SQLException {
        return DBTools.getDescriptorStringValue(descriptor, PropertyID.NAME);
    }

    protected void replaceElement(String oldname, String newname) {
        // XXX: We can set the name only for simple name (ie: column, index...)
        replaceElement(oldname, newname, true);
    }

    protected void replaceElement(String oldname, String newname, boolean rename) {
        System.out.println("ContainerSuper.replaceElement() 1");
        if (!newname.equals(oldname) && hasByName(oldname)) {
            T element = null;
            synchronized (mLock) {
                element = mBimap.renameElement(oldname, newname);
            }
            if (element != null && rename) {
                // XXX: We cannot set the name of composed names (ie: table and view)
                element.setName(newname);
            }
            if (element != null) {
                broadcastElementReplaced(element, oldname, newname);
            }
        }
    }

    protected void removeElement(int index)
        throws java.sql.SQLException {
        System.out.println("ContainerSuper.removeElement() 1 index: "  + index);
        removeElement(index, true);
    }

    protected void removeElement(String name,
                                 boolean really)
        throws java.sql.SQLException {
        int index = mBimap.getIndex(name);
        removeElement(index, really);
    }

    protected XPropertySet cloneDescriptor(XPropertySet descriptor) {
        XPropertySet element = createDescriptor();
        UnoHelper.copyProperties(descriptor, element);
        return element;
    }

    protected abstract XPropertySet createDescriptor();

    protected void insertElement(String name,
                                 T element) {
        if (!hasByName(name)) {
            synchronized (mLock) {
                mBimap.addElement(name, element);
            }
        }
    }

    @Override
    protected T createElement(int index) throws java.sql.SQLException {
        String name = mBimap.getName(index);
        return createElement(name);
    }

    // Abstract protected methods
    protected abstract T createElement(String name) throws java.sql.SQLException;

    // XXX: Shared methods between ContainerMain, ContainerBase and ContainerSuper
    // XXX: ContainerBase support duplicate name and the contents will not be sorted.
    // XXX: ContainerSuper does not support duplicate names and the contents will be sorted alphabetically.
    // Abstract protected methods
    protected abstract void refreshInternal();
    protected abstract T appendElement(XPropertySet descriptor) throws java.sql.SQLException;
    protected abstract void removeDataBaseElement(int index, String name) throws java.sql.SQLException;

    @Override
    protected void broadcastRefreshed() {
        broadcastRefreshed(mRefresh.iterator());
    }

    @Override
    protected Iterator<T> getActiveElements(Collection<String> filter) {
        return getActiveElements(filter, true);
    }

    private void removeElement(int index,
                               boolean really)
        throws java.sql.SQLException {
        if (really) {
            String name = mBimap.getName(index);
            removeDataBaseElement(index, name);
        }
        removeContainerElement(index);
    }

    private void broadcastElementReplaced(T element, String oldname, String newname) {
        broadcastContainerElementReplaced(element, oldname, newname);
        broadcastRefreshed();
    }

    protected void broadcastElementInserted(T element, String name) {
        broadcastContainerElementInserted(element, name);
        broadcastRefreshed();
    }

    private void broadcastContainerElementReplaced(T element, String oldname, String newname) {
        ContainerEvent event = null;
        for (XContainerListener listener : getContainerListeners()) {
            if (event == null) {
                event = new ContainerEvent(this, newname, element, oldname);
            }
            listener.elementReplaced(event);
        }
    }

    private void broadcastContainerElementInserted(T element, String name) {
        // XXX: notify our container listeners
        ContainerEvent event = new ContainerEvent(this, name, element, null);
        for (XContainerListener listener : getContainerListeners()) {
            listener.elementInserted(event);
        }
    }

    private void broadcastRefreshed(Iterator<?> refresh) {
        EventObject event = null;
        while (refresh.hasNext()) {
            if (event == null) {
                event = new EventObject(this);
            }
            XRefreshListener listener = (XRefreshListener) refresh.next();
            listener.refreshed(event);
        }
    }

}
