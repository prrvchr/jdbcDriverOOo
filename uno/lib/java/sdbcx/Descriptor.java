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

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySet;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.lang.ServiceInfo;


public abstract class Descriptor
    extends PropertySet
    implements XServiceInfo
{

    private final String m_service;
    private final String[] m_services;
    private String m_Name;
    private final boolean m_sensitive;

    // The constructor method:
    public Descriptor(String service,
                      String[] services,
                      boolean sensitive)
    {
        this(service, services, sensitive, "", false);
    }
    public Descriptor(String service,
                      String[] services,
                      boolean sensitive,
                      String name)
    {
        this(service, services, sensitive, name, true);
    }
    private Descriptor(String service,
                      String[] services,
                      boolean sensitive,
                      String name,
                      boolean readonly)
    {
        m_service = service;
        m_services = services;
        m_sensitive = sensitive;
        m_Name = name;
        registerProperties(readonly);
    }

    private void registerProperties(boolean readonly) {
        short attribute = readonly ? PropertyAttribute.READONLY : 0;
        registerProperty(PropertyIds.NAME.name, PropertyIds.NAME.id, Type.STRING, attribute,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Name;
                }
            },
            readonly ? null :
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Name = (String) value;
                }
            });
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // Method for internal use (no UNO method)
    public String getName() {
        return m_Name;
    }
    
    public void setName(String name) {
        m_Name = name;
    }
    
    public boolean isCaseSensitive() {
        return m_sensitive;
    }


}
