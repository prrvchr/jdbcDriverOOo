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

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.BaseRow;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;
import io.github.prrvchr.jdbcdriver.rowset.RowSetWriter;


public abstract class CachedResultSet
    extends ResultSet
{

    protected DriverProvider m_Provider;
    // XXX: If ResultSet cannot be updated, we use a RowSetWriter
    // XXX: which allows us to send the correct SQL queries
    protected RowSetWriter m_RowSetWriter = null;
    // XXX: If we use RowSetWriter we need the RowCatalog
    protected RowCatalog m_Catalog = null;
    protected int m_MinSize = 10;
    protected int m_ColumnCount = 0;
    protected int m_FetchSize;
    protected String m_Query;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor that supports deleted lines
    protected int m_Cursor = 0;
    // XXX: If we want to be able to manage deletion visibility then we need to
    // XXX: maintain a cursor which hides deleted lines
    protected int m_Position = 0;
    // XXX: If we want to be able to manage bookmarks then we need to keep a cache of all deleted rows.
    protected List<Integer> m_DeletedRows = new ArrayList<>();
    protected int m_DeletedInsert = 0;
    // XXX: The field that temporarily holds the last position of the cursor before it moved to the insert row
    protected int m_CurrentRow = 0;

    // XXX: If ResultSet is not updatable then we need to emulate the insert row.
    protected InsertRow m_InsertRow = null;

    protected boolean m_Insertable = false;
    // XXX: We need to know when we are on the insert row
    protected boolean m_OnInsert = false;
    // XXX: We need to keep the index references of the columns already assigned for insertion
    protected BitSet m_InsertedColumns;
    protected boolean m_IsDeleteVisible;
    protected boolean m_IsInsertVisible;
    protected boolean m_IsUpdateVisible;

    // The constructor method:
    public CachedResultSet(DriverProvider provider,
                           java.sql.ResultSet result,
                           RowCatalog catalog,
                           String query)
        throws SQLException
    {
        super(result);
        m_Provider = provider;
        m_Catalog = catalog;
        m_Query = query;
        int rstype = result.getType();
        boolean updatable = provider.isResultSetUpdatable(result);
        m_IsDeleteVisible = updatable && provider.isDeleteVisible(rstype);
        m_IsInsertVisible = updatable && provider.isInsertVisible(rstype);
        m_IsUpdateVisible = updatable && provider.isUpdateVisible(rstype);
        m_FetchSize = result.getFetchSize();
        m_ColumnCount = result.getMetaData().getColumnCount();
        m_Insertable = updatable;
        m_InsertedColumns = new BitSet(m_ColumnCount);
        System.out.println("CachedResultSet() Insertable: " + m_Insertable);
    }


    @Override
    public void insertRow()
        throws SQLException
    {
        // XXX: Base does not allow auto-increment columns to be entered,
        // XXX: some drivers force us to update these columns to null
        RowHelper.setDefaultColumnValues(m_Result, m_InsertedColumns);
        m_InsertedColumns.clear();
        m_Result.insertRow();
        moveToCurrentRow();
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException
    {
        if (m_Insertable) {
            if (isOnInsertRow()) {
                moveToCurrentRow();
            }
            else {
                m_Result.cancelRowUpdates();
            }
        }
        if (isOnInsertRow()) {
            setInsertMode(false);
        }
    }

    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        if (m_Insertable) {
            m_Result.moveToInsertRow();
            m_InsertedColumns.clear();
        }
        setInsertMode(true);
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        if (m_Insertable) {
            m_Result.moveToCurrentRow();
        }
        setInsertMode(false);
    }

    @Override
    public int getConcurrency()
        throws SQLException
    {
        return m_Result.getConcurrency();
    }

    @Override
    public int getType()
        throws SQLException
    {
        return m_Result.getType();
    }


    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        boolean deleted = false;
        // XXX: We can assume the delete is valid without any
        // XXX: movement in the ResultSet since the delete.
        deleted = m_DeletedRows.contains(m_Cursor);
        return deleted;
    }


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException
    {
        /*
         * make sure things look sane. The cursor must be
         * positioned in the rowset or before first (0) or
         * after last (numRows + 1)
         */
        if (m_Cursor < 0 || m_Cursor > getRowCount() + 1) {
            throw new SQLException();
        }
        return internalNext();
    }

    @Override
    public boolean previous()
        throws SQLException 
    {
        /*
         * make sure things look sane. The cursor must be
         * positioned in the rowset or before first (0) or
         * after last (numRows + 1)
         */
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
        // this becomes nasty because of deletes.
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
            return internalAbsolute(m_Position, row);
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
        if (checkAbsolute(row)) {
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
            return internalAbsolute(m_Cursor, row);
        }
        return false;
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
        m_Result.updateString(index, value);
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

    protected boolean setResultPosition()
        throws SQLException
    {
        int position = m_IsDeleteVisible ? m_Position : m_Cursor;
        return m_Result.absolute(position);
    }

    protected boolean isDeleted()
    {
        return isDeleted(m_Cursor);
    }

    protected RowSetWriter getRowSetWriter()
        throws SQLException
    {
        if (m_RowSetWriter == null) {
            m_RowSetWriter = new RowSetWriter(m_Provider, m_Catalog, m_Result, m_Query);
        }
        return m_RowSetWriter;
    }

    protected boolean internalNext()
        throws SQLException
    {
        boolean moved = false;
        do {
            int count = getRowCount();
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

    protected int getMaxPosition()
    {
        return getRowCount() - m_DeletedRows.size();
    }

    protected void incrementCursor()
        throws SQLException
    {
        ++m_Cursor;
    }

    protected void setColumnObject(int index, Object value)
        throws SQLException
    {
        getCurrentRow(index).setColumnObject(index, value);
    }

    protected void setColumnDouble(int index, Double value)
        throws SQLException
    {
        int type = m_Result.getMetaData().getColumnType(index);
        getCurrentRow(index).setColumnDouble(index, value, type);
    }

    protected void checkCursor()
        throws SQLException
    {
        if (m_Cursor <= 0 || m_Cursor > getRowCount()) {
            throw new SQLException("ERROR Row is out of range");
        }
    }


    protected abstract boolean isEmpty();

    protected abstract int getRowCount();

    protected abstract BaseRow getCurrentRow(int index) throws SQLException;

    protected abstract boolean internalAbsolute(Integer position, int row) throws SQLException;


    private boolean isDeleted(int row)
    {
        return m_DeletedRows.contains(row);
    }

    private boolean internalFirst()
        throws SQLException
    {
        boolean moved = false;
        if (!isEmpty()) {
            m_Cursor = 1;
            if (isDeleted()) {
                moved = internalNext();
            }
            else {
                moved = true;
            }
        }
        if (moved) {
            m_Position = 1;
        }
        else {
            m_Position = 0;
        }
        return moved;
    }

    private boolean internalLast()
        throws SQLException
    {
        boolean moved = false;
        if (!isEmpty()) {
            m_Cursor = getRowCount();
            if (isDeleted()) {
                moved = internalPrevious();
            }
            else {
                moved = true;
            }
        }
        if (moved) {
            m_Position = getMaxPosition();
        }
        else {
            m_Position = 0;
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
