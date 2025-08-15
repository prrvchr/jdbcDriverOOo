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


public class BiMapSuper<T extends Descriptor>
    implements BiMap<T> {

    private BiMap<T> mBimap;
    private Set<String> mNames;
    private List<String> mOrder;

    public BiMapSuper(Comparator<String> comparator, BiMap<T> bimap, String[] data) {
        mBimap = bimap;
        mNames = new TreeSet<>(comparator);
        for (String value : data) {
            mNames.add(value);
        }
        mOrder = getOrder();
    }
    public BiMapSuper(Comparator<String> comparator, BiMap<T> bimap) {
        mBimap = bimap;
        mNames = new TreeSet<>(comparator);
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
            orders[i++] = mOrder.indexOf(value);
        }
        return orders;
    }

    @Override
    public void clear() {
        mNames.clear();
        mOrder.clear();
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
        return mNames.size();
    }

    @Override
    public T getByIndex(int index) {
        String name = mOrder.get(index);
        return mBimap.getByName(name);
    }

    @Override
    public T getByName(String name) {
        return mBimap.getByName(name);
    }

    @Override
    public String[] getElementNames() {
        return mOrder.toArray(new String[0]);
    }

    @Override
    public void setElement(int index, T element) { }

    @Override
    public T addElement(String name, T element) {
        mNames.add(name);
        mOrder = getOrder();
        return mBimap.getByName(name);
    }

    @Override
    public T removeElement(int index) {
        String name = mOrder.get(index);
        T value = mBimap.getByName(name);
        mNames.remove(name);
        mOrder = getOrder();
        return value;
    }

    @Override
    public T renameElement(String oldname, String newname) {
        mNames.remove(oldname);
        mNames.add(newname);
        mOrder = getOrder();
        return mBimap.getByName(newname);
    }

    private List<String> getOrder() {
        return new ArrayList<>(mNames);
    }

}
