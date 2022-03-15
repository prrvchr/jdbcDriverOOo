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

package io.github.prrvchr.uno.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.star.beans.Property;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;


public abstract class PropertySet
extends com.sun.star.lib.uno.helper.PropertySet
{
	public PropertySet(Map<String, Property> properties)
	{
		super();
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
		oldValue[0] = getPropertyValue(property);
		Class<?> clazz = property.Type.getZClass();
		boolean voidvalue = false;
		boolean anyvalue = value instanceof Any;
		if (anyvalue)
			voidvalue = ((Any) value).getObject() == null;
		else
			voidvalue = value == null;
		if (voidvalue && clazz.isPrimitive())
			throw new IllegalArgumentException("The implementation does not support the MAYBEVOID attribute for this property");
		Object converted = null;
		if (clazz.equals(Any.class))
		{
			if (anyvalue)
				converted = value;
			else
			{
				if (value instanceof XInterface)
				{
					XInterface xInt = UnoRuntime.queryInterface(XInterface.class, value);
					if (xInt != null)
						converted = new Any(new Type(XInterface.class), xInt);
				}
				else if (value == null)
				{
					if (oldValue[0] == null)
						converted = new Any(new Type(), null);
					else
						converted = new Any(((Any)oldValue[0]).getType(), null);
				}
				else
					converted = new Any(new Type(value.getClass()), value);
			}
		}
		else
			converted = convert(clazz, value);
		newValue[0] = converted;
		return true;
	}

	private static Object convert(Class<?> clazz, Object object)
	throws IllegalArgumentException
	{
		Object value = null;
		if (object == null || (object instanceof Any && ((Any) object).getObject() == null))
			value = null;
		else if (clazz.equals(Object.class))
		{
			if (object instanceof Any)
				object = ((Any) object).getObject();
			value = object;
		}
		else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class))
			value = Boolean.valueOf(AnyConverter.toBoolean(object));
		else if (clazz.equals(char.class) || clazz.equals(Character.class))
			value = Character.valueOf(AnyConverter.toChar(object));
		else if (clazz.equals(byte.class) || clazz.equals(Byte.class))
			value = Byte.valueOf(AnyConverter.toByte(object));
		else if (clazz.equals(short.class) || clazz.equals(Short.class))
			value = Short.valueOf(AnyConverter.toShort(object));
		else if (clazz.equals(int.class) || clazz.equals(Integer.class))
			value = Integer.valueOf(AnyConverter.toInt(object));
		else if (clazz.equals(long.class) || clazz.equals(Long.class))
			value = Long.valueOf(AnyConverter.toLong(object));
		else if (clazz.equals(float.class) || clazz.equals(Float.class))
			value = Float.valueOf(AnyConverter.toFloat(object));
		else if (clazz.equals(double.class) || clazz.equals(Double.class))
			value = Double.valueOf(AnyConverter.toDouble(object));
		else if (clazz.equals(String.class))
			value = AnyConverter.toString(object);
		else if (clazz.isArray())
			value = AnyConverter.toArray(object);
		else if (clazz.equals(Type.class))
			value = AnyConverter.toType(object);
		else if (XInterface.class.isAssignableFrom(clazz))
			value = AnyConverter.toObject(new Type(clazz), object);
		else if (com.sun.star.uno.Enum.class.isAssignableFrom(clazz))
			value = AnyConverter.toObject(new Type(clazz), object);
		else
			throw new IllegalArgumentException("Could not convert the argument");
		return value;
	}


	@Override
	public void setPropertyValueNoBroadcast(Property property,
											Object value)
	throws WrappedTargetException
	{
		Method method = null;
		String setter = "set" + (String) getPropertyId(property);
		try 
		{
			method = this.getClass().getMethod(setter, property.Type.getZClass());
		}
		catch (SecurityException | NoSuchMethodException e)
		{
			String msg = e.getMessage();
			throw new WrappedTargetException(msg);
		}
		try
		{
			if (method != null)
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
		String getter = "get" + (String) getPropertyId(property);
		try
		{
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


}
