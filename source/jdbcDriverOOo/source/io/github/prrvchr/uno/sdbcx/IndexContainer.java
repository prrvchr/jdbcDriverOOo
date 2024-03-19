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

import java.text.MessageFormat;
import java.util.ArrayList;
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
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBDefaultQuery;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class IndexContainer
    extends Container<Index>
{
    private static final String m_service = IndexContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Indexes",
                                                "com.sun.star.sdbcx.Container"};

    private final TableSuper<?> m_Table;
    private final ConnectionLog m_logger;

    // The constructor method:
    public IndexContainer(TableSuper<?> table,
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
            System.out.println("sdbcx.IndexContainer.createElement() 1");
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            String separator = metadata.getCatalogSeparator();
            System.out.println(String.format("sdbcx.IndexContainer.createElement() Separator: %s", separator));
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
            try (java.sql.ResultSet result = metadata.getIndexInfo(m_Table.getCatalog(), m_Table.getSchema(), m_Table.getName(), false, false))
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
                Boolean primary = DBColumnHelper.isPrimaryKeyIndex(metadata, m_Table.getCatalog(), m_Table.getSchema(), m_Table.getName(), subname);
                System.out.println(String.format("sdbcx.IndexContainer.createElement() PrimaryKey: %s", primary));
                boolean clustered = type == IndexType.CLUSTERED;
                index = new Index(m_Table, isCaseSensitive(), subname, qualifier, unique, primary, clustered, columns);
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw UnoHelper.getSQLException(e, m_Table);
        }
        return index;
    }

    @Override
    protected Index appendElement(XPropertySet descriptor)
        throws SQLException
    {
        Index index = null;
        String name = getElementName(descriptor);
        System.out.println("sdbcx.IndexContainer.appendElement() 1 Name: " + name);
        if (createIndex(descriptor, name)) {
            index = createElement(name);
        }
        return index;
    }

    private boolean createIndex(XPropertySet descriptor,
                                String name)
        throws SQLException
    {
        try {
            if (getConnection() == null) {
                return false;
            }
            ComposeRule rule = ComposeRule.InIndexDefinitions;
            List<Object> arguments = new ArrayList<Object>();
            DriverProvider provider = getConnection().getProvider();
            boolean unique = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISUNIQUE);
            arguments.add(unique ? "UNIQUE" : "");
            arguments.add(DBTools.enquoteIdentifier(provider, name, isCaseSensitive()));
            //String table = DBTools.composeTableName(provider, m_Table, rule, false, false, isCaseSensitive());
            arguments.add(DBTools.composeTableName(provider, m_Table, rule, isCaseSensitive()));
            XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            List<String> indexes = new ArrayList<String>();
            for (int i = 0; i < columns.getCount(); i++) {
                XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                String column = DBTools.getDescriptorStringValue(property, PropertyIds.NAME);
                String index = DBTools.enquoteIdentifier(provider, column, isCaseSensitive());
                if (provider.addIndexAppendix()) {
                    index += DBTools.getDescriptorBooleanValue(property, PropertyIds.ISASCENDING) ? " ASC" : " DESC";
                }
                indexes.add(index);
            }
            if (!indexes.isEmpty()) {
                String command = DBDefaultQuery.STR_QUERY_ALTER_TABLE_ADD_INDEX;
                arguments.add(String.join(", ", indexes));
                String query = MessageFormat.format(command, arguments.toArray(new Object[0]));
                System.out.println("sdbcx.IndexContainer.createIndex() 1 Query: " + query);
                String table = DBTools.composeTableName(provider, m_Table, rule, false);
                return DBTools.executeDDLQuery(provider, getLogger(), query,
                                               this.getClass().getName(), "createIndex",
                                               Resources.STR_LOG_INDEXES_CREATE_INDEX_QUERY, name, table);
            }
            return false;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        catch (WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(e, this);
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
        try {
            ComposeRule rule = ComposeRule.InIndexDefinitions;
            DriverProvider provider = getConnection().getProvider();
            String command = provider.getDropConstraintQuery(KeyType.UNIQUE);
            String arg1 = DBTools.composeTableName(provider, m_Table, rule, isCaseSensitive());
            String arg2 = DBTools.enquoteIdentifier(provider, name, isCaseSensitive());
            String query = MessageFormat.format(command, arg1, arg2);
            String table = DBTools.composeTableName(provider, m_Table, rule, false);
            System.out.println("sdbcx.IndexContainer.removeDataBaseElement() Query: " + query);
            DBTools.executeDDLQuery(provider, getLogger(), query, this.getClass().getName(),
                                    "removeDataBaseElement", Resources.STR_LOG_INDEXES_REMOVE_INDEX_QUERY, name, table);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
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
        for (String column : getElementNames()) {
            Index index = getElement(column);
            if (index.m_IsPrimaryKeyIndex) {
                removeElement(column, false);
                break;
            }
        }
    }

    protected void renameIndexColumn(String oldname, String newname)
        throws SQLException
    {
        for (String name: getElementNames()) {
            getElement(name).getColumnsInternal().renameIndexColumn(oldname, newname);
        }
    }

    protected void renamePrimaryKeyIndexColumn(String oldname, String newname)
        throws SQLException
    {
        for (String name: getElementNames()) {
            Index index = getElement(name);
            if (index.m_IsPrimaryKeyIndex) {
                index.getColumnsInternal().renameIndexColumn(oldname, newname);
                break;
            }
        }
    }

}
