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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.sdbc.IndexType;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.sdbcx.Index;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public class IndexHelper {

    public static String[] readIndexes(Provider provider,
                                       NamedComponents component,
                                       boolean qualified)
        throws java.sql.SQLException {
        List<String> names = new ArrayList<>();
        DatabaseMetaData metadata = provider.getConnection().getMetaData();
        String separator = metadata.getCatalogSeparator();
        ConfigSQL config = provider.getConfigSQL();
        try (java.sql.ResultSet result = metadata.getIndexInfo(config.getMetaDataIdentifier(component.getCatalog()),
                                                               config.getMetaDataIdentifier(component.getSchema()),
                                                               config.getMetaDataIdentifier(component.getTable()),
                                                               false, false)) {
            String name;
            String previous = "";
            final int INDEX_QUALIFIER = 5;
            final int INDEX_NAME = 6;
            while (result.next()) {
                StringBuilder buffer = new StringBuilder();
                if (qualified) {
                    String qualifier = result.getString(INDEX_QUALIFIER);
                    if (!result.wasNull() && !qualifier.isEmpty()) {
                        buffer.append(qualifier);
                        buffer.append(separator);
                    }
                }
                name = result.getString(INDEX_NAME);
                if (!result.wasNull()) {
                    buffer.append(name);
                    name = buffer.toString();
                    // XXX: Don't insert the name if the last one we inserted was the same
                    if (!name.isEmpty() && !previous.equals(name)) {
                        names.add(name);
                        previous = name;
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

    public static boolean isPrimaryKeyIndex(java.sql.DatabaseMetaData metadata,
                                            NamedComponents table,
                                            String name)
        throws java.sql.SQLException {
        boolean primary = false;
        final int PK_NAME = 6;
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(),
                                                                 table.getTable())) {
            // XXX: There can be only one primary key
            if (result.next()) {
                primary = name.equals(result.getString(PK_NAME));
            }
        }
        return primary;
    }

    public static Index createIndex(Provider provider,
                                    DatabaseMetaData metadata,
                                    TableSuper table,
                                    NamedComponents component,
                                    String qualifier,
                                    String subname,
                                    boolean sensitive)
        throws SQLException {
        Index index = null;
        IndexProperties properties = getIndexProperties(provider, metadata, component, qualifier, subname);
        if (properties != null) {
            Boolean primary = isPrimaryKeyIndex(metadata, component, subname);
            index = new Index(table, sensitive, subname, qualifier, properties.isUnique(),
                              primary, properties.isClustered(), properties.getColumns());
        }
        return index;
        
    }

    public static IndexProperties getIndexProperties(Provider provider,
                                                     DatabaseMetaData metadata,
                                                     NamedComponents table,
                                                     String qualifier,
                                                     String subname)
        throws java.sql.SQLException {
        boolean found = false;
        IndexProperties properties = new IndexProperties();
        final int NON_UNIQUE = 4;
        final int INDEX_QUALIFIER = 5;
        final int INDEX_NAME = 6;
        final int TYPE = 7;
        final int COLUMN_NAME = 9;
        ConfigSQL config = provider.getConfigSQL();
        String catalog = config.getMetaDataIdentifier(table.getCatalog());
        String schema = config.getMetaDataIdentifier(table.getSchema());
        String name = config.getMetaDataIdentifier(table.getTable());
        try (ResultSet result = metadata.getIndexInfo(catalog, schema, name, false, false)) {
            while (result.next()) {
                properties.setUnique(!result.getBoolean(NON_UNIQUE));
                if ((qualifier.isEmpty() || qualifier.equals(result.getString(INDEX_QUALIFIER)))
                                         && subname.equals(result.getString(INDEX_NAME))) {
                    found = true;
                    properties.setType(result.getShort(TYPE));
                    String columnName = result.getString(COLUMN_NAME);
                    if (!result.wasNull()) {
                        properties.addColumn(columnName);
                    }
                }
            }
        }
        if (!found) {
            properties = null;
        }
        return properties;
    }


    // XXX: Private helper function
    public static class IndexProperties {
        public boolean mUnique;
        public int mType = -1;
        public List<String> mColumns = new ArrayList<>();

        private IndexProperties() { }

        public void setType(int type) {
            mType = type;
        }

        public void setUnique(boolean unique) {
            mUnique = unique;
        }

        public void addColumn(String column) {
            mColumns.add(column);
        }

        public boolean isClustered() {
            return mType == IndexType.CLUSTERED;
        }

        public boolean isUnique() {
            return mUnique;
        }

        public String[] getColumns() {
            return mColumns.toArray(new String[0]);
        }
    }

}
