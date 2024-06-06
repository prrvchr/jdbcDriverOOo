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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;

public class RowCatalog
    implements Iterable<RowTable>
{

    private DriverProvider m_Provider;
    private List<RowTable> m_Tables = new ArrayList<>();
    private ComposeRule rule = ComposeRule.InDataManipulation;
    private String m_Mark = "?";
    private String m_Parameter = "%s = ?";
    private String m_And = " AND ";
    private String m_Separator = ", ";

    // The constructor method:
    public RowCatalog(DriverProvider provider)
        throws SQLException
    {
        m_Provider = provider;
        System.out.println("RowCatalog() 1");
    }

    public RowTable getTable(ResultSetMetaData metadata, int index)
        throws SQLException
    {
        for (RowTable table : m_Tables) {
            if (table.isSameTable(metadata, index)) {
                return table;
            }
        }
        RowTable table = new RowTable(this, metadata, index);
        m_Tables.add(table);
        return table;
    }

    public DriverProvider getProvider()
    {
        return m_Provider;
    }

    public ComposeRule getRule()
    {
        return rule;
    }

    public String getMark()
    {
        return m_Mark;
    }

    public String getParameter()
    {
        return m_Parameter;
    }

    public String getAnd()
    {
        return m_And;
    }

    public String getSeparator()
    {
        return m_Separator;
    }

    @Override
    public Iterator<RowTable> iterator() {
        return m_Tables.iterator();
    }

}
