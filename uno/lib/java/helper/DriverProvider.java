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
package io.github.prrvchr.uno.helper;

import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.DataBaseTools.NameComponents;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.ColumnMain;

public interface DriverProvider
{

    public String getProtocol();

    public String getSubProtocol();

    public java.sql.Connection getConnection();

    public String getConnectionUrl(String location,
                                   String level);

    public void setConnection(String url,
                              PropertyValue[] info,
                              String level)
        throws java.sql.SQLException;

    public boolean isCaseSensitive(String string);

    public String[] getAlterViewQueries(String view, String command);

    public int getDataType(int type);

    public String[] getTableTypes();

    public String getTableType(String type);

    public String getViewQuery(NameComponents component);

    public String getViewCommand(String string);

    public String getUserQuery();

    public String getGroupQuery();

    public String getGroupUsersQuery();

    public String getUserGroupsQuery();

    public int getPrivilege(String privilege);

    public List<String> getPrivileges(int privilege);

    public String getTableIdentifier(ConnectionBase connection,
                                     String catalog,
                                     String schema,
                                     String table,
                                     String quote,
                                     boolean mixed)
        throws SQLException;

    public String getColumnIdentifier(ConnectionBase connection,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String column,
                                      String quote,
                                      boolean mixed)
        throws SQLException;

    public String getTableIdentifier(java.sql.DatabaseMetaData metadata,
                                     String catalog,
                                     String schema,
                                     String table,
                                     String quote,
                                     boolean mixed)
        throws java.sql.SQLException;


    public String getColumnIdentifier(java.sql.DatabaseMetaData metadata,
                                      String catalog,
                                      String schema,
                                      String table,
                                      String column,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException;

    public String getQuotedIdentifier(String identifier,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException;

    public String getDropTableQuery(ConnectionBase connection,
                                    String catalog,
                                    String schema,
                                    String table)
        throws SQLException;

    public String getDropViewQuery(ConnectionBase connection,
                                   String catalog,
                                   String schema,
                                   String view)
    throws SQLException;

    public String getDropColumnQuery(ConnectionBase connection,
                                     ColumnMain column);

    public String getDropUserQuery(ConnectionBase connection,
                                   String user);

    public String getAutoIncrementCreation();

    public String[] getCreateTableQueries(ConnectionBase connection,
                                          XPropertySet descriptor)
        throws SQLException;


    public String getCreateTableQuery(String identifier,
                                      String elements);


    public String getTableCommentQuery(ConnectionBase connection,
                                       String catalog,
                                       String schema,
                                       String table,
                                       String description,
                                       String quote,
                                       boolean mixed)
        throws SQLException;


    public String getColumnCommentQuery(ConnectionBase connection,
                                        String catalog,
                                        String schema,
                                        String table,
                                        String column,
                                        String description,
                                        String quote,
                                        boolean mixed)
        throws SQLException;


    public String[] getTableElementsQuery(ConnectionBase connection,
                                          XNameAccess columns,
                                          XIndexAccess keys,
                                          String quote,
                                          boolean mixed)
        throws SQLException;

    public String[] getColumnQuery(ConnectionBase connection,
                                   XPropertySet column,
                                   String quote,
                                   boolean mixed)
        throws SQLException;

    public String[] getKeyQuery(ConnectionBase connection,
                                XEnumerationAccess keys,
                                String quote,
                                boolean mixed)
        throws SQLException;

    public String[] getKeyColumnQuery(ConnectionBase connection,
                                      XEnumerationAccess columns,
                                      String quote,
                                      boolean mixed)
        throws java.sql.SQLException;

    public boolean supportWarningsSupplier();

    public boolean acceptsURL(String url,
                              PropertyValue[] info);

    public ConnectionBase getConnection(XComponentContext ctx,
                                        ResourceBasedEventLogger logger,
                                        boolean enhanced);

    public String getUrl();

    public PropertyValue[] getInfo();

    public String getLoggingLevel(XHierarchicalNameAccess driver);

    public Properties getConnectionProperties(List<String> list,
                                              PropertyValue[] info);

    public void setSystemProperties(String level)
        throws SQLException;

    public DatabaseMetaDataBase getDatabaseMetaData(ConnectionBase connection)
        throws java.sql.SQLException;

    public ResultSetBase getResultSet(ConnectionBase connection,
                                      java.sql.ResultSet resultset)
        throws SQLException;

    public ResultSetBase getResultSet(ConnectionBase connection,
                                      java.sql.ResultSet resultset,
                                      StatementMain statement)
        throws SQLException;

    public ResultSetBase getResultSet(ConnectionBase connection,
                                      java.sql.ResultSet resultset,
                                      StatementMain statement,
                                      boolean bookmark)
        throws SQLException;

    public boolean isAutoRetrievingEnabled();

    public String getAutoRetrievingStatement();

    public String getTransformedGeneratedStatement(String sql);

    void registerURL(String url,
                     PropertyValue[] info);

    public String getRevokeTableOrViewPrivileges();

    public String getRevokeRoleQuery();

    public boolean isIgnoreCurrencyEnabled();

    public boolean supportCreateTableKeyParts();

}
