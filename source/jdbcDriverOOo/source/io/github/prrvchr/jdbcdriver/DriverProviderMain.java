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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaData;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbcx.ColumnBase;

public abstract class DriverProviderMain
    implements DriverProvider
{

    static final String m_protocol = "xdbc:";
    private static String m_subprotocol;

    static final boolean m_warnings = true;
    protected ConnectionLog m_logger;
    private PropertyValue[] m_infos;
    private java.sql.Statement m_statement = null;
    private boolean m_SupportsTransactions = true;
    private boolean m_IsCatalogAtStart = true;
    private String m_CatalogSeparator = "";
    private String m_IdentifierQuoteString = "";
    private String m_TableDescriptionCommand = null;
    private String m_ColumnDescriptionCommand = null;
    private boolean m_IsAutoIncrementIsPrimaryKey = false;
    private boolean m_IsAutoRetrievingEnabled = false;
    private boolean m_IsResultSetUpdatable = false;
    private String m_AutoRetrievingStatement = "";
    private boolean m_IgnoreDriverPrivileges = true;
    private boolean m_SupportRenameView = true;
    private String m_RenameColumnCommand = null;
    private Object[] m_RenameTableCommands = null;
    private Object[] m_ViewDefinitionCommands = null;
    private Object[] m_AlterViewCommands = null;
    private Object[] m_TypeInfoSettings = null;

    @Override
    public ConnectionLog getLogger() {
        return m_logger;
    }

    @Override
    public PropertyValue[] getInfos() {
        return m_infos;
    }

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
    public String getColumnDescriptionQuery(String column, String description) {
        return MessageFormat.format(m_ColumnDescriptionCommand, column, description);
    }

    @Override
    public String getTableDescriptionQuery(String table, String description) {
        return MessageFormat.format(m_TableDescriptionCommand, table, description);
    }

    @Override
    public List<String> getAlterViewQueries(Object... arguments)
    {
        return getDDLQueriesCommand(m_AlterViewCommands, null, false, arguments);
    }

    @Override
    public List<String> getRenameTableQueries(boolean reverse,
                                              Object... arguments)
    {
        return getDDLQueriesCommand(m_RenameTableCommands, null, reverse, arguments);
    }

    public String getRenameColumnQuery() {
        return m_RenameColumnCommand != null ?
               m_RenameColumnCommand :
               DBDefaultQuery.STR_QUERY_RENAME_COLUMN;
    }

    @Override
    public List<String> getViewDefinitionQuery(List<Integer[]> positions,
                                               Object... arguments)
    {
        return getDDLQueriesCommand(m_ViewDefinitionCommands, positions, false, arguments);
    }

    private List<String> getDDLQueriesCommand(Object[] commands,
                                              List<Integer[]> positions,
                                              boolean reversed,
                                              Object... arguments)
    {
        int count = 0;
        boolean simple = positions == null;
        List<String> queries = new ArrayList<String>();
        if (reversed) {
            for (int i = commands.length - 1; i >= 0; i--) {
                setDDLQueryCommand(queries, commands[i].toString(), simple, count, arguments);
                count ++;
            }
        }
        else {
            for (Object command : commands) {
                Integer[] position = setDDLQueryCommand(queries, command.toString(), simple, count, arguments);
                if (!simple && position != null) {
                    positions.add(position);
                }
                count ++;
            }
        }
        return queries;
    }

    private Integer[] setDDLQueryCommand(List<String> queries,
                                         String command,
                                         boolean simple,
                                         int count,
                                         Object... arguments)
    {
        if (simple || count % 2 == 0) {
            // XXX: Some commands may be empty, we need to filter such command.
            if (!command.isBlank()) {
                queries.add(MessageFormat.format(command, arguments));
            }
            return null;
        }
        List<Integer> positions = new ArrayList<Integer>();
        // XXX: For the parameterized PrepareStatement, we need to get the positions of the parameters
        String sql = command.replaceAll("\\s","");
        if (sql.chars().allMatch(Character::isDigit)) {
            char[] position = sql.toCharArray();
            for (int i = 0; i < position.length; i ++) {
                positions.add(position[i] - '0');
            }
        }
        return positions.toArray(new Integer[0]);
    }

    public boolean supportRenameView() {
        return m_SupportRenameView;
    }

    private boolean _supportRenamingTable() {
        return m_RenameTableCommands != null;
    }

    @Override
    public boolean supportRenamingTable() {
        return _supportRenamingTable() && m_RenameTableCommands.length > 0;
    }

    @Override
    public boolean canRenameAndMove() {
        return _supportRenamingTable() && m_RenameTableCommands.length > 1;
    }

    @Override
    public boolean hasMultiRenameQueries()
    {
        return canRenameAndMove() && !m_RenameTableCommands[1].toString().isBlank();
    }


    @Override
    public boolean supportViewDefinition() {
        return m_ViewDefinitionCommands != null && m_ViewDefinitionCommands.length > 1;
    }

    @Override
    public int getDataType(int type) {
        return type;
    }

    @Override
    public String[] getTableTypes(final boolean showsystem)
    {
        String[] types = null;
        if (!showsystem) {
            types = new String[]{"TABLE", "VIEW"};
        }
        return types;
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
    public void setConnection(ResourceBasedEventLogger logger,
                              final String location,
                              final PropertyValue[] infos,
                              String level)
        throws java.sql.SQLException
    {
        m_infos = infos;
        m_logger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);
        String url = getConnectionUrl(location, level);
        java.sql.Connection connection = DriverManager.getConnection(url, getJavaConnectionProperties(infos));
        java.sql.DatabaseMetaData metadata = connection.getMetaData();
        m_SupportsTransactions = metadata.supportsTransactions();
        m_IsCatalogAtStart = metadata.isCatalogAtStart();
        m_CatalogSeparator = metadata.getCatalogSeparator();
        m_IdentifierQuoteString = metadata.getIdentifierQuoteString();
        m_ColumnDescriptionCommand = (String) getConnectionProperties(infos, "ColumnDescriptionCommand", null);
        m_TableDescriptionCommand = (String) getConnectionProperties(infos, "TableDescriptionCommand", null);
        m_IsAutoIncrementIsPrimaryKey = (boolean) getConnectionProperties(infos, "AutoIncrementIsPrimaryKey", false);
        m_IgnoreDriverPrivileges = (boolean) getConnectionProperties(infos, "IgnoreDriverPrivileges", true);
        m_AlterViewCommands = (Object[]) getConnectionProperties(infos, "AlterViewCommands", null);
        m_ViewDefinitionCommands = (Object[]) getConnectionProperties(infos, "ViewDefinitionCommands", null);
        m_SupportRenameView = (boolean) getConnectionProperties(infos, "SupportRenameView", true);
        m_RenameColumnCommand = (String) getConnectionProperties(infos, "RenameColumnCommand", null);
        m_RenameTableCommands = (Object[]) getConnectionProperties(infos, "RenameTableCommands", null);
        m_TypeInfoSettings = (Object[]) getConnectionProperties(infos, "TypeInfoSettings", null);
        if (_getAutoRetrieving(metadata, infos)) {
            m_AutoRetrievingStatement = (String) getConnectionProperties(infos, "AutoRetrievingStatement", "");
            m_IsAutoRetrievingEnabled = (boolean) getConnectionProperties(infos, "IsAutoRetrievingEnabled", false);
        }
        m_IsResultSetUpdatable = metadata.supportsResultSetConcurrency(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                       java.sql.ResultSet.CONCUR_UPDATABLE);
        m_statement = connection.createStatement();
    }


    private boolean _getAutoRetrieving(java.sql.DatabaseMetaData metadata,
                                       PropertyValue[] infos)
        throws java.sql.SQLException
    {
        Boolean support = false;
        support = (Boolean) getConnectionProperties(infos, "IsAutoRetrievingEnabled", null);
        // FIXME: If IsAutoRetrievingEnabled is not set, we retrieve the option from the underlying metadata driver.
        // FIXME: This allows you to correct possible failures of certain drivers (ie: like for Derby)
        if (support == null) {
            support = metadata.supportsGetGeneratedKeys();
        }
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
        return m_ColumnDescriptionCommand != null;
    }
    public boolean supportsTableDescription() {
        return m_TableDescriptionCommand != null;
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
    public boolean ignoreDriverPrivileges() {
        return m_IgnoreDriverPrivileges;
    }
    public Object[] getTypeInfoSettings() {
        return m_TypeInfoSettings;
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
        throws java.sql.SQLException
    {
        if (m_statement != null) {
            return m_statement.getConnection();
        }
        return null;
    }
    public java.sql.Statement getStatement()
        throws java.sql.SQLException
    {
        if (m_statement != null) {
            return m_statement;
        }
        return null;
    }

    @Override
    public void closeConnection()
        throws java.sql.SQLException
    {
        if (m_statement != null) {
            java.sql.Connection connection = m_statement.getConnection();
            m_statement.close();
            m_statement = null;
            connection.close();
        }
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
                info.Name.equals("ColumnDescriptionCommand") ||
                info.Name.equals("TableDescriptionCommand") ||
                info.Name.equals("DriverLoggerLevel") ||
                info.Name.equals("AutoIncrementIsPrimaryKey") ||
                info.Name.equals("InMemoryDataBase") ||
                info.Name.equals("AlterViewCommands") ||
                info.Name.equals("SupportRenameView") ||
                info.Name.equals("RenameTableCommands") ||
                info.Name.equals("ViewDefinitionCommands") ||
                info.Name.equals("Type") ||
                info.Name.equals("Url") ||
                info.Name.equals("ConnectionService"))
            {
                continue;
            }
            System.out.println("DriverProvider.getJavaConnectionProperties() *********************: " + info.Name);
            properties.setProperty(info.Name, String.format("%s", info.Value));
        }
        return properties;
    }

    public Object getConnectionProperties(PropertyValue[] infos,
                                          String name,
                                          Object value)
    {
        for (PropertyValue info : infos) {
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