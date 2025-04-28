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
package io.github.prrvchr.driver.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.query.DDLParameter;

public class ConstraintHelper {

    public static String getCreateConstraintQuery(DriverProvider provider,
                                                  XPropertySet descriptor,
                                                  NamedComponents table,
                                                  String name,
                                                  ComposeRule rule,
                                                  boolean sensitive)
        throws java.sql.SQLException, SQLException {
        try {
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
            String tablename = DBTools.buildName(provider, table, rule, sensitive);
            String keyname = KeyHelper.getKeyName(name, table.getTableName(), type);
            keyname = provider.enquoteIdentifier(keyname, sensitive);
            List<String> columns = getKeyColumns(provider, descriptor, PropertyIds.NAME, sensitive);
            Map<String, Object> arguments = DDLParameter.getCreateConstraint(tablename, keyname, columns);
            if (type == KeyType.FOREIGN) {
                String reftable = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
                reftable = DBTools.quoteTableName(provider, reftable, rule, sensitive);
                columns = getKeyColumns(provider, descriptor, PropertyIds.RELATEDCOLUMN, sensitive);
                int update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
                int delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
                DDLParameter.setCreateConstraint(arguments, reftable, columns, update, delete);
            }
            return provider.getDDLQuery().getAddConstraintCommand(arguments, type);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            throw new java.sql.SQLException();
        }
    }

    public static List<String> getKeyColumns(DriverProvider provider,
                                             XPropertySet descriptor,
                                             PropertyIds name,
                                             boolean sensitive)
        throws java.sql.SQLException, SQLException {
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        return getKeyColumns(provider, indexes, name, sensitive);
    }

    public static List<String> getCreatePrimaryKeyParts(DriverProvider provider,
                                                        XPropertySet descriptor,
                                                        boolean sensitive)
        throws java.sql.SQLException, SQLException {
        List<String> queries = new ArrayList<>();
        XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = keysSupplier.getKeys();
        if (keys != null) {
            boolean hasPrimaryKey = false;
            try {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        setCreatePrimaryKeyQueries(provider, queries, columnProperties, sensitive, hasPrimaryKey);
                    }
                }
            } catch (WrappedTargetException | IndexOutOfBoundsException e) {
                throw new SQLException(e.getMessage());
            }
        }
        return queries;
    }

    public static void setCreatePrimaryKeyQueries(DriverProvider provider,
                                                  List<String> queries,
                                                  XPropertySet columnProperties,
                                                  boolean sensitive,
                                                  boolean hasPrimaryKey)
        throws java.sql.SQLException, SQLException {
        StringBuilder buffer = new StringBuilder();
        int keyType = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.TYPE);
        XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, columnProperties);
        XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
        if (columns != null && columns.getCount() > 0) {
            if (keyType == KeyType.PRIMARY) {
                if (hasPrimaryKey) {
                    throw new java.sql.SQLException();
                }
                hasPrimaryKey = true;
                buffer.append("PRIMARY KEY");
                buffer.append(getKeyColumns(provider, columns, sensitive));
            } else if (keyType == KeyType.UNIQUE) {
                buffer.append("UNIQUE");
                buffer.append(getKeyColumns(provider, columns, sensitive));
            } else if (keyType == KeyType.FOREIGN) {
                int deleteRule = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.DELETERULE);
                buffer.append("FOREIGN KEY");
                
                String referencedTable = DBTools.getDescriptorStringValue(columnProperties,
                                                                          PropertyIds.REFERENCEDTABLE);
                NamedComponents nameComponents = DBTools.qualifiedNameComponents(provider, referencedTable,
                                                                                 ComposeRule.InDataManipulation);
                String composedName = DBTools.buildName(provider, nameComponents.getCatalogName(),
                                                        nameComponents.getSchemaName(), nameComponents.getTableName(),
                                                        ComposeRule.InTableDefinitions, true);
                if (composedName.isEmpty()) {
                    throw new java.sql.SQLException();
                }
                
                buffer.append(getKeyColumns(provider, columns, sensitive));
                buffer.append(" ");
                buffer.append(DDLParameter.getKeyRuleString(false, deleteRule));
            }
            queries.add(buffer.toString());
        }
    }

    private static String getKeyColumns(DriverProvider provider,
                                        XIndexAccess columns,
                                        boolean sensitive)
        throws java.sql.SQLException, SQLException {
        String separator = ", ";
        StringBuilder buffer = new StringBuilder();
        List<String> names = getKeyColumns(provider, columns, PropertyIds.NAME, sensitive);
        if (!names.isEmpty()) {
            buffer.append(" (");
            buffer.append(String.join(separator, names));
            buffer.append(")");
        }
        return buffer.toString();
    }

    private static List<String> getKeyColumns(DriverProvider provider,
                                              XIndexAccess indexes,
                                              PropertyIds name,
                                              boolean sensitive)
        throws java.sql.SQLException, SQLException {
        List<String> columns = new ArrayList<>();
        try {
            for (int i = 0; i < indexes.getCount(); i++) {
                XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
                if (property != null) {
                    String value = DBTools.getDescriptorStringValue(property, name);
                    columns.add(provider.enquoteIdentifier(value, sensitive));
                }
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException e) {
            throw new SQLException(e.getMessage());
        }
        return columns;
    }

}
