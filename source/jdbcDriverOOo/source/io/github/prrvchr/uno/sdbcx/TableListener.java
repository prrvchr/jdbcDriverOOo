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


public class TableListener
    implements XContainerListener {

    public TableListener() { }

    @Override
    public void disposing(EventObject event) {
        //System.out.println("TableListener.disposing()");
    }

    @Override
    public void elementInserted(ContainerEvent event) {
        //System.out.println("TableListener.elementInserted()");
    }

    @Override
    public void elementRemoved(ContainerEvent event) {
        //String name = AnyConverter.toString(event.Accessor);
        //TableSuper table = (TableSuper) event.Element;
        //System.out.println("TableListener.elementRemoved() 1 TableName:" + table.getName() +
        //                   " - name: " + name);
        //table.getConnection().getTablesInternal().removeForeignKeyTables(table, name);
    }

    @Override
    public void elementReplaced(ContainerEvent event) {
        //System.out.println("TableListener.elementReplaced() 1");
    }

}
