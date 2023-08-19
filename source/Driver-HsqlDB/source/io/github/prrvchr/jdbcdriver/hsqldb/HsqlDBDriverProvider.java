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

import java.sql.Types;
import java.util.Map;
import java.util.logging.Level;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DriverProviderMain;
//import io.github.prrvchr.uno.sdbc.ConnectionBase;
//import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class HsqlDBDriverProvider
    extends DriverProviderMain
    implements DriverProvider
{

    protected static final Map<Integer, Integer> m_datatype = Map.ofEntries(Map.entry(Types.LONGNVARCHAR, Types.LONGVARCHAR),
                                                                            Map.entry(Types.NCHAR, Types.CHAR),
                                                                            Map.entry(Types.NVARCHAR, Types.VARCHAR),
                                                                            Map.entry(Types.ROWID, Types.JAVA_OBJECT),
                                                                            Map.entry(Types.DATALINK, Types.VARCHAR),
                                                                            Map.entry(Types.SQLXML, Types.VARCHAR),
                                                                            Map.entry(Types.NCLOB, Types.CLOB),
                                                                            Map.entry(Types.REF_CURSOR, Types.REF));

    private final Map<String, String> m_sqllogger = Map.ofEntries(Map.entry("0", "1"),
                                                                  Map.entry("1", "1"),
                                                                  Map.entry("2", "1"),
                                                                  Map.entry("3", "2"),
                                                                  Map.entry("4", "2"),
                                                                  Map.entry("5", "3"),
                                                                  Map.entry("6", "3"),
                                                                  Map.entry("7", "3"));

    @SuppressWarnings("unused")
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
        super("hsqldb");
        System.out.println("hsqldb.HsqlDBDriverProvider() 1");
    }

    @Override
    public int getDataType(int type) {
        if (m_datatype.containsKey(type)) {
            return m_datatype.get(type);
        }
        return type;
    }

    @Override
    public String getRevokeTableOrViewPrivileges()
    {
        return "REVOKE %s ON %s FROM %s CASCADE";
    }

    @Override
    public String getRevokeRoleQuery()
    {
        return "REVOKE %s FROM %s RESTRICT";
    }

    
    @Override
    public String getUserQuery()
    {
        return "SELECT USER_NAME FROM INFORMATION_SCHEMA.SYSTEM_USERS;";
    }

    @Override
    public String getGroupQuery()
    {
        return "SELECT ROLE_NAME FROM INFORMATION_SCHEMA.ADMINISTRABLE_ROLE_AUTHORIZATIONS;";
    }

    @Override
    public String getDropTableQuery()
    {
        return "DROP TABLE %s IF EXISTS;";
    }

    @Override
    public String getDropViewQuery()
    {
        return "DROP VIEW %s IF EXISTS;";
    }

    @Override
    public String getCreateTableQuery()
    {
        return "CREATE TABLE IF NOT EXISTS %s (%s);";
    }

    @Override
    public String getTableCommentQuery()
    {
        return "COMMENT ON %s IS '%s';";
    }


    @Override
    public String getColumnCommentQuery()
    {
        return "COMMENT ON %s IS '%s';";
    }


    @Override
    public String getLoggingLevel(XHierarchicalNameAccess driver)
    {
        String level = "-1";
        String property = "Installed/" + getSubProtocol() + ":*/Properties/DriverLoggerLevel/Value";
        try {
            level = (String) driver.getByHierarchicalName(property);
        }
        catch (NoSuchElementException e) { }
        return level;
    }

    @Override
    public String getConnectionUrl(final String location,
                                   final String level)
    {
        System.out.println("hsqldb.HsqlDBDriverProvider.getConnectionUrl() 1 Level: " + level);
        String url = location;
        if (!level.equals("-1")) {
            String value = m_sqllogger.get(level);
            url += String.format(";hsqldb.extlog=%s;hsqldb.applog=%s;hsqldb.sqllog=%s", value, value, value);
        }
        System.out.println("hsqldb.HsqlDBDriverProvider.getConnectionUrl() 2 Url: " + url);
        return url;
    }

    @Override
    public void setSystemProperties(String level)
        throws SQLException
    {
        System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 1 Level: " + level);
        if (!level.equals("-1")) {
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 2");
            System.setProperty("hsqldb.reconfig_logging", "true");
            //LogManager.getLogManager().reset();
            //SLF4JBridgeHandler.removeHandlersForRootLogger();
            //SLF4JBridgeHandler.install();
            //LogManager.getLogManager().getLogger("").setLevel(m_sqllevel.get(level));
            System.out.println("hsqldb.HsqlDBDriverProvider.setSystemProperties() 3");
            //LoggerFactory.getLogger("something").info("something");
        }
    }

    //@Override
    //public final DatabaseMetaDataBase getDatabaseMetaData(final ConnectionBase connection)
    //    throws java.sql.SQLException
    //{
    //    return new HsqlDBDatabaseMetaData(connection);
    //}


}