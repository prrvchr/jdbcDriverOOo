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
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdb.ResultSet;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.Column;

public abstract class DriverProviderMain
    implements DriverProvider
{

    static final String m_protocol = "xdbc:";
    static final boolean m_warnings = true;
    protected List<String> m_properties = List.of("user", "password");

    // The constructor method:
    public DriverProviderMain()
    {
        System.out.println("jdbcdriver.DriverProviderMain() 1");
    }

    @Override
    public String getProtocol()
    {
        return m_protocol;
    }

    @Override
    public String getProtocol(String subprotocol)
    {
        return m_protocol + subprotocol;
    }

    @Override
    public String[] getTableTypes()
    {
        String[] types = {"TABLE", "VIEW", "ALIAS", "SYNONYM"};
        return types;
    }

    @Override
    public String getTableType(String type)
    {
        return type;
    }

    @Override
    public String getViewQuery()
    {
        return "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?;";
    }

    @Override
    public String getUserQuery()
    {
        return null;
    }

    @Override
    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table)
    {
        String query = "DROP TABLE \"%s\".\"%s\"";
        return String.format(query, schema, table);
    }

    @Override
    public String getDropViewQuery(ConnectionBase connection,
                                   String catalog,
                                   String schema,
                                   String view)
    {
        String query = "DROP VIEW \"%s\".\"%s\"";
        return String.format(query, schema, view);
    }

    @Override
    public String getDropColumnQuery(ConnectionBase connection,
                                     Column column)
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
    public String getCreateTableQuery(ConnectionBase connection,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String columns)
        throws java.sql.SQLException
    {
        return String.format("CREATE TABLE \"%s\".\"%s\" (%s)", schema, table, columns);
    }

    @Override
    public String getTableElementsQuery(ConnectionBase connection,
                                        XNameAccess columns,
                                        XIndexAccess keys)
        throws java.sql.SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            for (String name : columns.getElementNames()) {
                XPropertySet column = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, columns.getByName(name));
                elements.add(getColumnQuery(connection, column));
            }
            if (keys.hasElements()) {
                elements.add(getKeyQuery(connection, keys));
            }
        } catch (NoSuchElementException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.join(", ", elements);
    }

    @Override
    public String getColumnQuery(ConnectionBase connection,
                                 XPropertySet column)
        throws java.sql.SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            elements.add(String.format("\"%s\"", (String) column.getPropertyValue("Name")));
            int type = (int) column.getPropertyValue("Type");
            String typename = (String) column.getPropertyValue("TypeName");
            int precision, scale;
            switch (type) {
            case DataType.BIT:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.LONGVARCHAR:
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BLOB:
            case DataType.CLOB:
                precision = (int) column.getPropertyValue("Precision");
                elements.add(String.format("%s(%s)", typename, precision));
                break;
            case DataType.NUMERIC:
            case DataType.DECIMAL:
                precision = (int) column.getPropertyValue("Precision");
                scale = (int) column.getPropertyValue("Scale");
                elements.add(String.format("%s(%s,%s)", typename, precision, scale));
                break;
            default:
                elements.add(typename);
                break;
            }
            if ((boolean) column.getPropertyValue("IsAutoIncrement")) {
                elements.add("GENERATED BY DEFAULT AS IDENTITY");
            }
            if ((int) column.getPropertyValue("IsNullable") == ColumnValue.NO_NULLS) {
                elements.add("NOT NULL");
            }
        } catch (WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.join(" ", elements);
    }

    @Override
    public String getKeyQuery(ConnectionBase connection,
                              XIndexAccess keys)
        throws java.sql.SQLException
    {
        String query = null;
        List<String> elements = new ArrayList<String>();
        try {
            for (int i= 0; i < keys.getCount(); i++) {
                XPropertySet key = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                int type = (int) key.getPropertyValue("Type");
                String columns = getKeyColumnQuery(connection, (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, key));
                switch (type) {
                case KeyType.PRIMARY:
                    query = String.format("PRIMARY KEY(%s)", columns);
                    break;
                case KeyType.UNIQUE:
                    query = String.format("UNIQUE(%s)", columns);
                    break;
                case KeyType.FOREIGN:
                    query = String.format("FOREIGN KEY(%s)", columns);
                    break;
                default:
                    query = null;
                    break;
                }
                if (query != null) {
                    elements.add(query);
                }
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.join(",", elements);
    }

    @Override
    public String getKeyColumnQuery(ConnectionBase connection,
                                    XColumnsSupplier key)
        throws java.sql.SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            XNameAccess columns = key.getColumns();
            for (String name : columns.getElementNames()) {
                XPropertySet column = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, columns.getByName(name));
                elements.add((String) column.getPropertyValue("Name"));
            }
        } catch (WrappedTargetException | NoSuchElementException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.join(",", elements);
    }

    @Override
    public boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol());
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
    };


    @Override
    public java.sql.Connection getConnection(final String url,
                                             final PropertyValue[] info,
                                             String level)
        throws java.sql.SQLException
    {
        return DriverManager.getConnection(url, getConnectionProperties(m_properties, info));
    }

    @Override
    public Properties getConnectionProperties(List<String> list,
                                              PropertyValue[] info)
    {
        Properties properties = new Properties();
        System.out.println("DriverProviderMain.getConnectionProperties() 1");
        for (PropertyValue property : info) {
            System.out.println("DriverProviderMain.getConnectionProperties() 2  Name: " + property.Name + " - Value: " + property.Value);
            if (list.contains(property.Name))
            {
                System.out.println("DriverProviderMain.getConnectionProperties() 3 Name: " + property.Name + " - Value: " + property.Value);
                properties.setProperty(property.Name, AnyConverter.toString(property.Value));
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
    public DatabaseMetaDataBase getDatabaseMetaData(final XComponentContext context,
                                                          final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new DatabaseMetaData(context, connection);
    }

    @Override
    public ResultSetBase getResultSet(final XComponentContext context,
                                      final ConnectionBase connection,
                                      final java.sql.ResultSet resultset)
        throws java.sql.SQLException
    {
        return new ResultSet(context, connection, resultset);
    }

    @Override
    public ResultSetBase getResultSet(final XComponentContext context,
                                      final ConnectionBase connection,
                                      final StatementMain statement,
                                      final java.sql.ResultSet resultset)
        throws java.sql.SQLException
    {
        return new ResultSet(context, connection, statement, resultset);
    }


}