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
package io.github.prrvchr.driver.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.driver.helper.DBTools;


public class DDLQuery extends SQLQuery {

    private static final String SUPPORTS_RENAME_VIEW = "SupportsRenameView";
    private static final String SUPPORTS_ALTER_COLUMN_PROPERTIES = "SupportsAlterColumnProperties";
    private static final String SUPPORTS_ALTER_COLUMN_TYPE = "SupportsAlterColumnType";
    private static final String SUPPORTS_ALTER_PRIMARY_KEY = "SupportsAlterPrimaryKey";
    private static final String SUPPORTS_ALTER_FOREIGN_KEY = "SupportsAlterForeignKey";
    private static final String SUPPORTS_ALTER_IDENTITY = "SupportsAlterIdentity";
    private static final String SUPPORTS_TABLE_DESCRIPTION = "SupportsTableDescription";
    private static final String SUPPORTS_COLUMN_DESCRIPTION = "SupportsColumnDescription";

    // java.sql.Statement DDL commands
    private static final String CREATE_TABLE_COMMAND = "CreateTableCommand";
    private static final String DROP_TABLE_COMMAND = "DropTableCommand";
    private static final String CREATE_VIEW_COMMAND = "CreateViewCommand";
    private static final String DROP_VIEW_COMMAND = "DropViewCommand";
    private static final String ADD_COLUMN_COMMAND = "AddColumnCommand";
    private static final String DROP_COLUMN_COMMAND = "DropColumnCommand";
    private static final String ALTER_COLUMN_COMMAND = "AlterColumnCommand";
    private static final String RENAME_COLUMN_COMMAND = "RenameColumnCommand";
    private static final String COLUMN_SET_DATATYPE_COMMAND = "ColumnSetDataTypeCommand";
    private static final String COLUMN_SET_NOTNULL_COMMAND = "ColumnSetNotNullCommand";
    private static final String COLUMN_DROP_NOTNULL_COMMAND = "ColumnDropNotNullCommand";
    private static final String COLUMN_SET_DEFAULT_COMMAND = "ColumnSetDefaultCommand";
    private static final String COLUMN_DROP_DEFAULT_COMMAND = "ColumnDropDefaultCommand";
    private static final String COLUMN_ADD_IDENTITY_COMMAND = "ColumnAddIdentityCommand";
    private static final String COLUMN_DROP_IDENTITY_COMMAND = "ColumnDropIdentityCommand";
    private static final String ADD_PRIMARY_KEY_COMMAND = "AddPrimaryKeyCommand";
    private static final String DROP_PRIMARY_KEY_COMMAND = "DropPrimaryKeyCommand";
    private static final String ADD_FOREIGN_KEY_COMMAND = "AddForeignKeyCommand";
    private static final String ADD_UNIQUE_CONSTRAINT_COMMAND = "AddUniqueConstraintCommand";
    private static final String DROP_CONSTRAINT_COMMAND = "DropConstraintCommand";
    private static final String DROP_INDEX_COMMAND = "DropIndexCommand";
    private static final String TABLE_DESCRIPTION_COMMAND = "TableDescriptionCommand";
    private static final String COLUMN_DESCRIPTION_COMMAND = "ColumnDescriptionCommand";
    private static final String CREATE_INDEX_COMMAND = "CreateIndexCommand";

    private static final String ALTER_VIEW_COMMANDS = "AlterViewCommands";
    private static final String RENAME_TABLE_COMMANDS = "RenameTableCommands";
    private static final String SYSTEM_VERSIONING_COMMANDS = "SystemVersioningCommands";

    // java.sql.PreparedStatement queries
    private static final String VIEW_DEFINITION_QUERY = "ViewDefinitionQuery";

    // The constructor method:
    public DDLQuery(XHierarchicalNameAccess config,
                    java.sql.DatabaseMetaData metadata,
                    String subprotocol) throws SQLException {
        super(config, metadata, subprotocol);
    }

    public boolean supportsSystemVersioning() {
        String[] versioning = getSystemVersioningCommands();
        return versioning != null && versioning.length > 0;
    }

    public boolean supportsRenameView() {
        return getSupportsRenameView();
    }

    public boolean supportsAlterColumnProperties() {
        return getSupportsAlterColumnProperties();
    }

    public boolean supportsAlterColumnType() {
        return getSupportsAlterColumnType();
    }

    public boolean supportsAlterPrimaryKey() {
        return getSupportsAlterPrimaryKey();
    }

    public boolean supportsAlterForeignKey() {
        return getSupportsAlterForeignKey();
    }

    public boolean supportsAlterIdentity() {
        return getSupportsAlterIdentity();
    }

    public boolean supportsViewDefinition() {
        return getViewDefinitionQuery() != null;
    }

    public boolean supportsRenamingTable() {
        return getRenameTableCommands().length != 0;
    }

    public boolean canRenameAndMove() {
        return getRenameTableCommands().length > 1;
    }

    public boolean hasMultiRenameQueries() {
        return canRenameAndMove() && !getRenameTableCommands()[1].isBlank();
    }

