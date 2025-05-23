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
package io.github.prrvchr.driver.rowset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseRow {

    protected Object[] mOldValues;
    protected int mCount;
    protected List<String> mQueries = new ArrayList<>();


    public BaseRow(int count) {
        mOldValues = new Object[count];
        mCount = count;
    }

    public BaseRow(BaseRow row) {
        this(row.getCount());
        System.arraycopy(row.getOldValues(), 0, mOldValues, 0, mCount);
    }

    public Object[] getOldValues() {
        return mOldValues;
    }

    public int getCount() {
        return mCount;
    }

    public void initColumnObject(ResultSet result,
                                 int index)
        throws SQLException {
        Object value = RowHelper.getResultSetValue(result, index);
        initColumnObject(index, value);
    }

    public void initColumnObject(int index,
                                 Object value)
        throws SQLException {
        mOldValues[index - 1] = value;
    }

    public String[] getQueries() {
        String[] queries = mQueries.toArray(new String[0]);
        mQueries = new ArrayList<>();
        return queries;
    }

    public String getQuery(String sql, Object... arguments) {
        String query = String.format(sql, arguments);
        mQueries.add(query);
        return query;
    }

    public abstract boolean isColumnNull(int index);

    public abstract boolean isColumnSet(int index);

    public abstract Object getColumnObject(int index);

    public abstract void setColumnObject(int index, Object value);

    public abstract void setColumnDouble(int index, Double value, int type) throws SQLException;

}
