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
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.ColumnMain;

public abstract class DriverProviderMain
    implements DriverProvider
{

    static final String m_protocol = "xdbc:";
    static final boolean m_warnings = true;
    private java.sql.Connection m_connection = null;
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
    public boolean isCaseSensitive()
    {
        return true;
    }

    @Override
    public String getAlterViewQuery()
    {
        return "ALTER VIEW %s AS %s";
    }


    @Override
    public int getDataType(int type) {
        return type;
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
    public String getGroupQuery()
    {
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS GROUP BY ROLE_NAME;";
    }

    @Override
    public String getGroupUsersQuery()
    {
        return "SELECT GRANTEE FROM INFORMATION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS WHERE ROLE_NAME=?;";
    }

    @Override
    public String getUserGroupsQuery()
    {
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ROLE_AUTHORIZATION_DESCRIPTORS WHERE GRANTEE=?;";
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
    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table)
        throws SQLException
    {
        try {
            String query = "DROP TABLE %s";
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            boolean mixed = metadata.supportsMixedCaseQuotedIdentifiers();
            return String.format(query, getTableIdentifier(connection, catalog, schema, table, quote, mixed));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    @Override
    public String getDropViewQuery(ConnectionBase connection,
                                   String catalog,
                                   String schema,
                                   String view)
        throws SQLException
    {
        try {
            String query = "DROP VIEW %s";
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            boolean mixed = metadata.supportsMixedCaseQuotedIdentifiers();
            return String.format(query, getTableIdentifier(connection, catalog, schema, view, quote, mixed));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    @Override
    public String getDropColumnQuery(ConnectionBase connection,
                                     ColumnMain column)
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
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    @Override
    public String[] getCreateTableQueries(ConnectionBase connection,
                                          XPropertySet descriptor)
        throws SQLException
    {
        List<String> queries = new ArrayList<String>();
        System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 1");
        XNameAccess columns = ((XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor)).getColumns();
        XIndexAccess keys = ((XKeysSupplier) UnoRuntime.queryInterface(XKeysSupplier.class, descriptor)).getKeys();
        System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 2");
        try {
            String catalog = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.CATALOGNAME.name));
            String schema = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.SCHEMANAME.name));
            String table = AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.NAME.name));
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            String quote = metadata.getIdentifierQuoteString();
            boolean mixed = metadata.supportsMixedCaseQuotedIdentifiers();
            String[] elements = getTableElementsQuery(connection, columns, keys, quote, mixed);
            System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 3: " + elements.length);
            if (elements.length > 0) {
                 queries.add(getCreateTableQuery(getTableIdentifier(connection, catalog, schema, table, quote, mixed), String.join(", ", elements)));
                 System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 4");
            }
            String description = (String) descriptor.getPropertyValue("Description");
            System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 5: " + description);
            if (!description.isBlank()) {
                queries.add(getTableCommentQuery(connection, catalog, schema, table, description, quote, mixed));
            }
            XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
            while (iter.hasMoreElements()) {
                XPropertySet column = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                String coldesc = (String) column.getPropertyValue("Description");
                System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 6: " + coldesc);
                if (!coldesc.isBlank()) {
                    String name = (String) column.getPropertyValue("Name");
                    queries.add(getColumnCommentQuery(connection, catalog, schema, table, name, coldesc, quote, mixed));
                }
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        System.out.println("jdbcdriver.DriverProviderMain.getCreateTableQueries() 7");
        return queries.toArray(new String[0]);
     }

    @Override
    public String getTableIdentifier(ConnectionBase connection,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String quote,
                                      boolean mixed)
        throws SQLException
    {
        try {
            return getTableIdentifier(connection.getProvider().getConnection().getMetaData(), catalog, schema, table, quote, mixed);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    @Override
    public String getTableIdentifier(java.sql.DatabaseMetaData metadata,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException
    {
        StringBuilder identifier = new StringBuilder(getQuotedIdentifier(table, quote, mixed));
        if (metadata.supportsSchemasInDataManipulation()) {
            identifier.insert(0, ".");
            identifier.insert(0, getQuotedIdentifier(schema, quote, mixed));
        }
        if (metadata.supportsCatalogsInDataManipulation()) {
            if (metadata.isCatalogAtStart()) {
                identifier.insert(0, metadata.getCatalogSeparator());
                identifier.insert(0, getQuotedIdentifier(catalog, quote, mixed));
            }
            else {
                identifier.append(metadata.getCatalogSeparator());
                identifier.append(getQuotedIdentifier(catalog, quote, mixed));
            }
        }
        return identifier.toString();
    }

    @Override
    public String getColumnIdentifier(ConnectionBase connection,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String column,
                                      String quote,
                                      boolean mixed)
        throws SQLException
    {
        try {
            return getColumnIdentifier(connection.getProvider().getConnection().getMetaData(), catalog, schema, table, column, quote, mixed);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
    }

    @Override
    public String getColumnIdentifier(java.sql.DatabaseMetaData metadata,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String column,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException
    {
        StringBuilder identifier = new StringBuilder(getTableIdentifier(metadata, catalog, schema, table, quote, mixed));
        identifier.append(".");
        identifier.append(getQuotedIdentifier(column, quote, mixed));
        return identifier.toString();
    }

    @Override
    public String getQuotedIdentifier(String id,
                                      String quote,
                                      boolean mixed)
    {
        StringBuilder identifier = new StringBuilder(id);
        if (mixed && id != id.toUpperCase() && id != id.toLowerCase()) {
            identifier.insert(0, quote);
            identifier.append(quote);
        }
        return identifier.toString();
    }

    @Override
    public String getCreateTableQuery(String identifier,
                                      String columns)
    {
        return String.format("CREATE TABLE %s (%s)", identifier, columns);
    }

    @Override
    public String getTableCommentQuery(ConnectionBase connection,
                                       String catalog,
                                       String schema,
                                       String table,
                                       String description,
                                       String quote,
                                       boolean mixed)
        throws SQLException
    {
        String query = "COMMENT ON %s IS '%s'";
        String identifier = getTableIdentifier(connection, catalog, schema, table, quote, mixed);
        return String.format(query, identifier, description);
    }

    @Override
    public String getColumnCommentQuery(ConnectionBase connection,
                                        String catalog,
                                        String schema,
                                        String table,
                                        String column,
                                        String description,
                                        String quote,
                                        boolean mixed)
        throws SQLException
    {
        String query = "COMMENT ON %s IS '%s'";
        String identifier = getColumnIdentifier(connection, catalog, schema, table, column, quote, mixed);
        return String.format(query, identifier, description);
    }

    @Override
    public String[] getTableElementsQuery(ConnectionBase connection,
                                          XNameAccess columns,
                                          XIndexAccess keys,
                                          String quote,
                                          boolean mixed)
        throws SQLException
    {
        System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 1");
        List<String> elements = new ArrayList<String>();
        try {
            XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
            System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 2");
            while (iter.hasMoreElements()) {
                String[] column = getColumnQuery(connection, (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement()), quote, mixed);
                System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 3");
                if (column.length > 0) {
                    elements.add(String.join(" ", column));
                }
            }
            if (keys.hasElements()) {
                System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 4");
                String[] key = getKeyQuery(connection, (XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, keys), quote, mixed);
                if (key.length > 0) {
                    elements.add(String.join(" ", key));
                }
            }
        } 
        catch (NoSuchElementException | WrappedTargetException e) {
            System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 5: ********************************************");
            throw UnoHelper.getSQLException(e, connection);
        }
        System.out.println("jdbcdriver.DriverProviderMain.getTableElementsQuery() 7");
        return elements.toArray(new String[0]);
    }

    @Override
    public String[] getColumnQuery(ConnectionBase connection,
                                   XPropertySet column,
                                   String quote,
                                   boolean mixed)
        throws SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            elements.add(getQuotedIdentifier((String) column.getPropertyValue("Name"), quote, mixed));
            int type = (int) column.getPropertyValue("Type");
            String typename = (String) column.getPropertyValue("TypeName");
            int precision = (int) column.getPropertyValue("Precision");
            int scale = (int) column.getPropertyValue("Scale");
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
                elements.add(String.format("%s(%s)", typename, precision));
                break;
            case DataType.NUMERIC:
            case DataType.DECIMAL:
                elements.add(String.format("%s(%s,%s)", typename, precision, scale));
                break;
            default:
                elements.add(typename);
                break;
            }
            if ((boolean) column.getPropertyValue("IsAutoIncrement")) {
                String autoincrement = "";
                if (column.getPropertySetInfo().hasPropertyByName("AutoIncrementCreation")) {
                    autoincrement = (String) column.getPropertyValue("AutoIncrementCreation");
                }
                else {
                    autoincrement = getAutoIncrementCreation();
                }
                if (!autoincrement.isBlank()) {
                    System.out.println("jdbcdriver.DriverProviderMain.getColumnQuery() 1 : " + autoincrement);
                    elements.add(autoincrement);
                }
            }
            if ((int) column.getPropertyValue("IsNullable") == ColumnValue.NO_NULLS) {
                elements.add("NOT NULL");
            }
            if (column.getPropertySetInfo().hasPropertyByName("DefaultValue")) {
                System.out.println("jdbcdriver.DriverProviderMain.getColumnQuery() 2");
                String value = (String) column.getPropertyValue("DefaultValue");
                System.out.println("jdbcdriver.DriverProviderMain.getColumnQuery() 3 : '" + value + "'");
                if (!value.isBlank()) {
                    System.out.println("jdbcdriver.DriverProviderMain.getColumnQuery() 4 : " + value);
                    elements.add(String.format("DEFAULT %s", value));
                }
            }
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return elements.toArray(new String[0]);
    }

    @Override
    public String[] getKeyQuery(ConnectionBase connection,
                                XEnumerationAccess keys,
                                String quote,
                                boolean mixed)
        throws SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            XEnumeration iter = keys.createEnumeration();
            while (iter.hasMoreElements()) {
                String query = null;
                XPropertySet key = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                XColumnsSupplier supplier = (XColumnsSupplier) UnoRuntime.queryInterface(XColumnsSupplier.class, key);
                String[] columns = getKeyColumnQuery(connection, UnoRuntime.queryInterface(XEnumerationAccess.class, supplier.getColumns()), quote, mixed);
                if (columns.length > 0) {
                    switch ((int) key.getPropertyValue("Type")) {
                    case KeyType.PRIMARY:
                        query = String.format("PRIMARY KEY(%s)", String.join(",", columns));
                        break;
                    case KeyType.UNIQUE:
                        query = String.format("UNIQUE(%s)", String.join(",", columns));
                        break;
                    case KeyType.FOREIGN:
                        query = String.format("FOREIGN KEY(%s)", String.join(",", columns));
                        break;
                    default:
                        query = null;
                        break;
                    }
                }
                if (query != null) {
                    elements.add(query);
                }
            }
        } catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, connection);
        }
        return elements.toArray(new String[0]);
    }

    @Override
    public String[] getKeyColumnQuery(ConnectionBase connection,
                                      XEnumerationAccess columns,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException
    {
        List<String> elements = new ArrayList<String>();
        try {
            XEnumeration iter = columns.createEnumeration();
            while (iter.hasMoreElements()) {
                XPropertySet column = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                elements.add(getQuotedIdentifier((String) column.getPropertyValue("Name"), quote, mixed));
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return elements.toArray(new String[0]);
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
                System.out.println("DriverProviderMain.getConnectionProperties() 2 Name: " + property.Name + " - Value: " + value);
            }
            if (list.contains(property.Name))
            {
                System.out.println("DriverProviderMain.getConnectionProperties() 3 Name: " + property.Name + " - Value: " + value);
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
    public ResultSetBase getResultSet(final ConnectionBase connection,
                                      final java.sql.ResultSet resultset)
        throws SQLException
    {
        return new io.github.prrvchr.uno.sdbc.ResultSet(connection, resultset, null, false);
    }

    @Override
    public ResultSetBase getResultSet(final ConnectionBase connection,
                                      final java.sql.ResultSet resultset,
                                      final StatementMain statement)
        throws SQLException
    {
        return new io.github.prrvchr.uno.sdbc.ResultSet(connection, resultset, statement, false);
    }

    @Override
    public ResultSetBase getResultSet(final ConnectionBase connection,
                                      final java.sql.ResultSet resultset,
                                      final StatementMain statement,
                                      boolean bookmark)
        throws SQLException
    {
        return new io.github.prrvchr.uno.sdbcx.ResultSet(connection, resultset, statement, bookmark);
    }

}