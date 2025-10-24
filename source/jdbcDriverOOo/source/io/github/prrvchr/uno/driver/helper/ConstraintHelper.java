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
package io.github.prrvchr.uno.driver.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;

public class ConstraintHelper {

    public static String getCreateConstraintQuery(ConfigDDL config,
                                                  NamedSupport support,
                                                  XPropertySet descriptor,
                                                  NamedComponent table,
                                                  String name,
                                                  boolean sensitive)
        throws SQLException {
        try {
            int type = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.TYPE);
            String tablename = ComponentHelper.buildName(support, table, sensitive);
            String keyname = KeyHelper.getKeyName(name, table.getTableName(), type);
            keyname = support.enquoteIdentifier(keyname, sensitive);
            String[] columns = getKeyColumns(support, descriptor, PropertyID.NAME, sensitive);
            Map<String, Object> arguments = ParameterDDL.getCreateConstraint(tablename, keyname, columns);
            if (type == KeyType.FOREIGN) {
                String reftable = DBTools.getDescriptorStringValue(descriptor, PropertyID.REFERENCEDTABLE);
                reftable = ComponentHelper.quoteTableName(support, reftable, sensitive);
                columns = getKeyColumns(support, descriptor, PropertyID.RELATEDCOLUMN, sensitive);
                int update = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.UPDATERULE);
                int delete = DBTools.getDescriptorIntegerValue(descriptor, PropertyID.DELETERULE);
                ParameterDDL.setCreateConstraint(arguments, reftable, columns, update, delete);
            }
            return config.getAddConstraintCommand(arguments, type);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            throw new SQLException();
        }
    }

    public static String[] getKeyColumns(NamedSupport support,
                                         XPropertySet descriptor,
                                         PropertyID name,
                                         boolean sensitive)
        throws SQLException {
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
        return getKeyColumns(support, indexes, name, sensitive);
    }

    public static List<String> getCreatePrimaryKeyParts(NamedSupport support,
                                                        XPropertySet descriptor,
                                                        boolean sensitive)
        throws SQLException {
        List<String> queries = new ArrayList<>();
        XKeysSupplier keysSupplier = UnoRuntime.queryInterface(XKeysSupplier.class, descriptor);
        XIndexAccess keys = keysSupplier.getKeys();
        if (keys != null) {
            boolean hasPrimaryKey = false;
            try {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        setCreatePrimaryKeyQueries(support, queries, columnProperties, sensitive, hasPrimaryKey);
                    }
                }
            } catch (WrappedTargetException | IndexOutOfBoundsException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
        return queries;
    }

    public static void setCreatePrimaryKeyQueries(NamedSupport support,
                                                  List<String> queries,
                                                  XPropertySet columnProperties,
                                                  boolean sensitive,
                                                  boolean hasPrimaryKey)
        throws SQLException {
        StringBuilder buffer = new StringBuilder();
        int keyType = DBTools.getDescriptorIntegerValue(columnProperties, PropertyID.TYPE);
        XColumnsSupplier columnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, columnProperties);
        XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
        if (columns != null && columns.getCount() > 0) {
            if (keyType == KeyType.PRIMARY) {
                if (hasPrimaryKey) {
                    throw new SQLException();
                }
                hasPrimaryKey = true;
                buffer.append("PRIMARY KEY");
                buffer.append(getKeyColumns(support, columns, sensitive));
            } else if (keyType == KeyType.UNIQUE) {
                buffer.append("UNIQUE");
                buffer.append(getKeyColumns(support, columns, sensitive));
            } else if (keyType == KeyType.FOREIGN) {
                int deleteRule = DBTools.getDescriptorIntegerValue(columnProperties, PropertyID.DELETERULE);
                buffer.append("FOREIGN KEY");
                
                String refTable = DBTools.getDescriptorStringValue(columnProperties, PropertyID.REFERENCEDTABLE);
                NamedComponent nameComponents = ComponentHelper.qualifiedNameComponents(support, refTable);
                String composedName = ComponentHelper.buildName(support, nameComponents, true);
                if (composedName.isEmpty()) {
                    String msg = "ConstraintHelper::setCreatePrimaryKeyQueries: Error Referenced table can't de read";
                    throw new SQLException(msg);
                }
                
                buffer.append(getKeyColumns(support, columns, sensitive));
                buffer.append(" ");
                buffer.append(ParameterDDL.getKeyRuleString(false, deleteRule));
            }
            queries.add(buffer.toString());
        }
    }

    private static String getKeyColumns(NamedSupport support,
                                        XIndexAccess columns,
                                        boolean sensitive)
        throws SQLException {
        String separator = ", ";
        StringBuilder buffer = new StringBuilder();
        String[] names = getKeyColumns(support, columns, PropertyID.NAME, sensitive);
        if (names.length > 0) {
            buffer.append(" (");
            buffer.append(String.join(separator, names));
            buffer.append(")");
        }
        return buffer.toString();
    }

    private static String[] getKeyColumns(NamedSupport support,
                                          XIndexAccess indexes,
                                          PropertyID name,
                                          boolean sensitive)
        throws SQLException {
        List<String> columns = new ArrayList<>();
        try {
            for (int i = 0; i < indexes.getCount(); i++) {
                XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
                if (property != null) {
                    String value = DBTools.getDescriptorStringValue(property, name);
                    columns.add(support.enquoteIdentifier(value, sensitive));
                }
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException e) {
            throw new SQLException(e.getMessage(), e);
        }
        return columns.toArray(new String[0]);
    }

}
