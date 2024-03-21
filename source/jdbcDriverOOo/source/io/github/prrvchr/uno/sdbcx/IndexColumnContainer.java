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
package io.github.prrvchr.uno.sdbcx;

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.StandardSQLState;


public final class IndexColumnContainer
    extends Container<IndexColumn>
{
    private static final String m_service = IndexColumnContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.IndexColumns",
                                                "com.sun.star.sdbcx.Container"};

    private final Index m_index;

    // The constructor method:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexDescriptor()
    public IndexColumnContainer(Index index,
                                List<String> columns)
        throws ElementExistException
    {
        super(m_service, m_services, index.getTable(), true, columns);
        m_index = index;
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }

    @Override
    protected XPropertySet createDescriptor()
    {
        return new IndexColumnDescriptor(isCaseSensitive());
    }
    
    @Override
    protected IndexColumn createElement(String name)
        throws SQLException
    {
        IndexColumn index = null;
        boolean isascending = true;
        String catalog = m_index.getTable().getCatalog();
        String schema = m_index.getTable().getSchema();
        String table = m_index.getTable().getName();
        try {
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getIndexInfo(catalog, schema, table, false, false))
            {
                while (result.next()) {
                    if (name.equals(result.getString(9))) {
                        isascending = !"D".equals(result.getString(10));
                    }
                }
            }
            try (java.sql.ResultSet result = metadata.getColumns(catalog, schema, table, name))
            {
                while (result.next()) {
                    if (name.equals(result.getString(4))) {
                        int datatype = getConnection().getProvider().getDataType(result.getInt(5));
                        String typename = result.getString(6);
                        int precision = result.getInt(7);
                        int scale = result.getInt(9);
                        scale = result.wasNull() ? 0 : scale;
                        int nullable = result.getInt(11);
                        String defaultvalue = result.getString(13);
                        defaultvalue = result.wasNull() ? "" : defaultvalue;
                        index = new IndexColumn(m_index.getTable(), isCaseSensitive(), name, typename, defaultvalue,
                                                "", nullable, precision, scale, datatype, false, false, false, isascending);
                        break;
                    }
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return index;
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.IndexColumnContainer.refreshInternal() *********************************");
        // FIXME
    }

    @Override
    protected IndexColumn appendElement(XPropertySet descriptor)
        throws SQLException
    {
        System.out.println("sdbcx.IndexColumnContainer.appendElement() *********************************");
        throw new SQLException("Unsupported");
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        System.out.println("sdbcx.IndexColumnContainer.removeDataBaseElement() *********************************");
        throw new SQLException("Unsupported");
    }

    protected ConnectionSuper getConnection()
    {
        return m_index.getTable().getConnection();
    }

    protected void renameIndexColumn(String oldname, String newname)
        throws SQLException
    {
        if (hasByName(oldname)) {
            replaceElement(oldname, newname);
        }
    }

}
