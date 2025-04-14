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
package io.github.prrvchr.driver.resultset;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import com.sun.star.logging.LogLevel;

import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.rowset.BaseRow;
import io.github.prrvchr.driver.rowset.InsertRow;
import io.github.prrvchr.driver.rowset.Row;
import io.github.prrvchr.driver.rowset.RowCatalog;


//XXX: This ResultSet is supposed to emulate a TYPE_SCROLL_SENSITIVE from a TYPE_FORWARD_ONLY
//XXX: SQL DML commands will be used (ie: INSERT, DELETE, UPDATE...) instead of positioned updates.
public class ScrollableResultSet
    extends CachedResultSet {
    // XXX: We keep a cache of all the ResultSet rows
    private Vector<Row> mRowData = null;
    private boolean mRowCountFinal = false;
    private int mInsertedRow = 0;

    // The constructor method:
    public ScrollableResultSet(DriverProvider provider,
                               ResultSet result,
                               RowCatalog catalog,
                               String table,
                               ConnectionLog logger)
        throws SQLException {
        super(provider, result, catalog, table, logger);
        loadNextRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_MOVE_TO_CURRENT_ROW);
        setInsertMode(false);
    }

    @Override
    public void moveToInsertRow()
        throws SQLException {
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_MOVE_TO_INSERT_ROW);
        setInsertMode(true);
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException {
        if (isOnInsertRow()) {
            setInsertMode(false);
        }
    }

    @Override
    public void insertRow()
        throws SQLException {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !");
        }
        // XXX: The result set cannot be updated, the insert
        // XXX: will be done by a SQL command from the cached insert row.
        InsertRow insert = mInsertRow.clown();
        getRowSetWriter().insertRow(insert);
        for (String query : insert.getQueries()) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_INSERT_ROW, query);
        }
        getRowData().add(new Row(insert));
        // XXX: cursor and absolute position must be set on the inserted row
        mCursor = getRowCount();
        mPosition = getMaxPosition();
        setInsertMode(false);
        // XXX: We must be able to respond positively to the insert
        mInsertedRow = mCursor;
    }

    @Override
    public boolean rowDeleted()
        throws SQLException {
        boolean deleted = false;
        // XXX: We can assume the delete is valid without any
        // XXX: movement in the ResultSet since the delete.
        deleted = isDeleted(mCursor);
        return deleted;
    }

    @Override
    public boolean rowInserted()
        throws SQLException {
        boolean inserted = mInsertedRow == mCursor;
        mInsertedRow = 0;
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: updateRow() cannot be called when moveToInsertRow() has been called!");
        }
        // XXX: The result set cannot be updated, the update
        // XXX: will be done by a SQL command from the cached current row.
        Row row = (Row) getCurrentRow();
        getRowSetWriter().updateRow(row);
        for (String query : row.getQueries()) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_UPDATE_ROW, query);
        }
    }

    @Override
    public void deleteRow()
        throws SQLException {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: deleteRow() cannot be called when moveToInsertRow() has been called!");
        }
        // XXX: The result set cannot be updated, the delete
        // XXX: will be done by a SQL command from the current row.
        int position = mCursor;
        Row row = (Row) getCurrentRow();
        getRowSetWriter().deleteRow(row);
        for (String query : row.getQueries()) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_DELETE_ROW, query);
        }
        mDeletedRows.add(position);
    }

    // XXX: java.sql.ResultSet getter
    @Override
    public boolean wasNull()
        throws SQLException {
        return mWasNull;
    }

    @Override
    public String getString(int index)
        throws SQLException {
        checkIndex(index);
        String value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (String) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException {
        checkIndex(index);
        boolean value = false;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Boolean) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public byte getByte(int index)
        throws SQLException {
        checkIndex(index);
        byte value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Byte) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        checkIndex(index);
        short value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Short) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        checkIndex(index);
        int value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Integer) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        checkIndex(index);
        long value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Long) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public float getFloat(int index)
        throws SQLException {
        checkIndex(index);
        float value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Float) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public double getDouble(int index)
        throws SQLException {
        checkIndex(index);
        double value = 0;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Double) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException {
        checkIndex(index);
        BigDecimal value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (BigDecimal) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException {
        checkIndex(index);
        byte[] value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (byte[]) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Date getDate(int index)
        throws SQLException {
        checkIndex(index);
        Date value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Date) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Time getTime(int index)
        throws SQLException {
        checkIndex(index);
        Time value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Time) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException {
        checkIndex(index);
        Timestamp value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Timestamp) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException {
        checkIndex(index);
        InputStream value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getUnicodeStream(int index)
        throws SQLException {
        checkIndex(index);
        InputStream value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException {
        checkIndex(index);
        InputStream value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Object getObject(int index)
        throws SQLException {
        checkIndex(index);
        Object value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Object) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException {
        checkIndex(index);
        Reader value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Reader) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException {
        checkIndex(index);
        BigDecimal value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (BigDecimal) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException {
        checkIndex(index);
        Object value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Object) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException {
        checkIndex(index);
        Ref value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Ref) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException {
        checkIndex(index);
        Blob value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Blob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException {
        checkIndex(index);
        Clob value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Clob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Array getArray(int index)
        throws SQLException {
        checkIndex(index);
        Array value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Array) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException {
        checkIndex(index);
        Date value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Date) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException {
        checkIndex(index);
        Time value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Time) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException {
        checkIndex(index);
        Timestamp value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Timestamp) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public URL getURL(int index)
        throws SQLException {
        checkIndex(index);
        URL value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (URL) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException {
        checkIndex(index);
        RowId value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (RowId) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException {
        checkIndex(index);
        NClob value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (NClob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException {
        checkIndex(index);
        SQLXML value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (SQLXML) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public String getNString(int index)
        throws SQLException {
        checkIndex(index);
        String value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (String) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException {
        checkIndex(index);
        Reader value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (Reader) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int index, Class<T> type)
        throws SQLException {
        checkIndex(index);
        T value = null;
        BaseRow row = getCurrentRow();
        mWasNull = row.isColumnNull(index);
        if (!mWasNull) {
            value = (T) row.getColumnObject(index);
        }
        return value;
    }


    // XXX: java.sql.ResultSet updater
    @Override
    public void updateNull(int index)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, null);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        checkIndex(index);
        // XXX: Base using updateDouble() for most numeric SQL types,
        // XXX: it is necessary to convert to the native column type
        setColumnDouble(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException  {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException {
        checkIndex(index);
        setColumnObject(index, value);
    }


    // XXX: Protected overloaded methods
    @Override
    protected void setInsertMode(boolean mode) {
        super.setInsertMode(mode);
        if (mode) {
            mInsertRow = new InsertRow(mColumnCount);
        } else {
            mInsertRow = null;
        }
    }

    @Override
    protected boolean isEmpty() {
        return getMaxPosition() == 0;
    }

    @Override
    protected int getRowCount() {
        int count = 0;
        if (mRowData != null) {
            count = mRowData.size();
        }
        return count;
    }

    @Override
    protected void incrementCursor()
        throws SQLException {
        moveResultSet();
        ++mCursor;
    }

    @Override
    protected boolean moveResultSet(Integer position, int row)
        throws SQLException {
        boolean moved = false;
        if (!isAfterLast() && !isBeforeFirst()) {
            moveResultSet();
            moved = true;
        }
        
        return moved;
    }

    @Override
    protected BaseRow getCurrentRow()
        throws SQLException {
        BaseRow row;
        if (isOnInsertRow()) {
            row = mInsertRow;
        } else {
            checkCursor();
            row = getRowData().get(mCursor - 1);
        }
        return row;
    }

    // XXX: Private methods
    private void moveResultSet()
        throws SQLException {
        if (!mRowCountFinal) {
            loadNextRow();
        }
    }

    private Vector<Row> getRowData() {
        if (mRowData == null) {
            int size;
            if (mFetchSize < MINSIZE) {
                size = MINSIZE;
            } else {
                size = mFetchSize;
            }
            mRowData = new Vector<Row>(size);
        }
        return mRowData;
    }

    private void loadNextRow()
        throws SQLException {
        if (mResult.next()) {
            getRowData().add(getResultRow());
        } else {
            mRowCountFinal = true;
        }
    }

}
