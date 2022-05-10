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
import com.sun.star.container.XContainer;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;

import io.github.prrvchr.uno.container.NamedServiceProperty;
import io.github.prrvchr.uno.lang.ServiceWeak;


public class Container<T extends NamedServiceProperty>
    extends ServiceWeak
    implements XContainer,
               XEnumerationAccess,
               XIndexAccess,
               XNameAccess

{

    private static final String m_name = Container.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
    private final List<T> m_Elements;
    private final List<String> m_Names;
    private final Type m_type;

    // The constructor method:
    public Container()
    {
        this(new ArrayList<T>(), new ArrayList<String>());
    }
    public Container(List<T> elements,
                     List<String> names)
    {
        this(elements, names, "com.sun.star.uno.XInterface");
    }
    public Container(List<T> elements,
                     List<String> names,
                     String typename)
    {
        super(m_name, m_services);
        m_Elements = elements;
        m_Names = names;
        m_type = new Type(typename);
    }
    public Container(List<T> elements,
                     List<String> names,
                     String typename,
                     TypeClass typeclass)
    {
        super(m_name, m_services);
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
        return m_Elements.get(index);
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
        if (!hasByName(name)) throw new NoSuchElementException();
        return m_Elements.get(m_Names.indexOf(name));
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
            return m_Iterator.next();
        }
    }

    // com.sun.star.container.XContainer:
    @Override
    public void addContainerListener(XContainerListener arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeContainerListener(XContainerListener arg0) {
        // TODO Auto-generated method stub
    }


}
