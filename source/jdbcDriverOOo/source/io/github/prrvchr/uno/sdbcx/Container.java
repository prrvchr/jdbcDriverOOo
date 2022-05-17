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
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XContainer;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.util.XRefreshListener;
import com.sun.star.util.XRefreshable;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceWeak;


public class Container<T extends ContainerElement>
    extends ServiceWeak
    implements XContainer,
               XEnumerationAccess,
               XIndexAccess,
               XNameAccess,
               XAppend,
               XDrop,
               XRefreshable,
               XDataDescriptorFactory

{

    private static final String m_name = Container.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    private final java.sql.Connection m_Connection;
    private final DriverProvider m_provider;
    private final List<T> m_Elements;
    private final List<String> m_Names;
    private final List<XContainerListener> m_Listeners = new ArrayList<XContainerListener>();
    private final Type m_type;

    // The constructor method:
    public Container(java.sql.Connection connection,
                     DriverProvider provider)
    {
        this(connection, provider, new ArrayList<T>(), new ArrayList<String>());
    }
    public Container(java.sql.Connection connection,
                     DriverProvider provider,
                     List<T> elements,
                     List<String> names)
    {
        this(connection, provider, elements, names, "com.sun.star.uno.XInterface");
    }
    public Container(java.sql.Connection connection,
                     DriverProvider provider,
                     List<T> elements,
                     List<String> names,
                     String typename)
    {
        super(m_name, m_services);
        m_Connection = connection;
        m_provider = provider;
        m_Elements = elements;
        m_Names = names;
        m_type = new Type(typename);
    }
    public Container(java.sql.Connection connection,
                     DriverProvider provider,
                     List<T> elements,
                     List<String> names,
                     String typename,
                     TypeClass typeclass)
    {
        super(m_name, m_services);
        m_Connection = connection;
        m_provider = provider;
        m_Elements = elements;
        m_Names = names;
        m_type = new Type(typename, typeclass);
    }

    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType()
    {
        return m_type;
    }

    @Override
    public boolean hasElements()
    {
        return !m_Elements.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException
    {
        if (index < getCount()) {
            return m_Elements.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getCount()
    {
        return m_Elements.size();
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

    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener listener) {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.addContainerListener() ****************************************");
        m_Listeners.add(listener);
    }

    @Override
    public void removeContainerListener(XContainerListener listener) {
        // TODO Auto-generated method stub
       System.out.println("sdbcx.Container.removeContainerListener() ****************************************");
       if (m_Listeners.contains(listener)) {
           m_Listeners.remove(listener);
       }
    }

    // com.sun.star.sdbcx.XDrop:
    @Override
    public void dropByIndex(int index)
        throws SQLException, IndexOutOfBoundsException
    {
        System.out.println("sdbcx.Container.dropByIndex()");
        if (index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        _dropElement(m_Elements.get(index), m_Names.get(index));
    }

    @Override
    public void dropByName(String name)
        throws SQLException, NoSuchElementException
    {
        System.out.println("sdbcx.Container.dropByName()");
        if (!m_Names.contains(name)) {
            throw new NoSuchElementException();
        }
        int index = m_Names.indexOf(name);
        _dropElement(m_Elements.get(index), name);
    }

    private void _dropElement(T element,
                              String name)
        throws SQLException
    {
        System.out.println("sdbcx.Container._dropElement() 1 Element: " + element.getClass().getSimpleName());
        String query = element.getDropQuery(m_provider);
        System.out.println("sdbcx.Container._dropElement() 2 Query: " + query);
        if (query != null) {
            try {
                java.sql.Statement statement = m_Connection.createStatement();
                statement.execute(query);
                statement.close();
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
            _elementRemoved(element);
        }
    }

    private void _elementRemoved(T element)
    {
        ContainerEvent event = new ContainerEvent();
        event.Source = this;
        event.Accessor = this;
        event.Element = element;
        event.ReplacedElement = element;
        for (XContainerListener listener : m_Listeners) {
            listener.elementRemoved(event);
        }
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet properties)
        throws SQLException,
        ElementExistException
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.appendByDescriptor() ****************************************");
    }

    // com.sun.star.util.XRefreshable
    @Override
    public void addRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.addRefreshListener() ****************************************");
    }
    @Override
    public void refresh()
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.refresh() ****************************************");
    }
    @Override
    public void removeRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.removeRefreshListener() ****************************************");
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.Container.createDataDescriptor() ****************************************");
        return null;
    }


}
