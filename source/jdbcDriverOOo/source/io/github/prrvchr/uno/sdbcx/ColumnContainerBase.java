/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DataBaseTableHelper;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DataBaseTableHelper.ColumnDescription;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public abstract class ColumnContainerBase
    extends Container
{

    private Map<String, ColumnDescription> m_descriptions = new HashMap<>();
    private Map<String, ExtraColumnInfo> m_extrainfos = new HashMap<>();
    protected final TableBase m_table;


    // The constructor method:
    public ColumnContainerBase(TableBase table,
                               boolean sensitive,
                               List<ColumnDescription> descriptions)
        throws ElementExistException
    {
        super(table, sensitive, toColumnNames(descriptions));
        m_table = table;
        for (ColumnDescription description : descriptions) {
            m_descriptions.put(description.columnName, description);
        }
    }

    private static List<String> toColumnNames(List<ColumnDescription> descriptions)
    {
        List<String> names = new ArrayList<>(descriptions.size());
        for (ColumnDescription description : descriptions) {
            names.add(description.columnName);
        }
        return names;
    }


    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        if (m_table == null) {
            return _cloneDescriptor(descriptor);
        }
        try {
            String table = DBTools.composeTableName(_getConnection(), m_table, ComposeRule.InTableDefinitions, false, false, true);
            String column = DBTools.getStandardColumnPartQuery(_getConnection(), descriptor, null, m_table.getTypeCreatePattern());
            String sql = String.format("ALTER TABLE %s ADD %s", table, column);
            java.sql.Statement statement = _getConnection().getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
            return _createElement(name);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        return null;
    }


    @Override
    protected ColumnSuper _createElement(String name)
        throws SQLException
    {
        ColumnSuper column = null;
        try {
            @SuppressWarnings("unused")
            boolean queryInfo = true;
            boolean isAutoIncrement = false;
            boolean isCurrency = false;
            @SuppressWarnings("unused")
            int dataType = DataType.OTHER;
            
            ColumnDescription description = m_descriptions.get(name);
            if (description == null) {
                // could be a recently added column. Refresh:
                List<ColumnDescription> newcolumns = DataBaseTableHelper.readColumns(_getConnection(), m_table);
                for (ColumnDescription newcolumn : newcolumns) {
                    if (newcolumn.columnName.equals(name)) {
                        m_descriptions.put(name, newcolumn);
                        break;
                    }
                }
                description = m_descriptions.get(name);
            }
            if (description == null) {
                throw new SQLException("No column " + name + " found");
            }
            
            ExtraColumnInfo info = m_extrainfos.get(name);
            if (info == null) {
                String composedName = DBTools.composeTableNameForSelect(_getConnection(), m_table);
                m_extrainfos = DBTools.collectColumnInformation(_getConnection(), composedName, "*");
                info = m_extrainfos.get(name);
            }
            if (info != null) {
                queryInfo = false;
                isAutoIncrement = info.isAutoIncrement;
                isCurrency = info.isCurrency;
                dataType = info.dataType;
            }
            
            XNameAccess primaryKeyColumns = DBTools.getPrimaryKeyColumns(m_table.getKeys());
            int nullable = description.nullable;
            if (nullable != ColumnValue.NO_NULLS && primaryKeyColumns != null && primaryKeyColumns.hasByName(name)) {
                nullable = ColumnValue.NO_NULLS;
            }
            column = _getColumn(name, description.typeName, description.defaultValue, description.remarks,
                                nullable, description.columnSize, description.decimalDigits, description.type,
                                isAutoIncrement, false, isCurrency);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_table);
        }
        return column;
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        UnoHelper.ensure(m_table, "Table is null!", _getConnection().getLogger());
        if (m_table == null) {
            return;
        }
        try {
            String quote = _getConnection().getProvider().getConnection().getMetaData().getIdentifierQuoteString();
            String sql = String.format("ALTER TABLE %s DROP %s",
                    DBTools.composeTableName(_getConnection(), m_table, ComposeRule.InTableDefinitions, false, false, true),
                    DBTools.quoteName(quote, name));
            java.sql.Statement statement = _getConnection().getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_table);
        }
    }

    /// The XDatabaseMetaData.getColumns() data stored in columnDescriptions doesn't provide everything we need, so this class stores the rest.
    public static class ExtraColumnInfo
    {
        public boolean isAutoIncrement;
        public boolean isCurrency;
        public int dataType;
    }

    
    @Override
    protected void _refresh() {
        m_extrainfos.clear();
        // FIXME: won't help
        m_table._refreshColumns();
    }

    public ConnectionSuper _getConnection()
    {
        return m_table.getConnection();
    }

    protected abstract ColumnSuper _getColumn(String name,
                                              String typename,
                                              String defaultvalue,
                                              String description,
                                              int nullable,
                                              int precision,
                                              int scale,
                                              int type,
                                              boolean autoincrement,
                                              boolean rowversion,
                                              boolean currency);


}
