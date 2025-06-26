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
package io.github.prrvchr.uno.driver.resultset;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

import io.github.prrvchr.uno.helper.UnoHelper;


public class CachedRowSet
    implements javax.sql.rowset.CachedRowSet {

    protected Statement mStatement;
    protected javax.sql.rowset.CachedRowSet mRowset;
    private boolean mClosed = false;

    // The constructor method:
    public CachedRowSet(java.sql.ResultSet result)
        throws SQLException {
        mStatement = result.getStatement();
        mRowset = getCachedRowSet(result);
        result.close();
    }

    private static javax.sql.rowset.CachedRowSet getCachedRowSet(java.sql.ResultSet result)
        throws SQLException {
        try {
            System.out.println("RowSet.getCachedRowSet() 1");
            javax.sql.rowset.CachedRowSet rowset = ResultSetHelper.getCachedRowSet();
            System.out.println("RowSet.getCachedRowSet() 2 ProviderID: "
                               + rowset.getSyncProvider().getProviderID());
            rowset.setSyncProvider(ResultSetHelper.ROWSET_PROVIDER);
            System.out.println("RowSet.getCachedRowSet() 2 ProviderID: "
                    + rowset.getSyncProvider().getProviderID());
            rowset.populate(result);
            RowSetMetaData metadata = (RowSetMetaData) rowset.getMetaData();
            int count = metadata.getColumnCount();
            if (count > 0) {
                rowset.setTableName(metadata.getTableName(1));
                rowset.setKeyColumns(new int[]{1});
                System.out.println("RowSet.getCachedRowSet() 3 Catalogue: '" + metadata.getCatalogName(1) + "'");
                System.out.println("RowSet.getCachedRowSet() 3 Schema: '" + metadata.getSchemaName(1) + "'");
                System.out.println("RowSet.getCachedRowSet() 3 Table: '" + metadata.getTableName(1) + "'");
            } else {
                rowset.setReadOnly(true);
            }
            System.out.println("RowSet.getCachedRowSet() 4 table: " + rowset.getTableName());
            while (rowset.next()) {
                for (int i = 1; i <= count; i++) {
                    String name = rowset.getMetaData().getColumnName(i);
                    Object value = rowset.getObject(i);
                    System.out.println("RowSet.getCachedRowSet() 5 row: " + rowset.getRow() +
                                       " - ColumnIndex: " + i + " - ColumnName: " + name + " - Value:" + value);
                }
            }
            rowset.beforeFirst();
            return rowset;
        } catch (Throwable e) {
            System.out.println("ResultSetHelper.getRowSetFactory() ERROR: " + UnoHelper.getStackTrace(e));
            throw e;
        }
    }

    @Override
    public void insertRow()
        throws SQLException {
        mRowset.insertRow();
    }

    @Override
    public void cancelRowUpdates()
        throws SQLException {
        mRowset.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow()
        throws SQLException {
        mRowset.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        mRowset.moveToCurrentRow();
    }

    @Override
    public int getConcurrency()
        throws SQLException {
        return mRowset.getConcurrency();
    }

    @Override
    public int getType()
        throws SQLException {
        return mRowset.getType();
    }

    @Override
    public Statement getStatement()
        throws SQLException {
        return mStatement;
    }

    @Override
    public SQLWarning getWarnings()
        throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings()
        throws SQLException {
        mRowset.clearWarnings();
    }

    @Override
    public String getCursorName()
        throws SQLException {
        return mRowset.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData()
        throws SQLException {
        return mRowset.getMetaData();
    }

    @Override
    public int findColumn(String label)
        throws SQLException {
        return mRowset.findColumn(label);
    }

    @Override
    public void setFetchDirection(int direction)
        throws SQLException {
        mRowset.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection()
        throws SQLException {
        return mRowset.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows)
        throws SQLException {
        mRowset.setFetchSize(rows);
    }

    @Override
    public int getFetchSize()
        throws SQLException {
        return mRowset.getFetchSize();
    }

    @Override
    public int getHoldability()
        throws SQLException {
        return mRowset.getHoldability();
    }

    @Override
    public boolean isClosed()
        throws SQLException {
        return mClosed;
    }

    @Override
    public boolean rowUpdated()
        throws SQLException {
        return mRowset.rowUpdated();
    }

    @Override
    public boolean rowInserted()
        throws SQLException {
        return mRowset.rowInserted();
    }

    @Override
    public boolean rowDeleted()
        throws SQLException {
        return mRowset.rowDeleted();
    }

    @Override
    public void updateRow()
        throws SQLException {
        mRowset.updateRow();
    }

    @Override
    public void deleteRow()
        throws SQLException {
        mRowset.deleteRow();
    }

    @Override
    public void refreshRow()
        throws SQLException {
        mRowset.refreshRow();
    }


    // XXX: java.sql.ResultSet mover
    @Override
    public boolean next()
        throws SQLException {
        try {
            return mRowset.next();
        } catch (Throwable e) {
            System.out.println("RowSet.next() ERROR: " + UnoHelper.getStackTrace(e));
            throw e;
        }
    }

    @Override
    public boolean previous()
        throws SQLException {
        boolean previous = false;
        if (mRowset.size() > 0) {
            previous = mRowset.previous();
        }
        return previous;
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException {
        return mRowset.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast()
        throws SQLException {
        return mRowset.isAfterLast();
    }

    @Override
    public boolean isFirst()
        throws SQLException {
        return mRowset.isFirst();
    }

    @Override
    public boolean isLast()
        throws SQLException {
        return mRowset.isLast();
    }

    @Override
    public void beforeFirst()
        throws SQLException {
        mRowset.beforeFirst();
    }

    @Override
    public void afterLast()
        throws SQLException {
        mRowset.afterLast();
    }

    @Override
    public boolean first()
        throws SQLException {
        return mRowset.first();
    }

    @Override
    public boolean last()
        throws SQLException {
        return mRowset.last();
    }

    @Override
    public int getRow()
        throws SQLException {
        System.out.println("RowSet.getRow() 1");
        int row = mRowset.getRow();
        System.out.println("RowSet.getRow() 2 row: " + row);
        return row;
    }

    @Override
    public boolean absolute(int row)
        throws SQLException {
        return mRowset.absolute(row);
    }

    @Override
    public boolean relative(int row)
        throws SQLException {
        return mRowset.relative(row);
    }

    @Override
    public <T> T unwrap(Class<T> iface)
        throws SQLException {
        return mRowset.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return mRowset.isWrapperFor(iface);
    }

    @Override
    public void close()
        throws SQLException {
        if (!mClosed) {
            // XXX: When using an updatable javax.sql.rowset.CachedRowSet, the auto commit must be restored.
            mRowset.close();
            mClosed = true;
        }
    }


    // XXX: java.sql.ResultSet getter by index
    @Override
    public boolean wasNull()
        throws SQLException {
        return mRowset.wasNull();
    }

    @Override
    public String getString(int index)
        throws SQLException {
        return mRowset.getString(index);
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException {
        return mRowset.getBoolean(index);
    }

    @Override
    public byte getByte(int index)
        throws SQLException {
        return mRowset.getByte(index);
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        return mRowset.getShort(index);
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        return mRowset.getInt(index);
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        return mRowset.getLong(index);
    }

    @Override
    public float getFloat(int index)
        throws SQLException {
        return mRowset.getFloat(index);
    }

    @Override
    public double getDouble(int index)
        throws SQLException {
        return mRowset.getDouble(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index, int scale)
        throws SQLException {
        return mRowset.getBigDecimal(index);
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException {
        return mRowset.getBytes(index);
    }

    @Override
    public Date getDate(int index)
        throws SQLException {
        return mRowset.getDate(index);
    }

    @Override
    public Time getTime(int index)
        throws SQLException {
        return mRowset.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index)
        throws SQLException {
        return mRowset.getTimestamp(index);
    }

    @Override
    public InputStream getAsciiStream(int index)
        throws SQLException {
        return mRowset.getAsciiStream(index);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int index)
        throws SQLException {
        return mRowset.getUnicodeStream(index);
    }

    @Override
    public InputStream getBinaryStream(int index)
        throws SQLException {
        return mRowset.getBinaryStream(index);
    }

    @Override
    public Object getObject(int index)
        throws SQLException {
        return mRowset.getObject(index);
    }

    @Override
    public Reader getCharacterStream(int index)
        throws SQLException {
        return mRowset.getCharacterStream(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index)
        throws SQLException {
        return mRowset.getBigDecimal(index);
    }

    @Override
    public Object getObject(int index, Map<String, Class<?>> map)
        throws SQLException {
        return mRowset.getObject(index, map);
    }

    @Override
    public Ref getRef(int index)
        throws SQLException {
        return mRowset.getRef(index);
    }

    @Override
    public Blob getBlob(int index)
        throws SQLException {
        return mRowset.getBlob(index);
    }

    @Override
    public Clob getClob(int index)
        throws SQLException {
        return mRowset.getClob(index);
    }

    @Override
    public Array getArray(int index)
        throws SQLException {
        return mRowset.getArray(index);
    }

    @Override
    public Date getDate(int index, Calendar cal)
        throws SQLException {
        return mRowset.getDate(index);
    }

    @Override
    public Time getTime(int index, Calendar cal)
        throws SQLException {
        return mRowset.getTime(index);
    }

    @Override
    public Timestamp getTimestamp(int index, Calendar cal)
        throws SQLException {
        return mRowset.getTimestamp(index);
    }

    @Override
    public URL getURL(int index)
        throws SQLException {
        return mRowset.getURL(index);
    }

    @Override
    public RowId getRowId(int index)
        throws SQLException {
        return mRowset.getRowId(index);
    }

    @Override
    public NClob getNClob(int index)
        throws SQLException {
        return mRowset.getNClob(index);
    }

    @Override
    public SQLXML getSQLXML(int index)
        throws SQLException {
        return mRowset.getSQLXML(index);
    }

    @Override
    public String getNString(int index)
        throws SQLException {
        return mRowset.getNString(index);
    }

    @Override
    public Reader getNCharacterStream(int index)
        throws SQLException {
        return mRowset.getNCharacterStream(index);
    }

    @Override
    public <T> T getObject(int index, Class<T> type)
        throws SQLException {
        return mRowset.getObject(index, type);
    }

    // XXX: java.sql.ResultSet getter by label
    @Override
    public String getString(String label)
        throws SQLException {
        return mRowset.getString(label);
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException {
        return mRowset.getBoolean(label);
    }

    @Override
    public byte getByte(String label)
        throws SQLException {
        return mRowset.getByte(label);
    }

    @Override
    public short getShort(String label)
        throws SQLException {
        return mRowset.getShort(label);
    }

    @Override
    public int getInt(String label)
        throws SQLException {
        return mRowset.getInt(label);
    }

    @Override
    public long getLong(String label)
        throws SQLException {
        return mRowset.getLong(label);
    }

    @Override
    public float getFloat(String label)
        throws SQLException {
        return mRowset.getFloat(label);
    }

    @Override
    public double getDouble(String label)
        throws SQLException {
        return mRowset.getDouble(label);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(String label, int scale)
        throws SQLException {
        return mRowset.getBigDecimal(label, scale);
    }

    @Override
    public byte[] getBytes(String label)
        throws SQLException {
        return mRowset.getBytes(label);
    }

    @Override
    public Date getDate(String label)
        throws SQLException {
        return mRowset.getDate(label);
    }

    @Override
    public Time getTime(String label)
        throws SQLException {
        return mRowset.getTime(label);
    }

    @Override
    public Timestamp getTimestamp(String label)
        throws SQLException {
        return mRowset.getTimestamp(label);
    }

    @Override
    public InputStream getAsciiStream(String label)
        throws SQLException {
        return mRowset.getAsciiStream(label);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(String label)
        throws SQLException {
        return mRowset.getUnicodeStream(label);
    }

    @Override
    public InputStream getBinaryStream(String label)
        throws SQLException {
        return mRowset.getBinaryStream(label);
    }

    @Override
    public Object getObject(String label)
        throws SQLException {
        return mRowset.getObject(label);
    }

    @Override
    public Reader getCharacterStream(String label)
        throws SQLException {
        return mRowset.getCharacterStream(label);
    }

    @Override
    public BigDecimal getBigDecimal(String label)
        throws SQLException {
        return mRowset.getBigDecimal(label);
    }

    @Override
    public Object getObject(String label, Map<String, Class<?>> map)
        throws SQLException {
        return mRowset.getObject(label, map);
    }

    @Override
    public Ref getRef(String label)
        throws SQLException {
        return mRowset.getRef(label);
    }

    @Override
    public Blob getBlob(String label)
        throws SQLException {
        return mRowset.getBlob(label);
    }

    @Override
    public Clob getClob(String label)
        throws SQLException {
        return mRowset.getClob(label);
    }

    @Override
    public Array getArray(String label)
        throws SQLException {
        return mRowset.getArray(label);
    }

    @Override
    public Date getDate(String label, Calendar cal)
        throws SQLException {
        return mRowset.getDate(label, cal);
    }

    @Override
    public Time getTime(String label, Calendar cal)
        throws SQLException {
        return mRowset.getTime(label, cal);
    }

    @Override
    public Timestamp getTimestamp(String label, Calendar cal)
        throws SQLException {
        return mRowset.getTimestamp(label, cal);
    }

    @Override
    public URL getURL(String label)
        throws SQLException {
        return mRowset.getURL(label);
    }

    @Override
    public RowId getRowId(String label)
        throws SQLException {
        return mRowset.getRowId(label);
    }

    @Override
    public NClob getNClob(String label)
        throws SQLException {
        return mRowset.getNClob(label);
    }

    @Override
    public SQLXML getSQLXML(String label)
        throws SQLException {
        return mRowset.getSQLXML(label);
    }

    @Override
    public String getNString(String label)
        throws SQLException {
        return mRowset.getNString(label);
    }

    @Override
    public Reader getNCharacterStream(String label)
        throws SQLException {
        return mRowset.getNCharacterStream(label);
    }

    @Override
    public <T> T getObject(String label, Class<T> type)
        throws SQLException {
        return mRowset.getObject(label, type);
    }


    // XXX: java.sql.ResultSet updater by index
    @Override
    public void updateNull(int index)
        throws SQLException {
        mRowset.updateNull(index);
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException {
        mRowset.updateBoolean(index, value);
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException {
        mRowset.updateByte(index, value);
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException {
        mRowset.updateShort(index, value);
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException {
        mRowset.updateInt(index, value);
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException {
        mRowset.updateLong(index, value);
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException {
        mRowset.updateFloat(index, value);
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException {
        mRowset.updateDouble(index, value);
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException {
        mRowset.updateBigDecimal(index, value);
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException {
        mRowset.updateString(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException {
        mRowset.updateBytes(index, value);
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException {
        mRowset.updateDate(index, value);
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException {
        mRowset.updateTime(index, value);
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException {
        mRowset.updateTimestamp(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException {
        mRowset.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException {
        mRowset.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException {
        mRowset.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException {
        mRowset.updateObject(index, value, length);
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException {
        mRowset.updateObject(index, value);
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException {
        mRowset.updateRef(index, value);
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException {
        mRowset.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException {
        mRowset.updateClob(index, value);
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException {
        mRowset.updateArray(index, value);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mRowset.updateNCharacterStream(index, value, length);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException {
        mRowset.updateAsciiStream(index, value, length);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException {
        mRowset.updateBinaryStream(index, value, length);
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException {
        mRowset.updateCharacterStream(index, value, length);
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException  {
        mRowset.updateBlob(index, value, length);
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException {
        mRowset.updateClob(index, value, length);
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException {
        mRowset.updateNClob(index, value, length);
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException {
        mRowset.updateNCharacterStream(index, value);
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException {
        mRowset.updateAsciiStream(index, value);
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException {
        mRowset.updateBinaryStream(index, value);
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException {
        mRowset.updateCharacterStream(index, value);
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException {
        mRowset.updateBlob(index, value);
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException {
        mRowset.updateClob(index, value);
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException {
        mRowset.updateNClob(index, value);
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException {
        mRowset.updateSQLXML(index, value);
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException {
        mRowset.updateNString(index, value);
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException {
        mRowset.updateNClob(index, value);
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException {
        mRowset.updateRowId(index, value);
    }

    // XXX: java.sql.ResultSet updater by label
    @Override
    public void updateNull(String label)
        throws SQLException {
        mRowset.updateNull(label);
    }

    @Override
    public void updateBoolean(String label, boolean value)
        throws SQLException {
        mRowset.updateBoolean(label ,value);
    }

    @Override
    public void updateByte(String label, byte value)
        throws SQLException {
        mRowset.updateByte(label, value);
    }

    @Override
    public void updateShort(String label, short value)
        throws SQLException {
        mRowset.updateShort(label, value);
    }

    @Override
    public void updateInt(String label, int value)
        throws SQLException {
        mRowset.updateInt(label, value);
    }

    @Override
    public void updateLong(String label, long value)
        throws SQLException {
        mRowset.updateLong(label, value);
    }

    @Override
    public void updateFloat(String label, float value)
        throws SQLException {
        mRowset.updateFloat(label, value);
    }

    @Override
    public void updateDouble(String label, double value)
        throws SQLException {
        mRowset.updateDouble(label, value);
    }

    @Override
    public void updateBigDecimal(String label, BigDecimal value)
        throws SQLException {
        mRowset.updateBigDecimal(label, value);
    }

    @Override
    public void updateString(String label, String value)
        throws SQLException {
        mRowset.updateString(label, value);
    }

    @Override
    public void updateBytes(String label, byte[] value)
        throws SQLException {
        mRowset.updateBytes(label, value);
    }

    @Override
    public void updateDate(String label, Date value)
        throws SQLException {
        mRowset.updateDate(label, value);
    }

    @Override
    public void updateTime(String label, Time value)
        throws SQLException {
        mRowset.updateTime(label, value);
    }

    @Override
    public void updateTimestamp(String label, Timestamp value)
        throws SQLException {
        mRowset.updateTimestamp(label, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, int length)
        throws SQLException {
        mRowset.updateAsciiStream(label, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, int length)
        throws SQLException {
        mRowset.updateBinaryStream(label, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, int length)
        throws SQLException {
        mRowset.updateCharacterStream(label, value, length);
    }

    @Override
    public void updateObject(String label, Object value, int length)
        throws SQLException {
        mRowset.updateObject(label, value, length);
    }

    @Override
    public void updateObject(String label, Object value)
        throws SQLException {
        mRowset.updateObject(label, value);
    }

    @Override
    public void updateRef(String label, Ref value)
        throws SQLException {
        mRowset.updateRef(label, value);
    }

    @Override
    public void updateBlob(String label, Blob value)
        throws SQLException {
        mRowset.updateBlob(label, value);
    }

    @Override
    public void updateClob(String label, Clob value)
        throws SQLException {
        mRowset.updateClob(label, value);
    }

    @Override
    public void updateArray(String label, Array value)
        throws SQLException {
        mRowset.updateArray(label, value);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value, long length)
        throws SQLException {
        mRowset.updateNCharacterStream(label, value, length);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value, long length)
        throws SQLException {
        mRowset.updateAsciiStream(label, value, length);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value, long length)
        throws SQLException {
        mRowset.updateBinaryStream(label, value, length);
    }

    @Override
    public void updateCharacterStream(String label, Reader value, long length)
        throws SQLException {
        mRowset.updateCharacterStream(label, value, length);
    }

    @Override
    public void updateBlob(String label, InputStream value, long length)
        throws SQLException {
        mRowset.updateBlob(label, value, length);
    }

    @Override
    public void updateClob(String label, Reader value, long length)
        throws SQLException {
        mRowset.updateClob(label, value, length);
    }

    @Override
    public void updateNClob(String label, Reader value, long length)
        throws SQLException {
        mRowset.updateNClob(label, value, length);
    }

    @Override
    public void updateNCharacterStream(String label, Reader value)
        throws SQLException {
        mRowset.updateNCharacterStream(label, value);
    }

    @Override
    public void updateAsciiStream(String label, InputStream value)
        throws SQLException {
        mRowset.updateAsciiStream(label, value);
    }

    @Override
    public void updateBinaryStream(String label, InputStream value)
        throws SQLException {
        mRowset.updateBinaryStream(label, value);
    }

    @Override
    public void updateCharacterStream(String label, Reader value)
        throws SQLException {
        mRowset.updateCharacterStream(label, value);
    }

    @Override
    public void updateBlob(String label, InputStream value)
        throws SQLException {
        mRowset.updateBlob(label, value);
    }

    @Override
    public void updateClob(String label, Reader value)
        throws SQLException {
        mRowset.updateClob(label, value);
    }

    @Override
    public void updateNClob(String label, Reader value)
        throws SQLException {
        mRowset.updateNClob(label, value);
    }

    @Override
    public void updateSQLXML(String label, SQLXML value)
        throws SQLException {
        mRowset.updateSQLXML(label, value);
    }

    @Override
    public void updateNString(String label, String value)
        throws SQLException {
        mRowset.updateNString(label, value);
    }

    @Override
    public void updateNClob(String label, NClob value)
        throws SQLException {
        mRowset.updateNClob(label, value);
    }

    @Override
    public void updateRowId(String label, RowId value)
        throws SQLException {
        mRowset.updateRowId(label, value);
    }

    // XXX: javax.sql.rowset.CachedRowSet methods
    @Override
    public void addRowSetListener(RowSetListener listener) {
        mRowset.addRowSetListener(listener);
    }

    @Override
    public void clearParameters() throws SQLException {
        mRowset.clearParameters();
    }

    @Override
    public void execute() throws SQLException {
        mRowset.execute();
    }

    @Override
    public String getCommand() {
        return mRowset.getCommand();
    }

    @Override
    public String getDataSourceName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getEscapeProcessing() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxRows() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTransactionIsolation() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeRowSetListener(RowSetListener arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setArray(int arg0, Array arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream(String arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream(String arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(int arg0, Blob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(int arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(String arg0, Blob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(String arg0, InputStream arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBoolean(int arg0, boolean arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBoolean(String arg0, boolean arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setByte(int arg0, byte arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setByte(String arg0, byte arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBytes(int arg0, byte[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBytes(String arg0, byte[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream(String arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(int arg0, Clob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(int arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(String arg0, Clob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(String arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob(String arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCommand(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setConcurrency(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDataSourceName(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate(int arg0, Date arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate(String arg0, Date arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate(String arg0, Date arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDouble(int arg0, double arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDouble(String arg0, double arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEscapeProcessing(boolean arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFloat(int arg0, float arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFloat(String arg0, float arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setInt(int arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setInt(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLong(int arg0, long arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLong(String arg0, long arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMaxFieldSize(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMaxRows(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream(String arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(String arg0, NClob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(String arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(int arg0, NClob arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(int arg0, Reader arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(String arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNString(int arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNString(String arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull(int arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull(int arg0, int arg1, String arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull(String arg0, int arg1, String arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(String arg0, Object arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(int arg0, Object arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(String arg0, Object arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject(String arg0, Object arg1, int arg2, int arg3) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPassword(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setQueryTimeout(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setReadOnly(boolean arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRef(int arg0, Ref arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRowId(int arg0, RowId arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRowId(String arg0, RowId arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSQLXML(String arg0, SQLXML arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShort(int arg0, short arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShort(String arg0, short arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setString(int arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setString(String arg0, String arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime(int arg0, Time arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime(String arg0, Time arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime(String arg0, Time arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp(String arg0, Timestamp arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp(String arg0, Timestamp arg1, Calendar arg2) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTransactionIsolation(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setType(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setURL(int arg0, URL arg1) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUrl(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUsername(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int[] getMatchColumnIndexes() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getMatchColumnNames() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMatchColumn(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMatchColumn(int[] arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMatchColumn(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMatchColumn(String[] arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unsetMatchColumn(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unsetMatchColumn(int[] arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unsetMatchColumn(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unsetMatchColumn(String[] arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void acceptChanges() throws SyncProviderException {
        mRowset.acceptChanges();
    }

    @Override
    public void acceptChanges(Connection connection) throws SyncProviderException {
        mRowset.acceptChanges(connection);
    }

    @Override
    public boolean columnUpdated(int idx) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean columnUpdated(String columnName) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void commit() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public javax.sql.rowset.CachedRowSet createCopy() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sql.rowset.CachedRowSet createCopyNoConstraints() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sql.rowset.CachedRowSet createCopySchema() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sql.RowSet createShared() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute(Connection conn) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int[] getKeyColumns() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getOriginal() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getOriginalRow() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPageSize() {
        return mRowset.getPageSize();
    }

    @Override
    public RowSetWarning getRowSetWarnings() throws SQLException {
        return mRowset.getRowSetWarnings();
    }

    @Override
    public boolean getShowDeleted() throws SQLException {
        return mRowset.getShowDeleted();
    }

    @Override
    public SyncProvider getSyncProvider() throws SQLException {
        return mRowset.getSyncProvider();
    }

    @Override
    public String getTableName() throws SQLException {
        return mRowset.getTableName();
    }

    @Override
    public boolean nextPage() throws SQLException {
        return mRowset.nextPage();
    }

    @Override
    public void populate(ResultSet data) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void populate(ResultSet rs, int startRow) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean previousPage() throws SQLException {
        return mRowset.previousPage();
    }

    @Override
    public void release() throws SQLException {
        mRowset.release();
    }

    @Override
    public void restoreOriginal() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rollback() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rollback(Savepoint s) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rowSetPopulated(RowSetEvent event, int numRows) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setKeyColumns(int[] keys) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMetaData(RowSetMetaData md) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOriginalRow() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPageSize(int size) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShowDeleted(boolean b) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSyncProvider(String provider) throws SQLException {
        mRowset.setSyncProvider(provider);
    }

    @Override
    public void setTableName(String tabName) throws SQLException {
        mRowset.setTableName(tabName);
        
    }

    @Override
    public int size() {
        return mRowset.size();
    }

    @Override
    public Collection<?> toCollection() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<?> toCollection(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<?> toCollection(String column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void undoDelete() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void undoInsert() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void undoUpdate() throws SQLException {
        // TODO Auto-generated method stub
        
    }

}
