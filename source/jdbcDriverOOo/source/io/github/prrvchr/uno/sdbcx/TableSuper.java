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
import com.sun.star.sdbcx.XAlterTable;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.sdbcx.XIndexesSupplier;
import com.sun.star.sdbcx.XKeysSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBTableHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DBColumnHelper.ColumnDescription;
import io.github.prrvchr.jdbcdriver.DBIndexHelper;
import io.github.prrvchr.jdbcdriver.DBKeyHelper;
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
        alterColumn(m_columns.getElement(index), newcolumn);
    }

    @Override
    public void alterColumnByName(String name, XPropertySet newcolumn)
        throws SQLException, NoSuchElementException
    {
        checkDisposed();
        alterColumn(m_columns.getElement(name), newcolumn);
    }

    private void alterColumn(ColumnSuper<?> oldcolumn, XPropertySet newcolumn)
        throws SQLException
    {
        if (oldcolumn != null) {
            DriverProvider provider = getConnection().getProvider();

            // XXX: Identity have been changed?
            boolean auto = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
            // XXX: Identity switching is only allowed if the underlying driver supports it.
            if (oldcolumn.m_IsAutoIncrement != auto && !provider.supportsAlterIdentity()) {
                int resource = Resources.STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR;
                String msg = getLogger().getStringResource(resource, oldcolumn.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }

            // XXX: Type have been changed?
            String type = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME);
            // XXX: Changing column type is only allowed if the underlying driver supports it.
            if (!oldcolumn.m_TypeName.equals(type) && !provider.supportsAlterColumnType()) {
                int resource = Resources.STR_LOG_COLUMN_ALTER_UNSUPPORTED_FEATURE_ERROR;
                String msg = getLogger().getStringResource(resource, oldcolumn.getName());
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }

            String table = null;
            List<String> queries = new ArrayList<String>();
            String oldname = oldcolumn.getName();
            boolean alterpk = isPrimaryKeyColumn(oldname);
            boolean alteridx = isIndexColumn(oldname);
            try {
                table = DBTools.buildName(provider, getCatalogName(), getSchemaName(), getName(), ComposeRule.InTableDefinitions);
                byte result = DBTableHelper.getAlterColumnQueries(queries, provider, this, oldcolumn, newcolumn, alterpk, isCaseSensitive());
                if (!queries.isEmpty()) {
                    for (String query : queries) {
                        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table, query);
                    }
                    if (DBTools.executeDDLQueries(provider, queries)) {
                        // Column have changed its name
                        if ((result & 1) == 1) {
                            String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
                            if (alterpk) {
                                // XXX: If the renamed column is a primary key we need to rename the Key column name to.
                                // XXX: Renaming the primary key should rename the associated Index column name as well.
                                getKeysInternal().renameKeyColumn(oldname, newname);
                            }
                            if (alteridx) {
                                // XXX: If the renamed column is declared as index we need to rename the Index column name to.
                                getIndexesInternal().renameIndexColumn(oldname, newname);
                            }
                            m_columns.replaceElement(oldname, newname);
                        }
                        // Column have changed its identity (auto-increment)
                        if ((result & 2) == 2) {
                            oldcolumn.m_IsAutoIncrement = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
                        }
                        // Column have changed its type
                        if ((result & 4) == 4) {
                            oldcolumn.m_Type = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.TYPE);
                            oldcolumn.m_TypeName = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME);
                        }
                        // Column have changed its default value
                        if ((result & 8) == 8) {
                            oldcolumn.m_DefaultValue = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DEFAULTVALUE);
                        }
                        // Column have changed its not null constraint
                        if ((result & 16) == 16) {
                            oldcolumn.m_IsNullable = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.ISNULLABLE);
                        }
                        // Column have changed its description value
                        if ((result & 32) == 32) {
                            oldcolumn.m_Description = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DESCRIPTION);
                        }
                    }
                }
            }
            catch (java.sql.SQLException e) {
                int resource = Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY_ERROR;
                String query = "<" + String.join("> <", queries) + ">";
                String msg = getLogger().getStringResource(resource, table, query);
                getLogger().logp(LogLevel.SEVERE, msg);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
        }
    }

    private boolean isPrimaryKeyColumn(String oldname)
        throws SQLException
    {
        // FIXME: Here we search and retrieve the first primary key having this column.
        KeyContainer keys = getKeysInternal();
        for (String name : keys.getElementNames()) {
            if (keys.getElement(name).getColumnsInternal().hasByName(oldname)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIndexColumn(String oldname)
        throws SQLException
    {
        // FIXME: Here we search and retrieve if this column is declared as index.
        IndexContainer indexes = getIndexesInternal();
        for (String name : indexes.getElementNames()) {
            if (indexes.getElement(name).getColumnsInternal().hasByName(oldname)) {
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
        boolean isview = m_Type.toUpperCase().contains("VIEW");
        // FIXME: Table and View use the same functions to rename but we need a custom message for both,
        // FIXME: we use an offset for that.
        ComposeRule rule = ComposeRule.InDataManipulation;
        DriverProvider provider = getConnection().getProvider();
        try {
            String oldname = DBTools.composeTableName(provider, this, rule, false);
            if (!provider.supportRenamingTable()) {
                String msg = getLogger().getStringResource(getRenameTableUnsupportedResource(isview), oldname);
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            NameComponents component = DBTools.qualifiedNameComponents(provider, name, rule);
            boolean renamed = false;
            if (isview) {
                ViewContainer views = getConnection().getViewsInternal();
                View view = views.getElement(oldname);
                if (view == null) {
                    int resource = Resources.STR_LOG_VIEW_RENAME_VIEW_NOT_FOUND_ERROR;
                    String msg = getLogger().getStringResource(resource, oldname);
                    throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
                }
                // FIXME: Some databases DRIVER cannot rename views (ie: SQLite). In this case the Drivers.xcu property
                // FIXME: SupportRenameView can be used to signal this and allow execution by a DROP VIEW then CREATE VIEW
                if (provider.supportRenameView()) {
                    view.rename(name);
                    renamed = true;
                }
                else {
                    getConnection().getViewsInternal().removeView(view);
                    String query = DBTools.getCreateViewQuery(provider, component, view.m_Command, rule, isCaseSensitive());
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name, query);
                    if (DBTools.executeDDLQuery(provider, query)) {
                        views.rename(oldname, name);
                        renamed = true;
                    }
                }
            }
            else {
                renamed = rename(component, oldname, name, false, rule);
            }

            if (renamed) {
                m_SchemaName = component.getSchema();
                m_CatalogName = component.getCatalog();
                m_Name = component.getTable();
                getConnection().getTablesInternal().rename(oldname, name);
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    protected void refreshColumns()
    {
        try {
            List<ColumnDescription> columns = DBColumnHelper.readColumns(getConnection().getProvider(), getCatalog(), getSchema(), getName());
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
            List<String> keys = DBKeyHelper.refreshKeys(provider, getCatalog(), getSchema(), getName());
            System.out.println("sdbcx.TableSuper.refreshKeys() Key Count: " + keys.size());
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
            List<String> indexes = DBIndexHelper.readIndexes(getConnection().getProvider(), getCatalog(),
                                                             getSchema(), getName(), m_qualifiedindex);
            System.out.println("sdbcx.TableSuper.refreshIndexes() Index Count: " + indexes.size());
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
