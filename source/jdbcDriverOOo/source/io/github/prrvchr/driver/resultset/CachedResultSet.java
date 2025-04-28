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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.rowset.BaseRow;
import io.github.prrvchr.driver.rowset.InsertRow;
import io.github.prrvchr.driver.rowset.Row;
import io.github.prrvchr.driver.rowset.RowCatalog;
import io.github.prrvchr.driver.rowset.RowSetWriter;


public abstract class CachedResultSet
    extends ResultSet {

    protected static final int MINSIZE = 10;
    protected int mColumnCount = 0;
    protected int mFetchSize;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor that supports deleted lines
    protected int mCursor = 0;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor which hides deleted lines
    protected int mPosition = 0;
    // XXX: If we want to be able to manage bookmarks then we need to keep a cache of all deleted rows.
    protected List<Integer> mDeletedRows = new ArrayList<>();
    protected int mDeletedInsert = 0;

    // XXX: If ResultSet is not updatable then we need to emulate the insert row.
    protected InsertRow mInsertRow = null;

    // XXX: We need to know when we are on the insert row
    protected boolean mOnInsert = false;
    // XXX: We need to keep the index references of the columns already assigned for insertion
    protected BitSet mInsertedColumns;

    // XXX: Is the last ResultSet value null
    protected boolean mWasNull = false;
    protected final ConnectionLog mLogger;

    private DriverProvider mProvider;
    // XXX: If ResultSet cannot be updated, we use a RowSetWriter
    // XXX: which allows us to send the correct SQL queries
    private RowSetWriter mRowSetWriter = null;
    // XXX: If we use RowSetWriter we need the RowCatalog
    private RowCatalog mCatalog = null;
    private String mTable;

    // The constructor method:
    public CachedResultSet(DriverProvider provider,
                           java.sql.ResultSet result,
                           RowCatalog catalog,
                           String table,
                           ConnectionLog logger)
        throws SQLException {
        super(result);
        mProvider = provider;
        mCatalog = catalog;
        mTable = table;
        mFetchSize = result.getFetchSize();
        mColumnCount = result.getMetaData().getColumnCount();
        mInsertedColumns = new BitSet(mColumnCount);
        mLogger = logger;
    }

    @Override
    public abstract void moveToCurrentRow() throws SQLException;

    @Override
    public abstract void moveToInsertRow() throws SQLException;

    @Override
    public abstract void cancelRowUpdates() throws SQLException;

    @Override
    public abstract void insertRow() throws SQLException;

    @Override
    public abstract boolean rowDeleted() throws SQLException;


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException {
         // XXX: Make sure things look sane. The cursor must be positioned in the ResultSet
         // XXX: or before first row (0) or after last row (RowCount + 1)
        if (mCursor < 0 || mCursor > getRowCount() + 1) {
            throw new SQLException();
        }
        return internalNext();
    }

    @Override
    public boolean previous()
        throws SQLException  {
        // XXX: Make sure things look sane. The cursor must be positioned in the ResultSet
        // XXX: or before first row (0) or after last row (RowCount + 1)
        if (mCursor < 0 || mCursor > getRowCount() + 1) {
            throw new SQLException();
        }
        return internalPrevious();
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException {
        boolean isbefore = false;
        if (!isEmpty()) {
            isbefore = mCursor == 0;
        }
        return isbefore;
    }

    @Override
    public boolean isFirst()
        throws SQLException {
        // XXX: This becomes nasty because of deletes.
        boolean first = false;
        int cursor = mCursor;
        int position = mPosition;
        internalFirst();
        if (mCursor == cursor) {
            first = true;
        } else {
            mCursor = cursor;
            mPosition = position;
        }
        return first;
    }

    @Override
    public boolean isLast()
        throws SQLException {
        // XXX: This becomes nasty because of deletes.
        boolean last = false;
        int cursor = mCursor;
        int position = mPosition;
        internalLast();
        if (mCursor == cursor) {
            last = true;
        } else {
            mCursor = cursor;
            mPosition = position;
        }
        return last;
    }

    @Override
    public boolean isAfterLast()
        throws SQLException {
        boolean isafter = false;
        if (!isEmpty()) {
            isafter = mCursor == getRowCount() + 1;
        }
        return isafter;
    }

    @Override
    public void beforeFirst()
        throws SQLException {
        mCursor = 0;
        mPosition = 0;
    }

    @Override
    public void afterLast()
        throws SQLException {
        if (!isEmpty()) {
            mCursor = getRowCount() + 1;
            mPosition = getMaxPosition() + 1;
        }
    }

    @Override
    public boolean first()
        throws SQLException {
        boolean first = false;
        if (! isEmpty()) {
            first = internalFirst();
        }
        return first;
    }

    @Override
    public boolean last()
        throws SQLException {
        boolean last = false;
        if (!isEmpty()) {
            last = internalLast();
        }
        return last;
    }

    @Override
    public boolean relative(int row)
        throws SQLException {
        if (isEmpty() || isBeforeFirst() || isAfterLast()) {
            throw new SQLException();
        }
        boolean moved = true;
        if (row != 0) {
            if (row > 0) {
                // XXX: We are moving forward
                moveForward(row);
            } else {
                // XXX: We are moving backward
                moveBackward(row);
            }
            if (isAfterLast() || isBeforeFirst()) {
                moved = false;
            }
        }
        return moved;
    }

    private void moveForward(int row) throws SQLException {
        // XXX: We are moving forward
        if (mCursor + row > getRowCount()) {
            // XXX: Fell off the end
            afterLast();
        } else {
            for (int i = 0; i < row; i++) {
                if (!internalNext()) {
                    break;
                }
            }
        }
    }

    private void moveBackward(int row) throws SQLException {
        // XXX: We are moving backward
        if (mCursor + row < 0) {
            // XXX: Fell off the front
            beforeFirst();
        } else {
            for (int i = row; i < 0; i++) {
                if (!internalPrevious()) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean absolute(int row)
        throws SQLException {
        boolean absolute = false;
        if (checkAbsolute(row)) {
            // XXX: Now move towards the absolute row that we're looking for
            while (mPosition != row) {
                if (mPosition < row) {
                    if (!internalNext()) {
                        break;
                    }
                } else {
                    if (!internalPrevious()) {
                        break;
                    }
                }
            }
            absolute = moveResultSet(mPosition, row);
        }
        return absolute;
    }

    @Override
    public int getRow()
        throws SQLException {
        int row = 0;
        if (!isEmpty() && mCursor > 0 && mCursor <= getRowCount()) {
            row = mPosition; 
        }
        return row;
    }

    public int getBookmark() {
        return mCursor;
    }

    public boolean moveToBookmark(int row)
        throws SQLException {
        boolean moved = false;
        System.out.println("CachedResultSet.moveToBookmark() 1 Cursor: " + mCursor + " - row: " + row);
        if (checkAbsolute(row)) {
            System.out.println("CachedResultSet.moveToBookmark() 2");
            // XXX: Now move towards the absolute row that we're looking for
            while (mCursor != row) {
                if (mCursor < row) {
                    if (!internalNext()) {
                        break;
                    }
                } else {
                    if (!internalPrevious()) {
                        break;
                    }
                }
            }
            moved = moveResultSet(mCursor, row);
        }
        System.out.println("CachedResultSet.moveToBookmark() 3 Cursor: " + mCursor + " - moved: " + moved);
        return moved;
    }


    // XXX: java.sql.ResultSet updater by index
    @Override
    public void updateNull(int index)
        throws SQLException {
        mResult.updateNull(index);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        mResult.updateBoolean(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        mResult.updateByte(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        mResult.updateShort(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        mResult.updateInt(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        mResult.updateLong(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        mResult.updateFloat(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        mResult.updateDouble(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException {
        mResult.updateBigDecimal(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        System.out.println("CachedResultSet.updateString() 1 Value: " + value);
        mResult.updateString(index, value);
        System.out.println("CachedResultSet.updateString() 2 Value: " + mResult.getString(index));
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        mResult.updateBytes(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        mResult.updateDate(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        mResult.updateTime(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException {
        mResult.updateTimestamp(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException {
        mResult.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException {
        mResult.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException {
        mResult.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException {
        mResult.updateObject(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        mResult.updateObject(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException {
        mResult.updateRef(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException {
        mResult.updateBlob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException {
        mResult.updateClob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException {
        mResult.updateArray(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mResult.updateNCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException {
        mResult.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException {
        mResult.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mResult.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException  {
        mResult.updateBlob(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException {
        mResult.updateClob(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException {
        mResult.updateNClob(index, value, length);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException {
        mResult.updateNCharacterStream(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException {
        mResult.updateAsciiStream(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException {
        mResult.updateBinaryStream(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException {
        mResult.updateCharacterStream(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException {
        mResult.updateBlob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException {
        mResult.updateClob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException {
        mResult.updateNClob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException {
        mResult.updateSQLXML(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException {
        mResult.updateNString(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException {
        mResult.updateNClob(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException {
        mResult.updateRowId(index, value);
        if (isOnInsertRow()) {
            mInsertedColumns.set(index - 1);
        }
    }

    // XXX: Protected methods
    protected void checkIndex(int index)
        throws SQLException {
        if (index < 1 || index > mColumnCount) {
            throw new SQLException("ERROR Index is out of range");
        }
    }

    protected boolean isOnInsertRow() {
        return mOnInsert;
    }

    protected void setInsertMode(boolean mode) {
        mOnInsert = mode;
    }

    protected Row getResultRow()
        throws SQLException {
        Row row = new Row(mColumnCount);
        for (int i = 1; i <= mColumnCount; i++) {
            row.initColumnObject(mResult, i);
            System.out.println("CachedResultSet.getResultRow() 1 Value: " + mResult.getObject(i).toString());
        }
        return row;
    }

    protected InsertRow getResultInsertRow()
        throws SQLException {
        InsertRow row = new InsertRow(mColumnCount);
        for (int i = 1; i <= mColumnCount; i++) {
            row.initColumnObject(mResult, i);
        }
        return row;
    }

    protected boolean isDeleted() {
        return isDeleted(mCursor);
    }

    protected RowSetWriter getRowSetWriter()
        throws SQLException {
        if (mRowSetWriter == null) {
            mRowSetWriter = new RowSetWriter(mProvider, mCatalog, mResult, mTable);
        }
        return mRowSetWriter;
    }

    protected int getMaxPosition() {
        // FIXME: m_Position skips any row in the ResultSet that was deleted
        return getRowCount() - mDeletedRows.size();
    }

    protected void setColumnObject(int index, Object value)
        throws SQLException {
        getCurrentRow().setColumnObject(index, value);
    }

    protected void setColumnDouble(int index, Double value)
        throws SQLException {
        int type = mResult.getMetaData().getColumnType(index);
        getCurrentRow().setColumnDouble(index, value, type);
    }

    protected void checkCursor()
        throws SQLException {
        if (mCursor <= 0 || mCursor > getRowCount()) {
            throw new SQLException("ERROR Row is out of range");
        }
    }

    protected boolean isDeleted(int row) {
        return mDeletedRows.contains(row);
    }

    protected abstract boolean isEmpty();

    protected abstract int getRowCount();

    protected abstract BaseRow getCurrentRow() throws SQLException;

    protected abstract void incrementCursor() throws SQLException;

    protected abstract boolean moveResultSet(Integer position, int row) throws SQLException;


    private boolean internalFirst()
        throws SQLException {
        boolean first = false;
        if (!isEmpty()) {
            mCursor = 0;
            mPosition = 0;
            first = internalNext();
        }
        return first;
    }

    private boolean internalLast()
        throws SQLException {
        boolean last = false;
        if (!isEmpty()) {
            mCursor = getRowCount();
            mPosition = getMaxPosition();
            last = internalPrevious();
        }
        return last;
    }

    protected boolean internalNext()
        throws SQLException {
        boolean moved = false;
        int count = getRowCount();
        do {
            if (mCursor < count) {
                incrementCursor();
                moved = true;
            } else if (mCursor == count) {
                // XXX: Increment to after last
                ++mCursor;
                moved = false;
                break;
            }
        } while (isDeleted());
        // XXX: Each call to internalNext may increment cursor m_Cursor multiple
        // XXX: times however, the m_Position only increments once per call.
        if (moved) {
            mPosition++;
        } else {
            mPosition = getMaxPosition() + 1;
        }
        return moved;
    }

    private boolean internalPrevious()
        throws SQLException {
        boolean moved = false;
        do {
            if (mCursor > 1) {
                --mCursor;
                moved = true;
            } else if (mCursor == 1) {
                // XXX: Decrement to before first
                --mCursor;
                moved = false;
                break;
            }
        } while (isDeleted());
        // XXX: Each call to internalPrevious may move the cursor m_Cursor over
        // XXX: multiple rows, the absolute position m_Position moves one row
        if (moved) {
            --mPosition;
        } else {
            mPosition = 0;
        }
        return moved;
    }

    private boolean checkAbsolute(int row)
        throws SQLException {
        boolean checked = true;
        if (row == 0) {
            throw new SQLException();
        }
        int count = getRowCount();
        // XXX: We are moving forward
        if (row > 0) {
            if (row > count) {
                // XXX: Fell off the end
                afterLast();
                checked = false;
            } else if (mCursor <= 0) {
                internalFirst();
            } else if (isDeleted()) {
                // XXX: In addition to a position outside the ResultSet,
                // XXX: we need to handle the case if the row has been deleted
                internalPrevious();
            }
        } else {
            // XXX: We are moving backward
            if (count + row < 0) {
                // XXX: Fell off the front
                beforeFirst();
                checked = false;
            } else if (mCursor > count) {
                internalLast();
            } else if (isDeleted()) {
                // XXX: In addition to a position outside the ResultSet,
                // XXX: we need to handle the case if the row has been deleted
                internalNext();
            }
        }
        return checked;
    }

}
