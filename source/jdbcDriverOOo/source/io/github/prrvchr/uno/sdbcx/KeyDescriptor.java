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
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class KeyDescriptor
    extends Descriptor
    implements XColumnsSupplier
{

    private static final String m_name = KeyDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.KeyDescriptor",
                                                "com.sun.star.sdbcx.Descriptor"};
    private XNameAccess m_xColumns = null;
    protected int m_Type;
    protected String m_ReferencedTable = "";
    protected int m_UpdateRule;
    protected int m_DeleteRule;
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Type", UnoHelper.getProperty("Type", "long"));
        map.put("m_ReferencedTable", UnoHelper.getProperty("ReferencedTable", "string"));
        map.put("m_UpdateRule", UnoHelper.getProperty("UpdateRule", "long"));
        map.put("m_DeleteRule", UnoHelper.getProperty("DeleteRule", "long"));
        return map;
    }

    // The constructor method:
    public KeyDescriptor(ConnectionBase connection,
                         TableBase table)
    {
        super(m_name, m_services, connection, _getPropertySet());
        m_xColumns = new ColumnContainer(connection, table);
        System.out.println("sdbcx.KeyDescriptor() ***************************************************");
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdbcx.TableDescriptor.getColumns() ***************************************************");
        return m_xColumns;
    }


}
