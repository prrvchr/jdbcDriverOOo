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
import io.github.prrvchr.jdbcdriver.rowset.BaseRow;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;


public class ScrollableResultSet
    extends CachedResultSet
{
    // XXX: We keep a cache of all the ResultSet rows
    private Vector<BaseRow> m_RowData = null;
    // XXX: If ResultSet cannot be updated, then we must simulate the insertion row
    private InsertRow m_InsertRow = null;
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
    }

    @Override
    public void insertRow()
        throws SQLException
    {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !");
        }
        // XXX: The result set cannot be updated, the insert
        // XXX: will be done by a SQL command from the cached insert row.
        BaseRow row = getCurrentRow();
        getRowSetWriter().insertRow(row);
        setInsertMode(false);
        // XXX: If we reload the ResultSet we must be able to respond positively to the insert
        m_InsertedRow = m_RowCount + 1;
        m_CurrentRow = m_InsertedRow;
        // XXX: If we want to be able to correctly display the auto-increments
        // XXX:  it is necessary to reload the ResultSet.
        loadResultSet();
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
        BaseRow row = getCurrentRow();
        getRowSetWriter().updateRow(row);
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
        BaseRow row = getCurrentRow();
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
        System.out.println("ScrollableResultSet.getRow() 1 Row: " + row);
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
            System.out.println("ScrollableResultSet.absolute() 1 RowCount: " + m_RowCount + " - Row: " + row);
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
        System.out.println("ScrollableResultSet.getString() 1 OnInsert: " + isOnInsertRow());
        String value = (String) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        Boolean value = (Boolean) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        Byte value = (Byte) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        Short value = (Short) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        Integer value = (Integer) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        Long value = (Long) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        Float value = (Float) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        Double value = (Double) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException
    {
        BigDecimal value = (BigDecimal) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        byte[] value = (byte[]) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        Date value = (Date) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        Time value = (Time) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException
    {
        Timestamp value = (Timestamp) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException
    {
        InputStream value = (InputStream) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public InputStream getUnicodeStream(int index)
        throws SQLException
    {
        InputStream value = (InputStream) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException
    {
        InputStream value = (InputStream) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Object getObject(int index)
        throws SQLException
    {
        Object value = (Object) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException
    {
        Reader value = (Reader) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException
    {
        BigDecimal value = (BigDecimal) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException
    {
        Object value = (Object) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException
    {
        Ref value = (Ref) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException
    {
        Blob value = (Blob) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException
    {
        Clob value = (Clob) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Array getArray(int index)
        throws SQLException
    {
        Array value = (Array) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException
    {
        Date value = (Date) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException
    {
        Time value = (Time) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException
    {
        Timestamp value = (Timestamp) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public URL getURL(int index)
        throws SQLException
    {
        URL value = (URL) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException
    {
        RowId value = (RowId) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException
    {
        NClob value = (NClob) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException
    {
        SQLXML value = (SQLXML) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public String getNString(int index)
        throws SQLException
    {
        String value = (String) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException
    {
        Reader value = (Reader) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }

    @Override
    public <T> T getObject(int index, Class<T> type)
        throws SQLException
    {
        @SuppressWarnings("unchecked")
        T value = (T) getCurrentColumn(index);
        m_WasNull = value == null;
        return value;
    }


    // XXX: java.sql.ResultSet updater
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        setCurrentColumn(index, null);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);

    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        setCurrentColumn(index, value);

    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        setCurrentColumn(index, value);

    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        setCurrentColumn(index, value);

    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }


    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        setCurrentColumn(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        setCurrentColumn(index, value);
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
        m_InsertRow = mode ? new InsertRow(m_ColumnCount) : null;
    }


    // XXX: Private methods
    private boolean isEmpty()
    {
        return m_RowCount == 0;
    }

    protected void setCurrentColumn(int index, Object value)
        throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    protected Object getCurrentColumn(int index)
        throws SQLException
    {
        checkCursor();
        checkIndex(index);
        return getCurrentRow().getColumnObject(index);
    }

    private BaseRow getCurrentRow()
    {
        try {
            if (isOnInsertRow()) {
                return (BaseRow) m_InsertRow;
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
        getRowData().add(createCurrentRow());
    }

    private Vector<BaseRow> getRowData()
    {
        if (m_RowData == null) {
            int size = m_FetchSize < m_MinSize ? m_MinSize : m_FetchSize;
            m_RowData = new Vector<BaseRow>(size);
        }
        return m_RowData;
    }

    private void loadResultSet()
            throws SQLException
        {
            ResultSet result = getResultSet();
            if (result != null) {
                m_RowCountFinal = false;
                m_RowCount = 0;
                getRowData().clear();
                m_Result.close();
                m_Result = result;
                while (!m_RowCountFinal) {
                    internalNext();
                }
            }
        }

}
