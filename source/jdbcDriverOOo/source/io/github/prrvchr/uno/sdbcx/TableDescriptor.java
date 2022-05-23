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
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class TableDescriptor
    extends Descriptor
    implements XColumnsSupplier,
               XKeysSupplier
{

    private static final String m_name = TableDescriptor.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.TableDescriptor",
                                                "com.sun.star.sdbcx.Descriptor"};
    private XNameAccess m_xColumns = null;
    private XIndexAccess m_xKeys = null;
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    protected String m_Description = "";
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_CatalogName", UnoHelper.getProperty("CatalogName", "string"));
        map.put("m_SchemaName", UnoHelper.getProperty("SchemaName", "string"));
        map.put("m_Description", UnoHelper.getProperty("Description", "string"));
        return map;
    }

    // The constructor method:
    public TableDescriptor(ConnectionBase connection)
    {
        super(m_name, m_services, connection, _getPropertySet());
        m_xColumns = new ColumnContainer(connection);
        m_xKeys = new KeyContainer(connection);
        System.out.println("sdbcx.TableDescriptor() ***************************************************");
    }


    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        System.out.println("sdbcx.TableDescriptor.getColumns() ***************************************************");
        return m_xColumns;
    }


    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        System.out.println("sdbcx.TableDescriptor.getKeys() ***************************************************");
        return m_xKeys;
    }


}
