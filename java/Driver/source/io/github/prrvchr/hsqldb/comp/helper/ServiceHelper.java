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

import java.util.ArrayList;

import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;


public abstract class ServiceHelper extends WeakBase
implements XComponent,
           XServiceInfo
{
	private final String m_implementationName;
	private final String[] m_serviceNames;
	private final ArrayList<XEventListener> m_xEventListeners = new ArrayList<>();

	// The constructor method:
	public ServiceHelper(String name,
                         String[] services)
	{
		m_implementationName = name;
		m_serviceNames = services;
	}


	// com.sun.star.uno.XComponent:
	@Override
	public void addEventListener(XEventListener listener)
	{
		m_xEventListeners.add(listener);
	}

	@Override
	public void dispose()
	{
		EventObject event = new EventObject(this);
		for (XEventListener listener : m_xEventListeners)
		{
			listener.disposing(event);
		}
	}

	@Override
	public void removeEventListener(XEventListener listener)
	{
		if (m_xEventListeners.contains(listener)) m_xEventListeners.remove(listener);
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String getImplementationName()
	{
		return m_implementationName;
	}

	@Override
	public String[] getSupportedServiceNames()
	{
		return m_serviceNames;
	}

	@Override
	public boolean supportsService(String service)
	{
		boolean support = false;
		int len = m_serviceNames.length;
		for (int i = 0; i < len; i++)
		{
			if (service.equals(m_serviceNames[i]))
			{
				support = true;
				break;
			}
		}
		return support;
	}


}
