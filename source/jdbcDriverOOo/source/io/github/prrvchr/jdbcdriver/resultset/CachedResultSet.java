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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.BitSet;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;
import io.github.prrvchr.jdbcdriver.rowset.RowSetWriter;


public class CachedResultSet
    extends ResultSet
{

    protected DriverProvider m_Provider;
    protected int m_MinSize = 10;
    protected int m_ColumnCount = 0;
    protected int m_FetchSize;
    protected String m_Query;
    protected boolean m_IsUpdatable = false;
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
                           java.sql.ResultSet result,
                           String query)
        throws SQLException
    {
        super(result);
        m_Provider = provider;
        m_Query = query;
        int rstype = result.getType();
        m_IsDeleteVisible = provider.isDeleteVisible(rstype);
        m_IsInsertVisible = provider.isInsertVisible(rstype);
        m_IsUpdateVisible = provider.isUpdateVisible(rstype);
        m_FetchSize = result.getFetchSize();
        m_ColumnCount = result.getMetaData().getColumnCount();
        m_IsUpdatable = provider.isResultSetUpdatable(result);
        m_InsertedColumns = new BitSet(m_ColumnCount);
        System.out.println("CachedResultSet() IsUpdatable: " + m_IsUpdatable);
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
        moveToCurrentRow();
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
        if (isOnInsertRow()) {
            setInsertMode(false);
        }
    }

    @Override
    public void moveToInsertRow()
        throws SQLException
    {
        if (m_IsUpdatable) {
            m_Result.moveToInsertRow();
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
    public int getConcurrency()
        throws SQLException
    {
        return m_Result.getConcurrency();
    }

    @Override
    public int getType()
        throws SQLException
    {
        //return ResultSet.TYPE_SCROLL_SENSITIVE;
        return m_Result.getType();
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

    protected Row getResultRow()
        throws SQLException
    {
        Row row = new Row(m_ColumnCount);
        for (int i = 1; i <= m_ColumnCount; i++) {
            row.initColumnObject(m_Result, i);
        }
        return row;
    }

}
