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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Map;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;
import io.github.prrvchr.jdbcdriver.rowset.RowSetWriter;


public class CachedResultSet
    implements ResultSet
{

    protected DriverProvider m_Provider;
    protected ResultSet m_Result;
    protected int m_MinSize = 10;
    protected int m_ColumnCount = 0;
    protected int m_FetchSize;
    protected String m_Query;
    protected boolean m_IsUpdatable = false;
    protected boolean m_MakeUpdatable = false;
    // XXX: We need to know when we are on the insert row
    protected boolean m_OnInsert = false;
    // XXX: We need to keep the index references of the columns already assigned for insertion
    protected BitSet m_InsertedColumns;
    // XXX: If ResultSet cannot be updated, we use a RowSetWriter
    // XXX: which allows us to send the correct SQL queries
    protected RowSetWriter m_RowSetWriter = null;
    protected boolean m_IsDeleteVisible;
    protected boolean m_IsInsertVisible;
    protected boolean m_IsUpdateVisible;

    // The constructor method:
    public CachedResultSet(DriverProvider provider,
                           ResultSet result,
                           String query)
        throws SQLException
    {
        m_Provider = provider;
        m_Result = result;
        m_Query = query;
        int rstype = result.getType();
        m_IsDeleteVisible = provider.isDeleteVisible(rstype);
        m_IsInsertVisible = provider.isInsertVisible(rstype);
        m_IsUpdateVisible = provider.isUpdateVisible(rstype);
        m_MakeUpdatable = provider.makeResultSetUpdatable();
        m_FetchSize = result.getFetchSize();
        m_ColumnCount = result.getMetaData().getColumnCount();
        m_IsUpdatable = provider.isResultSetUpdatable(result);
        m_InsertedColumns = new BitSet(m_ColumnCount);
        System.out.println("CachedResultSet() IsUpdatable: " + m_IsUpdatable + " - MakeUpdatable: " + m_MakeUpdatable);
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
    public int getType()
        throws SQLException
    {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }

    @Override
    public int getConcurrency()
        throws SQLException
    {
        int concurrency;
        if (m_MakeUpdatable) {
            concurrency = ResultSet.CONCUR_UPDATABLE;
        }
        else {
            concurrency = m_Result.getConcurrency();
        }
        return concurrency;
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
    public void insertRow()
        throws SQLException
    {
        // XXX: Base does not allow auto-increment columns to be entered,
        // XXX: some drivers force us to update these columns to null
        RowHelper.setDefaultColumnValues(m_Result, m_InsertedColumns);
        m_InsertedColumns.clear();
        m_Result.insertRow();
        m_Result.moveToCurrentRow();
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

    @Override
    public void cancelRowUpdates()
        throws SQLException
    {
        if (m_IsUpdatable) {
            if (isOnInsertRow()) {
                moveToCurrentRow();
            }
            else {
                m_Result.cancelRowUpdates();
            }
        }
        else if (isOnInsertRow()) {
            setInsertMode(false);
        }
    }

    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        if (m_IsUpdatable) {
            m_Result.moveToInsertRow();
        }
        else {
            m_InsertedColumns.clear();
        }
        setInsertMode(true);
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        if (m_IsUpdatable) {
            m_Result.moveToCurrentRow();
        }
        setInsertMode(false);
    }

    @Override
    public Statement getStatement()
        throws SQLException
    {
        return m_Result.getStatement();
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
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBoolean(int index, boolean value)
        throws SQLException
    {
        m_Result.updateBoolean(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateByte(int index, byte value)
        throws SQLException
    {
        m_Result.updateByte(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateShort(int index, short value)
        throws SQLException
    {
        m_Result.updateShort(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateInt(int index, int value)
        throws SQLException
    {
        m_Result.updateInt(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateLong(int index, long value)
        throws SQLException
    {
        m_Result.updateLong(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateFloat(int index, float value)
        throws SQLException
    {
        m_Result.updateFloat(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDouble(int index, double value)
        throws SQLException
    {
        m_Result.updateDouble(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBigDecimal(int index, BigDecimal value)
        throws SQLException
    {
        m_Result.updateBigDecimal(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateString(int index, String value)
        throws SQLException
    {
        m_Result.updateString(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBytes(int index, byte[] value)
        throws SQLException
    {
        m_Result.updateBytes(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateDate(int index, Date value)
        throws SQLException
    {
        m_Result.updateDate(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTime(int index, Time value)
        throws SQLException
    {
        m_Result.updateTime(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateTimestamp(int index, Timestamp value)
        throws SQLException
    {
        m_Result.updateTimestamp(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, int length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, int length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value, int length)
        throws SQLException
    {
        m_Result.updateObject(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateObject(int index, Object value)
        throws SQLException
    {
        m_Result.updateObject(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRef(int index, Ref value)
        throws SQLException
    {
        m_Result.updateRef(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, Blob value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Clob value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateArray(int index, Array value)
        throws SQLException
    {
        m_Result.updateArray(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value, long length)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value, long length)
        throws SQLException 
    {
        m_Result.updateBlob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateClob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value, long length)
        throws SQLException
    {
        m_Result.updateNClob(index, value, length);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNCharacterStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateAsciiStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateAsciiStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBinaryStream(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBinaryStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateCharacterStream(int index, Reader value)
        throws SQLException
    {
        m_Result.updateCharacterStream(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateBlob(int index, InputStream value)
        throws SQLException
    {
        m_Result.updateBlob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, Reader value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateSQLXML(int index, SQLXML value)
        throws SQLException
    {
        m_Result.updateSQLXML(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNString(int index, String value)
        throws SQLException
    {
        m_Result.updateNString(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateNClob(int index, NClob value)
        throws SQLException
    {
        m_Result.updateNClob(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
    }

    @Override
    public void updateRowId(int index, RowId value)
        throws SQLException
    {
        m_Result.updateRowId(index, value);
        if (isOnInsertRow()) {
            m_InsertedColumns.set(index - 1);
        }
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


    // XXX: Protected methods
    protected void checkIndex(int index)
        throws SQLException
    {
        if (index < 1 || index > m_ColumnCount) {
            throw new SQLException("ERROR Index is out of range");
        }
    }

    protected RowSetWriter getRowSetWriter()
        throws SQLException
    {
        if (m_RowSetWriter == null) {
            m_RowSetWriter = new RowSetWriter(m_Provider, m_Result);
        }
        return m_RowSetWriter;
    }

    protected boolean isOnInsertRow()
    {
        return m_OnInsert;
    }

    protected void setInsertMode(boolean mode)
    {
        m_OnInsert = mode;
    }

    protected Row createCurrentRow()
        throws SQLException
    {
        Row row = new Row(m_ColumnCount);
        for (int i = 1; i <= m_ColumnCount; i++) {
            row.initColumnObject(m_Result, i);
        }
        return row;
    }

    protected ResultSet getResultSet()
        throws SQLException
    {
        ResultSet result = null;
        Statement statement = m_Result.getStatement();
        if (statement.isWrapperFor(CallableStatement.class)) {
            result = statement.unwrap(CallableStatement.class).executeQuery();
        }
        else if (statement.isWrapperFor(PreparedStatement.class)) {
            result = statement.unwrap(PreparedStatement.class).executeQuery();
        }
        else if (statement.isWrapperFor(Statement.class)) {
            result = statement.unwrap(Statement.class).executeQuery(m_Query);
        }
        return result;
    }

}
