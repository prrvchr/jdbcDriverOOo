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
import io.github.prrvchr.jdbcdriver.DBColumnHelper;
import io.github.prrvchr.jdbcdriver.DBTableHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DBColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class TableSuper
    extends TableMain
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    private ColumnContainerBase m_columns = null;
    private KeyContainer m_keys = null;
    private IndexContainer m_indexes = null;
    protected String m_Description = "";
    protected String m_Type = "";

    // The constructor method:
    public TableSuper(String service,
                      String[] services,
                      ConnectionSuper connection,
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

    protected ColumnContainerBase getColumnsInternal()
    {
        checkDisposed();
        if (m_columns == null) {
            m_columns = refreshColumns();
        }
        return m_columns;
    }

    protected KeyContainer getKeysInternal()
    {
        checkDisposed();
        if (m_keys == null) {
            m_keys = refreshKeys();
        }
        return m_keys;
    }

    protected IndexContainer getIndexesInternal()
    {
        checkDisposed();
        if (m_indexes == null) {
            m_indexes = refreshIndexes();
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

    private void alterColumn(ColumnSuper oldcolumn, XPropertySet newcolumn)
        throws SQLException
    {
        if (oldcolumn != null) {
            try {
                boolean executed = false;
                String oldname = oldcolumn.getName();
                KeyColumnContainer keys = getKeyColumnContainer(oldname);
                boolean alterpk = keys != null;
                List<String> queries = new ArrayList<String>();
                boolean renamed = DBTableHelper.getAlterColumnQueries(queries, getConnection().getProvider(), this, oldcolumn, newcolumn, alterpk, isCaseSensitive());
                if (!queries.isEmpty()) {
                    String table = DBTools.buildName(getConnection().getProvider(), this, ComposeRule.InTableDefinitions);
                    executed = DBTools.executeDDLQueries(getConnection().getProvider(), queries, m_logger, this.getClass().getName(),
                                                         "alterColumnByName", Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table);
                }
                if (executed) {
                    String newname = DBTools.getDescriptorStringValue(newcolumn, PropertyIds.NAME);
                    if (renamed) { 
                        if (alterpk) {
                            // XXX: If the renamed column is a primary key we need to rename the Key name to.
                            keys.rename(oldcolumn.getName(), newname);
                        }
                        m_columns.getElement(oldname).setName(newname);
                    }
                    m_columns.replaceElement(oldname, newname);
                    
                }
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
    }

    private KeyColumnContainer getKeyColumnContainer(String oldname)
        throws SQLException
    {
        // FIXME: Here we search and retrieve the first primary key having this column.
        KeyColumnContainer container = null;
        KeyContainer keys = getKeysInternal();
        for (String keyname : keys.getElementNames()) {
            Key key = keys.getElement(keyname);
            KeyColumnContainer columns = key.getColumnsInternal();
            for (String name : columns.getElementNames()) {
                if (oldname.equals(name)) {
                    container = columns;
                    break;
                }
            }
            if (container != null) {
                break;
            }
        }
        return container;
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

    protected ColumnContainerBase refreshColumns()
    {
        try {
            List<ColumnDescription> columns = DBColumnHelper.readColumns(getConnection().getProvider(), this);
            return getColumnContainer(columns);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (java.sql.SQLException e) {
            return null;
        }
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
        int offset = isview ? Resources.STR_JDBC_LOG_MESSAGE_TABLE_VIEW_OFFSET : 0;
        ComposeRule rule = ComposeRule.InDataManipulation;
        try {
            String oldname = DBTools.composeTableName(m_connection.getProvider(), this, rule, false);
            if (!m_connection.getProvider().supportRenamingTable()) {
                int resource = Resources.STR_LOG_TABLE_RENAME_UNSUPPORTED_FEATURE_ERROR + offset;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            NameComponents component = DBTools.qualifiedNameComponents(m_connection.getProvider(), name, rule);
            if (isview) {
                ViewContainer views = m_connection.getViewsInternal();
                View view = views.getElement(oldname);
                if (view == null) {
                    int resource = Resources.STR_LOG_TABLE_RENAME_TABLE_NOT_FOUND_ERROR + offset;
                    String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
                    throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
                }
                if (m_connection.getProvider().supportRenameView()) {
                    view.rename(name);
                }
                // FIXME: Some databases DRIVER cannot rename views (ie: SQLite). In this case the Drivers.xcu property
                // FIXME: SupportRenameView can be used to signal this and allow execution by a DROP VIEW then CREATE VIEW
                else {
                    m_connection.getViewsInternal().removeElement(view);
                    String query = DBTools.getCreateViewQuery(m_connection.getProvider(), component, view.m_Command, rule, isCaseSensitive());
                        DBTools.executeDDLQuery(m_connection.getProvider(), query, m_logger, this.getClass().getName(),
                                                "_createView", Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name);
                    views.rename(oldname, name, offset);
                }
                m_SchemaName = component.getSchema();
            }
            else {
                rename(component, oldname, name, rule, offset);
                m_SchemaName = component.getSchema();
            }
            m_Name = component.getTable();
            m_connection.getTablesInternal().rename(oldname, name, offset);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    private IndexContainer refreshIndexes()
    {
        try {
            List<String> indexes = DBColumnHelper.readIndexes(getConnection().getProvider(), this);
            System.out.println("sdbcx.TableBase._refreshIndexes() Index Count: " + indexes.size());
            return new IndexContainer(this, isCaseSensitive(), indexes);
        }
        catch (java.sql.SQLException | SQLException | ElementExistException e) {
            return null;
        }
    }

    private KeyContainer refreshKeys() {
        try {
            Map<String, Key> keys = DBColumnHelper.readKeys(getConnection().getProvider(), isCaseSensitive(), this);
            System.out.println("sdbcx.TableBase._refreshKeys() Key Count: " + keys.size());
            return new KeyContainer(this, isCaseSensitive(), keys);
        }
        catch (java.sql.SQLException | SQLException | ElementExistException e) {
            return null;
        }
    }

    protected abstract ColumnContainerBase getColumnContainer(List<ColumnDescription> descriptions) throws ElementExistException;

}
