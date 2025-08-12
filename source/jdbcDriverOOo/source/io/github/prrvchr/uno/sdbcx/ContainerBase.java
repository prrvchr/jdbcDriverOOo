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

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
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
        super(service, services, lock, sensitive);
    }

    public ContainerBase(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive,
                         boolean master) {
        super(service, services, lock, sensitive, master);
    }

    public ContainerBase(String service,
                         String[] services,
                         Object lock,
                         boolean sensitive,
                         String[] names,
                         boolean master) {
        super(service, services, lock, sensitive, names, master);
    }

    public ContainerBase(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     String[] names) {
        super(service, services, lock, sensitive, names);
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        Iterator<?> iterator;
        synchronized (mLock) {
            for (T element : mElements) {
                UnoHelper.disposeComponent(element);
            }
            mElements.clear();
            getNamesInternal().clear();
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
        removeElement(getIndexInternal(index));
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
        removeElement(getIndexInternal(name));
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
        synchronized (mLock) {
            System.out.println("ContainerBase.appendByDescriptor() 1");
            T element = appendElement(descriptor);
            if (element == null) {
                String name = getElementName(descriptor);
                String error = String.format("Table: %s can't be created!!!", name);
                throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
            // XXX: appendElement() can change the name!!!
            String name = getElementName(descriptor);
            getNamesInternal().add(name);
            mElements.add(element);

            // XXX: notify our container listeners
            event = new ContainerEvent(this, name, element, null);
        }
        for (XContainerListener listener : getContainerListeners()) {
            System.out.println("ContainerBase.appendByDescriptor() 2");
            listener.elementInserted(event);
        }
    }

    // Protected methods
    protected void refill(String[] names) {
        // XXX: We only add new elements, as per the C++ implementation.
        for (String name : names) {
            if (!getNamesInternal().contains(name)) {
                getNamesInternal().add(name);
                mElements.add(null);
            }
        }
    }

    // XXX: For all container but TableContainerMain has its own method
    protected String getElementName(XPropertySet descriptor)
        throws SQLException {
        return DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
    }

    protected void replaceElement(String oldname, String newname)
        throws SQLException {
        // XXX: We can set the name only for simple name (ie: column, index...)
        replaceElement(oldname, newname, true);
    }

    protected void replaceElement(String oldname, String newname, boolean rename)
        throws SQLException {
        synchronized (mLock) {
            System.out.println("ContainerSuper.replaceElement() 1");
            if (!newname.equals(oldname) && getNamesInternal().contains(oldname)) {
                int idx = getIndexInternal(oldname);
                getNamesInternal().set(idx, newname);
                T element = mElements.get(idx);
                if (element != null && rename) {
                    // XXX: We cannot set the name of composed names (ie: table and view)
                    element.setName(newname);
                }
                ContainerEvent e1 = null;
                for (XContainerListener listener : getContainerListeners()) {
                    if (e1 == null) {
                        e1 = new ContainerEvent(this, newname, element, oldname);
                    }
                    System.out.println("ContainerSuper.replaceElement() 2");
                    listener.elementReplaced(e1);
                }
                EventObject e2 = null; 
                for (Iterator<?> iterator = mRefresh.iterator(); iterator.hasNext();) {
                    if (e2 == null) {
                        e2 = new EventObject(this);
                    }
                    XRefreshListener listener = (XRefreshListener) iterator.next();
                    listener.refreshed(e2);
                }
            }
        }
    }

    protected void removeElement(int idx)
        throws SQLException {
        System.out.println("ContainerSuper.removeElement() 1 index: "  + idx);
        removeElement(idx, true);
    }

    protected void removeContainerElement(String name) {
        int idx = getIndexInternal(name);
        removeContainerElement(idx);
    }

    protected void removeElement(String name,
                                 boolean really)
        throws SQLException {
        int idx = getIndexInternal(name);
        removeElement(idx, really);
    }

    private void removeElement(int idx,
                               boolean really)
        throws SQLException {
        if (really) {
            String name = getNamesInternal().get(idx);
            removeDataBaseElement(idx, name);
        }
        removeContainerElement(idx);
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
            if (!getNamesInternal().contains(name)) {
                getNamesInternal().add(name);
                mElements.add(element);
            }
        }
    }

    @Override
    protected T createElement(int idx) throws SQLException {
        String name = getNamesInternal().get(idx);
        return createElement(name);
    }

    // Abstract protected methods
    protected abstract T createElement(String name) throws SQLException;

    // XXX: Shared methods between ContainerMain, ContainerBase and ContainerSuper
    // XXX: ContainerBase support duplicate name and the contents will not be sorted.
    // XXX: ContainerSuper does not support duplicate names and the contents will be sorted alphabetically.
    // Abstract protected methods
    protected abstract void refreshInternal();
    protected abstract T appendElement(XPropertySet descriptor) throws SQLException;
    protected abstract void removeDataBaseElement(int index, String name) throws SQLException;

}
