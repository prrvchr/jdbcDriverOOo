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
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.jdbcdriver.helper.DBDefaultQuery;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.resultset.TypeInfoResultSet;
import io.github.prrvchr.jdbcdriver.resultset.TypeInfoRows;
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
    private XOfficeDatabaseDocument m_document = null;
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
    private String m_SQLCommandSuffix = "";
    private String m_CatalogSeparator = "";
    private String m_IdentifierQuoteString = "";
    private String m_AutoIncrementCreation = "";
    private boolean m_AddIndexAppendix = false;
    private boolean m_AutoIncrementIsPrimaryKey = true;
    private boolean m_SupportsAlterColumnType = true;
    private boolean m_SupportsAlterColumnProperty = true;
    private boolean m_SupportsAlterPrimaryKey = true;
    private boolean m_SupportsAlterForeignKey = true;
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
    private String m_AddIdentityCommand = null;
    private String m_DropIdentityCommand = DBDefaultQuery.STR_QUERY_ALTER_COLUMN_DROP_IDENTITY;;
    private String m_CreateUserCommand = DBDefaultQuery.STR_QUERY_CREATE_USER;
    private String m_GetUsersCommand = null;
    private String m_GetGroupsCommand = null;
    private String m_GetUserGroupsCommand = null;
    private String m_GetGroupUsersCommand = null;
    private String m_GetGroupRolesCommand = null;
    private String m_GrantRoleCommand = DBDefaultQuery.STR_QUERY_GRANT_ROLE;
    private String m_RevokeRoleCommand = DBDefaultQuery.STR_QUERY_REVOKE_ROLE;
    private String m_GrantPrivilegesCommand = DBDefaultQuery.STR_QUERY_GRANT_PRIVILEGE;
    private String m_RevokePrivilegesCommand = DBDefaultQuery.STR_QUERY_REVOKE_PRIVILEGE;
    private Object[] m_TablePrivilegesCommands = null;
    private Object[] m_GrantablePrivilegesCommands = null;
    private Object[] m_RenameTableCommands = null;
    private Object[] m_ViewDefinitionCommands = null;
    private Object[] m_TypeInfoSettings = null;
    private List<String> m_PrivilegeNames = null;
    private List<Integer> m_PrivilegeValues = null;
    private TypeInfoRows m_TypeInfoRows = null;
    private List<ConnectionService> m_SupportedServices = null;

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
        throws java.sql.SQLException
    {
        return getStatement().enquoteLiteral(literal);
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
    public boolean hasDocument()
    {
        return m_document != null;
    }

    public boolean supportService(ConnectionService service)
    {
        return m_SupportedServices.contains(service);
    }

    @Override
    public XOfficeDatabaseDocument getDocument()
    {
        return m_document;
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
        return DBTools.formatSQLQuery(m_ColumnDescriptionCommand, column, description);
    }

    @Override
    public String getTableDescriptionQuery(String table, String description) {
        return DBTools.formatSQLQuery(m_TableDescriptionCommand, table, description);
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
        return DBTools.formatSQLQuery(m_CreateTableCommand, table, columns);
    }

    @Override
    public String getDropTableQuery(String table)
    {
        return DBTools.formatSQLQuery(m_DropTableCommand, table);
    }

    @Override
    public String getAddColumnQuery(String table, String column)
    {
        return DBTools.formatSQLQuery(m_AddColumnCommand, table, column);
    }

    @Override
    public String getDropColumnQuery(String table, String column)
    {
        return DBTools.formatSQLQuery(m_DropColumnCommand, table, column);
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
    public String getGrantRoleQuery(Object... arguments)
    {
        return DBTools.formatSQLQuery(m_GrantRoleCommand, arguments);
    }

    @Override
    public String getRevokeRoleQuery(Object... arguments)
    {
        return DBTools.formatSQLQuery(m_RevokeRoleCommand, arguments);
    }

    @Override
    public String getGrantPrivilegesQuery(Object... arguments)
    {
        return DBTools.formatSQLQuery(m_GrantPrivilegesCommand, arguments);
    }

    @Override
    public String getRevokePrivilegesQuery(Object... arguments)
    {
        return DBTools.formatSQLQuery(m_RevokePrivilegesCommand, arguments);
    }

    @Override
    public String getTablePrivilegesQuery(List<Integer[]> positions)
    {
        String query = null;
        if (m_TablePrivilegesCommands != null) {
            query = getDDLQueriesCommand(m_TablePrivilegesCommands, positions, false).get(0);
        }
        return query;
    }

    @Override
    public String getGrantablePrivilegesQuery(List<Integer[]> positions)
    {
        String query = null;
        if (m_GrantablePrivilegesCommands != null) {
            query = getDDLQueriesCommand(m_GrantablePrivilegesCommands, positions, false).get(0);
        }
        return query;
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
                queries.add(DBTools.formatSQLQuery(command, arguments));
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

    @Override
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

    @Override
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
    public String getUsersQuery()
    {
        return m_GetUsersCommand;
    }

    @Override
    public String getGroupsQuery()
    {
        return m_GetGroupsCommand;
    }

    @Override
    public String getGroupUsersQuery()
    {
        return m_GetGroupUsersCommand;
    }

    @Override
    public String getRoleGroupsQuery(boolean isrole)
    {
        String query = null;
        if (isrole) {
            query = m_GetGroupRolesCommand;
        }
        else {
            query = m_GetUserGroupsCommand;
        }
        return query;
    }

    @Override
    public String[] getPrivileges()
    {
        return m_PrivilegeNames.toArray(new String[0]);
    }

    @Override
    public int getPrivileges(List<String> privileges)
    {
        int flags = 0;
        for (String privilege : privileges) {
            flags |= getPrivilege(privilege);
        }
        return flags;
    }

    public boolean hasPrivilege(String privilege) {
        return m_PrivilegeNames.contains(privilege);
    }

    @Override
    public int getPrivilege(String privilege)
    {
        int flag = 0;
        if (m_PrivilegeNames.contains(privilege)) {
            flag = m_PrivilegeValues.get(m_PrivilegeNames.indexOf(privilege));
        }
        return flag;
    }

    @Override
    public int getMockPrivileges()
    {
        int privileges = 0;
        for (Integer value : m_PrivilegeValues) {
            privileges += value;
        }
        return privileges;
    }

    @Override
    public String[] getPrivileges(int privilege)
    {
        List<String> flags = new ArrayList<>();
        for (int value: m_PrivilegeValues) {
            if ((privilege & value) == value) {
                flags.add(m_PrivilegeNames.get(m_PrivilegeValues.indexOf(value)));
            }
        }
        return flags.toArray(new String[0]);
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
        try {
        m_infos = infos;
        // XXX: SQLCommandSuffix is needed for building query from sql command.
        m_SQLCommandSuffix = getDriverStringProperty(config1, "SQLCommandSuffix", m_SQLCommandSuffix);

        m_AutoIncrementIsPrimaryKey = getDriverBooleanProperty(config1, "AutoIncrementIsPrimaryKey", m_AutoIncrementIsPrimaryKey);
        m_SupportsAlterIdentity = getDriverBooleanProperty(config1, "SupportsAlterIdentity", m_SupportsAlterIdentity);
        m_SupportsRenameView = getDriverBooleanProperty(config1, "SupportsRenameView", m_SupportsRenameView);
        m_SupportsColumnDescription = getDriverBooleanProperty(config1, "SupportsColumnDescription", m_SupportsColumnDescription);
        m_SupportsAlterPrimaryKey = getDriverBooleanProperty(config1, "SupportsAlterPrimaryKey", m_SupportsAlterPrimaryKey);
        m_SupportsAlterForeignKey = getDriverBooleanProperty(config1, "SupportsAlterForeignKey", m_SupportsAlterForeignKey);

        m_SupportsAlterColumnType = getDriverBooleanProperty(config1, "SupportsAlterColumnType", m_SupportsAlterColumnType);
        m_SupportsAlterColumnProperty = getDriverBooleanProperty(config1, "SupportsAlterColumnProperty", m_SupportsAlterColumnProperty);

        m_CreateTableCommand = getDriverCommandProperty(config1, "CreateTableCommand", m_CreateTableCommand);
        m_DropTableCommand = getDriverCommandProperty(config1, "DropTableCommand", m_DropTableCommand);
        m_AddColumnCommand = getDriverCommandProperty(config1, "AddColumnCommand", m_AddColumnCommand);
        m_DropColumnCommand = getDriverCommandProperty(config1, "DropColumnCommand", m_DropColumnCommand);
        m_AlterColumnCommand = getDriverCommandProperty(config1, "AlterColumnCommand", m_AlterColumnCommand);
        m_GetUsersCommand = getDriverCommandProperty(config1, "GetUsersCommand", m_GetUsersCommand);
        m_GetGroupsCommand = getDriverCommandProperty(config1, "GetGroupsCommand", m_GetGroupsCommand);
        m_GetUserGroupsCommand = getDriverCommandProperty(config1, "GetUserGroupsCommand", m_GetUserGroupsCommand);
        m_GetGroupUsersCommand = getDriverCommandProperty(config1, "GetGroupUsersCommand", m_GetGroupUsersCommand);
        m_GetGroupRolesCommand = getDriverCommandProperty(config1, "GetGroupRolesCommand", m_GetGroupRolesCommand);
        m_AddPrimaryKeyCommand = getDriverCommandProperty(config1, "AddPrimaryKeyCommand", m_AddPrimaryKeyCommand);
        m_AddForeignKeyCommand = getDriverCommandProperty(config1, "AddForeignKeyCommand", m_AddForeignKeyCommand);
        m_AddIndexCommand = getDriverCommandProperty(config1, "AddIndexCommand", m_AddIndexCommand);
        m_DropPrimaryKeyCommand = getDriverCommandProperty(config1, "DropPrimaryKeyCommand", m_DropPrimaryKeyCommand);
        m_DropConstraintCommand = getDriverCommandProperty(config1, "DropConstraintCommand", m_DropConstraintCommand);
        m_AddIdentityCommand = getDriverCommandProperty(config1, "AddIdentityCommand", m_AddIdentityCommand);
        m_DropIdentityCommand = getDriverCommandProperty(config1, "DropIdentityCommand", m_DropIdentityCommand);
        m_TableDescriptionCommand = getDriverCommandProperty(config1, "TableDescriptionCommand", m_TableDescriptionCommand);
        m_ColumnDescriptionCommand = getDriverCommandProperty(config1, "ColumnDescriptionCommand", m_ColumnDescriptionCommand);
        m_CreateUserCommand = getDriverCommandProperty(config1, "CreateUserCommand", m_CreateUserCommand);
        m_GrantPrivilegesCommand = getDriverCommandProperty(config1, "GrantPrivilegesCommand", m_GrantPrivilegesCommand);
        m_RevokePrivilegesCommand = getDriverCommandProperty(config1, "RevokePrivilegesCommand", m_RevokePrivilegesCommand);
        m_GrantRoleCommand = getDriverCommandProperty(config1, "GrantRoleCommand", m_GrantRoleCommand);
        m_RevokeRoleCommand = getDriverCommandProperty(config1, "RevokeRoleCommand", m_RevokeRoleCommand);

        m_AlterViewCommands = getDriverCommandsProperty(config1, "AlterViewCommands", m_AlterViewCommands);
        m_RenameTableCommands = getDriverCommandsProperty(config1, "RenameTableCommands", m_RenameTableCommands);

        m_ViewDefinitionCommands = getDriverParametricCommandsProperty(config1, "ViewDefinitionCommands");
        m_TablePrivilegesCommands = getDriverParametricCommandsProperty(config1, "TablePrivilegesCommand");
        m_GrantablePrivilegesCommands = getDriverParametricCommandsProperty(config1, "GrantablePrivilegesCommand");

        m_SupportedServices = getSupportedService(config1, "SupportedConnectionServices");

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
        setPrivileges(setInfoProperties(infos, metadata));

        m_statement = connection.createStatement();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Driver properties cache data
    @Override
    public boolean supportsColumnDescription() {
        return m_SupportsColumnDescription;
    }

    // DatabaseMetadata cache data
    @Override
    public boolean supportsTransactions() {
        return m_SupportsTransactions;
    }
    @Override
    public boolean isCatalogAtStart() {
        return m_IsCatalogAtStart;
    }
    @Override
    public boolean isResultSetUpdatable() {
        return m_IsResultSetUpdatable;
    }
    @Override
    public String getCatalogSeparator() {
        return m_CatalogSeparator;
    }
    @Override
    public String getIdentifierQuoteString() {
        return m_IdentifierQuoteString;
    }

    // connection infos cache data
    @Override
    public boolean supportsTableDescription() {
        return false;
    }
    @Override
    public boolean isAutoRetrievingEnabled() {
        return m_IsAutoRetrievingEnabled;
    }
    @Override
    public boolean isAutoIncrementIsPrimaryKey() {
        return m_AutoIncrementIsPrimaryKey;
    }
    @Override
    public Boolean supportsAlterColumnType() {
        return m_SupportsAlterColumnType;
    }
    @Override
    public Boolean supportsAlterColumnProperty() {
        return m_SupportsAlterColumnProperty;
    }
    @Override
    public Boolean supportsAlterPrimaryKey() {
        return m_SupportsAlterPrimaryKey;
    }
    @Override
    public Boolean supportsAlterForeignKey() {
        return m_SupportsAlterForeignKey;
    }
    @Override
    public String getAutoRetrievingStatement() {
        return m_AutoRetrievingStatement;
    }
    @Override
    public boolean ignoreDriverPrivileges() {
        return m_IgnoreDriverPrivileges;
    }
    @Override
    public boolean addIndexAppendix() {
        return m_AddIndexAppendix;
    }

    @Override
    public java.sql.ResultSet getTypeInfoResultSet()
        throws java.sql.SQLException
    {
        return getTypeInfoResultSet(getConnection().getMetaData());
    }

    @Override
    public java.sql.ResultSet getTypeInfoResultSet(java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException
    {
        if (m_TypeInfoSettings != null) {
            if (m_TypeInfoRows == null) {
                m_TypeInfoRows = new TypeInfoRows(m_TypeInfoSettings);
            }
            return new TypeInfoResultSet(metadata, m_TypeInfoRows);
        }
        return metadata.getTypeInfo();
    }

    @Override
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
    @Override
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
        // XXX: These are properties used internally by LibreOffice,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        // XXX: see: connectivity/source/drivers/jdbc/tools.cxx createStringPropertyArray()
        Properties properties = new Properties();
        for (PropertyValue info : infos) {
            if (info.Name.equals("JavaDriverClass") ||
                info.Name.equals("JavaDriverClassPath") ||
                info.Name.equals("SystemProperties") ||
                info.Name.equals("CharSet") ||
                info.Name.equals("AppendTableAliasName") ||
                info.Name.equals("AppendTableAliasInSelect") ||
                info.Name.equals("DisplayVersionColumns") ||
                info.Name.equals("GeneratedValues") ||
                info.Name.equals("UseIndexDirectionKeyword") ||
                info.Name.equals("UseKeywordAsBeforeAlias") ||
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
                info.Name.equals("TablePrivilegesSettings") ||

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

    @Override
    public boolean supportCreateUser()
    {
        return !m_CreateUserCommand.isBlank();
    }

    @Override
    public String getCreateUserQuery()
    {
        return m_CreateUserCommand;
    }

    @Override
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

    private Object[] setInfoProperties(final PropertyValue[] infos,
                                       java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException
    {
        Object[] privileges = null;
        boolean autoretrieving = getAutoRetrieving(metadata, infos);
        for (PropertyValue info : infos) {
            switch (info.Name) {
            case "Document":
                m_document = (XOfficeDatabaseDocument) info.Value;
                break;
            case "TypeInfoSettings":
                m_TypeInfoSettings = (Object[]) info.Value;
                break;
            case "TablePrivilegesSettings":
                privileges = (Object[]) info.Value;
                break;
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
        return privileges;
    }
    private void setPrivileges(Object[] privileges)
    {
        boolean parsed = false;
        if (privileges != null) {
            parsed = parsePrivileges(privileges);
        }
        if (!parsed) {
            setDefaultPrivileges();
        }
    }

    private boolean parsePrivileges(Object[] infos)
    {
        try {
            m_PrivilegeNames = new ArrayList<>();
            m_PrivilegeValues  = new ArrayList<>();
            int count = DBTools.getEvenLength(infos.length);
            for (int i = 0; i < count; i += 2) {
                m_PrivilegeNames.add(infos[i].toString());
                m_PrivilegeValues.add(Integer.parseInt(infos[i + 1].toString()));
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void setDefaultPrivileges()
    {
        m_PrivilegeNames = List.of("SELECT",
                                   "INSERT",
                                   "UPDATE",
                                   "DELETE",
                                   "READ",
                                   "CREATE",
                                   "ALTER",
                                   "REFERENCES",
                                   "DROP");
        m_PrivilegeValues = List.of(Privilege.SELECT,
                                    Privilege.INSERT,
                                    Privilege.UPDATE,
                                    Privilege.DELETE,
                                    Privilege.READ,
                                    Privilege.CREATE,
                                    Privilege.ALTER,
                                    Privilege.REFERENCE,
                                    Privilege.DROP);
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
        return m_AddIdentityCommand != null;
    }

    @Override
    public String getAddIdentityQuery() {
        return m_AddIdentityCommand;
    }

    @Override
    public boolean supportsAlterIdentity() {
        return m_SupportsAlterIdentity;
    }

    @Override
    public String getDropIdentityQuery() {
        return m_DropIdentityCommand;
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
        String property = getPropertyPath(name);
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
        String property = getPropertyPath(name);
        try {
            if (driver.hasByHierarchicalName(property)) {
                value = (String) driver.getByHierarchicalName(property);
            }
        }
        catch (NoSuchElementException e) { }
        return value;
    }

    private List<ConnectionService> getSupportedService(XHierarchicalNameAccess driver,
                                                        String name)
    {
        List<ConnectionService> services = null;
        Object[] supported = getDriverProperties(driver, name, null);
        if (supported != null) {
            services = new ArrayList<>();
            for (Object service: supported) {
                services.add(ConnectionService.fromString(service.toString()));
            }
        }
        else {
            services = Arrays.asList(ConnectionService.CSS_SDBC_CONNECTION,
                                     ConnectionService.CSS_SDBCX_CONNECTION,
                                     ConnectionService.CSS_SDB_CONNECTION);
        }
        System.out.println("DriverProvider.getSupportedService() Service: " + services.size());
        return services;
    }

    private String getDriverCommandProperty(XHierarchicalNameAccess driver,
                                            String name,
                                            String value)
    {
        return getDriverCommandProperty(driver, name, value, false);
    }

    private String getDriverCommandProperty(XHierarchicalNameAccess driver,
                                            String name,
                                            String value,
                                            boolean parametric)
    {
        String property = getPropertyPath(name);
        try {
            if (driver.hasByHierarchicalName(property)) {
                value = (String) driver.getByHierarchicalName(property);
            }
            if (value != null && !value.isBlank() && !parametric && !m_SQLCommandSuffix.isBlank()) {
                value += m_SQLCommandSuffix;
            }
        }
        catch (NoSuchElementException e) { }
        return value;
    }

    @Override
    public String getSQLQuery(String command) {
        if (!m_SQLCommandSuffix.isBlank()) {
            command += m_SQLCommandSuffix;
        }
        return command;
    }

    private Object[] getDriverParametricCommandsProperty(XHierarchicalNameAccess driver,
                                                         String name)
    {
        return getDriverCommandsProperty(driver, name, null, true);
    }

    private Object[] getDriverCommandsProperty(XHierarchicalNameAccess driver,
                                               String name,
                                               Object[] values)
    {
        return getDriverCommandsProperty(driver, name, values, false);
    }


    private Object[] getDriverCommandsProperty(XHierarchicalNameAccess driver,
                                               String name,
                                               Object[] values,
                                               boolean parametric)
    {
        values = getDriverProperties(driver, name, values);
        if (values != null && !m_SQLCommandSuffix.isBlank()) {
            setSQLQueries(values, parametric);
        }
        return values;
    }

    private Object[] getDriverProperties(XHierarchicalNameAccess driver,
                                         String name,
                                         Object[] values)
    {
        String property = getPropertyPath(name);
        if (driver.hasByHierarchicalName(property)) {
            try {
                values = (Object[]) driver.getByHierarchicalName(property);
            }
            catch (NoSuchElementException e) { }
        }
        return values;
    }

    private void setSQLQueries(Object[] queries, boolean parametric)
    {
        // XXX: We need to be able to add a suffix to SQL commands.
        // XXX: This allows us to support drivers requiring a semicolon at the end of each command
        // XXX: while still being able to provide default SQL / DDL commands for these drivers.
        // XXX: If the command is parametric, then only even indexes of the content will be suffixed.
        int step = parametric ? 2 : 1;
        for (int i = 0; i < queries.length; i += step) {
            String value = (String) queries[i];
            // XXX: An blank query can exist in multi-query commands and should be left blank.
            if (!value.isBlank()) {
                queries[i] += m_SQLCommandSuffix;
            }
        }
    }

    private String getPropertyPath(String name) {
        return "Installed/" + getSubProtocol() + ":*/MetaData/" + name + "/Value";
    }

}