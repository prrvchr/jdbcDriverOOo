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

import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.UnknownPropertyException;


interface XPropertySetInfo extends com.sun.star.beans.XPropertySetInfo
{

	Map<String, Property> _getProperties();


	// com.sun.star.beans.XPropertySetInfo:
	@Override
	default Property[] getProperties()
	{
		Map<String, Property> properties = _getProperties();
		int len = properties.size();
		return properties.values().toArray(new Property[len]);
	}

	@Override
	default Property getPropertyByName(String name)
	throws UnknownPropertyException
	{
		Map<String, Property> properties = _getProperties();
		if (!properties.containsKey(name)) throw new UnknownPropertyException();
		return properties.get(name);
	}

	@Override
	default boolean hasPropertyByName(String name)
	{
		Map<String, Property> properties = _getProperties();
		return properties.containsKey(name);
	}
}