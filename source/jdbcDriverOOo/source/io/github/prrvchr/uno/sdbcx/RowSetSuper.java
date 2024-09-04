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

import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.helper.DBException;
import io.github.prrvchr.jdbcdriver.resultset.CachedResultSet;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class RowSetSuper
    extends ResultSetSuper
    implements XRowLocate,
               XDeleteRows,
               XCancellable
{

    // The constructor method:
    public RowSetSuper(String service,
                       String[] services,
                       DriverProvider provider,
                       ConnectionSuper connection,
                       CachedResultSet result,
                       StatementMain statement)
        throws SQLException
    {
        super(service, services, connection, result, statement, true, true);
        showResultVisibility(provider, result);
    }

    private void showResultVisibility(DriverProvider provider,
                                      java.sql.ResultSet result)
        throws SQLException
    {
        try {
            int rstype = result.getType();
            boolean deletevisible = provider.isDeleteVisible(rstype);
            boolean insertvisible = provider.isInsertVisible(rstype);
            boolean updatevisible = provider.isUpdateVisible(rstype);
            System.out.println("RowSetSuper() Delete are visible: " + deletevisible);
            System.out.println("RowSetSuper() Insert are visible: " + insertvisible);
            System.out.println("RowSetSuper() Update are visible: " + updatevisible);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

    protected CachedResultSet getResultSet()
    {
        return (CachedResultSet) m_Result;
    }

    @Override
    protected int _getResultSetConcurrency()
        throws WrappedTargetException
    {
        // XXX: We want to emulate an updateable ResultSet
        return java.sql.ResultSet.CONCUR_UPDATABLE;
    }

    @Override
    protected int _getResultSetType()
        throws WrappedTargetException
    {
        // XXX: We want to emulate an scollable ResultSet
        return java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;
    }

    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object bookmark1, Object bookmark2)
        throws SQLException
    {
        int compare = CompareBookmark.NOT_COMPARABLE;
        int row1 = 0, row2 = 0;
        try {
            row1 = AnyConverter.toInt(bookmark1);
            row2 = AnyConverter.toInt(bookmark2);
        }
        catch (IllegalArgumentException e) { }
        if (row1 != 0 && row2 != 0) {
            if (row1 < row2) {
                compare = CompareBookmark.LESS;
            }
            else if (row1 > row2) {
                compare = CompareBookmark.GREATER;
            }
            else {
                compare = CompareBookmark.EQUAL;
            }
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_COMPARE_BOOKMARKS, Integer.toString(row1), Integer.toString(row2), Integer.toString(compare));
        return compare;
    }

    @Override
    public Object getBookmark()
        throws SQLException
    {
        int row = getResultSet().getBookmark();
        Object bookmark = (row != 0) ? row : Any.VOID;
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
    public int hashBookmark(Object bookmark)
        throws SQLException
    {
        int row;
        try {
            row = AnyConverter.toInt(bookmark);
        }
        catch (IllegalArgumentException e) {
            throw new SQLException("Bad bookmark", this, StandardSQLState.SQL_INVALID_BOOKMARK_VALUE.text(), 0, null);
        }
        return row;
    }

    @Override
    public boolean moveRelativeToBookmark(Object bookmark, int count)
        throws SQLException
    {
        try {
            boolean moved = false;
            int row = AnyConverter.toInt(bookmark);
            if (getResultSet().moveToBookmark(row)) {
                moved = relative(count);
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK, Integer.toString(count), Integer.toString(row), Boolean.toString(moved));
            if (!moved) {
                afterLast();
            }
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean moveToBookmark(Object bookmark)
        throws SQLException
    {
        try {
            int row = AnyConverter.toInt(bookmark);
            boolean moved = getResultSet().moveToBookmark(row);
            if (!moved) {
                afterLast();
            }
            return moved;
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
        throws SQLException
    {
        List<Integer> rows = new ArrayList<Integer>();
        for (Object bookmark : bookmarks) {
            int row = AnyConverter.toInt(bookmark);
            if (absolute(row)) {
                deleteRow();
                rows.add(0, 1);
            }
            else {
                rows.add(0, 0);
            }
        }
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel()
    {
        // TODO: implement me
    }


    // com.sun.star.sdbc.XResultSetUpdate:
    // XXX: If we want to be able to use a CachedRowset instead
    // XXX: of a ResultSet we must overwrite these methods...
    @Override
    public void insertRow() throws SQLException
    {
        try {
            m_Result.insertRow();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public void updateRow() throws SQLException
    {
        try {
            m_Result.updateRow();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public void deleteRow() throws SQLException
    {
        try {
            m_Result.deleteRow();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 111: xUp->cancelRowUpdates()
    @Override
    public void cancelRowUpdates() throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANCEL_ROW_UPDATES);
        // FIXME: *** LibreOffice Base call this method just after calling moveToInsertRow() ***
        // FIXME: Java documentation say: Throws: SQLException - if a database access error occurs;
        // FIXME: this method is called on a closed result set; the result set concurrency is CONCUR_READ_ONLY 
        // FIXME: or if this method is called when the cursor is on the insert row
        // FIXME: see: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#cancelRowUpdates--
        try {
            m_Result.cancelRowUpdates();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    // XXX: see: libreoffice/dbaccess/source/core/api/RowSetCache.cxx  Line 110: xUp->moveToInsertRow()
    @Override
    public void moveToInsertRow() throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_INSERTROW);
        try {
            m_Result.moveToInsertRow();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException
    {
        try {
            m_Result.moveToCurrentRow();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        boolean deleted = false;
        try {
            deleted = m_Result.rowDeleted();
        }
        catch (java.sql.SQLException e) {
            throw new SQLException();
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
        return deleted;
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        boolean inserted = false;
        try {
            inserted = m_Result.rowInserted();
        }
        catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
        return inserted;
    }

}
