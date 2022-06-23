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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdb.Connection;


public abstract class ContainerSuper<T extends Item>
    extends ContainerBase<T>
    implements XNameAccess

{

    protected final List<String> m_Names = new ArrayList<String>();

    // The constructor method:
    public ContainerSuper(String name,
                          String[] services,
                          Connection connection)
    {
        super(name, services, connection);
        System.out.println("sdbcx.ContainerSuper()");
    }


    // com.sun.star.container.XNameAccess:
    @Override
    public Object getByName(String name)
        throws NoSuchElementException, WrappedTargetException
    {
        if (hasByName(name)) {
            return m_Elements.get(m_Names.indexOf(name));
        }
        throw new NoSuchElementException();
    }

    @Override
    public String[] getElementNames()
    {
        return m_Names.toArray(new String[m_Names.size()]);
    }

    @Override
    public boolean hasByName(String name)
    {
        return m_Names.contains(name);
    }


    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException
    {
        System.out.println("sdbcx.Container.dropByName()");
        if (!m_Names.contains(name)) {
            throw new NoSuchElementException();
        }
        dropElement(m_Elements.get(m_Names.indexOf(name)));
    }


}
