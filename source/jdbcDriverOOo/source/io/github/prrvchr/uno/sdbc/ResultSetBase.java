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
package io.github.prrvchr.uno.sdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
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

import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertySet;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;

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
               XRowUpdate {

    protected ConnectionBase mConnection;
    protected java.sql.ResultSet mResult;
    protected StatementMain mStatement;
    // XXX: We need to know when we are on the insert row
    // XXX: see tdf#167434: SDBC method cancelRowUpdates caller on insert row
    protected boolean mOnInsert = false;
    protected final ConnectionLog mLogger;

    private final String mService;
    private final String[] mServices;
    private boolean mIsBookmarkable = false;
    private String mMethod;

    // The constructor method:

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet result)
        throws SQLException {
        this(service, services, connection, result, null, false);
    }

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet result,
                         String method)
        throws SQLException {
        this(service, services, connection, result, null, false, method);
    }

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet result,
                         StatementMain statement,
                         boolean bookmark)
        throws SQLException {
        this(service, services, connection, result, statement, bookmark, "");
    }

    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet resultset,
                         StatementMain statement,
                         boolean bookmark,
                         String method)
        throws SQLException {
        mService = service;
        mServices = services;
        mConnection = connection;
        mResult = resultset;
        mStatement = statement;
        mIsBookmarkable = bookmark;
        mMethod = method;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), LoggerObjectType.RESULTSET);
        if (!method.isEmpty()) {
            System.out.println("sdbc.ResultSetBase() 1 method: " + mMethod);
        }

    }

    protected void checkCursor() throws java.sql.SQLException { }

    protected abstract ConnectionBase getConnection();

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    protected String getMethod() {
        return mMethod;
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyID.CURSORNAME,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return getCursorName();
                },
                null));

        properties.put(PropertyID.FETCHDIRECTION,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getFetchDirection();
                },
                value -> {
                    setFetchDirection((int) value);
                }));

        properties.put(PropertyID.FETCHSIZE,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getFetchSize();
                },
                value -> {
                    setFetchSize((int) value);
                }));

        properties.put(PropertyID.RESULTSETCONCURRENCY,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getResultSetConcurrency();
                },
                null));

        properties.put(PropertyID.RESULTSETTYPE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return getResultSetType();
                },
                null));

        properties.put(PropertyID.ISBOOKMARKABLE,
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    System.out.println("ResultSetBase.IsBookmarkable() 1: " + mIsBookmarkable);
                    return mIsBookmarkable;
                },
                null));

        super.registerProperties(properties);
    }

    private String getCursorName()
        throws WrappedTargetException {
        try {
            String cursor = mResult.getCursorName();
            if (cursor == null) {
                cursor = "";
            }
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CURSORNAME, cursor);
            return cursor;
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    private int getFetchDirection()
        throws WrappedTargetException {
        try {
            int direction = mResult.getFetchDirection();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_FETCH_DIRECTION, Integer.toString(direction));
            return direction;
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    private synchronized void setFetchDirection(int direction)
        throws WrappedTargetException {
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_SET_FETCH_DIRECTION, Integer.toString(direction));
            mResult.setFetchDirection(direction);
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    protected int getFetchSize()
        throws WrappedTargetException {
        try {
            int size = mResult.getFetchSize();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_FETCH_SIZE, Integer.toString(size));
            return size;
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    protected synchronized void setFetchSize(int size)
        throws WrappedTargetException {
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_SET_FETCH_SIZE, Integer.toString(size));
            mResult.setFetchSize(size);
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    protected int getResultSetConcurrency()
        throws WrappedTargetException {
        try {
            System.out.println("ResultSetBase.getResultSetConcurrency() 1 type: " + mResult.getConcurrency());
            int concurrency = mResult.getConcurrency();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CONCURRENCY, Integer.toString(concurrency));
            return concurrency;
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    protected int getResultSetType()
        throws WrappedTargetException {
        try {
            System.out.println("ResultSetBase.getResultSetType() 1 type: " + mResult.getType());
            int type = mResult.getType();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_TYPE, type);
            return type;
        } catch (java.sql.SQLException e) {
            throw DBTools.getWrappedException(e, this);
        }
    }

    // com.sun.star.lang.XComponent
    @Override
    public synchronized void dispose() {
        try {
            System.out.println("sdbc.ResultSetBase.dispose() 1 method: " + mMethod);
            if (mResult != null) {
                try {
                    if (!mResult.isClosed()) {
                        mResult.close();
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                mResult = null;
                super.dispose();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // com.sun.star.sdbc.XCloseable
    @Override
    public synchronized void close() throws SQLException {
        checkDisposed();
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CLOSING);
        dispose();
    }

    // com.sun.star.sdbc.XColumnLocate:
    @Override
    public int findColumn(String name)
        throws SQLException {
        try {
            return mResult.findColumn(name);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // com.sun.star.sdbc.XResultSet:
    @Override
    public boolean absolute(int row)
        throws SQLException {
        try {
            resetOnInsertRow();
            boolean moved = mResult.absolute(row);
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ABSOLUTE, row, moved);
            return moved;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean relative(int row)
        throws SQLException {
        try {
            resetOnInsertRow();
            boolean moved = mResult.relative(row);
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_RELATIVE,
                           Integer.toString(row), Boolean.toString(moved));
            return moved;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void afterLast()
        throws SQLException {
        try {
            resetOnInsertRow();
            mResult.afterLast();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void beforeFirst()
        throws SQLException {
        try {
            resetOnInsertRow();
            mResult.beforeFirst();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean first()
        throws SQLException {
        try {
            resetOnInsertRow();
            return mResult.first();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean last()
        throws SQLException {
        try {
            resetOnInsertRow();
            return mResult.last();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isAfterLast()
        throws SQLException {
        try {
            return mResult.isAfterLast();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException {
        try {
            return mResult.isBeforeFirst();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isFirst()
        throws SQLException {
        try {
            return mResult.isFirst();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isLast()
        throws SQLException {
        try {
            return mResult.isLast();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean next()
        throws SQLException {
        try {
            checkCursor();
            boolean next = mResult.next();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_NEXT, Boolean.toString(next));
            return next;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean previous()
        throws SQLException {
        try {
            checkCursor();
            boolean previous = mResult.previous();
            return previous;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getRow()
        throws SQLException {
        try {
            return mResult.getRow();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void refreshRow()
        throws SQLException {
        try {
            System.out.println("ResultSetBase.refreshRow() *****************");
            mResult.refreshRow();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowDeleted()
        throws SQLException {
        try {
            boolean deleted = mResult.rowDeleted();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
            return deleted;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowInserted()
        throws SQLException {
        try {
            boolean inserted = mResult.rowInserted();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
            return inserted;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowUpdated()
        throws SQLException {
        try {
            boolean updated = mResult.rowUpdated();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_UPDATED, Boolean.toString(updated));
            return updated;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public com.sun.star.uno.XInterface getStatement()
        throws SQLException {
        return mStatement;
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mService);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServices);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServices, service);
    }

    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public void clearWarnings()
        throws SQLException {
        WarningsSupplier.clearWarnings(mResult, this);
    }

    @Override
    public Object getWarnings()
        throws SQLException {
        return WarningsSupplier.getWarnings(mResult, this);
    }

    // com.sun.star.sdbc.XResultSetUpdate:
    @Override
    public void insertRow()
        throws SQLException {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow cannot be called when moveToInsertRow has not been called !", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        try {
            insertRowInternal();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected void insertRowInternal()
        throws java.sql.SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_INSERT_ROW);
        mResult.insertRow();
    }

    @Override
    public void updateRow()
        throws SQLException {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: updateRow cannot be called when moveToInsertRow has been called!", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_ROW);
            mResult.updateRow();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void deleteRow()
        throws SQLException {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: deleteRow cannot be called when moveToInsertRow has been called!", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_DELETE_ROW);
            mResult.deleteRow();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx Line 111: xUp->cancelRowUpdates()
    // XXX: Fixed in LO 26.2 see: https://gerrit.libreoffice.org/c/core/+/187567
    @Override
    public void cancelRowUpdates()
        throws SQLException {
        // FIXME: *** LibreOffice Base call this method just after calling moveToInsertRow() ***
        // FIXME: Java documentation say: Throws: SQLException - if a database access error occurs;
        // FIXME: this method is called on a closed result set; the result set concurrency is CONCUR_READ_ONLY
        // FIXME: or if this method is called when the cursor is on the insert row
        // FIXME: see: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#cancelRowUpdates--
        // XXX: Fixed in LO 26.2 see: https://gerrit.libreoffice.org/c/core/+/187567
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
            if (isOnInsertRow()) {
                moveToCurrentRowInternal();
            } else {
                mResult.cancelRowUpdates();
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx Line 110: xUp->moveToInsertRow()
    // XXX: Fixed in LO 26.2 see: https://gerrit.libreoffice.org/c/core/+/187567
    @Override
    public void moveToInsertRow()
        throws SQLException {
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERT_ROW);
            mResult.moveToInsertRow();
            mOnInsert = true;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_CURRENT_ROW);
            moveToCurrentRowInternal();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected void moveToCurrentRowInternal()
        throws java.sql.SQLException {
        resetOnInsertRow();
        mResult.moveToCurrentRow();
    }

    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index)
        throws SQLException {
        XArray array = null;
        try {
            java.sql.Array value = mResult.getArray(index);
            if (value != null) {
                array = new Array(mConnection, value);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return array;
    }

    @Override
    public XInputStream getBinaryStream(int index)
        throws SQLException {
        XInputStream input = null;
        XBlob blob = getBlob(index);
        if (blob != null) {
            input = blob.getBinaryStream();
        }
        return input;
    }

    @Override
    public XBlob getBlob(int index)
        throws SQLException {
        XBlob blob = null;
        try {
            java.sql.Blob value = mResult.getBlob(index);
            if (value != null) {
                blob = new Blob(mConnection, value);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return blob;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException {
        try {
            return mResult.getBoolean(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index)
        throws SQLException {
        try {
            return mResult.getByte(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException {
        try {
            return mResult.getBytes(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index)
        throws SQLException {
        XInputStream input = null;
        XClob clob = getClob(index);
        if (clob != null) {
            input = clob.getCharacterStream();
        }
        return input;
    }

    @Override
    public XClob getClob(int index)
        throws SQLException {
        XClob clob = null;
        try {
            java.sql.Clob value = mResult.getClob(index);
            if (value != null) {
                clob = new Clob(mConnection, value);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return clob;
    }

    @Override
    public Date getDate(int index)
        throws SQLException {
        Date date = null;
        try {
            java.sql.Date value = mResult.getDate(index);
            if (value != null) {
                date = UnoHelper.getDate(value.toLocalDate());
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return date;
    }

    @Override
    public double getDouble(int index)
        throws SQLException {
        try {
            return mResult.getDouble(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index)
        throws SQLException {
        try {
            return mResult.getFloat(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        try {
            int value = mResult.getInt(index);
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        try {
            return mResult.getLong(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map)
        throws SQLException {
        try {
            return DBTools.getObject(mResult.getObject(index));
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XRef getRef(int index)
        throws SQLException {
        XRef ref = null;
        try {
            java.sql.Ref value = mResult.getRef(index);
            if (value != null) {
                ref = new Ref(value);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return ref;
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        try {
            return mResult.getShort(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index)
        throws SQLException {
        try {
            String value = mResult.getString(index);
            if (value == null) {
                value = "";
            }
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_PARAMETER, value, "getString",
                    Integer.toString(index));
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index)
        throws SQLException {
        Time time = null;
        try {
            java.sql.Time value = mResult.getTime(index);
            if (value != null) {
                time = UnoHelper.getTime(value.toLocalTime());
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return time;
    }

    @Override
    public DateTime getTimestamp(int index)
        throws SQLException {
        DateTime datetime = null;
        try {
            java.sql.Timestamp value = mResult.getTimestamp(index);
            if (value != null) {
                datetime = UnoHelper.getDateTime(value.toLocalDateTime());
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return datetime;
    }

    @Override
    public boolean wasNull()
        throws SQLException {
        try {
            return mResult.wasNull();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // com.sun.star.sdbc.XRowUpdate:
    @Override
    public void updateNull(int index)
        throws SQLException {
        try {
            mResult.updateNull(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        try {
            mResult.updateBoolean(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        try {
            mResult.updateByte(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        try {
            mResult.updateShort(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        try {
            mResult.updateInt(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        try {
            mResult.updateLong(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        try {
            mResult.updateFloat(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        try {
            mResult.updateDouble(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        try {
            mResult.updateString(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        try {
            mResult.updateBytes(index, value);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        try {
            mResult.updateDate(index, java.sql.Date.valueOf(UnoHelper.getJavaLocalDate(value)));
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        try {
            mResult.updateTime(index, java.sql.Time.valueOf(UnoHelper.getJavaLocalTime(value)));
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateTimestamp(int index, DateTime value)
        throws SQLException {
        try {
            mResult.updateTimestamp(index, java.sql.Timestamp.valueOf(UnoHelper.getJavaLocalDateTime(value)));
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateBinaryStream(int index, XInputStream value, int lenght)
        throws SQLException {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            mResult.updateBinaryStream(index, input, lenght);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateCharacterStream(int index, XInputStream value, int lenght)
        throws SQLException {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            Reader reader = new java.io.InputStreamReader(input);
            mResult.updateCharacterStream(index, reader, lenght);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        if (!DBTools.updateObject(mResult, index, value)) {
            String error = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNKNOWN_COLUMN_TYPE,
                    this.getClass().getName(), "updateObject()", Integer.toString(index));
            throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public void updateNumericObject(int index, Object value, int scale)
        throws SQLException {
        try {
            BigDecimal bigDecimal;
            if (AnyConverter.isDouble(value)) {
                bigDecimal = BigDecimal.valueOf(AnyConverter.toDouble(value));
            } else {
                bigDecimal = new BigDecimal(AnyConverter.toString(value));
            }
            mResult.updateObject(index, bigDecimal, scale);
        } catch (IllegalArgumentException | java.sql.SQLException e) {
            updateObject(index, value);
        }
    }

    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    @Override
    public XResultSetMetaData getMetaData()
        throws SQLException {
        XResultSetMetaData metadata = null;
        try {
            java.sql.ResultSetMetaData rsmd = mResult.getMetaData();
            if (rsmd != null) {
                metadata = new ResultSetMetaData(mConnection, rsmd);
            }
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return metadata;
    }

    protected boolean isOnInsertRow() {
        return mOnInsert;
    }

    protected void resetOnInsertRow() {
        if (mOnInsert) {
            mOnInsert = false;
        }

    }

}
