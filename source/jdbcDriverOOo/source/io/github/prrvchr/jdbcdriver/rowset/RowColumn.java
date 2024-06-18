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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;


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
                     int index)
        throws java.sql.SQLException
    {
        this(table, result);
        m_Index = index;
    }

    public RowColumn(RowTable table,
                     ResultSet result)
        throws java.sql.SQLException
    {
        m_Table = table;
        m_Name = result.getString(4);
        m_Identifier = table.getCatalog().enquoteIdentifier(m_Name);
        m_Type = result.getInt(5);
        m_Index = result.getInt(17);
        m_AutoIncrement = AUTOINCREMENT.equalsIgnoreCase(result.getString(23));
    }

    public RowColumn(RowTable table,
                     ResultSetMetaData metadata,
                     int index)
        throws java.sql.SQLException
    {
        m_Table = table;
        m_Name = metadata.getColumnName(index);
        m_Identifier = table.getCatalog().enquoteIdentifier(m_Name);
        m_Type = metadata.getColumnType(index);
        m_AutoIncrement = metadata.isAutoIncrement(index);
        m_Index = index;
    }

    public RowColumn(Connection connection,
                     RowTable table,
                     ResultSetMetaData metadata,
                     int index)
        throws java.sql.SQLException
    {
        m_Table = table;
        m_Name = metadata.getColumnName(index);
        m_Identifier = table.getCatalog().enquoteIdentifier(m_Name);
        try (ResultSet result = connection.getMetaData().getColumns(table.getCatalogName(), table.getSchemaName(), table.getName(), m_Name)) {
            if (result.next()) {
                m_Type = result.getInt(5);
                m_AutoIncrement = AUTOINCREMENT.equalsIgnoreCase(result.getString(23));
            }
        }
        m_Index = index;
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

    public String getUpdateParameter()
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

    public int getType()
    {
        return m_Type;
    }

    public boolean isAutoIncrement()
    {
        return m_AutoIncrement;
    }

}
