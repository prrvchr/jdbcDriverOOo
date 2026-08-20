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

import java.util.BitSet;
import javax.sql.rowset.CachedRowSet;

import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class CachedRowSetMain
    extends RowSetBase {

    // XXX: We need to know if a row has been inserted and after this insertion
    // XXX: when moveToCurrentRow is called then acceptChange will be triggered
    // XXX: and the insertion will be performed in the database.
    private boolean mRowInserted = false;

    private final BitSet deletedRows = new BitSet();
    private int numRows = 0;
    private int absolutePos = 0;

    // The constructor method:
    protected CachedRowSetMain(String service,
                         String[] services,
                         ConnectionBase connection,
                         CachedRowSet rowset,
                         StatementBase statement)
        throws java.sql.SQLException {
        super(service, services, connection, rowset, statement);
        rowset.setShowDeleted(true);
        numRows = rowset.size();
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
            int index = cursorPos();
            if (index != 0) {
                bookmark = index;
            }
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
        boolean moved = true;
        int index = AnyConverter.toInt(bookmark);
        if (index != cursorPos()) {
            moved = super.absolute(index);
        }
        if (moved) {
            moved = super.relative(count);
        }
        if (moved) {
            absolutePos = absolutePos(index + count);
        } else {
            super.afterLast();
            internalAfterLast();
        }
        return moved;
    }

    // XXX: If the bookmark could not be located, the result set will be positioned after the last record.
    // XXX: https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html#moveToBookmark
    @Override
    public boolean moveToBookmark(Object bookmark)
        throws SQLException {
        int index = AnyConverter.toInt(bookmark);
        boolean moved = super.absolute(index);
        if (moved) {
            absolutePos = absolutePos(index);
        } else {
            super.afterLast();
            internalAfterLast();
        }
        return moved;
    }

    @Override
    public void deleteRow() throws SQLException {
        super.deleteRow();
        deletedRows.set(cursorPos());
        // XXX: the delete will be committed
        acceptChanges();
    }

    @Override
    protected void internalInsertRow()
        throws java.sql.SQLException {
        super.internalInsertRow();
        numRows++;
    }

    @Override
    public int getRow()
        throws SQLException {
        if (absolutePos <= 0 || absolutePos > size()) {
            return 0;
        }
        return absolutePos;
    }

    @Override
    public void beforeFirst() throws SQLException {
        super.beforeFirst();
        internalBeforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        if (!isEmpty()) {
            super.afterLast();
            internalAfterLast();
        }
    }

    @Override
    public boolean first() throws SQLException {
        if (super.first()) {
            internalFirst();
            return true;
        }
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        if (super.last()) {
            internalLast();
            return true;
        }
        return false;
    }

    @Override
    public boolean next() throws SQLException {
        int index = cursorPos();
        boolean moved = true;
        do {
            if (super.next()) {
                index++;
            } else {
                internalAfterLast();
                moved = false;
                break;
            }
        } while (deletedRows.get(index));

        if (moved) {
            absolutePos++;
        }
        return moved;
    }

    @Override
    public boolean previous() throws SQLException {
        int  index = cursorPos();
        boolean moved = true;
        do {
            if (super.previous()) {
                index--;
            } else {
                internalBeforeFirst();
                moved = false;
                break;
            }
        } while (deletedRows.get(index));

        if (moved) {
            absolutePos--;
        }
        return moved;
    }

    @Override
    public boolean absolute(int rows) throws SQLException {
        if (rows == 0) {
            beforeFirst();
            return false;
        }

        int row = (rows < 0) ? (size() + rows + 1) : rows;
        if (row > size() || row <= 0) {
            if (row > size()) {
                afterLast();
            } else {
                beforeFirst();
            }
            return false;
        }

        int index = cursorPos(row);
        boolean moved = super.absolute(index);
        if (moved) {
            absolutePos = absolutePos(index);
        } else if (super.isBeforeFirst()) {
            internalBeforeFirst();
        } else if (super.isAfterLast()) {
            internalAfterLast();
        }
        return moved;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        if (rows == 0) {
            return true;
        }
        return absolute(absolutePos + rows);
    }

    private void internalBeforeFirst() {
        absolutePos = 0;
    }

    private void internalAfterLast() {
        absolutePos = size() + 1;
    }

    private void internalFirst() {
        absolutePos = 1;
    }

    private void internalLast() {
        absolutePos = size();
    }

    @Override
    protected void internalMoveToCurrentRow()
        throws java.sql.SQLException {
        super.internalMoveToCurrentRow();
        absolutePos = absolutePos(mResult.getRow());
    }

    private void acceptInsert() throws SQLException {
        acceptChanges();
        mRowInserted = false;
        // XXX: We must position the cursor on the new inserted row (ie: last row)
        if (super.last()) {
            absolutePos = size();
        }
    }

    private int cursorPos() {
        return cursorPos(absolutePos);
    }

    private int cursorPos(int row) {
        if (row == 0 || deletedRows.isEmpty()) {
            return row;
        }

        int i = deletedRows.nextSetBit(1);
        while (i >= 0 && i <= row) {
            row++; 
            i = deletedRows.nextSetBit(i + 1); 
        }
        return row;
    }

    private int absolutePos(int index) {
        if (index <= 0 || index > numRows) {
            if (index > numRows && !isEmpty()) {
                index = size() + 1;
            }
            return index;
        }

        if (deletedRows.get(index)) {
            return 0; 
        }

        // Count the number of bits set to true (deleted) that are less than or equal to index.
        int count = 0;
        int i = deletedRows.nextSetBit(1);
        while (i >= 0 && i <= index) {
            count++;
            i = deletedRows.nextSetBit(i + 1);
        }
        return index - count;
    }

    private int size() {
        return numRows - deletedRows.cardinality();
    }

    private boolean isEmpty() {
        return size() == 0;
    }
}
