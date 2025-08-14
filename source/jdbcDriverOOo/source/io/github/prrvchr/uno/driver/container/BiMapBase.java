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
package io.github.prrvchr.uno.driver.container;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.github.prrvchr.uno.sdbcx.Descriptor;


public class BiMapBase<T extends Descriptor>
    implements BiMap<T> {

    private List<T> mElement;
    private Set<String> mNames;
    private List<String> mOrder;
    private List<String> mIndex;

    public BiMapBase(Comparator<String> comparator, String[] data) {
        mElement = new ArrayList<>();
        mNames = new TreeSet<>(comparator);
        mIndex = new ArrayList<>();
        for (String value : data) {
            mNames.add(value);
            mIndex.add(value);
            mElement.add(null);
        }
        mOrder = getOrder();
    }
    public BiMapBase(Comparator<String> comparator) {
        mElement = new ArrayList<>();
        mNames = new TreeSet<>(comparator);
        mIndex = new ArrayList<>();
        mOrder = new ArrayList<>();
    }

    @Override
    public int getIndex(String value) {
        return mOrder.indexOf(value);
    }

    @Override
    public String getName(int index) {
        return  mOrder.get(index);
    }

    @Override
    public int[] getEnumerationOrder() {
        int[] orders = new int[mOrder.size()];
        int i = 0;
        for (String value : mOrder) {
            orders[i++] = mIndex.indexOf(value);
        }
        return orders;
    }

    @Override
    public void clear() {
        clearElements();
        mNames.clear();
        mOrder.clear();
        mIndex.clear();
    }

    @Override
    public boolean hasByName(String name) {
        return mNames.contains(name);
    }

    @Override
    public boolean isEmpty() {
        return mNames.isEmpty();
    }

    @Override
    public int getCount() {
        return mElement.size();
    }

    @Override
    public T getByIndex(int index) {
        return mElement.get(getIndexInternal(index));
    }

    @Override
    public T getByName(String name) {
        return mElement.get(getIndexInternal(name));
    }

    @Override
    public String[] getElementNames() {
        return mOrder.toArray(new String[0]);
    }

    @Override
    public void setElement(int index, T element) {
        mElement.set(getIndexInternal(index), element);
    }

    @Override
    public T addElement(String name, T element) {
        mElement.add(element);
        mNames.add(name);
        mIndex.add(name);
        mOrder = getOrder();
        return element;
    }

    @Override
    public T removeElement(int index) {
        String name = mOrder.get(index);
        T value = mElement.remove(mIndex.indexOf(name));
        mNames.remove(name);
        mIndex.remove(name);
        mOrder = getOrder();
        return value;
    }

    @Override
    public T renameElement(String oldname, String newname) {
        mNames.remove(oldname);
        mNames.add(newname);
        int index = mIndex.indexOf(oldname);
        mIndex.set(index, newname);
        mOrder = getOrder();
        return mElement.get(index);
    }

    // XXX: get the internal index with the used index
    private int getIndexInternal(int index) {
        String name = mOrder.get(index);
        return mIndex.indexOf(name);
    }
    // XXX: get the internal index with the name
    private int getIndexInternal(String name) {
        return mIndex.indexOf(name);
    }

    private List<String> getOrder() {
        return new ArrayList<>(mNames);
    }

    private void clearElements() {
        for (T element : mElement) {
            if (element != null) {
                element.dispose();
            }
        }
        mElement.clear();
    }

}
