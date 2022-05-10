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
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CheckOption;
import com.sun.star.sdbcx.XAlterView;

import io.github.prrvchr.uno.container.NamedServiceProperty;
import io.github.prrvchr.uno.helper.UnoHelper;


public class View
extends NamedServiceProperty
implements XAlterView
{
    private static final String m_name = View.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.View"};
    private String m_CatalogName = "";
    private String m_SchemaName = "";
    private final String m_Command;
    @SuppressWarnings("unused")
	private final int m_CheckOption;

    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_CatalogName", UnoHelper.getProperty("CatalogName", "string", readonly));
        map.put("m_SchemaName", UnoHelper.getProperty("SchemaName", "string", readonly));
        map.put("m_Command", UnoHelper.getProperty("Command", "string", readonly));
        map.put("m_CheckOption", UnoHelper.getProperty("CheckOption", "long", readonly));
        return map;
    }

    // The constructor method:
    public View(java.sql.Connection  connection,
                String catalog,
                String schema,
                String name)
    throws java.sql.SQLException
    {
        super(m_name, m_services, _getPropertySet(), name);
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Command = _getViewCommand(connection, name);
        m_CheckOption = CheckOption.NONE;
        System.out.println("View.View() Name: " + m_Name + " - Catalog: " + m_CatalogName + " - Schema: " + m_SchemaName + " - Command: " + m_Command);
    }

    private String _getViewCommand(java.sql.Connection connection,
                                   String view)
        throws java.sql.SQLException
    {
        String command = "";
        String sql = "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = ?;";
        java.sql.PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, view);
        java.sql.ResultSet result = statement.executeQuery();
        if (result.next()) {
            command = result.getString(1);
        }
        statement.close();
        return command;
    }

    // com.sun.star.sdbcx.XAlterView
    @Override
    public void alterCommand(String command)
        throws SQLException
    {
        System.out.println("View.alterCommand() : " + command);
        // TODO Auto-generated method stub
    }


}
