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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;


public class Row
{

    protected Object[] m_NewValues;
    protected Object[] m_OldValues;
    protected BitSet m_Updated;
    protected int m_Count;
    protected boolean m_Updatable = true;


    public Row(int count)
    {
        m_NewValues = new Object[count];
        m_OldValues = new Object[count];
        m_Updated = new BitSet(count);
        m_Count = count;
    }

    public Row(int count, Object[] values)
    {
        this(count);
        System.arraycopy(values, 0, m_NewValues, 0, count);
    }

    public boolean isColumnUpdated(int index)
    {
        return m_Updated.get(index - 1);
    }

    public void initColumnObject(ResultSet result,
                                 int index)
        throws SQLException
    {
        Object value = RowHelper.getResultSetValue(result, index);
        m_OldValues[index - 1] = value;
    }

    public void setColumnDouble(int index, Double value, int type)
        throws SQLException
    {
        if (m_Updatable) {
            int i = index - 1;
            m_NewValues[i] = RowHelper.getDoubleValue(value, type);
            m_Updated.set(i);
        }
    }

    public void setColumnObject(int index, Object value)
    {
        if (m_Updatable) {
            int i = index - 1;
            m_NewValues[i] = value;
            m_Updated.set(i);
        }
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
        return(m_OldValues[index - 1]);
    }

    public boolean isColumnNull(int index)
    {
        boolean isnull = true;
        if (isColumnUpdated(index)) {
            isnull = m_NewValues[index - 1] == null;
        }
        else {
            isnull = m_OldValues[index - 1] == null;
        }
        return isnull;
    }

    public Object getColumnObject(int index)
    {
        Object value = null;
        if (isColumnUpdated(index)) {
            value = m_NewValues[index - 1];
        }
        else {
            value = m_OldValues[index - 1];
        }
        return value;
    }

    public boolean isUpdated()
    {
        // XXX: In order to be able to throw an exception again if necessary
        // XXX: we need to reset the m_Updatable flag if it's set...
        if (!m_Updatable) {
            m_Updatable = true;
        }
        for (int i = 1; i <= m_Count; i++) {
            if (isColumnUpdated(i)) {
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

}
