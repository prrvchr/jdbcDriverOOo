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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.rowset.BaseRow;
import io.github.prrvchr.jdbcdriver.rowset.InsertRow;
import io.github.prrvchr.jdbcdriver.rowset.Row;
import io.github.prrvchr.jdbcdriver.rowset.RowHelper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.Array;
import io.github.prrvchr.uno.sdbc.Blob;
import io.github.prrvchr.uno.sdbc.Clob;
import io.github.prrvchr.uno.sdbc.Ref;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class RowSetSuper<C extends ConnectionSuper, S extends StatementMain<?, ?>>
    extends ResultSetSuper<C, S>
    implements XRowLocate,
               XDeleteRows,
               XCancellable
{

    // XXX: We need to keep reference of the current row (first row is 1)
    protected int m_CurrentRow = 0;
    // XXX: If IsForwardOnly then we need to keep reference of the current cursor (first row is 1)
    protected int m_CurrentCursor = 0;
    // XXX: We need to keep reference of deleted Bookmarks
    protected List<Integer> m_DeletedRows = new ArrayList<>();
    // XXX: We need to keep reference of inserted Bookmarks if m_IsInsertVisible is false
    protected int m_InsertedRow = 0;
    // XXX: We keep a cache of rows already browsed
    protected Vector<Object> m_RowData = null;
    // XXX: We need a specific row for inserting new rows
    private InsertRow m_InsertRow = null;
    // XXX: Is the last value read null
    protected boolean m_WasNull = false;
    protected boolean m_RowCountFinal = false;
    protected boolean m_AfterLast = false;
    protected boolean m_IsInsertVisible = false;
    protected boolean m_IsForwardOnly = true;
    protected int m_ColumnCount = 0;
    protected int m_MinSize = 10;

    // The constructor method:
    public RowSetSuper(String service,
                       String[] services,
                       C connection,
                       java.sql.ResultSet result,
                       S statement)
    throws SQLException
    {
        super(service, services, connection, result, statement, true, false);
        m_ColumnCount = getColumnCount(result, statement);
        m_IsForwardOnly = isForwardOnly(result, statement);
    }

    private static int getColumnCount(java.sql.ResultSet result, StatementMain<?, ?> statement)
        throws SQLException
    {
        try {
            return result.getMetaData().getColumnCount();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, statement);
        }
    }

    private static boolean isForwardOnly(java.sql.ResultSet result, StatementMain<?, ?> statement)
        throws SQLException
    {
        try {
            return result.getType() == java.sql.ResultSet.TYPE_FORWARD_ONLY;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, statement);
        }
    }

    @Override
    protected int _getResultSetType()
        throws WrappedTargetException
    {
        try {
            System.out.println("RowSetSuper._getResultSetType() 1");
            int type = m_Result.getType();
            return type == java.sql.ResultSet.TYPE_FORWARD_ONLY ? java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE : type;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }

    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object object1, Object object2)
    throws SQLException
    {
        //System.out.println("RowSetSuper.compareBookmarks() 1");
        int compare = CompareBookmark.NOT_COMPARABLE;
        int bookmark1 = 0, bookmark2 = 0;
        try {
            bookmark1 = AnyConverter.toInt(object1);
            bookmark2 = AnyConverter.toInt(object2);
        }
        catch (IllegalArgumentException e) { }
        //System.out.println("RowSetSuper.compareBookmarks() 2 Book1: " + bookmark1 + " - Book2: " + bookmark2);
        if (bookmark1 != 0 && bookmark2 != 0) {
            if (bookmark1 < bookmark2) {
                compare = CompareBookmark.LESS;
            }
            else if (bookmark1 > bookmark2) {
                compare = CompareBookmark.GREATER;
            }
            else {
                compare = CompareBookmark.EQUAL;
            }
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_COMPARE_BOOKMARKS, Integer.toString(bookmark1), Integer.toString(bookmark2), Integer.toString(compare));
        return compare;
    }

    @Override
    public Object getBookmark()
    throws SQLException
    {
        int row = getRow();
        System.out.println("sdbcx.RowSetSuper.getBookmark() 1 row: " + row);
        Object bookmark = (row != 0 && !m_DeletedRows.contains(row)) ? row : Any.VOID;
        System.out.println("sdbcx.RowSetSuper.getBookmark() 2 row: " + bookmark.toString());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_BOOKMARK, bookmark.toString());
        return bookmark;
    }

    @Override
    public boolean hasOrderedBookmarks()
    throws SQLException
    {
        return true;
    }

    @Override
    public int hashBookmark(Object object)
    throws SQLException
    {
        System.out.println("sdbcx.RowSetSuper.hashBookmark() 1");
        int bookmark;
        try {
            bookmark = AnyConverter.toInt(object);
        }
        catch (IllegalArgumentException e) {
            throw new SQLException("Bad bookmark", this, StandardSQLState.SQL_INVALID_BOOKMARK_VALUE.text(), 0, null);
        }
        return bookmark;
    }

    @Override
    public boolean moveRelativeToBookmark(Object object, int count)
    throws SQLException
    {
        System.out.println("sdbcx.RowSetSuper.moveRelativeToBookmark() 1");
        boolean moved = false;
        int bookmark = AnyConverter.toInt(object);
        int row = getBookmarkRow(bookmark);
        if (absolute(row)) {
            moved = relative(count);
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK, Integer.toString(count), Integer.toString(row), Boolean.toString(moved));
        if (!moved) {
            afterLast();
        }
        return moved;
    }

    @Override
    public boolean moveToBookmark(Object object)
    throws SQLException
    {
        int bookmark = AnyConverter.toInt(object);
        System.out.println("sdbcx.RowSetSuper.moveToBookmark() 1 Bookmark: " + bookmark);
        int row = getBookmarkRow(bookmark);
        System.out.println("sdbcx.RowSetSuper.moveToBookmark() 2 Row: " + row);
        boolean moved = absolute(row);
        if (!moved) {
            afterLast();
        }
        System.out.println("sdbcx.RowSetSuper.moveToBookmark() 3 Moved: " + moved);
        return moved;
    }

    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] objects)
    throws SQLException
    {
        System.out.println("RowSetSuper.deleteRows() 1 Size: " + getRowCount());
        List<Integer> rows = new ArrayList<Integer>();
        // XXX: We need to iterate in reverse order if we want to be able to do it in one go
        for (int i = 0; i < objects.length; i ++) {
            int bookmark = AnyConverter.toInt(objects[i]);
            int row = getBookmarkRow(bookmark);
            Object object = objects[i];
            if (deleteRow(row)) {
                rows.add(0, 1);
                System.out.println("RowSetSuper.deleteRows() 2 Row: " + AnyConverter.toInt(object));
            }
            else {
                rows.add(0, 0);
            }
        }
        System.out.println("RowSetSuper.deleteRows() 3 Size: " + getRowCount());
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        // TODO: implement me
    }


    // com.sun.star.sdbc.XResultSetUpdate:
    // XXX: If we want to be able to use a CachedRowset instead
    // XXX: of a ResultSet we must overwrite these methods...
    @Override
    public void insertRow() throws SQLException
    {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow() cannot be called when moveToInsertRow has not been called !", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        try {
            System.out.println("RowSetSuper.insertRow() 1 Size: " + getRowCount());
            // XXX: Move to inserted row
            m_Result.moveToInsertRow();
            // XXX: Set to null all columns that have not been entered
            // XXX: if they can be nullable or declared as auto-increment
            if (!m_InsertRow.isCompleteRow(m_Result.getMetaData())) {
                RowHelper.setDefaultColumnValues(m_Result, m_InsertRow.getInserted());
            }
            // XXX: Set to all columns that have been entered
            m_InsertedRow = RowHelper.setColumnValues(m_Result, m_InsertRow, m_ColumnCount, getRowCount() + 1);
            if (m_InsertedRow != 0) {
                // XXX: Obtaining default values or auto-increments will be obtained by reloading the ResultSet
                //getRowData().add(new Row(m_ColumnCount, m_InsertRow.getValues()));
                m_Result.insertRow();
            }
            m_Result.moveToCurrentRow();
            m_InsertRow = null;
            if (m_InsertedRow != 0) {
                m_CurrentRow = m_InsertedRow;
                m_RowCountFinal = false;
                if (!m_IsInsertVisible) {
                    reloadResultSet();
                }
                System.out.println("RowSetSuper.insertRow() 2");
                moveAbsolute(m_CurrentRow);
            }
            System.out.println("RowSetSuper.insertRow() 3 Size: " + getRowCount());
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_INSERT_ROW);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateRow() throws SQLException
    {
        try {
            Row row = (Row) getCurrentRow();
            if (row.isUpdated()) {
                moveAbsolute(m_CurrentRow);
                System.out.println("RowSetSuper.updateRow() 2");
                if (RowHelper.setColumnValues(m_Result, row, m_ColumnCount)) {
                    m_Result.updateRow();
                }
                System.out.println("RowSetSuper.updateRow() 3");
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void deleteRow() throws SQLException
    {
        System.out.println("RowSetSuper.deleteRow() 1 Size: " + getRowCount());
        deleteRow(m_CurrentRow);
        System.out.println("RowSetSuper.deleteRow() 2 Size: " + getRowCount());
    }

    private boolean deleteRow(int row)
        throws SQLException
    {
        try {
            boolean deleted = false;
            System.out.println("RowSetSuper.deleteRow() 1 Size: " + getRowCount());
            if (moveAbsolute(row)) {
                m_Result.deleteRow();
                System.out.println("RowSetSuper.deleteRow() 2 Bookmark: " + row);
                m_DeletedRows.add(row);
                getRowData().remove(row);
                reloadResultSet();
                deleted = true;
            }
            System.out.println("RowSetSuper.deleteRow() 3 Size: " + getRowCount());
            return deleted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 111: xUp->cancelRowUpdates()
    @Override
    public void cancelRowUpdates() throws SQLException
    {
        System.out.println("RowSetSuper.cancelRowUpdates() 1 OnInsert: " + isOnInsertRow());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
        // FIXME: *** LibreOffice Base call this method just after calling moveToInsertRow() ***
        // FIXME: Java documentation say: Throws: SQLException - if a database access error occurs;
        // FIXME: this method is called on a closed result set; the result set concurrency is CONCUR_READ_ONLY 
        // FIXME: or if this method is called when the cursor is on the insert row
        // FIXME: see: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#cancelRowUpdates--
        if (isOnInsertRow()) {
            m_InsertRow = null;
        }
        System.out.println("RowSetSuper.cancelRowUpdates() 2 OnInsert: " + isOnInsertRow());
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 110: xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow() throws SQLException
    {
        System.out.println("RowSetSuper.moveToInsertRow() 1 OnInsert: " + isOnInsertRow());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERTROW);
        if (!isOnInsertRow()) {
            m_InsertRow = new InsertRow(m_ColumnCount);
        }
        System.out.println("RowSetSuper.moveToInsertRow() 2 OnInsert: " + isOnInsertRow());
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        System.out.println("RowSetSuper.moveToCurrentRow() 1 OnInsert: " + isOnInsertRow());
    }


    // com.sun.star.sdbc.XResultSet:
    // XXX: If we want to be able to use a CachedRowset instead
    // XXX: of a ResultSet we must overwrite these methods...
    @Override
    public boolean absolute(int row)
        throws SQLException
    {
        System.out.println("sdbcx.RowSetSuper.absolute() 1 row: " + Integer.toString(row));
        boolean moved = absolute(row, getRowCount());
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_BOOKMARK, Integer.toString(row), Boolean.toString(moved));
        System.out.println("sdbcx.RowSetSuper.absolute() 2 row: " + Integer.toString(row) + " - moved: " + moved);
        return moved;
    }

    @Override
    public void afterLast() throws SQLException
    {
        System.out.println("RowSetSuper.afterLast() 1");
        m_CurrentRow = getRowCount() + 1;
        System.out.println("RowSetSuper.afterLast() 2");
    }

    @Override
    public void beforeFirst() throws SQLException
    {
        System.out.println("RowSetSuper.beforeFirst() 1");
        m_CurrentRow = 0;
        System.out.println("RowSetSuper.beforeFirst() 2");
    }

    @Override
    public boolean first()
        throws SQLException
    {
        boolean first = absolute(1);
        System.out.println("RowSetSuper.first() : " + first);
        return first;
    }

    @Override
    public int getRow() throws SQLException
    {
        System.out.println("sdbcx.RowSetSuper.getRow() 1");
        if (m_CurrentRow > getRowCount() && m_RowCountFinal || m_CurrentRow <= 0) {
            return 0;
        }
        return m_CurrentRow;
    }

    @Override
    public boolean isAfterLast() throws SQLException
    {
        return m_RowCountFinal && m_CurrentRow > getRowCount();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        return m_CurrentRow == 0;
    }

    @Override
    public boolean isFirst() throws SQLException
    {
        boolean first = m_CurrentRow == 1;
        System.out.println("sdbcx.RowSetSuper.isFirst() : " + first);
        return first;
    }

    @Override
    public boolean isLast() throws SQLException
    {
        boolean last = m_RowCountFinal && m_CurrentRow == getRowCount();
        System.out.println("sdbcx.RowSetSuper.isLast() : " + last);
        return last;
    }

    @Override
    public boolean last()
        throws SQLException
    {
        boolean last = absolute(-1);
        System.out.println("sdbcx.RowSetSuper.last() : " + last);
        return last;
    }

    @Override
    public boolean next()
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.next() 1 Row: " + m_CurrentRow);
            boolean next = moveAbsolute(m_CurrentRow + 1);
            if (next) {
                m_CurrentRow ++;
                if (m_CurrentRow > getRowCount()) {
                    setCurrentRow();
                }
            }
            else {
                m_RowCountFinal = true;
                m_CurrentRow = getRowCount() + 1;
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_NEXT, Boolean.toString(next));
            System.out.println("RowSetSuper.next() 2 next: " + next);
            return next;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean previous()
        throws SQLException
    {
        System.out.println("RowSetSuper.previous() 1 Row: " + m_CurrentRow);
        boolean previous = relative(-1);
        System.out.println("RowSetSuper.previous() 2 previous: " + previous);
        return previous;
    }

    @Override
    public boolean relative(int row)
        throws SQLException
    {
        System.out.println("sdbcx.RowSetSuper.relative() 1 row: " + Integer.toString(row));
        boolean moved = false;
        int count = getRowCount();
        int absolute = m_CurrentRow + row;
        if (absolute <= 0) {
            beforeFirst();
        }
        else {
            moved = absolute(absolute, count);
        }
        return moved;
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        try {
            boolean deleted = false;
            if (m_IsInsertVisible) {
                deleted = m_Result.rowDeleted();
            }
            else {
                deleted = m_DeletedRows.contains(m_CurrentRow);
            }
            System.out.println("sdbcx.RowSetSuper.rowDeleted() 1 row: " + m_CurrentRow + " - Deleted: " + deleted);
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
            return deleted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        try {
            boolean inserted = false;
            if (m_IsInsertVisible) {
                inserted = m_Result.rowInserted();
            }
            else {
                inserted = m_InsertedRow != 0 && m_InsertedRow == m_CurrentRow;
            }
            m_InsertedRow = 0;
            System.out.println("sdbcx.RowSetSuper.rowInserted() 1 Inserted: " + inserted);
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
            return inserted;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Array value = (java.sql.Array) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? null : new Array(m_Connection, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public XInputStream getBinaryStream(int index)
        throws SQLException
    {
        XBlob value = getBlob(index);
        return m_WasNull ? null : value.getBinaryStream();
    }

    @Override
    public XBlob getBlob(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Blob value = (java.sql.Blob) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? null : new Blob(m_Connection, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getBoolean() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Boolean value = (Boolean) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? false : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getByte() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Byte value = (Byte) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? (byte)0 : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getBytes() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            byte[] value = (byte[]) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? new byte[0] : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index)
        throws SQLException
    {
        XClob value = getClob(index);
        return m_WasNull ? null : value.getCharacterStream();
    }

    @Override
    public XClob getClob(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Clob value = (java.sql.Clob) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? null : new Clob(m_Connection, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Date getDate(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Date value = (java.sql.Date) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? new Date() : UnoHelper.getUnoDate(value.toLocalDate());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public double getDouble(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            Double value = (Double) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? 0D : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            Float value = (Float) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? 0F : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getInt() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Integer value = (Integer) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? 0 : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        System.out.println("RowSetSuper.getLong() 1");
        try {
            System.out.println("RowSetSuper.getLong() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Long value = (Long) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? 0L : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getObject() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Object value = getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? Any.VOID : DBTools.getObject(value, map);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getLoggedSQLException(this, getLogger(), e);
        }
    }

    @Override
    public XRef getRef(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Ref value = (java.sql.Ref) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? null : new Ref(value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getShort() 1 Index: " + index);
            checkIndex(index);
            checkCursor();
            Short value = (Short) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? 0 : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        try {
            System.out.println("RowSetSuper.getString() 1");
            checkIndex(index);
            checkCursor();
            String value = (String) getCurrentColumn(index);
            m_WasNull = value == null;
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_PARAMETER, value, "getString", Integer.toString(index));
            System.out.println("RowSetSuper.getString() 1 Index: " + index + " - Value: " + value);
            return m_WasNull ? "" : value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Time value = (java.sql.Time) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? new Time() : UnoHelper.getUnoTime(value.toLocalTime());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public DateTime getTimestamp(int index)
        throws SQLException
    {
        try {
            checkIndex(index);
            checkCursor();
            java.sql.Timestamp value = (java.sql.Timestamp) getCurrentColumn(index);
            m_WasNull = value == null;
            return m_WasNull ? new DateTime() : UnoHelper.getUnoDateTime(value.toLocalDateTime());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        System.out.println("RowSetSuper.wasNull() 1");
        checkCursor();
        return m_WasNull;
    }


    // com.sun.star.sdbc.XRowUpdate:
    @Override
    public void updateNull(int index) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, null);
    }

    @Override
    public void updateBoolean(int index, boolean value)
    throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateByte(int index, byte value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateShort(int index, short value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateInt(int index, int value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateLong(int index, long value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateFloat(int index, float value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateDouble(int index, double value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateString(int index, String value) throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_UPDATE_PARAMETER, "updateString", Integer.toString(index), value);
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateBytes(int index, byte[] value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateDate(int index, Date value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateTime(int index, Time value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateTimestamp(int index, DateTime value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateBinaryStream(int index, XInputStream value, int lenght) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateCharacterStream(int index, XInputStream value, int lenght) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateObject(int index, Object value) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }

    @Override
    public void updateNumericObject(int index, Object value, int scale) throws SQLException
    {
        checkIndex(index);
        checkCursor();
        getCurrentRow().setColumnObject(index, value);
    }


    private boolean absolute(int row, int count)
        throws SQLException
    {
        try {
            int absolute = 0;
            boolean moved = false;
            boolean afterlast = false;
            boolean beforefirst = false;
            if (m_RowCountFinal && row > count) {
                afterlast = true;
            }
            else if (row == 0 || (m_RowCountFinal && count + row < 0)) {
                beforefirst = true;
            }
            else if (row > 0) {
                absolute = row;
                moved = moveAbsolute(absolute);
                afterlast = !moved;
            }
            else if (m_RowCountFinal) {
                absolute = count + row + 1;
                moved = moveAbsolute(absolute);
                beforefirst = !moved;
            }
            else {
                System.out.println("sdbcx.RowSetSuper.absolute2() ERROR row: " + row + " - moved: " + moved);
            }

            System.out.println("sdbcx.RowSetSuper.absolute2() 1 row: " + row + " - moved: " + moved);
            if (moved) {
                m_CurrentRow = absolute;
                if (absolute > count) {
                    m_RowCountFinal = false;
                    setCurrentRow();
                }
            }
            System.out.println("sdbcx.RowSetSuper.absolute2() 2 row: " + row + " - count: " + count);

            if (afterlast) {
                m_RowCountFinal = true;
                afterLast();
            }
            else if (beforefirst) {
                beforeFirst();
            }
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    private void setCurrentRow()
        throws java.sql.SQLException
    {
        Row row = new Row(m_ColumnCount);
        for (int i = 1; i <= m_ColumnCount; i++) {
            row.initColumnObject(m_Result, i);
        }
        getRowData().add(row);
    }

    private Vector<Object> getRowData()
        throws java.sql.SQLException
    {
        if (m_RowData == null) {
            int size = Math.max(m_MinSize, m_Result.getFetchSize());
            m_RowData = new Vector<Object>(size);
        }
        return m_RowData;
    }

    private BaseRow getCurrentRow() {
        if (isOnInsertRow()) {
            return (BaseRow) m_InsertRow;
        }
        else {
            return (BaseRow)(m_RowData.get(m_CurrentRow - 1));
        }
    }

    private Object getCurrentColumn(int index)
        throws java.sql.SQLException
    {
        return getCurrentRow().getColumnObject(index);
    }

    private boolean moveAbsolute(int row)
        throws SQLException, java.sql.SQLException
    {
        boolean moved = false;
        if (m_IsForwardOnly) {
            // XXX: If the specified row number is zero, the cursor is moved
            // XXX: before the first row if it is not already at that position.
            if (row < m_CurrentCursor || m_AfterLast) {
                // XXX: It's a forward only ResultSet, we need to reload the ResultSet
                reloadResultSet();
            }
            if (row == m_CurrentCursor) {
                moved = !(m_AfterLast || m_CurrentCursor == 0);
            }
            else {
                while (m_CurrentCursor < row && !m_AfterLast) {
                    boolean next = m_Result.next();
                    if (next) {
                        m_CurrentCursor ++;
                    }
                    else {
                        m_AfterLast = true;
                    }
                }
                moved = !m_AfterLast;
            }
        }
        else {
            moved = m_Result.absolute(row);
        }
        return moved;
    }

    @Override
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    private int getBookmarkRow(int bookmark)
    {
        return bookmark - getRowOffset(bookmark);
    }

    private int getRowOffset(int bookmark)
    {
        int offset = 0;
        for (int row : m_DeletedRows) {
            if (row < bookmark) {
                offset ++;
            }
        }
        return offset;
    }

    private int getRowCount()
    {
        return m_RowData != null ? m_RowData.size() : 0;
    }

    private boolean isOnInsertRow()
    {
        return m_InsertRow != null;
    }

    private void reloadResultSet()
        throws SQLException, java.sql.SQLException
    {
        m_Result.close();
        m_Result = getJdbcResultSet();
        m_AfterLast = false;
        m_CurrentCursor = 0;
    }

    private void checkIndex(int index) throws SQLException {
        if (index < 1 || index > m_ColumnCount) {
            throw new SQLException("ERROR", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    private void checkCursor() throws SQLException {
        if (!isOnInsertRow() && (isAfterLast() || isBeforeFirst())) {
            throw new SQLException("ERROR", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

}
