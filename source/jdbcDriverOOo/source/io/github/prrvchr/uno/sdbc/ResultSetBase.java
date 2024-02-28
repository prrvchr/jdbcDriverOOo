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
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.PropertySet;
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
    protected final ConnectionLog m_logger;
    protected java.sql.ResultSet m_ResultSet;
    private StatementMain<?> m_Statement;
    protected boolean m_insert = false;


    // The constructor method:

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet resultset)
        throws SQLException
    {
        this(service, services, connection, resultset, null);
    }

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet resultset,
                         StatementMain<?> statement)
        throws SQLException
    {
        m_service = service;
        m_services = services;
        m_Connection = connection;
        m_ResultSet = resultset;
        m_Statement = statement;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), LoggerObjectType.RESULTSET);
        registerProperties();
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CURSORNAME.name, PropertyIds.CURSORNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getCursorName();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETCONCURRENCY.name, PropertyIds.RESULTSETCONCURRENCY.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetConcurrency();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETTYPE.name, PropertyIds.RESULTSETTYPE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetType();
                }
            }, null);
        registerProperty(PropertyIds.FETCHDIRECTION.name, PropertyIds.FETCHDIRECTION.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchDirection();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchDirection((int) value);
                }
            });
        registerProperty(PropertyIds.FETCHSIZE.name, PropertyIds.FETCHSIZE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchSize();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchSize((int) value);
                }
            });
    }

    private String _getCursorName()
        throws WrappedTargetException
    {
        try {
            String cursor = m_ResultSet.getCursorName();
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
            int direction = m_ResultSet.getFetchDirection();
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
            m_ResultSet.setFetchDirection(direction);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getFetchSize()
        throws WrappedTargetException
    {
        try {
            int size = m_ResultSet.getFetchSize();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_FETCH_SIZE, Integer.toString(size));
            return size;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private synchronized void _setFetchSize(int size)
        throws WrappedTargetException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_SET_FETCH_SIZE, Integer.toString(size));
            m_ResultSet.setFetchSize(size);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getResultSetConcurrency()
        throws WrappedTargetException
    {
        try {
            int concurrency = m_ResultSet.getConcurrency();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CONCURRENCY, Integer.toString(concurrency));
            return concurrency;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getResultSetType()
        throws WrappedTargetException
    {
        try {
            int type = m_ResultSet.getType();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_TYPE, Integer.toString(type));
            return type;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }


    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing()
    {
        if (m_Statement != null) {
            // FIXME: If we use logging and this ResultSet come from DatabaseMetaData
            // FIXME: then it may produce Fatal exception: Signal 11 (SIGSEGV)
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CLOSING);
        }
        super.postDisposing();
        if (m_ResultSet != null) {
            try {
                m_ResultSet.close();
            }
            catch (java.sql.SQLException e) {
                m_logger.logp(LogLevel.WARNING, e);
            }
            m_ResultSet = null;
        }
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public void close() throws SQLException
    {
        checkDisposed();
        dispose();
    }


    // com.sun.star.sdbc.XColumnLocate:
    @Override
    public int findColumn(String name) throws SQLException
    {
        try {
            return m_ResultSet.findColumn(name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XResultSet:
    @Override
    public boolean absolute(int row) throws SQLException
    {
        try {
            boolean moved = m_ResultSet.absolute(row);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ABSOLUTE, Integer.toString(row), Boolean.toString(moved));
            return moved;
        }
        catch (java.sql.SQLException e) {
            System.out.println("ResultSetBase.absolute() ERROR:\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("ResultSetBase.absolute() ERROR:\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
    }

    @Override
    public void afterLast() throws SQLException
    {
        try {
            m_ResultSet.afterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void beforeFirst() throws SQLException
    {
        try {
            m_ResultSet.beforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }

    }

    @Override
    public boolean first() throws SQLException
    {
        try {
            return m_ResultSet.first();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getRow() throws SQLException
    {
        try {
            return m_ResultSet.getRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public com.sun.star.uno.XInterface getStatement() throws SQLException
    {
        return m_Statement;
    }

    @Override
    public boolean isAfterLast() throws SQLException
    {
        try {
            return m_ResultSet.isAfterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        try {
            return m_ResultSet.isBeforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isFirst() throws SQLException
    {
        try {
            return m_ResultSet.isFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isLast() throws SQLException
    {
        try {
            return m_ResultSet.isLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean last() throws SQLException
    {
        try {
            return m_ResultSet.last();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean next() throws SQLException
    {
        try {
            boolean next = m_ResultSet.next();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_NEXT, Boolean.toString(next));
            return next;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean previous() throws SQLException
    {
        try {
            return m_ResultSet.previous();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void refreshRow() throws SQLException
    {
        try {
            System.out.println("ResultSetBase.refreshRow() 1");
            m_ResultSet.refreshRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean relative(int row) throws SQLException
    {
        try {
            boolean moved = m_ResultSet.relative(row);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_RELATIVE, Integer.toString(row), Boolean.toString(moved));
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        try {
            boolean deleted = m_ResultSet.rowDeleted();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
            return deleted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        try {
            boolean inserted = m_ResultSet.rowInserted();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
            return inserted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowUpdated() throws SQLException
    {
        try {
            boolean updated = m_ResultSet.rowUpdated();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_UPDATED, Boolean.toString(updated));
            return updated;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public void clearWarnings() throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            WarningsSupplier.clearWarnings(m_ResultSet, this);
    }


    @Override
    public Object getWarnings() throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            return WarningsSupplier.getWarnings(m_ResultSet, this);
         return Any.VOID;
    }


    // com.sun.star.sdbc.XResultSetUpdate:
    @Override
    public void insertRow() throws SQLException
    {
        try {
            m_ResultSet.insertRow();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_INSERT_ROW, Boolean.toString(m_ResultSet.rowInserted()));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateRow() throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_ROW);
            m_ResultSet.updateRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void deleteRow() throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_DELETE_ROW);
            m_ResultSet.deleteRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void cancelRowUpdates() throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
            // FIXME: *** LibreOffice Base call this method just after calling moveToInsertRow() ***
            // FIXME: Java documentation say: Throws: SQLException - if a database access error occurs;
            // FIXME: this method is called on a closed result set; the result set concurrency is CONCUR_READ_ONLY 
            // FIXME: or if this method is called when the cursor is on the insert row
            // FIXME: see: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#cancelRowUpdates--
            if (m_insert) {
                m_ResultSet.moveToCurrentRow();
                m_insert = false;
            }
            m_ResultSet.cancelRowUpdates();
        }
        catch (java.sql.SQLException e) {
            System.out.println("ResultSetBase.cancelRowUpdates() ERROR\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void moveToInsertRow() throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERTROW);
            m_ResultSet.moveToInsertRow();
            m_insert = true;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void moveToCurrentRow() throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_CURRENTROW);
            m_ResultSet.moveToCurrentRow();
            m_insert = false;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }


    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index) throws SQLException
    {
        try {
            java.sql.Array array = m_ResultSet.getArray(index);
            return (array != null) ? new Array(m_Connection, array) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public XInputStream getBinaryStream(int index) throws SQLException
    {
        XBlob blob = getBlob(index);
        return blob != null ? blob.getBinaryStream() : null;
    }

    @Override
    public XBlob getBlob(int index) throws SQLException
    {
        try {
            java.sql.Blob blob = m_ResultSet.getBlob(index);
            return (blob != null) ? new Blob(m_Connection, blob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getBoolean(int index) throws SQLException
    {
        try {
            return m_ResultSet.getBoolean(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index) throws SQLException
    {
        try {
            return m_ResultSet.getByte(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index) throws SQLException
    {
        try {
            return m_ResultSet.getBytes(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index) throws SQLException
    {
        XClob clob = getClob(index);
        return (clob != null) ? clob.getCharacterStream() : null;
    }

    @Override
    public XClob getClob(int index) throws SQLException
    {
        try {
            java.sql.Clob clob = m_ResultSet.getClob(index);
            return clob != null ? new Clob(m_Connection, clob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Date getDate(int index) throws SQLException
    {
        try {
            java.sql.Date value = m_ResultSet.getDate(index);
            return value != null ? UnoHelper.getUnoDate(value.toLocalDate()) : new Date();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public double getDouble(int index) throws SQLException
    {
        try {
            return m_ResultSet.getDouble(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index) throws SQLException
    {
        try {
            return m_ResultSet.getFloat(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index) throws SQLException
    {
        try {
            return m_ResultSet.getInt(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index) throws SQLException
    {
        try {
            return m_ResultSet.getLong(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map) throws SQLException
    {
        try {
            return DBTools.getObject(m_ResultSet.getObject(index), map);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getLoggedSQLException(this, m_logger, e);
        }
    }

    @Override
    public XRef getRef(int index) throws SQLException
    {
        try {
            java.sql.Ref ref = m_ResultSet.getRef(index);
            return ref != null ? new Ref(ref) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public short getShort(int index) throws SQLException
    {
        try {
            return m_ResultSet.getShort(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index) throws SQLException
    {
        try {
            String value = m_ResultSet.getString(index);
            if (value == null) {
                value = "";
            }
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_PARAMETER, value, "getString", Integer.toString(index));
            return value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index) throws SQLException
    {
        try {
            java.sql.Time value = m_ResultSet.getTime(index);
            return value != null ? UnoHelper.getUnoTime(value.toLocalTime()) : new Time();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public DateTime getTimestamp(int index) throws SQLException
    {
        try {
            java.sql.Timestamp value = m_ResultSet.getTimestamp(index);
            return value != null ? UnoHelper.getUnoDateTime(value.toLocalDateTime()) : new DateTime();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        try {
            return m_ResultSet.wasNull();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XRowUpdate:
    @Override
    public void updateNull(int index) throws SQLException
    {
        try {
            m_ResultSet.updateNull(index);
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
            m_ResultSet.updateBoolean(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateByte(int index, byte value) throws SQLException
    {
        try {
            m_ResultSet.updateByte(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateShort(int index, short value) throws SQLException
    {
        try {
            m_ResultSet.updateShort(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateInt(int index, int value) throws SQLException
    {
        try {
            m_ResultSet.updateInt(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateLong(int index, long value) throws SQLException
    {
        try {
            m_ResultSet.updateLong(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateFloat(int index, float value) throws SQLException
    {
        try {
            m_ResultSet.updateFloat(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateDouble(int index, double value) throws SQLException
    {
        try {
            m_ResultSet.updateDouble(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateString(int index, String value) throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_PARAMETER, "updateString", Integer.toString(index), value);
            m_ResultSet.updateString(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateBytes(int index, byte[] value) throws SQLException
    {
        try {
            m_ResultSet.updateBytes(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateDate(int index, Date value) throws SQLException
    {
        try {
            m_ResultSet.updateDate(index, java.sql.Date.valueOf(UnoHelper.getJavaLocalDate(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateTime(int index, Time value) throws SQLException
    {
        try {
            m_ResultSet.updateTime(index, java.sql.Time.valueOf(UnoHelper.getJavaLocalTime(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateTimestamp(int index, DateTime value) throws SQLException
    {
        try {
            m_ResultSet.updateTimestamp(index, java.sql.Timestamp.valueOf(UnoHelper.getJavaLocalDateTime(value)));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateBinaryStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            m_ResultSet.updateBinaryStream(index, input, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateCharacterStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            Reader reader = new java.io.InputStreamReader(input);
            m_ResultSet.updateCharacterStream(index, reader, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateObject(int index, Object value) throws SQLException
    {
        if (!DBTools.updateObject(m_ResultSet, index, value)) {
            String error = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNKNOWN_COLUMN_TYPE,
                                                                                     this.getClass().getName(),
                                                                                     "updateObject()",
                                                                                     Integer.toString(index));
            throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public void updateNumericObject(int index, Object value, int scale) throws SQLException
    {
        try {
            BigDecimal bigDecimal;
            if (AnyConverter.isDouble(value)) {
                bigDecimal = BigDecimal.valueOf(AnyConverter.toDouble(value));
            }
            else {
                bigDecimal = new BigDecimal(AnyConverter.toString(value));
            }
            m_ResultSet.updateObject(index, bigDecimal, scale);
        }
        catch (IllegalArgumentException | java.sql.SQLException e) {
            updateObject(index, value);
        }
    }


    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    @Override
    public XResultSetMetaData getMetaData() throws SQLException
    {
        System.out.println("sdbc.ResultSetBase.getMetaData() 1");
        try {
            java.sql.ResultSetMetaData metadata = m_ResultSet.getMetaData();
            return (metadata != null) ? new ResultSetMetaData(m_Connection, metadata) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
