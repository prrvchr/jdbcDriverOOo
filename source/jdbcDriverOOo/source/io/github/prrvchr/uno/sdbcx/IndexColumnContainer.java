/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class IndexColumnContainer
    extends Container
{

    private final Index m_index;

    // The constructor method:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexDescriptor()
    public IndexColumnContainer(Object lock,
                                Index index,
                                List<String> columns)
        throws ElementExistException
    {
        super(lock, true, columns);
        m_index = index;
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }

    @Override
    protected IndexColumnDescriptor _createDescriptor()
    {
        return new IndexColumnDescriptor(isCaseSensitive());
    }
    
    @Override
    protected IndexColumn _createElement(String name)
        throws SQLException
    {
        IndexColumn index = null;
        try {
            java.sql.DatabaseMetaData metadata = _getConnection().getProvider().getConnection().getMetaData();
            String catalog = m_index.getTable().getCatalogName();
            String schema = m_index.getTable().getSchemaName();
            String table = m_index.getTable().getName();
            System.out.println("sdbcx.IndexColumnContainer._createElement() 1 : " + catalog + "." + schema + "." + table);
            boolean isAscending = true;
            java.sql.ResultSet result = metadata.getIndexInfo(catalog, schema, table, false, false);
            while (result.next()) {
                System.out.println("sdbcx.IndexColumnContainer._createElement() 2");
                if (name.equals(result.getString(9))) {
                    System.out.println("sdbcx.IndexColumnContainer._createElement() 3");
                    isAscending = !"D".equals(result.getString(10));
                }
            }
            result.close();

            result = metadata.getColumns(catalog, schema, table, name);
            while (result.next()) {
                System.out.println("sdbcx.IndexColumnContainer._createElement() 4");
                if (name.equals(result.getString(4))) {
                    System.out.println("sdbcx.IndexColumnContainer._createElement() 5");
                    int dataType = _getConnection().getProvider().getDataType(result.getInt(5));
                    String typeName = result.getString(6);
                    int size = result.getInt(7);
                    int dec = result.getInt(9);
                    int nul = result.getInt(11);
                    String columnDef = result.getString(13);
                    index = new IndexColumn(isCaseSensitive(), m_index.getTable().m_CatalogName,
                                            m_index.getTable().m_SchemaName, table, name, typeName, columnDef,
                                            "", nul, size, dec, dataType, false, false, false, isAscending);
                    System.out.println("sdbcx.IndexColumnContainer._createElement() 6");
                    break;
                }
            }
            result.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        System.out.println("sdbcx.IndexColumnContainer._createElement() 7");
        return index;
    }

    @Override
    protected void _refresh() {
        System.out.println("sdbcx.IndexColumnContainer._refresh() *********************************");
        // FIXME
    }

    @Override
    protected IndexColumn _appendElement(XPropertySet descriptor,
                                         String name)
        throws SQLException
    {
        System.out.println("sdbcx.IndexColumnContainer._appendElement() *********************************");
        throw new SQLException("Unsupported");
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        System.out.println("sdbcx.IndexColumnContainer._removeElement() *********************************");
        throw new SQLException("Unsupported");
    }

    protected ConnectionSuper _getConnection()
    {
        return m_index.getTable().getConnection();
    }


}
