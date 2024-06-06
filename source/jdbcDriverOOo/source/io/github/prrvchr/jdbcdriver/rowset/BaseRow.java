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
import java.util.List;


public abstract class BaseRow
{

    protected Object[] m_NewValues;
    protected Object[] m_OldValues;
    protected BitSet m_Updated;

    protected int m_Count;

    protected BaseRow(int count)
    {
        m_NewValues = new Object[count];
        m_OldValues = new Object[count];
        m_Updated = new BitSet(count);
        m_Count = count;
    }

    public boolean isColumnUpdated(int index)
    {
        return m_Updated.get(index - 1);
    }

    public void setColumnObject(int index, Object value)
    {
        m_NewValues[index - 1] = value;
        m_Updated.set(index - 1);
    }

    public void clearUpdated(List<RowColumn> columns, int status)
    {
        for (RowColumn column : columns) {
            int index = column.getIndex();
            if (status != 0) {
                m_OldValues[index - 1] = m_NewValues[index - 1];
            }
            m_NewValues[index - 1] = null;
            m_Updated.clear(index - 1);
        }
    }

    public Object getOldColumnObject(int index)
    {
        return(m_OldValues[index - 1]);
    }

    public abstract Object getColumnObject(int index);

}
