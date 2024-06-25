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


public class RowColumn
{

    private RowTable m_Table;
    private String m_Name;
    private String m_Identifier;
    private int m_Type = 0;
    private int m_Index = 0;
    private boolean m_AutoIncrement = false;
    private String AUTOINCREMENT = "YES";

    // The constructor method:
    public RowColumn(RowTable table,
                     ResultSet result,
                     boolean ordinal)
        throws java.sql.SQLException
    {
        m_Table = table;
        m_Name = result.getString(4);
        m_Identifier = table.getCatalog().enquoteIdentifier(m_Name);
        m_Type = result.getInt(5);
        m_Index = ordinal ? result.getInt(17) : 0;
        m_AutoIncrement = AUTOINCREMENT.equalsIgnoreCase(result.getString(23));
    }

    public RowTable getTable()
    {
        return m_Table;
    }

    public String getCatalogName()
    {
        return m_Table.getCatalogName();
    }

    public String getSchemaName()
    {
        return m_Table.getSchemaName();
    }

    public String getTableName()
    {
        return m_Table.getName();
    }

    public String getName()
    {
        return m_Name;
    }

    public String getPredicate()
    {
        return String.format(m_Table.getParameter(), m_Identifier);
    }

    public String getIdentifier()
    {
        return m_Identifier;
    }

    public int getIndex()
    {
        return m_Index;
    }

    public void setIndex(int index)
    {
        m_Index = index;
    }

    public int getType()
    {
        return m_Type;
    }

    public boolean isAutoIncrement()
    {
        return m_AutoIncrement;
    }

}
