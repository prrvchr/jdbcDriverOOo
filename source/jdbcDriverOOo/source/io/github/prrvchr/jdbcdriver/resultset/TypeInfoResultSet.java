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


public class TypeInfoResultSet
    extends ResultSet
{

    private TypeInfoRows m_rows;

    public TypeInfoResultSet(java.sql.ResultSet resultset,
                             TypeInfoRows rows)
        throws SQLException
    {
        super(resultset);
        m_rows = rows;
    }

    @Override
    public boolean next()
        throws SQLException
    {
        return m_rows.next(super.next());
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_rows.wasNull(super.wasNull());
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getString(index));
    }

    @Override
    public String getString(String label)
        throws SQLException
    {
        return getString(super.findColumn(label));
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getBoolean(index));
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException
    {
        return getBoolean(super.findColumn(label));
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        return m_rows.getNewValue(index, super.getShort(index));
    }

    @Override
    public short getShort(String label)
        throws SQLException
    {
        return getShort(super.findColumn(label));
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getInt(index));
    }

    @Override
    public int getInt(String label)
        throws SQLException
    {
        return getInt(super.findColumn(label));
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getLong(index));
    }

    @Override
    public long getLong(String label)
        throws SQLException
    {
        return getLong(super.findColumn(label));
    }

}
