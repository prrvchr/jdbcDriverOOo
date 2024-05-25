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
package io.github.prrvchr.jdbcdriver.rowset;


import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.BitSet;


public class InsertRow
    extends BaseRow
{

    public InsertRow(int count)
    {
        super(count);
    }

    public boolean isCompleteRow(ResultSetMetaData metadata)
        throws SQLException
    {
        int nonull = ResultSetMetaData.columnNoNulls;
        for (int i = 0; i < m_Count; i++) {
            if (!m_Updated.get(i) && metadata.isNullable(i + 1) == nonull) {
                return false;
            }
        }
        return true;
    }

    public BitSet getInserted()
        throws SQLException
    {
        return (BitSet) m_Updated.clone();
    }

    public Object getColumnObject(int index)
        throws SQLException
    {
        if (!m_Updated.get(index - 1)) {
            throw new SQLException();
        }
        return m_Values[index - 1];
    }

}
