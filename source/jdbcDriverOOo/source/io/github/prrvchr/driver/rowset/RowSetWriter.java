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
package io.github.prrvchr.driver.rowset;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.prrvchr.driver.provider.DriverProvider;


public class RowSetWriter {

    private DriverProvider mProvider;
    private RowCatalog mCatalog;
    private String mInsertCmd = "INSERT INTO %s (%s) VALUES (%s)";
    private String mUpdateCmd = "UPDATE %s SET %s WHERE %s";
    private String mDeleteCmd = "DELETE FROM %s WHERE %s";

    // The constructor method:
    public RowSetWriter(DriverProvider provider,
                        RowCatalog catalog,
                        ResultSet result,
                        String table)
        throws SQLException {
        mProvider = provider;
        // XXX: We can do lazy loading on the catalog if ResultSet is updatable, because if
        // XXX: positioned update is available it will be preferred over SQL mode (ie: RowSetWriter)
        if (catalog == null) {
            catalog = new RowCatalog(provider, result, table);
        }
        mCatalog = catalog;
    }

    public boolean insertRow(BaseRow row)
        throws SQLException {
        int status = 0;
        for (RowTable table: mCatalog.getTables()) {
            List<RowColumn> columns = getModifiedColumns(table, row);
            if (!columns.isEmpty()) {
                String query = row.getQuery(mInsertCmd, table.getComposedName(true),
                                            getInsertColumns(table, columns), getInsertParameter(table, columns));
                try (PreparedStatement statement = getInsertStatement(query)) {
                    setStatementParameter(statement, columns, row);
                    status = statement.executeUpdate();
                    if (status == 1) {
                        mProvider.setGeneratedKeys(statement, table, row);
                    }
                }
            }
        }
        return status != 0;
    }

    public boolean updateRow(Row row)
        throws SQLException {
        int status = 0;
        for (RowTable table: mCatalog.getTables()) {
            status = 0;
            List<RowColumn> columns = getModifiedColumns(table, row);
            if (!columns.isEmpty()) {
                checkForUpdate(table, row, columns);
                String query = row.getQuery(mUpdateCmd, table.getComposedName(true),
                                            getUpdatedColumns(table, columns), table.getWhereCmd());
                try (PreparedStatement statement = mProvider.getConnection().prepareStatement(query)) {
                    int index = setStatementParameter(statement, columns, row);
                    RowHelper.setWhereParameter(statement, mCatalog, table, row, index);
                    status = statement.executeUpdate();
                }
                row.clearUpdated(columns, status);
            }
        }
        return status != 0;
    }

    public boolean deleteRow(Row row)
        throws SQLException {
        int status = 0;
        for (RowTable table: mCatalog.getTables()) {
            status = 0;
            checkForDelete(table, row);
            String query = row.getQuery(mDeleteCmd, table.getComposedName(true), table.getWhereCmd());
            try (PreparedStatement statement = mProvider.getConnection().prepareStatement(query)) {
                RowHelper.setWhereParameter(statement, mCatalog, table, row);
                status = statement.executeUpdate();
            }
        }
        return status != 0;
    }

    // XXX: Private methods
    private List<RowColumn> getModifiedColumns(RowTable table,
                                               BaseRow row) {
        List<RowColumn> columns = new ArrayList<>();
        for (RowColumn column : table.getColumns()) {
            if (row.isColumnSet(column.getIndex())) {
                columns.add(column);
            }
        }
        return columns;
    }

    private void checkForUpdate(RowTable table,
                                Row row,
                                List<RowColumn> columns)
        throws SQLException {
        int count = getResultRowCount(table, row);
        if (count != 1) {
            row.clearUpdated(columns, 0);
            row.clearUpdatable();
            String msg = "ERROR: It is not possible to precisely identify the record selected for updateRow()";
            throw new SQLException(msg);
        }
    }

    private void checkForDelete(RowTable table,
                                Row row)
        throws SQLException {
        int count = getResultRowCount(table, row);
        if (count != 1) {
            String msg = "ERROR: It is not possible to precisely identify the record selected for deleteRow()";
            throw new SQLException(msg);
        }
    }

    private int getResultRowCount(RowTable table,
                                  Row row)
        throws SQLException {
        int count = 0;
        try (PreparedStatement statement = mCatalog.getSelectStatement(table)) {
            RowHelper.setWhereParameter(statement, mCatalog, table, row);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                count ++;
                if (count > 1) {
                    break;
                }
            }
        }
        return count;
    }

    private PreparedStatement getInsertStatement(String query)
        throws SQLException {
        PreparedStatement statement = null;
        if (mProvider.getSQLQuery().isAutoRetrievingEnabled()) {
            statement = mProvider.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        } else {
            statement = mProvider.getConnection().prepareStatement(query);
        }
        return statement;
    }

    private String getInsertColumns(RowTable table,
                                    List<RowColumn> columns) {
        List<String> query = new ArrayList<>();
        for (RowColumn column : columns) {
            query.add(column.getIdentifier());
        }
        return String.join(table.getSeparator(), query);
    }

    private String getInsertParameter(RowTable table,
                                      List<RowColumn> columns) {
        String[] marks = new String[columns.size()];
        Arrays.fill(marks, table.getMark()) ;
        return String.join(table.getSeparator(), marks);
    }

    private String getUpdatedColumns(RowTable table,
                                     List<RowColumn> columns) {
        List<String> query = new ArrayList<>();
        for (RowColumn column : columns) {
            query.add(column.getPredicate());
        }
        return String.join(table.getSeparator(), query);
    }

    private int setStatementParameter(PreparedStatement statement,
                                      List<RowColumn> columns,
                                      BaseRow row)
        throws SQLException {
        int i = 1;
        for (RowColumn column : columns) {
            Object value = row.getColumnObject(column.getIndex());
            RowHelper.setStatementValue(statement, column.getType(), i, value);
            i ++;
        }
        return i;
    }

}
