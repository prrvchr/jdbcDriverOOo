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


public class Row
    extends BaseRow
{

    protected Object[] m_NewValues;
    protected BitSet m_Updated;
    protected boolean m_Updatable = true;


    public Row(int count)
    {
        super(count);
        m_NewValues = new Object[count];
        m_Updated = new BitSet(count);
    }

    public Row(BaseRow row)
    {
        super(row);
        m_NewValues = new Object[m_Count];
        m_Updated = new BitSet(m_Count);
    }

    public void clearUpdated(List<RowColumn> columns, int status)
    {
        for (RowColumn column : columns) {
            int i = column.getIndex() - 1;
            if (status != 0) {
                m_OldValues[i] = m_NewValues[i];
            }
            m_NewValues[i] = null;
            m_Updated.clear(i);
        }
    }

    public Object getOldColumnObject(int index)
    {
        return m_OldValues[index - 1];
    }

    public boolean isUpdated()
    {
        // XXX: In order to be able to throw an exception again if necessary
        // XXX: we need to reset the m_Updatable flag if it's set...
        if (!m_Updatable) {
            m_Updatable = true;
        }
        for (int i = 1; i <= m_Count; i++) {
            if (isColumnSet(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUpdatable()
    {
        return m_Updatable;
    }

    public void clearUpdatable()
    {
        m_Updatable = false;
    }

    @Override
    public boolean isColumnNull(int index)
    {
        boolean isnull = true;
        if (isColumnSet(index)) {
            isnull = m_NewValues[index - 1] == null;
        }
        else {
            isnull = m_OldValues[index - 1] == null;
        }
        return isnull;
    }

    @Override
    public Object getColumnObject(int index)
    {
        Object value = null;
        if (isColumnSet(index)) {
            value = m_NewValues[index - 1];
        }
        else {
            value = m_OldValues[index - 1];
        }
        return value;
    }

    @Override
    public void setColumnObject(int index, Object value)
    {
        if (m_Updatable) {
            int i = index - 1;
            m_NewValues[i] = value;
            m_Updated.set(i);
        }
    }

    @Override
    public void setColumnDouble(int index, Double value, int type)
    {
        if (m_Updatable) {
            int i = index - 1;
            m_NewValues[i] = RowHelper.getDoubleValue(value, type);
            m_Updated.set(i);
        }
    }

    @Override
    public boolean isColumnSet(int index)
    {
        return m_Updated.get(index - 1);
    }


}
