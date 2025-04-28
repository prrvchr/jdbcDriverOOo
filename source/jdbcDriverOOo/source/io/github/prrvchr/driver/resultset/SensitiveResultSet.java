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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import io.github.prrvchr.driver.rowset.RowHelper;


// XXX: This ResultSet is supposed to emulate a TYPE_SCROLL_SENSITIVE from a TYPE_SCROLL_INSENSITIVE
// XXX: If the ResultSet is not updatable (ie: CONCUR_UPDATABLE) then SQL DML commands will be used
// XXX: (ie: INSERT, DELETE, UPDATE...) instead of positioned updates.
public class SensitiveResultSet
    extends CachedResultSet {

    // XXX: The field that temporarily holds the last position of the cursor before it moved to the insert row
    protected int mCurrentRow = 0;
    protected int mCurrentCursor = 0;
    protected int mCurrentPosition = 0;

    // XXX: If the updated row are not visible then we keep a cache of all updated rows.
    private List<Integer> mUpdatedRows = new ArrayList<>();
    private Vector<Row> mUpdatedData = null;
    // XXX: If the inserted row are not visible then we keep a cache of all inserted rows.
    private List<Integer> mInsertedRows = new ArrayList<>();
    private Vector<Row> mInsertedData = null;


    private int mRowCount = 0;
    private int mInserted = -1;
    private int mDeleted = -1;

    private boolean mSQLDelete;
    private boolean mSQLInsert;
    private boolean mSQLUpdate;
    private boolean mIsDeleteVisible;
    private boolean mIsInsertVisible;
    private boolean mIsUpdateVisible;


    // The constructor method:
    public SensitiveResultSet(DriverProvider provider,
                              ResultSet result,
                              RowCatalog catalog,
                              String table,
                              ConnectionLog logger)
        throws SQLException {
        super(provider, result, catalog, table, logger);
        int size;
        if (mFetchSize < MINSIZE) {
            size = MINSIZE;
        } else {
            size = mFetchSize;
        }
        mUpdatedData = new Vector<Row>(size);
        mInsertedData = new Vector<Row>(size);
        boolean SQLMode = provider.useSQLMode(result);
        mSQLDelete = SQLMode || provider.useSQLDelete();
        mSQLInsert = SQLMode || provider.useSQLInsert();
        mSQLUpdate = SQLMode || provider.useSQLUpdate();
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_POSITIONED_UPDATE,
                       !mSQLDelete, !mSQLInsert, !mSQLUpdate);
        mIsDeleteVisible = !mSQLDelete && provider.isDeleteVisible(result);
        mIsInsertVisible = !mSQLInsert && provider.isInsertVisible(result);
        mIsUpdateVisible = !mSQLUpdate && provider.isUpdateVisible(result);
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_VISIBILITY,
                       !mIsDeleteVisible, !mIsInsertVisible, !mIsUpdateVisible);
        System.out.println("SensitiveResultSet() 1");
        System.out.println("SensitiveResultSet() Use SQL Delete: " + mSQLDelete);
        System.out.println("SensitiveResultSet() Use SQL Insert: " + mSQLInsert);
        System.out.println("SensitiveResultSet() Use SQL Update: " + mSQLUpdate);
        System.out.println("SensitiveResultSet() Delete are visible: " + mIsDeleteVisible);
        System.out.println("SensitiveResultSet() Insert are visible: " + mIsInsertVisible);
        System.out.println("SensitiveResultSet() Update are visible: " + mIsUpdateVisible);
        loadLastRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        if (mOnInsert) {
            if (mSQLInsert) {
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_MOVE_TO_CURRENT_ROW);
            } else {
                // XXX: We move to current row only if we don't use SQL mode for insert
                mResult.moveToCurrentRow();
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_CURRENT_ROW);
            }
            mInsertRow = null;
            mOnInsert = false;
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 110: xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow()
        throws SQLException {
        if (!mOnInsert) {
            // XXX: We create an cached insert row only if insert is not visible
            // XXX: and the inserted row must be put in cache.
            if (!mIsInsertVisible) {
                mInsertRow = new InsertRow(mColumnCount);
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_MOVE_TO_INSERT_ROW);
            }
            // XXX: We move to insert row only if we don't use SQL mode for insert
            if (!mSQLInsert) {
                mResult.moveToInsertRow();
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERT_ROW);
            }
            mOnInsert = true;
        }
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException {
        // XXX: It seems that LibreOffice uses cancelRowUpdates() to exit the insert row
        // XXX: after a call to moveToInsertRow() while JDBC forbids this.
        // XXX: See: libreoffice/dbaccess/source/core/api/RowSetCache.cxx
        // XXX: - Line 110: xUp->moveToInsertRow();
        // XXX: - Line 111: xUp->cancelRowUpdates();
        // XXX: Java doc: https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/ResultSet.html
        // XXX: #cancelRowUpdates() say: cancelRowUpdates throw SQL exception if this method is called when
        // XXX: the cursor is on the insert row...
        if (mOnInsert) {
            moveToCurrentRow();
        } else if (!mSQLUpdate) {
            // XXX: ResutSet concurrency must not be CONCUR_READ_ONLY
            mResult.cancelRowUpdates();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
        }
    }

    @Override
    public void insertRow()
        throws SQLException {
        if (!mOnInsert) {
            throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !");
        }
        BaseRow row;
        if (mInsertRow != null) {
            row = mInsertRow.clown();
        } else {
            row = null;
        }
        if (mSQLInsert) {
            getRowSetWriter().insertRow(row);
            for (String query : row.getQueries()) {
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_INSERT_ROW, query);
            }
        } else {
            // XXX: Base does not allow auto-increment columns to be entered,
            // XXX: some drivers force us to update these columns to null
            RowHelper.setDefaultColumnValues(mResult, mInsertedColumns);
            mInsertedColumns.clear();
            mResult.insertRow();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_INSERT_ROW);
        }
        moveToCurrentRow();
        // XXX: Positioned insert also must be put in the cache if inserted row are not visible
        if (row != null) {
            int index = getRowCount() + 1;
            mInsertedData.add(new Row(row));
            mInsertedRows.add(index);
        } else {
            // XXX: Since we are maintaining the row count of the ResultSet, we need to increment it.
            mRowCount ++;
        }
        // XXX: cursor and position must be set on the inserted row
        mCursor = getRowCount();
        mPosition = getMaxPosition();
        // XXX: We must be able to respond positively to the insert
        mInserted = mCursor;
    }

    @Override
    public boolean rowDeleted()
        throws SQLException {
        boolean deleted = isDeleted(mDeleted);
        mDeleted = -1;
        return deleted;
    }

    @Override
    public boolean rowInserted()
        throws SQLException {
        // XXX: We can assume the insertion is valid without any
        // XXX: movement in the ResultSet since the insertion.
        boolean inserted = mInserted == mCursor;
        mInserted = -1;
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException {
        // XXX: In addition to SQL updates we must be able to update inserted
        // XXX: row when insert are not visible (ie: inserted row are in cache)
        if (isInsertedRow() || mSQLUpdate) {
            Row row = getCachedRow();
            getRowSetWriter().updateRow(row);
            for (String query : row.getQueries()) {
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_UPDATE_ROW, query);
            }
        } else {
            mResult.updateRow();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_ROW);
        }
    }

    @Override
    public boolean rowUpdated()
        throws SQLException {
        System.out.println("SensitiveResultSet.rowUpdated() 1 **************************************************");
        boolean updated = mResult.rowUpdated();
        return updated;
    }

    @Override
    public void deleteRow()
        throws SQLException {
        mDeleted = mCursor;
        // XXX: Are we trying to delete a row that has been inserted or updated and which will be in cache?
        boolean cached = isCachedRow();
        // XXX: In addition to SQL deletes we must be able to delete inserted or updated row
        // XXX: when insert or update are not visible (ie: inserted or updated row are in cache)
        if (mSQLDelete || cached) {
            Row row;
            if (cached) {
                row = getCachedRow();
            } else {
                row = getResultRow();
            }
            getRowSetWriter().deleteRow(row);
            for (String query : row.getQueries()) {
                mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CACHED_RESULTSET_DELETE_ROW, query);
            }
        } else {
            mResult.deleteRow();
            // XXX: The row preceding the deleted row becomes the current row
            previous();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_DELETE_ROW);
        }
        if (cached) {
            deleteCachedRow(mDeleted);
        }
        // XXX: Managing bookmark requires us to manage a cursor taking into account the deleted lines.
        mDeletedRows.add(mDeleted);
    }


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException {
        boolean moved = super.next();
        if (moved && !isInCache()) {
            moved = moveResultSet();
        }
        return moved;
    }

    @Override
    public boolean previous()
        throws SQLException  {
        boolean moved = super.previous();
        if (moved && !isInCache()) {
            moved = moveResultSet();
        }
        return moved;
    }

    @Override
    public boolean first()
        throws SQLException {
        boolean first = super.first();
        if (first && !isInCache()) {
            first = moveResultSet();
        }
        return first;
    }

    @Override
    public boolean last()
        throws SQLException {
        boolean last = super.last();
        if (last && !isInCache()) {
            last = moveResultSet();
        }
        return last;
    }

    @Override
    public boolean relative(int row)
        throws SQLException {
        boolean moved = super.relative(row);
        if (moved && !isCachedRow()) {
            moved = moveResultSet();
        }
        return moved;
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
        String value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (String) row.getColumnObject(index);
            }
        } else {
            value = mResult.getString(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException {
        boolean value = false;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Boolean) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBoolean(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public byte getByte(int index)
        throws SQLException {
        byte value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Byte) row.getColumnObject(index);
            }
        } else {
            value = mResult.getByte(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        short value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Short) row.getColumnObject(index);
            }
        } else {
            value = mResult.getShort(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        int value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Integer) row.getColumnObject(index);
            }
        } else {
            value = mResult.getInt(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        long value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Long) row.getColumnObject(index);
            }
        } else {
            value = mResult.getLong(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public float getFloat(int index)
        throws SQLException {
        float value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Float) row.getColumnObject(index);
            }
        } else {
            value = mResult.getFloat(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public double getDouble(int index)
        throws SQLException {
        double value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Double) row.getColumnObject(index);
            }
        } else {
            value = mResult.getDouble(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException {
        BigDecimal value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (BigDecimal) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBigDecimal(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException {
        byte[] value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (byte[]) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBytes(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Date getDate(int index)
        throws SQLException {
        Date value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Date) row.getColumnObject(index);
            }
        } else {
            value = mResult.getDate(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Time getTime(int index)
        throws SQLException {
        Time value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Time) row.getColumnObject(index);
            }
        } else {
            value = mResult.getTime(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException {
        Timestamp value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Timestamp) row.getColumnObject(index);
            }
        } else {
            value = mResult.getTimestamp(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        } else {
            value = mResult.getAsciiStream(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int index)
        throws SQLException {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        } else {
            value = mResult.getUnicodeStream(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBinaryStream(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Object getObject(int index)
        throws SQLException {
        Object value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Object) row.getColumnObject(index);
            }
        } else {
            value = mResult.getObject(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException {
        Reader value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Reader) row.getColumnObject(index);
            }
        } else {
            value = mResult.getCharacterStream(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException {
        BigDecimal value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (BigDecimal) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBigDecimal(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException {
        Object value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Object) row.getColumnObject(index);
            }
        } else {
            value = mResult.getObject(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException {
        Ref value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Ref) row.getColumnObject(index);
            }
        } else {
            value = mResult.getRef(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException {
        Blob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Blob) row.getColumnObject(index);
            }
        } else {
            value = mResult.getBlob(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException {
        Clob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Clob) row.getColumnObject(index);
            }
        } else {
            value = mResult.getClob(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Array getArray(int index)
        throws SQLException {
        Array value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Array) row.getColumnObject(index);
            }
        } else {
            value = mResult.getArray(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException {
        Date value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Date) row.getColumnObject(index);
            }
        } else {
            value = mResult.getDate(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException {
        Time value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Time) row.getColumnObject(index);
            }
        } else {
            value = mResult.getTime(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException {
        Timestamp value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Timestamp) row.getColumnObject(index);
            }
        } else {
            value = mResult.getTimestamp(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public URL getURL(int index)
        throws SQLException {
        URL value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (URL) row.getColumnObject(index);
            }
        } else {
            value = mResult.getURL(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException {
        RowId value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (RowId) row.getColumnObject(index);
            }
        } else {
            value = mResult.getRowId(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException {
        NClob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (NClob) row.getColumnObject(index);
            }
        } else {
            value = mResult.getNClob(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException {
        SQLXML value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (SQLXML) row.getColumnObject(index);
            }
        } else {
            value = mResult.getSQLXML(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public String getNString(int index)
        throws SQLException {
        String value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (String) row.getColumnObject(index);
            }
        } else {
            value = mResult.getNString(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException {
        Reader value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (Reader) row.getColumnObject(index);
            }
        } else {
            value = mResult.getNCharacterStream(index);
            mWasNull = mResult.wasNull();
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int index, Class<T> type)
        throws SQLException {
        T value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            mWasNull = row.isColumnNull(index);
            if (!mWasNull) {
                value = (T) row.getColumnObject(index);
            }
        } else {
            value = mResult.getObject(index, type);
            mWasNull = mResult.wasNull();
        }
        return value;
    }


    // XXX: java.sql.ResultSet updater
    @Override
    public void updateNull(int index)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, null);
        }
        if (updateResultSet()) {
            super.updateNull(index);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBoolean(index, value);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateByte(index, value);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateShort(index, value);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateInt(index, value);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateLong(index, value);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateFloat(index, value);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        // XXX: Base using updateDouble() for most numeric SQL types,
        // XXX: it is necessary to convert to the native column type
        if (updateCache()) {
            setColumnDouble(index, value);
        }
        if (updateResultSet()) {
            super.updateDouble(index, value);
        }
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBigDecimal(index, value);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateString(index, value);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBytes(index, value);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateDate(index, value);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateTime(index, value);
        }
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateTimestamp(index, value);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value, length);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value, length);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateObject(index, value, length);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateObject(index, value);
        }
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateRef(index, value);
        }
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value);
        }
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value);
        }
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateArray(index, value);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException {
        if (updateCache()) {
            updateAsciiStream(index, value, length);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value, length);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException  {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value, length);
        }
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value, length);
        }
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value, length);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNCharacterStream(index, value);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value);
        }
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value);
        }
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value);
        }
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateSQLXML(index, value);
        }
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNString(index, value);
        }
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value);
        }
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateRowId(index, value);
        }
    }


    // XXX: Private methods
    @Override
    protected boolean isEmpty() {
        int count = mRowCount + mInsertedRows.size() - mDeletedRows.size();
        return count == 0;
    }

    @Override
    protected int getRowCount() {
        int count = mRowCount;
        // XXX: If we want to be able to make the insertions visible then we must add them.
        if (!mIsInsertVisible) {
            // XXX: In order to ensure the uniqueness of bookmarks we must take
            // XXX: into account the cached inserts that have been deleted.
            count += mInsertedRows.size() + mDeletedInsert;
        }
        return count;
    }

    @Override
    protected boolean moveResultSet(Integer position, int row)
        throws SQLException {
        boolean moved = false;
        if (!isAfterLast() && !isBeforeFirst()) {
            // XXX: For loading the first row Base use absolute(1) will the current row is beforeFirst.
            if (isCachedRow()) {
                moved = true;
            } else {
                moved =  moveResultSet();
            }
        }
        return moved;
    }

    @Override
    protected BaseRow getCurrentRow()
        throws SQLException {
        BaseRow row;
        if (mInsertRow != null) {
            row = mInsertRow;
        } else {
            checkCursor();
            if (!isInsertedRow() && !isUpdatedRow()) {
                createUpdatedRow();
            }
            row = getCachedRow();
        }
        return row;
    }

    @Override
    protected void incrementCursor()
        throws SQLException {
        ++mCursor;
    }


    // XXX: Private methods
    private boolean isInCache() {
        return isUpdated() || isInserted() || isDeleted() || mInsertRow != null;
    }

    private boolean updateCache() {
        // XXX: We update Cached if:
        // XXX: - We do an insert and insert are not visible
        // XXX: - We do an update and update are not visible
        // XXX: - The current row is an inserted row in cache
        return mInsertRow != null || isSQLUpdate() || isInsertedRow();
    }

    private boolean updateResultSet() {
        // XXX: We use positioned ResultSet update if:
        // XXX: - We do an insert and don't use SQL mode for insert
        // XXX: - The current row is not in any cache (inserted and updated row)
        return isPositionedInsert() || !(isCachedRow() || isSQLInsert());
    }

    private boolean isSQLUpdate() {
        return !mOnInsert && !mIsUpdateVisible;
    }

    private boolean isCachedRow() {
        return isUpdatedRow(mCursor) || isInsertedRow(mCursor);
    }

    private boolean isSQLInsert() {
        return mOnInsert && mSQLInsert;
    }

    private boolean isPositionedInsert() {
        return mOnInsert && !mSQLInsert;
    }

    private boolean isUpdatedRow() {
        return isUpdatedRow(mCursor);
    }

    private boolean isUpdatedRow(Integer row) {
        return mUpdatedRows.contains(row);
    }

    private boolean isInsertedRow() {
        return isInsertedRow(mCursor);
    }

    private boolean isInsertedRow(int row) {
        return mInsertedRows.contains(row);
    }

    private boolean isUpdated() {
        return isUpdated(mCursor);
    }

    private boolean isInserted() {
        return isInserted(mCursor);
    }

    private boolean isUpdated(int row) {
        return !mIsUpdateVisible && isUpdatedRow(row);
    }

    private boolean isInserted(int row) {
        return !mIsInsertVisible && isInsertedRow(row);
    }

    private void createUpdatedRow()
        throws SQLException {
        mUpdatedRows.add(mCursor);
        mUpdatedData.add(getResultRow());
    }

    private void deleteCachedRow(Integer position)
        throws SQLException {
        if (mUpdatedRows.contains(position)) {
            mUpdatedData.remove(mUpdatedRows.indexOf(position));
            mUpdatedRows.remove(position);
        } else if (mInsertedRows.contains(position)) {
            mInsertedData.remove(mInsertedRows.indexOf(position));
            mInsertedRows.remove(position);
            mDeletedInsert ++;
        }
    }

    private Row getCachedRow() {
        Row row = null;
        if (mUpdatedRows.contains(mCursor)) {
            row = mUpdatedData.get(mUpdatedRows.indexOf(mCursor));
        } else if (mInsertedRows.contains(mCursor)) {
            row = mInsertedData.get(mInsertedRows.indexOf(mCursor));
        }
        return row;
    }

    private boolean moveResultSet()
        throws SQLException {
        int position;
        if (mIsDeleteVisible) {
            position = mPosition;
        } else {
            position = mCursor;
        }
        boolean moved = mResult.absolute(position);
        System.out.println("SensitiveResultSet.moveResultSet() 1 Position: " + position + " - moved: " + moved);
        return moved;
    }

    // Private methods for managing last row cache
    private void loadLastRow()
        throws SQLException {
        if (mResult.last()) {
            mRowCount = mResult.getRow();
        }
        mResult.beforeFirst();
    }

}
