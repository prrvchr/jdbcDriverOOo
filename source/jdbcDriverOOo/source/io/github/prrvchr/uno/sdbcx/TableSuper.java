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
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XAlterTable;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XIndexesSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.helper.DBColumnHelper.ColumnDescription;
import io.github.prrvchr.jdbcdriver.helper.DBIndexHelper;
import io.github.prrvchr.jdbcdriver.helper.DBKeyHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTableHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class TableSuper<C extends ConnectionSuper>
    extends TableMain<C>
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    private ColumnContainerBase<?> m_columns = null;
    private KeyContainer m_keys = null;
    private IndexContainer m_indexes = null;
    protected String m_Description = "";
    protected String m_Type = "";
    private boolean m_qualifiedindex = false;

    // The constructor method:
    public TableSuper(String service,
                      String[] services,
                      C connection,
                      String catalog,
                      String schema,
                      boolean sensitive,
                      String name)
    {
        super(service, services, connection, catalog, schema, sensitive, name, LoggerObjectType.TABLE);
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.DESCRIPTION.name, PropertyIds.DESCRIPTION.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Description;
                }
            }, null);
        registerProperty(PropertyIds.TABLETYPE.name, PropertyIds.TABLETYPE.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Type;
                }
            }, null);
    }

    protected ColumnContainerBase<?> getColumnsInternal()
    {
        checkDisposed();
        if (m_columns == null) {
            refreshColumns();
        }
        return m_columns;
    }

    protected KeyContainer getKeysInternal()
    {
        checkDisposed();
        if (m_keys == null) {
            refreshKeys();
        }
        return m_keys;
    }

    protected IndexContainer getIndexesInternal()
    {
        checkDisposed();
        if (m_indexes == null) {
            refreshIndexes();
        }
        return m_indexes;
    }

    @Override
    protected void postDisposing() {
        super.postDisposing();
        if (m_keys != null) {
            m_keys.dispose();
        }
        if (m_columns != null) {
            m_columns.dispose();
        }
        if (m_indexes != null) {
            m_indexes.dispose();
        }
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns()
    {
        return getColumnsInternal();
    }

    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        return getKeysInternal();
    }


    // com.sun.star.sdbcx.XIndexesSupplier
    @Override
    public XNameAccess getIndexes() {
        return getIndexesInternal();
    }

    // com.sun.star.sdbcx.XAlterTable:
    @Override
    public void alterColumnByIndex(int index, XPropertySet newcolumn)
        throws SQLException, IndexOutOfBoundsException
    {
        checkDisposed();
        ColumnSuper<?> oldcolumn = m_columns.getElement(index);
        if (oldcolumn != null) {
            alterColumn(oldcolumn, newcolumn);
        }
    }

    @Override
    public void alterColumnByName(String name, XPropertySet newcolumn)
        throws SQLException, NoSuchElementException
    {
        checkDisposed();
        ColumnSuper<?> oldcolumn = m_columns.getElement(name);
        if (oldcolumn != null) {
            alterColumn(oldcolumn, newcolumn);
        }
    }

    private void alterColumn(ColumnSuper<?> oldcolumn, XPropertySet newcolumn)
        throws SQLException
    {
        DriverProvider provider = getConnection().getProvider();

        // XXX: Identity have been changed?
        boolean auto = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
        // XXX: Identity switching is only allowed if the underlying driver supports it.
        if (oldcolumn.m_IsAutoIncrement != auto && !provider.supportsAlterIdentity()) {
            int resource = Resources.STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldcolumn.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        // XXX: Type have been changed?
        String type = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME);
        // XXX: Changing column type is only allowed if the underlying driver supports it.
        if (!oldcolumn.m_TypeName.equals(type) && !provider.supportsAlterColumnType()) {
            int resource = Resources.STR_LOG_COLUMN_ALTER_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldcolumn.getName());
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        String table = null;
        List<String> queries = new ArrayList<String>();
        NamedComponents component = getNamedComponents();
        ComposeRule rule = ComposeRule.InTableDefinitions;
        String oldname = oldcolumn.getName();
        try {
            table = DBTools.buildName(provider, component, rule);
            boolean alterpk = isPrimaryKeyColumn(oldname);
            boolean alterfk = isForeignKeyColumn(oldname);
            boolean alteridx = isIndexColumn(oldname);
            boolean alterkey = alterpk || alterfk;
            byte result = DBTableHelper.getAlterColumnQueries(queries, provider, this, oldcolumn, newcolumn, alterkey, isCaseSensitive());
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table, query);
                if (DBTools.executeDDLQueries(provider, queries)) {
                    // Column have changed its description value
                    if ((result & 32) == 32) {
                        oldcolumn.m_Description = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DESCRIPTION);
                    }
                    // Column have changed its not null constraint
                    if ((result & 16) == 16) {
                        oldcolumn.m_IsNullable = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.ISNULLABLE);
                    }
                    // Column have changed its default value
                    if ((result & 8) == 8) {
                        oldcolumn.m_DefaultValue = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DEFAULTVALUE);
                    }
                    // Column have changed its type
                    if ((result & 4) == 4) {
                        oldcolumn.m_Type = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.TYPE);
                        oldcolumn.m_TypeName = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME);
                    }
                    // Column have changed its identity (auto-increment)
                    if ((result & 2) == 2) {
                        oldcolumn.m_IsAutoIncrement = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
                    }
                    // Column have changed its name
                    if ((result & 1) == 1) {
                        String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
                        m_columns.replaceElement(oldname, newname);
                        if (alterpk) {
                            getKeysInternal().renameKeyColumn(KeyType.PRIMARY, oldname, newname);
                            // XXX: If the renamed column is a primary key, we need to know if it is referenced as a foreign key.
                            // XXX: If this is the case then we need to rename the corresponding column in these foreign keys.
                            Map<String, List<String>> tables = DBKeyHelper.getExportedTablesColumns(provider, component, newname, rule);
                            if (!tables.isEmpty()) {
                                getConnection().getTablesInternal().renameForeignKeyColumn(tables, table, oldname, newname);
                            }
                        }
                        if (alterfk) {
                            // XXX: If the renamed column is a foreign key we need to rename the Key column name to.
                            // XXX: Renaming the foreign key should rename the associated Index column name as well.
                            getKeysInternal().renameKeyColumn(KeyType.FOREIGN, oldname, newname);
                        }
                        if (alteridx) {
                                // XXX: If the renamed column is declared as index we need to rename the Index column name to.
                                getIndexesInternal().renameIndexColumn(oldname, newname);
                        }
                    }
                }
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private boolean isPrimaryKeyColumn(String column)
        throws SQLException
    {
        // FIXME: Here we search and retrieve the first primary key having this column.
        KeyContainer keys = getKeysInternal();
        for (String name : keys.getElementNames()) {
            Key key = keys.getElement(name);
            if (key.m_Type == KeyType.PRIMARY && key.getColumnsInternal().hasByName(column)) {
                return true;
            }
        }
        return false;
    }

    private boolean isForeignKeyColumn(String column)
        throws SQLException
    {
        // FIXME: Here we search and retrieve the first foreign key having this table / column.
        KeyContainer keys = getKeysInternal();
        for (String name: keys.getElementNames()) {
            Key key = keys.getElement(name);
            if (key.m_Type == KeyType.FOREIGN && key.getColumnsInternal().hasByName(column)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIndexColumn(String column)
        throws SQLException
    {
        // FIXME: Here we search and retrieve if this column is declared as index.
        IndexContainer indexes = getIndexesInternal();
        for (String name : indexes.getElementNames()) {
            if (indexes.getElement(name).getColumnsInternal().hasByName(column)) {
                return true;
            }
        }
        return false;
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        TableDescriptor descriptor = new TableDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

    // com.sun.star.sdbcx.XRename
    // TODO: see: https://github.com/LibreOffice/core/blob/6361a9398584defe9ab8db1e3383e02912e3f24c/
    // TODO: connectivity/source/drivers/postgresql/pq_xtable.cxx#L136
    @Override
    public void rename(String name)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.TableSuper.rename() Table: '" + name + "'");

        String query = null;
        String table = null;
        try {
            boolean renamed = false;
            // XXX: Table and View use the same functions to rename.
            boolean isview = m_Type.toUpperCase().contains("VIEW");
            ComposeRule rule = ComposeRule.InDataManipulation;
            DriverProvider provider = getConnection().getProvider();
            table = DBTools.buildName(provider, getNamedComponents(), rule);
            // XXX: We can handle renaming if it is a table and the driver does not have a command to rename the table
            // XXX: or it's a view and we don't have access to the view's command definition.
            if (!isview && !provider.supportRenamingTable() || isview && !provider.supportViewDefinition()) {
                int resource = getRenameTableNotImplementedResource(isview);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table);
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            NamedComponents component = DBTools.qualifiedNameComponents(provider, name, rule);
            if (isview) {
                ViewContainer views = getConnection().getViewsInternal();
                View view = views.getElement(table);
                if (view == null) {
                    int resource = Resources.STR_LOG_VIEW_RENAME_VIEW_NOT_FOUND_ERROR;
                    String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table);
                    throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
                }
                // XXX: Some databases DRIVER cannot rename views (ie: SQLite). In this case the Drivers.xcu property
                // XXX: SupportRenameView can be used to signal this and allow execution by a DROP VIEW then CREATE VIEW
                if (provider.supportRenameView()) {
                    view.rename(name);
                    renamed = true;
                }
                else {
                    getConnection().getViewsInternal().removeView(view);
                    query = DBTools.getCreateViewQuery(provider, component, view.m_Command, rule, isCaseSensitive());
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEW_RENAME_QUERY, name, query);
                    if (DBTools.executeDDLQuery(provider, query)) {
                        views.rename(table, name);
                        renamed = true;
                    }
                }
            }
            else {
                renamed = rename(component, table, name, false, rule);
            }

            if (renamed) {
                m_CatalogName = component.getCatalogName();
                m_SchemaName = component.getSchemaName();
                m_Name = component.getTableName();
                getConnection().getTablesInternal().rename(table, name);
                // XXX: If renamed table is not a view and are part of a foreign key the referenced table name is not any more valid.
                // XXX: So we need to rename the referenced table name in all other table having a foreign keys referencing this table.
                if (!isview) {
                    List<String> filter = DBKeyHelper.getExportedTables(provider, component, rule);
                    getConnection().getTablesInternal().renameReferencedTableName(filter, table, name);
                }
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    protected void refreshColumns()
    {
        try {
            List<ColumnDescription> columns = DBColumnHelper.readColumns(getConnection().getProvider(), getNamedComponents());
            if (m_columns == null) {
                m_columns = getColumnContainer(columns);
            }
            else {
                m_columns.refill(getColumnName(columns));
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private List<String> getColumnName(List<ColumnDescription> descriptions) {
        List<String> columns = new ArrayList<>();
        for (ColumnDescription column: descriptions) {
            columns.add(column.columnName);
        }
        return columns;
    }

    protected void refreshKeys()
    {
        try {
            DriverProvider provider = getConnection().getProvider();
            List<String> keys = DBKeyHelper.refreshKeys(provider, getNamedComponents());
            System.out.println("TableSuper.refreshKeys() Table: " + m_Name + " - Keys: " + String.join(", ", keys));
            if (m_keys == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_KEYS);
                m_keys = new KeyContainer(this, isCaseSensitive(), keys);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_KEYS_ID, m_keys.getLogger().getObjectId());
            }
            else {
                m_keys.refill(keys);
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected void refreshIndexes()
    {
        try {
            List<String> indexes = DBIndexHelper.readIndexes(getConnection().getProvider(), getNamedComponents(), m_qualifiedindex);
            if (m_indexes == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_INDEXES);
                m_indexes = new IndexContainer(this, isCaseSensitive(), indexes);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_INDEXES_ID, m_indexes.getLogger().getObjectId());
            }
            else {
                m_indexes.refill(indexes);
            }
        }
        catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected abstract ColumnContainerBase<?> getColumnContainer(List<ColumnDescription> descriptions) throws ElementExistException;

}
