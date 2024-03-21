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
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.UnoHelper;
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
    private CustomTypeInfo m_typeinforows = null;
    protected boolean m_enhanced;
    protected boolean m_showsystem;
    protected boolean m_usebookmark;

    private boolean m_CatalogsInTableDefinitions;
    private boolean m_SchemasInTableDefinitions;
    private boolean m_CatalogsInIndexDefinitions;
    private boolean m_SchemasInIndexDefinitions;
    private boolean m_CatalogsInDataManipulation;
    private boolean m_SchemasInDataManipulation;
    private boolean m_CatalogsInProcedureCalls;
    private boolean m_SchemasInProcedureCalls;
    private boolean m_CatalogsInPrivilegeDefinitions;
    private boolean m_SchemasInPrivilegeDefinitions;

    private boolean m_SupportsColumnDescription = false;

    private boolean m_SupportsTransactions = true;
    private boolean m_IsCatalogAtStart = true;
    private String m_CatalogSeparator = "";
    private String m_IdentifierQuoteString = "";
    private String m_AutoIncrementCreation = "";
    private boolean m_AddIndexAppendix = false;
    private boolean m_IsAutoIncrementIsPrimaryKey = false;
    private boolean m_SupportsAlterColumnType = true;
    private boolean m_SupportsAlterColumnProperty = true;
    private boolean m_SupportsAlterPrimaryKey = true;
    private boolean m_IsAutoRetrievingEnabled = false;
    private boolean m_IsResultSetUpdatable = false;
    private String m_AutoRetrievingStatement = "";
    private boolean m_IgnoreDriverPrivileges = true;
    private boolean m_IgnoreCurrency = false;
    private boolean m_SupportsRenameView = true;
    private boolean m_SupportsAlterIdentity = false;
    private String m_CreateTableCommand = DBDefaultQuery.STR_QUERY_CREATE_TABLE;
    private String m_DropTableCommand = DBDefaultQuery.STR_QUERY_DROP_TABLE;
    private String m_AddColumnCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_COLUMN;
    private String m_DropColumnCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_COLUMN;
    private String m_RenameColumnCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_RENAME_COLUMN;
    private String m_AddPrimaryKeyCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_PRIMARY_KEY;
    private String m_AddForeignKeyCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_FOREIGN_KEY;
    private String m_AddIndexCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_INDEX;
    private String m_DropPrimaryKeyCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_PRIMARY_KEY;
    private String m_DropConstraintCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_CONSTRAINT;
    private String m_DropIndexCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_INDEX;
    private String m_TableDescriptionCommand = DBDefaultQuery.STR_QUERY_ADD_TABLE_COMMENT;;
    private String m_ColumnDescriptionCommand = DBDefaultQuery.STR_QUERY_ADD_COLUMN_COMMENT;
    private Object[] m_AlterViewCommands = {DBDefaultQuery.STR_QUERY_ALTER_VIEW};
    private String m_ColumnResetDefaultCommand = DBDefaultQuery.STR_QUERY_ALTER_COLUMN_DROP_DEFAULT;
    private String m_AlterColumnCommand = null;
    private String m_AddIdentityQuery = null;
    private String m_DropIdentityQuery = DBDefaultQuery.STR_QUERY_ALTER_COLUMN_DROP_IDENTITY;;
    private String m_RevokeRoleCommand = DBDefaultQuery.STR_QUERY_REVOKE_ROLE;
    private Object[] m_RenameTableCommands = null;
    private Object[] m_ViewDefinitionCommands = null;
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
    public String enquoteLiteral(String literal)
        throws SQLException
    {
        try {
            literal = getStatement().enquoteLiteral(literal);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e);
        }
        return literal;
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
    public String getAutoIncrementCreation()
    {
        return m_AutoIncrementCreation;
    }

    @Override
    public boolean isIgnoreCurrencyEnabled()
    {
        return m_IgnoreCurrency;
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

    @Override
    public String getRenameColumnQuery() {
        return m_RenameColumnCommand;
    }

    @Override
    public String getCreateTableQuery(String table,
                                      String columns)
    {
        return MessageFormat.format(m_CreateTableCommand, table, columns);
    }

    @Override
    public String getDropTableQuery(String table)
    {
        return MessageFormat.format(m_DropTableCommand, table);
    }

    @Override
    public String getAddColumnQuery(String table, String column)
    {
        return MessageFormat.format(m_AddColumnCommand, table, column);
    }

    @Override
    public String getDropColumnQuery(String table, String column)
    {
        return MessageFormat.format(m_DropColumnCommand, table, column);
    }

    @Override
    public String getAddConstraintQuery(int type) {
        String query = null;
        switch (type) {
        case KeyType.PRIMARY:
            query = m_AddPrimaryKeyCommand;
            break;
        case KeyType.FOREIGN:
            query = m_AddForeignKeyCommand;
            break;
        case KeyType.UNIQUE:
            query = m_AddIndexCommand;
            break;
        }
        return query;
    }

    @Override
    public String getDropConstraintQuery(int type) {
        String query = null;
        switch (type) {
        case KeyType.PRIMARY:
            query = m_DropPrimaryKeyCommand;
            break;
        case KeyType.FOREIGN:
            query = m_DropConstraintCommand;
            break;
        case KeyType.UNIQUE:
            query = m_DropIndexCommand;
            break;
        }
        return query;
    }

    @Override
    public boolean hasAlterColumnQuery() {
        return m_AlterColumnCommand != null;
    }

    @Override
    public String getColumnResetDefaultQuery() {
        return m_ColumnResetDefaultCommand;
    }

    @Override
    public String getAlterColumnQuery() {
        return m_AlterColumnCommand;
    }

    @Override
    public String getRevokeRoleQuery()
    {
        return m_RevokeRoleCommand;
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
        return m_SupportsRenameView;
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

    public boolean isEnhanced()
    {
        return m_enhanced;
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
    public String[] getTableTypes()
    {
        String[] types = null;
        if (!m_showsystem) {
            types = new String[]{"TABLE", "VIEW"};
        }
        return types;
    }

    @Override
    public String[] getViewTypes()
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
                                     ColumnBase<?> column)
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
    public void setConnection(ResourceBasedEventLogger logger,
                              XHierarchicalNameAccess config1,
                              XHierarchicalNameAccess config2,
                              final boolean enhanced,
                              final String location,
                              final PropertyValue[] infos,
                              String level)
        throws java.sql.SQLException
    {
        m_infos = infos;

        m_SupportsAlterIdentity = getDriverBooleanProperty(config1, "SupportsAlterIdentity", m_SupportsAlterIdentity);
        m_SupportsRenameView = getDriverBooleanProperty(config1, "SupportsRenameView", m_SupportsRenameView);
        m_SupportsColumnDescription = getDriverBooleanProperty(config1, "SupportsColumnDescription", m_SupportsColumnDescription);
        m_SupportsAlterPrimaryKey = getDriverBooleanProperty(config1, "SupportsAlterPrimaryKey", m_SupportsAlterPrimaryKey);
        
        m_SupportsAlterColumnType = getDriverBooleanProperty(config1, "SupportsAlterColumnType", m_SupportsAlterColumnType);
        m_SupportsAlterColumnProperty = getDriverBooleanProperty(config1, "SupportsAlterColumnProperty", m_SupportsAlterColumnProperty);
        m_CreateTableCommand = getDriverStringProperty(config1, "CreateTableCommand", m_CreateTableCommand);
        m_DropTableCommand = getDriverStringProperty(config1, "DropTableCommand", m_DropTableCommand);
        m_AddColumnCommand = getDriverStringProperty(config1, "AddColumnCommand", m_AddColumnCommand);
        m_DropColumnCommand = getDriverStringProperty(config1, "DropColumnCommand", m_DropColumnCommand);
        m_AlterColumnCommand = getDriverStringProperty(config1, "AlterColumnCommand", m_AlterColumnCommand);

        m_AddPrimaryKeyCommand = getDriverStringProperty(config1, "AddPrimaryKeyCommand", m_AddPrimaryKeyCommand);
        m_AddForeignKeyCommand = getDriverStringProperty(config1, "AddForeignKeyCommand", m_AddForeignKeyCommand);
        m_AddIndexCommand = getDriverStringProperty(config1, "AddIndexCommand", m_AddIndexCommand);
        m_DropPrimaryKeyCommand = getDriverStringProperty(config1, "DropPrimaryKeyCommand", m_DropPrimaryKeyCommand);
        m_DropConstraintCommand = getDriverStringProperty(config1, "DropConstraintCommand", m_DropConstraintCommand);
        m_DropIndexCommand = getDriverStringProperty(config1, "DropIndexCommand", m_DropIndexCommand);
        m_AddIdentityQuery = getDriverStringProperty(config1, "AddIdentityCommand", m_AddIdentityQuery);
        m_DropIdentityQuery = getDriverStringProperty(config1, "DropIdentityCommand", m_DropIdentityQuery);

        m_TableDescriptionCommand = getDriverStringProperty(config1, "TableDescriptionCommand", m_TableDescriptionCommand);
        m_ColumnDescriptionCommand = getDriverStringProperty(config1, "ColumnDescriptionCommand", m_ColumnDescriptionCommand);
        m_RenameColumnCommand = getDriverStringProperty(config1, "RenameColumnCommand", m_RenameColumnCommand);

        m_RevokeRoleCommand = getDriverStringProperty(config1, "RevokeRoleCommand", m_RevokeRoleCommand);

        m_AlterViewCommands = getDriverObjectProperty(config1, "AlterViewCommands", m_AlterViewCommands);
        m_ViewDefinitionCommands = getDriverObjectProperty(config1, "ViewDefinitionCommands", m_ViewDefinitionCommands);
        m_RenameTableCommands = getDriverObjectProperty(config1, "RenameTableCommands", m_RenameTableCommands);

        m_TypeInfoSettings = getDriverObjectProperty(config1, "TypeInfoSettings", m_TypeInfoSettings);

        m_logger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);
        m_showsystem = UnoHelper.getConfigurationOption(config2, "ShowSystemTable", false);
        m_usebookmark = UnoHelper.getConfigurationOption(config2, "UseBookmark", true);
        m_enhanced = enhanced;
        String url = getConnectionUrl(location, level);
        java.sql.Connection connection = DriverManager.getConnection(url, getJavaConnectionProperties(infos));
        java.sql.DatabaseMetaData metadata = connection.getMetaData();

        m_CatalogsInTableDefinitions = metadata.supportsCatalogsInTableDefinitions();
        m_SchemasInTableDefinitions = metadata.supportsSchemasInTableDefinitions();
        m_CatalogsInIndexDefinitions = metadata.supportsCatalogsInIndexDefinitions();
        m_SchemasInIndexDefinitions = metadata.supportsSchemasInIndexDefinitions();
        m_CatalogsInDataManipulation = metadata.supportsCatalogsInDataManipulation();
        m_SchemasInDataManipulation = metadata.supportsSchemasInDataManipulation();
        m_CatalogsInProcedureCalls = metadata.supportsCatalogsInProcedureCalls();
        m_SchemasInProcedureCalls = metadata.supportsSchemasInProcedureCalls();
        m_CatalogsInPrivilegeDefinitions = metadata.supportsCatalogsInPrivilegeDefinitions();
        m_SchemasInPrivilegeDefinitions = metadata.supportsSchemasInPrivilegeDefinitions();

        m_SupportsTransactions = metadata.supportsTransactions();
        m_IsCatalogAtStart = metadata.isCatalogAtStart();
        m_CatalogSeparator = metadata.getCatalogSeparator();
        m_IdentifierQuoteString = metadata.getIdentifierQuoteString();
        m_IsResultSetUpdatable = metadata.supportsResultSetConcurrency(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                       java.sql.ResultSet.CONCUR_UPDATABLE);
        setInfoProperties(infos, metadata);
        m_statement = connection.createStatement();
    }

    // Driver properties cache data
    public boolean supportsColumnDescription() {
        return m_SupportsColumnDescription;
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
    public boolean supportsTableDescription() {
        return false;
    }
    public boolean isAutoRetrievingEnabled() {
        return m_IsAutoRetrievingEnabled;
    }
    public boolean isAutoIncrementIsPrimaryKey() {
        return m_IsAutoIncrementIsPrimaryKey;
    }
    public Boolean supportsAlterColumnType() {
        return m_SupportsAlterColumnType;
    }
    public Boolean supportsAlterColumnProperty() {
        return m_SupportsAlterColumnProperty;
    }
    public Boolean supportsAlterPrimaryKey() {
        return m_SupportsAlterPrimaryKey;
    }
    public String getAutoRetrievingStatement() {
        return m_AutoRetrievingStatement;
    }
    public boolean ignoreDriverPrivileges() {
        return m_IgnoreDriverPrivileges;
    }
    public boolean addIndexAppendix() {
        return m_AddIndexAppendix;
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
                info.Name.equals("IgnoreDriverPrivileges") ||
                info.Name.equals("ImplicitCatalogRestriction") ||
                info.Name.equals("ImplicitSchemaRestriction") ||
                info.Name.equals("SupportsTableCreation") ||
                info.Name.equals("UseJava") ||
                info.Name.equals("Authentication") ||
                info.Name.equals("PreferDosLikeLineEnds") ||
                info.Name.equals("PrimaryKeySupport") ||
                info.Name.equals("RespectDriverResultSetType") ||
                info.Name.equals("DriverLoggerLevel") ||
                info.Name.equals("InMemoryDataBase") ||
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

    @Override
    public CustomColumn[] getTypeInfoRow(CustomColumn[] columns)
        throws SQLException
    {
        Object [] typeinfo = getTypeInfoSettings();
        if (typeinfo == null) {
            return columns;
        }
        if (m_typeinforows == null) {
            m_typeinforows = new CustomTypeInfo(typeinfo);
        }
        return m_typeinforows.getTypeInfoRow(columns);
    }

    private void setInfoProperties(final PropertyValue[] infos,
                                   java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException
    {
        boolean autoretrieving = getAutoRetrieving(metadata, infos);
        for (PropertyValue info : infos) {
            switch (info.Name) {
            case "AutoIncrementCreation":
                m_AutoIncrementCreation = (String) info.Value;
                break;
            case "IgnoreDriverPrivileges":
                m_IgnoreDriverPrivileges = (boolean) info.Value;
                break;
            case "IgnoreCurrency":
                m_IgnoreCurrency = (boolean) info.Value;
                break;
            case "AddIndexAppendix":
                m_AddIndexAppendix = (boolean) info.Value;
                break;
            case "AutoRetrievingStatement":
                if (autoretrieving) {
                    m_AutoRetrievingStatement = (String) info.Value;
                }
                break;
            case "IsAutoRetrievingEnabled":
                if (autoretrieving) {
                    m_IsAutoRetrievingEnabled = (boolean) info.Value;
                }
                break;
            }
        }
    }

    private boolean getAutoRetrieving(java.sql.DatabaseMetaData metadata,
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

    @Override
    public boolean hasAddIdentityQuery() {
        return m_AddIdentityQuery != null;
    }

    @Override
    public String getAddIdentityQuery() {
        return m_AddIdentityQuery;
    }

    @Override
    public boolean supportsAlterIdentity() {
        return m_SupportsAlterIdentity;
    }

    @Override
    public String getDropIdentityQuery() {
        return m_DropIdentityQuery;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions()
    {
        return m_CatalogsInTableDefinitions;
    }
    @Override
    public boolean supportsSchemasInTableDefinitions()
    {
        return m_SchemasInTableDefinitions;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions()
    {
        return m_CatalogsInIndexDefinitions;
    }
    @Override
    public boolean supportsSchemasInIndexDefinitions()
    {
        return m_SchemasInIndexDefinitions;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation()
    {
        return m_CatalogsInDataManipulation;
    }
    @Override
    public boolean supportsSchemasInDataManipulation()
    {
        return m_SchemasInDataManipulation;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls()
    {
        return m_CatalogsInProcedureCalls;
    }
    @Override
    public boolean supportsSchemasInProcedureCalls()
    {
        return m_SchemasInProcedureCalls;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions()
    {
        return m_CatalogsInPrivilegeDefinitions;
    }
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions()
    {
        return m_SchemasInPrivilegeDefinitions;
    }

    @Override
    public boolean getDriverBooleanProperty(XHierarchicalNameAccess driver, String name, boolean value)
    {
        String property = "Installed/" + getSubProtocol() + ":*/MetaData/" + name + "/Value";
        try {
            if (driver.hasByHierarchicalName(property)) {
                value = (boolean) driver.getByHierarchicalName(property);
            }
        }
        catch (NoSuchElementException e) { }
        return value;
    }

    @Override
    public String getDriverStringProperty(XHierarchicalNameAccess driver, String name, String value)
    {
        String property = "Installed/" + getSubProtocol() + ":*/MetaData/" + name + "/Value";
        try {
            if (driver.hasByHierarchicalName(property)) {
                value = (String) driver.getByHierarchicalName(property);
            }
        }
        catch (NoSuchElementException e) { }
        return value;
    }


    @Override
    public Object[] getDriverObjectProperty(XHierarchicalNameAccess driver, String name, Object[] value)
    {
        String property = "Installed/" + getSubProtocol() + ":*/MetaData/" + name + "/Value";
        try {
            if (driver.hasByHierarchicalName(property)) {
                value = (Object[]) driver.getByHierarchicalName(property);
            }
        }
        catch (NoSuchElementException e) { }
        return value;
    }


}