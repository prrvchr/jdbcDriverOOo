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
package io.github.prrvchr.driver.metadata;

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

abstract class ResultSet implements java.sql.ResultSet {

    protected final java.sql.ResultSet mResult;

    protected ResultSet(java.sql.ResultSet result) {
        mResult = result;
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
    public boolean next()
        throws SQLException {
        return mResult.next();
    }

    @Override
    public void close()
        throws SQLException {
        mResult.close();
    }

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

    @SuppressWarnings("deprecation")
    @Override
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
    public String getString(String label)
        throws SQLException {
        return mResult.getString(label);
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException {
        return mResult.getBoolean(label);
    }

    @Override
    public byte getByte(String label)
        throws SQLException {
        return mResult.getByte(label);
    }

    @Override
    public short getShort(String label)
        throws SQLException {
        return mResult.getShort(label);
    }

    @Override
    public int getInt(String label)
        throws SQLException {
        return mResult.getInt(label);
    }

    @Override
    public long getLong(String label)
        throws SQLException {
        return mResult.getLong(label);
    }

    @Override
    public float getFloat(String label)
        throws SQLException {
        return mResult.getFloat(label);
    }

    @Override
    public double getDouble(String label)
        throws SQLException {
        return mResult.getDouble(label);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(String label, int scale)
        throws SQLException {
        return mResult.getBigDecimal(label, scale);
    }

    @Override
    public byte[] getBytes(String label)
        throws SQLException {
        return mResult.getBytes(label);
    }

    @Override
    public Date getDate(String label)
        throws SQLException {
        return mResult.getDate(label);
    }

    @Override
    public Time getTime(String label)
        throws SQLException {
        return mResult.getTime(label);
    }

    @Override
    public Timestamp getTimestamp(String label)
        throws SQLException {
        return mResult.getTimestamp(label);
    }

    @Override
    public InputStream getAsciiStream(String label)
        throws SQLException {
        return mResult.getAsciiStream(label);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(String label)
        throws SQLException {
        return mResult.getUnicodeStream(label);
    }

    @Override
    public InputStream getBinaryStream(String label)
        throws SQLException {
        return mResult.getBinaryStream(label);
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
    public Object getObject(int index)
        throws SQLException {
        return mResult.getObject(index);
    }

    @Override
    public Object getObject(String label)
        throws SQLException {
        return mResult.getObject(label);
    }

    @Override
    public int findColumn(String label)
        throws SQLException {
        return mResult.findColumn(label);
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException {
        return mResult.getCharacterStream(index);
    }

    @Override
    public Reader getCharacterStream(String label)
        throws SQLException {
        return mResult.getCharacterStream(label);
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException {
        return mResult.getBigDecimal(index);
    }

    @Override
    public BigDecimal getBigDecimal(String label)
        throws SQLException {
        return mResult.getBigDecimal(label);
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
    public boolean previous()
        throws SQLException {
        return mResult.previous();
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
    public void setFetchSize(int size)
        throws SQLException {
        mResult.setFetchSize(size);
    }

    @Override
    public int getFetchSize()
        throws SQLException {
        return mResult.getFetchSize();
    }

    @Override
    public int getType()
        throws SQLException {
        return mResult.getType();
    }

    @Override
    public int getConcurrency()
        throws SQLException {
        return mResult.getConcurrency();
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
    public void updateNull(int index)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBoolean(int index, boolean x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateByte(int index, byte x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateShort(int index, short x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateInt(int index, int x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateLong(int index, long x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateFloat(int index, float x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateDouble(int index, double x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateString(int index, String x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBytes(int index, byte[] x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateDate(int index, Date x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateTime(int index, Time x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateTimestamp(int index, Timestamp x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(int index, InputStream x, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(int index, InputStream x, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(int index, Reader x, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateObject(int index, Object x, int scaleOrLength)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateObject(int index, Object x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNull(String label)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBoolean(String label, boolean x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateByte(String label, byte x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateShort(String label, short x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateInt(String label, int x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateLong(String label, long x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateFloat(String label, float x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateDouble(String label, double x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBigDecimal(String label, BigDecimal x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateString(String label, String x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBytes(String label, byte[] x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateDate(String label, Date x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateTime(String label, Time x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateTimestamp(String label, Timestamp x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(String label, InputStream x, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(String label, InputStream x, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(String label, Reader reader, int length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateObject(String label, Object x, int scaleOrLength)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateObject(String label, Object x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void insertRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void deleteRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void refreshRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void moveToInsertRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public Statement getStatement()
        throws SQLException {
        return mResult.getStatement();
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Ref getRef(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Clob getClob(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Array getArray(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Object getObject(String label, Map<String, Class<?>> map)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Ref getRef(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Blob getBlob(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Clob getClob(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Array getArray(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(String label, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Time getTime(String label, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(String label, Calendar cal)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public URL getURL(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public URL getURL(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public void updateRef(int index, Ref x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateRef(String label, Ref x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(int index, Blob x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(String label, Blob x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(int index, Clob x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(String label, Clob x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateArray(int index, Array x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateArray(String label, Array x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public RowId getRowId(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public void updateRowId(int index, RowId x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateRowId(String label, RowId x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public int getHoldability()
        throws SQLException {
        // XXX Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isClosed()
        throws SQLException {
        return mResult.isClosed();
    }

    @Override
    public void updateNString(int index, String nString)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNString(String label, String nString)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(int index, NClob nClob)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(String label, NClob nClob)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public NClob getNClob(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML getSQLXML(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public void updateSQLXML(int index, SQLXML xmlObject)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateSQLXML(String label, SQLXML xmlObject)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public String getNString(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public String getNString(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public Reader getNCharacterStream(String label)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public void updateNCharacterStream(int index, Reader x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNCharacterStream(String label, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(int index, InputStream x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(int index, InputStream x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(int index, Reader x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(String label, InputStream x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(String label, InputStream x, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(String label, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(int index, InputStream inputStream, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(String label, InputStream inputStream, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(int index, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(String label, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(int index, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(String label, Reader reader, long length)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNCharacterStream(int index, Reader x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNCharacterStream(String label, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(int index, InputStream x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(int index, InputStream x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(int index, Reader x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateAsciiStream(String label, InputStream x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBinaryStream(String label, InputStream x)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateCharacterStream(String label, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(int index, InputStream inputStream)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateBlob(String label, InputStream inputStream)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(int index, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateClob(String label, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(int index, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public void updateNClob(String label, Reader reader)
        throws SQLException {
        // XXX Auto-generated method stub
    }

    @Override
    public <T> T getObject(int index, Class<T> type)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getObject(String label, Class<T> type)
        throws SQLException {
        // XXX Auto-generated method stub
        return null;
    }

}
