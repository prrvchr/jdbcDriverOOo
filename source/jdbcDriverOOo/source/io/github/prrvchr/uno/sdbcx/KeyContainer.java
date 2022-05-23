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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class KeyContainer
    extends ContainerBase<Key>
{

    private final TableBase m_Table;

    // The constructor method:
    public KeyContainer(ConnectionBase connection)
    {
        super(connection);
        m_Table = null;
    }
    public KeyContainer(ConnectionBase connection,
                        TableBase table)
    {
        super(connection);
        m_Table = table;
        refresh();
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(Key key)
    {
        return null;
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.KeyContainer.appendByDescriptor() 1 ***************************");
        try {
            Key key = new Key(m_Connection, descriptor, (String) descriptor.getPropertyValue("Name"));
            m_Elements.add(key);
            elementInserted(key);
            System.out.println("sdbcx.KeyContainer.appendByDescriptor() 2");
        } 
        catch (java.sql.SQLException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        if(m_Table != null) {
            try {
                m_Elements.clear();
                java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getPrimaryKeys(null, m_Table.m_SchemaName, m_Table.m_Name);
                while (result.next()) {
                    String column = result.getString(4);
                    String name = result.getString(6);
                    Key key = new Key(m_Connection, m_Table.m_CatalogName, m_Table.m_SchemaName, m_Table.m_Name, column, name, 1, "", 0, 0);
                    m_Elements.add(key);
                }
                result.close();
            } catch (java.sql.SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return new KeyDescriptor(m_Connection, m_Table);
    }


}
