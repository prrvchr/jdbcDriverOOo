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
import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbcx.ColumnBase;

public interface DriverProvider
{

    public String getProtocol();

    public String getSubProtocol();

    public java.sql.Connection getConnection() throws java.sql.SQLException;

    public java.sql.Statement getStatement() throws java.sql.SQLException;

    public void closeConnection() throws java.sql.SQLException;

    public void setHoldability(int holdability);

    public boolean useBookmarks(boolean use);

    public ConnectionLog getLogger();

    public PropertyValue[] getInfos();

    public String getConnectionUrl(String location,
                                   String level);

    public boolean useBookmark();

    public boolean hasTableTypesSettings();

    public boolean hasDocument();

    public boolean supportService(ConnectionService service);

    public int getMockPrivileges();

    public boolean supportCreateUser();

    public String getCreateUserQuery();

    public String getSQLQuery(String command);

    public XOfficeDatabaseDocument getDocument();

    public boolean getDriverBooleanProperty(XHierarchicalNameAccess driver,
                                            String property,
                                            boolean value);

    public String getDriverStringProperty(XHierarchicalNameAccess driver,
                                          String name,
                                          String value);

    public void setConnection(ResourceBasedEventLogger logger,
                              XHierarchicalNameAccess config1,
                              XHierarchicalNameAccess config2,
                              boolean enhanced,
                              String url,
                              PropertyValue[] info,
                              String level)
        throws java.sql.SQLException;

    public String enquoteLiteral(String literal) throws java.sql.SQLException;

    // DataBaseMetadata cache data
    public boolean supportsTransactions();
    public boolean isCatalogAtStart();
    public boolean isResultSetUpdatable();
    public String getCatalogSeparator();
    public String getIdentifierQuoteString();

    public boolean supportsCatalogsInTableDefinitions();
    public boolean supportsSchemasInTableDefinitions();
    public boolean supportsCatalogsInIndexDefinitions();
    public boolean supportsSchemasInIndexDefinitions();
    public boolean supportsCatalogsInDataManipulation();
    public boolean supportsSchemasInDataManipulation();
    public boolean supportsCatalogsInProcedureCalls();
    public boolean supportsSchemasInProcedureCalls();
    public boolean supportsCatalogsInPrivilegeDefinitions();
    public boolean supportsSchemasInPrivilegeDefinitions();

    public boolean isCaseSensitive(String string);

    public boolean isEnhanced();

    // Does the underlying database driver support Commenting Objects (ie:COMMENT ON TABLE..)
    // Default value is false. see: http://hsqldb.org/doc/2.0/guide/guide.html#dbc_commenting
    public boolean supportsColumnDescription();

    // Does the underlying database driver support Commenting Objects (ie:COMMENT ON COLUMN..)
    // Default value is false. see: http://hsqldb.org/doc/2.0/guide/guide.html#dbc_commenting
    public boolean supportsTableDescription();

    // Does the underlying database driver support renaming table and view.
    public boolean supportRenamingTable();

    // Where the underlying database driver put auto-increment column declaration in table creation:
    // - In the column definition (false)
    // - In the primary key definition (true)
    public boolean isAutoIncrementIsPrimaryKey();

    // Does the underlying database driver support altering column type
    // Default value is true.
    public boolean supportsAlterColumnType();

    // Does the underlying database driver support altering column default value and not null constraint
    // Default value is true.
    public boolean supportsAlterColumnProperty();

    // Does the underlying database driver support adding, removing or altering primary key
    // Default value is true.
    public boolean supportsAlterPrimaryKey();

    // Does the underlying database driver support adding, removing or altering foreign key
    // Default value is true.
    public boolean supportsAlterForeignKey();

    // Does the underlying database driver support system versioning table
    // Default value is false.
    public boolean supportsSystemVersioning();

    // Does the underlying database driver support java.sql.Statement.getGeneratedValues()
    // Default value is false.
    public boolean isAutoRetrievingEnabled();

