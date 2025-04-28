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


public abstract class ResultSet
    implements java.sql.ResultSet {

    protected java.sql.ResultSet mResult;

    // The constructor method:
    public ResultSet(java.sql.ResultSet result)
        throws SQLException {
        mResult = result;
    }


    @Override
    public void insertRow()
        throws SQLException {
        mResult.insertRow();
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException {
        mResult.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow()
        throws SQLException {
        mResult.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        mResult.moveToCurrentRow();
    }

    @Override
    public int getConcurrency()
        throws SQLException {
        return mResult.getConcurrency();
    }

    @Override
    public int getType()
        throws SQLException {
        return mResult.getType();
    }

    @Override
    public Statement getStatement()
        throws SQLException {
        return mResult.getStatement();
    }

    @Override
    public SQLWarning getWarnings()
        throws SQLException {
        return mResult.getWarnings();
    }

    @Override
    public void clearWarnings()
        throws SQLException {
        mResult.clearWarnings();
    }

    @Override
    public String getCursorName()
        throws SQLException {
        return mResult.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData()
        throws SQLException {
        return mResult.getMetaData();
    }

    @Override
    public int findColumn(String label)
        throws SQLException {
        return mResult.findColumn(label);
    }

    @Override
    public void setFetchDirection(int direction)
        throws SQLException {
        mResult.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection()
        throws SQLException {
        return mResult.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows)
        throws SQLException {
        mResult.setFetchSize(rows);
    }

    @Override
    public int getFetchSize()
        throws SQLException {
        return mResult.getFetchSize();
    }

    @Override
    public int getHoldability()
        throws SQLException {
        return mResult.getHoldability();
    }

    @Override
    public boolean isClosed()
        throws SQLException {
        return mResult.isClosed();
    }

    @Override
    public boolean rowUpdated()
        throws SQLException {
        return mResult.rowUpdated();
    }

    @Override
    public boolean rowInserted()
        throws SQLException {
        return mResult.rowInserted();
    }

    @Override
    public boolean rowDeleted()
        throws SQLException {
        return mResult.rowDeleted();
    }

    @Override
    public void updateRow()
        throws SQLException {
        mResult.updateRow();
    }

    @Override
    public void deleteRow()
        throws SQLException {
        mResult.deleteRow();
    }

    @Override
    public void refreshRow()
        throws SQLException {
        mResult.refreshRow();
    }


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException {
        return mResult.next();
    }

    @Override
    public boolean previous()
        throws SQLException {
        return mResult.previous();
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException {
        return mResult.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast()
        throws SQLException {
        return mResult.isAfterLast();
    }

    @Override
    public boolean isFirst()
        throws SQLException {
        return mResult.isFirst();
    }

    @Override
    public boolean isLast()
        throws SQLException {
        return mResult.isLast();
    }

    @Override
    public void beforeFirst()
        throws SQLException {
        mResult.beforeFirst();
    }

    @Override
    public void afterLast()
        throws SQLException {
        mResult.afterLast();
    }

    @Override
    public boolean first()
        throws SQLException {
        return mResult.first();
    }

    @Override
    public boolean last()
        throws SQLException {
        return mResult.last();
    }

    @Override
    public int getRow()
        throws SQLException {
        return mResult.getRow();
    }

    @Override
    public boolean absolute(int row)
        throws SQLException {
        return mResult.absolute(row);
    }

    @Override
    public boolean relative(int row)
        throws SQLException {
        return mResult.relative(row);
    }

    @Override
    public <T> T unwrap(Class<T> iface)
        throws SQLException {
        return mResult.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return mResult.isWrapperFor(iface);
    }

    @Override
    public void close()
        throws SQLException {
        mResult.close();
    }


    // XXX: java.sql.ResultSet getter by index
    @Override
    public boolean wasNull()
        throws SQLException {
        return mResult.wasNull();
    }

    @Override
    public String getString(int index)
        throws SQLException {
        return mResult.getString(index);
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException {
        return mResult.getBoolean(index);
    }

    @Override
    public byte getByte(int index)
        throws SQLException {
        return mResult.getByte(index);
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        return mResult.getShort(index);
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        return mResult.getInt(index);
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        return mResult.getLong(index);
    }

    @Override
    public float getFloat(int index)
        throws SQLException {
        return mResult.getFloat(index);
    }

    @Override
    public double getDouble(int index)
        throws SQLException {
        return mResult.getDouble(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException {
        return mResult.getBigDecimal(index);
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException {
        return mResult.getBytes(index);
    }

    @Override
    public Date getDate(int index)
        throws SQLException {
        return mResult.getDate(index);
    }

    @Override
    public Time getTime(int index)
        throws SQLException {
        return mResult.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException {
        return mResult.getTimestamp(index);
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException {
        return mResult.getAsciiStream(index);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int index)
        throws SQLException {
        return mResult.getUnicodeStream(index);
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException {
        return mResult.getBinaryStream(index);
    }

    @Override
    public Object getObject(int index)
        throws SQLException {
        return mResult.getObject(index);
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException {
        return mResult.getCharacterStream(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException {
        return mResult.getBigDecimal(index);
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException {
        return mResult.getObject(index, map);
    }

    @Override
    public Ref getRef(int index)
        throws SQLException {
        return mResult.getRef(index);
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException {
        return mResult.getBlob(index);
    }

    @Override
    public Clob getClob(int index)
        throws SQLException {
        return mResult.getClob(index);
    }

    @Override
    public Array getArray(int index)
        throws SQLException {
        return mResult.getArray(index);
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException {
        return mResult.getDate(index);
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException {
        return mResult.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException {
        return mResult.getTimestamp(index);
    }

    @Override
    public URL getURL(int index)
        throws SQLException {
        return mResult.getURL(index);
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException {
        return mResult.getRowId(index);
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException {
        return mResult.getNClob(index);
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException {
        return mResult.getSQLXML(index);
    }

    @Override
    public String getNString(int index)
        throws SQLException {
        return mResult.getNString(index);
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException {
        return mResult.getNCharacterStream(index);
    }

    @Override
    public <T> T getObject(int index, Class<T> type)
        throws SQLException {
        return mResult.getObject(index, type);
    }

    // XXX: java.sql.ResultSet getter by label
    @Override
    public String getString(String label)
        throws SQLException {
        int index = findColumn(label);
        return getString(index);
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException {
        int index = findColumn(label);
        return getBoolean(index);
    }

    @Override
    public byte getByte(String label)
        throws SQLException {
        int index = findColumn(label);
        return getByte(index);
    }

    @Override
    public short getShort(String label)
        throws SQLException {
        int index = findColumn(label);
        return getShort(index);
    }

    @Override
    public int getInt(String label)
        throws SQLException {
        int index = findColumn(label);
        return getInt(index);
    }

    @Override
    public long getLong(String label)
        throws SQLException {
        int index = findColumn(label);
        return getLong(index);
    }

    @Override
    public float getFloat(String label)
        throws SQLException {
        int index = findColumn(label);
        return getFloat(index);
    }

    @Override
    public double getDouble(String label)
        throws SQLException {
        int index = findColumn(label);
        return getDouble(index);
    }

    @Override
    public BigDecimal getBigDecimal(String label, int scale)
        throws SQLException {
        int index = findColumn(label);
        return getBigDecimal(index);
    }

    @Override
    public byte[] getBytes(String label)
        throws SQLException {
        int index = findColumn(label);
        return getBytes(index);
    }

    @Override
    public Date getDate(String label)
        throws SQLException {
        int index = findColumn(label);
        return getDate(index);
    }

    @Override
    public Time getTime(String label)
        throws SQLException {
        int index = findColumn(label);
        return getTime(index);
    }

    @Override
    public Timestamp getTimestamp(String label)
        throws SQLException {
        int index = findColumn(label);
        return getTimestamp(index);
    }

    @Override
    public InputStream getAsciiStream(String label)
        throws SQLException {
        int index = findColumn(label);
        return getAsciiStream(index);
    }

    @Override
    public InputStream getUnicodeStream(String label)
        throws SQLException {
        int index = findColumn(label);
        return getUnicodeStream(index);
    }

    @Override
    public InputStream getBinaryStream(String label)
        throws SQLException {
        int index = findColumn(label);
        return getBinaryStream(index);
    }

    @Override
    public Object getObject(String label)
        throws SQLException {
        int index = findColumn(label);
        return getObject(index);
    }

    @Override
    public Reader getCharacterStream(String label)
        throws SQLException {
        int index = findColumn(label);
        return getCharacterStream(index);
    }

    @Override
    public BigDecimal getBigDecimal(String label)
        throws SQLException {
        int index = findColumn(label);
        return getBigDecimal(index);
    }

    @Override
    public Object getObject(String label, Map<String, Class<?>> map)
        throws SQLException {
        int index = findColumn(label);
        return getObject(index, map);
    }

    @Override
    public Ref getRef(String label)
        throws SQLException {
        int index = findColumn(label);
        return getRef(index);
    }

    @Override
    public Blob getBlob(String label)
        throws SQLException {
        int index = findColumn(label);
        return getBlob(index);
    }

    @Override
    public Clob getClob(String label)
        throws SQLException {
        int index = findColumn(label);
        return getClob(index);
    }

    @Override
    public Array getArray(String label)
        throws SQLException {
        int index = findColumn(label);
        return getArray(index);
    }

    @Override
    public Date getDate(String label, Calendar cal)
        throws SQLException {
        int index = findColumn(label);
        return getDate(index, cal);
    }

    @Override
    public Time getTime(String label, Calendar cal)
        throws SQLException {
        int index = findColumn(label);
        return getTime(index, cal);
    }

    @Override
    public Timestamp getTimestamp(String label, Calendar cal)
        throws SQLException {
        int index = findColumn(label);
        return getTimestamp(index, cal);
    }

    @Override
    public URL getURL(String label)
        throws SQLException {
        int index = findColumn(label);
        return getURL(index);
    }

    @Override
    public RowId getRowId(String label)
        throws SQLException {
        int index = findColumn(label);
        return getRowId(index);
    }

    @Override
    public NClob getNClob(String label)
        throws SQLException {
        int index = findColumn(label);
        return getNClob(index);
    }

    @Override
    public SQLXML getSQLXML(String label)
        throws SQLException {
        int index = findColumn(label);
        return getSQLXML(index);
    }

    @Override
    public String getNString(String label)
        throws SQLException {
        int index = findColumn(label);
        return getNString(index);
    }

    @Override
    public Reader getNCharacterStream(String label)
        throws SQLException {
        int index = findColumn(label);
        return getNCharacterStream(index);
    }

    @Override
    public <T> T getObject(String label, Class<T> type)
        throws SQLException {
        int index = findColumn(label);
        return getObject(index, type);
    }


    // XXX: java.sql.ResultSet updater by index
    @Override
    public void updateNull(int index)
        throws SQLException {
        mResult.updateNull(index);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        mResult.updateBoolean(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        mResult.updateByte(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        mResult.updateShort(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        mResult.updateInt(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        mResult.updateLong(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        mResult.updateFloat(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        mResult.updateDouble(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException {
        mResult.updateBigDecimal(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        mResult.updateString(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        mResult.updateBytes(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        mResult.updateDate(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        mResult.updateTime(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException {
        mResult.updateTimestamp(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException {
        mResult.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException {
        mResult.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException {
        mResult.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException {
        mResult.updateObject(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        mResult.updateObject(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException {
        mResult.updateRef(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException {
        mResult.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException {
        mResult.updateClob(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException {
        mResult.updateArray(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mResult.updateNCharacterStream(index, value, length);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException {
        mResult.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException {
        mResult.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mResult.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException  {
        mResult.updateBlob(index, value, length);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException {
        mResult.updateClob(index, value, length);
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException {
        mResult.updateNClob(index, value, length);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException {
        mResult.updateNCharacterStream(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException {
        mResult.updateAsciiStream(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException {
        mResult.updateBinaryStream(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException {
        mResult.updateCharacterStream(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException {
        mResult.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException {
        mResult.updateClob(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException {
        mResult.updateNClob(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException {
        mResult.updateSQLXML(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException {
        mResult.updateNString(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException {
        mResult.updateNClob(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException {
        mResult.updateRowId(index, value);
    }

    // XXX: java.sql.ResultSet updater by label
    @Override
    public void updateNull(String label)
        throws SQLException {
        int index = findColumn(label);
        updateNull(index);
    }

    @Override
    public void updateBoolean(String label, boolean value)
        throws SQLException {
        int index = findColumn(label);
        updateBoolean(index ,value);
    }

    @Override
    public void updateByte(String label, byte value)
        throws SQLException {
        int index = findColumn(label);
        updateByte(index, value);
    }

    @Override
    public void updateShort(String label, short value)
        throws SQLException {
        int index = findColumn(label);
        updateShort(index, value);
    }

    @Override
    public void updateInt(String label, int value)
        throws SQLException {
        int index = findColumn(label);
        updateInt(index, value);
    }

    @Override
    public void updateLong(String label, long value)
        throws SQLException {
        int index = findColumn(label);
        updateLong(index, value);
    }

    @Override
    public void updateFloat(String label, float value)
        throws SQLException {
        int index = findColumn(label);
        updateFloat(index, value);
    }

    @Override
    public void updateDouble(String label, double value)
        throws SQLException {
        int index = findColumn(label);
        updateDouble(index, value);
    }

    @Override
    public void updateBigDecimal(String label, BigDecimal value)
        throws SQLException {
        int index = findColumn(label);
        updateBigDecimal(index, value);
    }

    @Override
    public void updateString(String label, String value)
        throws SQLException {
        int index = findColumn(label);
        updateString(index, value);
    }

    @Override
    public void updateBytes(String label, byte[] value)
        throws SQLException {
        int index = findColumn(label);
        updateBytes(index, value);
    }

    @Override
    public void updateDate(String label, Date value)
        throws SQLException {
        int index = findColumn(label);
        updateDate(index, value);
    }

    @Override
    public void updateTime(String label, Time value)
        throws SQLException {
        int index = findColumn(label);
        updateTime(index, value);
    }

    @Override
    public void updateTimestamp(String label, Timestamp value)
        throws SQLException {
        int index = findColumn(label);
        updateTimestamp(index, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, int length)
        throws SQLException {
        int index = findColumn(label);
        updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, int length)
        throws SQLException {
        int index = findColumn(label);
        updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, int length)
        throws SQLException {
        int index = findColumn(label);
        updateCharacterStream(index, value, length);
    }

    @Override
    public void updateObject(String label, Object value, int length)
        throws SQLException {
        int index = findColumn(label);
        updateObject(index, value, length);
    }

    @Override
    public void updateObject(String label, Object value)
        throws SQLException {
        int index = findColumn(label);
        updateObject(index, value);
    }

    @Override
    public void updateRef(String label, Ref value)
        throws SQLException {
        int index = findColumn(label);
        updateRef(index, value);
    }

    @Override
    public void updateBlob(String label, Blob value)
        throws SQLException {
        int index = findColumn(label);
        updateBlob(index, value);
    }

    @Override
    public void updateClob(String label, Clob value)
        throws SQLException {
        int index = findColumn(label);
        updateClob(index, value);
    }

    @Override
    public void updateArray(String label, Array value)
        throws SQLException {
        int index = findColumn(label);
        updateArray(index, value);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateNCharacterStream(index, value, length);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateCharacterStream(index, value, length);
    }

    @Override
    public void updateBlob(String label, InputStream value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateBlob(index, value, length);
    }

    @Override
    public void updateClob(String label, Reader value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateClob(index, value, length);
    }

    @Override
    public void updateNClob(String label, Reader value, long length)
        throws SQLException {
        int index = findColumn(label);
        updateNClob(index, value, length);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value)
        throws SQLException {
        int index = findColumn(label);
        updateNCharacterStream(index, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value)
        throws SQLException {
        int index = findColumn(label);
        updateAsciiStream(index, value);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value)
        throws SQLException {
        int index = findColumn(label);
        updateBinaryStream(index, value);
    }

    @Override
    public void updateCharacterStream(String label, Reader value)
        throws SQLException {
        int index = findColumn(label);
        updateCharacterStream(index, value);
    }

    @Override
    public void updateBlob(String label, InputStream value)
        throws SQLException {
        int index = findColumn(label);
        updateBlob(index, value);
    }

    @Override
    public void updateClob(String label, Reader value)
        throws SQLException {
        int index = findColumn(label);
        updateClob(index, value);
    }

    @Override
    public void updateNClob(String label, Reader value)
        throws SQLException {
        int index = findColumn(label);
        updateNClob(index, value);
    }

    @Override
    public void updateSQLXML(String label, SQLXML value)
        throws SQLException {
        int index = findColumn(label);
        updateSQLXML(index, value);
    }

    @Override
    public void updateNString(String label, String value)
        throws SQLException {
        int index = findColumn(label);
        updateNString(index, value);
    }

    @Override
    public void updateNClob(String label, NClob value)
        throws SQLException {
        int index = findColumn(label);
        updateNClob(index, value);
    }

    @Override
    public void updateRowId(String label, RowId value)
        throws SQLException {
        int index = findColumn(label);
        updateRowId(index, value);
    }

}
