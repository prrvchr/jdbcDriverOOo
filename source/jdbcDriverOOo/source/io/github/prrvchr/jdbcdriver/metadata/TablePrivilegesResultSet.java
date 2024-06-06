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
package io.github.prrvchr.jdbcdriver.metadata;

import java.sql.SQLException;


public class TablePrivilegesResultSet
    extends TablePrivilegesResultSetBase
{

    //private String[] m_privileges = {"SELECT", "INSERT", "UPDATE", "DELETE",
    //                                 "READ", "CREATE", "ALTER", "REFERENCE", "DROP"};
    private final String[] m_privileges;
    private final TablePrivilegesRows m_rows;
    private int m_index = -1;

    public TablePrivilegesResultSet(java.sql.ResultSet result,
                                    String [] privileges,
                                    String username)
        throws SQLException
    {
        super(result, result.getMetaData());
        m_privileges = privileges;
        m_rows = new TablePrivilegesRows(username);
    }

    @Override
    public boolean next()
        throws SQLException
    {
        m_index ++;
        boolean next = true;
        int index = m_index % m_privileges.length;
        if (index == 0) {
            next = m_result.next();
        }
        if (next) {
            m_rows.setPrivilege(m_privileges[index]);
        }
        return next;
    }

    @Override
    public int getRow()
        throws SQLException
    {
        if (m_index >= 0) {
            return m_index + 1;
        }
        return 0;
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_rows.wasNull(m_result.wasNull());
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getString(index));
    }

    @Override
    public String getString(String label)
        throws SQLException
    {
        return getString(findColumn(label));
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getBoolean(index));
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException
    {
        return getBoolean(findColumn(label));
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getShort(index));
    }

    @Override
    public short getShort(String label)
        throws SQLException
    {
        return getShort(findColumn(label));
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getInt(index));
    }

    @Override
    public int getInt(String label)
        throws SQLException
    {
        return getInt(findColumn(label));
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getLong(index));
    }

    @Override
    public long getLong(String label)
        throws SQLException
    {
        return getLong(findColumn(label));
    }

}
