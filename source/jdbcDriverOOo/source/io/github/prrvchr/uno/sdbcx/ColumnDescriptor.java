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

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class ColumnDescriptor
    extends Descriptor
{

    private static final String m_name = ColumnDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.ColumnDescriptor",
                                                "com.sun.star.sdbcx.Descriptor"};
    protected int m_Type;
    protected String m_TypeName = "";
    protected int m_Precision;
    protected int m_Scale;
    protected int m_IsNullable;
    protected boolean m_IsAutoIncrement;
    protected boolean m_IsRowVersion;
    protected String m_Description = "";
    protected String m_DefaultValue = "";
    protected String m_AutoIncrementCreation = "";
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Type", UnoHelper.getProperty("Type", "long"));
        map.put("m_TypeName", UnoHelper.getProperty("TypeName", "string"));
        map.put("m_Precision", UnoHelper.getProperty("Precision", "long"));
        map.put("m_Scale", UnoHelper.getProperty("Scale", "long"));
        map.put("m_IsNullable", UnoHelper.getProperty("IsNullable", "long"));
        map.put("m_IsAutoIncrement", UnoHelper.getProperty("IsAutoIncrement", "boolean"));
        map.put("m_IsRowVersion", UnoHelper.getProperty("IsRowVersion", "boolean"));
        map.put("m_Description", UnoHelper.getProperty("Description", "string"));
        map.put("m_DefaultValue", UnoHelper.getProperty("DefaultValue", "string"));
        map.put("m_AutoIncrementCreation", UnoHelper.getProperty("AutoIncrementCreation", "string"));
        return map;
    }

    // The constructor method:
    public ColumnDescriptor(ConnectionBase connection)
    {
        super(m_name, m_services, connection, _getPropertySet());
        System.out.println("sdbcx.ColumnDescriptor() ***************************************************");
    }


}
