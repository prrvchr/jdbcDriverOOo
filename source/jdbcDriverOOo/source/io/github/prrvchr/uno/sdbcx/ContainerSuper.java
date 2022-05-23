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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public abstract class ContainerSuper<T extends Item>
    extends ContainerBase<T>
    implements XServiceInfo,
               XEnumerationAccess,
               XNameAccess

{

    private static final String m_name = ContainerSuper.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    protected final List<String> m_Names = new ArrayList<String>();

    // The constructor method:
    public ContainerSuper(ConnectionBase connection)
    {
        super(connection);
        System.out.println("sdbcx.ContainerSuper()");
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_name);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
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


    // com.sun.star.container.XEnumerationAccess:
    @Override
    public XEnumeration createEnumeration()
    {
        return new Enumeration(m_Elements.iterator());
    }

    private class Enumeration
        extends WeakBase
        implements XEnumeration
    {
        private final java.util.Iterator<T> m_Iterator;

        public Enumeration(java.util.Iterator<T> iterator)
        {
            m_Iterator = iterator;
        }

        @Override
        public boolean hasMoreElements()
        {
            return m_Iterator.hasNext();
        }

        @Override
        public Object nextElement()
            throws NoSuchElementException, WrappedTargetException
        {
            if (m_Iterator.hasNext()) {
                return m_Iterator.next();
            }
            throw new NoSuchElementException();
        }
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
        int index = m_Names.indexOf(name);
        dropElement(m_Elements.get(index));
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public abstract void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException;

    // com.sun.star.util.XRefreshable
    @Override
    public abstract void refresh();
    @Override
    public void addRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.addRefreshListener() ****************************************");
    }
    @Override
    public void removeRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.removeRefreshListener() ****************************************");
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


}
