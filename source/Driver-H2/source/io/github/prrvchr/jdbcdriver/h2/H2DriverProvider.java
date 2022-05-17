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
package io.github.prrvchr.jdbcdriver.h2;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSet;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public final class H2DriverProvider
    implements DriverProvider
{

    private static final String m_subProtocol = "h2";
    private static final boolean m_warnings = true;
    private List<String> m_properties = List.of("user", "password");
    @SuppressWarnings("unused")
    private boolean m_highLevel;
    private static final String m_logger = ";TRACE_LEVEL_FILE=4";

    // The constructor method:
    public H2DriverProvider()
    {
        System.out.println("h2.H2DriverProvider() 1");
    }

    @Override
    public final boolean acceptsURL(final String url)
    {
        return url.startsWith(getProtocol(m_subProtocol));
    }

    @Override
    public final boolean supportWarningsSupplier() {
        return m_warnings;
    }

    public String[] getTableTypes()
    {
        String[] types = {"BASE TABLE", "VIEW", "ALIAS", "SYNONYM"};
        return types;
    }

    public String getTableType(String type)
    {
        if (type.equals("BASE TABLE")) {
            type = "TABLE";
        }
        return type;
    }

    @Override
    public String getUserQuery()
    {
        return "SELECT * FROM INFORMATION_SCHEMA.USERS";
    }

    @Override
    public String getDropQuery(String element,
                               String catalog,
                               String schema,
                               String name)
    {
        String query = null;
        switch (element) {
            case "Table":
                String sql = "DROP TABLE \"%s\".\"%s\";";
                query = String.format(sql, schema, name);
                break;
            case "Column":
                break;
            case "View":
                break;
            case "User":
                break;
        }
        return query;
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
    public java.sql.Connection getConnection(boolean highLevel,
                                             final String level,
                                             final String url,
                                             final PropertyValue[] info)
        throws SQLException
    {
        m_highLevel = highLevel;
        String location = url;
        if (!level.equals("-1")) {
            location += m_logger;
        }
        return DriverManager.getConnection(location, getConnectionProperties(m_properties, info));
    }

    @Override
    public final DatabaseMetaDataBase getDatabaseMetaData(final XComponentContext context,
                                                          final ConnectionBase connection,
                                                          final java.sql.DatabaseMetaData metadata,
                                                          final PropertyValue[] info,
                                                          final String url)
    {
        return new H2DatabaseMetaData(context, this, connection, metadata, info, url, m_highLevel);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final java.sql.ResultSet resultset,
                                            final PropertyValue[] info)
    {
        return new ResultSet(context, this, resultset, info);
    }

    @Override
    public final ResultSetBase getResultSet(final XComponentContext context,
                                            final StatementMain statement,
                                            final java.sql.ResultSet resultset,
                                            final PropertyValue[] info)
    {
        return new ResultSet(context, this, statement, resultset, info);
    }


}