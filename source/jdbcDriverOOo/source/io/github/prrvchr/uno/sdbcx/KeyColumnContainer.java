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

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.StandardSQLState;


public final class KeyColumnContainer
    extends Container<KeyColumn>
{
    private static final String m_service = IndexContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.KeyColumns",
                                                "com.sun.star.sdbcx.Container"};

    protected final Key m_key;

    // The constructor method:
    public KeyColumnContainer(Key key,
                              List<String> columns)
        throws ElementExistException
    {
        super(m_service, m_services, key.getTable(), true, columns);
        m_key = key;
    }


    
    @Override
    protected KeyColumn createElement(String name)
        throws SQLException
    {
        KeyColumn column = null;
        try {
            DriverProvider provider = getConnection().getProvider();
            String catalog = m_key.getTable().getCatalog();
            String schema = m_key.getTable().getSchema();
            String table = m_key.getTable().getName();
            String refColumnName = "";
            try (java.sql.ResultSet result = provider.getConnection().getMetaData().getImportedKeys(catalog, schema, table))
            {
                while (result.next()) {
                    if (name.equals(result.getString(8)) && m_key.getName().equals(result.getString(12))) {
                        refColumnName = result.getString(4);
                        break;
                    }
                }
            }
            // now describe the column name and set its related column
            try (java.sql.ResultSet result = provider.getConnection().getMetaData().getColumns(catalog, schema, table, name))
            {
                if (result.next()) {
                    if (result.getString(4).equals(name)) {
                        int dataType = provider.getDataType(result.getInt(5));
                        String typeName = result.getString(6);
                        int size = result.getInt(7);
                        int dec = result.getInt(9);
                        int nul = result.getInt(11);
                        String columnDef = "";
                        try {
                            columnDef = result.getString(13);
                        }
                        catch (java.sql.SQLException e) {
                            // sometimes we get an error when asking for this param
                        }
                        column = new KeyColumn(m_key.getTable(), isCaseSensitive(), name, typeName, "", columnDef, nul, size, dec, dataType, false, false, false, refColumnName);
                    }
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return column;
    }

    @Override
    protected KeyColumn appendElement(XPropertySet descriptor)
        throws SQLException
    {
        System.out.println("sdbcx.KeyColumnContainer.appendElement() ******************************************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException
    {
        System.out.println("sdbcx.KeyColumnContainer.removeDataBaseElement() ******************************************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    
    @Override
    protected XPropertySet createDescriptor()
    {
        return new KeyColumnDescriptor(isCaseSensitive());
    }

    @Override
    protected void refreshInternal()
    {
    }

    protected void renameKeyColumn(String oldname, String newname)
        throws SQLException
    {
        if (hasByName(oldname)) {
            replaceElement(oldname, newname);
        }
    }

    public ConnectionSuper getConnection()
    {
        return m_key.getTable().getConnection();
    }

}
