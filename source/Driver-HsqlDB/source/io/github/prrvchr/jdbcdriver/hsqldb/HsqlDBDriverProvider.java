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
package io.github.prrvchr.jdbcdriver.hsqldb;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;

import org.slf4j.bridge.SLF4JBridgeHandler;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class HsqlDBDriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    private static final String m_subProtocol = "hsqldb";
    private final Map<String, String> m_sqllogger = Map.ofEntries(Map.entry("0", "1"),
                                                                  Map.entry("1", "1"),
                                                                  Map.entry("2", "1"),
                                                                  Map.entry("3", "2"),
                                                                  Map.entry("4", "2"),
                                                                  Map.entry("5", "3"),
                                                                  Map.entry("6", "3"),
                                                                  Map.entry("7", "3"));
    private final Map<String, Level> m_sqllevel = Map.ofEntries(Map.entry("0", Level.SEVERE),
                                                                Map.entry("1", Level.WARNING),
                                                                Map.entry("2", Level.INFO),
                                                                Map.entry("3", Level.CONFIG),
                                                                Map.entry("4", Level.FINE),
                                                                Map.entry("5", Level.FINER),
                                                                Map.entry("6", Level.FINEST),
                                                                Map.entry("7", Level.ALL));

    // The constructor method:
    public HsqlDBDriverProvider()
    {
        System.out.println("hsqldb.HsqlDBDriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url,
                                    final PropertyValue[] info)
    {
        return super.acceptsURL(url, info, m_subProtocol);
    }

    @Override
    public int getDataType(int type) {
        if (HsqlDBDatabaseMetaData.m_dataType.containsKey(type)) {
            return HsqlDBDatabaseMetaData.m_dataType.get(type);
        }
        return type;
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
    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table)
        throws SQLException
    {
        try {
            String query = "DROP TABLE %s IF EXISTS;";
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
            String query = "DROP VIEW %s IF EXISTS;";
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
    public String getCreateTableQuery(String identifier,
                                      String columns)
    {
        return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", identifier, columns);
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
        String query = "COMMENT ON %s IS '%s';";
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
        String query = "COMMENT ON %s IS '%s';";
        String identifier = getColumnIdentifier(connection, catalog, schema, table, column, quote, mixed);
        return String.format(query, identifier, description);
    }


    @Override
    public String getLoggingLevel(XHierarchicalNameAccess driver)
    {
        String level = "-1";
        String property = "Installed/" + getProtocol(m_subProtocol) + ":*/Properties/DriverLoggerLevel/Value";
        try {
            level = (String) driver.getByHierarchicalName(property);
        } catch (NoSuchElementException e) { }
        return level;
    }

    @Override
    public String getConnectionUrl(final String location,
                                                final String level)
    {
        @SuppressWarnings("unused")
        String url = location;
        if (!level.equals("-1")) {
            url += ";hsqldb.sqllog=" + m_sqllogger.get(level);
        }
        return location;
    }

    @Override
    public void setSystemProperties(String level)
        throws SQLException
    {
        System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 1");
        if (!level.equals("-1")) {
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 2");
            System.setProperty("hsqldb.reconfig_logging", "false");
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            Logger.getLogger("").setLevel(m_sqllevel.get(level));
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 3");
        }
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        return new HsqlDBDatabaseMetaData(connection);
    }


}