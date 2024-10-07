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

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.BaseRow;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;


// XXX: This ResultSet is supposed to emulate a TYPE_SCROLL_SENSITIVE from a TYPE_SCROLL_INSENSITIVE
// XXX: If the ResultSet is not updatable (ie: CONCUR_UPDATABLE) then SQL DML commands will be used
// XXX: (ie: INSERT, DELETE, UPDATE...) instead of positioned updates.
public class SensitiveResultSet
    extends CachedResultSet
{

    // XXX: If the updated row are not visible then we keep a cache of all updated rows.
    private List<Integer> m_UpdatedRows = new ArrayList<>();
    private Vector<Row> m_UpdatedData = null;
    // XXX: If the inserted row are not visible then we keep a cache of all inserted rows.
    private List<Integer> m_InsertedRows = new ArrayList<>();
    private Vector<Row> m_InsertedData = null;
    private int m_RowCount = 0;
    private int m_Inserted = -1;
    private int m_Deleted = -1;

    private boolean m_SQLDelete;
    private boolean m_SQLInsert;
    private boolean m_SQLUpdate;
    private boolean m_IsDeleteVisible;
    private boolean m_IsInsertVisible;
    private boolean m_IsUpdateVisible;


    // The constructor method:
    public SensitiveResultSet(DriverProvider provider,
                              ResultSet result,
                              RowCatalog catalog,
                              String table)
        throws SQLException
    {
        super(provider, result, catalog, table);
        int size = m_FetchSize < m_MinSize ? m_MinSize : m_FetchSize;
        m_UpdatedData = new Vector<Row>(size);
        m_InsertedData = new Vector<Row>(size);
        boolean SQLMode = provider.useSQLMode(result);
        m_SQLDelete = SQLMode || provider.useSQLDelete();
        m_SQLInsert = SQLMode || provider.useSQLInsert();
        m_SQLUpdate = SQLMode || provider.useSQLUpdate();
        m_IsDeleteVisible = !m_SQLDelete && provider.isDeleteVisible(result);
        m_IsInsertVisible = !m_SQLInsert && provider.isInsertVisible(result);
        m_IsUpdateVisible = !m_SQLUpdate && provider.isUpdateVisible(result);
        System.out.println("SensitiveResultSet() 1");
        System.out.println("SensitiveResultSet() Use SQL Delete: " + m_SQLDelete);
        System.out.println("SensitiveResultSet() Use SQL Insert: " + m_SQLInsert);
        System.out.println("SensitiveResultSet() Use SQL Update: " + m_SQLUpdate);
        System.out.println("SensitiveResultSet() Delete are visible: " + m_IsDeleteVisible);
        System.out.println("SensitiveResultSet() Insert are visible: " + m_IsInsertVisible);
        System.out.println("SensitiveResultSet() Update are visible: " + m_IsUpdateVisible);
        loadLastRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        if (m_OnInsert) {
            if (m_IsInsertVisible) {
                System.out.println("SensitiveResultSet.moveToCurrentRow() 2");
                m_Result.moveToCurrentRow();
            }
            else {
                System.out.println("SensitiveResultSet.moveToCurrentRow() 1");
                m_InsertRow = null;
            }
            m_Cursor = m_CurrentRow;
            m_OnInsert = false;
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 110: xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        if (m_IsInsertVisible) {
            System.out.println("SensitiveResultSet.moveToInsertRow() 2");
            m_Result.moveToInsertRow();
            m_InsertedColumns.clear();
        }
        else {
            System.out.println("SensitiveResultSet.moveToInsertRow() 1");
            m_InsertRow = new InsertRow(m_ColumnCount);
        }
        m_OnInsert = true;
        m_CurrentRow = m_Cursor;
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException
    {
        if (isOnInsertRow()) {
            moveToCurrentRow();
        }
        else {
            m_Result.cancelRowUpdates();
        }
    }

    @Override
    public void insertRow()
        throws SQLException
    {
        BaseRow insert = null;
        System.out.println("SensitiveResultSet.insertRow() 1 Cursor: " + m_Cursor + " - InsertVisible: " + m_IsInsertVisible);
        if (m_IsInsertVisible) {
            System.out.println("SensitiveResultSet.insertRow() 2");
            // XXX: Base does not allow auto-increment columns to be entered,
            // XXX: some drivers force us to update these columns to null
            RowHelper.setDefaultColumnValues(m_Result, m_InsertedColumns);
            m_InsertedColumns.clear();
            m_Result.insertRow();
            System.out.println("SensitiveResultSet.insertRow() 3");
        }
        else {
            if (m_InsertRow == null) {
                throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !");
            }
            System.out.println("SensitiveResultSet.insertRow() 4");
            insert = m_InsertRow.clown();
            getRowSetWriter().insertRow(insert);
        }
        moveToCurrentRow();
        int cursor = 0;
        int position = 0;
        if (insert != null) {
            // XXX: As we have inserted a new row by an SQL command then
            // XXX: we need to put this new row in the cache
            System.out.println("SensitiveResultSet.insertRow() 5");
            cursor = getRowCount() + 1;
            position = getMaxPosition() + 1;
            Row row = new Row(insert);
            m_InsertedData.add(row);
            m_InsertedRows.add(cursor);
            System.out.println("SensitiveResultSet.insertRow() 6 at Cursor: " + cursor);
        }
        else {
            System.out.println("SensitiveResultSet.insertRow() 7");
            // XXX: Since we are maintaining the row count of the ResultSet, we need to increment it.
            m_RowCount ++;
            cursor = m_RowCount;
            position = getMaxPosition();
        }
        // XXX: cursor and position must be set on the inserted row
        m_Cursor = cursor;
        m_Position = position;
        // XXX: We must be able to respond positively to the insert
        m_Inserted = cursor;
        System.out.println("SensitiveResultSet.insertRow() 8 Cursor: " + m_Cursor);
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        boolean deleted = isDeleted(m_Deleted);
        m_Deleted = -1;
        System.out.println("SensitiveResultSet.rowDeleted() 1 : " + deleted);
        return deleted;
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        System.out.println("SensitiveResultSet.rowInserted() 1");
        // XXX: We can assume the insertion is valid without any
        // XXX: movement in the ResultSet since the insertion.
        boolean inserted = m_Inserted == m_Cursor;
        m_Inserted = -1;
        System.out.println("SensitiveResultSet.rowInserted() 2 : " + inserted);
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        System.out.println("SensitiveResultSet.updateRow() 1");
        if (updateCachedRow()) {
            System.out.println("SensitiveResultSet.updateRow() 2");
            getRowSetWriter().updateRow(getCachedRow());
        }
        else {
            System.out.println("SensitiveResultSet.updateRow() 3");
            super.updateRow();
        }
        System.out.println("SensitiveResultSet.updateRow() 4");
    }

    @Override
    public boolean rowUpdated()
        throws SQLException
    {
        System.out.println("SensitiveResultSet.rowUpdated() 1");
        boolean updated = m_Result.rowUpdated();
        System.out.println("SensitiveResultSet.rowUpdated() 2 : " + updated);
        return updated;
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        m_Deleted = m_Cursor;
        System.out.println("SensitiveResultSet.deleteRow() 1 Position: " + m_Deleted);
        boolean incache = isCachedRow();
        if (m_IsDeleteVisible) {
            int row = m_Result.getRow();
            System.out.println("SensitiveResultSet.deleteRow() 3 getRow: " + row);
            m_Result.last();
            int last = m_Result.getRow();
            System.out.println("SensitiveResultSet.deleteRow() 4 Last Row: " + last);
            m_Result.absolute(row);
            m_Result.deleteRow();
            m_Result.last();
            last = m_Result.getRow();
            System.out.println("SensitiveResultSet.deleteRow() 5 Last Row: " + last);
            m_Result.absolute(row -1);
            // XXX: The row preceding the deleted row becomes the current row
            //previous();
            System.out.println("SensitiveResultSet.deleteRow() 6 getRow: " + m_Result.getRow());
        }
        else {
            System.out.println("SensitiveResultSet.deleteRow() 2");
            Row row = incache ? getCachedRow() : getResultRow();
            getRowSetWriter().deleteRow(row);
        }
        // XXX: Are we trying to delete a row that has been inserted or updated and which will be in cache?
        if (incache) {
            deleteCachedRow(m_Deleted);
        }
        // XXX: Managing bookmark requires us to manage a cursor taking into account the deleted lines.
        m_DeletedRows.add(m_Deleted);
    }


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException
    {
        boolean moved = super.next();
        if (moved && !isInCache()) {
            moved = moveResultSet();
        }
        return moved;
    }

    @Override
    public boolean previous()
        throws SQLException 
    {
        boolean moved = super.previous();
        if (moved && !isInCache()) {
            moved = moveResultSet();
        }
        return moved;
    }

    @Override
    public boolean first()
        throws SQLException
    {
        boolean first = super.first();
        if (first && !isInCache()) {
            first = moveResultSet();
        }
        return first;
    }

    @Override
    public boolean last()
        throws SQLException
    {
        System.out.println("SensitiveResultSet.last() 1");
        boolean last = super.last();
        System.out.println("SensitiveResultSet.last() 2 : " + last + " - IsInCache: " + isInCache());
        System.out.println("SensitiveResultSet.last() 3 isUpdated: " + isUpdated() + " - isInserted: " + isInserted() + " - isDeleted: " + isDeleted() + " - isOnInsertRow: " + isOnInsertRow());
        if (last && !isInCache()) {
            System.out.println("SensitiveResultSet.last() 4 Cursor: " + m_Cursor + " - Position: " + m_Position);
            last = moveResultSet();
        }
        return last;
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        boolean moved = super.relative(row);
        if (moved && !isCachedRow()) {
            moved = moveResultSet();
        }
        return moved;
    }


    // XXX: java.sql.ResultSet getter
    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_WasNull;
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        String value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (String) row.getColumnObject(index);
                System.out.println("SensitiveResultSet.getString() 1 : " + value);
            }
        }
        else {
            value = m_Result.getString(index);
            System.out.println("SensitiveResultSet.getString() 2 : " + value + " - Row: " + m_Result.getRow());
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        boolean value = false;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Boolean) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBoolean(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        byte value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Byte) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getByte(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        short value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Short) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getShort(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        int value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Integer) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getInt(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        long value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Long) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getLong(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        float value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Float) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getFloat(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        double value = 0;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Double) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getDouble(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException
    {
        BigDecimal value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (BigDecimal) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBigDecimal(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        byte[] value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (byte[]) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBytes(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        Date value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Date) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getDate(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        Time value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Time) row.getColumnObject(index);
            }
       }
        else {
            value = m_Result.getTime(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException
    {
        Timestamp value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Timestamp) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getTimestamp(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException
    {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getAsciiStream(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int index)
        throws SQLException
    {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getUnicodeStream(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException
    {
        InputStream value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (InputStream) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBinaryStream(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Object getObject(int index)
        throws SQLException
    {
        Object value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Object) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getObject(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException
    {
        Reader value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Reader) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getCharacterStream(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException
    {
        BigDecimal value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (BigDecimal) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBigDecimal(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException
    {
        Object value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Object) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getObject(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException
    {
        Ref value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Ref) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getRef(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException
    {
        Blob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Blob) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getBlob(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException
    {
        Clob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Clob) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getClob(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Array getArray(int index)
        throws SQLException
    {
        Array value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Array) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getArray(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException
    {
        Date value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Date) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getDate(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException
    {
        Time value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Time) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getTime(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException
    {
        Timestamp value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Timestamp) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getTimestamp(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public URL getURL(int index)
        throws SQLException
    {
        URL value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (URL) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getURL(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException
    {
        RowId value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (RowId) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getRowId(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException
    {
        NClob value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (NClob) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getNClob(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException
    {
        SQLXML value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (SQLXML) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getSQLXML(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public String getNString(int index)
        throws SQLException
    {
        String value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (String) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getNString(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException
    {
        Reader value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (Reader) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getNCharacterStream(index);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int index, Class<T> type)
        throws SQLException
    {
        T value = null;
        if (isInCache()) {
            checkIndex(index);
            BaseRow row = getCurrentRow();
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (T) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getObject(index, type);
            m_WasNull = m_Result.wasNull();
        }
        return value;
    }


    // XXX: java.sql.ResultSet updater
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, null);
        }
        if (updateResultSet()) {
            super.updateNull(index);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBoolean(index, value);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateByte(index, value);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateShort(index, value);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateInt(index, value);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateLong(index, value);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateFloat(index, value);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        // XXX: Base using updateDouble() for most numeric SQL types,
        // XXX: it is necessary to convert to the native column type
        System.out.println("SensitiveResultSet.updateDouble() 1 Value: " + value);
        if (updateCache()) {
            setColumnDouble(index, value);
        }
        if (updateResultSet()) {
            super.updateDouble(index, value);
        }
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBigDecimal(index, value);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        System.out.println("SensitiveResultSet.updateString() 1 Value: " + value);
        if (updateCache()) {
            System.out.println("SensitiveResultSet.updateString() 2");
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            System.out.println("SensitiveResultSet.updateString() 3");
            super.updateString(index, value);
        }
        System.out.println("SensitiveResultSet.updateString() 4");
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBytes(index, value);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateDate(index, value);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateTime(index, value);
        }
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateTimestamp(index, value);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value, length);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value, length);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateObject(index, value, length);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateObject(index, value);
        }
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateRef(index, value);
        }
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value);
        }
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value);
        }
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateArray(index, value);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        if (updateCache()) {
            updateAsciiStream(index, value, length);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value, length);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value, length);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value, length);
        }
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value, length);
        }
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value, length);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNCharacterStream(index, value);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateAsciiStream(index, value);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBinaryStream(index, value);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateCharacterStream(index, value);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateBlob(index, value);
        }
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateClob(index, value);
        }
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value);
        }
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateSQLXML(index, value);
        }
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNString(index, value);
        }
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateNClob(index, value);
        }
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        if (updateResultSet()) {
            super.updateRowId(index, value);
        }
    }


    // XXX: Private methods
    @Override
    protected boolean isEmpty()
    {
        int count = m_RowCount + m_InsertedRows.size() - m_DeletedRows.size();
        return count == 0;
    }

    @Override
    protected int getRowCount()
    {
        int count = m_RowCount;
        // XXX: If we want to be able to make the insertions visible then we must add them.
        if (!m_IsInsertVisible) {
            count += m_InsertedRows.size() + m_DeletedInsert;
        }
        return count;
    }

    @Override
    protected boolean moveResultSet(Integer position, int row)
        throws SQLException
    {
        boolean moved = false;
        try {
            if (isAfterLast() || isBeforeFirst()) {
                return false;
            }
            // XXX: For loading the first row Base use absolute(1) will the current row is beforeFirst.
            boolean cached = isCachedRow();
            System.out.println("SensitiveResultSet.internalAbsolute() 1 Cursor: " + m_Cursor + " - Cached: " + cached);
            moved = cached ? true : moveResultSet();
            System.out.println("SensitiveResultSet.internalAbsolute() 2 Cursor: " + m_Cursor);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return moved;
    }

    @Override
    protected BaseRow getCurrentRow()
        throws SQLException
    {
        if (m_OnInsert) {
            return m_InsertRow;
        }
        checkCursor();
        if (!isInsertedRow() && !isUpdatedRow()) {
            createUpdatedRow();
        }
        return getCachedRow();
    }

    @Override
    protected void incrementCursor()
        throws SQLException
    {
        ++m_Cursor;
    }


    // XXX: Private methods
    private boolean isInCache()
    {
        return isUpdated() || isInserted() || isDeleted() || isOnInsertRow();
    }

    private boolean updateCache()
    {
        // XXX: We update Cached if:
        // XXX: - We do an insert and insert are not visible
        // XXX: - We do an update and update are not visible
        // XXX: - The current row is an inserted row in cache
        return isInsertNotVisible() || isUpdateNotVisible() || isOnInsertedRow();
    }

    private boolean updateResultSet()
    {
        // XXX: We don't update ResultSet if:
        // XXX: - We do an insert and use SQL mode for insert
        // XXX: - We do an update and use SQL mode for update
        // XXX: - The current row is an inserted row in cache
        return !(isSQLInsert() || isSQLUpdate() || isOnInsertedRow());
    }

    private boolean isCachedRow()
    {
        return isCachedRow(m_Cursor);
    }

    private boolean updateCachedRow()
    {
        return (m_SQLUpdate && isUpdatedRow()) || (m_SQLInsert && isInsertedRow());
    }

    private boolean isInsertNotVisible()
    {
        return m_OnInsert && !m_IsInsertVisible;
    }

    private boolean isUpdateNotVisible()
    {
        return !m_OnInsert && !m_IsUpdateVisible;
    }

    private boolean isSQLInsert()
    {
        return m_OnInsert && m_SQLInsert;
    }

    private boolean isSQLUpdate()
    {
        return !m_OnInsert && m_SQLUpdate;
    }

    private boolean isOnInsertedRow()
    {
        return !m_OnInsert && !m_IsInsertVisible && isInsertedRow();
    }

    private boolean isCachedRow(int position)
    {
        return isUpdatedRow(position) || isInsertedRow(position);
    }

    private boolean isUpdatedRow()
    {
        return m_UpdatedRows.contains(m_Cursor);
    }

    private boolean isUpdatedRow(Integer row)
    {
        return m_UpdatedRows.contains(row);
    }

    private boolean isInsertedRow()
    {
        return m_InsertedRows.contains(m_Cursor);
    }

    private boolean isInsertedRow(int row)
    {
        return m_InsertedRows.contains(row);
    }

    private boolean isUpdated()
    {
        return isUpdated(m_Cursor);
    }

    private boolean isInserted()
    {
        return isInserted(m_Cursor);
    }

    private boolean isUpdated(int row)
    {
        return !m_IsUpdateVisible && isUpdatedRow(row);
    }

    private boolean isInserted(int row)
    {
        return !m_IsInsertVisible && isInsertedRow(row);
    }

    private void createUpdatedRow()
        throws SQLException
    {
        m_UpdatedRows.add(m_Cursor);
        m_UpdatedData.add(getResultRow());
    }

    private void deleteCachedRow(Integer position)
        throws SQLException
    {
        if (m_UpdatedRows.contains(position)) {
            m_UpdatedData.remove(m_UpdatedRows.indexOf(position));
            m_UpdatedRows.remove(position);
        }
        else if (m_InsertedRows.contains(position)) {
            m_InsertedData.remove(m_InsertedRows.indexOf(position));
            m_InsertedRows.remove(position);
            m_DeletedInsert ++;
        }
    }

    private Row getCachedRow()
    {
        Row row = null;
        if (m_UpdatedRows.contains(m_Cursor)) {
            row = m_UpdatedData.get(m_UpdatedRows.indexOf(m_Cursor));
        }
        else if (m_InsertedRows.contains(m_Cursor)) {
            row = m_InsertedData.get(m_InsertedRows.indexOf(m_Cursor));
        }
        return row;
    }

    private boolean moveResultSet()
        throws SQLException
    {
        int position = m_IsDeleteVisible ? m_Position : m_Cursor;
        return m_Result.absolute(position);
    }

    // Private methods for managing last row cache
    private void loadLastRow()
        throws SQLException
    {
        if (m_Result.last()) {
            m_RowCount = m_Result.getRow();
        }
        m_Result.beforeFirst();
    }

}
