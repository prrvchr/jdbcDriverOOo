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
import java.util.List;

import com.sun.star.container.ContainerEvent;
import com.sun.star.container.XContainerListener;
import com.sun.star.lang.EventObject;
import com.sun.star.uno.AnyConverter;


public class ColumnListener
    implements XContainerListener {

    public ColumnListener() { }

    @Override
    public void disposing(EventObject event) {
        System.out.println("ColumnListener.disposing()");
    }

    @Override
    public void elementInserted(ContainerEvent event) {
        System.out.println("ColumnListener.elementInserted()");
    }

    @SuppressWarnings("unused")
    @Override
    public void elementRemoved(ContainerEvent event) {
        System.out.println("ColumnListener.elementRemoved() 1");
        String name = AnyConverter.toString(event.Accessor);
        ColumnBase column = (ColumnBase) event.Element;
        System.out.println("ColumnListener.elementRemoved() 2 ColumnName: " + column.getName());
        //mContainer.getConnection().getTablesInternal().removeForeignKeyColumns(column, name);
        System.out.println("ColumnListener.elementRemoved() 3");
    }

    @Override
    public void elementReplaced(ContainerEvent event) {
        String oldname =  AnyConverter.toString(event.ReplacedElement);
        String newname =  AnyConverter.toString(event.Accessor);
        ColumnBase column = (ColumnBase) event.Element;
        System.out.println("ColumnListener.elementReplaced() 1 oldName: " + oldname);
        System.out.println("ColumnListener.elementReplaced() 2 newName: " + newname);

        TableSuper table = column.getTableInternal();
        // XXX: If the renamed column is declared as index
        // XXX: we need to rename the Index column name to.
        Iterator<Index> indexes = table.getIndexesInternal().getActiveElements();
        while (indexes.hasNext()) {
            Index index = indexes.next();
            // XXX: Containers use lazy loading, updating
            // XXX: is only necessary if they have been loaded
            if (!index.isColumnsLoaded()) {
                System.out.println("ColumnListener.elementReplaced() lasy loaded Index: " + index.getName());
                continue;
            }
            List<String> columns = index.getColumnsInternal().getNamesInternal();
            if (columns.contains(oldname)) {
                columns.set(columns.indexOf(oldname), newname);
                System.out.println("ColumnListener.elementReplaced() 3 Column Index: " + oldname +
                                   " renamed: " + newname);
            }
        }

        System.out.println("ColumnListener.elementReplaced() 4");
        // XXX: If the renamed column is declared as primary key or foreign key
        // XXX: we need to rename the container's column names list to.
        Iterator<Key> keys = table.getKeysInternal().getActiveElements();
        while (keys.hasNext()) {
            Key key = keys.next();
            // XXX: Containers use lazy loading, updating
            // XXX: is only necessary if they have been loaded
            if (!key.isColumnsLoaded()) {
                System.out.println("ColumnListener.elementReplaced() lasy loaded Key: " + key.getName());
                continue;
            }
            List<String> columns = key.getColumnsInternal().getNamesInternal();
            System.out.println("ColumnListener.elementReplaced() 5 Key: " + key.getName() +
                               " - Columns: " + String.join(", ", columns));
            if (columns.contains(oldname)) {
                columns.set(columns.indexOf(oldname), newname);
                System.out.println("ColumnListener.elementReplaced() 6 Key Column: " + oldname +
                                   " renamed: " + newname);
            }
        }
        System.out.println("ColumnListener.elementReplaced() 7");
    }

}
