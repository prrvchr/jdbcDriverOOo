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
package io.github.prrvchr.jdbcdriver;

import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public interface DriverProvider
{

    static String m_protocol = "xdbc:";

    default String getProtocol()
    {
        return m_protocol;
    }
    default String getProtocol(String subprotocol)
    {
        return m_protocol + subprotocol;
    }

    default String[] getTableTypes()
    {
        String[] types = {"TABLE", "VIEW", "ALIAS", "SYNONYM"};
        return types;
    }

    default String getTableType(String type)
    {
        return type;
    }

    default String getViewQuery()
    {
        return "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?;";
    }

    default String getUserQuery()
    {
        return null;
    }

    default String getDropQuery(String element,
                                String name)
    {
        return null;
    }

    default String getDropQuery(String element,
                                String catalog,
                                String schema,
                                String name)
    {
        return null;
    }

    public boolean acceptsURL(String url);

    public boolean supportWarningsSupplier();

    default String getLoggingLevel(XHierarchicalNameAccess driver) {
        return "-1";
    };

    public java.sql.Connection getConnection(String url,
                                             PropertyValue[] info,
                                             String level)
        throws java.sql.SQLException;

    default Properties getConnectionProperties(List<String> list,
                                               PropertyValue[] info)
    {
        Properties properties = new Properties();
        System.out.println("DriverProvider.getConnectionProperties() 1");
        for (PropertyValue property : info) {
            System.out.println("DriverProvider.getConnectionProperties() 2  Name: " + property.Name + " - Value: " + property.Value);
            if (list.contains(property.Name))
            {
                System.out.println("DriverProvider.getConnectionProperties() 3 Name: " + property.Name + " - Value: " + property.Value);
                properties.setProperty(property.Name, AnyConverter.toString(property.Value));
            }
        }
        return properties;
    }

    default void setSystemProperties(String level)
        throws SQLException
    {
        // noop
    }


    public DatabaseMetaDataBase getDatabaseMetaData(XComponentContext context,
                                                    ConnectionBase connection)
        throws java.sql.SQLException;


    public ResultSetBase getResultSet(XComponentContext context,
                                      ConnectionBase connection, 
                                      java.sql.ResultSet resultset)
        throws java.sql.SQLException;

    public ResultSetBase getResultSet(XComponentContext context,
                                      ConnectionBase connection, 
                                      StatementMain statement,
                                      java.sql.ResultSet resultset)
        throws java.sql.SQLException;


}
