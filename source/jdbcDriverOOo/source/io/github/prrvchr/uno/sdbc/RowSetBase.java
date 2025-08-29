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
package io.github.prrvchr.uno.sdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Resources;


public abstract class RowSetBase
    extends ResultSetBase
    implements XRowLocate,
               XDeleteRows,
               XCancellable {

    // XXX: We need to know if a row has been inserted and after this insertion
    // XXX: when moveToCurrentRow is called then acceptChange will be triggered
    // XXX: and the insertion will be performed in the database.
    private boolean mRowInserted = false;

    // The constructor method:
    protected RowSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         CachedRowSet rowset,
                         StatementMain statement)
        throws SQLException {
        super(service, services, connection, rowset, statement, true);
    }

    protected CachedRowSet getRowSet() {
        return (CachedRowSet) mResult;
    }

    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }


    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object bookmark1, Object bookmark2)
        throws SQLException {
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
        try {
            // XXX: Base can call getBookmark() while still on
            // XXX: the insert row. See tdf#168145
            if (mOnInsert) {
                moveToCurrentRowInternal();
            }
            // XXX: If an insert was made, we need to validate that insert.
            if (mRowInserted) {
                acceptInsertInternal();
            }
            Object bookmark = Any.VOID;
            boolean showdeleted = getRowSet().getShowDeleted();
            getRowSet().setShowDeleted(true);
            int row = mResult.getRow();
            getRowSet().setShowDeleted(showdeleted);
            if (row != 0) {
                bookmark = row;
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_BOOKMARK, bookmark.toString());
            return bookmark;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
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

    // XXX: If the bookmark could not be located, the result set will be positioned after the last record.
    // XXX: https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html#moveRelativeToBookmark
    @Override
    public boolean moveRelativeToBookmark(Object bookmark, int count)
        throws SQLException {
        try {
            boolean moved = false;
            if (moveToBookmarkInternal(bookmark)) {
                moved = mResult.relative(count);
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK,
                               count, AnyConverter.toInt(bookmark), moved);
            if (!moved) {
                mResult.afterLast();
            }
            return moved;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    // XXX: If the bookmark could not be located, the result set will be positioned after the last record.
    // XXX: https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html#moveToBookmark
    @Override
    public boolean moveToBookmark(Object bookmark)
        throws SQLException {
        try {
            boolean moved = moveToBookmarkInternal(bookmark);
            if (!moved) {
                mResult.afterLast();
            }
            return moved;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    private boolean moveToBookmarkInternal(Object bookmark) throws java.sql.SQLException {
        boolean showdeleted = getRowSet().getShowDeleted();
        getRowSet().setShowDeleted(true);
        boolean moved = mResult.absolute(AnyConverter.toInt(bookmark));
        getRowSet().setShowDeleted(showdeleted);
        return moved;
    }

    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
        throws SQLException {
        try {
            List<Integer> rows = new ArrayList<Integer>();
            int index = 0;
            for (Object bookmark : bookmarks) {
                int row = AnyConverter.toInt(bookmark);
                if (mResult.absolute(row)) {
                    deleteRow();
                    rows.add(index, 1);
                } else {
                    rows.add(index, 0);
                }
                index++;
            }
            return rows.stream().mapToInt(Integer::intValue).toArray();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        // XXX: implement me
    }

    // com.sun.star.sdbc.XResultSetUpdate
    // XXX: If we want to be able to use a CachedRowset instead
    // XXX: of a ResultSet we must overwrite these methods...
    @Override
    public void insertRow() throws SQLException {
        if (!isOnInsertRow()) {
            throw new SQLException("ERROR: insertRow cannot be called when moveToInsertRow has not been called !", this,
                                   StandardSQLState.SQL_GENERAL_ERROR.text(), 0, null);
        }
        try {
            insertRowInternal();
            // XXX: We cannot commit this insert while we are on the insert row.
            // XXX: So we keep the fact that we performed an insert.
            mRowInserted = true;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    public void moveToCurrentRow()
        throws SQLException {
        try {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_CURRENT_ROW);
            moveToCurrentRowInternal();
            // XXX: we need to check if an insertion has not taken
            // XXX: place and in this case commit this insertion.
            if (mRowInserted) {
                acceptInsertInternal();
            }
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    public void updateRow() throws SQLException {
        try {
            mResult.updateRow();
            // XXX: the update will be committed
            acceptChanges();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    public void deleteRow() throws SQLException {
        try {
            mResult.deleteRow();
            // XXX: the delete will be committed
            acceptChanges();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public void clearWarnings()
        throws SQLException {
        System.out.println("RowSetSuper.clearWarnings() 1");
    }

    @Override
    public Object getWarnings()
        throws SQLException {
        return Any.VOID;
    }

    private void acceptInsertInternal() throws java.sql.SQLException {
        // XXX: We must position the cursor on the new inserted row (ie: last row)
        mResult.last();
        acceptChanges();
        mRowInserted = false;
    }

    private void acceptChanges() throws java.sql.SQLException {
        try {
            getRowSet().acceptChanges(mConnection.getProvider().getConnection());
        } catch (SyncProviderException spe) {
            // XXX: If conflicts occur then the current operation will be canceled
            // XXX: If we want to be able to undoDelete we need to show deleted row
            boolean showDel = getRowSet().getShowDeleted();
            getRowSet().setShowDeleted(true);

            SyncResolver resolver = spe.getSyncResolver();
            while (resolver.nextConflict()) {
                switch (resolver.getStatus()) {
                    case SyncResolver.UPDATE_ROW_CONFLICT:
                        getRowSet().undoUpdate();
                        break;
                    case SyncResolver.DELETE_ROW_CONFLICT:
                        getRowSet().undoDelete();
                        break;
                    case SyncResolver.INSERT_ROW_CONFLICT:
                        getRowSet().undoInsert();
                        break;
                }
            }
            // XXX: We need to close the resolver to make sure we restore
            // XXX: the cursor position of the CachedRowSet
            resolver.close();

            // reset CachedRowSet
            getRowSet().setShowDeleted(showDel);
            // XXX: we throw the original SQLException
            throw spe.getNextException();
        }
    }

}
