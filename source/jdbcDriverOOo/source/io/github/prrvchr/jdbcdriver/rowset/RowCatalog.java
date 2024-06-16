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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.resultset.ResultSetWrapper;

public class RowCatalog
    implements Iterable<RowTable>
{

    private List<RowTable> m_Tables = new ArrayList<>();
    private RowColumn[] m_Columns;
    private ComposeRule rule = ComposeRule.InDataManipulation;
    private String m_SelectCmd = "SELECT * FROM %s WHERE %s";
    private String m_Mark = "?";
    private String m_Parameter = "%s = ?";
    private String m_And = " AND ";
    private String m_Separator = ", ";

    // The constructor method:
    // XXX: this constructor is called from sdbc.StatementMain.getStatementCatalog()
    public RowCatalog(DriverProvider provider,
                      Statement statement,
                      String identifier)
        throws SQLException
    {
        List<RowColumn> columns = new ArrayList<>();
        ComposeRule rule = ComposeRule.InDataManipulation;
        NamedComponents component = DBTools.qualifiedNameComponents(provider, identifier, rule, true);
        RowTable table = new RowTable(this, component);
        try (ResultSet result = provider.getConnection().getMetaData().getColumns(component.getCatalog(), component.getSchema(), component.getTable(), "%")) {
            while (result.next()) {
                columns.add(new RowColumn(table, statement, result));
            }
        }
        m_Tables.add(table);
        m_Columns = columns.toArray(new RowColumn[0]);
        System.out.println("RowCatalog() 1");
    }
    // XXX: this constructor is called from rowset.RowSetWriter()
    public RowCatalog(Statement statement,
                      ResultSet result)
        throws SQLException
    {
        List<RowColumn> columns = new ArrayList<>();
        ResultSetMetaData metadata = result.getMetaData();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            RowTable table = getTable(metadata, index);
            columns.add(new RowColumn(table, statement, metadata, index));
        }
        m_Columns = columns.toArray(new RowColumn[0]);
        System.out.println("RowCatalog() 1");
    }

    public RowTable getMainTable()
        throws SQLException
    {
        if (m_Tables.isEmpty()) {
            throw new SQLException();
        }
        return m_Tables.get(0);
    }

    public RowColumn[] getColumns()
    {
        return m_Columns;
    }

    public Map<String, RowColumn> getColumnNames(RowTable table)
    {
        Map<String, RowColumn> columns = new HashMap<>();
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table)) {
                columns.put(column.getName(), column);
            }
        }
        return columns;
    }

    public Map<Integer, RowColumn> getColumnIndexes(RowTable table)
    {
        Map<Integer, RowColumn> columns = new HashMap<>();
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table)) {
                columns.put(column.getIndex(), column);
            }
        }
        return columns;
    }

    public ComposeRule getRule()
    {
        return rule;
    }

    public String getMark()
    {
        return m_Mark;
    }

    public String getParameter()
    {
        return m_Parameter;
    }

    public String getAnd()
    {
        return m_And;
    }

    public String getSeparator()
    {
        return m_Separator;
    }

    public PreparedStatement getSelectStatement(DriverProvider provider,
                                                RowTable table)
        throws SQLException
    {
        String query = String.format(m_SelectCmd, table.getComposedName(provider, true), table.getWhereCmd());
        System.out.println("RowCatalog.getSelectStatement() Query: " + query);
        return provider.getConnection().prepareStatement(query);
    }

    public ResultSet getSelectResult(DriverProvider provider,
                                     RowTable table,
                                     Row row)
        throws SQLException
    {
        PreparedStatement prepared = getSelectStatement(provider, table);
        RowHelper.setWhereParameter(prepared, this, table, row);
        return new ResultSetWrapper(prepared);
    }

    public RowColumn getAutoIncrementColumn(RowTable table, int type)
    {
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table) && column.isAutoIncrement() && column.getType() == type) {
                System.out.println("RowCatalog.getAutoIncrementColumn() 1");
                return column;
            }
        }
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table) && column.getType() == type) {
                return column;
            }
        }
        return null;
    }

    @Override
    public Iterator<RowTable> iterator() {
        return m_Tables.iterator();
    }

    private RowTable getTable(ResultSetMetaData metadata, int index)
        throws SQLException
    {
        for (RowTable table : m_Tables) {
            if (table.isSameTable(metadata, index)) {
                return table;
            }
        }
        RowTable table = new RowTable(this, metadata, index);
        m_Tables.add(table);
        return table;
    }

}
