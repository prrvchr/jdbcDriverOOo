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

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;
import io.github.prrvchr.jdbcdriver.rowset.Row;


public class SensitiveResultSet
    extends CachedResultSet
{

    // XXX: If ResultSet cannot be updated, then we must simulate the insertion row
    private InsertRow m_InsertRow = null;
    // XXX: If ResultSet cannot be updated, then we must simulate the updated row
    private Row m_UpdateRow = null;
    // XXX: If ResultSet cannot be updated, we need to keep reference of last updated row
    private int m_UpdatedRow = 0;
    // XXX: We need to keep some flags to be able to respond to rowDeleted() and rowInserted()
    private boolean m_Moved = false;
    private boolean m_Deleted = false;
    private boolean m_Inserted = false;
    private boolean m_WasNull = false;


    // The constructor method:
    public SensitiveResultSet(DriverProvider provider,
                              ResultSet result,
                              String query)
        throws SQLException
    {
        super(provider, result, query);
    }

    @Override
    public void insertRow()
        throws SQLException
    {
        if (m_IsUpdatable) {
            super.insertRow();
        }
        else if (m_InsertRow != null) {
            // XXX: The result set cannot be updated, the insert
            // XXX: will be done by a SQL command from the cached insert row.
            getRowSetWriter().insertRow(m_InsertRow);
            m_InsertRow = null;
            m_Result.moveToCurrentRow();
        }
        else {
            throw new SQLException();
        }
        // XXX: If insert are not visible, as we are not maintaining a cache on the contents
        // XXX: of the ResultSet here, we need to reload the ResultSet after each insert...
        if (!m_IsInsertVisible) {
            loadResultSet();
        }
        // XXX: If we reload the ResultSet we must be able to respond positively to the insertion
        // XXX: ie: rowInserted(). For this, we monitor any movement in the ResultSet
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
        if (m_IsUpdatable) {
            super.updateRow();
        }
        else if (m_UpdateRow != null) {
            // XXX: The result set cannot be updated, the update
            // XXX: will be done by a SQL command from the current row.
            getRowSetWriter().updateRow(m_UpdateRow);
            m_UpdateRow = null;
            m_UpdatedRow = 0;
        }
        else {
            throw new SQLException();
        }
        // XXX: If update are not visible, as we are not maintaining a cache on the contents
        // XXX: of the ResultSet here, we need to reload the ResultSet after each update...
        if (!m_IsUpdateVisible) {
            loadResultSet();
        }
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        if (m_IsUpdatable) {
            super.deleteRow();
        }
        else {
            // XXX: The result set cannot be updated, the delete
            // XXX: will be done by a SQL command from the current row.
            Row row = createCurrentRow();
            getRowSetWriter().deleteRow(row);
        }
        // XXX: If delete are not visible, as we are not maintaining a cache on the contents
        // XXX: of the ResultSet here, we need to reload the ResultSet after each delete...
        if (!m_IsDeleteVisible) {
            loadResultSet();
        }
        // XXX: If we reload the ResultSet we must be able to respond positively to the delete
        // XXX: ie: rowDeleted(). For this, we monitor any movement in the ResultSet
        m_Deleted = true;
        m_Moved = false;
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        // XXX: We can assume the delete is valid without any
        // XXX: movement in the ResultSet since the delete.
        boolean deleted = !m_Moved && m_Deleted;
        m_Deleted = false;
        return deleted;
    }


    // XXX: java.sql.ResultSet moover
    @Override
    public boolean next()
        throws SQLException
    {
        m_Moved = true;
        return super.next();
    }

    @Override
    public boolean previous()
        throws SQLException
    {
        m_Moved = true;
        return super.previous();
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        m_Moved = true;
        super.beforeFirst();
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        m_Moved = true;
        super.afterLast();
    }

    @Override
    public boolean first()
        throws SQLException
    {
        m_Moved = true;
        return super.first();
    }

    @Override
    public boolean last()
        throws SQLException
    {
        m_Moved = true;
        return super.last();
    }

    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        m_Moved = true;
        return super.absolute(row);
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        m_Moved = true;
        return super.relative(row);
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
        String value;
        if (isInCache()) {
            value = (String) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Boolean value;
        if (isInCache()) {
            value = (Boolean) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Byte value;
        if (isInCache()) {
            value = (Byte) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Short value;
        if (isInCache()) {
            value = (Short) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Integer value;
        if (isInCache()) {
            value = (Integer) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Long value;
        if (isInCache()) {
            value = (Long) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Float value;
        if (isInCache()) {
            value = (Float) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Double value;
        if (isInCache()) {
            value = (Double) getCurrentColumn(index);
            m_WasNull = value == null;
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
        BigDecimal value;
        if (isInCache()) {
            value = (BigDecimal) getCurrentColumn(index);
            m_WasNull = value == null;
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
        byte[] value;
        if (isInCache()) {
            value = (byte[]) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Date value;
        if (isInCache()) {
            value = (Date) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Time value;
        if (isInCache()) {
            value = (Time) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Timestamp value;
        if (isInCache()) {
            value = (Timestamp) getCurrentColumn(index);
            m_WasNull = value == null;
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
        InputStream value;
        if (isInCache()) {
            value = (InputStream) getCurrentColumn(index);
            m_WasNull = value == null;
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
        InputStream value;
        if (isInCache()) {
            value = (InputStream) getCurrentColumn(index);
            m_WasNull = value == null;
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
        InputStream value;
        if (isInCache()) {
            value = (InputStream) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Object value;
        if (isInCache()) {
            value = (Object) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Reader value;
        if (isInCache()) {
            value = (Reader) getCurrentColumn(index);
            m_WasNull = value == null;
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
        BigDecimal value;
        if (isInCache()) {
            value = (BigDecimal) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Object value;
        if (isInCache()) {
            value = (Object) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Ref value;
        if (isInCache()) {
            value = (Ref) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Blob value;
        if (isInCache()) {
             value = (Blob) getCurrentColumn(index);
             m_WasNull = value == null;
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
        Clob value;
        if (isInCache()) {
            value = (Clob) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Array value;
        if (isInCache()) {
            value = (Array) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Date value;
        if (isInCache()) {
            value = (Date) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Time value;
        if (isInCache()) {
            value = (Time) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Timestamp value;
        if (isInCache()) {
            value = (Timestamp) getCurrentColumn(index);
            m_WasNull = value == null;
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
        URL value;
        if (isInCache()) {
            value = (URL) getCurrentColumn(index);
            m_WasNull = value == null;
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
        RowId value;
        if (isInCache()) {
            value = (RowId) getCurrentColumn(index);
            m_WasNull = value == null;
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
        NClob value;
        if (isInCache()) {
            value = (NClob) getCurrentColumn(index);
            m_WasNull = value == null;
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
        SQLXML value;
        if (isInCache()) {
            value = (SQLXML) getCurrentColumn(index);
            m_WasNull = value == null;
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
        String value;
        if (isInCache()) {
            value = (String) getCurrentColumn(index);
            m_WasNull = value == null;
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
        Reader value;
        if (isInCache()) {
            value = (Reader) getCurrentColumn(index);
            m_WasNull = value == null;
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
            value = (T) getCurrentColumn(index);
            m_WasNull = value == null;
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
            setCurrentColumn(index, null);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
        }
        else {
            super.updateFloat(index, value);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        if (updateCache()) {
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
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
            setCurrentColumn(index, value);
        }
        else {
            super.updateRowId(index, value);
        }
    }


    // XXX: Protected overloaded methods
    @Override
    protected boolean isOnInsertRow()
    {
        return m_IsUpdatable ? m_OnInsert : m_InsertRow != null;
    }

    @Override
    protected void setInsertMode(boolean mode)
    {
        super.setInsertMode(mode);
        if (!m_IsUpdatable) {
            m_InsertRow = mode ? new InsertRow(m_ColumnCount) : null;
        }
    }


    // XXX: Private methods
    private boolean isInCache()
        throws SQLException
    {
        return !m_IsUpdatable && (isOnInsertRow() || m_UpdatedRow == getRow());
    }

    private boolean updateCache()
    {
        return !m_IsUpdatable;
    }

    private Object getCurrentColumn(int index)
        throws SQLException
    {
        checkIndex(index);
        if (isOnInsertRow()) {
            return m_InsertRow.getColumnObject(index);
        }
        return m_UpdateRow;
    }

    private void setCurrentColumn(int index, Object value)
        throws SQLException
    {
        checkIndex(index);
        if (isOnInsertRow()) {
            m_InsertRow.setColumnObject(index, value);
        }
        else {
            int row = getRow();
            if (m_UpdatedRow != row) {
                m_UpdateRow = createCurrentRow();
                m_UpdatedRow = row;
            }
            m_UpdateRow.setColumnObject(index, value);
        }
    }

    private void loadResultSet()
        throws SQLException
    {
        ResultSet result = getResultSet();
        if (result != null) {
            m_Result.close();
            m_Result = result;
        }
    }

}
