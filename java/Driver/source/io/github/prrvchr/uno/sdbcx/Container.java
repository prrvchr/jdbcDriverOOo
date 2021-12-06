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
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XChild;
import com.sun.star.container.XContainer;
import com.sun.star.container.XContainerListener;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.NoSupportException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XDrop;
import com.sun.star.uno.Type;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XRefreshListener;
import com.sun.star.util.XRefreshable;

import io.github.prrvchr.uno.lang.ServiceWeak;


public class Container<T>
extends ServiceWeak
implements XAppend,
           XChild,
           XColumnLocate,
           XContainer,
           XDataDescriptorFactory,
           XDrop,
           XEnumerationAccess,
           XIndexAccess,
           XNameAccess,
           XRefreshable,
           XServiceInfo
{
	private static final String m_name = Columns.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbcx.Container"};
	private XInterface m_Component = null;
	private final List<String> m_Index;
	private final Map<String, T> m_Map;
	private final List<XContainerListener> m_Listeners = new ArrayList<XContainerListener>();
	private String m_type = "com.sun.star.beans.XPropertySet";

	// The constructor method:
	public Container(List<String> index,
                     Map<String, T> map)
	{
		m_Index = index;
		m_Map = map;
	}
	public Container(XInterface component,
                     List<String> index,
                     Map<String, T> map)
	{
		m_Component = component;
		m_Index = index;
		m_Map = map;
	}
	public Container(List<String> index,
                     Map<String, T> map,
                     String type)
	{
		m_Index = index;
		m_Map = map;
		m_type = type;
	}


	// com.sun.star.sdbc.XColumnLocate:
	@Override
	public int findColumn(String name)
	throws SQLException
	{
		return m_Index.indexOf(name) +1;
	}


	// com.sun.star.container.XContainer:
	@Override
	public void addContainerListener(XContainerListener listener)
	{
		m_Listeners.add(listener);
	}
	@Override
	public void removeContainerListener(XContainerListener listener)
	{
		if (m_Listeners.contains(listener)) m_Listeners.remove(listener);
	}


	// com.sun.star.container.XElementAccess:
	@Override
	public Type getElementType()
	{
		return new Type(m_type);
	}


	@Override
	public boolean hasElements()
	{
		return !m_Index.isEmpty();
	}


	// com.sun.star.sdbcx.XDrop:
	@Override
	public void dropByIndex(int arg0) throws SQLException, IndexOutOfBoundsException {
		// TODO Auto-generated method stub
	}


	@Override
	public void dropByName(String arg0) throws SQLException, NoSuchElementException {
		// TODO Auto-generated method stub
	}


	// com.sun.star.sdbcx.XAppend:
	@Override
	public void appendByDescriptor(XPropertySet arg0) throws SQLException, ElementExistException {
		// TODO Auto-generated method stub
	}


	// com.sun.star.sdbcx.XDataDescriptorFactory:
	@Override
	public XPropertySet createDataDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}


	// com.sun.star.util.XRefreshable:
	@Override
	public void addRefreshListener(XRefreshListener arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeRefreshListener(XRefreshListener arg0) {
		// TODO Auto-generated method stub
		
	}


	// com.sun.star.container.XIndexAccess:
	@Override
	public Object getByIndex(int index)
	throws IndexOutOfBoundsException, WrappedTargetException
	{
		String key = m_Index.get(index);
		return m_Map.get(key);
	}


	@Override
	public int getCount()
	{
		return m_Index.size();
	}


	// com.sun.star.container.XNameAccess:
	@Override
	public Object getByName(String name)
	throws NoSuchElementException, WrappedTargetException
	{
		if (!hasByName(name)) throw new NoSuchElementException();
		return m_Map.get(name);
	}


	@Override
	public String[] getElementNames()
	{
		return m_Index.toArray(new String[m_Index.size()]);
	}


	@Override
	public boolean hasByName(String name)
	{
		return m_Index.contains(name);
	}


	// com.sun.star.container.XEnumerationAccess:
	@Override
	public XEnumeration createEnumeration()
	{
		return new Enumeration(m_Index.iterator());
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String _getImplementationName()
	{
		return m_name;
	}
	@Override
	public String[] _getServiceNames()
	{
		return m_services;
	}


	private class Enumeration
	extends WeakBase
	implements XEnumeration
	{
		private final java.util.Iterator<String> m_Iterator;

		public Enumeration(java.util.Iterator<String> iterator)
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
			String key = m_Iterator.next();
			return m_Map.get(key);
		}


	}


	// com.sun.star.container.XChild:
	@Override
	public Object getParent()
	{
		return m_Component;
	}
	@Override
	public void setParent(Object arg0)
	throws NoSupportException
	{
		// TODO Auto-generated method stub
	}


}