    public String getCreateTableCommand(final Map<String, Object> keys,
                                        final boolean versioning,
                                        final String key) {
        String query = null;
        String command = getCreateTableCommand();
        if (command != null) {
            if (versioning && supportsTableSystemVersioning()) {
                keys.put(key, " " + getSystemVersioningTableCommand());
            }
            query = format(command, keys);
        }
        return query;
    }

    public String getDropTableCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getDropTableCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public String getCreateViewCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getCreateViewCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public String getDropViewCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getDropViewCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public List<String> getAlterViewCommands(final Map<String, Object> keys) {
        List<String> queries = new ArrayList<>();
        String[] commands = getAlterViewCommands();
        if (commands != null) {
            for (String command : commands) {
                queries.add(format(command, keys));
            }
        }
        return queries;
    }

    public List<String> getRenameTableCommands(final Map<String, Object> keys,
                                               final boolean reversed) {
        List<String> queries = new ArrayList<>();
        List<String> commands = Arrays.asList(getRenameTableCommands());
        if (commands != null) {
            if (reversed) {
                Collections.reverse(commands);
            }
            for (String command : commands) {
                queries.add(format(command, keys));
            }
        }
        return queries;
    }

    public String getAddColumnCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getAddColumnCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public String getDropColumnCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getDropColumnCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public boolean hasAlterColumnCommand() {
        return getAlterColumnCommand() != null;
    }

    public String getAlterColumnCommand(final Map<String, Object> keys) {
        String command = getAlterColumnCommand();
        return format(command, keys);
    }

    public boolean hasColumnSetDataTypeCommand() {
        return getColumnSetDataTypeCommand() != null;
    }

    public String getColumnSetDataTypeCommand(final Map<String, Object> keys) {
        String command = getColumnSetDataTypeCommand();
        return format(command, keys);
    }

    public String getRenameColumnCommand(final Map<String, Object> keys) {
        String command = getRenameColumnCommand();
        return format(command, keys);
    }

    public String getColumnSetDefaultCommand(final Map<String, Object> keys) {
        String command = getColumnSetDefaultCommand();
        return format(command, keys);
    }

    public String getColumnDropDefaultCommand(final Map<String, Object> keys) {
        String command = getColumnDropDefaultCommand();
        return format(command, keys);
    }

    public String getColumnSetNotNullCommand(final Map<String, Object> keys) {
        String command = getColumnSetNotNullCommand();
        return format(command, keys);
    }

    public String getColumnDropNotNullCommand(final Map<String, Object> keys) {
        String command = getColumnDropNotNullCommand();
        return format(command, keys);
    }

    public boolean hasColumnAddIdentityCommand() {
        return getColumnAddIdentityCommand() != null;
    }

    public String getColumnAddIdentityCommand(final Map<String, Object> keys) {
        String command = getColumnAddIdentityCommand();
        return format(command, keys);
    }

    public String getColumnDropIdentityCommand(final Map<String, Object> keys) {
        String command = getColumnDropIdentityCommand();
        return format(command, keys);
    }

    public String getAddConstraintCommand(final Map<String, Object> keys,
                                          final int type) {
        String command = null;
        switch (type) {
            case KeyType.PRIMARY:
                command = getAddPrimaryKeyCommand();
                break;
            case KeyType.FOREIGN:
                command = getAddForeignKeyCommand();
                break;
            case KeyType.UNIQUE:
                command = getAddUniqueConstraintCommand();
                break;
        }
        return format(command, keys);
    }

    public String getDropConstraintCommand(final Map<String, Object> keys,
                                           final int type) {
        String command = null;
        switch (type) {
            case KeyType.PRIMARY:
                command = getDropPrimaryKeyCommand();
                break;
            case KeyType.FOREIGN:
                command = getDropConstraintCommand();
                break;
            case KeyType.UNIQUE:
                command = getDropIndexCommand();
                break;
        }
        return format(command, keys);
    }

    public boolean supportsTableDescription() {
        return getSupportsTableDescription();
    }

    public boolean supportsColumnDescription() {
        return getSupportsColumnDescription();
    }

    public String getTableDescriptionCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getTableDescriptionCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public String getColumnDescriptionCommand(final Map<String, Object> keys) {
        String query = null;
        String command = getColumnDescriptionCommand();
        if (command != null) {
            query = format(command, keys);
        }
        return query;
    }

    public String getAddIndexCommand(final Map<String, Object> keys,
                                     final boolean unique) {
        String command = null;
        if (unique) {
            command = getAddUniqueConstraintCommand();
        } else {
            command = getCreateIndexCommand();
        }
        return DBTools.formatSQLQuery(command, keys);
    }

    public String getSystemVersioningColumnQuery(final List<String> columns)
        throws java.sql.SQLException {
        String query = null;
        String command = getSystemVersioningColumnCommand();
        if (command != null) {
            Map<String, Object> keys = Map.of("ColumnNames", getIdentifiersAsString(columns));
            query = format(command, keys);
        }
        return query;
    }

    public String getViewDefinitionQuery(final Map<String, Object> parameters,
                                         final List<Object> values) {
        String command = getViewDefinitionQuery();
        return format(command, parameters, values, "?");
    }

