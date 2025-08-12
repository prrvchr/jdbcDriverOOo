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

import java.util.ArrayList;
import java.util.List;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.Provider;


public class IndexHelper {

    public static String[] readIndexes(Provider provider,
                                       NamedComponents component,
                                       boolean qualified)
        throws java.sql.SQLException {
        List<String> names = new ArrayList<>();
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
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

}
