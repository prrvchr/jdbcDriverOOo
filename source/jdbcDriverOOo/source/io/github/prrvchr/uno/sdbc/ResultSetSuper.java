/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public abstract class ResultSetSuper
    extends ResultSetBase
    implements XRowLocate,
               XDeleteRows,
               XCancellable
{
    private final boolean m_IsBookmarkable;
    @SuppressWarnings("unused")
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
        m_IsBookmarkable = bookmark;
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.ISBOOKMARKABLE.name, PropertyIds.ISBOOKMARKABLE.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_IsBookmarkable;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object bookmark1, Object bookmark2)
    throws SQLException
    {
        int row1, row2;
        try {
            row1 = AnyConverter.toInt(bookmark1);
            row2 = AnyConverter.toInt(bookmark2);
        }
        catch (IllegalArgumentException e) {
            return CompareBookmark.NOT_COMPARABLE;
        }
        if (row1 < row2) {
            return CompareBookmark.LESS;
        }
        else if (row1 > row2) {
            return CompareBookmark.GREATER;
        }
        else {
            return CompareBookmark.EQUAL;
        }
    }

    @Override
    public Object getBookmark()
    throws SQLException
    {
        return AnyConverter.toObject(new Type(Integer.class), getRow());
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
        return AnyConverter.toInt(bookmark);
    }

    @Override
    public boolean moveRelativeToBookmark(Object bookmark, int count)
    throws SQLException
    {
        return absolute(AnyConverter.toInt(bookmark) + count);
    }

    @Override
    public boolean moveToBookmark(Object bookmark)
    throws SQLException
    {
        return absolute(AnyConverter.toInt(bookmark));
    }


    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
    throws SQLException
    {
        List<Integer> rows = new ArrayList<Integer>();
        for (Object bookmark: bookmarks) {
            moveToBookmark(bookmark);
            deleteRow();
            rows.add(1);
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
