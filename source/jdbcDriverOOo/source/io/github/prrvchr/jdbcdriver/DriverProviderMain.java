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

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.uno.AnyConverter;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbcx.ColumnBase;

public abstract class DriverProviderMain
    implements DriverProvider
{

    static final String m_protocol = "xdbc:";
    private static String m_subprotocol;

    static final boolean m_warnings = true;
    private java.sql.Connection m_connection = null;
    private String m_url;
    private PropertyValue[] m_info;
    protected List<String> m_properties = List.of("user", "password");

    // The constructor method:
    public DriverProviderMain(String subprotocol)
    {
        System.out.println("jdbcdriver.DriverProviderMain() 1");
        m_subprotocol = subprotocol;
    }

    @Override
    public String getProtocol()
    {
        return m_protocol;
    }

    @Override
    public String getSubProtocol()
    {
        return m_protocol + m_subprotocol;
    }

    @Override
    public boolean isCaseSensitive(String clazz)
    {
        return true;
    }

    @Override
    public boolean isAutoRetrievingEnabled()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "IsAutoRetrievingEnabled", false);
    }

    @Override
    public String getAutoRetrievingStatement()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "AutoRetrievingStatement", "");
    }

    @Override
    public String[] getAlterViewQueries(String view,
                                        String command)
    {
        String[] queries = {String.format("ALTER VIEW %s AS %s", view, command)};
        return queries;
    }

    @Override
    public int getDataType(int type) {
        return type;
    }

    @Override
    public String[] getTableTypes()
    {
        return new String[]{"TABLE", "VIEW"};
    }

    @Override
    public String getTableType(String type)
    {
        return type;
    }

    @Override
    public String getViewQuery()
    {
        return "SELECT VIEW_DEFINITION, CHECK_OPTION FROM INFORMATION_SCHEMA.VIEWS WHERE ";
    }

    @Override
    public String getViewCommand(String sql)
    {
        return sql;
    }

    @Override
    public String getUserQuery()
    {
        return "SELECT USER_NAME FROM INFORMATION_SCHEMA.SYSTEM_USERS";
    }

    @Override
    public String getGroupQuery()
    {
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ADMINISTRABLE_ROLE_AUTHORIZATIONS;";
    }

    @Override
    public String getGroupUsersQuery()
    {
        return "SELECT GRANTEE FROM INFORMATION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS WHERE ROLE_NAME=?;";
    }

    @Override
    public String getUserGroupsQuery()
    {
        //TODO: We use recursion to find privileges inherited from roles,
        //TODO: we need to filter recursive entries (even ROLE_NAME and GRANTEE)
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS WHERE GRANTEE=? AND ROLE_NAME!=GRANTEE;";
    }


    @Override
    public int getPrivilege(String privilege)
    {
        int flag = 0;
        switch (privilege) {
        case "SELECT":
            flag = Privilege.SELECT;
            break;
        case "INSERT":
            flag = Privilege.INSERT;
            break;
        case "UPDATE":
            flag = Privilege.UPDATE;
            break;
        case "DELETE":
            flag = Privilege.DELETE;
            break;
        case "READ":
            flag = Privilege.READ;
            break;
        case "CREATE":
            flag = Privilege.CREATE;
            break;
        case "ALTER":
            flag = Privilege.ALTER;
            break;
        case "REFERENCES":
            flag = Privilege.REFERENCE;
            break;
        case "DROP":
            flag = Privilege.DROP;
            break;
        }
        return flag;
    }


    @Override
    public List<String> getPrivileges(int privilege)
    {
        List<String> flags = new ArrayList<>();
        if ((privilege & Privilege.SELECT) == Privilege.SELECT) {
            flags.add("SELECT");
        }
        if ((privilege & Privilege.INSERT) == Privilege.INSERT) {
            flags.add("INSERT");
        }
        if ((privilege & Privilege.UPDATE) == Privilege.UPDATE) {
            flags.add("UPDATE");
        }
        if ((privilege & Privilege.DELETE) == Privilege.DELETE) {
            flags.add("DELETE");
        }
        if ((privilege & Privilege.READ) == Privilege.READ) {
            flags.add("READ");
        }
        if ((privilege & Privilege.CREATE) == Privilege.CREATE) {
            flags.add("CREATE");
        }
        if ((privilege & Privilege.ALTER) == Privilege.ALTER) {
            flags.add("ALTER");
        }
        if ((privilege & Privilege.REFERENCE) == Privilege.REFERENCE) {
            flags.add("REFERENCES");
        }
        if ((privilege & Privilege.DROP) == Privilege.DROP) {
            flags.add("DROP");
        }
        return flags;
    }

    @Override
    public String getDropTableQuery()
    {
        return "DROP TABLE %s";
    }

    @Override
    public String getDropViewQuery()
    {
        return "DROP VIEW %s";
    }

    @Override
    public String getDropColumnQuery(ConnectionBase connection,
                                     ColumnBase column)
    {
        return null;
    }

    @Override
    public String getDropUserQuery(ConnectionBase connection,
                                   String user)
    {
        return null;
    }

    @Override
    public String getAutoIncrementCreation()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "AutoIncrementCreation", "");
    }

    @Override
    public String getRevokeTableOrViewPrivileges()
    {
        return "REVOKE %s ON %s FROM %s";
    }

    @Override
    public String getRevokeRoleQuery()
    {
        return "REVOKE %s FROM %s";
    }

    @Override
    public String getCreateTableQuery()
    {
        return "CREATE TABLE %s (%s)";
    }

    @Override
    public String getTableCommentQuery()
    {
        return "COMMENT ON %s IS '%s'";
    }

    @Override
    public String getColumnCommentQuery()
    {
        return "COMMENT ON %s IS '%s'";
    }

    @Override
    public boolean acceptsURL(final String url,
                              final PropertyValue[] info)
    {
        if (url.startsWith(getSubProtocol())) {
            registerURL(url, info);
            return true;
        }
        return false;
    }

    @Override
    public void registerURL(final String url,
                            final PropertyValue[] info)
    {
        m_url = url;
        m_info = info;
    }

    
    @Override
    public String getUrl()
    {
        return m_url;
    }

    @Override
    public PropertyValue[] getInfo()
    {
        return m_info;
    }

    @Override
    public boolean supportWarningsSupplier()
    {
        return m_warnings;
    }

    @Override
    public String getLoggingLevel(XHierarchicalNameAccess driver)
    {
        return "-1";
    }

    @Override
    public void setConnection(final String location,
                              final PropertyValue[] info,
                              String level)
        throws java.sql.SQLException
    {
        String url = getConnectionUrl(location, level);
        m_connection = DriverManager.getConnection(url, getConnectionProperties(m_properties, info));
    }

    @Override
    public java.sql.Connection getConnection()
    {
        return m_connection;
    }

    @Override
    public String getConnectionUrl(String location,
                                   String level)
    {
        return location;
    }

    @Override
    public Properties getConnectionProperties(List<String> list,
                                              PropertyValue[] info)
    {
        Properties properties = new Properties();
        System.out.println("DriverProviderMain.getConnectionProperties() 1");
        for (PropertyValue property : info) {
            Object value = property.Value;
            if (AnyConverter.isArray(value)) {
                Object[] objects = (Object[]) AnyConverter.toArray(value);
                System.out.println("DriverProviderMain.getConnectionProperties() 2 Name: " + property.Name + " - Value: " + Arrays.toString(objects));
            }
            else {
                System.out.println("DriverProviderMain.getConnectionProperties() 3 Name: " + property.Name + " - Value: " + value);
            }
            if (list.contains(property.Name)) {
                System.out.println("DriverProviderMain.getConnectionProperties() 4 Name: " + property.Name + " - Value: " + value);
                properties.setProperty(property.Name, AnyConverter.toString(value));
            }
        }
        return properties;
    }

    @Override
    public void setSystemProperties(String level)
        throws SQLException
    {
        // noop
    }

    @Override
    public DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new DatabaseMetaData(connection);
    }

    @Override
    public boolean isIgnoreCurrencyEnabled()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "IgnoreCurrency", false);
    }

    @Override
    public boolean supportCreateTableKeyParts()
    {
        return true;
    }

}