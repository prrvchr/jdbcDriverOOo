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
package io.github.prrvchr.jdbcdriver.rowset;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.prrvchr.jdbcdriver.DriverProvider;


public class RowSetWriter
{

    private DriverProvider m_Provider;
    private RowCatalog m_Catalog;
    private String m_InsertCmd = "INSERT INTO %s (%s) VALUES (%s)";
    private String m_UpdateCmd = "UPDATE %s SET %s WHERE %s";
    private String m_DeleteCmd = "DELETE FROM %s WHERE %s";

    // The constructor method:
    public RowSetWriter(DriverProvider provider,
                        RowCatalog catalog,
                        ResultSet result,
                        String query)
        throws SQLException
    {
        m_Provider = provider;
        // XXX: We can make lazy loading on catalog if ResultSet is updatable
        if (catalog == null) {
            catalog = new RowCatalog(provider, result, query);
        }
        m_Catalog = catalog;
    }

    public boolean insertRow(BaseRow row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog.getTables()) {
            List<RowColumn> columns = getModifiedColumns(table, row);
            if (!columns.isEmpty()) {
                try (PreparedStatement statement = getInsertStatement(table, columns)) {
                    setStatementParameter(statement, columns, row);
                    status = statement.executeUpdate();
                    if (status == 1) {
                        m_Provider.setGeneratedKeys(statement, table, row);
                    }
                }
            }
        }
        return status != 0;
    }

    public boolean updateRow(Row row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog.getTables()) {
            status = 0;
            List<RowColumn> columns = getModifiedColumns(table, row);
            if (!columns.isEmpty()) {
                checkForUpdate(table, row, columns);
                try (PreparedStatement statement = getUpdateStatement(table, columns)) {
                    int index = setStatementParameter(statement, columns, row);
                    RowHelper.setWhereParameter(statement, m_Catalog, table, row, index);
                    status = statement.executeUpdate();
                }
                row.clearUpdated(columns, status);
            }
        }
        return status != 0;
    }

    public boolean deleteRow(Row row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog.getTables()) {
            status = 0;
            checkForDelete(table, row);
            try (PreparedStatement statement = getDeleteStatement(table)) {
                RowHelper.setWhereParameter(statement, m_Catalog, table, row);
                status = statement.executeUpdate();
            }
        }
        return status != 0;
    }

    // XXX: Private methods
    private List<RowColumn> getModifiedColumns(RowTable table,
                                               BaseRow row)
    {
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
        throws SQLException
    {
        int count = getResultRowCount(table, row);
        if (count != 1) {
            row.clearUpdated(columns, 0);
            row.clearUpdatable();
            throw new SQLException("ERROR: It is not possible to precisely identify the record selected for updateRow()");
        }
    }

    private void checkForDelete(RowTable table,
                                Row row)
        throws SQLException
    {
        int count = getResultRowCount(table, row);
        if (count != 1) {
            throw new SQLException("ERROR: It is not possible to precisely identify the record selected for deleteRow()");
        }
    }

    private int getResultRowCount(RowTable table,
                                 Row row)
        throws SQLException
    {
        int count = 0;
        try (PreparedStatement statement = m_Catalog.getSelectStatement(table)) {
            RowHelper.setWhereParameter(statement, m_Catalog, table, row);
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

    private PreparedStatement getInsertStatement(RowTable table,
                                                 List<RowColumn> columns)
        throws SQLException
    {
        PreparedStatement statement = null;
        String query = String.format(m_InsertCmd, table.getComposedName(true), getInsertColumns(table, columns), getInsertParameter(table, columns));
        System.out.println("RowSetWriter.getInsertStatement() Query: " + query);
        if (m_Provider.isAutoRetrievingEnabled()) {
            statement = m_Provider.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        }
        else {
            statement = m_Provider.getConnection().prepareStatement(query);
        }
        return statement;
    }

    private PreparedStatement getUpdateStatement(RowTable table,
                                                 List<RowColumn> columns)
        throws SQLException
    {
        String query = String.format(m_UpdateCmd, table.getComposedName(true), getUpdatedColumns(table, columns), table.getWhereCmd());
        System.out.println("RowSetWriter.getUpdateStatement() Query: " + query);
        return m_Provider.getConnection().prepareStatement(query);
    }

    private PreparedStatement getDeleteStatement(RowTable table)
        throws SQLException
    {
        String query = String.format(m_DeleteCmd, table.getComposedName(true), table.getWhereCmd());
        System.out.println("RowSetWriter.getDeleteStatement() Query: " + query);
        return m_Provider.getConnection().prepareStatement(query);
    }

    private String getInsertColumns(RowTable table,
                                    List<RowColumn> columns)
    {
        List<String> query = new ArrayList<>();
        for (RowColumn column : columns) {
            query.add(column.getIdentifier());
        }
        return String.join(table.getSeparator(), query);
    }

    private String getInsertParameter(RowTable table,
                                      List<RowColumn> columns)
    {
        String[] marks = new String[columns.size()];
        Arrays.fill(marks, table.getMark()) ;
        return String.join(table.getSeparator(), marks);
    }

    private String getUpdatedColumns(RowTable table,
                                     List<RowColumn> columns)
    {
        List<String> query = new ArrayList<>();
        for (RowColumn column : columns) {
            query.add(column.getPredicate());
        }
        return String.join(table.getSeparator(), query);
    }

    private int setStatementParameter(PreparedStatement statement,
                                      List<RowColumn> columns,
                                      BaseRow row)
        throws SQLException
    {
        int i = 1;
        for (RowColumn column : columns) {
            Object value = row.getColumnObject(column.getIndex());
            RowHelper.setStatementValue(statement, column.getType(), i, value);
            i ++;
        }
        return i;
    }

}
