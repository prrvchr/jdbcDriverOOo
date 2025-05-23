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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.resultset.ResultSetWrapper;
import io.github.prrvchr.driver.rowset.RowCatalog;
import io.github.prrvchr.driver.rowset.RowColumn;
import io.github.prrvchr.driver.rowset.RowHelper;
import io.github.prrvchr.driver.rowset.RowTable;


public class GeneratedKeys {

    // XXX: Method called from StatementMain.getGeneratedValues()
    public static final java.sql.ResultSet getGeneratedResult(DriverProvider provider,
                                                              java.sql.Statement statement,
                                                              RowCatalog catalog,
                                                              String command)
        throws SQLException {
        RowTable table = catalog.getMainTable();
        return getGeneratedKeys(provider, statement, table, table.getColumnNames(), command);
    }

    // XXX: Method called from DriverProviderMain.setGeneratedKeys()
    public static final java.sql.ResultSet getGeneratedResult(DriverProvider provider,
                                                              java.sql.Statement statement,
                                                              RowTable table,
                                                              Map<String, RowColumn> columns,
                                                              String command)
        throws SQLException {
        return getGeneratedKeys(provider, statement, table, columns, command);
    }

    private static ResultSet getGeneratedKeys(DriverProvider provider,
                                              Statement statement,
                                              RowTable table,
                                              Map<String, RowColumn> columns,
                                              String command)
        throws SQLException {
        ResultSet resultset = null;
        Map<RowColumn, Object> keys = new HashMap<>();
        try (ResultSet result = statement.getGeneratedKeys()) {
            ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();
            if (result.next()) {
                for (int i = 1; i <= count; i++) {
                    RowColumn column = null;
                    String name = metadata.getColumnName(i);
                    // XXX: First we follow the JDBC API and we are looking for column name...
                    if (columns.containsKey(name)) {
                        column = columns.get(name);
                        // XXX: It is important to preserve the type of the original ResultSet/Table columns
                        keys.put(column, RowHelper.getResultSetValue(result, i, column.getType()));
                    } else {
                        // XXX: Here we assume that only one column is returned by getGeneratedKeys() and its name
                        // XXX: is unknown. If it exists, the first auto-increment column of the table concerned by
                        // XXX: the insertion will be attached to the unique value returned by getGeneratedKeys().
                        column = table.getRowIdentifierColumn();
                        if (column != null) {
                            // XXX: It is important to preserve the type of the original ResultSet/Table columns
                            keys.put(column, RowHelper.getResultSetValue(result, i, column.getType()));
                            break;
                        }
                    }
                }
            }
        }
        if (!keys.isEmpty()) {
            // XXX: If we want to follow the UNO API we must return all the columns of the table
            String query = String.format(command, table.getComposedName(true), getPredicates(table.getCatalog(), keys));
            PreparedStatement prepared = provider.getConnection().prepareStatement(query);
            setPredicates(prepared, keys);
            // XXX: The statement will be wrapped in order to be closed correctly when closing the ResultSet.
            resultset = new ResultSetWrapper(prepared);
        }
        return resultset;
    }

    private static String getPredicates(RowCatalog catalog,
                                        Map<RowColumn, Object> keys) {
        List<String> predicates = new ArrayList<>();
        for (RowColumn column : keys.keySet()) {
            predicates.add(String.format(column.getPredicate()));
        }
        return String.join(catalog.getAnd(), predicates);
    }

    private static void setPredicates(PreparedStatement statement,
                                      Map<RowColumn, Object> keys)
        throws SQLException {
        int index = 1;
        for (Entry<RowColumn, Object> entry : keys.entrySet()) {
            RowHelper.setStatementValue(statement, entry.getKey().getType(), index, entry.getValue());
            index ++;
        }
    }

}
