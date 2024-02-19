/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║ 
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

import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbcx.ColumnBase;

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

    // DataBaseMetadata cache data
    public boolean supportsTransactions();
    public boolean isCatalogAtStart();
    public boolean isResultSetUpdatable();
    public String getCatalogSeparator();
    public String getIdentifierQuoteString();

    public boolean isCaseSensitive(String string);

    // Does the underlying database driver support Commenting Objects (ie:COMMENT ON TABLE..)
    // Default value is false. see: http://hsqldb.org/doc/2.0/guide/guide.html#dbc_commenting
    public boolean supportsColumnDescription();

    // Where the underlying database driver put auto-increment column declaration in table creation:
    // - In the column definition (false)
    // - In the primary key definition (true)
    public boolean isAutoIncrementIsPrimaryKey();

    // Does the underlying database driver support java.sql.Statement.getGeneratedValues()
    // Default value is false.
    public boolean isAutoRetrievingEnabled();

    // Does the underlying database driver support rights management on users and groups
    // Default value is true (ie: no support).
    public boolean ignoreDriverPrivileges();

    // If the underlying database driver support java.sql.Statement.getGeneratedValues()
    // You must provide the SQL SELECT command which will be used (ie: SELECT * FROM %s WHERE %s)
    // The name of the table as well as the predicates will be provided by the driver
    public String getAutoRetrievingStatement();

    public Object[] getTypeInfoSettings();

    public int getGeneratedKeysOption();

    public List<String> getAlterViewQueries(String view, String command);

    public int getDataType(int type);

    public String[] getTableTypes();

    public String[] getViewTypes(boolean showsystem);

    public String getTableType(String type);

    public String getViewQuery(NameComponents component);

    public String getViewCommand(String string);

    public String getUserQuery();

    public String getGroupQuery();

    public String getGroupUsersQuery();

    public String getUserGroupsQuery();

    public int getPrivilege(String privilege);

    public List<String> getPrivileges(int privilege);

    public String getDropTableQuery();

    public String getDropViewQuery(String view);

    public String getDropColumnQuery(ConnectionBase connection,
                                     ColumnBase column);

    public String getDropUserQuery(ConnectionBase connection,
                                   String user);

    public String getCreateTableQuery();


    public String getTableCommentQuery();


    public String getColumnCommentQuery();

    public boolean supportWarningsSupplier();

    public boolean acceptsURL(String url);

    public String getLoggingLevel(XHierarchicalNameAccess driver);

    public Properties getJavaConnectionProperties(PropertyValue[] infos);

    public Object getConnectionProperties(PropertyValue[] infos,
                                          String name,
                                          Object value);

    public void setSystemProperties(String level)
        throws SQLException;

    public DatabaseMetaDataBase getDatabaseMetaData(ConnectionBase connection)
        throws java.sql.SQLException;

    public String getRevokeTableOrViewPrivileges(List<String> privileges,
                                                 String table,
                                                 String grantee);

    public String getRevokeRoleQuery();

    public boolean supportCreateTableKeyParts();

}
