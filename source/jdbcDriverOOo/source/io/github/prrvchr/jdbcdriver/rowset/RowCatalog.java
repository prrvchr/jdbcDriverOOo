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
import java.util.List;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBQueryParser;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NameComponentSupport;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.resultset.ResultSetWrapper;

public class RowCatalog
    //implements Iterable<RowTable>
{

    private Statement m_Statement;
    private List<RowTable> m_Tables = new ArrayList<>();
    private ComposeRule m_Rule = ComposeRule.InDataManipulation;
    private NameComponentSupport m_Support = null;
    private String m_SelectCmd = "SELECT * FROM %1$s WHERE %2$s";
    private String m_UniqueCmd = "SELECT %1$s, COUNT(%1$s) FROM %2$s GROUP BY %1$s HAVING COUNT(%1$s) > 1";
    private String m_Mark = "?";
    private String m_Parameter = "%s = ?";
    private String m_And = " AND ";
    private String m_Separator = ", ";

    // The constructor method:
    // XXX: this constructor is called from sdbc.StatementMain.getStatementCatalog()
    public RowCatalog(DriverProvider provider,
                      String identifier)
        throws SQLException
    {
        m_Statement = provider.getStatement();
        m_Support = DBTools.getNameComponentSupport(provider, m_Rule);
        NamedComponents component = DBTools.qualifiedNameComponents(provider, identifier, m_Rule, true);
        RowTable table = new RowTable(this, component);
        try (ResultSet result = provider.getConnection().getMetaData().getColumns(component.getCatalog(), component.getSchema(), component.getTable(), "%")) {
            while (result.next()) {
                table.addColumn(new RowColumn(table, result));
            }
        }
        m_Tables.add(table);
        setRowIdentifier(table);
    }

    // XXX: this constructor is called from rowset.RowSetWriter()
    public RowCatalog(DriverProvider provider,
                      ResultSet result,
                      String query)
        throws SQLException
    {
        RowTable table = null;
        NamedComponents component = null;
        m_Statement = provider.getStatement();
        m_Support = DBTools.getNameComponentSupport(provider, m_Rule);
        ResultSetMetaData metadata = result.getMetaData();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            if (metadata.getTableName(index).isBlank()) {
                table = getTable(component, query);
                if (table.isValid()) {
                    table.addColumn(new RowColumn(provider.getConnection(), table, metadata, index));
                }
            }
            else {
                table = getTable(metadata, index);
                if (table.isValid()) {
                    table.addColumn(new RowColumn(table, metadata, index));
                }
            }
        }
        if (table == null) {
            throw new SQLException();
        }
        setRowIdentifier(table);
    }

    private void setRowIdentifier(RowTable table)
        throws SQLException
    {
        try (ResultSet result = m_Statement.getConnection().getMetaData().getPrimaryKeys(table.getCatalogName(), table.getSchemaName(), table.getName())) {
            while (result.next()) {
                String key = result.getString(4);
                if (!result.wasNull() && table.hasColumn(key)) {
                    short index = result.getShort(5);
                    table.addRowIdentifier(key, index - 1);
                }
            }
        }
        if (!table.hasRowIdentifier()) {
            table.setDefaultRowIdentifier();
        }
    }

    public boolean hasRowIdentifier()
    {
        for (RowTable table : m_Tables) {
            if (table.hasRowIdentifier()) {
                return true;
            }
        }
        return false;
    }

    public NameComponentSupport getNamedSupport()
        throws SQLException
    {
        return m_Support;
    }

    public Statement getStatement()
        throws SQLException
    {
        return m_Statement;
    }

    public String enquoteIdentifier(String name)
        throws SQLException
    {
        return m_Statement.enquoteIdentifier(name, true);
    }

    public RowTable getMainTable()
        throws SQLException
    {
        if (m_Tables.isEmpty()) {
            throw new SQLException();
        }
        return m_Tables.get(0);
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

    public PreparedStatement getSelectStatement(RowTable table)
        throws SQLException
    {
        String query = String.format(m_SelectCmd, table.getComposedName(true), table.getWhereCmd());
        return m_Statement.getConnection().prepareStatement(query);
    }

    public ResultSet getSelectResult(RowTable table,
                                     Row row)
        throws SQLException
    {
        PreparedStatement prepared = getSelectStatement(table);
        RowHelper.setWhereParameter(prepared, this, table, row);
        return new ResultSetWrapper(prepared);
    }

    public String getUniqueQuery()
    {
        return m_UniqueCmd;
    }

    public List<RowTable> getTables() {
        return m_Tables;
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

    private RowTable getTable(NamedComponents component,
                              String query)
        throws SQLException
    {
        if (component == null) {
            component = getNamedComponent(query);
        }
        for (RowTable table : m_Tables) {
            if (table.isSameTable(component)) {
                return table;
            }
        }
        RowTable table = new RowTable(this, component);
        if (table.isValid()) {
            m_Tables.add(table);
        }
        return table;
    }

    private NamedComponents getNamedComponent(String query)
        throws SQLException
    {
        DBQueryParser parser = new DBQueryParser(DBQueryParser.SQL_SELECT, query);
        if (!parser.hasTable()) {
            throw new SQLException();
        }
        NamedComponents component = DBTools.qualifiedNameComponents(m_Statement, parser.getTable(), m_Support, true);
        return component;
    }


}
