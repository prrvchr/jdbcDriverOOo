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
package io.github.prrvchr.comp.lang;

import com.sun.star.lib.uno.helper.ComponentBase;


public abstract class ServiceComponent
extends ComponentBase
implements com.sun.star.lang.XServiceInfo
{
	private ServiceInfo m_ServiceInfo;
	public abstract String _getImplementationName();
	public abstract String[] _getServiceNames();

	// The constructor method:
	public ServiceComponent()
	{
		m_ServiceInfo = new ServiceInfo(_getImplementationName(), _getServiceNames());
	}


	// com.sun.star.lang.XServiceInfo:
	@Override
	public String getImplementationName()
	{
		return m_ServiceInfo.getImplementationName();
	}

	@Override
	public String[] getSupportedServiceNames()
	{
		return m_ServiceInfo.getSupportedServiceNames();
	}

	@Override
	public boolean supportsService(String service)
	{
		return m_ServiceInfo.supportsService(service);
	}


}
