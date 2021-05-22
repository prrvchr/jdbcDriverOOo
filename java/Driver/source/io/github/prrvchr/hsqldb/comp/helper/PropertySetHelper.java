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
package io.github.prrvchr.hsqldb.comp.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyChangeEvent;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertyChangeListener;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XVetoableChangeListener;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;


public class PropertySetHelper<T>
implements XPropertySet
{
	private final T m_component;
	private final Map<String, Property> m_properties;
	private final ArrayList<XPropertyChangeListener> m_xPropertyListeners = new ArrayList<>();
	private final ArrayList<XVetoableChangeListener> m_xVetoableListeners = new ArrayList<>();


	// The constructor method:
	public PropertySetHelper(T component, Map<String, Property> properties)
	{
		m_component = component;
		m_properties = properties;
	}


	
	// com.sun.star.beans.XPropertySet:
	@Override
	public void addPropertyChangeListener(String name, XPropertyChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		m_xPropertyListeners.add(listener);
	}

	@Override
	public void addVetoableChangeListener(String name, XVetoableChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		m_xVetoableListeners.add(listener);
	}

	@Override
	public XPropertySetInfo getPropertySetInfo()
	{
		return new PropertySetInfo(m_properties);
	}

	@Override
	public Object getPropertyValue(String name)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		Method method;
		String getter = "get" + name;
		try
		{
			method = m_component.getClass().getMethod(getter);
		}
		catch (SecurityException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
		catch (NoSuchMethodException e)
		{
			String msg = e.getMessage();
			throw new UnknownPropertyException(msg);
		}
		Object value;
		try
		{
			value = method.invoke(m_component);
		}
		catch (IllegalAccessException e)
		{
			String msg = e.getMessage();
			throw new UnknownPropertyException(msg);
		}
		catch (InvocationTargetException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
		return value;
	}

	@Override
	public void removePropertyChangeListener(String name, XPropertyChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		if (m_xPropertyListeners.contains(listener)) m_xPropertyListeners.remove(listener);
	}

	@Override
	public void removeVetoableChangeListener(String name, XVetoableChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		if (m_xVetoableListeners.contains(listener)) m_xVetoableListeners.remove(listener);
	}

	@Override
	public void setPropertyValue(String name, Object value)
	throws UnknownPropertyException,
	       PropertyVetoException,
	       IllegalArgumentException,
	       WrappedTargetException
	{
		if (!m_properties.containsKey(name)) throw new UnknownPropertyException();
		if (!m_xVetoableListeners.isEmpty())
		{
			PropertyChangeEvent event = new PropertyChangeEvent();
			event.PropertyName = name;
			event.Further = false;
			event.PropertyHandle = -1;
			event.OldValue = getPropertyValue(name);
			event.NewValue = value;
			for (XVetoableChangeListener listener : m_xVetoableListeners)
			{
				listener.vetoableChange(event);
			}
		}
		Method method;
		String setter = "set" + name;
		Type settertype = m_properties.get(name).Type;
		try 
		{
			method = m_component.getClass().getMethod(setter, settertype.getZClass());
		}
		catch (SecurityException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
		catch (NoSuchMethodException e)
		{
			String msg = e.getMessage();
			throw new UnknownPropertyException(msg);
		}
		try
		{
			method.invoke(m_component, value);
		}
		catch (java.lang.IllegalArgumentException e)
		{
			String msg = e.getMessage();
			throw new IllegalArgumentException(msg);
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
	}


	private class PropertySetInfo
	implements XPropertySetInfo
	{
	
		private final Map<String, Property> m_properties;
		
		// The constructor method:
		public PropertySetInfo(Map<String, Property> properties)
		{
			m_properties = properties;
		}
	
	
		// com.sun.star.beans.XPropertySetInfo:
		@Override
		public Property[] getProperties()
		{
			int len = m_properties.size();
			return m_properties.values().toArray(new Property[len]);
		}
	
		@Override
		public Property getPropertyByName(String name)
		throws UnknownPropertyException
		{
			if (!hasPropertyByName(name)) throw new UnknownPropertyException();
			return m_properties.get(name);
		}
	
		@Override
		public boolean hasPropertyByName(String name)
		{
			return m_properties.containsKey(name);
		}
	}


}
