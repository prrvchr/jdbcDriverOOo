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


import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.SharedResources;


public abstract class DescriptorContainer<T extends Descriptor>
    extends ContainerBase<T> {
    private static final String SERVICE = DescriptorContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    public DescriptorContainer(Object lock,
                               boolean sensitive) {
        super(SERVICE, SERVICES, lock, sensitive);
    }

    @Override
    protected T createElement(String name)
        throws java.sql.SQLException {
        // This should never be called. DescriptorContainer always starts off empty,
        // and only grows as a result of appending.
        String error = SharedResources.getInstance().getResource(Resources.STR_ERRORMSG_SEQUENCE);
        throw new java.sql.SQLException(error, StandardSQLState.SQL_FUNCTION_SEQUENCE_ERROR.text());
    }
    
    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws java.sql.SQLException {
    }
    
    @Override
    protected void refreshInternal() {
    }

}
