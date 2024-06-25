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
    private boolean m_Updatable = false;
    private int m_Inserted = -1;
    private boolean m_WasNull = false;
    private boolean m_SQLDelete = false;
    private boolean m_SQLInsert = true;
    private boolean m_SQLUpdate = false;
    private boolean m_SQLMode = false;


    // The constructor method:
    public SensitiveResultSet(DriverProvider provider,
                              ResultSet result,
                              RowCatalog catalog,
                              String query)
        throws SQLException
    {
        super(provider, result, catalog, query);
        int size = m_FetchSize < m_MinSize ? m_MinSize : m_FetchSize;
        m_UpdatedData = new Vector<Row>(size);
        m_InsertedData = new Vector<Row>(size);
        m_SQLDelete = provider.useSQLDelete();
        m_SQLInsert = provider.useSQLInsert();
        m_SQLUpdate = provider.useSQLUpdate();
        m_SQLMode = provider.isSQLMode();
        boolean updatable = provider.isResultSetUpdatable(result);
        if (!updatable) {
            m_Catalog = new RowCatalog(provider, result, query);
            updatable = m_Catalog.hasRowIdentifier();
            System.out.println("SensitiveResultSet()1 TableCount: " + m_Catalog.getTableCount());
        }
        System.out.println("SensitiveResultSet() 2 Updatable: " + updatable);
        m_Updatable = updatable;
        loadLastRow();
    }

    // XXX: We want to emulate an updatable ResultSet and it is the presence of
    // XXX: primary key in the catalog which determines if the ResultSet is updatable.
    @Override
    public int getConcurrency()
        throws SQLException
    {
        return m_Updatable ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY;
    }

    // XXX: We want to emulate an scollable ResultSet
    @Override
    public int getType()
        throws SQLException
    {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }


    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 110: xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        if (m_IsInsertVisible) {
            m_Result.moveToInsertRow();
            m_InsertedColumns.clear();
        }
        else {
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
    public void moveToCurrentRow()
        throws SQLException
    {
        if (m_OnInsert) {
            if (m_IsInsertVisible) {
                m_Result.moveToCurrentRow();
            }
            else {
                m_InsertRow = null;
            }
            m_Cursor = m_CurrentRow;
            m_OnInsert = false;
        }
    }


    @Override
    public void insertRow()
        throws SQLException
    {
        BaseRow insert = null;
        if (!m_IsInsertVisible || m_SQLMode || m_SQLInsert) {
            insert = m_InsertRow != null ? m_InsertRow.clown() : getResultInsertRow();
            getRowSetWriter().insertRow(insert);
            moveToCurrentRow();
        }
        else{
            super.insertRow();
        }
        int cursor = 0;
        int position = 0;
        if (m_IsInsertVisible) {
            m_RowCount ++;
            cursor = m_RowCount;
            position = getMaxPosition();
        }
        else {
            cursor = getRowCount() + 1;
            position = getMaxPosition() + 1;
            Row row = insert != null ? new Row(insert) : new Row(getResultRow());
            m_InsertedData.add(row);
            m_InsertedRows.add(cursor);
        }
        // XXX: cursor and position must be set on the inserted row
        m_Cursor = cursor;
        m_Position = position;
        // XXX: We must be able to respond positively to the insert
        m_Inserted = cursor;
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        // XXX: We can assume the insertion is valid without any
        // XXX: movement in the ResultSet since the insertion.
        boolean inserted = m_Inserted == m_Cursor;
        m_Inserted = -1;
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        boolean updated = isCachedRow();
        if (updated || m_SQLMode || m_SQLUpdate) {
            Row row = updated ? getCachedRow() : getResultRow();
            getRowSetWriter().updateRow(row);
        }
        else {
            super.updateRow();
        }
    }

    @Override
    public boolean rowUpdated()
        throws SQLException
    {
        boolean updated = m_Result.rowUpdated();
        return updated;
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        int position = m_Cursor;
        boolean incache = isCachedRow();
        if (incache || m_SQLMode || m_SQLDelete) {
            Row row = incache ? getCachedRow() : getResultRow();
            getRowSetWriter().deleteRow(row);
            // XXX: Are we trying to delete a row that has been inserted or updated and which will be in cache?
            if (incache) {
                deleteCachedRow(position);
            }
        }
        else {
            m_Result.deleteRow();
        }
        // XXX: Managing bookmark requires us to manage a cursor taking into account the deleted lines.
        m_DeletedRows.add(position);
    }

    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException
    {
        boolean moved = super.next();
        if (moved && !isInCache()) {
            moved = setResultPosition();
        }
        return moved;
    }

    @Override
    public boolean previous()
        throws SQLException 
    {
        boolean moved = super.previous();
        if (moved && !isInCache()) {
            moved = setResultPosition();
        }
        return moved;
    }

    @Override
    public boolean first()
        throws SQLException
    {
        boolean first = super.first();
        if (first && !isInCache()) {
            first = setResultPosition();
        }
        return first;
    }

    @Override
    public boolean last()
        throws SQLException
    {
        boolean last = super.last();
        if (last && !isInCache()) {
            last = setResultPosition();
        }
        return last;
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        boolean moved = super.relative(row);
        if (moved && !isCachedRow()) {
            moved = setResultPosition();
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
            BaseRow row = getCurrentRow(index);
            m_WasNull = row.isColumnNull(index);
            if (!m_WasNull) {
                value = (String) row.getColumnObject(index);
            }
        }
        else {
            value = m_Result.getString(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
            BaseRow row = getCurrentRow(index);
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
            super.updateFloat(index, value);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        // XXX: Base using updateDouble() for most numeric SQL types,
        // XXX: it is necessary to convert to the native column type
        if (updateCache()) {
            setColumnDouble(index, value);
        }
        else {
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
        else {
            super.updateBigDecimal(index, value);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        else {
            super.updateString(index, value);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        if (updateCache()) {
            setColumnObject(index, value);
        }
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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
        else {
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

    private boolean isCachedRow()
    {
        return isCachedRow(m_Cursor);
    }

    private boolean isInCache()
    {
        return isUpdated() || isInserted() || isDeleted() || isOnInsertRow();
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
    protected boolean internalNext()
        throws SQLException
    {
        boolean moved = false;
        int count = getRowCount();
        do {
            if (m_Cursor < count) {
                ++m_Cursor;
                moved = true;
            }
            else if (m_Cursor == count) {
                // increment to after last
                ++m_Cursor;
                moved = false;
                break;
            }
        } while (isDeleted());
        /* each call to internalNext may increment cursor m_Cursor multiple
         * times however, the m_Position only increments once per call.
         */
        if (moved) {
            m_Position++;
        }
        else {
            m_Position = getMaxPosition() + 1;
        }
        return moved;
    }

    @Override
    protected boolean internalAbsolute(Integer position, int row)
        throws SQLException
    {
        boolean moved = false;
        try {
            if (isAfterLast() || isBeforeFirst()) {
                return false;
            }
            // XXX: For loading the first row Base use absolute(1) will the current row is beforeFirst.
            boolean cached = isCachedRow();
            moved = cached ? true : setResultPosition();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return moved;
    }

    @Override
    protected BaseRow getCurrentRow(int index)
        throws SQLException
    {
        checkIndex(index);
        if (m_OnInsert) {
            return m_InsertRow;
        }
        checkCursor();
        if (!isInsertedRow() && !isUpdatedRow()) {
            createUpdatedRow();
        }
        return getCachedRow();
    }

    private boolean updateCache()
    {
        return m_InsertRow != null || isOnUpdatableRows() || isOnInsertedRows();
    }

    private boolean isOnUpdatableRows()
    {
        return !m_OnInsert && !m_IsUpdateVisible;
    }

    private boolean isOnInsertedRows()
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
