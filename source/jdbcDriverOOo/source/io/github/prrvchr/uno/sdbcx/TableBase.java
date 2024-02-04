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
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.sdbcx.XColumnsSupplier;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DataBaseTableHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.DataBaseTableHelper.ColumnDescription;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public abstract class TableBase
    extends Descriptor
    implements XColumnsSupplier,
               XIndexesSupplier,
               XKeysSupplier,
               XAlterTable,
               XDataDescriptorFactory
{

    protected final ConnectionSuper m_connection;
    protected final ConnectionLog m_logger; 
    private ColumnContainerBase m_columns = null;
    private KeyContainer m_keys = null;
    private IndexContainer m_indexes = null;
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    protected String m_Description = "";
    protected String m_Type = "";

    // The constructor method:
    public TableBase(String service,
                     String[] services,
                     ConnectionSuper connection,
                     boolean sensitive,
                     String name)
    {
        super(service, services, sensitive, name);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), LoggerObjectType.TABLE);
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CATALOGNAME.name, PropertyIds.CATALOGNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CatalogName;
                }
            }, null);
        registerProperty(PropertyIds.SCHEMANAME.name, PropertyIds.SCHEMANAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_SchemaName;
                }
            }, null);
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

    public ConnectionLog getLogger()
    {
        return m_logger;
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
        try {
            if (m_columns == null) {
                m_columns = _refreshColumns();
            }
            return m_columns;
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.TableBase.getColumns() 2" + UnoHelper.getStackTrace(e));
        }
        return null;
    }

    // com.sun.star.sdbcx.XKeysSupplier:
    @Override
    public XIndexAccess getKeys() {
        checkDisposed();
        if (m_keys == null) {
            m_keys = _refreshKeys();
        }
        return m_keys;
    }


    // com.sun.star.sdbcx.XIndexesSupplier
    @Override
    public XNameAccess getIndexes() {
        checkDisposed();
        if (m_indexes == null) {
            m_indexes = _refreshIndexes();
        }
        return m_indexes;
    }

    // com.sun.star.sdbcx.XAlterTable:
    @Override
    public void alterColumnByIndex(int index, XPropertySet newcolumn)
        throws SQLException, IndexOutOfBoundsException
    {
        checkDisposed();
        XPropertySet oldcolumn = null;
        try {
            oldcolumn = (XPropertySet) AnyConverter.toObject(XPropertySet.class, m_columns.getByIndex(index));
        }
        catch (WrappedTargetException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), getConnection());
        }
        if (oldcolumn != null) {
            String table = DBTools.composeTableName(getConnection(), this, ComposeRule.InTableDefinitions, false);
            List<String> queries = DBTools.getAlterColumnQueries(getConnection(), this, oldcolumn, newcolumn, isCaseSensitive());
            if (!queries.isEmpty()) {
                DBTools.executeDDLQueries(getConnection(), queries, m_logger, this.getClass().getName(),
                                          "alterColumnByIndex", Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table);
                m_columns = _refreshColumns();
            }
        }
    }

    @Override
    public void alterColumnByName(String name, XPropertySet newcolumn)
        throws SQLException, NoSuchElementException
    {
        checkDisposed();
        XPropertySet oldcolumn = null;
        try {
            oldcolumn = (XPropertySet) AnyConverter.toObject(XPropertySet.class, m_columns.getByName(name));
        }
        catch (WrappedTargetException e) {
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), getConnection());
        }
        if (oldcolumn != null) {
            String table = DBTools.composeTableName(getConnection(), this, ComposeRule.InTableDefinitions, false);
            List<String> queries = DBTools.getAlterColumnQueries(getConnection(), this, oldcolumn, newcolumn, isCaseSensitive());
            if (!queries.isEmpty()) {
                DBTools.executeDDLQueries(getConnection(), queries, m_logger, this.getClass().getName(),
                                          "alterColumnByName", Resources.STR_LOG_TABLE_ALTER_COLUMN_QUERY, table);
                m_columns = _refreshColumns();
            }
        }
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public abstract XPropertySet createDataDescriptor();


    protected ColumnContainerBase _refreshColumns()
    {
        try {
            List<ColumnDescription> columns = DataBaseTableHelper.readColumns(getConnection(), this);
            return _getColumnContainer(columns);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (java.sql.SQLException e) {
            return null;
        }
    }

    private IndexContainer _refreshIndexes()
    {
        try {
            List<String> indexes = DataBaseTableHelper.readIndexes(getConnection(), this);
            System.out.println("sdbcx.TableBase._refreshIndexes() Index Count: " + indexes.size());
            return new IndexContainer(this, isCaseSensitive(), indexes);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (SQLException e) {
            return null;
        }
    }

    private KeyContainer _refreshKeys() {
        try {
            Map<String, Key> keys = DataBaseTableHelper.readKeys(getConnection(), isCaseSensitive(), this);
            System.out.println("sdbcx.TableBase._refreshKeys() Key Count: " + keys.size());
            return new KeyContainer(this, isCaseSensitive(), keys);
        }
        catch (ElementExistException e) {
            return null;
        }
        catch (SQLException e) {
            return null;
        }
    }

    public String getCatalogName()
    {
        return m_CatalogName;
    }
    public String getSchemaName()
    {
        return m_SchemaName;
    }

    public String getCatalog()
    {
        return m_CatalogName.isEmpty() ? null : m_CatalogName;
    }
    public String getSchema()
    {
        return m_SchemaName.isEmpty() ? null : m_SchemaName;
    }

    public abstract ConnectionSuper getConnection();

    protected abstract ColumnContainerBase _getColumnContainer(List<ColumnDescription> descriptions) throws ElementExistException;

}
