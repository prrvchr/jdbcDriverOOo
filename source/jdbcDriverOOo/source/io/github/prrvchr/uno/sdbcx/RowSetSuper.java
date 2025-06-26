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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.uno.driver.helper.DBException;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class RowSetSuper
    extends ResultSetSuper
    implements XRowLocate,
               XDeleteRows,
               XCancellable {

    private java.sql.Connection mRsConnection;
    private boolean mAutoCommit;
    private List<Integer> mRowInserted = new ArrayList<>();

    // The constructor method:
    protected RowSetSuper(String service,
                          String[] services,
                          ConnectionSuper connection,
                          CachedRowSet rowset,
                          StatementMain statement)
        throws SQLException {
        super(service, services, connection, rowset, statement, true, true, true);
        try {
            java.sql.Connection con = connection.getProvider().getConnection();
            mAutoCommit = con.getAutoCommit();
            // XXX: To use a CachedRowSet, the connection must not be in auto-commit.
            // XXX: The auto-commit initial value will be restored when closing the RowSet.
            con.setAutoCommit(false);
            mRsConnection = con;
        } catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
    }

    protected CachedRowSet getResultSet() {
        return (CachedRowSet) mResult;
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException {
        return super.getJdbcResultSet();
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    // com.sun.star.sdbc.XCloseable
    @Override
    public void close()
        throws SQLException {
        try {
            mRsConnection.setAutoCommit(mAutoCommit);
            super.close();
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean isBeforeFirst()
        throws SQLException {
        boolean before = false;
        if (getResultSet().size() > 0) {
            before = super.isBeforeFirst();
        }
        return before;
    }

    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object bookmark1, Object bookmark2)
        throws SQLException {
        System.out.println("CachedRowSetSuper.compareBookmarks() 1");
        int compare = CompareBookmark.NOT_COMPARABLE;
        int row1 = 0, row2 = 0;
        try {
            row1 = AnyConverter.toInt(bookmark1);
            row2 = AnyConverter.toInt(bookmark2);
        } catch (IllegalArgumentException e) { }
        if (row1 != 0 && row2 != 0) {
            if (row1 < row2) {
                compare = CompareBookmark.LESS;
            } else if (row1 > row2) {
                compare = CompareBookmark.GREATER;
            }  else {
                compare = CompareBookmark.EQUAL;
            }
        }
        if (compare != CompareBookmark.EQUAL) {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_COMPARE_BOOKMARKS,
                               Integer.toString(row1), Integer.toString(row2), Integer.toString(compare));
        }
        return compare;
    }

    @Override
    public Object getBookmark()
        throws SQLException {
        int row = 0;
        Object bookmark = Any.VOID;
        try {
            row = mResult.getRow();
            if (row != 0) {
                bookmark = row;
            }
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
        System.out.println("CachedRowSetSuper.getBookmark() 1 bookmark: " + row);
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_BOOKMARK, bookmark.toString());
        return bookmark;
    }

    @Override
    public boolean hasOrderedBookmarks()
        throws SQLException {
        return true;
    }

    @Override
    public int hashBookmark(Object bookmark)
        throws SQLException {
        int row;
        try {
            row = AnyConverter.toInt(bookmark);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Bad bookmark", this, StandardSQLState.SQL_INVALID_BOOKMARK_VALUE.text(), 0, null);
        }
        return row;
    }

    @Override
    public boolean moveRelativeToBookmark(Object bookmark, int count)
        throws SQLException {
        try {
            System.out.println("CachedRowSetSuper.moveRelativeToBookmark() 1");
            boolean moved = false;
            if (moveToBookmark(bookmark)) {
                moved = mResult.relative(count);
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK,
                               count, AnyConverter.toInt(bookmark), moved);
            /**if (!moved) {
                mResult.afterLast();
            }*/
            return moved;
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean moveToBookmark(Object bookmark)
        throws SQLException {
        try {
            System.out.println("CachedRowSetSuper.moveToBookmark() 1 bookmark" + bookmark);
            int row = AnyConverter.toInt(bookmark);
            System.out.println("CachedRowSetSuper.moveToBookmark() 2 bookmark: " + row);
            boolean moved = mResult.absolute(row);
            /**if (!moved) {
                System.out.println("CachedRowSetSuper.moveToBookmark() 3");
                mResult.afterLast();
            }*/
            System.out.println("CachedRowSetSuper.moveToBookmark() 4 moved: " + moved);
            return moved;
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
        throws SQLException {
        List<Integer> rows = new ArrayList<Integer>();
        for (Object bookmark : bookmarks) {
            int row = AnyConverter.toInt(bookmark);
            try {
                if (mResult.absolute(row)) {
                    deleteRow();
                    rows.add(0, 1);
                } else {
                    rows.add(0, 0);
                }
            } catch (java.sql.SQLException e) {
                throw DBException.getSQLException(this, e);
            }
        }
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }

    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        // XXX: implement me
    }

    // com.sun.star.sdbc.XResultSetUpdate:
    // XXX: If we want to be able to use a CachedRowset instead
    // XXX: of a ResultSet we must overwrite these methods...
    @Override
    public void insertRow() throws SQLException {
        try {
            System.out.println("CachedRowSetSuper.insertRow() 1");
            ResultSetHelper.setDefaultColumnValues(getResultSet(), mInserted);
            System.out.println("CachedRowSetSuper.insertRow() 2");
            super.insertRow();
            System.out.println("CachedRowSetSuper.insertRow() 3");
            // XXX: the insert will be commited
            getResultSet().acceptChanges(mRsConnection);
            System.out.println("CachedRowSetSuper.insertRow() 4");
            // XXX: the last insert become the current row
            mResult.last();
            mRowInserted.add(mResult.getRow());
            System.out.println("CachedRowSetSuper.insertRow() 5");
        } catch (java.sql.SQLException e) {
            System.out.println("CachedRowSetSuper.insertRow() ERROR: " + UnoHelper.getStackTrace(e));
            throw DBException.getSQLException(this, e);
        } catch (Throwable e) {
            System.out.println("CachedRowSetSuper.insertRow() ERROR: " + UnoHelper.getStackTrace(e));
        }
    }

    @Override
    public void updateRow() throws SQLException {
        try {
            mResult.updateRow();
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public void deleteRow() throws SQLException {
        try {
            mResult.deleteRow();
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        boolean deleted = false;
        System.out.println("CachedRowSetSuper.rowDeleted() 1");
        try {
            deleted = mResult.rowDeleted();
        } catch (java.sql.SQLException e) {
            throw new SQLException();
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_DELETED, Boolean.toString(deleted));
        return deleted;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        boolean inserted = false;
        try {
            inserted = mRowInserted.contains(mResult.getRow());
            System.out.println("CachedRowSetSuper.rowInserted() 1: " + inserted);
        } catch (java.sql.SQLException e) {
            throw DBException.getSQLException(this, e);
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_INSERTED, Boolean.toString(inserted));
        return inserted;
    }

    @Override
    public boolean rowUpdated()
        throws SQLException {
        try {
            System.out.println("CachedRowSetSuper.rowUpdated() 1");
            boolean updated = mResult.rowUpdated();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ROW_UPDATED, Boolean.toString(updated));
            return updated;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    @Override
    public void clearWarnings()
        throws SQLException {
        System.out.println("CachedRowSet.clearWarnings() 1");
    }

    @Override
    public Object getWarnings()
        throws SQLException {
        System.out.println("CachedRowSet.getWarnings() 1");
        return Any.VOID;
    }

}
