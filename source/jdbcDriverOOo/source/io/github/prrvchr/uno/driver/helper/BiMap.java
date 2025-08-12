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
package io.github.prrvchr.uno.driver.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;


public class BiMap<K> extends ArrayList<K> {

    private static final long serialVersionUID = 2937728352555497256L;
    private Set<K> mOrder;
    private List<K> mIndex;

    public BiMap(Comparator<K> comparator, K[] data) {
        super();
        mOrder = new TreeSet<>(comparator);
        for (K value : data) {
            super.add(value);
            mOrder.add(value);
        }
        mIndex = getIndex();
    }
    public BiMap(Comparator<K> comparator) {
        super();
        mOrder = new TreeSet<>(comparator);
        mIndex = new ArrayList<>();
    }

    public static final Comparator<String> getComparator(boolean sensitive) {
        return new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                int comp;
                if (sensitive) {
                    comp = x.compareTo(y);
                } else {
                    comp = x.compareToIgnoreCase(y);
                }
                return comp;
            }
        };
    }

    public int getIndexInternal(int index) {
        K value = mIndex.get(index);
        return super.indexOf(value);
    }

    public int[] getEnumerationOrder() {
        int[] orders = new int[mOrder.size()];
        int i = 0;
        for (K value : mOrder) {
            orders[i++] = indexOf(value);
        }
        return orders;
    }

    @Override
    public K get(int index) {
        return  mIndex.get(index);
    }

    @Override
    public int indexOf(Object value) {
        return super.indexOf(value);
    }

    @Override
    public void clear() {
        super.clear();
        mOrder.clear();
        mIndex.clear();
    }

    @Override
    public boolean add(K value) {
        mOrder.add(value);
        mIndex = getIndex();
        return super.add(value);
    }

    @Override
    public void add(int index, K value) {
        mOrder.add(value);
        mIndex = getIndex();
        super.add(index, value);
    }

    @Override
    public boolean remove(Object value) {
        mOrder.remove(value);
        mIndex = getIndex();
        return super.remove(value);
    }

    @Override
    public K remove(int index) {
        K value = super.remove(index);
        mOrder.remove(value);
        mIndex = getIndex();
        return value;
    }

    @Override
    public Iterator<K> iterator() {
        return mIndex.iterator();
    }

    @Override
    public Object[] toArray() {
        return mIndex.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] values) {
        return mIndex.toArray(values);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        mOrder.addAll(c);
        mIndex = getIndex();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends K> c) {
        mOrder.addAll(c);
        mIndex = getIndex();
        return super.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        mOrder.removeAll(c);
        mIndex = getIndex();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        mOrder.retainAll(c);
        mIndex = getIndex();
        return super.retainAll(c);
    }

    @Override
    public K set(int index, K newvalue) {
        K oldvalue = super.set(index, newvalue);
        mOrder.remove(oldvalue);
        mOrder.add(newvalue);
        mIndex = getIndex();
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

    private List<K> getIndex() {
        return new ArrayList<K>(mOrder);
    }
}

