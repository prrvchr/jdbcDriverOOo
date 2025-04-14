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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;


public class DDLParameter extends SQLBase {

    public static String setCreateTable(final Map<String, Object> arguments,
                                        final String type,
                                        final String table,
                                        final Collection<String> columns) {
        // XXX: ${TableType} table type
        arguments.put("TableType", type);
        // XXX: ${TableName} unquoted / quoted full table name
        arguments.put("TableName", table);
        // XXX: ${ColumnDescriptions} unquoted schema name
        arguments.put("ColumnDescriptions", String.join(getSeparator(), columns));
        // XXX: ${Versioning} versioning will be set during query build
        String versioning = "Versioning";
        arguments.put(versioning, "");
        return versioning;
    }

    public static Map<String, Object> getTableDescription(String table, String description) {
        return Map.of("TableName", table, "Description", description);
    }

    public static Map<String, Object> getColumnDescription(String column, String description) {
        return Map.of("ColumnName", column, "Description", description);
    }

    public static Map<String, Object> getDropTable(String table) {
        return Map.of("TableName", table);
    }

    public static Map<String, Object> getCreateView(String view,
                                                             String select) {
        return Map.of("ViewName", view, "SelectCommand", select);
    }

    public static Map<String, Object> getDropView(String view) {
        return Map.of("ViewName", view);
    }

    public static Map<String, Object> getAddColumn(String table, String column) {
        return Map.of("TableName", table, "ColumnDescriptions", column);
    }

    public static Map<String, Object> getDropColumn(String table, String column) {
        return Map.of("TableName", table, "Column", column);
    }

    public static Map<String, Object> getAddIndex(final String table,
                                                  final String name,
                                                  List<String> columns) {
        return getCreateConstraint(table, name, columns);
    }

    public static Map<String, Object> getCreateConstraint(final String table,
                                                          final String name,
                                                          List<String> columns) {
        return Map.of("TableName", table, "Name", name, "Columns", String.join(getSeparator(), columns));
    }

    public static Map<String, Object> getDropConstraint(String table, String name) {
        return Map.of("TableName", table, "Name", name);
    }

    public static void setCreateConstraint(Map<String, Object> arguments,
                                           String table,
                                           List<String> columns,
                                           int update,
                                           int delete) {
        arguments.put("ReferencedTable", table);
        arguments.put("RelatedColumns", String.join(getSeparator(), columns));
        arguments.put("UpdateRule", getKeyRuleString(true, update));
        arguments.put("DeleteRule", getKeyRuleString(false, delete));
    }

    public static String getKeyRuleString(final boolean isUpdate, final int rule) {
        String keyRule = "";
        switch (rule) {
            case KeyRule.CASCADE:
                if (isUpdate) {
                    keyRule = "ON UPDATE CASCADE";
                } else {
                    keyRule = "ON DELETE CASCADE";
                }
                break;
            case KeyRule.RESTRICT:
                if (isUpdate) {
                    keyRule = "ON UPDATE RESTRICT";
                } else {
                    keyRule = "ON DELETE RESTRICT";
                }
                break;
            case KeyRule.SET_NULL:
                if (isUpdate) {
                    keyRule = "ON UPDATE SET NULL";
                } else {
                    keyRule = "ON DELETE SET NULL";
                }
                break;
            case KeyRule.SET_DEFAULT:
                if (isUpdate) {
                    keyRule = "ON UPDATE SET DEFAULT";
                } else {
                    keyRule = "ON DELETE SET DEFAULT";
                }
                break;
        }
        return keyRule;
    }

