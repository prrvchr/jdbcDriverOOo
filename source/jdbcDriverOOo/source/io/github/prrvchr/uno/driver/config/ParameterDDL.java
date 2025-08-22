/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.driver.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.TableHelper.ColumnProperties;


public class ParameterDDL extends ParameterBase {

    public static String setCreateTable(final Map<String, Object> arguments,
                                        final NamedSupport support,
                                        final XPropertySet property,
                                        final String type,
                                        final Collection<String> columns,
                                        final boolean sensitive)
        throws java.sql.SQLException {
        // XXX: ${TableType} table type
        arguments.put("TableType", type);
        // XXX: ${TableName} unquoted / quoted full table name
        arguments.put("TableName", ComponentHelper.composeTableName(support, property, sensitive));
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

    public static Map<String, Object> getColumnDescription(final NamedSupport support,
                                                           final NamedComponent component,
                                                           final String column,
                                                           final String description,
                                                           final boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("RawSchema", component.getSchemaName());
        arguments.put("RawTable", component.getTableName());
        arguments.put("RawColumn", column);
        arguments.put("ColumnName", ComponentHelper.composeColumnName(support, component, column, sensitive));
        arguments.put("Description", support.enquoteLiteral(description));
        arguments.put("RawDescription", description);
        return arguments;
    }

    public static Map<String, Object> getColumnDescription(final NamedSupport support,
                                                           final NamedComponent component,
                                                           final String column,
                                                           final boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("RawSchema", component.getSchemaName());
        arguments.put("RawTable", component.getTableName());
        arguments.put("RawColumn", column);
        arguments.put("ColumnName", ComponentHelper.composeColumnName(support, component, column, sensitive));
        return arguments;
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

    public static Map<String, Object> getAddColumn(final NamedSupport support,
                                                   final NamedComponent component,
                                                   final ColumnProperties column,
                                                   final boolean sensitive) {
        return Map.of("TableName", ComponentHelper.composeTableName(support, component, sensitive),
                      "ColumnDescription", getColumnDescription(column));
    }

    public static Map<String, Object> getDropColumn(String table, String column) {
        return Map.of("TableName", table, "Column", column);
    }

    public static Map<String, Object> getAddIndex(final String table,
                                                  final String name,
                                                  String[] columns) {
        return getCreateConstraint(table, name, columns);
    }

    public static Map<String, Object> getCreateConstraint(final String table,
                                                          final String name,
                                                          String[] columns) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("TableName", table);
        arguments.put("Name", name);
        arguments.put("Columns", String.join(getSeparator(), columns));
        return arguments;
    }

    public static Map<String, Object> getDropConstraint(String table, String name) {
        return Map.of("TableName", table, "Name", name);
    }

    public static void setCreateConstraint(Map<String, Object> arguments,
                                           String table,
                                           String[] columns,
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

    public static Map<String, Object> getRenameTable(NamedSupport support,
                                                     NamedComponent newtable,
                                                     NamedComponent oldtable,
                                                     String fullname,
                                                     boolean reversed,
                                                     boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${TableName} quoted / unquoted full old table name
        arguments.put("TableName", ComponentHelper.quoteTableName(support, fullname, sensitive));
        arguments.put("RawTableName", fullname);
        // XXX: ${NewSchema} quoted / unquoted new schema name
        arguments.put("NewSchema", support.enquoteIdentifier(newtable.getSchemaName(), sensitive));
        // XXX: ${Catalog.NewSchema.Table} quoted / unquoted full old table name overwritten with the new schema name
        arguments.put("Catalog.NewSchema.Table", ComponentHelper.buildName(support, oldtable.getCatalogName(),
                                                                                    newtable.getSchemaName(),
                                                                                    oldtable.getTableName(),
                                                                                    sensitive));
        // XXX: ${Table} quoted / unquoted old table name
        arguments.put("Table", support.enquoteIdentifier(oldtable.getTableName(), sensitive));
        // XXX: ${NewTable} quoted / unquoted new table name
        arguments.put("NewTable", support.enquoteIdentifier(newtable.getTableName(), sensitive));
        arguments.put("RawNewTable", newtable.getTableName());
        // XXX: ${Catalog.Schema.NewTable} quoted / unquoted full old table name overwritten with the new table name
        arguments.put("Catalog.Schema.NewTable", ComponentHelper.buildName(support, oldtable.getCatalogName(),
                                                                                    oldtable.getSchemaName(),
                                                                                    newtable.getTableName(),
                                                                                    sensitive));
        // XXX: ${NewCatalog} quoted / unquoted new catalog name
        arguments.put("NewCatalog", support.enquoteIdentifier(newtable.getCatalogName(), sensitive));
        // XXX: ${NewCatalog.Schema.Table} quoted / unquoted full old table name overwritten with the new catalog name
        arguments.put("NewCatalog.Schema.Table", ComponentHelper.buildName(support, newtable.getCatalogName(),
                                                                                    oldtable.getSchemaName(),
                                                                                    oldtable.getTableName(),
                                                                                    sensitive));
        // XXX: ${NewCatalog.NewSchema.NewTable} quoted / unquoted full new table name
        arguments.put("NewCatalog.NewSchema.NewTable", ComponentHelper.buildName(support, newtable.getCatalogName(),
                                                                                          newtable.getSchemaName(),
                                                                                          newtable.getTableName(),
                                                                                          sensitive));
        if (reversed) {
            Object argument = arguments.get("TableName");
            arguments.put("TableName", arguments.get("Catalog.Schema.NewTable"));
            arguments.put("Catalog.Schema.NewTable", arguments.get("Catalog.NewSchema.Table"));
            arguments.put("Catalog.NewSchema.NewTable", argument);
        }
        return arguments;
    }

    public static Map<String, Object> getAlterView(NamedSupport support,
                                                   NamedComponent view,
                                                   String command,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = getViewDefinition(support, view, sensitive);
        arguments.put("SelectCommand", command);
        return arguments;
    }

    public static Map<String, Object> getViewDefinition(NamedSupport support,
                                                        NamedComponent view,
                                                        boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${ViewName} quoted / unquoted  full view name
        arguments.put("ViewName", ComponentHelper.buildName(support, view, sensitive));
        // XXX: ${Catalog} quoted / unquoted  catalog view name
        arguments.put("Catalog", support.enquoteIdentifier(view.getCatalogName(), sensitive));
        // XXX: ${Schema} quoted / unquoted  schema view name
        arguments.put("Schema", support.enquoteIdentifier(view.getSchemaName(), sensitive));
        // XXX: ${View} quoted / unquoted  view name
        arguments.put("View", support.enquoteIdentifier(view.getTableName(), sensitive));
        arguments.put("RawCatalog", view.getCatalogName());
        arguments.put("RawSchema", view.getSchemaName());
        arguments.put("RawView", view.getTableName());
        return arguments;
    }

    public static Map<String, Object> getColumnProperties(NamedSupport support,
                                                          NamedComponent component,
                                                          final ColumnProperties column,
                                                          boolean sensitive) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("TableName", ComponentHelper.composeTableName(support, component, sensitive));
        arguments.put("RawTableName", ComponentHelper.composeTableName(support, component, false));
        arguments.put("Table", support.enquoteIdentifier(component.getTable(), sensitive));
        arguments.put("RawTable", component.getTable());
        arguments.put("Column", support.enquoteIdentifier(column.getName(), sensitive));
        String name = support.enquoteIdentifier(column.getNewName(), sensitive);
        arguments.put("NewColumn", name);
        arguments.put("RawColumn", column.getName());
        arguments.put("RawNewColumn", column.getNewName());
        arguments.put("Type", column.getType());
        if (!column.getDefaultValue().isBlank()) {
            arguments.put("Default", "DEFAULT " + column.getDefaultValue());
        } else {
            arguments.put("Default", "");
        }
        if (column.getNotNull()) {
            arguments.put("Nullable", "NOT NULL");
        } else {
            arguments.put("Nullable", "");
        }
        if (column.isAutoIncrement()) {
            arguments.put("Autoincrement", column.getAutoIncrement());
        } else {
            arguments.put("Autoincrement", "");
        }
        arguments.put("ColumnDescription", getColumnDescription(column));
        return arguments;
    }

    public static String getColumnDescription(final ColumnProperties column) {
        // XXX: We try to construct the Column part needed for Table creation
        StringBuilder builder = new StringBuilder(column.getNewName());
        builder.append(" ");
        builder.append(column.getType());
        if (!column.getDefaultValue().isBlank()) {
            builder.append(" DEFAULT ");
            builder.append(column.getDefaultValue());
        }
        if (column.getNotNull()) {
            builder.append(" NOT NULL");
        }
        if (column.isAutoIncrement()) {
            builder.append(" ");
            builder.append(column.getAutoIncrement());
        }
        return builder.toString();
    }

}
