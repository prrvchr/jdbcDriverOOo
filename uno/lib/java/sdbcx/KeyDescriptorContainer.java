/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.helper.DataBaseTools;


public class KeyDescriptorContainer
    extends DescriptorContainer
{

    // The constructor method:
    public KeyDescriptorContainer(Object lock,
                                  boolean sensitive)
    {
        super(lock, sensitive);
        System.out.println("sdbcx.descriptors.KeyDescriptorContainer()");
    }

    @Override
    protected XPropertySet _createDescriptor() {
        System.out.println("sdbcx.descriptors.KeyDescriptorContainer._createDescriptor() 1");
        return new KeyDescriptor(isCaseSensitive());
    }

    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        System.out.println("sdbcx.descriptors.KeyDescriptorContainer._appendElement() 1");
        XPropertySet newDescriptor = _cloneDescriptor(descriptor);
        DataBaseTools.cloneDescriptorColumns(descriptor, newDescriptor);
        System.out.println("sdbcx.descriptors.KeyDescriptorContainer._appendElement() 2");
        return newDescriptor;
    }


}