    public static Map<String, Object> getRenameTable(DriverProvider provider,
                                                     NamedComponents newtable,
                                                     NamedComponents oldtable,
                                                     String fullname,
                                                     boolean reversed,
                                                     ComposeRule rule,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Catalog.Schema.Table} quoted / unquoted full old table name
        arguments.put("Catalog.Schema.Table", DBTools.quoteTableName(provider, fullname, rule, sensitive));
        // XXX: ${NewSchema} quoted / unquoted new schema name
        arguments.put("NewSchema", provider.enquoteIdentifier(newtable.getSchemaName(), sensitive));
        // XXX: ${Catalog.NewSchema.Table} quoted / unquoted full old table name overwritten with the new schema name
        arguments.put("Catalog.NewSchema.Table",
                      DBTools.buildName(provider, oldtable.getCatalogName(), newtable.getSchemaName(),
                                        oldtable.getTableName(), rule, sensitive));
        // XXX: ${NewTable} quoted / unquoted new table name
        arguments.put("NewTable", provider.enquoteIdentifier(newtable.getTableName(), sensitive));
        // XXX: ${Catalog.Schema.NewTable} quoted / unquoted full old table name overwritten with the new table name
        arguments.put("Catalog.Schema.NewTable",
                      DBTools.buildName(provider, oldtable.getCatalogName(), oldtable.getSchemaName(),
                                        newtable.getTableName(), rule, sensitive));
        // XXX: ${NewCatalog} quoted / unquoted new catalog name
        arguments.put("NewCatalog", provider.enquoteIdentifier(newtable.getCatalogName(), sensitive));
        // XXX: ${NewCatalog.Schema.Table} quoted / unquoted full old table name overwritten with the new catalog name
        arguments.put("NewCatalog.Schema.Table",
                      DBTools.buildName(provider, newtable.getCatalogName(), oldtable.getSchemaName(),
                                        oldtable.getTableName(), rule, sensitive));
        // XXX: ${NewCatalog.NewSchema.NewTable} quoted / unquoted full new table name
        arguments.put("NewCatalog.NewSchema.NewTable",
                      DBTools.buildName(provider, newtable.getCatalogName(), newtable.getSchemaName(),
                                        newtable.getTableName(), rule, sensitive));
        if (reversed) {
            Object argument = arguments.get("Catalog.Schema.Table");
            arguments.put("Catalog.Schema.Table", arguments.get("Catalog.Schema.NewTable"));
            arguments.put("Catalog.Schema.NewTable", arguments.get("Catalog.NewSchema.Table"));
            arguments.put("Catalog.NewSchema.NewTable", argument);
        }
        return arguments;
    }

    public static Map<String, Object> getAlterView(DriverProvider provider,
                                                   NamedComponents table,
                                                   String fullname,
                                                   String command,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = getViewDefinition(provider, table, fullname, rule, sensitive);
        arguments.put("SelectCommand", command);
        return arguments;
    }

    public static Map<String, Object> getViewDefinition(DriverProvider provider,
                                                        NamedComponents table,
                                                        String fullname,
                                                        ComposeRule rule,
                                                        boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${ViewName} quoted / unquoted  full view name
        arguments.put("ViewName", DBTools.quoteTableName(provider, fullname, rule, sensitive));
        // XXX: ${Catalog} quoted / unquoted  catalog view name
        arguments.put("Catalog", provider.enquoteIdentifier(table.getCatalogName(), sensitive));
        // XXX: ${Schema} quoted / unquoted  schema view name
        arguments.put("Schema", provider.enquoteIdentifier(table.getSchemaName(), sensitive));
        // XXX: ${View} quoted / unquoted  view name
        arguments.put("View", provider.enquoteIdentifier(table.getTableName(), sensitive));
        return arguments;
    }

    public static Map<String, Object> getColumnProperties(String table,
                                                          String oldIdentifier,
                                                          String newIdentifier,
                                                          String columnType,
                                                          String defaultValue,
                                                          boolean notNull,
                                                          boolean isAutoincrement,
                                                          String autoincrement,
                                                          String columndescription) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("TableName", table);
        arguments.put("OldName", oldIdentifier);
        arguments.put("Column", newIdentifier);
        arguments.put("Type", columnType);
        if (!defaultValue.isBlank()) {
            arguments.put("Default", "DEFAULT " + defaultValue);
        } else {
            arguments.put("Default", "");
        }
        if (notNull) {
            arguments.put("Nullable", "NOT NULL");
        } else {
            arguments.put("Nullable", "");
        }
        if (isAutoincrement) {
            arguments.put("Autoincrement", autoincrement);
        } else {
            arguments.put("Autoincrement", "");
        }
        arguments.put("ColumnDescription", columndescription);
        return arguments;
    }

}
