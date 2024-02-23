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
package io.github.prrvchr.uno.sdbc;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class ResultSetSuper
    extends ResultSetBase
    implements XRowLocate,
               XDeleteRows,
               XCancellable
{
    private boolean m_IsBookmarkable;
    private final boolean m_CanUpdateInsertedRows = false;
    
    // The constructor method:
    public ResultSetSuper(String service,
                          String[] services,
                          ConnectionBase connection,
                          java.sql.ResultSet resultset,
                          StatementMain statement,
                          boolean bookmark)
    throws SQLException
    {
        super(service, services, connection, resultset, statement);
        m_IsBookmarkable = bookmark && connection.m_usebookmark;
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.ISBOOKMARKABLE.name, PropertyIds.ISBOOKMARKABLE.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ISBOOKMARKABLE, Boolean.toString(m_IsBookmarkable));
                    return m_IsBookmarkable;
                }
            }, null);
        registerProperty(PropertyIds.CANUPDATEINSERTEDROWS.name, PropertyIds.CANUPDATEINSERTEDROWS.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANUPDATEINSERTEDROWS, Boolean.toString(m_CanUpdateInsertedRows));
                    return m_CanUpdateInsertedRows;
                }
            }, null);
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
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_COMPARE_BOOKMARKS, Integer.toString(bookmark1), Integer.toString(bookmark2), Integer.toString(compare));
        return compare;
    }

    @Override
    public Object getBookmark()
    throws SQLException
    {
        int row = getRow();
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_BOOKMARK, Integer.toString(row));
        return row;
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
        try {
            if (moveToBookmark(object)) {
                moved = relative(count);
            }
            int bookmark = AnyConverter.toInt(object);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK, Integer.toString(count), Integer.toString(bookmark), Boolean.toString(moved));
        }
        catch (IllegalArgumentException e) { }
        return moved;
    }

    @Override
    public boolean moveToBookmark(Object object)
    throws SQLException
    {
        boolean moved = false;
        try {
            int bookmark = AnyConverter.toInt(object);
            if (m_insert) {
                m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_BOOKMARK_ON_INSERT, Integer.toString(bookmark));
                moveToCurrentRow();
            }
            moved = absolute(bookmark);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_TO_BOOKMARK, Integer.toString(bookmark), Boolean.toString(moved));
        }
        catch (IllegalArgumentException e) { }
        return moved;
    }


    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
    throws SQLException
    {
        List<Integer> rows = new ArrayList<Integer>();
        for (Object bookmark: bookmarks) {
            if (moveToBookmark(bookmark)) {
                deleteRow();
                rows.add(1);
            }
            else {
                rows.add(0);
            }
        }
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        try {
            m_ResultSet.cancelRowUpdates();
        }
        catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
