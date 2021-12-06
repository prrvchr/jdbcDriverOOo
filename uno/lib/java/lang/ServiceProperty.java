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
package io.github.prrvchr.ooo.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.star.beans.Property;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.PropertySet;
import com.sun.star.uno.Type;


public abstract class ServiceProperty
extends PropertySet
implements com.sun.star.lang.XServiceInfo
{
	public abstract String _getImplementationName();
	public abstract String[] _getServiceNames();


	public ServiceProperty(Map<String, Property> properties)
	{
		for (Entry <String, Property> map : properties.entrySet())
		{
			registerProperty(map.getValue(), map.getKey());
		}
	}


	// com.sun.star.lib.uno.helper.PropertySet:
	@Override
	public boolean convertPropertyValue(Property property,
                                        Object[] newValue,
                                        Object[] oldValue,
                                        Object value)
	throws com.sun.star.lang.IllegalArgumentException,
           com.sun.star.lang.WrappedTargetException
	{
		newValue  = new Object[] {value};
		return true;
	}

	@Override
	public void setPropertyValueNoBroadcast(Property property,
                                            Object value)
     throws com.sun.star.lang.WrappedTargetException
	{
		Method method;
		String setter = "set" + property.Name;
		Type type = property.Type;
		try 
		{
			method = this.getClass().getMethod(setter, type.getZClass());
		}
		catch (SecurityException | NoSuchMethodException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
		try
		{
			method.invoke(this, value);
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

	@Override
	public Object getPropertyValue(Property property)
	{
		Method method = null;
		String getter = "get" + property.Name;
		try {
			method = this.getClass().getMethod(getter);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
		Object value = null;
		try
		{
			value = method.invoke(this);
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return value;
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String getImplementationName()
	{
		String name = _getImplementationName();
		return ServiceInfo.getImplementationName(name);
	}

	@Override
	public String[] getSupportedServiceNames()
	{
		String[] services = _getServiceNames();
		return ServiceInfo.getSupportedServiceNames(services);
	}

	@Override
	public boolean supportsService(String service)
	{
		String[] services = _getServiceNames();
		return ServiceInfo.supportsService(services, service);
	}


}
