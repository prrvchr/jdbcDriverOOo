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

import java.util.List;

import com.sun.star.container.XEnumeration;

import io.github.prrvchr.uno.driver.helper.BiMap;


public abstract class ContainerSuper<T extends Descriptor>
    extends ContainerBase<T> {

    protected BiMap<String> mNames;

    // The constructor method:
    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        super(service, services, lock, sensitive, false);
        mNames = new BiMap<>(BiMap.getComparator(sensitive));
    }

    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     String[] names) {
        super(service, services, lock, sensitive, names, false);
        mNames = new BiMap<>(BiMap.getComparator(sensitive), names);
    }

    @Override
    protected List<String> getNamesInternal() {
        return mNames;
    }
    @Override
    protected int getIndexInternal(int index) {
        return mNames.getIndexInternal(index);
    }
    @Override
    protected int getIndexInternal(String name) {
        return mNames.indexOf(name);
    }

    @Override
    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this, mNames.getEnumerationOrder());
    }

}
