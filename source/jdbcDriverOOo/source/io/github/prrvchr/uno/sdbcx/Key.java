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

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.uno.beans.PropertySet;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class Key
    extends PropertySet
    implements XServiceInfo,
               XColumnsSupplier
{

    private static final String m_name = Key.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Key"};
    private XNameAccess m_xColumns = null;
    @SuppressWarnings("unused")
    private final String m_Name;
    @SuppressWarnings("unused")
    private final int m_Type;
    @SuppressWarnings("unused")
    private final String m_ReferencedTable;
    @SuppressWarnings("unused")
    private final int m_UpdateRule;
    @SuppressWarnings("unused")
    private final int m_DeleteRule;
    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Name", UnoHelper.getProperty("Name", "string", readonly));
        map.put("m_Type", UnoHelper.getProperty("Type", "long", readonly));
        map.put("m_ReferencedTable", UnoHelper.getProperty("ReferencedTable", "string", readonly));
        map.put("m_UpdateRule", UnoHelper.getProperty("UpdateRule", "long", readonly));
        map.put("m_DeleteRule", UnoHelper.getProperty("DeleteRule", "long", readonly));
        return map;
    }

    // The constructor method:
    public Key(ConnectionBase connection,
               String catalog,
               String schema,
               String table,
               String column,
               String name,
               int type,
               String reference,
               int update,
               int delete)
        throws SQLException
    {
        super(_getPropertySet());
        m_Name = name;
        m_Type = type;
        m_ReferencedTable = reference;
        m_UpdateRule = update;
        m_DeleteRule = delete;
        m_xColumns = new ColumnContainer(connection, catalog, schema, table, column);
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_name);
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


    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns()
    {
        return m_xColumns;
    }


}
