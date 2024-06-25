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
import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.IndexType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBIndexHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;


public final class IndexContainer
    extends Container<Index>
{
    private static final String m_service = IndexContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Indexes",
                                                "com.sun.star.sdbcx.Container"};

    private final TableSuper m_Table;
    private final ConnectionLog m_logger;

    // The constructor method:
    public IndexContainer(TableSuper table,
                          boolean sensitive,
                          List<String> indexes)
    throws ElementExistException
    {
        super(m_service, m_services, table, sensitive, indexes);
        m_Table = table;
        m_logger = new ConnectionLog(table.getLogger(), LoggerObjectType.INDEXCONTAINER);
    }

    protected ConnectionSuper getConnection()
    {
        return m_Table.getConnection();
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    public void dispose()
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_INDEXES_DISPOSING);
        super.dispose();
    }

    @Override
    protected Index createElement(String name)
        throws SQLException
    {
        Index index = null;
        try {
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            String separator = metadata.getCatalogSeparator();
            String qualifier = "";
            String subname;
            int len;
            if (separator != null && !separator.isBlank() && (len = name.indexOf(separator)) >= 0) {
                qualifier = name.substring(0, len);
                subname = name.substring(len + 1);
            }
            else {
                subname = name;
            }
            int type = -1;
            boolean found = false;
            boolean unique = false;
            List<String> columns = new ArrayList<>();
            NamedComponents table = m_Table.getNamedComponents();
            try (java.sql.ResultSet result = metadata.getIndexInfo(table.getCatalog(), table.getSchema(), table.getTable(), false, false))
            {
                while (result.next()) {
                    unique  = !result.getBoolean(4);
                    if ((qualifier.isEmpty() || qualifier.equals(result.getString(5))) && subname.equals(result.getString(6))) {
                        found = true;
                        type = result.getShort(7);
                        String columnName = result.getString(9);
                        if (!result.wasNull()) {
                            columns.add(columnName);
                        }
                    }
                }
            }
            if (found) {
                Boolean primary = DBIndexHelper.isPrimaryKeyIndex(metadata, table, subname);
                boolean clustered = type == IndexType.CLUSTERED;
                index = new Index(m_Table, isCaseSensitive(), subname, qualifier, unique, primary, clustered, columns);
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return index;
    }

    @Override
    protected Index appendElement(XPropertySet descriptor)
        throws SQLException
    {
        Index index = null;
        String name = getElementName(descriptor);
        if (createIndex(descriptor, name)) {
            index = createElement(name);
        }
        return index;
    }

    private boolean createIndex(XPropertySet descriptor,
                                String name)
        throws SQLException
    {
        String query = null;
        String table = null;
        try {
            if (getConnection() == null) {
                return false;
            }
            ComposeRule rule = ComposeRule.InIndexDefinitions;
            List<Object> arguments = new ArrayList<>();
            DriverProvider provider = getConnection().getProvider();
            boolean unique = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISUNIQUE);
            arguments.add(DBTools.composeTableName(provider, m_Table, rule, isCaseSensitive()));
            arguments.add(DBTools.enquoteIdentifier(provider, name, isCaseSensitive()));
            XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            List<String> indexes = new ArrayList<String>();
            for (int i = 0; i < columns.getCount(); i++) {
                XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                String column = DBTools.getDescriptorStringValue(property, PropertyIds.NAME);
                String index = DBTools.enquoteIdentifier(provider, column, isCaseSensitive());
                if (!unique && provider.addIndexAppendix()) {
                    index += DBTools.getDescriptorBooleanValue(property, PropertyIds.ISASCENDING) ? " ASC" : " DESC";
                }
                indexes.add(index);
            }
            if (!indexes.isEmpty()) {
                arguments.add(String.join(", ", indexes));
                query = provider.getAddIndexQuery(unique, arguments.toArray(new Object[0]));
                System.out.println("sdbcx.IndexContainer.createIndex() 1 Query: " + query);
                table = DBTools.composeTableName(provider, m_Table, rule, false);
                getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY, name, table, query);
                return DBTools.executeSQLQuery(provider, query);
            }
            return false;
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        catch (WrappedTargetException | IndexOutOfBoundsException e) {
            int resource = Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String elementName)
        throws SQLException
    {
        if (getConnection() == null) {
            return;
        }
        String name;
        int len = elementName.indexOf('.');
        name = elementName.substring(len + 1);
        String query = null;
        String table = null;
        try {
            ComposeRule rule = ComposeRule.InIndexDefinitions;
            DriverProvider provider = getConnection().getProvider();
            String command = provider.getDropConstraintQuery(KeyType.UNIQUE);
            String arg1 = DBTools.composeTableName(provider, m_Table, rule, isCaseSensitive());
            String arg2 = DBTools.enquoteIdentifier(provider, name, isCaseSensitive());
            query = DBTools.formatSQLQuery(command, arg1, arg2);
            table = DBTools.composeTableName(provider, m_Table, rule, false);
            System.out.println("sdbcx.IndexContainer.removeDataBaseElement() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY, name, table, query);
            DBTools.executeSQLQuery(provider, query);
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.IndexContainer.refreshInternal() *********************************");
        m_Table.refreshIndexes();
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new IndexDescriptor(isCaseSensitive());
    }

    protected void removePrimaryKeyIndex()
        throws SQLException
    {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            Index index = Indexes.next();
            if (index.m_IsPrimaryKeyIndex) {
                removeElement(index.getName(), false);
                break;
            }
        }
    }

    protected void removeForeignKeyIndex(String name)
            throws SQLException
    {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            Index index = Indexes.next();
            if (name.equals(index.getName())) {
                removeElement(name, false);
                break;
            }
        }
    }

    protected void renameIndexColumn(String oldname,
                                     String newname)
        throws SQLException
    {
        Iterator<Index> Indexes = getActiveElements();
        while (Indexes.hasNext()) {
            IndexColumnContainer columns = Indexes.next().getColumnsInternal();
            if (columns.hasByName(oldname)) {
                columns.renameIndexColumn(oldname, newname);
                break;
            }
        }
    }

}
