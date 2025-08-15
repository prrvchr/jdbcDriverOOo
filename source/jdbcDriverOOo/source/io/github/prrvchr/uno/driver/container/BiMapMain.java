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
import java.util.List;

import io.github.prrvchr.uno.sdbcx.Descriptor;


public class BiMapMain<T extends Descriptor>
    implements BiMap<T> {

    private List<T> mElement;
    private List<String> mNames;

    public BiMapMain(String[] data) {
        this();
        for (String value : data) {
            mNames.add(value);
            mElement.add(null);
        }
    }
    public BiMapMain() {
        mElement = new ArrayList<>();
        mNames = new ArrayList<>();
    }

    @Override
    public int getIndex(String value) {
        return mNames.indexOf(value);
    }

    @Override
    public String getName(int index) {
        return  mNames.get(index);
    }

    @Override
    public int[] getEnumerationOrder() {
        int[] orders = new int[mNames.size()];
        for (int i = 0; i < mNames.size(); i++) {
            orders[i] = i;
        }
        return orders;
    }

    @Override
    public void clear() {
        clearElements();
        mNames.clear();
    }

    @Override
    public boolean hasByName(String name) {
        return mNames.contains(name);
    }

    @Override
    public boolean isEmpty() {
        return mElement.isEmpty();
    }

    @Override
    public int getCount() {
        return mElement.size();
    }

    @Override
    public T getByIndex(int index) {
        return mElement.get(index);
    }

    @Override
    public T getByName(String name) {
        return mElement.get(mNames.indexOf(name));
    }

    @Override
    public String[] getElementNames() {
        return mNames.toArray(new String[0]);
    }

    @Override
    public void setElement(int index, T element) {
        mElement.set(index, element);
    }

    @Override
    public T addElement(String name, T element) {
        mElement.add(element);
        mNames.add(name);
        return element;
    }

    @Override
    public T removeElement(int index) {
        mNames.remove(index);
        return mElement.remove(index);

    }

    @Override
    public T renameElement(String oldname, String newname) {
        int index = mNames.indexOf(oldname);
        mNames.set(index, newname);
        return mElement.get(index);
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
