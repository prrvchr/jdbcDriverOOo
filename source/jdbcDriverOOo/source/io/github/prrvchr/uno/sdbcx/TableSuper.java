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
import java.util.Iterator;
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

import io.github.prrvchr.uno.driver.helper.ColumnHelper;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.IndexHelper;
import io.github.prrvchr.uno.driver.helper.KeyHelper;
import io.github.prrvchr.uno.driver.helper.TableHelper;
import io.github.prrvchr.uno.driver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class TableSuper
    extends TableBase
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory {

    protected String mDescription = "";
    protected String mType = "";
    protected int mPrivileges = 0;
    private ColumnContainerBase<?> mColumns = null;
    private ColumnListener mListener = null;
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

        properties.put(PropertyIds.PRIVILEGES.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getPrivileges();
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
    public void dispose() {
        if (mKeys != null) {
            mKeys.dispose();
        }
        if (mIndexes != null) {
            mIndexes.dispose();
        }
        if (mColumns != null) {
            if (mListener != null) {
                mColumns.removeContainerListener(mListener);
            }
            mColumns.dispose();
        }
        super.dispose();
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
        System.out.println("TableSuper.alterColumnByIndex() 1 index: " + index);
        System.out.println("TableSuper.alterColumnByIndex() 2 columns: " +
                            String.join(", ", mColumns.getElementNames()));
        checkDisposed();
        ColumnSuper oldcolumn = mColumns.getElement(index);
        System.out.println("TableSuper.alterColumnByIndex() 3 oldname: " + oldcolumn.getName());
        if (oldcolumn != null) {
            alterColumn(oldcolumn, newcolumn);
        }
    }

    @Override
    public void alterColumnByName(String name, XPropertySet newcolumn)
        throws SQLException, NoSuchElementException {
        System.out.println("TableSuper.alterColumnByName() 1 oldname: " + name);
        checkDisposed();
        ColumnSuper oldcolumn = mColumns.getElement(name);
        if (oldcolumn != null) {
            System.out.println("TableSuper.alterColumnByName() 2 oldname: " + name);
            alterColumn(oldcolumn, newcolumn);
        }
    }

    private void alterColumn(ColumnSuper oldcolumn, XPropertySet newcolumn)
        throws SQLException {
        System.out.println("TableSuper.alterColumn() 1");
        Provider provider = getConnection().getProvider();

        String oldname = oldcolumn.getName();
        String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
        System.out.println("TableSuper.alterColumn() 2 oldname: " + oldname +
                           " - newname: " + newname);
        int flags = TableHelper.getAlterColumnChanges(oldcolumn, newcolumn, oldname, newname);
        System.out.println("TableSuper.alterColumn() 3 flags: " + flags);

        // XXX: Identity or Type have been changed?
        // XXX: Identity switching is only allowed if the underlying driver supports it.
        // XXX: Changing column type is only allowed if the underlying driver supports it.
        if (TableHelper.hasColumnIdentityChanged(flags) && !supportColumnIdentityChange(provider) ||
            TableHelper.hasColumnTypeChanged(flags) && !supportColumnTypeChange(provider)) {
            System.out.println("TableSuper.alterColumn() 4 ERROR");
            int resource = Resources.STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
        }

        System.out.println("TableSuper.alterColumn() 5");
        int result = alterColumn(provider, oldcolumn, newcolumn, oldname, flags);
        System.out.println("TableSuper.alterColumn() 6 result: " + result);
        if (result != flags) {
            System.out.println("TableSuper.alterColumn() ERROR ******************************************");
        }
    }

    private boolean supportColumnIdentityChange(Provider provider) {
        return provider.getConfigDDL().supportsAlterIdentity() ||
               provider.getConfigDDL().hasAlterColumnCommand();
    }

    private boolean supportColumnTypeChange(Provider provider) {
        return provider.getConfigDDL().hasAlterColumnTypeCommand() ||
               provider.getConfigDDL().hasAlterColumnCommand();
    }

    private int alterColumn(Provider provider,
                            ColumnSuper oldcolumn,
                            XPropertySet newcolumn,
                            String oldname,
                            int flags)
        throws SQLException {
        int result = 0;
        String table = null;
        System.out.println("TableSuper.alterColumn() 1");
        List<String> queries = new ArrayList<>();
        System.out.println("TableSuper.alterColumn() 2");
        NamedComponents component = getNamedComponents();
        System.out.println("TableSuper.alterColumn() 3");
        ComposeRule rule = ComposeRule.InTableDefinitions;
        try {
            System.out.println("TableSuper.alterColumn() 4");
            table = DBTools.buildName(provider, component, rule);
            System.out.println("TableSuper.alterColumn() 5");
            boolean alterpk = isPrimaryKeyColumn(oldname);
            System.out.println("TableSuper.alterColumn() 6");
            boolean alterfk = isForeignKeyColumn(oldname);
            System.out.println("TableSuper.alterColumn() 7");
            boolean alterkey = alterpk || alterfk;
            System.out.println("TableSuper.alterColumn() 8");
            result = TableHelper.getAlterColumnQueries(queries, provider, component, rule, oldname, oldcolumn,
                                                       newcolumn, flags, alterkey, isCaseSensitive());
            System.out.println("TableSuper.alterColumn() 9");
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table, query);
                for (String q : queries) {
                    System.out.println("TableSuper.alterColumn() Query: " + q);
                }
                if (DBTools.executeSQLQueries(provider, queries)) {
                    setColumnProperties(oldcolumn, newcolumn, oldname, result);
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

    private void setColumnProperties(ColumnSuper oldcolumn,
                                     XPropertySet newcolumn,
                                     String oldname,
                                     int result)
        throws SQLException, java.sql.SQLException {
        System.out.println("TableSuper.setColumnProperties() 1");

        // Column have changed its description value
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_DESCRIPTION)) {
            oldcolumn.setDescriptionInternal(DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DESCRIPTION));
        }
        // Column have changed its not null constraint
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_NULLABLE)) {
            oldcolumn.setIsNullableInternal(DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.ISNULLABLE));
        }
        // Column have changed its default value
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_DEFAULT_VALUE)) {
            oldcolumn.setDefaultValueInternal(DBTools.getDescriptorStringValue(newcolumn, PropertyIds.DEFAULTVALUE));
        }
        // Column have changed its type
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_TYPE)) {
            oldcolumn.setTypeInternal(DBTools.getDescriptorIntegerValue(newcolumn, PropertyIds.TYPE));
            oldcolumn.setTypeNameInternal(DBTools.getDescriptorStringValue(newcolumn, PropertyIds.TYPENAME));
        }
        // Column have changed its identity (auto-increment)
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_IDENTITY)) {
            oldcolumn.setIsAutoIncrementInternal(DBTools.getDescriptorBooleanValue(newcolumn,
                                                                                   PropertyIds.ISAUTOINCREMENT));
        }
        // Column have changed its name
        if (TableHelper.hasPropertyChanged(result, TableHelper.COLUMN_NAME)) {
            System.out.println("TableSuper.setColumnProperties() 2");
            String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
            mColumns.replaceElement(oldname, newname);
        }
    }

    @SuppressWarnings("unused")
    private void renameColumnName(Provider provider,
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
            System.out.println("TableSuper.renameColumnName() 1");
            //getKeysInternal().renameKeyColumn(KeyType.PRIMARY, oldname, newname);
            // XXX: If the renamed column is a primary key, we need to know if it is referenced as a foreign key.
            // XXX: If this is the case then we need to rename the corresponding column in these foreign keys.
            //Map<String, List<String>> tables = KeyHelper.getExportedTablesColumns(provider, component, newname, rule);
            //if (!tables.isEmpty()) {
            //    getConnection().getTablesInternal().renameForeignKeyColumn(tables, table, oldname, newname);
            //}
        }
        if (alterfk) {
            System.out.println("TableSuper.renameColumnName() 2");
            // XXX: If the renamed column is a foreign key we need to rename the Key column name to.
            // XXX: Renaming the foreign key should rename the associated Index column name as well.
            //getKeysInternal().renameKeyColumn(KeyType.FOREIGN, oldname, newname);
        }
        if (alteridx) {
            System.out.println("TableSuper.renameColumnName() 3");
            // XXX: If the renamed column is declared as index
            // XXX: we need to rename the Index column name to.
            //getIndexesInternal().renameIndexColumn(oldname, newname);
        }
    }

    private boolean isPrimaryKeyColumn(String column)
        throws SQLException {
        // FIXME: Here we search and retrieve the first primary key having this column.
        boolean primary = false;
        Iterator<Key> it = getKeysInternal().getActiveElements();
        while (it.hasNext()) {
            Key key = it.next();
            primary = key.getTypeInternal() == KeyType.PRIMARY && key.getColumnsInternal().hasByName(column);
            break;
        }
        return primary;
    }

    private boolean isForeignKeyColumn(String column)
        throws SQLException {
        // FIXME: Here we search and retrieve the first foreign key having this table / column.
        boolean foreign = false;
        Iterator<Key> it = getKeysInternal().getActiveElements();
        while (it.hasNext()) {
            Key key = it.next();
            foreign = key.getTypeInternal() == KeyType.FOREIGN && key.getColumnsInternal().hasByName(column);
            break;
        }
        return foreign;
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

        String query = null;
        String table = null;
        try {
            boolean renamed = false;
            // XXX: Table and View use the same functions to rename.
            boolean isview = mType.toUpperCase().contains("VIEW");
            ComposeRule rule = ComposeRule.InDataManipulation;
            Provider provider = getConnection().getProvider();
            table = DBTools.buildName(provider, getNamedComponents(), rule);

            // XXX: We can handle renaming if it is a table and the driver does not have a command to rename the table
            // XXX: or it's a view and we don't have access to the view's command definition.
            if (!isview && !provider.getConfigDDL().supportsRenamingTable() ||
                isview && !provider.getConfigDDL().supportsViewDefinition()) {
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
                //if (!isview) {
                    //List<String> filter = KeyHelper.getExportedTables(provider, component, rule);
                    //getConnection().getTablesInternal().renameReferencedTableName(filter, table, name);
                //}
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            int resource = Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, table, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private boolean renameView(Provider provider,
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
            if (provider.getConfigDDL().supportsRenameView()) {
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
                mListener = new ColumnListener();
                mColumns.addContainerListener(mListener);
            } else {
                mColumns.refill(getColumnName(columns));
            }
        } catch (java.sql.SQLException | ElementExistException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private String[] getColumnName(List<ColumnDescription> descriptions) {
        List<String> columns = new ArrayList<>();
        for (ColumnDescription column: descriptions) {
            columns.add(column.mColumnName);
        }
        return columns.toArray(new String[0]);
    }

    protected void refreshKeys() {
        try {
            Provider provider = getConnection().getProvider();
            String[] keys = KeyHelper.refreshKeys(provider, getNamedComponents());
            if (mKeys == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_KEYS);
                mKeys = new KeyContainer(this, isCaseSensitive(), keys);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_KEYS_ID, mKeys.getLogger().getObjectId());
            } else {
                mKeys.refill(keys);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("TableSuper.refreshKeys() ERROR: " + UnoHelper.getStackTrace(e));
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected void refreshIndexes() {
        try {
            String[] indexes = IndexHelper.readIndexes(getConnection().getProvider(),
                                                       getNamedComponents(), mQualifiedindex);
            if (mIndexes == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_INDEXES);
                mIndexes = new IndexContainer(this, isCaseSensitive(), indexes);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_INDEXES_ID,
                                   mIndexes.getLogger().getObjectId());
            } else {
                mIndexes.refill(indexes);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected String composeTableName() {
        return composeTableName(ComposeRule.InTableDefinitions);
    }

    protected String composeTableName(ComposeRule rule) {
        NamedComponents component = getNamedComponents();
        return DBTools.buildName(getConnection().getProvider(), component, rule);
    }

    protected abstract int getPrivileges() throws WrappedTargetException;

    protected abstract ColumnContainerBase<?> getColumnContainer(List<ColumnDescription> descriptions)
        throws ElementExistException;

}
