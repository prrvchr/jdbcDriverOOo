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
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.Column;

public interface DriverProvider
{

    public String getProtocol();

    public String getProtocol(String subprotocol);

    public String[] getTableTypes();

    public String getTableType(String type);

    public String getViewQuery();

    public String getUserQuery();

    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table);

    public String getDropViewQuery(ConnectionBase connection,
                                   String catalog,
                                   String schema,
                                   String view);

    public String getDropColumnQuery(ConnectionBase connection,
                                     Column column);

    public String getDropUserQuery(ConnectionBase connection,
                                   String user);

    public String getCreateTableQuery(ConnectionBase connection,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String elements)
        throws java.sql.SQLException;


    public String getTableElementsQuery(ConnectionBase connection,
                                              XNameAccess columns,
                                              XIndexAccess keys)
        throws java.sql.SQLException;

    public String getColumnQuery(ConnectionBase connection,
                                 XPropertySet column)
        throws java.sql.SQLException;

    public String getKeyQuery(ConnectionBase connection,
                              XIndexAccess keys)
        throws java.sql.SQLException;

    public String getKeyColumnQuery(ConnectionBase connection,
                                    XColumnsSupplier key)
        throws java.sql.SQLException;

    public boolean supportWarningsSupplier();

    public boolean acceptsURL(String url);

    public String getLoggingLevel(XHierarchicalNameAccess driver);

    public java.sql.Connection getConnection(String url,
                                             PropertyValue[] info,
                                             String level)
        throws java.sql.SQLException;

    public Properties getConnectionProperties(List<String> list,
                                              PropertyValue[] info);

    public void setSystemProperties(String level)
        throws SQLException;

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
