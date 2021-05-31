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
package io.github.prrvchr.comp.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyChangeEvent;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertyChangeListener;
import com.sun.star.beans.XVetoableChangeListener;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.comp.beans.PropertySetInfo;

public interface XPropertySet<T>
extends com.sun.star.beans.XPropertySet
{
	T _getComponent();
	Map<String, Property> _getProperties();
	ArrayList<XPropertyChangeListener> _getPropertyListeners();
	ArrayList<XVetoableChangeListener> _getVetoableListeners();


	// com.sun.star.beans.XPropertySet:
	@Override
	default void addPropertyChangeListener(String name, XPropertyChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		ArrayList<XPropertyChangeListener> listeners = _getPropertyListeners();
		if (!_getProperties().containsKey(name)) throw new UnknownPropertyException();
		listeners.add(listener);
	}

	@Override
	default void addVetoableChangeListener(String name, XVetoableChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		ArrayList<XVetoableChangeListener> listeners = _getVetoableListeners();
		if (!_getProperties().containsKey(name)) throw new UnknownPropertyException();
		listeners.add(listener);
	}

	@Override
	default XPropertySetInfo getPropertySetInfo()
	{
		Map<String, Property> properties = _getProperties();
		return new PropertySetInfo(properties);
	}

	@Override
	default Object getPropertyValue(String name)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		Map<String, Property> properties = _getProperties();
		if (!properties.containsKey(name)) throw new UnknownPropertyException();
		T component = _getComponent();
		Method method;
		String getter = "get" + name;
		try
		{
			method = component.getClass().getMethod(getter);
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
			value = method.invoke(component);
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
	default void removePropertyChangeListener(String name, XPropertyChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		Map<String, Property> properties = _getProperties();
		if (!properties.containsKey(name)) throw new UnknownPropertyException();
		ArrayList<XPropertyChangeListener> listeners = _getPropertyListeners();
		if (listeners.contains(listener)) listeners.remove(listener);
	}

	@Override
	default void removeVetoableChangeListener(String name, XVetoableChangeListener listener)
	throws UnknownPropertyException,
	       WrappedTargetException
	{
		Map<String, Property> properties = _getProperties();
		if (!properties.containsKey(name)) throw new UnknownPropertyException();
		ArrayList<XVetoableChangeListener> listeners = _getVetoableListeners();
		if (listeners.contains(listener)) listeners.remove(listener);
	}

	@Override
	default void setPropertyValue(String name, Object value)
	throws UnknownPropertyException,
	       PropertyVetoException,
	       IllegalArgumentException,
	       WrappedTargetException
	{
		Map<String, Property> properties = _getProperties();
		if (!properties.containsKey(name)) throw new UnknownPropertyException();
		Property property = properties.get(name);
		short readonly = PropertyAttribute.READONLY;
		System.out.println("XPropertySet.setPropertyValue() 1 " + (property.Attributes & readonly));
		boolean isreadonly = (property.Attributes & readonly) == readonly;
		if (isreadonly)
		{
			System.out.println("XPropertySet.setPropertyValue() 2 " + isreadonly);
			throw new IllegalArgumentException();
		}
		ArrayList<XVetoableChangeListener> listeners = _getVetoableListeners();
		if (!listeners.isEmpty())
		{
			PropertyChangeEvent event = new PropertyChangeEvent();
			event.PropertyName = name;
			event.Further = false;
			event.PropertyHandle = -1;
			event.OldValue = getPropertyValue(name);
			event.NewValue = value;
			for (XVetoableChangeListener listener : listeners)
			{
				listener.vetoableChange(event);
			}
		}
		T component = _getComponent();
		Method method;
		String setter = "set" + name;
		Type type = property.Type;
		try 
		{
			method = component.getClass().getMethod(setter, type.getZClass());
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
			method.invoke(component, value);
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


}
