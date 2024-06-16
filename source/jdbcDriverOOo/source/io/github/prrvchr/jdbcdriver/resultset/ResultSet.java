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
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;


abstract public class ResultSet
    implements java.sql.ResultSet
{

    protected java.sql.ResultSet m_Result;

    // The constructor method:
    public ResultSet(java.sql.ResultSet result)
        throws SQLException
    {
        m_Result = result;
        System.out.println("ResultSet() 1");
    }


    @Override
    public void insertRow()
        throws SQLException
    {
        m_Result.insertRow();
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException
    {
        m_Result.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        m_Result.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        m_Result.moveToCurrentRow();
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
    public Statement getStatement()
        throws SQLException
    {
        return m_Result.getStatement();
    }

    @Override
    public SQLWarning getWarnings()
        throws SQLException
    {
        return m_Result.getWarnings();
    }

    @Override
    public void clearWarnings()
        throws SQLException
    {
        m_Result.clearWarnings();
    }

    @Override
    public String getCursorName()
        throws SQLException
    {
        return m_Result.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData()
        throws SQLException
    {
        return m_Result.getMetaData();
    }

    @Override
    public int findColumn(String label)
        throws SQLException
    {
        return m_Result.findColumn(label);
    }

    @Override
    public void setFetchDirection(int direction)
        throws SQLException
    {
        m_Result.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection()
        throws SQLException
    {
        return m_Result.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows)
        throws SQLException
    {
        m_Result.setFetchSize(rows);
    }

    @Override
    public int getFetchSize()
        throws SQLException
    {
        return m_Result.getFetchSize();
    }

    @Override
    public int getHoldability()
        throws SQLException
    {
        return m_Result.getHoldability();
    }

    @Override
    public boolean isClosed()
        throws SQLException
    {
        return m_Result.isClosed();
    }

    @Override
    public boolean rowUpdated()
        throws SQLException
    {
        return m_Result.rowUpdated();
    }

    @Override
    public boolean rowInserted()
        throws SQLException
    {
        return m_Result.rowInserted();
    }

    @Override
    public boolean rowDeleted()
        throws SQLException
    {
        return m_Result.rowDeleted();
    }

    @Override
    public void updateRow()
        throws SQLException
    {
        m_Result.updateRow();
    }

    @Override
    public void deleteRow()
        throws SQLException
    {
        m_Result.deleteRow();
    }

    @Override
    public void refreshRow()
        throws SQLException
    {
        m_Result.refreshRow();
    }


    // XXX: java.sql.ResultSet moover
    @Override
    public boolean next()
        throws SQLException
    {
        return m_Result.next();
    }

    @Override
    public boolean previous()
        throws SQLException
    {
        return m_Result.previous();
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException
    {
        return m_Result.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast()
        throws SQLException
    {
        return m_Result.isAfterLast();
    }

    @Override
    public boolean isFirst()
        throws SQLException
    {
        return m_Result.isFirst();
    }

    @Override
    public boolean isLast()
        throws SQLException
    {
        return m_Result.isLast();
    }

    @Override
    public void beforeFirst()
        throws SQLException
    {
        m_Result.beforeFirst();
    }

    @Override
    public void afterLast()
        throws SQLException
    {
        m_Result.afterLast();
    }

    @Override
    public boolean first()
        throws SQLException
    {
        return m_Result.first();
    }

    @Override
    public boolean last()
        throws SQLException
    {
        return m_Result.last();
    }

    @Override
    public int getRow()
        throws SQLException
    {
        return m_Result.getRow();
    }

    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        return m_Result.absolute(row);
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        return m_Result.relative(row);
    }

    @Override
    public <T> T unwrap(Class<T> iface)
        throws SQLException
    {
        return m_Result.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException
    {
        return m_Result.isWrapperFor(iface);
    }

    @Override
    public void close()
        throws SQLException
    {
        m_Result.close();
    }


    // XXX: java.sql.ResultSet getter by index
    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_Result.wasNull();
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        return m_Result.getString(index);
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        return m_Result.getBoolean(index);
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        return m_Result.getByte(index);
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        return m_Result.getShort(index);
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        return m_Result.getInt(index);
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        return m_Result.getLong(index);
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        return m_Result.getFloat(index);
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        return m_Result.getDouble(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException
    {   
        return m_Result.getBigDecimal(index);
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        return m_Result.getBytes(index);
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        return m_Result.getDate(index);
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        return m_Result.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException
    {
        return m_Result.getTimestamp(index);
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException
    {
        return m_Result.getAsciiStream(index);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int index)
        throws SQLException
    {
        return m_Result.getUnicodeStream(index);
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException
    {
        return m_Result.getBinaryStream(index);
    }

    @Override
    public Object getObject(int index)
        throws SQLException
    {
        return m_Result.getObject(index);
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException
    {
        return m_Result.getCharacterStream(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException
    {
        return m_Result.getBigDecimal(index);
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException
    {
        return m_Result.getObject(index, map);
    }

    @Override
    public Ref getRef(int index)
        throws SQLException
    {
        return m_Result.getRef(index);
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException
    {
        return m_Result.getBlob(index);
    }

    @Override
    public Clob getClob(int index)
        throws SQLException
    {
        return m_Result.getClob(index);
    }

    @Override
    public Array getArray(int index)
        throws SQLException
    {
        return m_Result.getArray(index);
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException
    {
        return m_Result.getDate(index);
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException
    {
        return m_Result.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException
    {
        return m_Result.getTimestamp(index);
    }

    @Override
    public URL getURL(int index)
        throws SQLException
    {
        return m_Result.getURL(index);
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException
    {
        return m_Result.getRowId(index);
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException
    {
        return m_Result.getNClob(index);
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException
    {
        return m_Result.getSQLXML(index);
    }

    @Override
    public String getNString(int index)
        throws SQLException
    {
        return m_Result.getNString(index);
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException
    {
        return m_Result.getNCharacterStream(index);
    }

    @Override
    public <T> T getObject(int index, Class<T> type)
        throws SQLException
    {
        return m_Result.getObject(index, type);
    }

    // XXX: java.sql.ResultSet getter by label
    @Override
    public String getString(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getString(index);
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getBoolean(index);
    }

    @Override
    public byte getByte(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getByte(index);
    }

    @Override
    public short getShort(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getShort(index);
    }

    @Override
    public int getInt(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getInt(index);
    }

    @Override
    public long getLong(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getLong(index);
    }

    @Override
    public float getFloat(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getFloat(index);
    }

    @Override
    public double getDouble(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getDouble(index);
    }

    @Override
    public BigDecimal getBigDecimal(String label, int scale)
        throws SQLException
    {
        int index = findColumn(label);
        return getBigDecimal(index);
    }

    @Override
    public byte[] getBytes(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getBytes(index);
    }

    @Override
    public Date getDate(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getDate(index);
    }

    @Override
    public Time getTime(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getTime(index);
    }

    @Override
    public Timestamp getTimestamp(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getTimestamp(index);
    }

    @Override
    public InputStream getAsciiStream(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getAsciiStream(index);
    }

    @Override
    public InputStream getUnicodeStream(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getUnicodeStream(index);
    }

    @Override
    public InputStream getBinaryStream(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getBinaryStream(index);
    }

    @Override
    public Object getObject(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getObject(index);
    }

    @Override
    public Reader getCharacterStream(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getCharacterStream(index);
    }

    @Override
    public BigDecimal getBigDecimal(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getBigDecimal(index);
    }

    @Override
    public Object getObject(String label, Map<String, Class<?>> map)
        throws SQLException
    {
        int index = findColumn(label);
        return getObject(index, map);
    }

    @Override
    public Ref getRef(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getRef(index);
    }

    @Override
    public Blob getBlob(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getBlob(index);
    }

    @Override
    public Clob getClob(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getClob(index);
    }

    @Override
    public Array getArray(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getArray(index);
    }

    @Override
    public Date getDate(String label, Calendar cal)
        throws SQLException
    {
        int index = findColumn(label);
        return getDate(index, cal);
    }

    @Override
    public Time getTime(String label, Calendar cal)
        throws SQLException
    {
        int index = findColumn(label);
        return getTime(index, cal);
    }

    @Override
    public Timestamp getTimestamp(String label, Calendar cal)
        throws SQLException
    {
        int index = findColumn(label);
        return getTimestamp(index, cal);
    }

    @Override
    public URL getURL(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getURL(index);
    }

    @Override
    public RowId getRowId(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getRowId(index);
    }

    @Override
    public NClob getNClob(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getNClob(index);
    }

    @Override
    public SQLXML getSQLXML(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getSQLXML(index);
    }

    @Override
    public String getNString(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getNString(index);
    }

    @Override
    public Reader getNCharacterStream(String label)
        throws SQLException
    {
        int index = findColumn(label);
        return getNCharacterStream(index);
    }

    @Override
    public <T> T getObject(String label, Class<T> type)
        throws SQLException
    {
        int index = findColumn(label);
        return getObject(index, type);
    }


    // XXX: java.sql.ResultSet updater by index
    @Override
    public void updateNull(int index)
        throws SQLException
    {
        m_Result.updateNull(index);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        m_Result.updateBoolean(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        m_Result.updateByte(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        m_Result.updateShort(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        m_Result.updateInt(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        m_Result.updateLong(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        m_Result.updateFloat(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        m_Result.updateDouble(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        m_Result.updateBigDecimal(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        m_Result.updateString(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        m_Result.updateBytes(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        m_Result.updateDate(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        m_Result.updateTime(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        m_Result.updateTimestamp(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        m_Result.updateObject(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        m_Result.updateObject(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        m_Result.updateRef(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        m_Result.updateArray(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value, length);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        m_Result.updateBlob(index, value, length);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateClob(index, value, length);
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNClob(index, value, length);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        m_Result.updateSQLXML(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        m_Result.updateNString(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        m_Result.updateRowId(index, value);
    }

    // XXX: java.sql.ResultSet updater by label
    @Override
    public void updateNull(String label)
        throws SQLException
    {
        int index = findColumn(label);
        updateNull(index);
    }

    @Override
    public void updateBoolean(String label, boolean value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBoolean(index ,value);
    }

    @Override
    public void updateByte(String label, byte value)
        throws SQLException
    {
        int index = findColumn(label);
        updateByte(index, value);
    }

    @Override
    public void updateShort(String label, short value)
        throws SQLException
    {
        int index = findColumn(label);
        updateShort(index, value);
    }

    @Override
    public void updateInt(String label, int value)
        throws SQLException
    {
        int index = findColumn(label);
        updateInt(index, value);
    }

    @Override
    public void updateLong(String label, long value)
        throws SQLException
    {
        int index = findColumn(label);
        updateLong(index, value);
    }

    @Override
    public void updateFloat(String label, float value)
        throws SQLException
    {
        int index = findColumn(label);
        updateFloat(index, value);
    }

    @Override
    public void updateDouble(String label, double value)
        throws SQLException
    {
        int index = findColumn(label);
        updateDouble(index, value);
    }

    @Override
    public void updateBigDecimal(String label, BigDecimal value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBigDecimal(index, value);
    }

    @Override
    public void updateString(String label, String value)
        throws SQLException
    {
        int index = findColumn(label);
        updateString(index, value);
    }

    @Override
    public void updateBytes(String label, byte[] value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBytes(index, value);
    }

    @Override
    public void updateDate(String label, Date value)
        throws SQLException
    {
        int index = findColumn(label);
        updateDate(index, value);
    }

    @Override
    public void updateTime(String label, Time value)
        throws SQLException
    {
        int index = findColumn(label);
        updateTime(index, value);
    }

    @Override
    public void updateTimestamp(String label, Timestamp value)
        throws SQLException
    {
        int index = findColumn(label);
        updateTimestamp(index, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, int length)
        throws SQLException
    {
        int index = findColumn(label);
        updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, int length)
        throws SQLException
    {
        int index = findColumn(label);
        updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, int length)
        throws SQLException
    {
        int index = findColumn(label);
        updateCharacterStream(index, value, length);
    }

    @Override
    public void updateObject(String label, Object value, int length)
        throws SQLException
    {
        int index = findColumn(label);
        updateObject(index, value, length);
    }

    @Override
    public void updateObject(String label, Object value)
        throws SQLException
    {
        int index = findColumn(label);
        updateObject(index, value);
    }

    @Override
    public void updateRef(String label, Ref value)
        throws SQLException
    {
        int index = findColumn(label);
        updateRef(index, value);
    }

    @Override
    public void updateBlob(String label, Blob value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBlob(index, value);
    }

    @Override
    public void updateClob(String label, Clob value)
        throws SQLException
    {
        int index = findColumn(label);
        updateClob(index, value);
    }

    @Override
    public void updateArray(String label, Array value)
        throws SQLException
    {
        int index = findColumn(label);
        updateArray(index, value);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateNCharacterStream(index, value, length);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateCharacterStream(index, value, length);
    }

    @Override
    public void updateBlob(String label, InputStream value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateBlob(index, value, length);
    }

    @Override
    public void updateClob(String label, Reader value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateClob(index, value, length);
    }

    @Override
    public void updateNClob(String label, Reader value, long length)
        throws SQLException
    {
        int index = findColumn(label);
        updateNClob(index, value, length);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value)
        throws SQLException
    {
        int index = findColumn(label);
        updateNCharacterStream(index, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value)
        throws SQLException
    {
        int index = findColumn(label);
        updateAsciiStream(index, value);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBinaryStream(index, value);
    }

    @Override
    public void updateCharacterStream(String label, Reader value)
        throws SQLException
    {
        int index = findColumn(label);
        updateCharacterStream(index, value);
    }

    @Override
    public void updateBlob(String label, InputStream value)
        throws SQLException
    {
        int index = findColumn(label);
        updateBlob(index, value);
    }

    @Override
    public void updateClob(String label, Reader value)
        throws SQLException
    {
        int index = findColumn(label);
        updateClob(index, value);
    }

    @Override
    public void updateNClob(String label, Reader value)
        throws SQLException
    {
        int index = findColumn(label);
        updateNClob(index, value);
    }

    @Override
    public void updateSQLXML(String label, SQLXML value)
        throws SQLException
    {
        int index = findColumn(label);
        updateSQLXML(index, value);
    }

    @Override
    public void updateNString(String label, String value)
        throws SQLException
    {
        int index = findColumn(label);
        updateNString(index, value);
    }

    @Override
    public void updateNClob(String label, NClob value)
        throws SQLException
    {
        int index = findColumn(label);
        updateNClob(index, value);
    }

    @Override
    public void updateRowId(String label, RowId value)
        throws SQLException
    {
        int index = findColumn(label);
        updateRowId(index, value);
    }

}
