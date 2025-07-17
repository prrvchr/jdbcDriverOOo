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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ColumnHelper;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.TableHelper;
import io.github.prrvchr.uno.driver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ColumnContainerBase<C extends ColumnSuper>
    extends ContainerSuper<C> {

    protected final TableSuper mTable;
    private Map<String, ColumnDescription> mDescriptions = new HashMap<>();
    private Map<String, ExtraColumnInfo> mExtrainfos = new HashMap<>();

    // The constructor method:
    protected ColumnContainerBase(String service,
                                  String[] services,
                                  TableSuper table,
                                  boolean sensitive,
                                  List<ColumnDescription> descriptions)
        throws ElementExistException {
        super(service, services, table, sensitive, toColumnNames(descriptions));
        mTable = table;
        for (ColumnDescription description : descriptions) {
            mDescriptions.put(description.mColumnName, description);
        }
    }

    private static List<String> toColumnNames(List<ColumnDescription> descriptions) {
        List<String> names = new ArrayList<>(descriptions.size());
        for (ColumnDescription description : descriptions) {
            names.add(description.mColumnName);
        }
        return names;
    }

    @Override
    protected C appendElement(XPropertySet descriptor)
        throws SQLException {
        C column = null;
        String name = getElementName(descriptor);
        if (createColumn(descriptor, name)) {
            column = createElement(name);
        }
        return column;
    }

    private boolean createColumn(XPropertySet descriptor, String name)
        throws SQLException {
        boolean created = false;
        String table = null;
        List<String> queries = new ArrayList<>();
        try {
            Provider provider = getConnection().getProvider();
            table = DBTools.composeTableName(provider, mTable, ComposeRule.InTableDefinitions, false);
            TableHelper.getAddColumnQueries(queries, provider, mTable,  descriptor, isCaseSensitive());
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                mTable.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_COLUMN_ALTER_QUERY, name, table, query);
                created = DBTools.executeSQLQueries(provider, queries);
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_COLUMN_ALTER_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return created;
    }

    @Override
    protected C createElement(String name)
        throws SQLException {
        C column = null;
        try {
            @SuppressWarnings("unused")
            boolean queryInfo = true;
            boolean isAutoIncrement = false;
            boolean isCurrency = false;
            @SuppressWarnings("unused")
            int dataType = DataType.OTHER;

            Provider provider = getConnection().getProvider();
            ColumnDescription description = mDescriptions.get(name);
            if (description == null) {
                description = getColumnDescription(provider, name);
            }
            if (description == null) {
                throw new SQLException("No column " + name + " found");
            }
            
            ExtraColumnInfo info = mExtrainfos.get(name);
            if (info == null) {
                String composedName = DBTools.composeTableNameForSelect(provider, mTable, isCaseSensitive());
                mExtrainfos = ColumnHelper.collectColumnInformation(provider, composedName, "*");
                info = mExtrainfos.get(name);
            }
            if (info != null) {
                queryInfo = false;
                isAutoIncrement = info.mIsAutoIncrement;
                isCurrency = info.mIsCurrency;
                dataType = info.mDataType;
            }

            XNameAccess primaryKeyColumns = DBTools.getPrimaryKeyColumns(mTable.getKeys());
            int nullable = description.mNullable;
            if (nullable != ColumnValue.NO_NULLS && primaryKeyColumns != null && primaryKeyColumns.hasByName(name)) {
                nullable = ColumnValue.NO_NULLS;
            }
            column = getColumn(name, description.mTypeName, description.mDefaultValue, description.mRemarks,
                               nullable, description.mColumnSize, description.mDecimalDigits, description.mType,
                               isAutoIncrement, false, isCurrency);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return column;
    }

    private ColumnDescription getColumnDescription(Provider provider,
                                                   String name) throws java.sql.SQLException {
        // could be a recently added column. Refresh:
        List<ColumnDescription> newcolumns = ColumnHelper.readColumns(provider, mTable.getNamedComponents());
        for (ColumnDescription newcolumn : newcolumns) {
            if (newcolumn.mColumnName.equals(name)) {
                mDescriptions.put(name, newcolumn);
                break;
            }
        }
        return mDescriptions.get(name);
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        UnoHelper.ensure(mTable, "Table is null!", getConnection().getLogger());
        if (mTable == null) {
            return;
        }
        String query = null;
        Provider provider = getConnection().getProvider();
        try {
            ComposeRule rule = ComposeRule.InTableDefinitions;
            String table = DBTools.composeTableName(provider, mTable, rule, isCaseSensitive());
            String column = provider.enquoteIdentifier(name, isCaseSensitive());
            query = provider.getConfigDDL().getDropColumnCommand(ParameterDDL.getDropColumn(table, column));
            table = DBTools.composeTableName(provider, mTable, rule, false);
            mTable.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_COLUMN_REMOVE_QUERY, name, table, query);
            DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_COLUMN_REMOVE_QUERY_ERROR;
            String msg = mTable.getLogger().getStringResource(resource, name, query);
            mTable.getLogger().logp(LogLevel.SEVERE, msg);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    /// The XDatabaseMetaData.getColumns() data stored in columnDescriptions doesn't provide
    /// everything we need, so this class stores the rest.
    public static class ExtraColumnInfo {
        public boolean mIsAutoIncrement;
        public boolean mIsCurrency;
        public int mDataType;
    }

    @Override
    protected void refreshInternal() {
        mExtrainfos.clear();
        // FIXME: won't help
        mTable.refreshColumns();
    }

    public ConnectionSuper getConnection() {
        return mTable.getConnection();
    }

    protected abstract C getColumn(String name,
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
