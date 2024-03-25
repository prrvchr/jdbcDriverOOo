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
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBTableHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DBColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ColumnContainerBase<T extends TableSuper<?>>
    extends Container<ColumnSuper<?>>
{

    private Map<String, ColumnDescription> m_descriptions = new HashMap<>();
    private Map<String, ExtraColumnInfo> m_extrainfos = new HashMap<>();
    protected final T m_table;

    // The constructor method:
    public ColumnContainerBase(String service,
                               String[] services,
                               T table,
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
    protected ColumnSuper<?> appendElement(XPropertySet descriptor)
        throws SQLException
    {
        ColumnSuper<?> column = null;
        String name = getElementName(descriptor);
        if (createColumn(descriptor, name)) {
            column = createElement(name);
        }
        return column;
    }

    private boolean createColumn(XPropertySet descriptor, String name)
        throws SQLException
    {
        String table = null;
        List<String> queries = new ArrayList<String>();
        try {
            XPropertySet oldcolumn = createDataDescriptor();
            oldcolumn.setPropertyValue(PropertyIds.ISNULLABLE.name, ColumnValue.NULLABLE);

            DriverProvider provider = getConnection().getProvider();
            table = DBTools.composeTableName(provider, m_table, ComposeRule.InTableDefinitions, false);
            DBTableHelper.getAlterColumnQueries(queries, provider, m_table, oldcolumn, descriptor, false, isCaseSensitive());
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                m_table.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_COLUMN_ALTER_QUERY, name, table, query);
                return DBTools.executeDDLQueries(provider, queries);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_COLUMN_ALTER_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (IllegalArgumentException | UnknownPropertyException |
                PropertyVetoException | WrappedTargetException e) {
            int resource = Resources.STR_LOG_COLUMN_ALTER_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, (Exception) e);
         }
        return false;
    }

    @Override
    protected ColumnSuper<?> createElement(String name)
        throws SQLException
    {
        ColumnSuper<?> column = null;
        try {
            @SuppressWarnings("unused")
            boolean queryInfo = true;
            boolean isAutoIncrement = false;
            boolean isCurrency = false;
            @SuppressWarnings("unused")
            int dataType = DataType.OTHER;

            DriverProvider provider = getConnection().getProvider();
            ColumnDescription description = m_descriptions.get(name);
            if (description == null) {
                // could be a recently added column. Refresh:
                List<ColumnDescription> newcolumns = DBColumnHelper.readColumns(provider, m_table.getNamedComponents());
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
                String composedName = DBTools.composeTableNameForSelect(provider, m_table, isCaseSensitive());
                m_extrainfos = DBColumnHelper.collectColumnInformation(provider, composedName, "*");
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
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return column;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        UnoHelper.ensure(m_table, "Table is null!", getConnection().getLogger());
        if (m_table == null) {
            return;
        }
        String query = null;
        DriverProvider provider = getConnection().getProvider();
        try {
            String table = DBTools.composeTableName(provider, m_table, ComposeRule.InTableDefinitions, isCaseSensitive());
            String column = DBTools.enquoteIdentifier(provider, name, isCaseSensitive());
            query = provider.getDropColumnQuery(table, column);
            table = DBTools.composeTableName(provider, m_table, ComposeRule.InTableDefinitions, false);
            m_table.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY, name, table, query);
            DBTools.executeDDLQuery(provider, query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_GROUPS_CREATE_GROUP_QUERY_ERROR;
            String msg = m_table.getLogger().getStringResource(resource, name, query);
            m_table.getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
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
    protected void refreshInternal() {
        m_extrainfos.clear();
        // FIXME: won't help
        m_table.refreshColumns();
    }

    public ConnectionSuper getConnection()
    {
        return m_table.getConnection();
    }

    protected abstract ColumnSuper<?> getColumn(String name,
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
