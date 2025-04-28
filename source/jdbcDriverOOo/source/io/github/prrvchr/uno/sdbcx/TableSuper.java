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
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
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

import io.github.prrvchr.driver.helper.ColumnHelper;
import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.IndexHelper;
import io.github.prrvchr.driver.helper.KeyHelper;
import io.github.prrvchr.driver.helper.TableHelper;
import io.github.prrvchr.driver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class TableSuper
    extends TableMain
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory {

    protected String mDescription = "";
    protected String mType = "";
    private ColumnContainerBase<?> mColumns = null;
    private KeyContainer mKeys = null;
    private IndexContainer mIndexes = null;
    private boolean mQualifiedindex = false;

    // The constructor method:
    public TableSuper(String service,
                      String[] services,
                      ConnectionSuper connection,
                      boolean sensitive,
                      String catalog,
                      String schema,
                      String name,
                      String type,
                      String remarks) {
        super(service, services, connection, catalog, schema, sensitive, name, LoggerObjectType.TABLE);
        mType = type;
        mDescription = remarks;
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.DESCRIPTION.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mDescription;
                },
                null));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mType;
                },
                null));

        super.registerProperties(properties);
    }

    protected ColumnContainerBase<?> getColumnsInternal() {
        checkDisposed();
        if (mColumns == null) {
            refreshColumns();
        }
        return mColumns;
    }

    protected KeyContainer getKeysInternal() {
        checkDisposed();
        if (mKeys == null) {
            refreshKeys();
        }
        return mKeys;
    }

    protected IndexContainer getIndexesInternal() {
        checkDisposed();
        if (mIndexes == null) {
            refreshIndexes();
        }
        return mIndexes;
    }

    @Override
    protected void postDisposing() {
        super.postDisposing();
        if (mKeys != null) {
            mKeys.dispose();
        }
        if (mColumns != null) {
            mColumns.dispose();
        }
        if (mIndexes != null) {
            mIndexes.dispose();
        }
    }

    // com.sun.star.sdbcx.XColumnsSupplier:
    @Override
    public XNameAccess getColumns() {
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
        throws SQLException, IndexOutOfBoundsException {
        checkDisposed();
        ColumnSuper oldcolumn = mColumns.getElement(index);
        if (oldcolumn != null) {
            alterColumn(oldcolumn, newcolumn);
        }
    }

    @Override
    public void alterColumnByName(String name, XPropertySet newcolumn)
        throws SQLException, NoSuchElementException {
        checkDisposed();
        ColumnSuper oldcolumn = mColumns.getElement(name);
        if (oldcolumn != null) {
            alterColumn(oldcolumn, newcolumn);
        }
    }

    private void alterColumn(ColumnSuper oldcolumn, XPropertySet newcolumn)
        throws SQLException {
        DriverProvider provider = getConnection().getProvider();

        String oldname = oldcolumn.getName();
        boolean autoincrement = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
        int flags = TableHelper.getAlterColumnChanges(oldcolumn, newcolumn, oldname, autoincrement);

        // XXX: Identity or Type have been changed?
        // XXX: Identity switching is only allowed if the underlying driver supports it.
        // XXX: Changing column type is only allowed if the underlying driver supports it.
        if (TableHelper.hasColumnIdentityChanged(flags) && !supportColumnIdentityChange(provider) ||
            TableHelper.hasColumnTypeChanged(flags) && !supportColumnTypeChange(provider)) {
            int resource = Resources.STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        int result = alterColumn(provider, oldcolumn, newcolumn, oldname, autoincrement, flags);
        if (result != flags) {
            System.out.println("TableSuper.alterColumn() ERROR ******************************************");
        }
    }

    private boolean supportColumnIdentityChange(DriverProvider provider) {
        return provider.getDDLQuery().supportsAlterIdentity() ||
               provider.getDDLQuery().hasAlterColumnCommand();
    }

    private boolean supportColumnTypeChange(DriverProvider provider) {
        return provider.getDDLQuery().hasAlterColumnTypeCommand() ||
               provider.getDDLQuery().hasAlterColumnCommand();
    }

    private int alterColumn(DriverProvider provider,
                            ColumnSuper oldcolumn,
                            XPropertySet newcolumn,
                            String oldname,
                            boolean autoincrement,
                            int flags)
        throws SQLException {
        int result = 0;
        String table = null;
        List<String> queries = new ArrayList<>();
        NamedComponents component = getNamedComponents();
        ComposeRule rule = ComposeRule.InTableDefinitions;
        try {
            table = DBTools.buildName(provider, component, rule);
            boolean alterpk = isPrimaryKeyColumn(oldname);
            boolean alterfk = isForeignKeyColumn(oldname);
            boolean alteridx = isIndexColumn(oldname);
            boolean alterkey = alterpk || alterfk;
            String tablename = DBTools.composeTableName(provider, component, rule, isCaseSensitive());
            result = TableHelper.getAlterColumnQueries(queries, provider, tablename, oldname, oldcolumn,
                                                       newcolumn, flags, alterkey, isCaseSensitive());
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table, query);
                for (String q : queries) {
                    System.out.println("TableSuper.alterColumn() Query: " + q);
                }
                if (DBTools.executeSQLQueries(provider, queries)) {
                    setColumnProperties(provider, oldcolumn, newcolumn, component, rule,
                                        table, oldname, alterpk, alterfk, alteridx, result);
                }
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY_ERROR;
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return result;
    }

    private void setColumnProperties(DriverProvider provider,
                                     ColumnSuper oldcolumn,
                                     XPropertySet newcolumn,
                                     NamedComponents component,
                                     ComposeRule rule,
                                     String table,
                                     String oldname,
                                     boolean alterpk,
                                     boolean alterfk,
                                     boolean alteridx,
                                     int result)
        throws SQLException, java.sql.SQLException {
        // Column have changed its description value
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_DESCRIPTION)) {
            oldcolumn.mDescription = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DESCRIPTION);
        }
        // Column have changed its not null constraint
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_NULLABLE)) {
            oldcolumn.mIsNullable = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.ISNULLABLE);
        }
        // Column have changed its default value
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_DEFAULT_VALUE)) {
            oldcolumn.mDefaultValue = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DEFAULTVALUE);
        }
        // Column have changed its type
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_TYPE)) {
            oldcolumn.mType = DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.TYPE);
            oldcolumn.mTypeName = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME);
        }
        // Column have changed its identity (auto-increment)
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_IDENTITY)) {
            oldcolumn.mIsAutoIncrement = DBTools.getDescriptorBooleanValue(newcolumn, PropertyIds.ISAUTOINCREMENT);
            System.out.println("TableSuper.setColumnProperties() AutoIncrement: " + oldcolumn.mIsAutoIncrement);
        }
        // Column have changed its name
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_NAME)) {
            String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
            renameColumnName(provider, component, rule, table, oldname,
                             newname, alterpk, alterfk, alteridx);
        }
    }

    private void renameColumnName(DriverProvider provider,
                                  NamedComponents component,
                                  ComposeRule rule,
                                  String table,
                                  String oldname,
                                  String newname,
                                  boolean alterpk,
                                  boolean alterfk,
                                  boolean alteridx)
        throws SQLException, java.sql.SQLException {
        mColumns.replaceElement(oldname, newname);
        if (alterpk) {
            getKeysInternal().renameKeyColumn(KeyType.PRIMARY, oldname, newname);
            // XXX: If the renamed column is a primary key, we need to know if it is referenced as a foreign key.
            // XXX: If this is the case then we need to rename the corresponding column in these foreign keys.
            Map<String, List<String>> tables = KeyHelper.getExportedTablesColumns(provider, component, newname, rule);
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
            // XXX: If the renamed column is declared as index
            // XXX: we need to rename the Index column name to.
            getIndexesInternal().renameIndexColumn(oldname, newname);
        }
    }

    private boolean isPrimaryKeyColumn(String column)
        throws SQLException {
        // FIXME: Here we search and retrieve the first primary key having this column.
        boolean primary = false;
        KeyContainer keys = getKeysInternal();
        for (String name : keys.getElementNames()) {
            Key key = keys.getElement(name);
            primary = key.mType == KeyType.PRIMARY && key.getColumnsInternal().hasByName(column);
            break;
        }
        return primary;
    }

    private boolean isForeignKeyColumn(String column)
        throws SQLException {
        // FIXME: Here we search and retrieve the first foreign key having this table / column.
        boolean foreign = false;
        KeyContainer keys = getKeysInternal();
        for (String name: keys.getElementNames()) {
            Key key = keys.getElement(name);
            foreign = key.mType == KeyType.FOREIGN && key.getColumnsInternal().hasByName(column);
            break;
        }
        return foreign;
    }

    private boolean isIndexColumn(String column)
        throws SQLException {
        boolean index = false;
        // FIXME: Here we search and retrieve if this column is declared as index.
        IndexContainer indexes = getIndexesInternal();
        for (String name : indexes.getElementNames()) {
            index = indexes.getElement(name).getColumnsInternal().hasByName(column);
        }
        return index;
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        TableDescriptor descriptor = new TableDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

    // com.sun.star.sdbcx.XRename
    // XXX: see: https://github.com/LibreOffice/core/blob/6361a9398584defe9ab8db1e3383e02912e3f24c/
    // XXX: connectivity/source/drivers/postgresql/pq_xtable.cxx#L136
    @Override
    public void rename(String name)
        throws SQLException,
               ElementExistException {
        System.out.println("sdbcx.TableSuper.rename() Table: '" + name + "'");

        String query = null;
        String table = null;
        try {
            boolean renamed = false;
            // XXX: Table and View use the same functions to rename.
            boolean isview = mType.toUpperCase().contains("VIEW");
            ComposeRule rule = ComposeRule.InDataManipulation;
            DriverProvider provider = getConnection().getProvider();
            table = DBTools.buildName(provider, getNamedComponents(), rule);
            // XXX: We can handle renaming if it is a table and the driver does not have a command to rename the table
            // XXX: or it's a view and we don't have access to the view's command definition.
            if (!isview && !provider.getDDLQuery().supportsRenamingTable() ||
                isview && !provider.getDDLQuery().supportsViewDefinition()) {
                int resource = getRenameTableNotImplementedResource(isview);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table);
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            NamedComponents component = DBTools.qualifiedNameComponents(provider, name, rule);
            if (isview) {
                renamed = renameView(provider, component, rule, table, name);
            } else {
                renamed = rename(component, table, name, false, rule);
            }

            if (renamed) {
                mCatalogName = component.getCatalogName();
                mSchemaName = component.getSchemaName();
                setName(component.getTableName());
                getConnection().getTablesInternal().rename(table, name);
                // XXX: If renamed table is not a view and are part of a foreign key
                // XXX: the referenced table name is not any more valid.
                // XXX: So we need to rename the referenced table name in all other
                // XXX: table having a foreign keys referencing this table.
                if (!isview) {
                    List<String> filter = KeyHelper.getExportedTables(provider, component, rule);
                    getConnection().getTablesInternal().renameReferencedTableName(filter, table, name);
                }
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private boolean renameView(DriverProvider provider,
                               NamedComponents component,
                               ComposeRule rule,
                               String table,
                               String name) throws SQLException, java.sql.SQLException {
        boolean renamed = false;
        String query = "";
        ViewContainer views = getConnection().getViewsInternal();
        View view = (View) views.getElement(table);
        if (view == null) {
            int resource = Resources.STR_LOG_VIEW_RENAME_VIEW_NOT_FOUND_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table);
            throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
        }
        // XXX: Some databases DRIVER cannot rename views (ie: SQLite). In this case the Drivers.xcu property
        // XXX: SupportRenameView can be used to signal this and allow execution by a DROP VIEW then CREATE VIEW
        try {
            if (provider.getDDLQuery().supportsRenameView()) {
                view.rename(name);
                renamed = true;
            } else {
                getConnection().getViewsInternal().removeView(view);
                query = DBTools.getCreateViewCommand(provider, component, view.mCommand, rule, isCaseSensitive());
                getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEW_RENAME_QUERY, name, query);
                if (DBTools.executeSQLQuery(provider, query)) {
                    views.rename(table, name);
                    renamed = true;
                }
            }
        } catch (ElementExistException e) {
            int resource = Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return renamed;
    }

    protected void refreshColumns() {
        try {
            List<ColumnDescription> columns = ColumnHelper.readColumns(getConnection().getProvider(),
                                                                       getNamedComponents());
            if (mColumns == null) {
                mColumns = getColumnContainer(columns);
            } else {
                mColumns.refill(getColumnName(columns));
            }
        } catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private List<String> getColumnName(List<ColumnDescription> descriptions) {
        List<String> columns = new ArrayList<>();
        for (ColumnDescription column: descriptions) {
            columns.add(column.mColumnName);
        }
        return columns;
    }

    protected void refreshKeys() {
        try {
            DriverProvider provider = getConnection().getProvider();
            List<String> keys = KeyHelper.refreshKeys(provider, getNamedComponents());
            System.out.println("TableSuper.refreshKeys() Table: " + getName() + " - Keys: " + String.join(", ", keys));
            if (mKeys == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_KEYS);
                mKeys = new KeyContainer(this, isCaseSensitive(), keys);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_KEYS_ID, mKeys.getLogger().getObjectId());
            } else {
                mKeys.refill(keys);
            }
        } catch (java.sql.SQLException | ElementExistException e) {
            System.out.println("TableSuper.refreshKeys() ERROR: " + UnoHelper.getStackTrace(e));
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected void refreshIndexes() {
        try {
            List<String> indexes = IndexHelper.readIndexes(getConnection().getProvider(),
                                                           getNamedComponents(), mQualifiedindex);
            if (mIndexes == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_INDEXES);
                mIndexes = new IndexContainer(this, isCaseSensitive(), indexes);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_INDEXES_ID,
                                   mIndexes.getLogger().getObjectId());
            } else {
                mIndexes.refill(indexes);
            }
        } catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected abstract ColumnContainerBase<?> getColumnContainer(List<ColumnDescription> descriptions)
        throws ElementExistException;

}
