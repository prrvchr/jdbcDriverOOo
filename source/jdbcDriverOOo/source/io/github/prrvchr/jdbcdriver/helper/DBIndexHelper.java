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
package io.github.prrvchr.jdbcdriver.helper;

import java.util.ArrayList;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;


public class DBIndexHelper
{

    public static ArrayList<String> readIndexes(DriverProvider provider,
                                                NamedComponents table,
                                                boolean qualified)
        throws java.sql.SQLException
    {
        ArrayList<String> names = new ArrayList<>();
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        String separator = metadata.getCatalogSeparator();
        try (java.sql.ResultSet result = metadata.getIndexInfo(table.getCatalog(), table.getSchema(), table.getTable(), false, false))
        {
            String previous = "";
            while (result.next()) {
                StringBuilder buffer = new StringBuilder();
                if (qualified) {
                    String qualifier = result.getString(5);
                    if (!result.wasNull() && !qualifier.isEmpty()) {
                        buffer.append(qualifier);
                        buffer.append(separator);
                    }
                }
                buffer.append(result.getString(6));
                String name = buffer.toString();
                // XXX: Don't insert the name if the last one we inserted was the same
                if (!result.wasNull() && !name.isEmpty() && !previous.equals(name)) {
                    names.add(name);
                    previous = name;
                }
            }
        }
        return names;
    }

    public static boolean isPrimaryKeyIndex(java.sql.DatabaseMetaData metadata,
                                            NamedComponents table,
                                            String name)
        throws java.sql.SQLException
    {
        boolean primary = false;
        try (java.sql.ResultSet result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getTable()))
        {
            // XXX: There can be only one primary key
            if (result.next()) {
                primary = name.equals(result.getString(6));
            }
        }
        return primary;
    }

}
