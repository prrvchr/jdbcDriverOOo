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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBTableHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DBColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ColumnContainerBase
    extends Container<ColumnSuper>
{

    private Map<String, ColumnDescription> m_descriptions = new HashMap<>();
    private Map<String, ExtraColumnInfo> m_extrainfos = new HashMap<>();
    protected final TableSuper m_table;

protected abstract TableSuper getTable();

    // The constructor method:
    public ColumnContainerBase(String service,
                               String[] services,
                               TableSuper table,
                               boolean sensitive,
                               List<ColumnDescription> descriptions)
        throws ElementExistException
    {
        super(service, services, table, sensitive, toColumnNames(descriptions));
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
    protected String getElementName(List<String> names,
                                     XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME, this);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected ColumnSuper appendElement(XPropertySet descriptor,
                                            String name)
        throws SQLException
    {
        ColumnSuper column = null;
        if (createColumn(descriptor, name)) {
            column = createElement(name);
        }
        return column;
    }

    private boolean createColumn(XPropertySet descriptor, String name)
        throws SQLException
    {

        XPropertySet oldcolumn = createDataDescriptor();
        try {
            oldcolumn.setPropertyValue(PropertyIds.ISNULLABLE.name, ColumnValue.NULLABLE);
        }
        catch (IllegalArgumentException | UnknownPropertyException 
                | PropertyVetoException | WrappedTargetException e) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        try {
            String table = DBTools.composeTableName(getConnection().getProvider(), m_table, ComposeRule.InTableDefinitions, false);
            List<String> queries = new ArrayList<String>();
            DBTableHelper.getAlterColumnQueries(queries, getConnection().getProvider(), m_table, oldcolumn, descriptor, false, isCaseSensitive());
            if (!queries.isEmpty()) {
                return DBTools.executeDDLQueries(getConnection().getProvider(), queries, m_table.getLogger(), this.getClass().getName(),
                                                 "_appendElement", Resources.STR_LOG_COLUMN_ALTER_QUERY, name, table);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return false;
    }

    @Override
    protected ColumnSuper createElement(String name)
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
                List<ColumnDescription> newcolumns = DBColumnHelper.readColumns(getConnection().getProvider(), m_table);
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
                String composedName = DBTools.composeTableNameForSelect(getConnection().getProvider(), m_table, isCaseSensitive());
                m_extrainfos = DBTools.collectColumnInformation(getConnection().getProvider(), composedName, "*");
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
            column = getColumn(name, description.typeName, description.defaultValue, description.remarks,
                                nullable, description.columnSize, description.decimalDigits, description.type,
                                isAutoIncrement, false, isCurrency);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_table);
        }
        return column;
    }

    @Override
    protected void removeElement(int index,
                                  String name)
        throws SQLException
    {
        UnoHelper.ensure(m_table, "Table is null!", getConnection().getLogger());
        if (m_table == null) {
            return;
        }
        try {
            String quote = getConnection().getProvider().getIdentifierQuoteString();
            StringBuilder buffer = new StringBuilder("ALTER TABLE ");
            buffer.append(DBTools.composeTableName(getConnection().getProvider(), m_table, ComposeRule.InTableDefinitions, isCaseSensitive()));
            buffer.append(DBTools.quoteName(quote, name));
            DBTools.executeDDLQuery(getConnection().getProvider(), buffer.toString(), m_table.getLogger(),
                                     "ColumnContainer", "_removeElement", Resources.STR_LOG_COLUMN_REMOVE_QUERY);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
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
        m_table.refreshColumns();
    }

    public ConnectionSuper getConnection()
    {
        return m_table.getConnection();
    }

    protected abstract ColumnSuper getColumn(String name,
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
