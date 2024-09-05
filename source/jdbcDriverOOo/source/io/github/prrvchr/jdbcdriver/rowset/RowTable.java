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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;

public class RowTable
{

    private RowCatalog m_Catalog;
    private Map<String, RowColumn> m_Columns = new LinkedHashMap<>();
    private List<String> m_Keys = new ArrayList<>();
    private boolean m_IsPrimary = false;
    private NamedComponents m_Component;
    private String m_Where;


    // The constructor method:
    public RowTable (Connection connection,
                     RowCatalog catalog,
                     ResultSetMetaData metadata,
                     int index)
        throws SQLException
    {
        this(connection, catalog, DBTools.getNamedComponents(metadata, index), false);
    }

    public RowTable (Connection connection,
                     RowCatalog catalog,
                     NamedComponents component)
        throws SQLException
    {
        this(connection, catalog, component, false);
    }

    public RowTable (Connection connection,
                     RowCatalog catalog,
                     NamedComponents component,
                     boolean ordinal)
        throws SQLException
    {
        m_Catalog = catalog;
        m_Component = component;
        try (ResultSet result = connection.getMetaData().getColumns(component.getCatalog(), component.getSchema(), component.getTable(), "%")) {
            while (result.next()) {
                addColumn(new RowColumn(this, result, ordinal));
            }
        }
    }

    public RowCatalog getCatalog()
    {
        return m_Catalog;
    }

    public boolean isValid()
    {
        return m_Component.getTable() != null && !m_Component.getTable().isBlank();
    }

    public void addColumn(RowColumn column)
    {
        m_Columns.put(column.getName(), column);
    }

    public void setIndexColumn(String name, int index)
    {
        if (m_Columns.containsKey(name)) {
            RowColumn column = m_Columns.get(name);
            column.setIndex(index);
        }
    }

    public boolean hasRowIdentifier()
    {
        return !m_Keys.isEmpty();
    }

    public boolean isIdentifierPrimaryKey()
    {
        return m_IsPrimary;
    }

    public void addRowIdentifier(String column, int index)
    {
        m_Keys.add(index, column);
    }

    public void setIdentifierAsPrimaryKey()
    {
        m_IsPrimary = true;
    }

    public void setDefaultRowIdentifier()
        throws SQLException
    {
        // XXX: We don't know the primary key of this table!!!!
        // XXX: First auto-increment column become default row identifier
        RowColumn column = getAutoIncrementColumn();
        if (column == null) {
            // XXX: Ok we need to find a unique row column...
            column = getPseudoIdentifierColumn();
        }
        if (column != null) {
            m_Keys.add(column.getName());
        }
    }

    private RowColumn getPseudoIdentifierColumn()
        throws SQLException
    {
        RowColumn identifier = null;
        String query = m_Catalog.getUniqueQuery();
        for (RowColumn column : m_Columns.values()) {
            System.out.println("RowTable.getPseudoIdentifierColumn() 1 Column: " + column.getName());
            // We are looking only for certain column type.
            if (RowHelper.isValidKeyType(column.getType())) {
                String command = String.format(query, column.getIdentifier(), getComposedName(true));
                try (ResultSet result = m_Catalog.getStatement().executeQuery(command)) {
                    if (!result.next()) {
                        identifier = column;
                        System.out.println("RowTable.getPseudoIdentifierColumn() 2 Column: " + column.getName());
                        break;
                    }
                }
            }
        }
        return identifier;
    }

    public List<String> getRowIdentifier()
    {
        return m_Keys;
    }

    public boolean hasColumn(String column)
    {
        if (m_Columns.containsKey(column)) {
            return m_Columns.get(column).getIndex() > 0;
        }
        return false;
    }

    public RowColumn getColumn(String column)
    {
        return m_Columns.get(column);
    }

    public Map<String, RowColumn> getColumnNames()
    {
        return m_Columns;
    }

    public RowColumn getRowIdentifierColumn()
    {
        RowColumn key = null;
        if (!m_Keys.isEmpty()) {
            key = m_Columns.get(m_Keys.get(0));
        }
        else {
            key = getAutoIncrementColumn();
        }
        return key;
    }

    public RowColumn getAutoIncrementColumn()
    {
        for (RowColumn column : m_Columns.values()) {
            if (column.isAutoIncrement() && column.getIndex() > 0) {
                return column;
            }
        }
        return null;
    }

    public String getCatalogName()
    {
        return m_Component.getCatalogName();
    }

    public String getSchemaName()
    {
        return m_Component.getSchemaName();
    }

    public String getName()
    {
        return m_Component.getTableName();
    }

    public String getComposedName(boolean quoted)
        throws SQLException
    {
        return DBTools.buildName(m_Catalog.getStatement(), m_Component, m_Catalog.getNamedSupport(), quoted);
    }

    public boolean isSameTable(ResultSetMetaData metadata,
                               int index)
        throws SQLException
    {
        return getCatalogName().equals(metadata.getCatalogName(index)) &&
               getSchemaName().equals(metadata.getSchemaName(index)) &&
               getName().equals(metadata.getTableName(index));
    }

    public boolean isSameTable(NamedComponents component)
        throws SQLException
    {
        return component != null &&
               getCatalogName().equals(component.getCatalogName()) &&
               getSchemaName().equals(component.getSchemaName()) &&
               getName().equals(component.getTableName());
    }

    public String getWhereCmd()
        throws SQLException
    {
        if (m_Where == null) {
            List<String> columns = new ArrayList<>();
            for (String key : m_Keys) {
                columns.add(m_Columns.get(key).getPredicate());
            }
            m_Where = String.join(m_Catalog.getAnd(), columns);
        }
        return m_Where;
    }

    public String getSeparator()
    {
        return m_Catalog.getSeparator();
    }

    public String getParameter()
    {
        return m_Catalog.getParameter();
    }

    public String getMark()
    {
        return m_Catalog.getMark();
    }

    public Collection<RowColumn> getColumns()
    {
        return m_Columns.values();
    }

}
