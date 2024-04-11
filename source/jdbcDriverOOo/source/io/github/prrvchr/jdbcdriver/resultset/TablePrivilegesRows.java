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
package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TablePrivilegesRows
{

    // XXX: We rewrite column indexes 4,5,6 and 7, index 5 and 6 will be assigned dynamically
    private Map<Integer, Object> m_columns = new HashMap<>();
    private boolean m_replaced = false;
    private boolean m_wasnull = false;

    public TablePrivilegesRows(final String username)
        throws SQLException
    {
        m_columns.put(4, null);
        m_columns.put(5, username);
        m_columns.put(7, "YES");
    }

    public void setPrivilege(final String privilege)
    {
        m_columns.put(6, privilege);
    }

    public boolean wasNull(final boolean wasnull)
    {
        return m_replaced ? m_wasnull : wasnull;
    }

    public String getValue(final int index,
                           final String value)
    {
        if (setValue(index)) {
            return (String) getValue(index);
        }
        return value;
    }

    public boolean getValue(final int index,
                            final boolean value)
    {
        if (setValue(index)) {
            return (boolean) getValue(index);
        }
        return value;
    }

    public short getValue(final int index,
                          final short value)
    {
        if (setValue(index)) {
            return (short) getValue(index);
        }
        return value;
    }

    public int getValue(final int index,
                        final int value)
    {
        if (setValue(index)) {
            return (int) getValue(index);
        }
        return value;
    }

    public long getValue(final int index,
                         final long value)
    {
        if (setValue(index)) {
            return (long) getValue(index);
        }
        return value;
    }

    private boolean setValue(int index)
    {
        m_replaced = m_columns.containsKey(index);
        return m_replaced;
    }

    private Object getValue(final int index)
    {
        Object value = m_columns.get(index);
        m_wasnull = value == null;
        return value;
    }

}
