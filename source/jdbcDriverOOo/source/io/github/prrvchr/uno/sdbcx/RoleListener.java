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

import com.sun.star.container.ContainerEvent;
import com.sun.star.container.XContainerListener;
import com.sun.star.lang.EventObject;
import com.sun.star.uno.AnyConverter;

import io.github.prrvchr.uno.sdb.Role;


public class RoleListener<T extends Role>
    implements XContainerListener {

    private RoleContainer<T> mContainer;

    public RoleListener(RoleContainer<T> container) {
        mContainer = container;
    }

    @Override
    public void disposing(EventObject event) {
        System.out.println("RoleListener.disposing()");
    }

    @Override
    public void elementInserted(ContainerEvent event) {
        System.out.println("RoleListener.elementInserted()");
    }

    @Override
    public void elementRemoved(ContainerEvent event) {
        System.out.println("RoleListener.elementRemoved() 1");
        String name = AnyConverter.toString(event.Accessor);
        if (mContainer.hasByName(name)) {
            mContainer.removeElement(name);
        }
        System.out.println("RoleListener.elementRemoved() 2");
    }

    @Override
    public void elementReplaced(ContainerEvent event) {
        System.out.println("RoleListener.elementReplaced()");
    }

}
