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

import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;


public class KeyColumnContainer
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
        System.out.println("sdbcx.KeyColumnContainer() 1");
        m_key = key;
        System.out.println("sdbcx.KeyColumnContainer() Count: " + getCount());
    }


    
    @Override
    protected KeyColumn _createElement(String name)
        throws SQLException
    {
        KeyColumn column = null;
        try {
            String catalog = m_key.getTable().getCatalog();
            String schema = m_key.getTable().getSchema();
            String table = m_key.getTable().getName();
            System.out.println("sdbcx.KeyColumnContainer._createElement() 1 : " + catalog + "." + schema + "." + table + "." + name);
            java.sql.ResultSet result = getConnection().getProvider().getConnection().getMetaData().getImportedKeys(catalog, schema, table);
            String refColumnName = "";
            System.out.println("sdbcx.KeyColumnContainer._createElement() 2 Name: " + name + " - Key: " + m_key.getName());
            while (result.next()) {
                System.out.println("sdbcx.KeyColumnContainer._createElement() 3 Name: " + result.getString(8) + " - Key: " + result.getString(12));
                if (result.getString(8).equals(name) && m_key.getName().equals(result.getString(12))) {
                    refColumnName = result.getString(4);
                    System.out.println("sdbcx.KeyColumnContainer._createElement() 4");
                    break;
                }
            }
            result.close();
            // now describe the column name and set its related column
            result = getConnection().getProvider().getConnection().getMetaData().getColumns(catalog, schema, table, name);
            System.out.println("sdbcx.KeyColumnContainer._createElement() 5");
            if (result.next()) {
                System.out.println("sdbcx.KeyColumnContainer._createElement() 6 Name: " + name + " - Column: " + result.getString(4));
                if (result.getString(4).equals(name)) {
                    int dataType = getConnection().getProvider().getDataType(result.getInt(5));
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
                    System.out.println("sdbcx.KeyColumnContainer._createElement() 7");
                    column = new KeyColumn(m_key.getTable(), isCaseSensitive(), name, typeName, "", columnDef, nul, size, dec, dataType, false, false, false, refColumnName);
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("sdbcx.KeyColumnContainer._createElement() ERROR\n" + UnoHelper.getStackTrace(e));
        }
        System.out.println("sdbcx.KeyColumnContainer._createElement() 8");
        return column;
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected KeyColumn _appendElement(XPropertySet descriptor, String name)
        throws SQLException
    {
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    
    @Override
    protected XPropertySet _createDescriptor()
    {
        return new KeyColumnDescriptor(isCaseSensitive());
    }

    @Override
    protected void _refresh()
    {
    }
    

    public ConnectionSuper getConnection()
    {
        return m_key.getTable().getConnection();
    }


}