    // Does the underlying database driver support rights management on users and groups
    // Default value is true (ie: no support).
    public boolean ignoreDriverPrivileges();

    public boolean hasMultiRenameQueries();

    public boolean canRenameAndMove();
    
    public boolean supportRenameView();
    
    public boolean supportViewDefinition();

    // If the underlying database driver support java.sql.Statement.getGeneratedValues()
    // You must provide the SQL SELECT command which will be used (ie: SELECT * FROM %s WHERE %s)
    // The name of the table as well as the predicates will be provided by the driver
    public String getAutoRetrievingStatement();

    public String getAutoIncrementCreation();

    public boolean isIgnoreCurrencyEnabled();

    public boolean addIndexAppendix();

    public String enquoteIdentifier(String identifier) throws java.sql.SQLException;

    public String enquoteIdentifier(String identifier, boolean always) throws java.sql.SQLException;

    public String getSystemVersioningColumnQuery(List<String> columns) throws java.sql.SQLException;

    public java.sql.ResultSet getTypeInfoResultSet() throws java.sql.SQLException;

    public java.sql.ResultSet getTypeInfoResultSet(java.sql.DatabaseMetaData metadata) throws java.sql.SQLException;

    public java.sql.ResultSet getTableTypesResultSet(java.sql.DatabaseMetaData metadata) throws java.sql.SQLException;

    public String getCreateTableQuery(String table, String columns, boolean versioning);

    public String getDropTableQuery(String table);

    public String getAlterUserQuery(String user, String password);

    public String getAddColumnQuery(String table, String column);

    public String getDropColumnQuery(String table, String column);

    public int getGeneratedKeysOption();

    public List<String> getRenameTableQueries(boolean reverse, Object... args);

    public String getRenameColumnQuery();

    public String getAddConstraintQuery(int type);

    public String getDropConstraintQuery(int type);

    public String getAlterColumnQuery();

    public boolean hasAlterColumnQuery();

    public String getColumnResetDefaultQuery();

    public List<String> getAlterViewQueries(Object... args);

    public String getColumnDescriptionQuery(String column, String description);

    public String getTableDescriptionQuery(String table, String description);

    public List<String> getViewDefinitionQuery(List<Integer[]> positions, Object... args);

    public int getDataType(int type);

    public String[] getTableTypes();

    public String[] getViewTypes();

    public String getTableType(String type);

    public String getUsersQuery();

    public String getGroupsQuery();

    public String getGroupUsersQuery();

    public String getRoleGroupsQuery(boolean isrole);

    public String[] getPrivileges();

    public int getPrivileges(List<String> privileges);

    public boolean hasPrivilege(String privilege);

    public int getPrivilege(String privilege);

    public String[] getPrivileges(int privilege);

    public String getDropColumnQuery(ConnectionBase connection,
                                     ColumnBase<?> column);

    public String getDropUserQuery(ConnectionBase connection,
                                   String user);

    public boolean supportWarningsSupplier();

    public boolean acceptsURL(String url);

    public Properties getJavaConnectionProperties(PropertyValue[] infos);

    public Object getConnectionProperties(PropertyValue[] infos,
                                          String name,
                                          Object value);

    public void setSystemProperties(String level)
        throws SQLException;

    public DatabaseMetaDataBase getDatabaseMetaData(ConnectionBase connection)
        throws java.sql.SQLException;

    public String getGrantRoleQuery(Object... arguments);

    public String getRevokeRoleQuery(Object... arguments);

    public String getRevokePrivilegesQuery(Object... arguments);

    public String getGrantPrivilegesQuery(Object... arguments);

    public String getTablePrivilegesQuery(List<Integer[]> positions);

    public String getGrantablePrivilegesQuery(List<Integer[]> positions);

    public boolean supportsAlterIdentity();

    public boolean hasAddIdentityQuery();

    public String getAddIdentityQuery();

    public String getDropIdentityQuery();

    public boolean supportCreateTableKeyParts();

}
