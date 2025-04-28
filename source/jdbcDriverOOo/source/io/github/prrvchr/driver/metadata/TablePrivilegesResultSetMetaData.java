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
package io.github.prrvchr.driver.metadata;

import java.sql.SQLException;
import java.util.List;


public class TablePrivilegesResultSetMetaData extends ResultSetMetaData {

    private java.sql.ResultSet mResult;
    private List<String> mColumns;

    public TablePrivilegesResultSetMetaData(java.sql.ResultSetMetaData metadata,
                                            List<String> columns) {
        this(metadata, null, columns);
    }

    public TablePrivilegesResultSetMetaData(java.sql.ResultSetMetaData metadata,
                                            java.sql.ResultSet result,
                                            List<String> columns) {
        super(metadata);
        mResult = result;
        mColumns = columns;
    }

    @Override
    public int getColumnCount()
        throws SQLException {
        return mColumns.size();
    }

    @Override
    public String getColumnLabel(int index)
        throws SQLException {
        return mColumns.get(index - 1);
    }

    @Override
    public String getColumnName(int index)
        throws SQLException  {
        return getColumnLabel(index);
    }

    @Override
    int getIndex(int index)
        throws SQLException {
        // XXX: The index needs to be rewritten if we have a ResultSet
        if (mResult != null) {
            index = mResult.findColumn(getColumnLabel(index));
        }
        return index;
    }

}
