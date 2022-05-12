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

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNamed;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceProperty;


public abstract class ContainerElement
    extends ServiceProperty
    implements XNamed
{

    protected String m_Name;
    private final boolean m_ReadOnly;
    private static Map<String, Property> _getPropertySet(boolean readonly)
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Name", UnoHelper.getProperty("Name", "string", readonly ? PropertyAttribute.READONLY : 0));
        return map;
    }
    private static Map<String, Property> _getPropertySet(Map<String, Property> properties, boolean readonly)
    {
        Map<String, Property> map = _getPropertySet(readonly);
        map.putAll(properties);
        return map;
    }

    // The constructor method:
    public ContainerElement(String service,
                                String[] services,
                                String name)
    {
        super(service, services, _getPropertySet(true));
        m_Name = name;
        m_ReadOnly = true;
    }
    public ContainerElement(String service,
                                String[] services,
                                Map<String, Property> properties,
                                String name)
    {
        this(service, services, properties, name, true);
    }
    public ContainerElement(String service,
                                String[] services,
                                Map<String, Property> properties,
                                String name,
                                boolean readonly)
    {
        super(service, services, _getPropertySet(properties, readonly));
        m_Name = name;
        m_ReadOnly = readonly;
    }

    // com.sun.star.container.XNamed:
    @Override
    public String getName()
    {
        return m_Name;
    }

    @Override
    public void setName(String name)
    {
        if (!m_ReadOnly)
            m_Name = name;
    }

    public String getDropQuery(DriverProvider provider)
    {
        String type = this.getClass().getSimpleName();
        System.out.println("sdbcx.ContainerElement.getDropQuery() Type: " + type + " - Name: " + m_Name);
        return provider.getDropQuery(type, m_Name);
    }


}
