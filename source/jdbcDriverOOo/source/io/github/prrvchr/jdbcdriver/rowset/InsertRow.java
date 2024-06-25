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

import java.util.BitSet;


public class InsertRow
    extends BaseRow
{

    protected BitSet m_Inserted;


    public InsertRow(int count)
    {
        super(count);
        m_Inserted = new BitSet(count);
    }

    public InsertRow(InsertRow row)
    {
        super(row);
        m_Inserted = new BitSet(m_Count);
        for (int i = 1; i <= m_Count; i++) {
            if (row.isColumnSet(i)) {
                m_Inserted.set(i - 1);
            }
        }
    }

    public InsertRow clown()
    {
        return new InsertRow(this);
    }

    @Override
    public Object getColumnObject(int index)
    {
        Object value = null;
        if (isColumnSet(index)) {
            value = m_OldValues[index - 1];
        }
        return value;
    }

    @Override
    public boolean isColumnNull(int index)
    {
        boolean isnull = true;
        if (isColumnSet(index)) {
            isnull = m_OldValues[index - 1] == null;
        }
        return isnull;
    }

    @Override
    public void setColumnObject(int index, Object value)
    {
        int i = index - 1;
        m_OldValues[i] = value;
        m_Inserted.set(i);
    }

    @Override
    public void setColumnDouble(int index, Double value, int type)
    {
        int i = index - 1;
        m_OldValues[i] = RowHelper.getDoubleValue(value, type);
        m_Inserted.set(i);
    }

    @Override
    public boolean isColumnSet(int index)
    {
        return m_Inserted.get(index - 1);
    }

}
