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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBQueryParser;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;

public class RowTable
{

    private RowCatalog m_Tables;
    private Map<Integer, String> m_Keys = new HashMap<>();
    private String m_Catalog = "";
    private String m_Schema = "";
    private String m_Name = "";
    private String m_Where;


    // The constructor method:
    public RowTable(DriverProvider provider,
                    RowCatalog tables,
                    String query)
        throws SQLException
    {
        m_Tables = tables;
        DBQueryParser parser = new DBQueryParser(DBQueryParser.SQL_SELECT, query);
        if (parser.hasTable()) {
            ComposeRule rule = ComposeRule.InDataManipulation;
            NamedComponents component = DBTools.qualifiedNameComponents(provider, parser.getTable(), rule, true);
            m_Catalog = component.getCatalogName();
            m_Schema = component.getSchemaName();
            m_Name = component.getTableName();
        }
        System.out.println("RowTable() 1");
    }

    public RowTable(RowCatalog tables,
                    NamedComponents component)
        throws SQLException
    {
        m_Tables = tables;
        m_Catalog = component.getCatalogName();
        m_Schema = component.getSchemaName();
        m_Name = component.getTableName();
        System.out.println("RowTable() 1");
    }

    public RowTable(RowCatalog tables,
                    ResultSetMetaData metadata,
                    int index)
        throws SQLException
    {
        m_Tables = tables;
        m_Catalog = metadata.getCatalogName(index);
        m_Schema = metadata.getSchemaName(index);
        m_Name = metadata.getTableName(index);
        System.out.println("RowTable() 1");
    }

    public boolean isValid()
    {
        return !m_Name.isBlank();
    }

    public String getCatalogName()
    {
        return m_Catalog;
    }

    public String getSchemaName()
    {
        return m_Schema;
    }

    public String getName()
    {
        return m_Name;
    }

    public String getComposedName(DriverProvider provider, boolean quoted)
        throws SQLException
    {
        return DBTools.buildName(provider, m_Catalog, m_Schema, m_Name, m_Tables.getRule(), quoted);
    }

    public boolean isSameTable(ResultSetMetaData metadata,
                               int index)
        throws SQLException
    {
        return m_Catalog.equals(metadata.getCatalogName(index)) &&
               m_Schema.equals(metadata.getSchemaName(index)) &&
               m_Name.equals(metadata.getTableName(index));
    }

    public boolean isSameTable(NamedComponents component)
        throws SQLException
    {
        return component != null &&
               m_Catalog.equals(component.getCatalogName()) &&
               m_Schema.equals(component.getSchemaName()) &&
               m_Name.equals(component.getTableName());
    }

    public void setKeyColumn(int index, String identifier, int type) {
        if (RowHelper.isValidKeyType(type)) {
            m_Keys.put(index, identifier);
        }
    }

    public boolean isKeyColumn(int index) {
        return m_Keys.containsKey(index);
    }

    public Integer[] getKeyIndex()
    {
        return m_Keys.keySet().toArray(new Integer[0]);
    }

    public String getWhereCmd()
        throws SQLException
    {
        if (m_Where == null) {
            List<String> columns = new ArrayList<>();
            for (Entry<Integer, String> key : m_Keys.entrySet()) {
                columns.add(String.format(m_Tables.getParameter(), key.getValue()));
            }
            m_Where = String.join(m_Tables.getAnd(), columns);
        }
        return m_Where;
    }

    public String getSeparator()
    {
        return m_Tables.getSeparator();
    }

    public String getParameter()
    {
        return m_Tables.getParameter();
    }

    public String getMark()
    {
        return m_Tables.getMark();
    }

    public boolean equals(Object object)
    {
        if (!(object instanceof RowTable)) {
            return false;
        }
        RowTable table = (RowTable) object;
        return getCatalogName().equals(table.getCatalogName()) &&
               getSchemaName().equals(table.getSchemaName()) &&
               getName().equals(table.getName());
    }

}
