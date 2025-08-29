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


import com.sun.star.container.XEnumeration;

import io.github.prrvchr.uno.driver.container.BiMap;
import io.github.prrvchr.uno.driver.container.BiMapBase;
import io.github.prrvchr.uno.driver.container.BiMapSuper;
import io.github.prrvchr.uno.driver.provider.DBTools;


public abstract class ContainerSuper<T extends Descriptor>
    extends ContainerBase<T> {


    // The constructor method:
    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive) {
        super(service, services, lock,
              new BiMapBase<T>(DBTools.getComparator(sensitive)), sensitive);
    }

    public ContainerSuper(String service,
                     String[] services,
                     Object lock,
                     boolean sensitive,
                     String[] names) {
        super(service, services, lock,
              new BiMapBase<T>(DBTools.getComparator(sensitive), names), sensitive);
    }

    public ContainerSuper(String service,
                          String[] services,
                          Object lock,
                          BiMap<T> bimap,
                          boolean sensitive) {
        super(service, services, lock, bimap, sensitive);
        new BiMapSuper<T>(DBTools.getComparator(sensitive), bimap, new String[0]);
    }

    @Override
    protected XEnumeration createEnumerationInternal() {
        return new ContainerEnumeration(this, mBimap.getEnumerationOrder());
    }

}