    private boolean supportsTableSystemVersioning() {
        return supportsSystemVersioning() && getSystemVersioningCommands().length > 1;
    }

    private String getCreateTableCommand() {
        return getPropertyString(CREATE_TABLE_COMMAND);
    }

    private String getDropTableCommand() {
        return getPropertyString(DROP_TABLE_COMMAND);
    }

    private String getCreateViewCommand() {
        return getPropertyString(CREATE_VIEW_COMMAND);
    }

    private String getDropViewCommand() {
        return getPropertyString(DROP_VIEW_COMMAND);
    }

    private String[] getRenameTableCommands() {
        return getPropertyStrings(RENAME_TABLE_COMMANDS);
    }

    private String[] getAlterViewCommands() {
        return getPropertyStrings(ALTER_VIEW_COMMANDS);
    }

    private String getViewDefinitionQuery() {
        return getPropertyString(VIEW_DEFINITION_QUERY);
    }

    private boolean getSupportsRenameView() {
        return getPropertyBoolean(SUPPORTS_RENAME_VIEW);
    }

    private String getAddColumnCommand() {
        return getPropertyString(ADD_COLUMN_COMMAND);
    }

    private String getDropColumnCommand() {
        return getPropertyString(DROP_COLUMN_COMMAND);
    }

    private String getAlterColumnCommand() {
        return getPropertyString(ALTER_COLUMN_COMMAND);
    }

    private String getColumnSetDataTypeCommand() {
        return getPropertyString(COLUMN_SET_DATATYPE_COMMAND);
    }

    private String getRenameColumnCommand() {
        return getPropertyString(RENAME_COLUMN_COMMAND);
    }

    private String getColumnSetNotNullCommand() {
        return getPropertyString(COLUMN_SET_NOTNULL_COMMAND);
    }

    private String getColumnDropNotNullCommand() {
        return getPropertyString(COLUMN_DROP_NOTNULL_COMMAND);
    }

    private String getColumnSetDefaultCommand() {
        return getPropertyString(COLUMN_SET_DEFAULT_COMMAND);
    }

    private String getColumnDropDefaultCommand() {
        return getPropertyString(COLUMN_DROP_DEFAULT_COMMAND);
    }

    private String getColumnAddIdentityCommand() {
        return getPropertyString(COLUMN_ADD_IDENTITY_COMMAND);
    }

    private String getColumnDropIdentityCommand() {
        return getPropertyString(COLUMN_DROP_IDENTITY_COMMAND);
    }

    private String getAddPrimaryKeyCommand() {
        return getPropertyString(ADD_PRIMARY_KEY_COMMAND);
    }

    private String getAddForeignKeyCommand() {
        return getPropertyString(ADD_FOREIGN_KEY_COMMAND);
    }

    private String getAddUniqueConstraintCommand() {
        return getPropertyString(ADD_UNIQUE_CONSTRAINT_COMMAND);
    }

    private String getDropPrimaryKeyCommand() {
        return getPropertyString(DROP_PRIMARY_KEY_COMMAND);
    }

    private String getDropConstraintCommand() {
        return getPropertyString(DROP_CONSTRAINT_COMMAND);
    }

    private String getCreateIndexCommand() {
        return getPropertyString(CREATE_INDEX_COMMAND);
    }

    private String getDropIndexCommand() {
        return getPropertyString(DROP_INDEX_COMMAND);
    }

    private String getTableDescriptionCommand() {
        return getPropertyString(TABLE_DESCRIPTION_COMMAND);
    }

    private String getColumnDescriptionCommand() {
        return getPropertyString(COLUMN_DESCRIPTION_COMMAND);
    }

    private String[] getSystemVersioningCommands() {
        return getPropertyStrings(SYSTEM_VERSIONING_COMMANDS);
    }

    private String getSystemVersioningColumnCommand() {
        return getSystemVersioningCommands()[0];
    }

    private String getSystemVersioningTableCommand() {
        return getSystemVersioningCommands()[1];
    }

    private boolean getSupportsAlterColumnProperties() {
        return getPropertyBoolean(SUPPORTS_ALTER_COLUMN_PROPERTIES, true);
    }

    private boolean getSupportsAlterColumnType() {
        return getPropertyBoolean(SUPPORTS_ALTER_COLUMN_TYPE, true);
    }

    private boolean getSupportsAlterPrimaryKey() {
        return getPropertyBoolean(SUPPORTS_ALTER_PRIMARY_KEY, true);
    }

    private boolean getSupportsAlterForeignKey() {
        return getPropertyBoolean(SUPPORTS_ALTER_FOREIGN_KEY, true);
    }

    private boolean getSupportsAlterIdentity() {
        return getPropertyBoolean(SUPPORTS_ALTER_IDENTITY, false);
    }

    private boolean getSupportsTableDescription() {
        return getPropertyBoolean(SUPPORTS_TABLE_DESCRIPTION, true);
    }

    private boolean getSupportsColumnDescription() {
        return getPropertyBoolean(SUPPORTS_COLUMN_DESCRIPTION, true);
    }
}