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
package io.github.prrvchr.jdbcdriver.resultset;

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

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.BaseRow;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;
import io.github.prrvchr.jdbcdriver.rowset.RowSetWriter;


public abstract class CachedResultSet
    extends ResultSet
{

    private DriverProvider m_Provider;
    // XXX: If ResultSet cannot be updated, we use a RowSetWriter
    // XXX: which allows us to send the correct SQL queries
    private RowSetWriter m_RowSetWriter = null;
    // XXX: If we use RowSetWriter we need the RowCatalog
    private RowCatalog m_Catalog = null;
    private String m_Table;

    protected int m_MinSize = 10;
    protected int m_ColumnCount = 0;
    protected int m_FetchSize;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor that supports deleted lines
    protected int m_Cursor = 0;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor which hides deleted lines
    protected int m_Position = 0;
    // XXX: If we want to be able to manage bookmarks then we need to keep a cache of all deleted rows.
    protected List<Integer> m_DeletedRows = new ArrayList<>();
    protected int m_DeletedInsert = 0;

    // XXX: If ResultSet is not updatable then we need to emulate the insert row.
    protected InsertRow m_InsertRow = null;

    // XXX: We need to know when we are on the insert row
    protected boolean m_OnInsert = false;
    // XXX: We need to keep the index references of the columns already assigned for insertion
    protected BitSet m_InsertedColumns;

    // XXX: Is the last ResultSet value null
    protected boolean m_WasNull = false;
    protected final ConnectionLog m_logger;


    // The constructor method:
    public CachedResultSet(DriverProvider provider,
                           java.sql.ResultSet result,
                           RowCatalog catalog,
                           String table,
                           ConnectionLog logger)
        throws SQLException
    {
        super(result);
        m_Provider = provider;
        m_Catalog = catalog;
        m_Table = table;
        m_FetchSize = result.getFetchSize();
        m_ColumnCount = result.getMetaData().getColumnCount();
        m_InsertedColumns = new BitSet(m_ColumnCount);
        m_logger = logger;
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
        throws SQLException
    {
         // XXX: Make sure things look sane. The cursor must be positioned in the ResultSet
         // XXX: or before first row (0) or after last row (RowCount + 1)
        if (m_Cursor < 0 || m_Cursor > getRowCount() + 1) {
            throw new SQLException();
        }
        return internalNext();
    }

    @Override
    public boolean previous()
        throws SQLException 
    {
        // XXX: Make sure things look sane. The cursor must be positioned in the ResultSet
        // XXX: or before first row (0) or after last row (RowCount + 1)
        if (m_Cursor < 0 || m_Cursor > getRowCount() + 1) {
            throw new SQLException();
        }
        return internalPrevious();
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException
    {
        return isEmpty() ? false : m_Cursor == 0;
    }

    @Override
    public boolean isFirst()
        throws SQLException
    {
        // XXX: This becomes nasty because of deletes.
        boolean first = false;
        int cursor = m_Cursor;
        int position = m_Position;
        internalFirst();
        if (m_Cursor == cursor) {
            first = true;
        }
        else {
            m_Cursor = cursor;
            m_Position = position;
        }
        return first;
    }

    @Override
    public boolean isLast()
        throws SQLException
    {
        // XXX: This becomes nasty because of deletes.
        boolean last = false;
        int cursor = m_Cursor;
        int position = m_Position;
        internalLast();
        if (m_Cursor == cursor) {
            last = true;
        }
        else {
            m_Cursor = cursor;
            m_Position = position;
        }
        return last;
    }

    @Override
    public boolean isAfterLast()
        throws SQLException
    {
        return isEmpty() ? false : m_Cursor == getRowCount() + 1;
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        m_Cursor = 0;
        m_Position = 0;
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        if (!isEmpty()) {
            m_Cursor = getRowCount() + 1;
            m_Position = getMaxPosition() + 1;
        }
    }

    @Override
    public boolean first()
        throws SQLException
    {
        if (isEmpty()) {
            return false;
        }
        return internalFirst();
    }

    @Override
    public boolean last()
        throws SQLException
    {
        if (isEmpty()) {
            return false;
        }
        return internalLast();
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        if (isEmpty() || isBeforeFirst() || isAfterLast()) {
            throw new SQLException();
        }
        if (row == 0) {
            return true;
        }
        int count = getRowCount();
        // XXX: We are moving forward
        if (row > 0) {
            if (m_Cursor + row > count) {
                // XXX: Fell off the end
                afterLast();
            }
            else {
                for (int i = 0; i < row; i++) {
                    if (!internalNext())
                        break;
                }
            }
        }
        // XXX: We are moving backward
        else {
            if (m_Cursor + row < 0) {
                // XXX: Fell off the front
                beforeFirst();
            }
            else {
                for (int i = row; i < 0; i++) {
                    if (!internalPrevious())
                        break;
                }
            }
        }
        if (isAfterLast() || isBeforeFirst()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        if (checkAbsolute(row)) {
            // XXX: Now move towards the absolute row that we're looking for
            while (m_Position != row) {
                if (m_Position < row) {
                    if (!internalNext())
                        break;
                }
                else {
                    if (!internalPrevious())
                        break;
                }
            }
            return moveResultSet(m_Position, row);
        }
        return false;
    }

    @Override
    public int getRow()
        throws SQLException
    {
        int count = getRowCount();
        if (isEmpty() || m_Cursor < 0 || m_Cursor > count) {
            return 0; 
        }
        return m_Position;
    }

    public int getBookmark()
    {
        return m_Cursor;
    }

    public boolean moveToBookmark(int row)
        throws SQLException
    {
        boolean moved = false;
        System.out.println("CachedResultSet.moveToBookmark() 1 Cursor: " + m_Cursor + " - row: " + row);
        if (checkAbsolute(row)) {
            System.out.println("CachedResultSet.moveToBookmark() 2");
            // XXX: Now move towards the absolute row that we're looking for
            while (m_Cursor != row) {
                if (m_Cursor < row) {
                    if (!internalNext())
                        break;
                }
                else {
                    if (!internalPrevious())
                        break;
                }
            }
            moved = moveResultSet(m_Cursor, row);
        }
        System.out.println("CachedResultSet.moveToBookmark() 3 Cursor: " + m_Cursor + " - moved: " + moved);
        return moved;
    }


    // XXX: java.sql.ResultSet updater by index
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        m_Result.updateNull(index);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        m_Result.updateBoolean(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        m_Result.updateByte(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        m_Result.updateShort(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        m_Result.updateInt(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        m_Result.updateLong(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        m_Result.updateFloat(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        m_Result.updateDouble(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        m_Result.updateBigDecimal(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        System.out.println("CachedResultSet.updateString() 1 Value: " + value);
        m_Result.updateString(index, value);
        System.out.println("CachedResultSet.updateString() 2 Value: " + m_Result.getString(index));
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        m_Result.updateBytes(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        m_Result.updateDate(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        m_Result.updateTime(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        m_Result.updateTimestamp(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        m_Result.updateObject(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        m_Result.updateObject(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        m_Result.updateRef(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        m_Result.updateArray(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        m_Result.updateBlob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateClob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNClob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        m_Result.updateSQLXML(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        m_Result.updateNString(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        m_Result.updateRowId(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }


    // XXX: Protected methods
    protected void checkIndex(int index)
        throws SQLException
    {
        if (index < 1 || index > m_ColumnCount) {
            throw new SQLException("ERROR Index is out of range");
        }
    }

    protected boolean isOnInsertRow()
    {
        return m_OnInsert;
    }

    protected void setInsertMode(boolean mode)
    {
        m_OnInsert = mode;
    }

    protected Row getResultRow()
        throws SQLException
    {
        Row row = new Row(m_ColumnCount);
        for (int i = 1; i <= m_ColumnCount; i++) {
            row.initColumnObject(m_Result, i);
            System.out.println("CachedResultSet.getResultRow() 1 Value: " + m_Result.getObject(i).toString());
        }
        return row;
    }

    protected InsertRow getResultInsertRow()
        throws SQLException
    {
        InsertRow row = new InsertRow(m_ColumnCount);
        for (int i = 1; i <= m_ColumnCount; i++) {
            row.initColumnObject(m_Result, i);
        }
        return row;
    }

    protected boolean isDeleted()
    {
        return isDeleted(m_Cursor);
    }

    protected RowSetWriter getRowSetWriter()
        throws SQLException
    {
        if (m_RowSetWriter == null) {
            m_RowSetWriter = new RowSetWriter(m_Provider, m_Catalog, m_Result, m_Table);
        }
        return m_RowSetWriter;
    }

    protected int getMaxPosition()
    {
        // FIXME: m_Position skips any row in the ResultSet that was deleted
        return getRowCount() - m_DeletedRows.size();
    }

    protected void setColumnObject(int index, Object value)
        throws SQLException
    {
        getCurrentRow().setColumnObject(index, value);
    }

    protected void setColumnDouble(int index, Double value)
        throws SQLException
    {
        int type = m_Result.getMetaData().getColumnType(index);
        getCurrentRow().setColumnDouble(index, value, type);
    }

    protected void checkCursor()
        throws SQLException
    {
        if (m_Cursor <= 0 || m_Cursor > getRowCount()) {
            throw new SQLException("ERROR Row is out of range");
        }
    }

    protected boolean isDeleted(int row)
    {
        return m_DeletedRows.contains(row);
    }

    protected abstract boolean isEmpty();

    protected abstract int getRowCount();

    protected abstract BaseRow getCurrentRow() throws SQLException;

    protected abstract void incrementCursor() throws SQLException;

    protected abstract boolean moveResultSet(Integer position, int row) throws SQLException;


    private boolean internalFirst()
        throws SQLException
    {
        if (!isEmpty()) {
            m_Cursor = 0;
            m_Position = 0;
            return internalNext();
        }
        return false;
    }

    private boolean internalLast()
        throws SQLException
    {
        if (!isEmpty()) {
            m_Cursor = getRowCount();
            m_Position = getMaxPosition();
            return internalPrevious();
        }
        return false;
    }

    protected boolean internalNext()
        throws SQLException
    {
        boolean moved = false;
        int count = getRowCount();
        do {
            if (m_Cursor < count) {
                incrementCursor();
                moved = true;
            }
            else if (m_Cursor == count) {
                // XXX: Increment to after last
                ++m_Cursor;
                moved = false;
                break;
            }
        } while (isDeleted());
        // XXX: Each call to internalNext may increment cursor m_Cursor multiple
        // XXX: times however, the m_Position only increments once per call.
        if (moved) {
            m_Position++;
        }
        else {
            m_Position = getMaxPosition() + 1;
        }
        return moved;
    }

    private boolean internalPrevious()
        throws SQLException
    {
        boolean moved = false;
        do {
            if (m_Cursor > 1) {
                --m_Cursor;
                moved = true;
            }
            else if (m_Cursor == 1) {
                // XXX: Decrement to before first
                --m_Cursor;
                moved = false;
                break;
            }
        } while (isDeleted());
        // XXX: Each call to internalPrevious may move the cursor m_Cursor over
        // XXX: multiple rows, the absolute position m_Position moves one row
        if (moved) {
            --m_Position;
        }
        else {
            m_Position = 0;
        }
        return moved;
    }

    private boolean checkAbsolute(int row)
        throws SQLException
    {
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
            }
            else if (m_Cursor <= 0) {
                internalFirst();
            }
            // XXX: In addition to a position outside the ResultSet,
            // XXX: we need to handle the case if the row has been deleted
            else if (isDeleted()) {
                internalPrevious();
            }
        }
        // XXX: We are moving backward
        else {
            if (count + row < 0) {
                // XXX: Fell off the front
                beforeFirst();
                checked = false;
            }
            else if (m_Cursor > count) {
                internalLast();
            }
            // XXX: In addition to a position outside the ResultSet,
            // XXX: we need to handle the case if the row has been deleted
            else if (isDeleted()) {
                internalNext();
            }
        }
        return checked;
    }

}
