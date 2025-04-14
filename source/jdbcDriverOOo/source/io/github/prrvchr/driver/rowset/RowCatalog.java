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
package io.github.prrvchr.driver.rowset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.DBTools.NameComponentSupport;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.resultset.ResultSetWrapper;

public class RowCatalog {
    //implements Iterable<RowTable>

    private DriverProvider mProvider;
    private List<RowTable> mTables = new ArrayList<>();
    private ComposeRule mRule = ComposeRule.InDataManipulation;
    private NameComponentSupport mSupport = null;
    private String mSelectCmd = "SELECT * FROM %1$s WHERE %2$s";
    private String mUniqueCmd = "SELECT %1$s, COUNT(%1$s) FROM %2$s GROUP BY %1$s HAVING COUNT(%1$s) > 1";
    private String mMark = "?";
    private String mParameter = "%s = ?";
    private String mAnd = " AND ";
    private String mSeparator = ", ";

    // The constructor method:
    // XXX: this constructor is called from sdbc.StatementMain.getStatementCatalog()
    public RowCatalog(DriverProvider provider,
                      String identifier)
        throws SQLException {
        mProvider = provider;
        mSupport = DBTools.getNameComponentSupport(provider, mRule);
        NamedComponents component = DBTools.qualifiedNameComponents(provider, identifier, mRule, true);
        RowTable table = new RowTable(provider.getConnection(), this, component, true);
        mTables.add(table);
        setRowIdentifier(table);
    }

    // XXX: this constructor is called from rowset.RowSetWriter()
    public RowCatalog(DriverProvider provider,
                      ResultSet result,
                      String identifier)
        throws SQLException {
        mProvider = provider;
        RowTable table = null;
        NamedComponents component = null;
        Connection connection = provider.getConnection();
        mSupport = DBTools.getNameComponentSupport(provider, mRule);
        ResultSetMetaData metadata = result.getMetaData();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            String name = metadata.getTableName(index);
            if (name == null || name.isBlank()) {
                table = getTable(connection, component, identifier);
            } else {
                table = getTable(connection, metadata, index);
            }
            if (table.isValid()) {
                table.setIndexColumn(metadata.getColumnName(index), index);
            }
        }
        setTableIdentifier();
    }

    public boolean hasRowIdentifier() {
        boolean has = true;
        boolean multiple = getTableCount() > 1;
        for (RowTable table : mTables) {
            if (multiple) {
                has &= table.hasRowIdentifier() && table.isIdentifierPrimaryKey();
            } else {
                has = table.hasRowIdentifier();
            }
        }
        return !mTables.isEmpty() && has;
    }

    public int getTableCount() {
        return mTables.size();
    }

    public NameComponentSupport getNamedSupport()
        throws SQLException {
        return mSupport;
    }

    public DriverProvider getProvider()
        throws SQLException {
        return mProvider;
    }

    public Statement getStatement()
        throws SQLException {
        return mProvider.getStatement();
    }

    public String enquoteIdentifier(String name)
        throws SQLException {
        return mProvider.enquoteIdentifier(name, true);
    }

    public RowTable getMainTable()
        throws SQLException {
        if (mTables.isEmpty()) {
            throw new SQLException();
        }
        return mTables.get(0);
    }

    public ComposeRule getRule() {
        return mRule;
    }

    public String getMark() {
        return mMark;
    }

    public String getParameter() {
        return mParameter;
    }

    public String getAnd() {
        return mAnd;
    }

    public String getSeparator() {
        return mSeparator;
    }

    public PreparedStatement getSelectStatement(RowTable table)
        throws SQLException {
        String query = String.format(mSelectCmd, table.getComposedName(true), table.getWhereCmd());
        return getStatement().getConnection().prepareStatement(query);
    }

    public ResultSet getSelectResult(RowTable table,
                                     Row row)
        throws SQLException {
        PreparedStatement prepared = getSelectStatement(table);
        RowHelper.setWhereParameter(prepared, this, table, row);
        return new ResultSetWrapper(prepared);
    }

    public String getUniqueQuery() {
        return mUniqueCmd;
    }

    public List<RowTable> getTables() {
        return mTables;
    }


    private void setTableIdentifier()
        throws SQLException {
        for (RowTable table : getTables()) {
            if (table.isValid()) {
                setRowIdentifier(table);
            }
        }
    }

    private void setRowIdentifier(RowTable table)
        throws SQLException {
        try (ResultSet result = getStatement().getConnection().getMetaData().getPrimaryKeys(table.getCatalogName(),
                                                                                            table.getSchemaName(),
                                                                                            table.getName())) {
            while (result.next()) {
                // CHECKSTYLE:OFF: MagicNumber - Specific for database
                String key = result.getString(4); 
                if (!result.wasNull() && table.hasColumn(key)) {
                    short index = result.getShort(5);
                    table.addRowIdentifier(key, index - 1);
                    table.setIdentifierAsPrimaryKey();
                }
                // CHECKSTYLE:ON: MagicNumber - Specific for database
            }
        }
        if (!table.hasRowIdentifier()) {
            table.setDefaultRowIdentifier();
        }
    }

    private RowTable getTable(Connection connection,
                              ResultSetMetaData metadata,
                              int index)
        throws SQLException {
        RowTable table = null;
        for (RowTable t : mTables) {
            if (t.isSameTable(metadata, index)) {
                table = t;
            }
        }
        if (table == null) {
            table = new RowTable(connection, this, metadata, index);
            if (table.isValid()) {
                mTables.add(table);
            }
        }
        return table;
    }

    private RowTable getTable(Connection connection,
                              NamedComponents component,
                              String identifier)
        throws SQLException {
        if (component == null) {
            component = getNamedComponent(identifier);
        }
        RowTable table = null;
        for (RowTable t : mTables) {
            if (t.isSameTable(component)) {
                table = t;
            }
        }
        if (table == null) {
            table = new RowTable(connection, this, component);
            if (table.isValid()) {
                mTables.add(table);
            }
        }
        return table;
    }

    private NamedComponents getNamedComponent(String identifier)
        throws SQLException {
        return DBTools.qualifiedNameComponents(identifier, mSupport, true);
    }

}
