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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.prrvchr.jdbcdriver.DriverProvider;


public class RowSetWriter
{
    private Connection m_Connection;
    private RowCatalog m_Catalog;
    private RowColumn[] m_Columns;
    private String m_SelectCmd = "SELECT * FROM %s WHERE %s";
    private String m_InsertCmd = "INSERT INTO %s (%s) VALUES (%s)";
    private String m_UpdateCmd = "UPDATE %s SET %s WHERE %s";
    private String m_DeleteCmd = "DELETE FROM %s WHERE %s";

    public RowSetWriter(DriverProvider provider,
                        ResultSet result)
        throws SQLException
    {
        m_Connection = result.getStatement().getConnection();
        Statement statement = m_Connection.createStatement();
        ResultSetMetaData metadata = result.getMetaData();
        m_Catalog = new RowCatalog(provider);
        List<RowColumn> columns = new ArrayList<>();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            columns.add(new RowColumn(m_Catalog, statement, metadata, index));
        }
        statement.close();
        m_Columns = columns.toArray(new RowColumn[0]);
    }

    public boolean insertRow(BaseRow row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog) {
            System.out.println("RowSetWriter.insertRow() Catalog: " + table.getCatalogName() + " - Schema: " + table.getSchemaName() + " - Name: " + table.getName());
            List<RowColumn> columns = getUpdatedColumns(table, row);
            if (!columns.isEmpty()) {
                try (PreparedStatement statement = getInsertStatement(table, columns)) {
                    setValueParameter(statement, columns, row);
                    status = statement.executeUpdate();
                }
            }
        }
        return status != 0;
    }

    public boolean updateRow(BaseRow row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog) {
            System.out.println("RowSetWriter.updateRow() Catalog: " + table.getCatalogName() + " - Schema: " + table.getSchemaName() + " - Name: " + table.getName());
            status = 0;
            List<RowColumn> columns = getUpdatedColumns(table, row);
            if (!columns.isEmpty()) {
                checkRowIsUnique(table, row, true);
                try (PreparedStatement statement = getUpdateStatement(table, columns)) {
                    int index = setValueParameter(statement, columns, row);
                    setWhereParameter(statement, table, row, index);
                    status = statement.executeUpdate();
                }
                row.clearUpdated(columns, status);
            }
        }
        return status != 0;
    }

    public boolean deleteRow(BaseRow row)
        throws SQLException
    {
        int status = 0;
        for (RowTable table: m_Catalog) {
            System.out.println("RowSetWriter.deleteRow() Catalog: " + table.getCatalogName() + " - Schema: " + table.getSchemaName() + " - Name: " + table.getName());
            status = 0;
            checkRowIsUnique(table, row, false);
            try (PreparedStatement statement = getDeleteStatement(table)) {
                setWhereParameter(statement, table, row);
                status = statement.executeUpdate();
            }
        }
        return status != 0;
    }

    // XXX: Private methods
    private List<RowColumn> getUpdatedColumns(RowTable table,
                                              BaseRow row)
    {
        List<RowColumn> columns = new ArrayList<>();
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table) && row.isColumnUpdated(column.getIndex())) {
                columns.add(column);
            }
        }
        return columns;
    }

    private void checkRowIsUnique(RowTable table,
                                  BaseRow row,
                                  boolean update)
        throws SQLException
    {
        int count = 0;
        try (PreparedStatement statement = getUniqueStatement(table)) {
            setWhereParameter(statement, table, row);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                count ++;
                if (count > 1) {
                    break;
                }
            }
        }
        if (count != 1) {
            String msg = update ? "updateRow()" : "deleteRow()";
            throw new SQLException(String.format("ERROR: It is not possible to precisely identify the record selected for %s", msg));
        }
    }

    private PreparedStatement getInsertStatement(RowTable table,
                                                 List<RowColumn> columns)
        throws SQLException
    {
        String query = String.format(m_InsertCmd, table.getComposedName(true), getInsertColumns(table, columns), getInsertParameter(table, columns));
        System.out.println("RowSetWriter.getInsertStatement() Query: " + query);
        return m_Connection.prepareStatement(query);
    }

    private PreparedStatement getUpdateStatement(RowTable table,
                                                 List<RowColumn> columns)
        throws SQLException
    {
        String query = String.format(m_UpdateCmd, table.getComposedName(true), getUpdatedColumns(table, columns), table.getWhereCmd());
        System.out.println("RowSetWriter.getUpdateStatement() Query: " + query);
        return m_Connection.prepareStatement(query);
    }

    private PreparedStatement getDeleteStatement(RowTable table)
        throws SQLException
    {
        String query = String.format(m_DeleteCmd, table.getComposedName(true), table.getWhereCmd());
        System.out.println("RowSetWriter.getDeleteStatement() Query: " + query);
        return m_Connection.prepareStatement(query);
    }

    private PreparedStatement getUniqueStatement(RowTable table)
        throws SQLException
    {
        String query = String.format(m_SelectCmd, table.getComposedName(true), table.getWhereCmd());
        System.out.println("RowSetWriter.getUniqueStatement() Query: " + query);
        return m_Connection.prepareStatement(query);
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
            query.add(column.getUpdateParameter());
        }
        return String.join(table.getSeparator(), query);
    }

    private int setValueParameter(PreparedStatement statement,
                                  List<RowColumn> columns,
                                  BaseRow row)
        throws SQLException
    {
        int i = 1;
        for (RowColumn column : columns) {
            Object value = row.getColumnObject(column.getIndex());
            RowHelper.setRowValue(statement, column.getType(), i, value);
            i ++;
        }
        return i;
    }

    private void setWhereParameter(PreparedStatement statement,
                                   RowTable table,
                                   BaseRow row)
        throws SQLException
    {
        setWhereParameter(statement, table, row, 1);
    }

    private void setWhereParameter(PreparedStatement statement,
                                   RowTable table,
                                   BaseRow row,
                                   int offset)
        throws SQLException
    {
        int i = offset;
        for (int index : table.getKeyIndex()) {
            int type = m_Columns[index - 1].getType();
            Object value = row.getOldColumnObject(index);
            RowHelper.setRowValue(statement, type, i, value);
            i ++;
        }
    }

}
