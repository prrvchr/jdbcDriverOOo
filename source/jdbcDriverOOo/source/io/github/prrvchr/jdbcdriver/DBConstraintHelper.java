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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.KeyRule;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;

public class DBConstraintHelper
{

    public static String getCreateConstraintQuery(DriverProvider provider,
                                                  XPropertySet descriptor,
                                                  String catalog,
                                                  String schema,
                                                  String table,
                                                  String name,
                                                  ComposeRule rule,
                                                  boolean sensitive)
        throws SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
        List<String> args = new ArrayList<>();
        int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.TYPE);
        String command = provider.getAddConstraintQuery(type);
        args.add(DBTools.buildName(provider, catalog, schema, table, rule, sensitive));
        String keyname = DBColumnHelper.getKeyName(name, table, getKeyColumns(provider, descriptor, PropertyIds.NAME, false), type);
        args.add(DBTools.enquoteIdentifier(provider, keyname, sensitive));
        args.add(String.join(", ", getKeyColumns(provider, descriptor, PropertyIds.NAME, sensitive)));
        if (type == KeyType.FOREIGN) {
            String reftable = DBTools.getDescriptorStringValue(descriptor, PropertyIds.REFERENCEDTABLE);
            args.add(DBTools.quoteTableName(provider, reftable, rule, sensitive));
            args.add(String.join(", ", getKeyColumns(provider, descriptor, PropertyIds.RELATEDCOLUMN, sensitive)));
            int update = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.UPDATERULE);
            int delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyIds.DELETERULE);
            args.add(getKeyRuleString(true, update));
            args.add(getKeyRuleString(false, delete));
        }
        return MessageFormat.format(command, args.toArray(new Object[0]));
    }

    public static List<String> getKeyColumns(DriverProvider provider,
                                             XPropertySet descriptor,
                                             PropertyIds name,
                                             boolean sensitive)
        throws SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        return getKeyColumns(provider, indexes, name, sensitive);
    }

    public static List<String> getCreatePrimaryKeyParts(DriverProvider provider,
                                                        XPropertySet descriptor,
                                                        boolean sensitive)
        throws SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
        List<String> queries = new ArrayList<String>();
        XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = keysSupplier.getKeys();
        if (keys != null) {
            boolean hasPrimaryKey = false;
            for (int i = 0; i < keys.getCount(); i++) {
                XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                if (columnProperties != null) {
                    StringBuilder buffer = new StringBuilder();
                    int keyType = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.TYPE);
                    XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, columnProperties);
                    XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
                    if (columns != null && columns.getCount() > 0) {
                        if (keyType == KeyType.PRIMARY) {
                            if (hasPrimaryKey) {
                                throw new SQLException();
                            }
                            hasPrimaryKey = true;
                            buffer.append("PRIMARY KEY");
                            buffer.append(getKeyColumns(provider, columns, sensitive));
                        }
                        else if (keyType == KeyType.UNIQUE) {
                            buffer.append("UNIQUE");
                            buffer.append(getKeyColumns(provider, columns, sensitive));
                        }
                        else if (keyType == KeyType.FOREIGN) {
                            int deleteRule = DBTools.getDescriptorIntegerValue(columnProperties, PropertyIds.DELETERULE);
                            buffer.append("FOREIGN KEY");
                            
                            String referencedTable = DBTools.getDescriptorStringValue(columnProperties, PropertyIds.REFERENCEDTABLE);
                            NameComponents nameComponents = DBTools.qualifiedNameComponents(provider, referencedTable, ComposeRule.InDataManipulation);
                            String composedName = DBTools.buildName(provider, nameComponents.getCatalog(), nameComponents.getSchema(), nameComponents.getTable(),
                                                                    ComposeRule.InTableDefinitions, true);
                            if (composedName.isEmpty()) {
                                throw new SQLException();
                            }
                            
                            buffer.append(getKeyColumns(provider, columns, sensitive));
                            buffer.append(" ");
                            buffer.append(getKeyRuleString(false, deleteRule));
                        }
                        queries.add(buffer.toString());
                    }
                }
            }
        }
        return queries;
    }

    private static String getKeyRuleString(boolean isUpdate,
                                           int rule)
    {
        String keyRule = "";
        switch (rule) {
        case KeyRule.CASCADE:
            keyRule = isUpdate ? "ON UPDATE CASCADE" : "ON DELETE CASCADE";
            break;
        case KeyRule.RESTRICT:
            keyRule = isUpdate ? "ON UPDATE RESTRICT" : "ON DELETE RESTRICT";
            break;
        case KeyRule.SET_NULL:
            keyRule = isUpdate ? "ON UPDATE SET NULL" : "ON DELETE SET NULL";
            break;
        case KeyRule.SET_DEFAULT:
            keyRule = isUpdate ? "ON UPDATE SET DEFAULT" : "ON DELETE SET DEFAULT";
            break;
        }
        return keyRule;
    }

    private static String getKeyColumns(DriverProvider provider,
                                        XIndexAccess columns,
                                        boolean sensitive)
        throws SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
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
        throws SQLException, IndexOutOfBoundsException, WrappedTargetException
    {
        List<String> columns = new ArrayList<String>();
        for (int i = 0; i < indexes.getCount(); i++) {
            XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
            if (property != null) {
                String value = DBTools.getDescriptorStringValue(property, name);
                columns.add(DBTools.enquoteIdentifier(provider, value, sensitive));
            }
        }
        return columns;
    }

}
