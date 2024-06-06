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
        try {
            System.out.println("InsertRow.isCompleteRow() 1");
            int nonull = ResultSetMetaData.columnNoNulls;
            System.out.println("InsertRow.isCompleteRow() 2");
            for (int i = 0; i < m_Count; i++) {
                System.out.println("InsertRow.isCompleteRow() 3");
                if (!m_Updated.get(i) && metadata.isNullable(i + 1) == nonull) {
                    System.out.println("InsertRow.isCompleteRow() 4");
                    return false;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public BitSet getInsertedColumns()
    {
        return (BitSet) m_Updated.clone();
    }

    public Object getColumnObject(int index)
    {
        if (m_Updated.get(index - 1)) {
            return m_NewValues[index - 1];
        }
        return null;
    }

}
