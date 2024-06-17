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
import io.github.prrvchr.jdbcdriver.helper.DBQueryParser;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.resultset.ResultSetWrapper;

public class RowCatalog
    implements Iterable<RowTable>
{

    private List<RowTable> m_Tables = new ArrayList<>();
    private RowColumn[] m_Columns;
    private ComposeRule m_Rule = ComposeRule.InDataManipulation;
    private NamedComponents m_Component = null;
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
        NamedComponents component = DBTools.qualifiedNameComponents(provider, identifier, m_Rule, true);
        RowTable table = new RowTable(this, component);
        try (ResultSet result = provider.getConnection().getMetaData().getColumns(component.getCatalog(), component.getSchema(), component.getTable(), "%")) {
            while (result.next()) {
                columns.add(new RowColumn(table, statement, result));
            }
        }
        m_Tables.add(table);
        m_Columns = columns.toArray(new RowColumn[0]);
    }

    // XXX: this constructor is called from rowset.RowSetWriter()
    public RowCatalog(DriverProvider provider,
                      ResultSet result,
                      String query)
        throws SQLException
    {
        List<RowColumn> columns = new ArrayList<>();
        Statement statement = provider.getStatement();
        ResultSetMetaData metadata = result.getMetaData();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            if (metadata.getTableName(index).isBlank()) {
                RowTable table = getTable(provider, query);
                if (table.isValid()) {
                    columns.add(new RowColumn(provider, table, metadata, index));
                }
            }
            else {
                RowTable table = getTable(metadata, index);
                if (table.isValid()) {
                    columns.add(new RowColumn(table, statement, metadata, index));
                }
            }
        }
        m_Columns = columns.toArray(new RowColumn[0]);
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
        return m_Rule;
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

    public RowColumn getAutoIncrementColumn(RowTable table)
    {
        for (RowColumn column : m_Columns) {
            if (column.isColumnOfTable(table) && column.isAutoIncrement()) {
                return column;
            }
        }
        return null;
    }

    @Override
    public Iterator<RowTable> iterator() {
        return m_Tables.iterator();
    }

    private RowTable getTable(ResultSetMetaData metadata,
                              int index)
        throws SQLException
    {
        for (RowTable table : m_Tables) {
            if (table.isSameTable(metadata, index)) {
                return table;
            }
        }
        RowTable table = new RowTable(this, metadata, index);
        if (table.isValid()) {
            m_Tables.add(table);
        }
        return table;
    }

    private RowTable getTable(DriverProvider provider,
                              String query)
        throws SQLException
    {
        NamedComponents component = getNamedComponent(provider, query);
        for (RowTable table : m_Tables) {
            if (table.isSameTable(component)) {
                return table;
            }
        }
        RowTable table = new RowTable(this, component);
        if (table.isValid()) {
            System.out.println("RowCatalog.getTable() Table: " + table.getName());
            m_Tables.add(table);
        }
        return table;
    }
    
    private NamedComponents getNamedComponent(DriverProvider provider,
                                              String query)
        throws SQLException
    {
        if (m_Component == null) {
            DBQueryParser parser = new DBQueryParser(DBQueryParser.SQL_SELECT, query);
            if (parser.hasTable()) {
                m_Component = DBTools.qualifiedNameComponents(provider, parser.getTable(), m_Rule, true);
                System.out.println("RowCatalog.getNamedComponent() Table: " + parser.getTable());
            }
        }
        return m_Component;
    }


}
