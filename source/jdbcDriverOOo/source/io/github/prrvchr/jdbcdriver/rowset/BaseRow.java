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


import java.sql.SQLException;

import java.util.Arrays;
import java.util.BitSet;


public abstract class BaseRow
{

    protected Object[] m_Values;
    protected BitSet m_Updated;
    protected int m_Count;

    protected BaseRow(int count)
    {
        m_Values = new Object[count];
        m_Updated = new BitSet(count);
        m_Count = count;
    }

    public Object[] getValues()
    {
        return (m_Values != null) ? Arrays.copyOf(m_Values, m_Values.length) : null;
    }

    public boolean isColumnUpdated(int index)
    {
        return m_Updated.get(index - 1);
    }

    public void setColumnObject(int index, Object value)
    {
        m_Values[index - 1] = value;
        m_Updated.set(index - 1);
    }

    public abstract Object getColumnObject(int index) throws SQLException;

}
