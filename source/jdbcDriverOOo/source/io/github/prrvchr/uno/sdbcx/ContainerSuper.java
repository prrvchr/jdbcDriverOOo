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
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lib.uno.helper.InterfaceContainer;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.Any;
import com.sun.star.util.XRefreshable;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ContainerSuper<T extends Descriptor>
    extends ContainerBase<T>
    implements XAppend,
               XDrop,
               XDataDescriptorFactory,
               XRefreshable {

    protected InterfaceContainer mRefresh = new InterfaceContainer();
    private BiMap<String> mNames;
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
    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        super(service, services, lock, sensitive);
        mNames = new BiMap<>(mCaseSensitiveComparator);
    }

    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     List<String> names)
        throws ElementExistException {
        this(service, services, lock, sensitive);
        for (String name : names) {
            if (mNames.contains(name)) {
                throw new ElementExistException(name, this);
            }
            mNames.add(name);
            mElements.add(null);
        }
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
            mNames.add(name);
            mElements.add(element);

            // XXX: notify our container listeners
            event = new ContainerEvent(this, name, element, null);
            iterator = mContainer.iterator();
        }
        while (iterator.hasNext()) {
            XContainerListener listener = (XContainerListener) iterator.next();
            listener.elementInserted(event);
        }
    }

    protected void refill(List<String> names) {
        // XXX: We only add new elements, as per the C++ implementation.
        for (String name : names) {
            if (!mNames.contains(name)) {
                mNames.add(name);
                mElements.add(null);
            }
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

    // Protected methods
    protected void replaceElement(String oldname, String newname)
        throws SQLException {
        // XXX: We can set the name only for simple name (ie: column, index...)
        replaceElement(oldname, newname, true);
    }

    protected void replaceElement(String oldname, String newname, boolean rename)
        throws SQLException {
        synchronized (mLock) {
            if (!newname.equals(oldname) && mNames.contains(oldname)) {
                int index = mNames.indexOf(oldname);
                mNames.set(index, newname);
                T element = mElements.get(index);
                if (element != null && rename) {
                    // XXX: We cannot set the name of composed names (ie: table and view)
                    element.setName(newname);
                }
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

    protected void removeElement(int index)
        throws SQLException {
        removeElement(index, true);
    }

    protected void removeElement(String name,
                                 boolean really)
        throws SQLException {
        int index = mNames.indexOf(name);
        removeElement(index, really);
    }

    protected void removeElement(int index,
                                 boolean really)
        throws SQLException {
        if (really) {
            String name = mNames.get(index);
            removeDataBaseElement(index, name);
        }
        super.removeElement(index);
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
            if (!mNames.contains(name)) {
                mNames.add(name);
                mElements.add(element);
            }
        }
    }

    // XXX: Shared methods between ContainerBase and ContainerSuper
    // XXX: ContainerBase support duplicate name and the contents will not be sorted.
    // XXX: ContainerSuper does not support duplicate names and the contents will be sorted alphabetically.
    @Override
    protected List<String> getNamesInternal() {
        return mNames;
    }

    @Override
    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this, mNames.getEnumerationOrder());
    }

    protected T createElement(int index) throws SQLException {
        String name = mNames.get(index);
        return createElement(name);
    }

    // Abstract protected methods
    protected abstract T appendElement(XPropertySet descriptor) throws SQLException;
    protected abstract void removeDataBaseElement(int index, String name) throws SQLException;
    protected abstract void refreshInternal();
    protected abstract T createElement(String name) throws SQLException;

    private class BiMap<K> implements List<K> {

        private Set<K> mOrder;
        private List<K> mIndex;

        public BiMap(Comparator<K> comparator) {
            mOrder = new TreeSet<>(comparator);
            mIndex = new ArrayList<>();
        }

        public int[] getEnumerationOrder() {
            int[] orders = new int[mOrder.size()];
            int i = 0;
            for (K value : mOrder) {
                orders[i] = mIndex.indexOf(value);
                i++;
            }
            return orders;
        }

        @Override
        public K get(int index) {
            return mIndex.get(index);
        }

        @Override
        public void clear() {
            mIndex.clear();
            mOrder.clear();
        }

        @Override
        public boolean add(K value) {
            mOrder.add(value);
            return mIndex.add(value);
        }

        @Override
        public void add(int index, K value) {
            mOrder.add(value);
            mIndex.add(index, value);
        }

        @Override
        public boolean remove(Object value) {
            mOrder.remove(value);
            return mIndex.remove(value);
        }

        @Override
        public K remove(int index) {
            K value = mIndex.remove(index);
            mOrder.remove(value);
            return value;
        }

        @Override
        public boolean contains(Object value) {
            return mIndex.contains(value);
        }

        @Override
        public int indexOf(Object value) {
            return mIndex.indexOf(value);
        }

        @Override
        public int size() {
            return mIndex.size();
        }

        @Override
        public boolean isEmpty() {
            return mIndex.isEmpty();
        }

        @Override
        public Iterator<K> iterator() {
            return mOrder.iterator();
        }

        @Override
        public Object[] toArray() {
            return mOrder.toArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object[] toArray(Object[] values) {
            return mOrder.toArray(values);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return mIndex.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            mOrder.addAll(c);
            return mIndex.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends K> c) {
            mOrder.addAll(c);
            return mIndex.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            mOrder.removeAll(c);
            return mIndex.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            mOrder.retainAll(c);
            return mIndex.retainAll(c);
        }

        @Override
        public K set(int index, K newvalue) {
            K oldvalue = mIndex.set(index, newvalue);
            mOrder.remove(oldvalue);
            mOrder.add(newvalue);
            return oldvalue;
        }

        @Override
        public int lastIndexOf(Object o) {
            return mIndex.lastIndexOf(o);
        }

        @Override
        public ListIterator<K> listIterator() {
            return mIndex.listIterator();
        }

        @Override
        public ListIterator<K> listIterator(int index) {
            return mIndex.listIterator(index);
        }

        @Override
        public List<K> subList(int fromIndex, int toIndex) {
            return mIndex.subList(fromIndex, toIndex);
        }
    }

}
