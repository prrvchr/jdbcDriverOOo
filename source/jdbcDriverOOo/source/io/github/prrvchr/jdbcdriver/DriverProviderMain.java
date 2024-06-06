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
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.jdbcdriver.helper.DBDefaultQuery;
import io.github.prrvchr.jdbcdriver.helper.DBException;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.metadata.TableTypesResultSet;
import io.github.prrvchr.jdbcdriver.metadata.TypeInfoResultSet;
import io.github.prrvchr.jdbcdriver.metadata.TypeInfoRows;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;
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
    protected boolean m_forcesql;
    private String m_separator = ", ";
    private String m_SyncProvider = "io.github.prrvchr.rowset.providers.RIOptimisticProvider";

    private Boolean m_InsertVisibleInsensitive;
    private Boolean m_InsertVisibleSensitive;
    private Boolean m_InsertVisibleForwardonly;
    private Boolean m_DeleteVisibleInsensitive;
    private Boolean m_DeleteVisibleSensitive;
    private Boolean m_DeleteVisibleForwardonly;
    private Boolean m_UpdateVisibleInsensitive;
    private Boolean m_UpdateVisibleSensitive;
    private Boolean m_UpdateVisibleForwardonly;

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
    private String m_AddUniqueCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_UNIQUE;
    private String m_DropPrimaryKeyCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_PRIMARY_KEY;
    private String m_DropConstraintCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_CONSTRAINT;
    private String m_DropIndexCommand = DBDefaultQuery.STR_QUERY_ALTER_TABLE_DROP_INDEX;
    private String m_TableDescriptionCommand = DBDefaultQuery.STR_QUERY_ADD_TABLE_COMMENT;;
    private String m_ColumnDescriptionCommand = DBDefaultQuery.STR_QUERY_ADD_COLUMN_COMMENT;
    private Object[] m_AlterViewCommands = {DBDefaultQuery.STR_QUERY_ALTER_VIEW};
    private String m_ColumnResetDefaultCommand = DBDefaultQuery.STR_QUERY_ALTER_COLUMN_DROP_DEFAULT;
    private String m_AlterUserCommand = DBDefaultQuery.STR_QUERY_ALTER_USER;

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
    private Object[] m_SystemVersioningCommands = null;
    private List<String> m_PrivilegeNames = null;
    private List<Integer> m_PrivilegeValues = null;
    private TypeInfoRows m_TypeInfoRows = null;
    private Map<String, String> m_TableTypes = null;
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
    public boolean useBookmark()
    {
        return m_usebookmark;
    }

    @Override
    public boolean forceSQL()
    {
        return m_forcesql;
    }

    @Override
    public boolean supportsSystemVersioning()
    {
        return m_SystemVersioningCommands != null && m_SystemVersioningCommands.length > 0;
    }

    @Override
    public String getSyncProvider()
    {
        return m_SyncProvider;
    }

    @Override
    public String getSystemVersioningColumnQuery(List<String> columns)
        throws java.sql.SQLException
    {
        String query = "";
        if (supportsSystemVersioning()) {
            String command = (String) m_SystemVersioningCommands[0];
            query = MessageFormat.format(command, getIdentifiersAsString(columns, true));
        }
        return query;
    }

    private String getIdentifiersAsString(final List<String> identifiers,
                                          boolean quoted)
        throws java.sql.SQLException
    {
        if (quoted) {
            ListIterator<String> it = identifiers.listIterator();
            while (it.hasNext()) {
                it.set(enquoteIdentifier(it.next(), quoted));
            }
        }
        return String.join(m_separator, identifiers);
    }

    @Override
    public String enquoteIdentifier(String identifier)
        throws java.sql.SQLException
    {
        return enquoteIdentifier(identifier, true);
    }

    @Override
    public String enquoteIdentifier(String identifier, boolean always)
        throws java.sql.SQLException
    {
        // XXX: enquoteIdentifier don't support blank string (ie: catalog or schema name can be empty)
        if (always && !identifier.isBlank()) {
            identifier = getStatement().enquoteIdentifier(identifier, always);
        }
        return identifier;
    }

    private String getSystemVersioningTableQuery()
    {
        String query = "";
        if (supportsSystemVersioning() && m_SystemVersioningCommands.length > 1) {
            query = " " + (String) m_SystemVersioningCommands[1];
        }
        return query;
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
    public String getAddIndexQuery(boolean unique, Object... args) {
        String command = unique ? m_AddUniqueCommand : m_AddIndexCommand;
        return DBTools.formatSQLQuery(command, args);
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
    public String getCreateTableQuery(String type,
                                      String table,
                                      String columns,
                                      boolean versioning)
    {   String query = versioning ? getSystemVersioningTableQuery() : "";
        return DBTools.formatSQLQuery(m_CreateTableCommand, type, table, columns, query);
    }

    @Override
    public String getDropTableQuery(String table)
    {
        return DBTools.formatSQLQuery(m_DropTableCommand, table);
    }

    @Override
    public String getAlterUserQuery(String user, String password)
    {
        return DBTools.formatSQLQuery(m_AlterUserCommand, user, password);
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
        String sql = command.replaceAll("\\s", "");
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
        if (m_TableTypes != null && m_TableTypes.containsKey(type)) {
            return m_TableTypes.get(type);
        }
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
    public void setConnection(XInterface source,
                              ResourceBasedEventLogger logger,
                              final boolean enhanced,
                              XHierarchicalNameAccess config1,
                              XHierarchicalNameAccess config2,
                              final String location,
                              final PropertyValue[] infos,
                              String level)
        throws SQLException
    {
        try {
            System.out.println("DriverProvider.setConnection() 1");
            m_infos = infos;
            // XXX: SQLCommandSuffix is needed for building query from sql command.
            m_SQLCommandSuffix = getDriverStringProperty(config1, "SQLCommandSuffix", m_SQLCommandSuffix);

            m_InsertVisibleInsensitive = getDriverBooleanProperty(config1, "InsertVisibleInsensitive", null);
            m_InsertVisibleSensitive = getDriverBooleanProperty(config1, "InsertVisibleSensitive", null);
            m_InsertVisibleForwardonly = getDriverBooleanProperty(config1, "InsertVisibleForwardonly", null);
            m_DeleteVisibleInsensitive = getDriverBooleanProperty(config1, "DeleteVisibleInsensitive", null);
            m_DeleteVisibleSensitive = getDriverBooleanProperty(config1, "DeleteVisibleSensitive", null);
            m_DeleteVisibleForwardonly = getDriverBooleanProperty(config1, "DeleteVisibleForwardonly", null);
            m_UpdateVisibleInsensitive = getDriverBooleanProperty(config1, "UpdateVisibleInsensitive", null);
            m_UpdateVisibleSensitive = getDriverBooleanProperty(config1, "UpdateVisibleSensitive", null);
            m_UpdateVisibleForwardonly = getDriverBooleanProperty(config1, "UpdateVisibleForwardonly", null);

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
            m_AddUniqueCommand = getDriverCommandProperty(config1, "AddUniqueCommand", m_AddUniqueCommand);
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
            m_AlterUserCommand = getDriverCommandProperty(config1, "AlterUserCommand", m_AlterUserCommand);

            m_SystemVersioningCommands = getDriverProperties(config1, "SystemVersioningCommands", m_SystemVersioningCommands);
            m_AlterViewCommands = getDriverCommandsProperty(config1, "AlterViewCommands", m_AlterViewCommands);
            m_RenameTableCommands = getDriverCommandsProperty(config1, "RenameTableCommands", m_RenameTableCommands);

            m_ViewDefinitionCommands = getDriverParametricCommandsProperty(config1, "ViewDefinitionCommands");
            m_TablePrivilegesCommands = getDriverParametricCommandsProperty(config1, "TablePrivilegesCommand");
            m_GrantablePrivilegesCommands = getDriverParametricCommandsProperty(config1, "GrantablePrivilegesCommand");

            m_SupportedServices = getSupportedService(config1, "SupportedConnectionServices");

            m_logger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);
            m_showsystem = UnoHelper.getConfigurationOption(config2, "ShowSystemTable", false);
            m_usebookmark = UnoHelper.getConfigurationOption(config2, "UseBookmark", true);
            m_forcesql = UnoHelper.getConfigurationOption(config2, "ForceSQL", false);
            m_enhanced = enhanced;
            String url = getConnectionUrl(location, level);
            java.sql.Connection connection = DriverManager.getConnection(url, getJdbcConnectionProperties(infos));
            System.out.println("DriverProvider.setConnection() 2");
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

            m_SupportsTransactions = metadata.supportsTransactions() && getDriverBooleanProperty(config1, "SupportTransaction", true);
            m_IsCatalogAtStart = metadata.isCatalogAtStart();
            m_CatalogSeparator = metadata.getCatalogSeparator();
            m_IdentifierQuoteString = metadata.getIdentifierQuoteString();
            setPrivileges(setInfoProperties(infos, metadata));

            // XXX: We do not keep the connection but the statement
            // XXX: which allows us to find the connection if necessary.
            m_statement = connection.createStatement();
            System.out.println("DriverProvider.setConnection() 3");
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_NO_SYSTEM_CONNECTION;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, location);
            throw DBException.getSQLException(msg, source, StandardSQLState.SQL_UNABLE_TO_CONNECT, e);
        }
    }

    @Override
    public boolean isInsertVisible(int rstype)
        throws java.sql.SQLException
    {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_FORWARD_ONLY && m_InsertVisibleForwardonly != null) {
            visible = m_InsertVisibleForwardonly;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && m_InsertVisibleInsensitive != null) {
            visible = m_InsertVisibleInsensitive;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && m_InsertVisibleSensitive != null) {
            visible = m_InsertVisibleSensitive;
        }
        else {
            visible = getConnection().getMetaData().ownInsertsAreVisible(rstype);
        }
        return visible;
    }

    @Override
    public boolean isUpdateVisible(int rstype)
        throws java.sql.SQLException
    {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_FORWARD_ONLY && m_UpdateVisibleForwardonly != null) {
            visible = m_UpdateVisibleForwardonly;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && m_UpdateVisibleInsensitive != null) {
            visible = m_UpdateVisibleInsensitive;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && m_UpdateVisibleSensitive != null) {
            visible = m_UpdateVisibleSensitive;
        }
        else {
            visible = getConnection().getMetaData().ownUpdatesAreVisible(rstype);
        }
        return visible;
    }

    @Override
    public boolean isDeleteVisible(int rstype)
        throws java.sql.SQLException
    {
        boolean visible = false;
        if (rstype == ResultSet.TYPE_FORWARD_ONLY && m_DeleteVisibleForwardonly != null) {
            visible = m_DeleteVisibleForwardonly;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE && m_DeleteVisibleInsensitive != null) {
            visible = m_DeleteVisibleInsensitive;
        }
        else if (rstype == ResultSet.TYPE_SCROLL_SENSITIVE && m_DeleteVisibleSensitive != null) {
            visible = m_DeleteVisibleSensitive;
        }
        else {
            visible = getConnection().getMetaData().ownDeletesAreVisible(rstype);
        }
        return visible;
    }

    @Override
    public void setHoldability(int holdability) {
        try {
            getConnection().setHoldability(holdability);
        }
        catch (java.sql.SQLException e) {
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
    public boolean useBookmarks(boolean use) {
        System.out.println("DriverProvider.useBookmarks() 1 use: " + use + " - UseBookmark: " + m_usebookmark);
        return use && m_usebookmark;
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
    public boolean supportsAlterColumnType() {
        return m_SupportsAlterColumnType;
    }
    @Override
    public boolean supportsAlterColumnProperty() {
        return m_SupportsAlterColumnProperty;
    }
    @Override
    public boolean supportsAlterPrimaryKey() {
        return m_SupportsAlterPrimaryKey;
    }
    @Override
    public boolean supportsAlterForeignKey() {
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
    public boolean hasTableTypesSettings() {
        return m_TableTypes != null;
    }

    @Override
    public java.sql.ResultSet getTableTypesResultSet(java.sql.DatabaseMetaData metadata)
        throws java.sql.SQLException
    {
        java.sql.ResultSet result = metadata.getTableTypes();
        if (m_TableTypes != null) {
            return new TableTypesResultSet(result, m_TableTypes);
        }
        return result;
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
        java.sql.ResultSet result = metadata.getTypeInfo();
        if (m_TypeInfoSettings != null) {
            if (m_TypeInfoRows == null) {
                m_TypeInfoRows = new TypeInfoRows(m_TypeInfoSettings);
            }
            return new TypeInfoResultSet(result, m_TypeInfoRows);
        }
        return result;
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
    public Properties getJdbcConnectionProperties(PropertyValue[] infos)
    {
        Properties properties = new Properties();
        for (PropertyValue info : infos) {
            String property = info.Name;
            if (isLibreOfficeProperty(property) || isInternalProperty(property)) {
                continue;
            }
            System.out.println("DriverProvider.getJdbcConnectionProperties() ********************* Name: " + property);
            properties.setProperty(property, String.format("%s", info.Value));
            System.out.println("DriverProvider.getJdbcConnectionProperties() ********************* Value: " + info.Value);
        }
        return properties;
    }

    private boolean isLibreOfficeProperty(String property)
    {
        // XXX: These are properties used internally by LibreOffice,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        // XXX: see: connectivity/source/drivers/jdbc/tools.cxx createStringPropertyArray()
        return property.equals("JavaDriverClass") ||
               property.equals("JavaDriverClassPath") ||
               property.equals("SystemProperties") ||
               property.equals("CharSet") ||
               property.equals("AppendTableAliasName") ||
               property.equals("AppendTableAliasInSelect") ||
               property.equals("DisplayVersionColumns") ||
               property.equals("GeneratedValues") ||
               property.equals("UseIndexDirectionKeyword") ||
               property.equals("UseKeywordAsBeforeAlias") ||
               property.equals("AddIndexAppendix") ||
               property.equals("FormsCheckRequiredFields") ||
               property.equals("GenerateASBeforeCorrelationName") ||
               property.equals("EscapeDateTime") ||
               property.equals("ParameterNameSubstitution") ||
               property.equals("IsPasswordRequired") ||
               property.equals("IsAutoRetrievingEnabled") ||
               property.equals("AutoRetrievingStatement") ||
               property.equals("UseCatalogInSelect") ||
               property.equals("UseSchemaInSelect") ||
               property.equals("AutoIncrementCreation") ||
               property.equals("Extension") ||
               property.equals("NoNameLengthLimit") ||
               property.equals("EnableSQL92Check") ||
               property.equals("EnableOuterJoinEscape") ||
               property.equals("BooleanComparisonMode") ||
               property.equals("IgnoreCurrency") ||
               property.equals("TypeInfoSettings") ||
               property.equals("IgnoreDriverPrivileges") ||
               property.equals("ImplicitCatalogRestriction") ||
               property.equals("ImplicitSchemaRestriction") ||
               property.equals("SupportsTableCreation") ||
               property.equals("UseJava") ||
               property.equals("Authentication") ||
               property.equals("PreferDosLikeLineEnds") ||
               property.equals("PrimaryKeySupport") ||
               property.equals("RespectDriverResultSetType");
    }

    private boolean isInternalProperty(String property)
    {
        // XXX: These are properties used internally by jdbcDriverOOo,
        // XXX: and should not be passed to the JDBC driver
        // XXX: (which probably does not know anything about them anyway).
        return property.equals("TablePrivilegesSettings") ||
               property.equals("RowVersionCreation") ||
               property.equals("DriverLoggerLevel") ||
               property.equals("InMemoryDataBase") ||
               property.equals("Type") ||
               property.equals("Url") ||
               property.equals("ConnectionService");
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
            case "TableTypesSettings":
                parseTableTypes((Object[]) info.Value);
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

    private void parseTableTypes(Object[] infos)
    {
        Map<String, String> types = null;
        try {
            types = new TreeMap<>();
            int count = DBTools.getEvenLength(infos.length);
            for (int i = 0; i < count; i += 2) {
                types.put (infos[i].toString(), infos[i + 1].toString());
            }
            m_TableTypes = types;
        }
        catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
        System.out.println("DriverProvider.getAutoRetrieving() 1 support: " + support);
        if (support == null) {
            support = metadata.supportsGetGeneratedKeys();
        }
        System.out.println("DriverProvider.getAutoRetrieving() 2 support: " + metadata.supportsGetGeneratedKeys());
        System.out.println("DriverProvider.getAutoRetrieving() 3 support: " + support);
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
    public Boolean getDriverBooleanProperty(XHierarchicalNameAccess driver, String name, Boolean value)
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
        return DriverProvider.getDriverMetaDataInfo(getSubProtocol(), name);
    }

}