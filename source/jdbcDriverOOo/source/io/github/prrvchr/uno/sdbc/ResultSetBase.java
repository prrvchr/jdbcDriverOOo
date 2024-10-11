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
package io.github.prrvchr.uno.sdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.sdbc.XResultSetUpdate;
import com.sun.star.sdbc.XRow;
import com.sun.star.sdbc.XRowUpdate;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBException;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.PropertySet;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;

public abstract class ResultSetBase
    extends PropertySet
    implements XServiceInfo,
               XCloseable,
               XColumnLocate,
               XResultSet,
               XResultSetMetaDataSupplier,
               XRow,
               XWarningsSupplier,
               XResultSetUpdate,
               XRowUpdate
{

    private final String m_service;
    private final String[] m_services;
    protected ConnectionBase m_Connection;
    protected java.sql.ResultSet m_Result;
    protected StatementMain m_Statement;
    // XXX: We need to keep the index references of the columns already assigned for
    // insertion
    protected BitSet m_Inserted;
    // XXX: We need to know when we are on the insert row
    protected boolean m_OnInsert = false;
    // XXX: Is the last value read null
    protected boolean m_WasNull = false;
    protected final ConnectionLog m_logger;

    // The constructor method:

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet result)
        throws SQLException
    {
        this(service, services, connection, result, null);
    }

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet resultset,
                         StatementMain statement)
        throws SQLException
    {
        m_service = service;
        m_services = services;
        m_Connection = connection;
        m_Result = resultset;
        m_Statement = statement;
        m_Inserted = new BitSet(getResultColumnCount(resultset, statement));
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), LoggerObjectType.RESULTSET);
    }

    static private int getResultColumnCount(java.sql.ResultSet resultset,
                                            StatementMain statement)
        throws SQLException
    {
        try {
            return resultset.getMetaData().getColumnCount();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, statement);
        }
    }

    protected StatementMain getJdbcStatement() {
        return m_Statement;
    }

    protected abstract ConnectionBase getConnection();

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.CURSORNAME.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return _getCursorName();
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.FETCHDIRECTION.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return _getFetchDirection();
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   _setFetchDirection((int) value);
                                               }
                                           }));

        properties.put(PropertyIds.FETCHSIZE.getName(),
                       new PropertyWrapper(Type.LONG,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return _getFetchSize();
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   _setFetchSize((int) value);
                                               }
                                           }));

        properties.put(PropertyIds.RESULTSETCONCURRENCY.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return _getResultSetConcurrency();
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.RESULTSETTYPE.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return _getResultSetType();
                                               }
                                           },
                                           null));

        super.registerProperties(properties);
    }

    private String _getCursorName()
        throws WrappedTargetException
    {
        try {
            String cursor = m_Result.getCursorName();
            if (cursor == null) {
                cursor = "";
            }
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CURSORNAME, cursor);
            return cursor;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    private int _getFetchDirection()
        throws WrappedTargetException
    {
        try {
            int direction = m_Result.getFetchDirection();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_FETCH_DIRECTION, Integer.toString(direction));
            return direction;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    private synchronized void _setFetchDirection(int direction)
        throws WrappedTargetException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_SET_FETCH_DIRECTION, Integer.toString(direction));
            m_Result.setFetchDirection(direction);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    protected int _getFetchSize()
        throws WrappedTargetException
    {
        try {
            int size = m_Result.getFetchSize();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_FETCH_SIZE, Integer.toString(size));
            return size;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    protected synchronized void _setFetchSize(int size)
        throws WrappedTargetException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_SET_FETCH_SIZE, Integer.toString(size));
            m_Result.setFetchSize(size);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    protected int _getResultSetConcurrency()
        throws WrappedTargetException
    {
        try {
            int concurrency = m_Result.getConcurrency();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CONCURRENCY, Integer.toString(concurrency));
            return concurrency;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    protected int _getResultSetType()
        throws WrappedTargetException
    {
        try {
            int type = m_Result.getType();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_TYPE, Integer.toString(type));
            return type;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (m_Result != null) {
            super.postDisposing();
            try {
                m_Result.close();
            }
            catch (java.sql.SQLException e) {
                m_logger.logp(LogLevel.WARNING, e);
            }
            m_Result = null;
        }
    }

    // com.sun.star.sdbc.XCloseable
    @Override
    public void close()
        throws SQLException
    {
        dispose();
    }

    // com.sun.star.sdbc.XColumnLocate:
    @Override
    public int findColumn(String name)
        throws SQLException
    {
        try {
            return m_Result.findColumn(name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // com.sun.star.sdbc.XResultSet:
    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        try {
            boolean moved = m_Result.absolute(row);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ABSOLUTE, Integer.toString(row), Boolean.toString(moved));
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        try {
            m_Result.afterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        try {
            m_Result.beforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean first()
        throws SQLException
    {
        try {
            return m_Result.first();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getRow()
        throws SQLException
    {
        try {
            return m_Result.getRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public com.sun.star.uno.XInterface getStatement()
        throws SQLException
    {
        return m_Statement;
    }

    @Override
    public boolean isAfterLast()
        throws SQLException
    {
        try {
            return m_Result.isAfterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException
    {
        try {
            return m_Result.isBeforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isFirst()
        throws SQLException
    {
        try {
            return m_Result.isFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isLast()
        throws SQLException
    {
        try {
            return m_Result.isLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean last()
        throws SQLException
    {
        System.out.println("ResultSetBase.last() 1");
        try {
            return m_Result.last();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean next()
        throws SQLException
    {
        System.out.println("ResultSetBase.next() 1");
        try {
            boolean next = m_Result.next();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_NEXT, Boolean.toString(next));
            return next;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean previous()
        throws SQLException
    {
        try {
            return m_Result.previous();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void refreshRow()
        throws SQLException
    {
        try {
            m_Result.refreshRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        try {
            boolean moved = m_Result.relative(row);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_RELATIVE, Integer.toString(row), Boolean.toString(moved));
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        try {
            boolean deleted = m_Result.rowDeleted();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
            return deleted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        try {
            boolean inserted = m_Result.rowInserted();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
            return inserted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowUpdated()
        throws SQLException
    {
        try {
            boolean updated = m_Result.rowUpdated();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_UPDATED, Boolean.toString(updated));
            return updated;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(m_services, service);
    }

    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public void clearWarnings()
        throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            WarningsSupplier.clearWarnings(m_Result, this);
    }

    @Override
    public Object getWarnings()
        throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            return WarningsSupplier.getWarnings(m_Result, this);
        return Any.VOID;
    }

    // com.sun.star.sdbc.XResultSetUpdate:
    @Override
    public void insertRow()
        throws SQLException
    {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow cannot be called when moveToInsertRow has not been called !", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        insertNewRow();
    }

    protected void insertNewRow()
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_INSERT_ROW);
            RowHelper.setDefaultColumnValues(m_Result, m_Inserted);
            m_Result.insertRow();
            moveToCurrentRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: updateRow cannot be called when moveToInsertRow has been called!", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        updateCurrentRow();
    }

    protected void updateCurrentRow()
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_ROW);
            m_Result.updateRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: deleteRow cannot be called when moveToInsertRow has been called!", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        deleteCurrentRow();
    }

    protected void deleteCurrentRow()
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_DELETE_ROW);
            m_Result.deleteRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx Line 111:
    // xUp->cancelRowUpdates()
    @Override
    public void cancelRowUpdates()
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
            cancelRowUpdatesInternal();
        }
        catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e.getMessage(), this, StandardSQLState.SQL_INVALID_CURSOR_STATE.text(), 0, e);
        }
    }

    protected void cancelRowUpdatesInternal()
        throws java.sql.SQLException
    {
        // FIXME: *** LibreOffice Base call this method just after calling moveToInsertRow() ***
        // FIXME: Java documentation say: Throws: SQLException - if a database access error occurs;
        // FIXME: this method is called on a closed result set; the result set concurrency is CONCUR_READ_ONLY
        // FIXME: or if this method is called when the cursor is on the insert row
        // FIXME: see: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#cancelRowUpdates--
        if (isOnInsertRow()) {
            moveToCurrentRowInternal();
        }
        else {
            m_Result.cancelRowUpdates();
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx Line 110:
    // xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        try {
            moveToInsertRowInternal();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected void moveToInsertRowInternal()
        throws java.sql.SQLException
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERT_ROW);
        m_Result.moveToInsertRow();
        m_Inserted.clear();
        m_OnInsert = true;
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_CURRENT_ROW);
            moveToCurrentRowInternal();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected void moveToCurrentRowInternal()
        throws java.sql.SQLException
    {
        m_Result.moveToCurrentRow();
        m_OnInsert = false;
    }

    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index)
        throws SQLException
    {
        try {
            java.sql.Array value = m_Result.getArray(index);
            return (value != null) ? new Array(m_Connection, value) : null;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public XInputStream getBinaryStream(int index)
        throws SQLException
    {
        XBlob blob = getBlob(index);
        return blob != null ? blob.getBinaryStream() : null;
    }

    @Override
    public XBlob getBlob(int index)
        throws SQLException
    {
        try {
            java.sql.Blob value = m_Result.getBlob(index);
            return (value != null) ? new Blob(m_Connection, value) : null;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        try {
            return m_Result.getBoolean(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        try {
            return m_Result.getByte(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        try {
            return m_Result.getBytes(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index)
        throws SQLException
    {
        XClob clob = getClob(index);
        return (clob != null) ? clob.getCharacterStream() : null;
    }

    @Override
    public XClob getClob(int index)
        throws SQLException
    {
        try {
            java.sql.Clob value = m_Result.getClob(index);
            return value != null ? new Clob(m_Connection, value) : null;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        try {
            java.sql.Date value = m_Result.getDate(index);
            return value != null ? UnoHelper.getUnoDate(value.toLocalDate()) : new Date();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        try {
            return m_Result.getDouble(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        try {
            return m_Result.getFloat(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        try {
            return m_Result.getInt(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        try {
            return m_Result.getLong(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map)
        throws SQLException
    {
        try {
            return DBTools.getObject(m_Result.getObject(index), map);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public XRef getRef(int index)
        throws SQLException
    {
        try {
            java.sql.Ref value = m_Result.getRef(index);
            return value != null ? new Ref(value) : null;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        try {
            return m_Result.getShort(index);
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        try {
            String value = m_Result.getString(index);
            if (value == null) {
                value = "";
            }
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_PARAMETER, value, "getString",
                    Integer.toString(index));
            return value;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        try {
            java.sql.Time value = m_Result.getTime(index);
            return value != null ? UnoHelper.getUnoTime(value.toLocalTime()) : new Time();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public DateTime getTimestamp(int index)
        throws SQLException
    {
        try {
            java.sql.Timestamp value = m_Result.getTimestamp(index);
            return value != null ? UnoHelper.getUnoDateTime(value.toLocalDateTime()) : new DateTime();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        try {
            return m_Result.wasNull();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    // com.sun.star.sdbc.XRowUpdate:
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        try {
            m_Result.updateNull(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        try {
            m_Result.updateBoolean(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        try {
            m_Result.updateByte(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        try {
            m_Result.updateShort(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        try {
            m_Result.updateInt(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        try {
            m_Result.updateLong(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        try {
            m_Result.updateFloat(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        try {
            m_Result.updateDouble(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        try {
            m_Result.updateString(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        try {
            m_Result.updateBytes(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        try {
            m_Result.updateDate(index, java.sql.Date.valueOf(UnoHelper.getJavaLocalDate(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        try {
            m_Result.updateTime(index, java.sql.Time.valueOf(UnoHelper.getJavaLocalTime(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateTimestamp(int index, DateTime value)
        throws SQLException
    {
        try {
            m_Result.updateTimestamp(index, java.sql.Timestamp.valueOf(UnoHelper.getJavaLocalDateTime(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBinaryStream(int index, XInputStream value, int lenght)
        throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            m_Result.updateBinaryStream(index, input, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateCharacterStream(int index, XInputStream value, int lenght)
        throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            Reader reader = new java.io.InputStreamReader(input);
            m_Result.updateCharacterStream(index, reader, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        if (!DBTools.updateObject(m_Result, index, value)) {
            String error = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNKNOWN_COLUMN_TYPE,
                    this.getClass().getName(), "updateObject()", Integer.toString(index));
            throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public void updateNumericObject(int index, Object value, int scale)
        throws SQLException
    {
        try {
            BigDecimal bigDecimal;
            if (AnyConverter.isDouble(value)) {
                bigDecimal = BigDecimal.valueOf(AnyConverter.toDouble(value));
            }
            else {
                bigDecimal = new BigDecimal(AnyConverter.toString(value));
            }
            m_Result.updateObject(index, bigDecimal, scale);
        } catch (IllegalArgumentException | java.sql.SQLException e) {
            updateObject(index, value);
        }
    }

    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    @Override
    public XResultSetMetaData getMetaData()
        throws SQLException
    {
        try {
            java.sql.ResultSetMetaData metadata = m_Result.getMetaData();
            return (metadata != null) ? new ResultSetMetaData(m_Connection, metadata) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected boolean isOnInsertRow() {
        return m_OnInsert;
    }

    protected java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException
    {
        return getJdbcStatement().getJdbcResultSet();
    }

}
