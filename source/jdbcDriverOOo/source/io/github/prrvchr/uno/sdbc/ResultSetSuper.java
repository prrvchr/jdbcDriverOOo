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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XDeleteRows;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ResultSetSuper
extends ResultSetBase
implements XRowLocate,
           XDeleteRows
{
    private int m_row = 0;
    protected boolean m_IsBookmarkable = false;
    private static Map<String, Property> _getPropertySet()
    {
        short readonly = PropertyAttribute.READONLY;
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_IsBookmarkable", UnoHelper.getProperty("IsBookmarkable", "boolean", readonly));
        return map;
    }

    // The constructor method:
    public ResultSetSuper(XComponentContext ctx,
                         String name,
                         String[] services,
                         ConnectionBase connection,
                         XInterface statement,
                         java.sql.ResultSet resultset)
    throws java.sql.SQLException
    {
        super(ctx, name, services, connection, statement, resultset, _getPropertySet());
        m_row = m_ResultSet.getMetaData().getColumnCount();
        if (m_row > 0)
        {
            String index = m_ResultSet.getMetaData().getColumnTypeName(1);
            String column = m_ResultSet.getMetaData().getColumnName(m_row);
            String bookmark = m_ResultSet.getMetaData().getColumnTypeName(m_row);
            m_IsBookmarkable = index.equals("VARCHAR") && column.equals("Bookmark") && bookmark.equals("INTEGER");
        }

    }


    // com.sun.star.sdbcx.XRowLocate:
    @Override
    public int compareBookmarks(Object bookmark1, Object bookmark2)
    throws SQLException
    {
        System.out.println("ResultSet.compareBookmarks() 1");
        int value = CompareBookmark.NOT_COMPARABLE;
        Integer row1 = (Integer) bookmark1;
        Integer row2 = (Integer) bookmark2;
        System.out.println("ResultSet.compareBookmarks() 2 id1: " + row1.toString() + " id2: " + row2.toString());
        if (row1 == row2) value = CompareBookmark.EQUAL;
        else if (row1 < row2) value = CompareBookmark.LESS;
        else if (row1 > row2) value = CompareBookmark.GREATER;
        else if (row1 != row2) value = CompareBookmark.NOT_EQUAL;
        System.out.println("ResultSet.compareBookmarks() 3 value: " + value);
        return value;
    }


    @Override
    public Object getBookmark()
    throws SQLException
    {
        System.out.println("ResultSet.getBookmark() 1");
        Integer bookmark = null;
        try {
            String name = m_ResultSet.getMetaData().getColumnTypeName(m_row);
            System.out.println("ResultSet.getBookmark() 2 ColumnTypeName: " + name);
            bookmark = m_ResultSet.getInt(m_row);
        } catch (java.sql.SQLException e) {
            System.out.println("ResultSet.getBookmark() 3");
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("ResultSet.getBookmark() 3 " + bookmark.toString());
        return bookmark;
    }


    @Override
    public boolean hasOrderedBookmarks()
    throws SQLException
    {
        System.out.println("ResultSet.hasOrderedBookmarks()");
        return true;
    }


    @Override
    public int hashBookmark(Object object)
    throws SQLException
    {
        System.out.println("ResultSet.hashBookmark()");
        return object.hashCode();
    }


    @Override
    public boolean moveRelativeToBookmark(Object object, int count)
    throws SQLException
    {
        System.out.println("ResultSet.moveRelativeToBookmark()");
        int row = ((Integer) object) + count;
        return _moveToRow(row);
    }


    @Override
    public boolean moveToBookmark(Object object)
    throws SQLException
    {
        System.out.println("ResultSet.moveToBookmark()");
        int row = ((Integer) object);
        return _moveToRow(row);
    }

    public boolean _moveToRow(int row)
    throws SQLException
    {
        System.out.println("ResultSet._moveToRow() 1");
        boolean state = false; 
        try {
            state = m_ResultSet.absolute(row);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("ResultSet._moveToRow() 2 State: " + state + " - Row: " + row);
        return state;
    }
    
    // com.sun.star.sdbcx.XDeleteRows:
    @Override
    public int[] deleteRows(Object[] bookmarks)
    throws SQLException
    {
        List<Integer> rows = new ArrayList<Integer>();
        for (Object bookmark: bookmarks)
        {
            moveToBookmark(bookmark);
            deleteRow();
            rows.add(1);
        }
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }

}
