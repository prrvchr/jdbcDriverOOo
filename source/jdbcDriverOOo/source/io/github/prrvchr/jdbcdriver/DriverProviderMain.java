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

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.StatementMain;
import io.github.prrvchr.uno.sdbcx.ColumnBase;

public abstract class DriverProviderMain
    implements DriverProvider
{

    static final String m_protocol = "xdbc:";
    private static String m_subprotocol;

    static final boolean m_warnings = true;
    private java.sql.Connection _connection = null;
    private boolean m_SupportsTransactions = true;
    private boolean m_IsCatalogAtStart = true;
    private String m_CatalogSeparator = "";
    private String m_IdentifierQuoteString = "";
    private boolean m_SupportsColumnDescription = false;
    private boolean m_IsAutoIncrementIsPrimaryKey = false;
    private boolean m_IsAutoRetrievingEnabled = false;
    private boolean m_IsResultSetUpdatable = false;
    private String m_AutoRetrievingStatement = "";

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
    public List<String> getAlterViewQueries(String view,
                                            String command)
    {
        List<String> queries = Arrays.asList(String.format("ALTER VIEW %s AS %s", view, command));
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
    public String[] getViewTypes(final boolean showsystem)
    {
        return new String[]{"VIEW"};
    }

    @Override
    public String getTableType(String type)
    {
        return type;
    }

    @Override
    public String getViewQuery(NameComponents component)
    {
        final StringBuilder sql = new StringBuilder("SELECT VIEW_DEFINITION, CHECK_OPTION FROM INFORMATION_SCHEMA.VIEWS WHERE ");
        if (!component.getCatalog().isEmpty()) {
            sql.append("TABLE_CATALOG = ? AND ");
        }
        if (!component.getSchema().isEmpty()) {
            sql.append("TABLE_SCHEMA = ? AND ");
        }
        sql.append("TABLE_NAME = ?");
        return sql.toString();
    }

    @Override
    public String getViewCommand(String sql)
    {
        return sql;
    }

    @Override
    public String getUserQuery()
    {
        return null;
    }

    @Override
    public String getGroupQuery()
    {
        return null;
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
    public String getDropViewQuery(String view)
    {
        return String.format("DROP VIEW %s", view);
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
    public String getRevokeTableOrViewPrivileges(List<String> privileges,
                                                 String table,
                                                 String grantee)
    {
        String separator = ", ";
        return String.format("REVOKE %s ON %s FROM %s", String.join(separator, privileges), table, grantee);
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
    public boolean acceptsURL(final String url)
    {
        if (url.startsWith(getSubProtocol())) {
            return true;
        }
        return false;
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
                              final PropertyValue[] infos,
                              String level)
        throws java.sql.SQLException
    {
        try {
            String url = getConnectionUrl(location, level);
            _connection = DriverManager.getConnection(url, getJavaConnectionProperties(infos));
            java.sql.DatabaseMetaData metadata = _connection.getMetaData();
            m_SupportsTransactions = metadata.supportsTransactions();
            m_IsCatalogAtStart = metadata.isCatalogAtStart();
            m_CatalogSeparator = metadata.getCatalogSeparator();
            m_IdentifierQuoteString = metadata.getIdentifierQuoteString();
            m_SupportsColumnDescription = (boolean) getConnectionProperties(infos, "SupportsColumnDescription", false);
            m_IsAutoIncrementIsPrimaryKey = (boolean) getConnectionProperties(infos, "AutoIncrementIsPrimaryKey", false);
            if (_getAutoRetrieving(metadata, infos)) {
                m_AutoRetrievingStatement = (String) getConnectionProperties(infos, "AutoRetrievingStatement", "");
                m_IsAutoRetrievingEnabled = !m_AutoRetrievingStatement.isBlank();
            }
            m_IsResultSetUpdatable = metadata.supportsResultSetConcurrency(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                           java.sql.ResultSet.CONCUR_UPDATABLE);
            System.out.println("DriverProvider.setConnection() AutoIncrementIsPrimaryKey: " + m_IsAutoIncrementIsPrimaryKey);
        }
        catch (Exception e) {
            System.out.println("DriverProvider.setConnection() ERROR: " + e.getMessage());
            for (StackTraceElement trace : e.getStackTrace()) {
                System.out.println(trace);
            }
        }
    }

    private boolean _getAutoRetrieving(java.sql.DatabaseMetaData metadata,
                                       PropertyValue[] infos)
        throws java.sql.SQLException
    {
        boolean support = false;
        System.out.println("DriverProvider._getAutoRetrieving() 1");
        // We cannot validate the option if the underlying driver
        // does not support the getGeneratedValues() method
        if (metadata.supportsGetGeneratedKeys()) {
            support = (boolean) getConnectionProperties(infos, "IsAutoRetrievingEnabled", false);
        }
        System.out.println("DriverProvider._getAutoRetrieving() 2 Support: " + String.valueOf(support));
        return support;
    }
    // DatabaseMetadata cache data
    public boolean supportsTransactions() {
        return m_SupportsTransactions;
    }
    public boolean isCatalogAtStart() {
        return m_IsCatalogAtStart;
    }
    public boolean isResultSetUpdatable() {
        return m_IsResultSetUpdatable;
    }
    public String getCatalogSeparator() {
        return m_CatalogSeparator;
    }
    public String getIdentifierQuoteString() {
        return m_IdentifierQuoteString;
    }

    // connection infos cache data
    public boolean supportsColumnDescription() {
        return m_SupportsColumnDescription;
    }
    public boolean isAutoRetrievingEnabled() {
        return m_IsAutoRetrievingEnabled;
    }
    public boolean isAutoIncrementIsPrimaryKey() {
        return m_IsAutoIncrementIsPrimaryKey;
    }
    public String getAutoRetrievingStatement() {
        return m_AutoRetrievingStatement;
    }

    public java.sql.ResultSet getGeneratedKeys(StatementMain statement, String method, String upsert)
        throws java.sql.SQLException
    {
        String query = "SELECT 1 WHERE 0 = 1";
        if (statement.getStatement() != null) {
            String sql = getAutoRetrievingStatement();
            if (!sql.isBlank()) {
                String table = DBTools.getQueryTableName(upsert);
                if (!table.isBlank()) {
                    String keys = DBTools.getGeneratedKeys(statement.getStatement(), table);
                    if (!keys.isBlank()) {
                        query = String.format(sql, table, keys);
                    }
                }
            }
        }
        int resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_QUERY;
        statement.getLogger().logprb(LogLevel.FINE, statement.getClass().getName(), method, resource, query);
        return statement.getGeneratedStatement().executeQuery(query);
    }

    public int getGeneratedKeysOption()
    {
        if (isAutoRetrievingEnabled())
            return java.sql.Statement.RETURN_GENERATED_KEYS;
        else
            return java.sql.Statement.NO_GENERATED_KEYS;
    }

    @Override
    public java.sql.Connection getConnection()
    {
        return _connection;
    }

    @Override
    public String getConnectionUrl(String location,
                                   String level)
    {
        return location;
    }

    @Override
    public Properties getJavaConnectionProperties(PropertyValue[] infos)
    {
        Properties properties = new Properties();
        for (PropertyValue info : infos) {
            if (info.Name.equals("JavaDriverClass") ||
                info.Name.equals("JavaDriverClassPath") ||
                info.Name.equals("SystemProperties") ||
                info.Name.equals("CharSet") ||
                info.Name.equals("AppendTableAliasName") ||
                info.Name.equals("AddIndexAppendix") ||
                info.Name.equals("FormsCheckRequiredFields") ||
                info.Name.equals("GenerateASBeforeCorrelationName") ||
                info.Name.equals("EscapeDateTime") ||
                info.Name.equals("ParameterNameSubstitution") ||
                info.Name.equals("IsPasswordRequired") ||
                info.Name.equals("IsAutoRetrievingEnabled") ||
                info.Name.equals("AutoRetrievingStatement") ||
                info.Name.equals("UseCatalogInSelect") ||
                info.Name.equals("UseSchemaInSelect") ||
                info.Name.equals("AutoIncrementCreation") ||
                info.Name.equals("Extension") ||
                info.Name.equals("NoNameLengthLimit") ||
                info.Name.equals("EnableSQL92Check") ||
                info.Name.equals("EnableOuterJoinEscape") ||
                info.Name.equals("BooleanComparisonMode") ||
                info.Name.equals("IgnoreCurrency") ||
                info.Name.equals("TypeInfoSettings") ||
                info.Name.equals("IgnoreDriverPrivileges") ||
                info.Name.equals("ImplicitCatalogRestriction") ||
                info.Name.equals("ImplicitSchemaRestriction") ||
                info.Name.equals("SupportsTableCreation") ||
                info.Name.equals("UseJava") ||
                info.Name.equals("Authentication") ||
                info.Name.equals("PreferDosLikeLineEnds") ||
                info.Name.equals("PrimaryKeySupport") ||
                info.Name.equals("RespectDriverResultSetType") ||
                info.Name.equals("SupportsColumnDescription") ||
                info.Name.equals("DriverLoggerLevel") ||
                info.Name.equals("AutoIncrementIsPrimaryKey") ||
                info.Name.equals("InMemoryDataBase") ||
                info.Name.equals("Type") ||
                info.Name.equals("Url") ||
                info.Name.equals("ConnectionService"))
            {
                continue;
            }
            System.out.println("DriverProvider.getJavaConnectionProperties() ERROR*************: " + info.Name);
            properties.setProperty(info.Name, String.format("%s", info.Value));
        }
        return properties;
    }

    public Object getConnectionProperties(PropertyValue[] infos,
                                          String name,
                                          Object value)
    {
        for (PropertyValue info : infos) {
            System.out.println("DriverProviderMain.getConnectionProperties() Name: " + info.Name + " - Value: " + info.Value);
            if (name.equals(info.Name)) {
                value = info.Value;
                break;
            }
        }
        return value;
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
    public boolean supportCreateTableKeyParts()
    {
        return true;
    }

}