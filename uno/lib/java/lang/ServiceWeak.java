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

import com.sun.star.lib.uno.helper.WeakBase;


public abstract class ServiceWeak
extends WeakBase
implements com.sun.star.lang.XServiceInfo
{
	public abstract String _getImplementationName();
	public abstract String[] _getServiceNames();


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
