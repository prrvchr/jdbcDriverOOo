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

import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;

import static com.sun.star.uno.UnoRuntime.queryInterface;
import static io.github.prrvchr.uno.driver.helper.ComponentHelper.buildName;
import static io.github.prrvchr.uno.driver.helper.ComponentHelper.qualifiedNameComponents;
import static io.github.prrvchr.uno.driver.helper.ComponentHelper.quoteTableName;

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
            String tablename = buildName(support, table, sensitive);
            String keyname = KeyHelper.getKeyName(name, table.getTableName(), type);
            keyname = support.enquoteIdentifier(keyname, sensitive);
            String[] columns = getKeyColumns(support, descriptor, PropertyID.NAME, sensitive);
            Map<String, Object> arguments = ParameterDDL.getCreateConstraint(tablename, keyname, columns);
            if (type == KeyType.FOREIGN) {
                String reftable = DBTools.getDescriptorStringValue(descriptor, PropertyID.REFERENCEDTABLE);
                reftable = quoteTableName(support, reftable, sensitive);
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
        XColumnsSupplier supplier = queryInterface(XColumnsSupplier.class, descriptor);
        XIndexAccess indexes = queryInterface(XIndexAccess.class, supplier.getColumns());
        return getKeyColumns(support, indexes, name, sensitive);
    }

    public static List<String> getPrimaryKeys(XPropertySet descriptor)
        throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        XIndexAccess keys = queryInterface(XKeysSupplier.class, descriptor).getKeys();
        if (keys != null) {
            try {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet properties = queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (properties != null) {
                        int keyType = DBTools.getDescriptorIntegerValue(properties, PropertyID.TYPE);
                        if (keyType == KeyType.PRIMARY) {
                            XColumnsSupplier supplier = queryInterface(XColumnsSupplier.class, properties);
                            XIndexAccess columns = queryInterface(XIndexAccess.class, supplier.getColumns());
                            primaryKeys = getKeyColumns(columns, PropertyID.NAME);
                        }
                    }
                }
            } catch (WrappedTargetException | IndexOutOfBoundsException e) {
                throw new SQLException(e.getLocalizedMessage(), e);
            }
        }
        return primaryKeys;
    }

    public static List<String> getCreatePrimaryKeyParts(NamedSupport support,
                                                        XPropertySet descriptor,
                                                        boolean sensitive)
        throws SQLException {
        List<String> queries = new ArrayList<>();
        XIndexAccess keys = queryInterface(XKeysSupplier.class, descriptor).getKeys();
        if (keys != null) {
            boolean hasPrimaryKey = false;
            try {
                for (int i = 0; i < keys.getCount(); i++) {
                    XPropertySet columnProperties = queryInterface(XPropertySet.class, keys.getByIndex(i));
                    if (columnProperties != null) {
                        setCreatePrimaryKeyQueries(support, queries, columnProperties, sensitive, hasPrimaryKey);
                    }
                }
            } catch (WrappedTargetException | IndexOutOfBoundsException e) {
                throw new SQLException(e.getLocalizedMessage(), e);
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
        XColumnsSupplier columnsSupplier = queryInterface(XColumnsSupplier.class, columnProperties);
        XIndexAccess columns = queryInterface(XIndexAccess.class, columnsSupplier.getColumns());
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
                NamedComponent nameComponents = qualifiedNameComponents(support, refTable);
                String composedName = buildName(support, nameComponents, true);
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
        return getKeyColumns(indexes, name).stream()
                .map(e -> support.enquoteIdentifier(e, sensitive))
                .toList().toArray(new String[0]);
    }

    private static List<String> getKeyColumns(XIndexAccess indexes,
                                              PropertyID name)
        throws SQLException {
        List<String> columns = new ArrayList<>();
        try {
            for (int i = 0; i < indexes.getCount(); i++) {
                XPropertySet property = queryInterface(XPropertySet.class, indexes.getByIndex(i));
                if (property != null) {
                    columns.add(DBTools.getDescriptorStringValue(property, name));
                }
            }
        } catch (IndexOutOfBoundsException | WrappedTargetException e) {
            throw new SQLException(e.getLocalizedMessage(), e);
        }
        return columns;
    }
}
