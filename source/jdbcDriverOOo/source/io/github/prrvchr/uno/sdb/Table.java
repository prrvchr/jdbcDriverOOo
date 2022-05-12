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
package io.github.prrvchr.uno.sdb;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.TableBase;


public final class Table
    extends TableBase
{

    private static final String m_name = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Table",
                                                "com.sun.star.sdbcx.Table"};
    @SuppressWarnings("unused")
	private final int m_Privileges;
    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_Privileges", UnoHelper.getProperty("Privileges", "long", readonly));
        return map;
    }

    // The constructor method:
    public Table(DriverProvider provider,
                 java.sql.DatabaseMetaData metadata,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String description)
        throws java.sql.SQLException
    {
        super(m_name, m_services, provider, metadata, catalog, schema, name, type, description, _getPropertySet());
        m_Privileges = _getPrivileges(metadata);
        System.out.println("sdb.Table.Table() : 1" );
    }
    public Table(java.sql.Connection connection,
                 DriverProvider provider,
                 schemacrawler.schema.Table table,
                 String catalog,
                 String name)
        throws SQLException
    {
        super(m_name, m_services, connection, provider, table, catalog, name, _getPropertySet());
        m_Privileges = _getPrivileges(table);
        System.out.println("sdb.Table.Table() : 1" );
    }

    private int _getPrivileges(java.sql.DatabaseMetaData metadata)
        throws SQLException
    {
        int value = 0;
        System.out.println("sdb.Table._getPrivileges() : 1 Catalog: " + m_CatalogName + " - Schema: " + m_SchemaName + " - Table: " + m_Name);
        java.sql.ResultSet result = metadata.getTablePrivileges(m_CatalogName, m_SchemaName, m_Name);
        while (result != null && result.next()) {
            System.out.println("sdb.Table._getPrivileges() : 2 " + result.getString(6));
            value += UnoHelper.getConstantValue(Privilege.class, result.getString(6));
            System.out.println("sdb.Table._getPrivileges() : 3 " + value);
        }
        result.close();
        value = 265;
        System.out.println("sdb.Table._getPrivileges() : 4 " + value);
        return value;
    }
    private static int _getPrivileges(schemacrawler.schema.Table table)
        throws SQLException
    {
        int value = 265;
        System.out.println("sdb.Table._getPrivileges() : 1");
        Collection<schemacrawler.schema.Privilege<schemacrawler.schema.Table>> privileges = table.getPrivileges();
        System.out.println("sdb.Table._getPrivileges() : 2 : " + privileges.size());
        for (schemacrawler.schema.Privilege<schemacrawler.schema.Table> privilege : privileges)
        {
            System.out.println("sdb.Table._getPrivileges() : 3 " + privilege.getName());
            value = UnoHelper.getConstantValue(Privilege.class, privilege.getName());
            System.out.println("sdb.Table._getPrivileges() : 4 " + value);
            break;
        }
        return value;
    }


}
