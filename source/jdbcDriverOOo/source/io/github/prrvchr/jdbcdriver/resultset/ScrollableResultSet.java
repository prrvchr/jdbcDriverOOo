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
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.Row;


//XXX: This ResultSet is supposed to emulate a TYPE_SCROLL_SENSITIVE from a TYPE_FORWARD_ONLY
//XXX: SQL DML commands will be used (ie: INSERT, DELETE, UPDATE...) instead of positioned updates.
public class ScrollableResultSet
    extends CachedResultSet
{
    // XXX: We keep a cache of all the ResultSet rows
    private Vector<Row> m_RowData = null;
    // XXX: If ResultSet cannot be updated, then we must simulate the insertion row
    private Row m_InsertRow = null;
    private int m_RowCount = 0;
    private boolean m_RowCountFinal = false;
    private int m_CurrentRow = 0;
    private int m_DeletedRow = 0;
    private int m_InsertedRow = 0;
    private boolean m_WasNull = false;

    // The constructor method:
    public ScrollableResultSet(DriverProvider provider,
                               ResultSet result,
                               String query)
        throws SQLException
    {
        super(provider, result, query);
        m_IsUpdatable = false;
        initCache(result);
        System.out.println("ScrollableResultSet() 1");
    }

    // XXX: We want to emulate an updateable ResultSet
    @Override
    public int getConcurrency()
        throws SQLException
    {
        return ResultSet.CONCUR_UPDATABLE;
    }

    // XXX: We want to emulate an scollable ResultSet
    @Override
    public int getType()
        throws SQLException
    {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }


    @Override
    public void insertRow()
        throws SQLException
    {
        System.out.println("ScollableResultSet.insertRow() 1");
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !");
        }
        // XXX: The result set cannot be updated, the insert
        // XXX: will be done by a SQL command from the cached insert row.
        Row row = getCurrentRow();
        getRowSetWriter().insertRow(row);
        getRowData().add(row);
        m_RowCount ++;
        setInsertMode(false);
        // XXX: We must be able to respond positively to the insert
        m_InsertedRow = m_RowCount;
        m_CurrentRow = m_InsertedRow;
        System.out.println("ScollableResultSet.insertRow() 2");
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        boolean inserted = m_InsertedRow == m_CurrentRow;
        m_InsertedRow = 0;
        return inserted;
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: updateRow() cannot be called when moveToInsertRow() has been called!");
        }
        // XXX: The result set cannot be updated, the update
        // XXX: will be done by a SQL command from the cached current row.
        Row row = getCurrentRow();
        if (row.isUpdated()) {
            getRowSetWriter().updateRow(row);
        }
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        if (isOnInsertRow()) {
            throw new SQLException("ERROR: deleteRow() cannot be called when moveToInsertRow() has been called!");
        }
        // XXX: The result set cannot be updated, the delete
        // XXX: will be done by a SQL command from the current row.
        Row row = getCurrentRow();
        getRowSetWriter().deleteRow(row);
        getRowData().remove(m_CurrentRow - 1);
        m_RowCount --;
        // XXX: We must be able to respond positively to the delete
        m_DeletedRow = m_CurrentRow;
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        boolean deleted = m_DeletedRow == m_CurrentRow;
        m_DeletedRow = 0;
        return deleted;
    }


    // XXX: java.sql.ResultSet moover
    @Override
    public boolean next()
        throws SQLException
    {
        if (isEmpty()) {
            return false;
        }
        if (!m_RowCountFinal) {
            internalNext();
        }
        boolean next = false;
        if (m_RowCountFinal && m_CurrentRow >= m_RowCount) {
            m_CurrentRow = m_RowCount + 1;
        }
        else {
            m_CurrentRow ++;
            next = true;
        }
        return next;
    }

    @Override
    public boolean previous()
        throws SQLException
    {
        if (isEmpty()) {
            return false;
        }
        boolean previous = false;
        if (m_CurrentRow > 0) {
            m_CurrentRow --;
            previous = m_CurrentRow > 0;
        }
        return previous;
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException
    {
        return isEmpty() ? false : m_CurrentRow == 0;
    }

    @Override
    public boolean isAfterLast()
        throws SQLException
    {
        return isEmpty() || !m_RowCountFinal ? false : m_CurrentRow == m_RowCount + 1;
    }

    @Override
    public boolean isFirst()
        throws SQLException
    {
        return isEmpty() ? false : m_CurrentRow == 1;
    }

    @Override
    public boolean isLast()
        throws SQLException
    {
        return isEmpty() || !m_RowCountFinal ? false : m_CurrentRow == m_RowCount;
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        m_CurrentRow = 0;
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        if (m_RowCountFinal) {
            m_CurrentRow = m_RowCount + 1;
        }
    }

    @Override
    public boolean first()
        throws SQLException
    {
        if (isEmpty()) {
            return false;
        }
        m_CurrentRow = 1;
        return true;
    }

    @Override
    public boolean last()
        throws SQLException
    {
        if (isEmpty() || !m_RowCountFinal) {
            return false;
        }
        m_CurrentRow = m_RowCount;
        return true;
    }

    @Override
    public int getRow()
        throws SQLException
    {
        int row = 0;
        if (!isOnInsertRow() && m_CurrentRow > 0 && (!m_RowCountFinal || m_CurrentRow <= m_RowCount)) {
            row = m_CurrentRow;
        }
        return row;
    }

    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        boolean moved = false;
        if (row < 0) {
            if (m_RowCountFinal) {
                int absolute = m_RowCount + row;
                moved = absolute > 0;
                m_CurrentRow = moved ? absolute : 0;
            }
            else {
                throw new SQLException("ERROR: absolute() cannot be called with a negative value");
            }
        }
        else {
            while (!m_RowCountFinal && m_RowCount <= row) {
                internalNext();
            }
            if (m_RowCountFinal && row > m_RowCount) {
                m_CurrentRow = m_RowCount + 1;
            }
            else {
                m_CurrentRow = row;
                moved = m_CurrentRow != 0;
            }
        }
        return moved;
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        boolean moved = false;
        if (row != 0) {
            int absolute = m_CurrentRow + row;
            if (absolute <= 0) {
                m_CurrentRow = 0;
            }
            else if (m_RowCountFinal && absolute > m_RowCount) {
                m_CurrentRow = m_RowCount + 1;
            }
            else {
                m_CurrentRow = absolute;
                moved = true;
            }
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
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (String) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        boolean value = false;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Boolean) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        byte value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Byte) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        short value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Short) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        int value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Integer) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        long value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Long) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        float value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Float) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        double value = 0;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Double) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException
    {
        BigDecimal value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (BigDecimal) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        byte[] value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (byte[]) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        Date value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Date) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        Time value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Time) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException
    {
        Timestamp value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Timestamp) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException
    {
        InputStream value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getUnicodeStream(int index)
        throws SQLException
    {
        InputStream value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException
    {
        InputStream value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (InputStream) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Object getObject(int index)
        throws SQLException
    {
        Object value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Object) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException
    {
        Reader value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Reader) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException
    {
        BigDecimal value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (BigDecimal) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException
    {
        Object value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Object) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException
    {
        Ref value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Ref) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException
    {
        Blob value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Blob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException
    {
        Clob value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Clob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Array getArray(int index)
        throws SQLException
    {
        Array value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Array) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException
    {
        Date value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Date) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException
    {
        Time value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Time) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException
    {
        Timestamp value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Timestamp) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public URL getURL(int index)
        throws SQLException
    {
        URL value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (URL) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException
    {
        RowId value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (RowId) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException
    {
        NClob value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (NClob) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException
    {
        SQLXML value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (SQLXML) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public String getNString(int index)
        throws SQLException
    {
        String value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (String) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException
    {
        Reader value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (Reader) row.getColumnObject(index);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int index, Class<T> type)
        throws SQLException
    {
        T value = null;
        Row row = getCurrentRow(index);
        m_WasNull = row.isColumnNull(index);
        if (!m_WasNull) {
            value = (T) row.getColumnObject(index);
        }
        return value;
    }


    // XXX: java.sql.ResultSet updater
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        setColumnObject(index, null);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        // XXX: Base using updateDouble() for most numeric SQL types,
        // XXX: it is necessary to convert to the native column type
        setColumnDouble(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        setColumnObject(index, value);

    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        setColumnObject(index, value);

    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        setColumnObject(index, value);

    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        setColumnObject(index, value);

    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        setColumnObject(index, value);
    }


    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        setColumnObject(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        setColumnObject(index, value);
    }


    // XXX: Protected overloaded methods
    @Override
    protected boolean isOnInsertRow()
    {
        return m_InsertRow != null;
    }

    @Override
    protected void setInsertMode(boolean mode)
    {
        super.setInsertMode(mode);
        m_InsertRow = mode ? new Row(m_ColumnCount) : null;
    }


    // XXX: Private methods
    private boolean isEmpty()
    {
        return m_RowCount == 0;
    }

    private void setColumnObject(int index, Object value)
        throws SQLException
    {
        getCurrentRow(index).setColumnObject(index, value);
    }


    private void setColumnDouble(int index, Double value)
        throws SQLException
    {
        int type = m_Result.getMetaData().getColumnType(index);
        getCurrentRow(index).setColumnDouble(index, value, type);
    }

    private Row getCurrentRow(int index)
        throws SQLException
    {
        checkIndex(index);
        checkCursor();
        return getCurrentRow();
    }

    private Row getCurrentRow()
    {
        try {
            if (isOnInsertRow()) {
                return m_InsertRow;
            }
            else {
                return getRowData().get(m_CurrentRow - 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkCursor()
        throws SQLException
    {
        if (!isOnInsertRow() && (m_CurrentRow <= 0 || (m_RowCountFinal && m_CurrentRow > m_RowCount))) {
            throw new SQLException("ERROR Row is out of range");
        }
    }

    private void initCache(ResultSet result)
            throws SQLException, SQLException
        {
            m_RowCountFinal = false;
            m_RowCount = 0;
            m_Result = result;
            internalNext();
        }

    private void internalNext()
        throws SQLException, SQLException
    {
        if (m_Result.next()) {
            m_RowCount ++;
            setCurrentRow();
        }
        else {
            m_RowCountFinal = true;
        }
    }

    private void setCurrentRow()
        throws SQLException
    {
        getRowData().add(getResultRow());
    }

    private Vector<Row> getRowData()
    {
        if (m_RowData == null) {
            int size = m_FetchSize < m_MinSize ? m_MinSize : m_FetchSize;
            m_RowData = new Vector<Row>(size);
        }
        return m_RowData;
    }

}
