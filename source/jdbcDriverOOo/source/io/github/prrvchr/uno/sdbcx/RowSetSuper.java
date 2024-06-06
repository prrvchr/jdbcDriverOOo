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

import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

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
import io.github.prrvchr.jdbcdriver.resultset.ScrollableResultSet;
import io.github.prrvchr.jdbcdriver.resultset.SensitiveResultSet;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class RowSetSuper<C extends ConnectionSuper,
                                  S extends StatementMain<?, ?>>
    extends ResultSetSuper<C, S>
    implements XRowLocate,
               XDeleteRows,
               XCancellable
{

    // XXX: If we want to be able to use Bookmarks (ie: XRowLocate)
    // XXX: we need to keep reference of deleted rows
    protected List<Integer> m_DeletedRows = new ArrayList<>();
    protected DriverProvider m_Provider;
    protected boolean m_UpdateOnClose = true;
    protected boolean m_IsDeleteVisible;
    protected boolean m_IsInsertVisible;
    protected boolean m_IsUpdateVisible;

    // The constructor method:
    public RowSetSuper(String service,
                       String[] services,
                       DriverProvider provider,
                       C connection,
                       ResultSet result,
                       S statement,
                       String query)
        throws SQLException
    {
        super(service, services, connection, getResultSet(provider, result, query), statement, true, true);
        setResult(provider, result);
    }

    private static ResultSet getResultSet(DriverProvider provider, ResultSet result, String query)
        throws SQLException
    {
        try {
            int rstype = result.getType();
            boolean forwardonly = rstype == ResultSet.TYPE_FORWARD_ONLY;
            boolean sensitive = rstype == ResultSet.TYPE_SCROLL_SENSITIVE;
            //m_IsUpdatable = result.getConcurrency() == ResultSet.CONCUR_UPDATABLE;
            int fetchsize = result.getFetchSize();
            System.out.println("RowSetSuper.getResultSet() IsForwardOnly: " + forwardonly + " - IsSensitive: " + sensitive + " - FetchSize: " + fetchsize);
            if (rstype == ResultSet.TYPE_FORWARD_ONLY || rstype == ResultSet.TYPE_SCROLL_INSENSITIVE) {
                result = new ScrollableResultSet(provider, result, query, true);
                System.out.println("RowSetSuper.getResultSet() ResultSet: ScrollableResultSet");
            }
            else if (rstype == ResultSet.TYPE_SCROLL_INSENSITIVE) {
                result = new SensitiveResultSet(provider, result, query, true);
                System.out.println("RowSetSuper.getResultSet() ResultSet: SensitiveResultSet");
            }
            else {
                result = new CachedResultSet(provider, result, query, true);
                System.out.println("RowSetSuper.getResultSet() ResultSet: CachedResultSet");
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException();
        }
        return result;
    }

    private void setResult(DriverProvider provider, ResultSet result)
        throws SQLException
    {
        try {
            m_Provider = provider;
            int rstype = result.getType();
            m_IsDeleteVisible = provider.isDeleteVisible(rstype);
            m_IsInsertVisible = provider.isInsertVisible(rstype);
            m_IsUpdateVisible = provider.isUpdateVisible(rstype);
            System.out.println("RowSetSuper() Delete are visible: " + m_IsDeleteVisible);
            System.out.println("RowSetSuper() Update are visible: " + m_IsUpdateVisible);
            System.out.println("RowSetSuper() Insert are visible: " + m_IsInsertVisible);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing()
    {
        super.postDisposing();
        try {
            m_Provider.getConnection().commit();
        }
        catch (java.sql.SQLException e) {
            getLogger().logp(LogLevel.WARNING, e);
        }
    }



    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object object1, Object object2)
        throws SQLException
    {
        int compare = CompareBookmark.NOT_COMPARABLE;
        int bookmark1 = 0, bookmark2 = 0;
        try {
            bookmark1 = AnyConverter.toInt(object1);
            bookmark2 = AnyConverter.toInt(object2);
        }
        catch (IllegalArgumentException e) { }
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
        int row = getBookmarkFromRow(getRow());
        Object bookmark = (row != 0 && !m_DeletedRows.contains(row)) ? row : Any.VOID;
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
        boolean moved = false;
        int bookmark = AnyConverter.toInt(object);
        int row = getRowFromBookmark(bookmark);
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
        int row = getRowFromBookmark(bookmark);
        boolean moved = absolute(row);
        if (!moved) {
            afterLast();
        }
        return moved;
    }

    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] objects)
        throws SQLException
    {
        List<Integer> rows = new ArrayList<Integer>();
        for (int i = 0; i < objects.length; i ++) {
            int bookmark = AnyConverter.toInt(objects[i]);
            int row = getRowFromBookmark(bookmark);
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
    public void cancel() {
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
            int bookmark = getBookmarkFromRow(getRow());
            m_Result.deleteRow();
            m_DeletedRows.add(bookmark);
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

    private int getRowFromBookmark(int bookmark)
    {
        return bookmark - getRowOffset(bookmark);
    }

    private int getBookmarkFromRow(int row)
    {
        return row + getBookmarkOffset(row);
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

    private int getBookmarkOffset(int bookmark)
    {
        int offset = 0;
        for (int row : m_DeletedRows) {
            if (row <= bookmark) {
                offset ++;
            }
        }
        return offset;
    }

}
