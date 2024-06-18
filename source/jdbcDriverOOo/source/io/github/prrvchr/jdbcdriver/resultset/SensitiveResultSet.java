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
    // XXX: If the deleted row are not visible then we keep a cache of all deleted rows.
    private List<Integer> m_DeletedRows = new ArrayList<>();
    private Row m_DeletedRow = null;
    // XXX: As long as it has not been completely read, the ResultSet is one row ahead
    // XXX: and in order to avoid unnecessary moves we keep the previous line in cache.
    private int m_Previous = -1;
    private Row m_PreviousRow = null;
    // XXX: If ResultSet is not updatable then we need to emulate the insert row.
    private Row m_InsertRow = null;
    private int m_Position = 0;
    private int m_Cursor = 0;
    private boolean m_RowCountFinal = false;
    private int m_RowCount = 0;
    private boolean m_Moved = false;
    private boolean m_Updatable = false;
    private boolean m_Deleted = false;
    private boolean m_Inserted = false;
    private boolean m_WasNull = false;
    private boolean m_SQLDelete = false;
    private boolean m_SQLInsert = true;
    private boolean m_SQLUpdate = false;
    private boolean m_SQLMode = false;


    // The constructor method:
    public SensitiveResultSet(DriverProvider provider,
                              ResultSet result,
                              String query)
        throws SQLException
    {
        super(provider, result, query);
        int size = m_FetchSize < m_MinSize ? m_MinSize : m_FetchSize;
        m_UpdatedData = new Vector<Row>(size);
        m_DeletedRow = new Row(m_ColumnCount);
        m_SQLDelete = provider.useSQLDelete();
        m_SQLInsert = provider.useSQLInsert();
        m_SQLUpdate = provider.useSQLUpdate();
        m_SQLMode = provider.isSQLMode();
        boolean updatable = provider.isResultSetUpdatable(result);
        if (!updatable) {
            m_Catalog = new RowCatalog(provider, result, query);
            updatable = m_Catalog.hasRowIdentifier();
        }
        m_Updatable = updatable;
        internalNext();
    }


    // XXX: We want to emulate an updateable ResultSet
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
            m_InsertRow = new Row(m_ColumnCount);
        }
        m_OnInsert = true;
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
        if (m_IsInsertVisible) {
            m_Result.moveToCurrentRow();
        }
        else {
            m_InsertRow = null;
        }
        m_OnInsert = false;
    }


    @Override
    public void insertRow()
        throws SQLException
    {
        Row row = null;
        boolean sqlinsert = m_InsertRow != null;
        if (m_SQLMode || m_SQLInsert) {
            row = sqlinsert ? m_InsertRow : getResultRow();
            getRowSetWriter().insertRow(row);
            moveToCurrentRow();
        }
        else if (m_IsInsertVisible){
            super.insertRow();
        }
        else {
            row = sqlinsert ? m_InsertRow : getResultRow();
            if (sqlinsert) {
                RowHelper.setCurrentRow(m_Result, row, m_ColumnCount);
            }
            super.insertRow();
        }
        if (!m_IsInsertVisible) {
            int position = getRowCount() + 1;
            m_UpdatedData.add(row);
            m_UpdatedRows.add(position);
            m_Position = position;
        }
        // XXX: We must be able to respond positively to the insertion ie: rowInserted().
        // XXX: For this, we monitor any movement in the ResultSet
        m_Inserted = true;
        m_Moved = false;
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        // XXX: We can assume the insertion is valid without any
        // XXX: movement in the ResultSet since the insertion.
        boolean inserted = !m_Moved && m_Inserted;
        m_Inserted = false;
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        boolean updated = isUpdatedRow(m_Position);
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
        boolean visible = false;
        boolean updated = isUpdatedRow(m_Position);
        if (updated || m_SQLMode || m_SQLDelete) {
            Row row = updated ? getCachedRow() : getResultRow();
            getRowSetWriter().deleteRow(row);
            visible = m_IsDeleteVisible || isInsertedRow(m_Position);
            // XXX: Are we trying to delete a row that has been inserted or updated and which will be in cache?
            if (updated) {
                deleteCachedRow(m_Position);
            }
        }
        else if (m_IsDeleteVisible) {
            m_Result.deleteRow();
            m_RowCount --;
            visible = true;
        }
        else {
            m_Result.deleteRow();
        }
        // XXX: If delete are not visible, as we are not maintaining a cache on the contents
        // XXX: of the ResultSet here, we need to keep in cache each delete...
        if (!visible) {
            m_DeletedRows.add(m_Position);
        }
        // XXX: We must be able to respond positively to the rowDeleted().
        m_Deleted = visible;
        m_Moved = !visible;
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        boolean deleted = false;
         // XXX: We can assume the delete is valid without any
        // XXX: movement in the ResultSet since the delete.
        deleted = m_Deleted && !m_Moved;
        m_Deleted = false;
        return deleted;
    }


    // XXX: java.sql.ResultSet moover
    @Override
    public boolean next()
        throws SQLException
    {
        m_Moved = true;
        if (isEmpty()) {
            return false;
        }
        if (!m_RowCountFinal && m_Cursor == m_Position + 1) {
            putNextRowInCache();
            return true;
        }
        else {
            return internalAbsolute(m_Position + 1);
        }
    }

    @Override
    public boolean previous()
        throws SQLException 
    {
        m_Moved = true;
        if (isEmpty()) {
            return false;
        }
        return internalAbsolute(m_Position - 1);
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException
    {
        return isEmpty() ? false : m_Position == 0;
    }

    @Override
    public boolean isFirst()
        throws SQLException
    {
        return isEmpty() ? false : m_Position == 1;
    }

    @Override
    public boolean isLast()
        throws SQLException
    {
        return isEmpty() || !m_RowCountFinal ? false : m_Position == getRowCount();
    }

    @Override
    public boolean isAfterLast()
        throws SQLException
    {
        return isEmpty() || !m_RowCountFinal ? false : m_Position == getRowCount() + 1;
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        m_Moved = true;
        m_Position = 0;
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        m_Moved = true;
        if (!m_RowCountFinal) {
            throw new SQLException();
        }
        m_Position = getRowCount() + 1;
    }

    @Override
    public boolean first()
        throws SQLException
    {
        m_Moved = true;
        if (isEmpty()) {
            return false;
        }
        return internalAbsolute(1);
    }

    @Override
    public boolean last()
        throws SQLException
    {
        m_Moved = true;
        if (isEmpty() || !m_RowCountFinal) {
            return false;
        }
        return internalAbsolute(getRowCount());
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        m_Moved = true;
        if (m_Position <= 0 || (m_RowCountFinal && m_Position > getRowCount())) {
            throw new SQLException();
        }
        return internalAbsolute(m_Position + row);
    }

    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        m_Moved = true;
        if (!m_RowCountFinal && row < 0) {
            throw new SQLException();
        }
        if (!m_RowCountFinal && row == m_Cursor && row == m_Position + 1) {
            putNextRowInCache();
            return true;
        }
        if (row < 0) {
            row = getRowCount() + row;
        }
        return internalAbsolute(row);
    }

    @Override
    public int getRow()
        throws SQLException
    {
        return m_RowCountFinal && m_Position > getRowCount() ? 0 : m_Position;
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getByte() 1");
        byte value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getShort() 1");
        short value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getInt() 1");
        int value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getLong() 1");
        long value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getFloat() 1");
        float value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getDouble() 1");
        double value = 0;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getBigDecimal() 1");
        BigDecimal value = null;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
        System.out.println("SensitiveResultSet.getBytes() 1");
        byte[] value = null;
        if (isInCache()) {
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
            Row row = getCurrentRow(index);
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
    private boolean isEmpty()
    {
        return m_RowCount + m_UpdatedData.size() == 0;
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
        return !m_OnInsert && !m_IsInsertVisible && m_RowCountFinal && isInsertedRow(m_Position);
    }

    private boolean isUpdatedRow(Integer row)
    {
        return m_UpdatedRows.contains(row);
    }

    private boolean isInsertedRow(int row)
    {
        return row > m_RowCount;
    }

    private int getRowCount()
        throws SQLException
    {
        // XXX: getRowCount() cannot be called until all rows of the
        // XXX: ResultSet have been scanned and RowCountFinal is true.
        if (!m_RowCountFinal) {
            throw new SQLException();
        }
        int count = m_RowCount;
        if (!m_IsInsertVisible) {
            count += getInsertedRowCount(count);
        }
        return count;
    }

    private int getInsertedRowCount(int max)
    {
        int offset = 0;
        for (int row : m_UpdatedRows) {
            if (row > max) {
                offset ++;
            }
        }
        return offset;
    }

    private void internalNext()
        throws SQLException
    {
        if (m_Result.next()) {
            m_Cursor ++;
            if (!m_RowCountFinal) {
                m_RowCount ++;
            }
        }
        else {
            m_RowCountFinal = true;
        }
    }

    private boolean internalAbsolute(int row)
        throws SQLException
    {
        m_Previous = -1;
        int position = row;
        boolean moved = false;
        if (position == 0) {
            m_Position = 0;
        }
        else if (m_RowCountFinal && position > getRowCount()) {
            m_Position = getRowCount() + 1;
        }
        else if (position <= m_RowCount) {
            moved = m_Result.absolute(position);
            if (moved) {
                m_Cursor = position;
            }
        }
        else if (isInCache(position)) {
            moved = true;
        }
        if (moved) {
            m_Position = position;
        }
        return moved;
    }

    private boolean isInCache()
    {
        return isPrevious() || isUpdated() || isDeleted() || isOnInsertRow();
    }

    private boolean isInCache(int row)
    {
        return isPrevious(row) || isUpdated(row) || isDeleted(row);
    }

    private boolean isPrevious()
    {
        return isPrevious(m_Position);
    }

    private boolean isPrevious(int row)
    {
        return row == m_Previous;
    }

    private boolean isUpdated()
    {
        return isUpdated(m_Position);
    }

    private boolean isUpdated(int row)
    {
        return (!m_IsUpdateVisible || !m_IsInsertVisible) && isUpdatedRow(row);
    }

    private boolean isDeleted()
    {
        return isDeleted(m_Position);
    }

    private boolean isDeleted(int row)
    {
        return !m_IsDeleteVisible && m_DeletedRows.contains(row);
    }

    private Row getCurrentRow(int index)
        throws SQLException
    {
        checkIndex(index);
        if (isPrevious()) {
            return m_PreviousRow;
        }
        if (isUpdated()) {
            return getCachedRow();
        }
        if (isDeleted()) {
            return m_DeletedRow;
        }
        throw new SQLException();
    }

    private void setColumnObject(int index, Object value)
        throws SQLException
    {
        getRowColumn(index).setColumnObject(index, value);;
    }

    private void setColumnDouble(int index, Double value)
        throws SQLException
    {
        int type = m_Result.getMetaData().getColumnType(index);
        getRowColumn(index).setColumnDouble(index, value, type);
    }

    private Row getRowColumn(int index)
        throws SQLException
    {
        Row row = null;
        checkIndex(index);
        if (m_OnInsert) {
            row = m_InsertRow;
        }
        else {
            if (!isUpdatedRow(m_Position)) {
                createCachedRow(m_Position);
            }
            row = getCachedRow();
        }
        return row;
    }

    private void putNextRowInCache()
        throws SQLException
    {
        m_PreviousRow = getResultRow();
        m_Previous = m_Cursor;
        internalNext();
        m_Position ++;
    }

    private void createCachedRow(Integer position)
        throws SQLException
    {
        m_UpdatedRows.add(position);
        m_UpdatedData.add(getResultRow());
    }

    private void deleteCachedRow(Integer position)
        throws SQLException
    {
        m_UpdatedData.remove(m_UpdatedRows.indexOf(position));
        m_UpdatedRows.remove(position);
        if (m_IsDeleteVisible || isInsertedRow(position)) {
            for (int i = 0; i < m_UpdatedRows.size(); i++) {
                int row = m_UpdatedRows.get(i);
                if (row > position) {
                    m_UpdatedRows.set(i, row - 1);
                }
            }
        }
    }

    private Row getCachedRow()
    {
        return m_UpdatedData.get(m_UpdatedRows.indexOf(m_Position));
    }

}
