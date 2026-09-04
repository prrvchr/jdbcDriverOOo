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

import javax.sql.rowset.CachedRowSet;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class RowSetMain
    extends RowSetBase {

    // XXX: We need to know if a row has been inserted and after this insertion
    // XXX: when moveToCurrentRow is called then acceptChange will be triggered
    // XXX: and the insertion will be performed in the database.
    private boolean mRowInserted = false;

    // The constructor method:
    protected RowSetMain(String service,
                         String[] services,
                         ConnectionBase connection,
                         CachedRowSet rowset,
                         StatementBase statement) {
        super(service, services, connection, rowset, statement);
    }

    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public Object getBookmark()
        throws SQLException {
        try {
            // XXX: Base can call getBookmark() while still on
            // XXX: the insert row. See tdf#168145
            if (mOnInsert) {
                internalMoveToCurrentRow();
            }
            // XXX: If an insert was made, we need to validate that insert.
            if (mRowInserted) {
                acceptInsert();
            }
            Object bookmark = Any.VOID;
            boolean showdeleted = getRowSet().getShowDeleted();
            getRowSet().setShowDeleted(true);
            int index = super.getRow();
            getRowSet().setShowDeleted(showdeleted);
            if (index != 0) {
                bookmark = index;
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_GET_BOOKMARK, bookmark.toString());
            return bookmark;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // XXX: If the bookmark could not be located, the result set will be positioned after the last record.
    // XXX: https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html#moveRelativeToBookmark
    @Override
    public boolean moveRelativeToBookmark(Object bookmark, int count)
        throws SQLException {
        try {
            boolean moved = false;
            if (internalMoveToBookmark(bookmark)) {
                moved = super.relative(count);
            }
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK,
                               count, AnyConverter.toInt(bookmark), moved);
            if (!moved) {
                super.afterLast();
            }
            return moved;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    // XXX: If the bookmark could not be located, the result set will be positioned after the last record.
    // XXX: https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html#moveToBookmark
    @Override
    public boolean moveToBookmark(Object bookmark)
        throws SQLException {
        try {
            boolean moved = internalMoveToBookmark(bookmark);
            if (!moved) {
                super.afterLast();
            }
            return moved;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    private boolean internalMoveToBookmark(Object bookmark) throws java.sql.SQLException {
        boolean showdeleted = getRowSet().getShowDeleted();
        getRowSet().setShowDeleted(true);
        boolean moved = mResult.absolute(AnyConverter.toInt(bookmark));
        getRowSet().setShowDeleted(showdeleted);
        return moved;
    }

    private void acceptInsert() throws SQLException {
        acceptChanges();
        mRowInserted = false;
        // XXX: We must position the cursor on the new inserted row (ie: last row)
        super.last();
    }

}
